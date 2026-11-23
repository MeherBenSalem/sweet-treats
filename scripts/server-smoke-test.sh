#!/usr/bin/env bash
# Starts a dev server for one Minecraft version and loader (./gradlew :<loader>:runServer in that workspace), places
# every Sweet Treats block with /setblock from a datapack (each on a stone block), lets the world run for 20 seconds and stops.
# Fails if the server crashes, never gets there, the datapack function does not load (an unknown block id makes it fail
# to parse), not every block was placed, or the log has an error that names the mod.
#   usage: scripts/server-smoke-test.sh <minecraft version folder> <fabric|forge|neoforge>
set -u
VERSION="$1"; LOADER="$2"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT/$VERSION"
RUN="$LOADER/runs/server"
rm -rf "$RUN/world" "$RUN/crash-reports" "$RUN/logs"
PACK="$RUN/world/datapacks/sweet_smoke"
mkdir -p "$PACK"
echo "eula=true" > "$RUN/eula.txt"
if [ "$LOADER" = forge ]; then
  # In the Forge 1.20.1 dev environment the LAN advertiser loads a client-only class and the dedicated server stops.
  mkdir -p "$RUN/defaultconfigs"
  printf '[server]\n\tadvertiseDedicatedServerToLan = false\n' > "$RUN/defaultconfigs/forge-server.toml"
fi
cat > "$RUN/server.properties" <<PROPS
online-mode=false
function-permission-level=4
spawn-protection=0
view-distance=4
simulation-distance=4
level-type=minecraft\:flat
level-seed=20261123
initial-enabled-packs=vanilla,file/sweet_smoke
PROPS
# The mod's own pack object (pack_format / supported_formats / min_format+max_format) fits this Minecraft version.
python3 - common/src/main/resources/pack.mcmeta "$PACK/pack.mcmeta" <<'PY'
import json, sys
pack = json.load(open(sys.argv[1]))["pack"]
pack["description"] = "Sweet Treats smoke test"
json.dump({"pack": pack}, open(sys.argv[2], "w"))
PY

IDS=$(ls common/src/main/resources/assets/sweettreats/blockstates | sed 's/\.json$//')
COUNT=$(echo "$IDS" | grep -c .)
# 1.20.1 reads functions/ and tags/functions/, 1.21+ reads function/ and tags/function/.
for dir in function functions; do
  mkdir -p "$PACK/data/sweet_smoke/$dir" "$PACK/data/minecraft/tags/$dir"
  echo '{"values":["sweet_smoke:start"]}' > "$PACK/data/minecraft/tags/$dir/load.json"
  printf 'forceload add 0 0 63 63\nschedule function sweet_smoke:place 100t\n' > "$PACK/data/sweet_smoke/$dir/start.mcfunction"
  {
    i=0
    for id in $IDS; do
      x=$(( (i % 10) * 3 )); z=$(( (i / 10) * 3 ))
      echo "setblock $x 9 $z minecraft:stone"
      echo "setblock $x 10 $z sweettreats:$id"
      i=$((i + 1))
    done
    echo "say SWEET_SMOKE_PLACED $i"
    echo 'schedule function sweet_smoke:finish 400t'
  } > "$PACK/data/sweet_smoke/$dir/place.mcfunction"
  printf 'say SWEET_SMOKE_OK\nstop\n' > "$PACK/data/sweet_smoke/$dir/finish.mcfunction"
done

LOG="$ROOT/server-smoke-$VERSION-$LOADER.log"
# Lets the workspace's toolchain lookup find the JDKs setup-java installs (Java 17 for 1.20.1 bytecode).
./gradlew ":$LOADER:runServer" --stacktrace \
  -Porg.gradle.java.installations.fromEnv=JAVA_HOME_17_X64,JAVA_HOME_21_X64,JAVA_HOME_25_X64 \
  < /dev/null > "$LOG" 2>&1 &
PID=$!
result=timeout
for _ in $(seq 1 180); do
  sleep 5
  if grep -q "SWEET_SMOKE_OK" "$LOG"; then result=ok; break; fi
  if grep -q -E "This crash report has been saved|Encountered an unexpected exception|Failed to start the minecraft server|Failed to load function sweet_smoke|Mixin apply.*failed|InvalidInjectionException" "$LOG"; then result=crash; break; fi
  if ! kill -0 $PID 2>/dev/null; then result=exited; break; fi
done
sleep 10
pkill -P $PID 2>/dev/null; kill $PID 2>/dev/null; wait $PID 2>/dev/null

grep -n -i -E "SWEET_SMOKE|sweettreats|Sweet Treats|Done \(" "$LOG" | grep -v -E "Download|Task :" | head -40
for f in "$RUN"/crash-reports/*.txt; do [ -f "$f" ] && { echo "=== $f"; head -80 "$f"; }; done
grep -n -E "ERROR|Exception|Caused by|[Mm]ixin.*(fail|error)" "$LOG" | head -60
echo "Result: $result"
if [ "$result" != ok ]; then
  # The Gradle output (compile errors included) only lives in $LOG, so show the useful part here.
  grep -n -E ": error:|What went wrong|FAILURE:" -A6 "$LOG" | head -150
  echo "=== last lines of $LOG"; tail -60 "$LOG"
fi
if [ "$result" != ok ] || grep -q -E "This crash report has been saved|InvalidInjectionException" "$LOG"; then exit 1; fi
grep -q "SWEET_SMOKE_PLACED $COUNT" "$LOG" || { echo "Not every block was placed"; exit 1; }
if grep -E "ERROR|Exception|Caused by" "$LOG" | grep -q -E 'com.sugarplumstudios|sweettreats([^-.a-z]|$)'; then echo "The log has errors naming Sweet Treats"; exit 1; fi
echo "Server smoke test passed ($VERSION $LOADER)"

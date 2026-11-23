#!/usr/bin/env bash
# Boots a real Forge 1.20.1 dedicated server with one built Sweet Treats jar in its mods folder, places every Sweet
# Treats block with /setblock from a datapack (each on a stone block), lets the world run for 20 seconds and stops.
# Fails if the server crashes, the mod does not load, the datapack function does not load (an unknown block id makes
# it fail to parse), or the log has an error that names the mod.
#   usage: scripts/server-boot-test.sh <path to the mod jar>
set -u
JAR="$(cd "$(dirname "$1")" && pwd)/$(basename "$1")"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
MC=$(grep '^minecraft_version=' "$ROOT/gradle.properties" | cut -d= -f2)
FORGE=$(grep '^forge_version=' "$ROOT/gradle.properties" | cut -d= -f2)
SERVER="$ROOT/build/boot-test-server"
IDS=$(ls "$ROOT/src/main/resources/assets/sweettreats/blockstates" | sed 's/\.json$//')
COUNT=$(echo "$IDS" | grep -c .)
rm -rf "$SERVER"; mkdir -p "$SERVER/mods"
cd "$SERVER"

curl -fsSL -o installer.jar "https://maven.minecraftforge.net/net/minecraftforge/forge/$MC-$FORGE/forge-$MC-$FORGE-installer.jar"
java -jar installer.jar --installServer > install.log 2>&1 || { tail -40 install.log; exit 1; }
cp "$JAR" mods/

echo "eula=true" > eula.txt
cat > server.properties <<PROPS
online-mode=false
function-permission-level=4
spawn-protection=0
view-distance=4
simulation-distance=4
level-type=minecraft\:flat
level-seed=20261123
initial-enabled-packs=vanilla,file/sweet_boot
PROPS
PACK="world/datapacks/sweet_boot"
mkdir -p "$PACK/data/sweet_boot/functions" "$PACK/data/minecraft/tags/functions"
echo '{"pack":{"pack_format":15,"description":"Sweet Treats boot test"}}' > "$PACK/pack.mcmeta"
echo '{"values":["sweet_boot:start"]}' > "$PACK/data/minecraft/tags/functions/load.json"
printf 'forceload add 0 0 63 63\nschedule function sweet_boot:place 100t\n' > "$PACK/data/sweet_boot/functions/start.mcfunction"
{
  i=0
  for id in $IDS; do
    x=$(( (i % 10) * 3 )); z=$(( (i / 10) * 3 ))
    echo "setblock $x 9 $z minecraft:stone"
    echo "setblock $x 10 $z sweettreats:$id"
    i=$((i + 1))
  done
  echo "say SWEET_BOOT_PLACED $i"
  echo 'schedule function sweet_boot:finish 400t'
} > "$PACK/data/sweet_boot/functions/place.mcfunction"
printf 'say SWEET_BOOT_OK\nstop\n' > "$PACK/data/sweet_boot/functions/finish.mcfunction"

LOG="$SERVER/boot.log"
bash run.sh nogui < /dev/null > "$LOG" 2>&1 &
PID=$!
result=timeout
for _ in $(seq 1 120); do
  sleep 5
  if grep -q "SWEET_BOOT_OK" "$LOG"; then result=ok; break; fi
  if grep -q -E "This crash report has been saved|Encountered an unexpected exception|Failed to start the minecraft server|Failed to load function sweet_boot" "$LOG"; then result=crash; break; fi
  if ! kill -0 $PID 2>/dev/null; then result=exited; break; fi
done
sleep 10
pkill -P $PID 2>/dev/null; kill $PID 2>/dev/null; wait $PID 2>/dev/null

grep -n -i -E "SWEET_BOOT|sweettreats|Sweet Treats|Done \(" "$LOG" | head -40
for f in crash-reports/*.txt; do [ -f "$f" ] && { echo "=== $f"; head -80 "$f"; }; done
grep -n -E "ERROR|Exception|Caused by" "$LOG" | head -60
echo "Result: $result"
[ "$result" = ok ] || exit 1
grep -q "This crash report has been saved" "$LOG" && exit 1
grep -q -E "Done \([0-9.]+s\)!" "$LOG" || { echo "The server never finished starting"; exit 1; }
grep -q "SWEET_BOOT_PLACED $COUNT" "$LOG" || { echo "Not every block was placed"; exit 1; }
if grep -E "ERROR|Exception|Caused by" "$LOG" | grep -q -E 'com.sugarplumstudios|sweettreats'; then echo "The log has errors naming Sweet Treats"; exit 1; fi
echo "Server boot test passed: $COUNT blocks placed"

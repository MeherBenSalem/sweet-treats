# Sweet Treats 🍓 — a cute bakery mod by Sugarplum Studios

*A Sugarplum Studios mod for Minecraft Java 1.20.1 (Fabric, Forge), 1.21.1, 26.2 and 26.3 (Fabric, NeoForge).* Mod id: `sweettreats`.

Bake pastel pastries, brew teas, mix milkshakes and show everything off in your own bakery.

## Features

### Treats (24)
| Group | Treats |
|---|---|
| Cakes & pastries | Strawberry Cake *(placeable, 7 slices)*, Pink Vanilla Cupcake, Chocolate Cupcake, Lavender Macaron, Strawberry Macaron, Heart Cookie, Cinnamon Roll, Mini Donuts, Peach Pastry, Cherry Pie *(placeable, 4 slices)* |
| Drinks | Strawberry Milk, Vanilla Milkshake, Lavender Tea, Peach Tea, Bubble Tea, Pink Lemonade, Hot Chocolate, Rose Tea |
| Special treats | Heart Candy, Strawberry Lollipop, Marshmallow Cloud, Rainbow Cookie, Honey Cupcake, Moon Cookie |

Drinks use the drinking animation, can always be sipped, and hand back their glass bottle or teacup.
Many treats give a small, short bonus (all configurable):

| Treat | Bonus |
|---|---|
| Lavender Tea | Regeneration I, 10s |
| Peach Tea | Haste I, 45s |
| Hot Chocolate | Resistance I, 30s |
| Rose Tea | Absorption I, 60s |
| Bubble Tea | Jump Boost I, 45s |
| Pink Lemonade | Speed I, 30s |
| Lavender Macaron | Regeneration I, 5s |
| Heart Candy | Regeneration I, 4s |
| Marshmallow Cloud | Slow Falling I, 10s |
| Rainbow Cookie | Luck I, 60s |
| Moon Cookie | Night Vision I, 30s |

Hunger values sit between vanilla cookies (2) and bread (5). Multi-step treats restore more (cinnamon roll, peach pastry and honey cupcake give 6).

### Ingredients and crops
* **Crops on farmland:** Strawberries, Vanilla and Lavender (8 growth stages, bone meal and Fortune work, bees pollinate them).
* **Fruit trees:** Peach Tree and Cherry Tree, planted from pits. They grow sapling → young → blossoming → fruiting. Right-click to harvest and the tree regrows its fruit.
* **Wild plants** generate in the world: wild strawberries (plains, forests, meadows), wild lavender (meadows, flower forests, cherry groves) and wild vanilla (jungles).
* **Extra drops:** grass, tall grass and ferns sometimes drop seeds and pits. Cherry leaves drop cherries.
* **Wandering traders** sell seeds and pits (Minecraft 1.20.1 and 1.21.1).
* **Kitchen ingredients:** Flour, Cream, Butter, Cocoa Powder, Chocolate, Cinnamon, Sugar Flowers, Icing, Pink Icing, Chocolate Frosting, Rainbow Sprinkles, Tapioca Pearls, Cake Batter, Cookie Dough, Pastry Dough, Macaron Batter, Sponge Cake, Plain Cupcake, Sugar Cookie, Macaron Shells and Teacup.

### Cooking system
* **Bakery Oven:** 4 ingredient slots, a fuel slot (any furnace fuel) and an output slot. It glows and crackles while baking, puffs smoke from its top, rings a bell and pops hearts when a batch is done, and gives experience like a furnace. Hoppers insert ingredients from the top and fuel from the sides, and extract from the bottom.
* **Mixing Bowl:** 6 ingredient slots, a container slot (glass bottles for drinks) and an output slot. It needs no fuel. The ingredients visibly swirl inside the bowl while they mix. It supports the same hopper sides.
* **Decoration Station:** a base treat plus up to two toppings becomes a decorated treat instantly, with a sparkly chime. Leftover containers (honey bottles → glass bottles) are handed back.
* **Stack-aware recipes:** an ingredient listed twice (e.g. 2× Sugar) can come from a single stack, so automation is easy.
* **Recipe Book:** every player gets one on first join (craft another from Book + Sugar). It has four tabs: Oven, Mixing Bowl, Decorating and Crafting. Each recipe shows its ingredients (alternatives cycle), station, time, food value and bonus effect.
  * **Discovery tracking:** a recipe becomes *discovered* the first time you make or pick up its result. You get a chime and a "New recipe discovered" message. Undiscovered recipes appear as dark silhouettes, and each tab shows a `found / total` counter. Discoveries survive death.
  * The **Bakery Menu Board** block also opens the recipe book.

### Bakery decorations
| Block | What it does |
|---|---|
| Cake Display | Glass showcase: holds one treat that slowly turns. Cakes and pies show as tiny real cakes. |
| Bakery Counter | Full-block counter: shows one treat on top. |
| Cupcake Stand | Three porcelain tiers, one treat on each. |
| Dessert Plate | Holds one treat. |
| Glass Jar | Holds a whole stack of one treat, shown piled up inside. Sneak-click to take the whole stack. |
| Tea Set | Decorative tray with teapot and cups. |
| Bakery Sign | Wall-mounted shop sign. |
| Bakery Menu Board | Chalkboard easel that opens the recipe book. |

Right-click a display with a treat to place it and with an empty hand to take it back. Comparators read how full a display is.
**Placeable cakes:** put a Strawberry Cake or Cherry Pie on any solid block and eat it slice by slice. Sneak-right-click an uneaten cake with an empty hand to pick it back up. Sneak-click a counter while holding a cake to place the cake block on top of it.

## Recipes
See [docs/RECIPES.md](docs/RECIPES.md) for the full list. The in-game recipe book shows it too.

**Progression:** Crafting Table (flour, cream, cocoa powder, mixing bowl) → Mixing Bowl (batters, doughs, icings) → Bakery Oven (furnace + terracotta + bricks) → Decoration Station (needs icing and sugar flowers from the bowl).

## Configuration
`config/sweettreats-common.toml`:

| Option | Default | Meaning |
|---|---|---|
| `food.foodEffects` | true | Treat bonus effects on/off |
| `food.effectDurationMultiplier` | 1.0 | Scales every effect duration |
| `machines.ovenSpeedMultiplier` | 1.0 | Oven speed |
| `machines.ovenRequiresFuel` | true | Set false for a fuel-free oven |
| `machines.mixingSpeedMultiplier` | 1.0 | Mixing Bowl speed |
| `recipe_book.giveRecipeBookOnFirstJoin` | true | Starter recipe book |
| `recipe_book.discoveryMessages` | true | Chime and message on discovery |
| `recipe_book.hideUndiscoveredIngredients` | false | Hide ingredients of undiscovered recipes |
| `world.grassSeedDrops` | true | Seeds and pits from grass and ferns |
| `world.cherryLeafDrops` | true | Cherries from cherry leaves |
| `world.dropChanceMultiplier` | 1.0 | Scales those drop chances |
| `world.fruitTreeGrowthChance` | 5 | Fruit trees grow with a 1-in-N chance per random tick |
| `world.wanderingTraderTrades` | true | Seeds sold by wandering traders (1.20.1 and 1.21.1 only) |

`config/sweettreats-client.toml`: `visuals.cookingParticles`, `visuals.cookingAnimations`.

On Forge and NeoForge, world generation uses normal biome modifiers (`data/sweettreats/forge/biome_modifier`, `data/sweettreats/neoforge/biome_modifier`), so a datapack can turn it off or change it. On Fabric the wild plants are added through Fabric API's biome modifications, and the grass and cherry leaf drops are added to the vanilla loot tables when they load.

## Which jar do I download?

Jar names are `sweettreats-{loader}-{minecraft}-{modVersion}.jar`. Match **both** the Minecraft version and the loader.

| Minecraft | Java | Fabric jar (needs Fabric API) | NeoForge / Forge jar |
|-----------|------|-------------------------------|----------------------|
| 1.20.1 | 17+ | `…-fabric-1.20.1-….jar` | `…-forge-1.20.1-….jar` (Forge 47) |
| 1.21.1 | 21+ | `…-fabric-1.21.1-….jar` | `…-neoforge-1.21.1-….jar` |
| 26.2 | 25+ | `…-fabric-26.2-….jar` | `…-neoforge-26.2-….jar` |
| 26.3 | 25+ | `…-fabric-26.3-….jar` | `…-neoforge-26.3-….jar` |

The mod is needed on both the server and the client.

## Building

Each Minecraft version is a separate MultiLoader Gradle workspace with its own wrapper. These are the only supported
targets (the NightBeam convention): 8 jars per release, all with the same version.

| Minecraft | Loaders | Java | Folder |
| --- | --- | --- | --- |
| 1.20.1 | Forge, Fabric | 17 bytecode (build on 21) | `1.20.1/{common,forge,fabric}` |
| 1.21.1 | NeoForge, Fabric | 21 | `1.21.1/{common,neoforge,fabric}` |
| 26.2 | NeoForge, Fabric | 25 | `26.2/{common,neoforge,fabric}` |
| 26.3 | NeoForge, Fabric | 25 | `26.3/{common,neoforge,fabric}` |

```bash
./gradlew buildAll             # every workspace, then the 8 loader jars into releases/
cd 1.20.1 && ./gradlew build   # Gradle on Java 21 (Java 17 toolchain)
cd 1.21.1 && ./gradlew build   # Java 21
cd 26.3 && ./gradlew build     # Java 25 (the same for 26.2)
```

The root build hands the 26.x workspaces `JAVA_HOME_25_X64` (or `-Pjava25Home=<jdk>`) and the others
`JAVA_HOME_21_X64` (or `-Pjava21Home=<jdk>`); without them each workspace uses the current `JAVA_HOME`.
Jars land in `<workspace>/<loader>/build/libs/sweettreats-<loader>-<minecraft>-<version>.jar`. Every workspace's
`gradle.properties` carries the same `version=` (`./gradlew checkVersions`; set it with
`./gradlew syncVersion -PmodVersion=x.y.z`). `python3 scripts/check_targets.py workspaces` checks the targets, the
shared version and the metadata ranges.

| Command | What it does |
|---|---|
| `cd 1.20.1 && ./gradlew :forge:runGameTestServer` | Runs the 13 in-game GameTests headless, then exits (non-zero on failure). |
| `cd 1.20.1 && ./gradlew :forge:runVisualTestClient` | Development only: builds a demo bakery, screenshots every screen into `1.20.1/forge/runs/visualtest/screenshots/`, then quits. |
| `cd 1.20.1 && ./gradlew :forge:runShowcaseClient` | Development only: builds the marketing bakery shop and takes the 1920×1080 listing screenshots into `1.20.1/forge/runs/showcase/screenshots/`. |
| `scripts/server-smoke-test.sh <version> <loader>` | Starts that workspace's dev server and places every block. |
| `python tools/generate_resources.py` | Regenerates the JSON data, lang and `docs/RECIPES.md` in the 1.20.1 workspace. |
| `python tools/validate_resources.py` | Checks models, textures, lang and loot against the 1.20.1 registry. |

Textures are generated by `tools/textures/gen_item_textures.py`, `gen_block_textures.py` and `gen_gui_textures.py`
(into the 1.20.1 workspace). The dev harnesses live in `1.20.1/forge/.../dev/` and are left out of the jar.

## Releasing

CI (`.github/workflows/ci.yml`) builds all 8 jars on every push to `main`, checks the 1.20.1 Forge jar, runs the
GameTests, boots a Forge server with the built jar, starts a dev server for every target that places every block, and
launches a 1.20.1 Forge client that screenshots every screen. Publishing (to CurseForge only) is
`.github/workflows/publish.yml`, run by hand (`version`, `dry_run`) or by a `v*.*.*` tag; it never runs on a plain
push. It refuses a second release of the mod on the same day (Africa/Tunis), takes the jars from `releases/`, the
GitHub Release or a build from source, and fails unless exactly the 8 jars are found. It needs the secrets
`CURSEFORGE_TOKEN` (upload) and `CURSEFORGE_API_KEY` (Core API, for the same-day guard) and the variable
`CURSEFORGE_ID`; until the variable is set a real release stops before building (a dry run only warns).
`release/1.0.0/` keeps the original Forge-only 1.0.0 build.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

## Security

See [.github/SECURITY.md](.github/SECURITY.md).

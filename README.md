# Sweet Treats 🍓 — a cute bakery mod by Sugarplum Studios

Minecraft **Java 1.20.1**, **Forge 47.x**. Mod id: `sweettreats`.

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
* **Wandering traders** sell seeds and pits.
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
| `world.wanderingTraderTrades` | true | Seeds sold by wandering traders |

`config/sweettreats-client.toml`: `visuals.cookingParticles`, `visuals.cookingAnimations`.

World generation uses normal Forge biome modifiers (`data/sweettreats/forge/biome_modifier`), so a datapack can turn it off or change it.

## Building and testing
```bash
./gradlew build                 # jar in build/libs/sweettreats-forge-1.20.1-1.0.0.jar
./gradlew runGameTestServer     # runs the 12 in-game GameTests headless, then exits
./gradlew runVisualTestClient   # dev-only: builds a demo bakery, screenshots every GUI to run-visualtest/screenshots, quits
./gradlew runClient             # play in a dev client
python tools/generate_resources.py   # regenerate JSON data, lang and docs/RECIPES.md
python tools/validate_resources.py   # check models/textures/lang/loot against the registry
```
Textures are generated by `tools/textures/gen_item_textures.py`, `gen_block_textures.py` and `gen_gui_textures.py`.

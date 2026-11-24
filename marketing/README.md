# Marketing assets: 1.0

Listing copy and artwork for CurseForge (NightBeam publishes to CurseForge only). Nothing here ships in the mod JAR.

## Listing

| File | Use |
| --- | --- |
| [`curseforge/description.md`](curseforge/description.md) | Full CurseForge project description (Markdown) |
| [`curseforge/summary.txt`](curseforge/summary.txt) | Short CurseForge project summary (201 characters) |
| `images/icon.png` | Project avatar |

Image links in the description point at `raw.githubusercontent.com/MeherBenSalem/sweet-treats/main/marketing/images/...`. **They only work for visitors once this folder is on `main` and the repository is public.** While the repository is private, upload the images to CurseForge instead (the description editor takes uploads) and replace each URL.

The description leads with the pitch, then the treats, drinks, the Bakery Oven, the Mixing Bowl, the Decoration Station, the Recipe Book, the garden, the bakery decorations, getting started, configuration, everything else, downloads and compatibility, the FAQ and a gallery.

## Adding a Minecraft version

The supported versions are listed in these places:

1. `VERSIONS` at the top of [`source/render.cjs`](source/render.cjs). Add one line, for example `{ minecraft: '26.4', loaders: ['Fabric', 'NeoForge'] },`. The banner's loader pills and version line are built from it. Then rerun the script for `banner`.
2. The table under **Downloads & Compatibility** in `curseforge/description.md`. Add one row per loader.
3. The FAQ answer **Is there a Fabric or NeoForge version?** in `curseforge/description.md`.

No other copy or artwork names a Minecraft version. A new release number goes in `RELEASE` in `render.cjs` and in the banner's alt text on the first line of the description.

## Images

| File | Size | Use |
| --- | --- | --- |
| `images/banner.png` | 1800×600, 829 KB | Description header: the bakery interior, the title, "A cute bakery mod.", four treats, the loader pill and version line |
| `images/icon.png` | 512×512, 117 KB | Project avatar: the Pink Vanilla Cupcake in a candy-striped ring |
| `images/card-treats.png` | 1600×900, 575 KB | The 16 baked and special treats; 24 treats, 8 drinks, 11 bonus effects, 64 recipes |
| `images/card-drinks.png` | 1600×900, 613 KB | The eight drinks with their bonus or returned bottle |
| `images/card-oven.png` | 1600×900, 581 KB | The Bakery Oven idle and baking, and its screen |
| `images/card-mixing.png` | 1600×900, 591 KB | The Mixing Bowl, its ingredients and its screen |
| `images/card-decorating.png` | 1600×900, 538 KB | Four decorating recipes and the Decoration Station screen |
| `images/card-book.png` | 1600×900, 829 KB | The Recipe Book's Bakery Oven and Decorating tabs |
| `images/card-garden.png` | 1600×900, 593 KB | The three crops and the fruit tree stages |
| `images/card-displays.png` | 1600×900, 974 KB | The display counter shot and six decoration blocks |
| `images/card-config.png` | 1600×900, 536 KB | Excerpts of `sweettreats-common.toml` and `sweettreats-client.toml` |
| `images/header-*.png` | 1400×120, transparent, 30–51 KB | 14 section headings for the description |
| `images/divider.png` | 1400×40, transparent | Section divider |
| `images/screenshots/*.jpg` | 1920×1080, 91–380 KB | 13 captures: gallery images and card art |

### Where the pictures come from

- **Screenshots** are the unedited captures in `source/captures/`, converted to JPEG. They are taken by the marketing showcase (`./gradlew :forge:runShowcaseClient` in `1.20.1/`, `1.20.1/forge/src/main/java/.../dev/MarketingShowcase.java`, dev-only and not in the jar): a flat creative world in Peaceful at 1920×1080 with the HUD hidden for the world shots. It builds the bakery shop, its kitchen, the display counter, the café tables, the crops and the fruit trees itself, so every Sweet Treats block in them is placed and filled by the mod's own code. The **Marketing screenshots** workflow (`.github/workflows/marketing-screenshots.yml`) runs it under Xvfb and commits fresh captures to `source/captures/` on main; it runs only by hand, from the Actions tab, after changing the showcase.
- **Block renders** on the cards are isometric renders of the mod's own block models and textures, made without Minecraft by [`source/renders.py`](source/renders.py). They are in `source/renders/`, one PNG per model: the stations, the displays, the placeable cakes, the crops, each fruit tree stage and the wild plants.
- **Item art** on the cards, banner and icon is the mod's own item textures from `1.20.1/common/src/main/resources/assets/sweettreats/textures/item/`, scaled up with nearest-neighbour.

## Regenerating the artwork

```bash
python marketing/source/renders.py
python marketing/source/screenshots.py
NODE_PATH="$(npm root -g)" node marketing/source/render.cjs            # everything
NODE_PATH="$(npm root -g)" node marketing/source/render.cjs banner icon  # selected assets
```

- `renders.py` needs only Pillow and numpy. The few vanilla parent models it uses (cake, crop, cross, orientable) are written into the script.
- `screenshots.py` converts `source/captures/*.png` to `images/screenshots/*.jpg` and makes `source/backdrop.jpg`, the blurred card background.
- Set `CHROMIUM=/path/to/chromium` to render with a Chromium other than Playwright's own download.
- The **Marketing artwork** workflow (`.github/workflows/marketing-artwork.yml`) runs `render.cjs` on GitHub's Ubuntu runner with its Google Chrome and commits the result to `images/` on main. It runs only by hand, from the Actions tab (optionally with a list of `render.cjs` targets).
- The `region()` crops in `render.cjs` are pixel regions of the 1920×1080 captures. The screen crops (`OVEN`, `BOWL`, `STATION`, `BOOK`, `BOOKD`) are listed at the top of the script; adjust them if the GUI scale or the screens change.
- `source/fonts/` holds Fredoka and Nunito with their SIL Open Font License 1.1 texts. The script uses only these files.

Treat names on the cards come from the lang file. Effects and food values are written in `render.cjs`, copied from `registry/ModItems.java`. Update them when those change.

## Wording

Every number in the copy and on the cards comes from the 1.0.0 code: food values and effects from `registry/ModItems.java`, slices from `block/CakeTreatBlock.java` and `block/PieTreatBlock.java`, slot counts from the block entities, recipe counts from `data/sweettreats/recipes/` (18 baking, 17 mixing, 9 decorating, 20 crafting), biomes from `data/sweettreats/forge/biome_modifier/`, and defaults from `SweetTreatsConfig.java`. Update the copy when those change. Call the food items **treats**, the oven, bowl and station the **stations**, and the book the **Sweet Treats Recipe Book** (the **Recipe Book** after first mention).

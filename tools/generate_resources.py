"""
Sweet Treats resource generator.

Writes every JSON asset/data file of the mod (blockstates, models, loot tables, recipes, tags, world generation,
global loot modifiers, language file), the GameTest structure and docs/RECIPES.md.

Run from the project root:   python tools/generate_resources.py
"""
import gzip
import json
import os
import struct
from collections import OrderedDict

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "src", "main", "resources")
ASSETS = os.path.join(RES, "assets", "sweettreats")
DATA = os.path.join(RES, "data")
MOD = "sweettreats"

written = []


def write_json(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        json.dump(obj, f, indent=2, ensure_ascii=False)
        f.write("\n")
    written.append(path)


def m(name):
    return f"{MOD}:{name}"


def blk(tex):
    return f"{MOD}:block/{tex}"


def itm(tex):
    return f"{MOD}:item/{tex}"


FACING_Y = OrderedDict([("north", 0), ("east", 90), ("south", 180), ("west", 270)])

# =====================================================================================================================
# Names
# =====================================================================================================================
ITEM_NAMES = OrderedDict([
    ("recipe_book", "Sweet Treats Recipe Book"),
    ("strawberry", "Strawberry"), ("strawberry_seeds", "Strawberry Seeds"),
    ("vanilla_pod", "Vanilla Pod"), ("vanilla_seeds", "Vanilla Seeds"),
    ("lavender", "Lavender"), ("lavender_seeds", "Lavender Seeds"),
    ("peach", "Peach"), ("peach_pit", "Peach Pit"),
    ("cherry", "Cherry"), ("cherry_pit", "Cherry Pit"),
    ("flour", "Flour"), ("cream", "Cream"), ("butter", "Butter"), ("cocoa_powder", "Cocoa Powder"),
    ("chocolate", "Chocolate"), ("cinnamon", "Cinnamon"), ("sugar_flowers", "Sugar Flowers"),
    ("icing", "Icing"), ("pink_icing", "Pink Icing"), ("chocolate_frosting", "Chocolate Frosting"),
    ("sprinkles", "Rainbow Sprinkles"), ("tapioca_pearls", "Tapioca Pearls"),
    ("cake_batter", "Cake Batter"), ("cookie_dough", "Cookie Dough"), ("pastry_dough", "Pastry Dough"),
    ("macaron_batter", "Macaron Batter"), ("sponge_cake", "Sponge Cake"), ("plain_cupcake", "Plain Cupcake"),
    ("sugar_cookie", "Sugar Cookie"), ("macaron_shells", "Macaron Shells"), ("teacup", "Teacup"),
    ("pink_vanilla_cupcake", "Pink Vanilla Cupcake"), ("chocolate_cupcake", "Chocolate Cupcake"),
    ("lavender_macaron", "Lavender Macaron"), ("strawberry_macaron", "Strawberry Macaron"),
    ("heart_cookie", "Heart Cookie"), ("cinnamon_roll", "Cinnamon Roll"), ("mini_donuts", "Mini Donuts"),
    ("peach_pastry", "Peach Pastry"),
    ("strawberry_milk", "Strawberry Milk"), ("vanilla_milkshake", "Vanilla Milkshake"),
    ("lavender_tea", "Lavender Tea"), ("peach_tea", "Peach Tea"), ("bubble_tea", "Bubble Tea"),
    ("pink_lemonade", "Pink Lemonade"), ("hot_chocolate", "Hot Chocolate"), ("rose_tea", "Rose Tea"),
    ("heart_candy", "Heart Candy"), ("strawberry_lollipop", "Strawberry Lollipop"),
    ("marshmallow_cloud", "Marshmallow Cloud"), ("rainbow_cookie", "Rainbow Cookie"),
    ("honey_cupcake", "Honey Cupcake"), ("moon_cookie", "Moon Cookie"),
])

BLOCK_NAMES = OrderedDict([
    ("strawberry_crop", "Strawberries"), ("vanilla_crop", "Vanilla"), ("lavender_crop", "Lavender"),
    ("peach_tree", "Peach Tree"), ("cherry_tree", "Cherry Tree"),
    ("wild_strawberries", "Wild Strawberries"), ("wild_lavender", "Wild Lavender"), ("wild_vanilla", "Wild Vanilla"),
    ("bakery_oven", "Bakery Oven"), ("mixing_bowl", "Mixing Bowl"), ("decoration_station", "Decoration Station"),
    ("strawberry_cake", "Strawberry Cake"), ("cherry_pie", "Cherry Pie"),
    ("cake_display", "Cake Display"), ("bakery_counter", "Bakery Counter"), ("cupcake_stand", "Cupcake Stand"),
    ("dessert_plate", "Dessert Plate"), ("glass_jar", "Glass Jar"), ("tea_set", "Tea Set"),
    ("bakery_sign", "Bakery Sign"), ("decorative_menu", "Bakery Menu Board"),
])

CROPS = {"strawberry_crop": ("strawberry", "strawberry_seeds", 2, 4),
         "vanilla_crop": ("vanilla_pod", "vanilla_seeds", 1, 3),
         "lavender_crop": ("lavender", "lavender_seeds", 2, 3)}
FRUIT_TREES = {"peach_tree": ("peach", "peach_pit"), "cherry_tree": ("cherry", "cherry_pit")}
WILD = {"wild_strawberries": ("strawberry", "strawberry_seeds"),
        "wild_lavender": ("lavender", "lavender_seeds"),
        "wild_vanilla": ("vanilla_pod", "vanilla_seeds")}
FACING_BLOCKS = ["decoration_station", "cake_display", "bakery_counter", "cupcake_stand", "dessert_plate",
                 "glass_jar", "tea_set", "bakery_sign", "decorative_menu"]
SELF_DROP = ["bakery_oven", "mixing_bowl", "decoration_station", "cake_display", "bakery_counter", "cupcake_stand",
             "dessert_plate", "glass_jar", "tea_set", "bakery_sign", "decorative_menu"]

# Items whose inventory model is the block model.
BLOCK_ITEM_MODELS = ["bakery_oven", "mixing_bowl", "decoration_station", "cake_display", "bakery_counter",
                     "cupcake_stand", "dessert_plate", "glass_jar", "tea_set", "bakery_sign", "decorative_menu"]


# =====================================================================================================================
# Blockstates & block models
# =====================================================================================================================
def facing_variants(model, extra=""):
    variants = OrderedDict()
    for facing, y in FACING_Y.items():
        key = f"facing={facing}" + (f",{extra}" if extra else "")
        v = {"model": model}
        if y:
            v["y"] = y
        variants[key] = v
    return variants


def blockstates_and_models():
    bs = os.path.join(ASSETS, "blockstates")
    mdl = os.path.join(ASSETS, "models", "block")

    # Crops: age 0-7 -> 4 visual stages
    stage_of_age = [0, 0, 1, 1, 2, 2, 2, 3]
    for crop in CROPS:
        write_json(os.path.join(bs, f"{crop}.json"), {"variants": OrderedDict(
            (f"age={a}", {"model": blk(f"{crop}_stage{stage_of_age[a]}")}) for a in range(8))})
        for s in range(4):
            write_json(os.path.join(mdl, f"{crop}_stage{s}.json"), {
                "parent": "minecraft:block/crop", "render_type": "minecraft:cutout",
                "textures": {"crop": blk(f"{crop}_stage{s}")}})

    # Fruit trees: age 0-3
    for tree in FRUIT_TREES:
        write_json(os.path.join(bs, f"{tree}.json"), {"variants": OrderedDict(
            (f"age={a}", {"model": blk(f"{tree}_stage{a}")}) for a in range(4))})
        for s in range(4):
            write_json(os.path.join(mdl, f"{tree}_stage{s}.json"), {
                "parent": "minecraft:block/cross", "render_type": "minecraft:cutout",
                "textures": {"cross": blk(f"{tree}_stage{s}")}})

    # Wild plants
    for plant in WILD:
        write_json(os.path.join(bs, f"{plant}.json"), {"variants": {"": {"model": blk(plant)}}})
        write_json(os.path.join(mdl, f"{plant}.json"), {
            "parent": "minecraft:block/cross", "render_type": "minecraft:cutout", "textures": {"cross": blk(plant)}})

    # Bakery oven
    variants = OrderedDict()
    for lit in ("false", "true"):
        for k, v in facing_variants(blk("bakery_oven_on" if lit == "true" else "bakery_oven"), f"lit={lit}").items():
            variants[k] = v
    write_json(os.path.join(bs, "bakery_oven.json"), {"variants": variants})
    for name, front in (("bakery_oven", "bakery_oven_front"), ("bakery_oven_on", "bakery_oven_front_on")):
        write_json(os.path.join(mdl, f"{name}.json"), {
            "parent": "minecraft:block/orientable_with_bottom",
            "textures": {"top": blk("bakery_oven_top"), "bottom": blk("bakery_oven_bottom"),
                         "side": blk("bakery_oven_side"), "front": blk(front), "particle": blk("bakery_oven_side")}})

    # Decoration station & counter (orientable cubes)
    write_json(os.path.join(mdl, "decoration_station.json"), {
        "parent": "minecraft:block/orientable_with_bottom",
        "textures": {"top": blk("decoration_station_top"), "bottom": blk("decoration_station_side"),
                     "side": blk("decoration_station_side"), "front": blk("decoration_station_front"),
                     "particle": blk("decoration_station_side")}})
    write_json(os.path.join(mdl, "bakery_counter.json"), {
        "parent": "minecraft:block/orientable_with_bottom",
        "textures": {"top": blk("bakery_counter_top"), "bottom": blk("bakery_counter_side"),
                     "side": blk("bakery_counter_side"), "front": blk("bakery_counter_front"),
                     "particle": blk("bakery_counter_side")}})

    # Mixing bowl (no facing)
    write_json(os.path.join(bs, "mixing_bowl.json"), {"variants": {"": {"model": blk("mixing_bowl")}}})
    S, I, B = "#side", "#inside", "#bottom"
    write_json(os.path.join(mdl, "mixing_bowl.json"), {
        "parent": "minecraft:block/block",
        "textures": {"particle": blk("mixing_bowl_side"), "side": blk("mixing_bowl_side"),
                     "inside": blk("mixing_bowl_inside"), "bottom": blk("mixing_bowl_bottom")},
        "elements": [
            el([4, 0, 4], [12, 1, 12], {"down": f(B, cull="down"), "north": f(S), "south": f(S), "west": f(S), "east": f(S)}),
            el([4, 1, 4], [12, 2, 12], {"up": f(I)}),
            el([3, 1, 3], [13, 6, 4], {"north": f(S), "south": f(I), "west": f(S), "east": f(S), "up": f(S), "down": f(B)}),
            el([3, 1, 12], [13, 6, 13], {"south": f(S), "north": f(I), "west": f(S), "east": f(S), "up": f(S), "down": f(B)}),
            el([3, 1, 4], [4, 6, 12], {"west": f(S), "east": f(I), "up": f(S), "down": f(B)}),
            el([12, 1, 4], [13, 6, 12], {"east": f(S), "west": f(I), "up": f(S), "down": f(B)}),
        ]})

    # Strawberry cake: 7 bites, re-using the vanilla cake geometry
    variants = OrderedDict()
    for b in range(7):
        variants[f"bites={b}"] = {"model": blk("strawberry_cake" if b == 0 else f"strawberry_cake_slice{b}")}
    write_json(os.path.join(bs, "strawberry_cake.json"), {"variants": variants})
    cake_tex = {"particle": blk("strawberry_cake_side"), "bottom": blk("strawberry_cake_bottom"),
                "top": blk("strawberry_cake_top"), "side": blk("strawberry_cake_side")}
    write_json(os.path.join(mdl, "strawberry_cake.json"), {"parent": "minecraft:block/cake", "textures": cake_tex})
    for b in range(1, 7):
        write_json(os.path.join(mdl, f"strawberry_cake_slice{b}.json"), {
            "parent": f"minecraft:block/cake_slice{b}",
            "textures": dict(cake_tex, inside=blk("strawberry_cake_inner"))})

    # Cherry pie: 4 bites, one quarter at a time
    pie_models()

    # Facing decor/display blocks
    for name in FACING_BLOCKS:
        write_json(os.path.join(bs, f"{name}.json"), {"variants": facing_variants(blk(name))})
    decor_models(mdl)


def f(texture, uv=None, cull=None):
    face = {"texture": texture}
    if uv is not None:
        face["uv"] = uv
    if cull:
        face["cullface"] = cull
    return face


def el(frm, to, faces, rotation=None):
    e = {"from": frm, "to": to, "faces": faces}
    if rotation:
        e["rotation"] = rotation
    return e


def pie_models():
    bs = os.path.join(ASSETS, "blockstates")
    mdl = os.path.join(ASSETS, "models", "block")
    quads = OrderedDict([
        ("nw", ([1, 0, 1], [8, 4, 8], {"east": "ne", "south": "sw"})),
        ("ne", ([8, 0, 1], [15, 4, 8], {"west": "nw", "south": "se"})),
        ("se", ([8, 0, 8], [15, 4, 15], {"north": "ne", "west": "sw"})),
        ("sw", ([1, 0, 8], [8, 4, 15], {"north": "nw", "east": "se"})),
    ])
    remaining_by_bites = [["nw", "ne", "se", "sw"], ["nw", "se", "sw"], ["nw", "sw"], ["nw"]]
    variants = OrderedDict()
    for bites, remaining in enumerate(remaining_by_bites):
        name = "cherry_pie" if bites == 0 else f"cherry_pie_slice{bites}"
        variants[f"bites={bites}"] = {"model": blk(name)}
        elements = []
        for q in remaining:
            frm, to, neighbours = quads[q]
            faces = OrderedDict([("down", f("#bottom", cull="down")), ("up", f("#top"))])
            for direction in ("north", "south", "west", "east"):
                if direction in neighbours:
                    if neighbours[direction] in remaining:
                        continue  # hidden inside the pie
                    faces[direction] = f("#inside")
                else:
                    faces[direction] = f("#side")
            elements.append(el(frm, to, faces))
        write_json(os.path.join(mdl, f"{name}.json"), {
            "parent": "minecraft:block/block",
            "textures": {"particle": blk("cherry_pie_side"), "top": blk("cherry_pie_top"),
                         "side": blk("cherry_pie_side"), "inside": blk("cherry_pie_inner"),
                         "bottom": blk("cherry_pie_bottom")},
            "elements": elements})
    write_json(os.path.join(bs, "cherry_pie.json"), {"variants": variants})


def all_faces(texture, uv=None, skip=()):
    return OrderedDict((d, f(texture, uv)) for d in ("north", "south", "west", "east", "up", "down") if d not in skip)


def decor_models(mdl):
    full = [0, 0, 16, 16]
    row0 = [0, 0, 16, 1]

    # Cake display: wooden plinth + glass box
    glass = OrderedDict((d, f("#glass", full)) for d in ("north", "south", "west", "east", "up"))
    write_json(os.path.join(mdl, "cake_display.json"), {
        "parent": "minecraft:block/block", "render_type": "minecraft:translucent",
        "textures": {"particle": blk("cake_display_base"), "base": blk("cake_display_base"),
                     "glass": blk("cake_display_glass")},
        "elements": [
            el([0, 0, 0], [16, 2, 16], OrderedDict([
                ("down", f("#base", full, "down")), ("up", f("#base", full)),
                ("north", f("#base", [0, 14, 16, 16])), ("south", f("#base", [0, 14, 16, 16])),
                ("west", f("#base", [0, 14, 16, 16])), ("east", f("#base", [0, 14, 16, 16]))])),
            el([1, 2, 1], [15, 15, 15], glass),
        ]})

    # Cupcake stand: three porcelain tiers on a pole
    def plate(frm, to):
        faces = OrderedDict([("up", f("#plate", full)), ("down", f("#plate", full))])
        for d in ("north", "south", "west", "east"):
            faces[d] = f("#plate", row0)
        return el(frm, to, faces)

    write_json(os.path.join(mdl, "cupcake_stand.json"), {
        "parent": "minecraft:block/block", "render_type": "minecraft:cutout",
        "textures": {"particle": blk("cupcake_stand_plate"), "plate": blk("cupcake_stand_plate"),
                     "pole": blk("cupcake_stand_pole")},
        "elements": [
            el([6, 0, 6], [10, 1, 10], all_faces("#pole")),
            el([7.5, 1, 7.5], [8.5, 12, 8.5], all_faces("#pole", skip=("up", "down"))),
            plate([1, 2, 1], [15, 3, 15]),
            plate([3, 7, 3], [13, 8, 13]),
            plate([5, 12, 5], [11, 13, 11]),
        ]})

    # Dessert plate
    faces = OrderedDict([("up", f("#plate", full)), ("down", f("#plate", full, "down"))])
    for d in ("north", "south", "west", "east"):
        faces[d] = f("#plate", row0)
    write_json(os.path.join(mdl, "dessert_plate.json"), {
        "parent": "minecraft:block/block", "render_type": "minecraft:cutout",
        "textures": {"particle": blk("dessert_plate"), "plate": blk("dessert_plate")},
        "elements": [el([2, 0, 2], [14, 1, 14], faces)]})

    # Glass jar
    write_json(os.path.join(mdl, "glass_jar.json"), {
        "parent": "minecraft:block/block", "render_type": "minecraft:translucent",
        "textures": {"particle": blk("glass_jar_side"), "side": blk("glass_jar_side"),
                     "top": blk("glass_jar_top"), "lid": blk("glass_jar_lid")},
        "elements": [
            el([4, 0, 4], [12, 10, 12], OrderedDict([
                ("north", f("#side")), ("south", f("#side")), ("west", f("#side")), ("east", f("#side")),
                ("up", f("#top")), ("down", f("#top", cull="down"))])),
            el([5, 10, 5], [11, 12, 11], all_faces("#lid", skip=("down",))),
        ]})

    # Tea set: tray, teapot, two cups
    tray = OrderedDict([("up", f("#tray", full)), ("down", f("#tray", full, "down"))])
    for d in ("north", "south", "west", "east"):
        tray[d] = f("#tray", row0)
    cup_faces = all_faces("#porcelain", skip=("up",))
    cup_faces["up"] = f("#tea")
    write_json(os.path.join(mdl, "tea_set.json"), {
        "parent": "minecraft:block/block",
        "textures": {"particle": blk("tea_set_porcelain"), "tray": blk("tea_set_tray"),
                     "porcelain": blk("tea_set_porcelain"), "tea": blk("tea_set_tea")},
        "elements": [
            el([1, 0, 1], [15, 1, 15], tray),
            el([5, 1, 6], [10, 6, 11], all_faces("#porcelain")),
            el([6, 6, 7], [9, 7, 10], all_faces("#porcelain", skip=("down",))),
            el([7, 7, 8], [8, 8, 9], all_faces("#porcelain", skip=("down",))),
            el([3, 2, 8], [5, 4, 9], all_faces("#porcelain")),
            el([10, 2, 8], [12, 5, 9], all_faces("#porcelain")),
            el([11, 1, 2], [14, 3, 5], cup_faces),
            el([2, 1, 11], [5, 3, 14], cup_faces),
        ]})

    # Bakery sign (wall mounted; board sits against the wall behind it)
    write_json(os.path.join(mdl, "bakery_sign.json"), {
        "parent": "minecraft:block/block", "render_type": "minecraft:cutout",
        "textures": {"particle": blk("bakery_sign_back"), "front": blk("bakery_sign_front"),
                     "back": blk("bakery_sign_back")},
        "elements": [el([0, 3, 14], [16, 13, 16], OrderedDict([
            ("north", f("#front", [0, 3, 16, 13])), ("south", f("#back", [0, 3, 16, 13])),
            ("west", f("#back", [14, 3, 16, 13])), ("east", f("#back", [0, 3, 2, 13])),
            ("up", f("#back", [0, 14, 16, 16])), ("down", f("#back", [0, 14, 16, 16]))]))],
        "display": {
            "gui": {"rotation": [0, 0, 0], "translation": [0, 0, 0], "scale": [0.8, 0.8, 0.8]},
            "fixed": {"rotation": [0, 180, 0], "translation": [0, 0, -6], "scale": [0.8, 0.8, 0.8]}}})

    # Chalkboard menu easel
    wood = "#frame"
    write_json(os.path.join(mdl, "decorative_menu.json"), {
        "parent": "minecraft:block/block",
        "textures": {"particle": blk("decorative_menu_frame"), "front": blk("decorative_menu_front"),
                     "frame": blk("decorative_menu_frame")},
        "elements": [
            el([1, 4, 7], [15, 16, 8], OrderedDict([
                ("north", f("#front", [1, 0, 15, 12])), ("south", f(wood)), ("west", f(wood)),
                ("east", f(wood)), ("up", f(wood)), ("down", f(wood))])),
            el([2, 0, 7], [3, 4, 8], all_faces(wood)),
            el([13, 0, 7], [14, 4, 8], all_faces(wood)),
            el([7.5, 0, 10], [8.5, 13, 11], all_faces(wood)),
            el([7.5, 12, 8], [8.5, 13, 10], all_faces(wood)),
        ]})


# =====================================================================================================================
# Item models
# =====================================================================================================================
def item_models():
    out = os.path.join(ASSETS, "models", "item")
    for name in ITEM_NAMES:
        write_json(os.path.join(out, f"{name}.json"),
                   {"parent": "minecraft:item/generated", "textures": {"layer0": itm(name)}})
    for name in ("strawberry_cake", "cherry_pie"):
        write_json(os.path.join(out, f"{name}.json"),
                   {"parent": "minecraft:item/generated", "textures": {"layer0": itm(name)}})
    for name in WILD:
        write_json(os.path.join(out, f"{name}.json"),
                   {"parent": "minecraft:item/generated", "textures": {"layer0": blk(name)}})
    for name in BLOCK_ITEM_MODELS:
        write_json(os.path.join(out, f"{name}.json"), {"parent": blk(name)})


# =====================================================================================================================
# Loot tables
# =====================================================================================================================
def loot_tables():
    out = os.path.join(DATA, MOD, "loot_tables", "blocks")

    def table(name, pools, decay=False):
        t = {"type": "minecraft:block", "pools": pools, "random_sequence": f"{MOD}:blocks/{name}"}
        if decay:
            t["functions"] = [{"function": "minecraft:explosion_decay"}]
        write_json(os.path.join(out, f"{name}.json"), t)

    for name in SELF_DROP:
        table(name, [{"rolls": 1.0, "bonus_rolls": 0.0, "entries": [{"type": "minecraft:item", "name": m(name)}],
                      "conditions": [{"condition": "minecraft:survives_explosion"}]}])

    for name, sliced_block in (("strawberry_cake", "strawberry_cake"), ("cherry_pie", "cherry_pie")):
        table(name, [{"rolls": 1.0, "bonus_rolls": 0.0, "entries": [{"type": "minecraft:item", "name": m(name)}],
                      "conditions": [{"condition": "minecraft:block_state_property", "block": m(sliced_block),
                                      "properties": {"bites": "0"}},
                                     {"condition": "minecraft:survives_explosion"}]}])

    for crop, (produce, seeds, lo, hi) in CROPS.items():
        mature = {"condition": "minecraft:block_state_property", "block": m(crop), "properties": {"age": "7"}}
        table(crop, [
            {"rolls": 1.0, "bonus_rolls": 0.0, "entries": [{"type": "minecraft:alternatives", "children": [
                {"type": "minecraft:item", "name": m(produce), "conditions": [mature],
                 "functions": [{"function": "minecraft:set_count",
                                "count": {"type": "minecraft:uniform", "min": lo, "max": hi}},
                               {"function": "minecraft:apply_bonus", "enchantment": "minecraft:fortune",
                                "formula": "minecraft:uniform_bonus_count", "parameters": {"bonusMultiplier": 1}}]},
                {"type": "minecraft:item", "name": m(seeds)}]}]},
            {"rolls": 1.0, "bonus_rolls": 0.0, "conditions": [mature],
             "entries": [{"type": "minecraft:item", "name": m(seeds),
                          "functions": [{"function": "minecraft:apply_bonus", "enchantment": "minecraft:fortune",
                                         "formula": "minecraft:binomial_with_bonus_count",
                                         "parameters": {"extra": 1, "probability": 0.5}}]}]},
        ], decay=True)

    for tree, (fruit, pit) in FRUIT_TREES.items():
        table(tree, [
            {"rolls": 1.0, "bonus_rolls": 0.0, "entries": [{"type": "minecraft:item", "name": m(pit)}]},
            {"rolls": 1.0, "bonus_rolls": 0.0,
             "conditions": [{"condition": "minecraft:block_state_property", "block": m(tree), "properties": {"age": "3"}}],
             "entries": [{"type": "minecraft:item", "name": m(fruit),
                          "functions": [{"function": "minecraft:set_count",
                                         "count": {"type": "minecraft:uniform", "min": 2, "max": 3}}]}]},
        ], decay=True)

    shears = {"condition": "minecraft:match_tool", "predicate": {"items": ["minecraft:shears"]}}
    silk = {"condition": "minecraft:match_tool", "predicate": {"enchantments": [
        {"enchantment": "minecraft:silk_touch", "levels": {"min": 1}}]}}
    for plant, (produce, seeds) in WILD.items():
        table(plant, [
            {"rolls": 1.0, "bonus_rolls": 0.0, "entries": [{"type": "minecraft:alternatives", "children": [
                {"type": "minecraft:item", "name": m(plant), "conditions": [shears]},
                {"type": "minecraft:item", "name": m(plant), "conditions": [silk]},
                {"type": "minecraft:item", "name": m(produce),
                 "functions": [{"function": "minecraft:set_count",
                                "count": {"type": "minecraft:uniform", "min": 1, "max": 2}}]}]}]},
            {"rolls": 1.0, "bonus_rolls": 0.0,
             "conditions": [{"condition": "minecraft:inverted", "term": shears},
                            {"condition": "minecraft:inverted", "term": silk},
                            {"condition": "minecraft:random_chance", "chance": 0.75}],
             "entries": [{"type": "minecraft:item", "name": m(seeds)}]},
        ], decay=True)


# =====================================================================================================================
# Recipes
# =====================================================================================================================
def I(name):
    """Ingredient helper: 'minecraft:x', 'sweettreats:x', bare mod item name, '#tag', or a list of alternatives."""
    if isinstance(name, list):
        return [I(n) for n in name]
    if name.startswith("#"):
        return {"tag": name[1:]}
    if ":" not in name:
        name = m(name)
    return {"item": name}


def R(name, count=1):
    if ":" not in name:
        name = m(name)
    r = {"item": name}
    if count != 1:
        r["count"] = count
    return r


ROSE = ["minecraft:rose_bush", "minecraft:poppy"]

BAKING = [
    # id, ingredients, result, count, ticks, xp
    ("sponge_cake", ["cake_batter"], "sponge_cake", 1, 200, 0.35),
    ("plain_cupcake", ["cake_batter", "minecraft:paper"], "plain_cupcake", 4, 200, 0.35),
    ("sugar_cookie", ["cookie_dough"], "sugar_cookie", 4, 160, 0.3),
    ("macaron_shells", ["macaron_batter"], "macaron_shells", 2, 160, 0.3),
    ("cinnamon_roll", ["pastry_dough", "cinnamon", "minecraft:sugar"], "cinnamon_roll", 3, 200, 0.35),
    ("mini_donuts", ["pastry_dough", "minecraft:sugar", "sprinkles"], "mini_donuts", 4, 160, 0.35),
    ("peach_pastry", ["pastry_dough", "peach", "peach", "minecraft:sugar"], "peach_pastry", 2, 200, 0.35),
    ("cherry_pie", ["pastry_dough", "cherry", "cherry", "minecraft:sugar"], "cherry_pie", 1, 240, 0.5),
    ("heart_candy", ["minecraft:sugar", "minecraft:sugar", "strawberry"], "heart_candy", 4, 120, 0.2),
    ("strawberry_lollipop", ["minecraft:sugar", "minecraft:sugar", "strawberry", "minecraft:stick"],
     "strawberry_lollipop", 2, 120, 0.2),
    ("teacup", ["minecraft:clay_ball", "minecraft:bone_meal"], "teacup", 2, 160, 0.1),
    ("dessert_plate", ["minecraft:clay_ball", "minecraft:clay_ball", "minecraft:white_dye"], "dessert_plate", 2, 160, 0.1),
    ("lavender_tea", ["teacup", "lavender", "lavender"], "lavender_tea", 1, 120, 0.2),
    ("peach_tea", ["teacup", "peach", "minecraft:sugar"], "peach_tea", 1, 120, 0.2),
    ("rose_tea", ["teacup", ROSE, "minecraft:sugar"], "rose_tea", 1, 120, 0.2),
    ("hot_chocolate", ["teacup", "chocolate", "cream"], "hot_chocolate", 1, 120, 0.2),
    ("bread_from_flour", ["flour", "flour", "flour"], "minecraft:bread", 2, 160, 0.2),
    ("chocolate_chip_cookies", ["cookie_dough", "minecraft:cocoa_beans"], "minecraft:cookie", 8, 160, 0.3),
]

MIXING = [
    # id, ingredients, result, count, container, ticks
    ("cake_batter", ["flour", "flour", "minecraft:sugar", "minecraft:egg", "cream", "vanilla_pod"], "cake_batter", 1, None, 100),
    ("cookie_dough", ["flour", "butter", "minecraft:sugar", "minecraft:egg"], "cookie_dough", 2, None, 80),
    ("pastry_dough", ["flour", "flour", "butter", "minecraft:egg"], "pastry_dough", 2, None, 80),
    ("macaron_batter", ["flour", "minecraft:sugar", "minecraft:sugar", "minecraft:egg"], "macaron_batter", 1, None, 80),
    ("butter", ["cream", "cream"], "butter", 1, None, 100),
    ("chocolate", ["cocoa_powder", "cocoa_powder", "minecraft:sugar", "cream"], "chocolate", 2, None, 80),
    ("icing", ["minecraft:sugar", "minecraft:sugar", "cream"], "icing", 2, None, 60),
    ("pink_icing", ["minecraft:sugar", "minecraft:sugar", "cream", "strawberry"], "pink_icing", 2, None, 60),
    ("chocolate_frosting", ["minecraft:sugar", "cream", "chocolate"], "chocolate_frosting", 2, None, 60),
    ("sprinkles", ["minecraft:sugar", "minecraft:red_dye", "minecraft:yellow_dye", "minecraft:blue_dye"], "sprinkles", 4, None, 60),
    ("sugar_flowers", ["minecraft:sugar", "minecraft:sugar", "#minecraft:small_flowers"], "sugar_flowers", 2, None, 60),
    ("tapioca_pearls", ["minecraft:potato", "minecraft:sugar"], "tapioca_pearls", 4, None, 80),
    ("marshmallow_cloud", ["minecraft:sugar", "minecraft:sugar", "minecraft:egg", "vanilla_pod"], "marshmallow_cloud", 4, None, 100),
    ("strawberry_milk", ["cream", "strawberry", "strawberry"], "strawberry_milk", 1, "minecraft:glass_bottle", 60),
    ("vanilla_milkshake", ["cream", "cream", "vanilla_pod", "minecraft:snowball"], "vanilla_milkshake", 1, "minecraft:glass_bottle", 80),
    ("bubble_tea", ["cream", "tapioca_pearls", "minecraft:sugar"], "bubble_tea", 1, "minecraft:glass_bottle", 80),
    ("pink_lemonade", ["strawberry", "minecraft:glow_berries", "minecraft:sugar"], "pink_lemonade", 1, "minecraft:glass_bottle", 60),
]

DECORATING = [
    # id, base, toppings, result, count
    ("strawberry_cake", "sponge_cake", ["cream", "strawberry"], "strawberry_cake", 1),
    ("pink_vanilla_cupcake", "plain_cupcake", ["pink_icing", "sugar_flowers"], "pink_vanilla_cupcake", 1),
    ("chocolate_cupcake", "plain_cupcake", ["chocolate_frosting"], "chocolate_cupcake", 1),
    ("honey_cupcake", "plain_cupcake", ["minecraft:honey_bottle"], "honey_cupcake", 1),
    ("lavender_macaron", "macaron_shells", ["icing", "lavender"], "lavender_macaron", 2),
    ("strawberry_macaron", "macaron_shells", ["icing", "strawberry"], "strawberry_macaron", 2),
    ("heart_cookie", "sugar_cookie", ["pink_icing"], "heart_cookie", 1),
    ("rainbow_cookie", "sugar_cookie", ["icing", "sprinkles"], "rainbow_cookie", 1),
    ("moon_cookie", "sugar_cookie", ["icing", "minecraft:glow_berries"], "moon_cookie", 1),
]

# Crafting table recipes: (id, kind, pattern-or-ingredients, key, result, count)
CRAFTING = [
    ("recipe_book", "shapeless", ["minecraft:book", "minecraft:sugar"], None, "recipe_book", 1),
    ("flour", "shapeless", ["minecraft:wheat"], None, "flour", 1),
    ("cream", "shapeless", ["minecraft:milk_bucket"], None, "cream", 3),
    ("cocoa_powder", "shapeless", ["minecraft:cocoa_beans"], None, "cocoa_powder", 1),
    ("cinnamon", "shapeless", ["#minecraft:jungle_logs"], None, "cinnamon", 4),
    ("strawberry_seeds", "shapeless", ["strawberry"], None, "strawberry_seeds", 1),
    ("vanilla_seeds", "shapeless", ["vanilla_pod"], None, "vanilla_seeds", 1),
    ("lavender_seeds", "shapeless", ["lavender"], None, "lavender_seeds", 1),
    ("peach_pit", "shapeless", ["peach"], None, "peach_pit", 1),
    ("cherry_pit", "shapeless", ["cherry"], None, "cherry_pit", 1),
    ("mixing_bowl", "shaped", ["  S", "P P", " P "], {"S": "minecraft:stick", "P": "#minecraft:planks"}, "mixing_bowl", 1),
    ("bakery_oven", "shaped", ["TTT", "TFT", "BBB"],
     {"T": "#minecraft:terracotta", "F": "minecraft:furnace", "B": "minecraft:brick"}, "bakery_oven", 1),
    ("decoration_station", "shaped", ["IFI", "PCP", "P P"],
     {"I": "icing", "F": "sugar_flowers", "C": "minecraft:crafting_table", "P": "#minecraft:planks"},
     "decoration_station", 1),
    ("cake_display", "shaped", ["GGG", "G G", "SSS"], {"G": "minecraft:glass", "S": "#minecraft:wooden_slabs"},
     "cake_display", 1),
    ("bakery_counter", "shaped", ["SSS", "PDP", "PPP"],
     {"S": "minecraft:smooth_stone_slab", "P": "#minecraft:planks", "D": "minecraft:pink_dye"}, "bakery_counter", 2),
    ("cupcake_stand", "shaped", [" P ", " S ", "PSP"], {"P": "dessert_plate", "S": "minecraft:stick"},
     "cupcake_stand", 1),
    ("glass_jar", "shaped", [" W ", "G G", "GGG"], {"W": "#minecraft:wooden_slabs", "G": "minecraft:glass"},
     "glass_jar", 2),
    ("tea_set", "shapeless", ["teacup", "teacup", "teacup", "dessert_plate"], None, "tea_set", 1),
    ("bakery_sign", "shaped", ["PPP", "PDP", " S "],
     {"P": "#minecraft:planks", "D": "minecraft:pink_dye", "S": "minecraft:stick"}, "bakery_sign", 2),
    ("decorative_menu", "shaped", ["SSS", "SCS", "S S"],
     {"S": "minecraft:stick", "C": ["minecraft:coal", "minecraft:charcoal"]}, "decorative_menu", 1),
]


def recipes():
    out = os.path.join(DATA, MOD, "recipes")
    check_unique_sets("baking", [(rid, ing) for rid, ing, *_ in BAKING])
    check_unique_sets("mixing", [(rid, ing) for rid, ing, *_ in MIXING])
    check_unique_sets("decorating", [(rid, [base] + tops) for rid, base, tops, *_ in DECORATING])

    for rid, ingredients, result, count, ticks, xp in BAKING:
        assert 1 <= len(ingredients) <= 4, rid
        write_json(os.path.join(out, "baking", f"{rid}.json"), {
            "type": m("baking"), "ingredients": [I(i) for i in ingredients],
            "result": R(result, count), "cookingtime": ticks, "experience": xp})
    for rid, ingredients, result, count, container, ticks in MIXING:
        assert 1 <= len(ingredients) <= 6, rid
        recipe = {"type": m("mixing"), "ingredients": [I(i) for i in ingredients]}
        if container:
            recipe["container"] = I(container)
        recipe.update({"result": R(result, count), "mixingtime": ticks})
        write_json(os.path.join(out, "mixing", f"{rid}.json"), recipe)
    for rid, base, toppings, result, count in DECORATING:
        assert 1 <= len(toppings) <= 2, rid
        write_json(os.path.join(out, "decorating", f"{rid}.json"), {
            "type": m("decorating"), "base": I(base), "toppings": [I(t) for t in toppings],
            "result": R(result, count)})
    for rid, kind, pattern, key, result, count in CRAFTING:
        if kind == "shapeless":
            recipe = {"type": "minecraft:crafting_shapeless", "category": "misc",
                      "ingredients": [I(i) for i in pattern], "result": R(result, count)}
        else:
            recipe = {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": pattern,
                      "key": {k: I(v) for k, v in key.items()}, "result": R(result, count)}
        write_json(os.path.join(out, "crafting", f"{rid}.json"), recipe)


def check_unique_sets(kind, entries):
    """Stack-aware matching means two recipes with the same *set* of ingredient kinds would clash."""
    seen = {}
    for rid, ingredients in entries:
        key = frozenset(json.dumps(I(i), sort_keys=True) for i in ingredients)
        if key in seen:
            raise SystemExit(f"{kind} recipes '{rid}' and '{seen[key]}' use the same ingredient set")
        seen[key] = rid


# =====================================================================================================================
# Tags
# =====================================================================================================================
def tags():
    def tag(path, values):
        write_json(os.path.join(DATA, *path.split("/")) + ".json", {"replace": False, "values": values})

    crops = [m(c) for c in CROPS]
    trees = [m(t) for t in FRUIT_TREES]
    tag("minecraft/tags/blocks/mineable/pickaxe",
        [m(b) for b in ("bakery_oven", "mixing_bowl", "cupcake_stand", "dessert_plate", "tea_set", "glass_jar")])
    tag("minecraft/tags/blocks/mineable/axe",
        [m(b) for b in ("decoration_station", "bakery_counter", "cake_display", "bakery_sign", "decorative_menu")])
    tag("minecraft/tags/blocks/crops", crops)
    tag("minecraft/tags/blocks/maintains_farmland", crops)
    tag("minecraft/tags/blocks/bee_growables", crops + trees)
    tag("minecraft/tags/blocks/small_flowers", [m("wild_lavender")])
    tag("minecraft/tags/blocks/sword_efficient", trees + [m(w) for w in WILD])
    tag("minecraft/tags/items/small_flowers", [m("wild_lavender")])

    tag("forge/tags/items/seeds", [m(s) for s in ("strawberry_seeds", "vanilla_seeds", "lavender_seeds", "peach_pit", "cherry_pit")])
    tag("forge/tags/items/seeds/strawberry", [m("strawberry_seeds")])
    tag("forge/tags/items/seeds/vanilla", [m("vanilla_seeds")])
    tag("forge/tags/items/seeds/lavender", [m("lavender_seeds")])
    tag("forge/tags/items/crops", [m(s) for s in ("strawberry", "vanilla_pod", "lavender")])
    tag("forge/tags/items/crops/strawberry", [m("strawberry")])
    tag("forge/tags/items/crops/vanilla", [m("vanilla_pod")])
    tag("forge/tags/items/crops/lavender", [m("lavender")])
    tag("forge/tags/items/fruits", [m(s) for s in ("strawberry", "peach", "cherry")])
    tag("forge/tags/items/fruits/strawberry", [m("strawberry")])
    tag("forge/tags/items/fruits/peach", [m("peach")])
    tag("forge/tags/items/fruits/cherry", [m("cherry")])
    tag("forge/tags/items/flour", [m("flour")])
    tag("forge/tags/items/flour/wheat", [m("flour")])
    tag("forge/tags/items/chocolate", [m("chocolate")])
    tag("forge/tags/items/butter", [m("butter")])
    treats = [m(n) for n in ITEM_NAMES if n not in ("recipe_book",)] + [m("strawberry_cake"), m("cherry_pie")]
    tag("sweettreats/tags/items/treats", [t for t in treats if t.split(":")[1] in TREAT_ITEMS])
    tag("sweettreats/tags/items/drinks", [m(n) for n in DRINKS])
    # Non-food things that still look nice on displays (food is always accepted).
    tag("sweettreats/tags/items/displayable",
        ["minecraft:cake", m("teacup"), m("sugar_flowers"), m("sponge_cake"), m("macaron_shells"),
         m("icing"), m("pink_icing"), m("chocolate_frosting"), m("sprinkles"), m("vanilla_pod"), m("lavender")])


TREAT_ITEMS = ["strawberry_cake", "pink_vanilla_cupcake", "chocolate_cupcake", "lavender_macaron", "strawberry_macaron",
               "heart_cookie", "cinnamon_roll", "mini_donuts", "peach_pastry", "cherry_pie", "heart_candy",
               "strawberry_lollipop", "marshmallow_cloud", "rainbow_cookie", "honey_cupcake", "moon_cookie"]
DRINKS = ["strawberry_milk", "vanilla_milkshake", "lavender_tea", "peach_tea", "bubble_tea", "pink_lemonade",
          "hot_chocolate", "rose_tea"]


# =====================================================================================================================
# World generation & loot modifiers
# =====================================================================================================================
WORLDGEN = {
    "wild_strawberries": (["minecraft:plains", "minecraft:sunflower_plains", "minecraft:forest",
                           "minecraft:flower_forest", "minecraft:birch_forest", "minecraft:meadow"], 24),
    "wild_lavender": (["minecraft:meadow", "minecraft:flower_forest", "minecraft:plains",
                       "minecraft:sunflower_plains", "minecraft:cherry_grove"], 20),
    "wild_vanilla": ("#minecraft:is_jungle", 12),
}


def worldgen():
    for plant, (biomes, rarity) in WORLDGEN.items():
        patch = f"patch_{plant}"
        write_json(os.path.join(DATA, MOD, "worldgen", "configured_feature", f"{patch}.json"), {
            "type": "minecraft:random_patch",
            "config": {
                "tries": 32, "xz_spread": 6, "y_spread": 2,
                "feature": {
                    "feature": {"type": "minecraft:simple_block", "config": {"to_place": {
                        "type": "minecraft:simple_state_provider", "state": {"Name": m(plant)}}}},
                    "placement": [{"type": "minecraft:block_predicate_filter", "predicate": {
                        "type": "minecraft:all_of", "predicates": [
                            {"type": "minecraft:matching_blocks", "blocks": "minecraft:air"},
                            {"type": "minecraft:matching_blocks", "blocks": "minecraft:grass_block",
                             "offset": [0, -1, 0]}]}}]}}})
        write_json(os.path.join(DATA, MOD, "worldgen", "placed_feature", f"{patch}.json"), {
            "feature": m(patch),
            "placement": [{"type": "minecraft:rarity_filter", "chance": rarity}, {"type": "minecraft:in_square"},
                          {"type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG"},
                          {"type": "minecraft:biome"}]})
        write_json(os.path.join(DATA, MOD, "forge", "biome_modifier", f"add_{plant}.json"), {
            "type": "forge:add_features", "biomes": biomes, "features": m(patch), "step": "vegetal_decoration"})


def loot_modifiers():
    seeds = [{"item": m("strawberry_seeds"), "weight": 5}, {"item": m("lavender_seeds"), "weight": 4},
             {"item": m("vanilla_seeds"), "weight": 2}, {"item": m("peach_pit"), "weight": 1},
             {"item": m("cherry_pit"), "weight": 1}]
    mods = OrderedDict([
        ("grass_seeds", ("minecraft:blocks/grass", "grass_seeds", 0.07, seeds)),
        ("tall_grass_seeds", ("minecraft:blocks/tall_grass", "grass_seeds", 0.08, seeds)),
        ("fern_seeds", ("minecraft:blocks/fern", "grass_seeds", 0.05, seeds)),
        ("cherry_leaves_fruit", ("minecraft:blocks/cherry_leaves", "cherry_leaves", 0.06,
                                 [{"item": m("cherry"), "weight": 3}, {"item": m("cherry_pit"), "weight": 1}])),
    ])
    for name, (table, toggle, chance, entries) in mods.items():
        write_json(os.path.join(DATA, MOD, "loot_modifiers", f"{name}.json"), {
            "type": m("add_weighted_item"),
            "conditions": [{"condition": "forge:loot_table_id", "loot_table_id": table}],
            "toggle": toggle, "chance": chance, "entries": entries})
    write_json(os.path.join(DATA, "forge", "loot_modifiers", "global_loot_modifiers.json"),
               {"replace": False, "entries": [m(n) for n in mods]})


# =====================================================================================================================
# Language
# =====================================================================================================================
def lang():
    entries = OrderedDict()
    entries["itemGroup.sweettreats"] = "Sweet Treats"
    for name, title in ITEM_NAMES.items():
        entries[f"item.{MOD}.{name}"] = title
    for name, title in BLOCK_NAMES.items():
        entries[f"block.{MOD}.{name}"] = title
    entries.update(OrderedDict([
        ("item.sweettreats.recipe_book.tooltip", "Right-click to browse your bakery recipes"),
        ("container.sweettreats.bakery_oven", "Bakery Oven"),
        ("container.sweettreats.mixing_bowl", "Mixing Bowl"),
        ("container.sweettreats.decoration_station", "Decoration Station"),
        ("gui.sweettreats.mixing_bowl.container_hint", "Drinks need an empty Glass Bottle here"),
        ("message.sweettreats.discovered", "✿ New recipe discovered: %s"),
        ("gui.sweettreats.recipe_book.title", "Sweet Treats Recipe Book"),
        ("gui.sweettreats.recipe_book.category.baking", "Bakery Oven"),
        ("gui.sweettreats.recipe_book.category.mixing", "Mixing Bowl"),
        ("gui.sweettreats.recipe_book.category.decorating", "Decorating"),
        ("gui.sweettreats.recipe_book.category.crafting", "Crafting"),
        ("gui.sweettreats.recipe_book.undiscovered", "Not discovered yet"),
        ("gui.sweettreats.recipe_book.unknown_name", "???"),
        ("gui.sweettreats.recipe_book.not_discovered", "Make it once to discover it!"),
        ("gui.sweettreats.recipe_book.hidden_hint", "Keep experimenting to discover this recipe!"),
        ("gui.sweettreats.recipe_book.empty", "No recipes here yet."),
        ("gui.sweettreats.recipe_book.station_timed", "%s · %s"),
        ("gui.sweettreats.recipe_book.slices", "%s slices · %s food each"),
        ("gui.sweettreats.recipe_book.food", "Food %s · Saturation %s"),
        ("gui.sweettreats.recipe_book.ingredient", "Baking ingredient"),
        ("gui.sweettreats.recipe_book.base_toppings", "Treat + toppings"),
        ("gui.sweettreats.recipe_book.container", "Pour into:"),
    ]))
    write_json(os.path.join(ASSETS, "lang", "en_us.json"), entries)


# =====================================================================================================================
# GameTest structure (empty 3x3x3 box)
# =====================================================================================================================
def nbt_string(s):
    b = s.encode("utf-8")
    return struct.pack(">H", len(b)) + b


def nbt_named(tag_id, name, payload):
    return bytes([tag_id]) + nbt_string(name) + payload


def gametest_structure():
    palette_entry = nbt_named(8, "Name", nbt_string("minecraft:air")) + b"\x00"
    payload = b""
    payload += nbt_named(3, "DataVersion", struct.pack(">i", 3465))
    payload += nbt_named(9, "size", bytes([3]) + struct.pack(">i", 3) + struct.pack(">iii", 5, 3, 5))
    payload += nbt_named(9, "palette", bytes([10]) + struct.pack(">i", 1) + palette_entry)
    payload += nbt_named(9, "blocks", bytes([10]) + struct.pack(">i", 0))
    payload += nbt_named(9, "entities", bytes([10]) + struct.pack(">i", 0))
    root = nbt_named(10, "", payload + b"\x00")
    path = os.path.join(DATA, MOD, "structures", "empty.nbt")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with gzip.open(path, "wb") as fh:
        fh.write(root)
    written.append(path)


# =====================================================================================================================
# Recipe documentation
# =====================================================================================================================
def nice(name):
    if isinstance(name, list):
        return " or ".join(nice(n) for n in name)
    if name.startswith("#"):
        return "any " + name.split(":")[-1].split("/")[-1].replace("_", " ").rstrip("s") + " (tag)"
    ns, _, path = name.rpartition(":")
    if ns in ("", MOD):
        return ITEM_NAMES.get(path) or BLOCK_NAMES.get(path) or path.replace("_", " ").title()
    return path.replace("_", " ").title()


def grouped(ingredients):
    counts = OrderedDict()
    for i in ingredients:
        key = json.dumps(i)
        counts[key] = counts.get(key, 0) + 1
    return ", ".join((f"{c}x " if c > 1 else "") + nice(json.loads(k)) for k, c in counts.items())


def docs():
    lines = ["# Sweet Treats — Recipe List", "",
             "Generated by `tools/generate_resources.py`. All machine recipes are *shapeless* and *stack-aware*: "
             "an ingredient listed twice can come from one stack.", ""]
    lines += ["## Crafting Table", "", "| Result | Recipe |", "|---|---|"]
    for rid, kind, pattern, key, result, count in CRAFTING:
        if kind == "shapeless":
            text = grouped(pattern)
        else:
            text = "`" + " / ".join(p.replace(" ", "·") for p in pattern) + "` where " + \
                   ", ".join(f"{k} = {nice(v)}" for k, v in key.items())
        lines.append(f"| {count}x {nice(result)} | {text} |")
    lines += ["", "## Bakery Oven (needs furnace fuel)", "", "| Result | Ingredients | Time |", "|---|---|---|"]
    for rid, ingredients, result, count, ticks, xp in BAKING:
        lines.append(f"| {count}x {nice(result)} | {grouped(ingredients)} | {ticks / 20:g}s |")
    lines += ["", "## Mixing Bowl", "", "| Result | Ingredients | Container | Time |", "|---|---|---|---|"]
    for rid, ingredients, result, count, container, ticks in MIXING:
        container_text = nice(container) if container else "—"
        lines.append(f"| {count}x {nice(result)} | {grouped(ingredients)} | {container_text} | {ticks / 20:g}s |")
    lines += ["", "## Decoration Station", "", "| Result | Base | Toppings |", "|---|---|---|"]
    for rid, base, toppings, result, count in DECORATING:
        lines.append(f"| {count}x {nice(result)} | {nice(base)} | {grouped(toppings)} |")
    path = os.path.join(ROOT, "docs", "RECIPES.md")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as fh:
        fh.write("\n".join(lines) + "\n")
    written.append(path)


if __name__ == "__main__":
    blockstates_and_models()
    item_models()
    loot_tables()
    recipes()
    tags()
    worldgen()
    loot_modifiers()
    lang()
    gametest_structure()
    docs()
    print(f"Wrote {len(written)} files")

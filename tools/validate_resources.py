"""
Cross-checks Sweet Treats resources against the registered blocks and items:
  * every block has a blockstate, a loot table (except crop-like blocks are included too) and a lang entry
  * every item has an item model and a lang entry
  * every model referenced by a blockstate/item model exists (walking parents inside our namespace)
  * every texture referenced by a model exists as a PNG with a sane size
  * every item referenced in recipes / loot tables / tags exists (sweettreats namespace)

Run from the project root:   python tools/validate_resources.py
"""
import json
import os
import re
import sys

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
JAVA = os.path.join(ROOT, "src", "main", "java", "com", "sugarplumstudios", "sweettreats", "registry")
RES = os.path.join(ROOT, "src", "main", "resources")
ASSETS = os.path.join(RES, "assets", "sweettreats")
DATA = os.path.join(RES, "data")
MOD = "sweettreats"
errors = []


def registered(java_file, pattern):
    with open(os.path.join(JAVA, java_file), encoding="utf-8") as fh:
        src = fh.read()
    return re.findall(pattern, src)


blocks = registered("ModBlocks.java", r'BLOCKS\.register\("([a-z_]+)"')
items = registered("ModItems.java", r'(?:ITEMS\.register|blockItem|simple)\("([a-z_]+)"')
items = list(dict.fromkeys(items))

with open(os.path.join(ASSETS, "lang", "en_us.json"), encoding="utf-8") as fh:
    lang = json.load(fh)


def load(path):
    with open(path, encoding="utf-8") as fh:
        return json.load(fh)


def res_path(kind, ref, ext):
    ns, _, path = ref.partition(":") if ":" in ref else ("minecraft", "", ref)
    if ns != MOD:
        return None
    return os.path.join(RES, "assets", ns, kind, *path.split("/")) + ext


checked_models = set()


def check_model(ref, origin):
    p = res_path("models", ref, ".json")
    if p is None or p in checked_models:
        return
    checked_models.add(p)
    if not os.path.exists(p):
        errors.append(f"missing model {ref} (from {origin})")
        return
    model = load(p)
    if "parent" in model:
        check_model(model["parent"], ref)
    for key, tex in model.get("textures", {}).items():
        if tex.startswith("#"):
            continue
        tp = res_path("textures", tex, ".png")
        if tp is None:
            continue
        if not os.path.exists(tp):
            errors.append(f"missing texture {tex} (model {ref})")
            continue
        img = Image.open(tp)
        w, h = img.size
        if w != 16 or h % 16 != 0:
            errors.append(f"texture {tex} has unexpected size {img.size}")
        if h > 16 and not os.path.exists(tp + ".mcmeta"):
            errors.append(f"texture {tex} is {w}x{h} but has no .mcmeta animation")


for b in blocks:
    bs = os.path.join(ASSETS, "blockstates", f"{b}.json")
    if not os.path.exists(bs):
        errors.append(f"block {b}: no blockstate")
    else:
        for variant in load(bs).get("variants", {}).values():
            for v in (variant if isinstance(variant, list) else [variant]):
                check_model(v["model"], f"blockstate {b}")
    if not os.path.exists(os.path.join(DATA, MOD, "loot_tables", "blocks", f"{b}.json")):
        errors.append(f"block {b}: no loot table")
    if f"block.{MOD}.{b}" not in lang:
        errors.append(f"block {b}: no lang entry")

for i in items:
    im = os.path.join(ASSETS, "models", "item", f"{i}.json")
    if not os.path.exists(im):
        errors.append(f"item {i}: no item model")
    else:
        check_model(f"{MOD}:item/{i}", f"item {i}")
    if f"item.{MOD}.{i}" not in lang and f"block.{MOD}.{i}" not in lang:
        errors.append(f"item {i}: no lang entry")

# Item references in data files
known = set(items) | set(blocks)
ref_pattern = re.compile(r'"(?:item|name|Name)"\s*:\s*"sweettreats:([a-z_]+)"')
value_pattern = re.compile(r'"sweettreats:([a-z_]+)"')
for folder in ("recipes", "loot_tables", "loot_modifiers", "worldgen", "tags"):
    base = os.path.join(DATA, MOD, folder)
    for dirpath, _, files in os.walk(base):
        for name in files:
            with open(os.path.join(dirpath, name), encoding="utf-8") as fh:
                text = fh.read()
            json.loads(text)
            for ref in ref_pattern.findall(text):
                if ref not in known:
                    errors.append(f"{folder}/{name}: unknown sweettreats:{ref}")
for dirpath, _, files in os.walk(os.path.join(DATA, "minecraft", "tags")):
    for name in files:
        with open(os.path.join(dirpath, name), encoding="utf-8") as fh:
            for ref in value_pattern.findall(fh.read()):
                if ref not in known:
                    errors.append(f"tag {name}: unknown sweettreats:{ref}")

# GUI textures
for gui in ("bakery_oven", "mixing_bowl", "decoration_station", "recipe_book"):
    p = os.path.join(ASSETS, "textures", "gui", f"{gui}.png")
    if not os.path.exists(p) or Image.open(p).size != (256, 256):
        errors.append(f"gui texture {gui} missing or not 256x256")

print(f"{len(blocks)} blocks, {len(items)} items, {len(checked_models)} models checked")
if errors:
    print(f"{len(errors)} problem(s):")
    for e in errors:
        print("  -", e)
    sys.exit(1)
print("All resources OK")

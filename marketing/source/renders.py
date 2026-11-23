#!/usr/bin/env python3
"""Renders the Sweet Treats blocks that the artwork shows from the mod's own block models and textures, as isometric
PNGs with transparent backgrounds, into marketing/source/renders/. It needs no Minecraft client: the few vanilla parent
models the mod uses (block, orientable_with_bottom, cake and its slices, crop, cross) are written out below.
Requires Python 3 with Pillow and NumPy. Run from anywhere:

    python marketing/source/renders.py
"""
import json
import math
from pathlib import Path

import numpy as np
from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / "src" / "main" / "resources" / "assets" / "sweettreats"
OUT = Path(__file__).resolve().parent / "renders"
SCALE = 12

# Block models drawn for the cards (file name = model name). Machines, decorations, placeable treats, the lit oven,
# fully grown crops and fruiting trees, and the wild plants.
MODELS = ["bakery_oven", "bakery_oven_on", "mixing_bowl", "decoration_station", "cake_display", "bakery_counter",
          "cupcake_stand", "dessert_plate", "glass_jar", "tea_set", "bakery_sign", "decorative_menu",
          "strawberry_cake", "strawberry_cake_slice3", "cherry_pie", "cherry_pie_slice1",
          "strawberry_crop_stage3", "vanilla_crop_stage3", "lavender_crop_stage3",
          "peach_tree_stage0", "peach_tree_stage1", "peach_tree_stage2", "peach_tree_stage3",
          "cherry_tree_stage0", "cherry_tree_stage1", "cherry_tree_stage2", "cherry_tree_stage3",
          "wild_strawberries", "wild_lavender", "wild_vanilla"]


def cube(tex):
    return [{"from": [0, 0, 0], "to": [16, 16, 16], "faces": {d: {"texture": t} for d, t in tex.items()}}]


def cake(bites):
    x0 = 1 + bites * 2
    side = "#inside" if bites else "#side"
    return [{"from": [x0, 0, 1], "to": [15, 8, 15], "faces": {
        "down": {"texture": "#bottom"}, "up": {"texture": "#top"}, "north": {"texture": "#side"},
        "south": {"texture": "#side"}, "west": {"texture": side}, "east": {"texture": "#side"}}}]


def crop():
    els = []
    for x in (4, 12):
        els.append({"from": [x, -1, 0], "to": [x, 15, 16], "shade": False,
                    "faces": {"west": {"texture": "#crop"}, "east": {"texture": "#crop"}}})
    for z in (4, 12):
        els.append({"from": [0, -1, z], "to": [16, 15, z], "shade": False,
                    "faces": {"north": {"texture": "#crop"}, "south": {"texture": "#crop"}}})
    return els


def cross():
    return [{"from": [0.8, 0, 8], "to": [15.2, 16, 8], "shade": False,
             "rotation": {"origin": [8, 8, 8], "axis": "y", "angle": 45},
             "faces": {"north": {"texture": "#cross"}, "south": {"texture": "#cross"}}},
            {"from": [8, 0, 0.8], "to": [8, 16, 15.2], "shade": False,
             "rotation": {"origin": [8, 8, 8], "axis": "y", "angle": 45},
             "faces": {"west": {"texture": "#cross"}, "east": {"texture": "#cross"}}}]


VANILLA = {
    "minecraft:block/orientable_with_bottom": cube({"down": "#bottom", "up": "#top", "north": "#front",
                                                    "south": "#side", "west": "#side", "east": "#side"}),
    "minecraft:block/cake": cake(0),
    **{f"minecraft:block/cake_slice{i}": cake(i) for i in range(1, 7)},
    "minecraft:block/crop": crop(),
    "minecraft:block/cross": cross(),
}


def flatten(name):
    tex, elements, ref = {}, None, f"sweettreats:block/{name}"
    chain = []
    while ref:
        if ref.startswith("sweettreats:"):
            m = json.loads((ASSETS / "models" / (ref.split(":")[1] + ".json")).read_text())
            chain.append(m)
            ref = m.get("parent")
        else:
            if elements is None and ref in VANILLA:
                chain.append({"elements": VANILLA[ref]})
            break
    for m in reversed(chain):
        tex.update(m.get("textures", {}))
    elements = next((m["elements"] for m in chain if "elements" in m), [])
    return tex, elements


def face_quad(fr, to, d):
    x0, y0, z0 = fr
    x1, y1, z1 = to
    return {"north": [(x1, y1, z0), (x0, y1, z0), (x0, y0, z0), (x1, y0, z0)],
            "south": [(x0, y1, z1), (x1, y1, z1), (x1, y0, z1), (x0, y0, z1)],
            "west": [(x0, y1, z0), (x0, y1, z1), (x0, y0, z1), (x0, y0, z0)],
            "east": [(x1, y1, z1), (x1, y1, z0), (x1, y0, z0), (x1, y0, z1)],
            "up": [(x0, y1, z0), (x1, y1, z0), (x1, y1, z1), (x0, y1, z1)],
            "down": [(x0, y0, z1), (x1, y0, z1), (x1, y0, z0), (x0, y0, z0)]}[d]


def default_uv(fr, to, d):
    x0, y0, z0 = fr
    x1, y1, z1 = to
    return {"north": [16 - x1, 16 - y1, 16 - x0, 16 - y0], "south": [x0, 16 - y1, x1, 16 - y0],
            "west": [z0, 16 - y1, z1, 16 - y0], "east": [16 - z1, 16 - y1, 16 - z0, 16 - y0],
            "up": [x0, z0, x1, z1], "down": [x0, 16 - z1, x1, 16 - z0]}[d]


def rot_point(p, rot):
    a = math.radians(rot["angle"])
    o = rot["origin"]
    x, y, z = (p[i] - o[i] for i in range(3))
    c, s = math.cos(a), math.sin(a)
    if rot["axis"] == "y":
        x, z = x * c + z * s, -x * s + z * c
    elif rot["axis"] == "x":
        y, z = y * c - z * s, y * s + z * c
    else:
        x, y = x * c - y * s, x * s + y * c
    return (x + o[0], y + o[1], z + o[2])


VIEW = np.array([1.0, -0.8, 1.0]); VIEW /= np.linalg.norm(VIEW)
RIGHT = np.array([-1 / math.sqrt(2), 0.0, 1 / math.sqrt(2)])
UP = np.cross(RIGHT, VIEW); UP /= np.linalg.norm(UP)
if UP[1] < 0:
    UP = -UP
_tex = {}


def texture(ref):
    if ref not in _tex:
        img = Image.open(ASSETS / "textures" / (ref.split(":")[1] + ".png")).convert("RGBA")
        a = np.asarray(img).astype(np.float32) / 255
        _tex[ref] = a[: a.shape[1]]  # animated strips: first frame
    return _tex[ref]


def render(name, scale=SCALE):
    tex, elements = flatten(name)
    faces = []
    for el in elements:
        for d, f in el["faces"].items():
            quad = face_quad(el["from"], el["to"], d)
            if "rotation" in el:
                quad = [rot_point(p, el["rotation"]) for p in quad]
            var = f["texture"].lstrip("#")
            while var in tex and tex[var].startswith("#"):
                var = tex[var].lstrip("#")
            if var not in tex:
                continue
            faces.append((np.array(quad, float) - 8, f.get("uv") or default_uv(el["from"], el["to"], d),
                          texture(tex[var]), el.get("shade", True)))
    pts = np.concatenate([q for q, *_ in faces])
    px, py = pts @ RIGHT, pts @ UP
    w = int((px.max() - px.min()) * scale) + 8
    h = int((py.max() - py.min()) * scale) + 8
    ox = w / 2 - (px.max() + px.min()) / 2 * scale
    oy = h / 2 + (py.max() + py.min()) / 2 * scale
    rgba = np.zeros((h, w, 4), np.float32)
    zbuf = np.full((h, w), 1e9, np.float32)
    ys, xs = np.mgrid[0:h, 0:w] + 0.5
    # opaque texels first with a depth buffer, then translucent ones blended back to front
    for translucent in (False, True):
        order = sorted(faces, key=lambda f: -(f[0].mean(0) @ VIEW)) if translucent else faces
        for quad, uv, tpx, shade in order:
            e1, e2 = quad[1] - quad[0], quad[3] - quad[0]
            n = -np.cross(e1, e2)
            n /= np.linalg.norm(n) or 1
            two_sided = not shade
            if n @ VIEW > 0.02 and not two_sided:
                continue
            bright = 1.0 if not shade else min(1.0, max(0.5, 0.6 + 0.4 * n[1] + 0.2 * abs(n[2])))
            sp = np.stack([ox + quad @ RIGHT * scale, oy - quad @ UP * scale, quad @ VIEW], 1)
            u0, v0, u1, v1 = uv
            tuv = np.array([(u0, v0), (u1, v0), (u1, v1), (u0, v1)], float)
            th, tw = tpx.shape[:2]
            for tri in ((0, 1, 2), (0, 2, 3)):
                A, B, C = sp[list(tri)]
                den = (B[1] - C[1]) * (A[0] - C[0]) + (C[0] - B[0]) * (A[1] - C[1])
                if abs(den) < 1e-9:
                    continue
                x0, x1 = max(0, int(min(A[0], B[0], C[0]))), min(w, int(max(A[0], B[0], C[0])) + 2)
                y0, y1 = max(0, int(min(A[1], B[1], C[1]))), min(h, int(max(A[1], B[1], C[1])) + 2)
                X, Y = xs[y0:y1, x0:x1], ys[y0:y1, x0:x1]
                l1 = ((B[1] - C[1]) * (X - C[0]) + (C[0] - B[0]) * (Y - C[1])) / den
                l2 = ((C[1] - A[1]) * (X - C[0]) + (A[0] - C[0]) * (Y - C[1])) / den
                l3 = 1 - l1 - l2
                inside = (l1 >= -1e-6) & (l2 >= -1e-6) & (l3 >= -1e-6)
                z = l1 * A[2] + l2 * B[2] + l3 * C[2]
                T = tuv[list(tri)]
                u = l1 * T[0, 0] + l2 * T[1, 0] + l3 * T[2, 0]
                v = l1 * T[0, 1] + l2 * T[1, 1] + l3 * T[2, 1]
                tx = np.clip((u / 16 * tw).astype(int), 0, tw - 1)
                ty = np.clip((v / 16 * th).astype(int), 0, th - 1)
                col = tpx[ty, tx]
                alpha = col[..., 3]
                zb = zbuf[y0:y1, x0:x1]
                if not translucent:
                    m = inside & (alpha > 0.9) & (z < zb)
                    zb[m] = z[m]
                    rgba[y0:y1, x0:x1][m] = np.concatenate([col[..., :3][m] * bright, np.ones((m.sum(), 1))], 1)
                else:
                    m = inside & (alpha > 0.1) & (alpha <= 0.9) & (z < zb)
                    a = alpha[m][:, None]
                    dst = rgba[y0:y1, x0:x1][m]
                    out_a = a + dst[:, 3:] * (1 - a)
                    out_rgb = (col[..., :3][m] * bright * a + dst[:, :3] * dst[:, 3:] * (1 - a)) / np.maximum(out_a, 1e-6)
                    rgba[y0:y1, x0:x1][m] = np.concatenate([out_rgb, out_a], 1)
    return Image.fromarray((np.clip(rgba, 0, 1) * 255).astype(np.uint8))


def main():
    OUT.mkdir(exist_ok=True)
    for name in MODELS:
        render(name).save(OUT / f"{name}.png")
    print(f"rendered {len(MODELS)} models into {OUT}")


if __name__ == "__main__":
    main()

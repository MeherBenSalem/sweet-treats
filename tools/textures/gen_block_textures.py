#!/usr/bin/env python3
"""
Sweet Treats - block texture generator (16x16 pixel art, vanilla-style).

Run from the project root (or anywhere):
    python tools/textures/gen_block_textures.py

Writes RGBA PNGs to src/main/resources/assets/sweettreats/textures/block/
Everything is deterministic: hand-authored ASCII maps + hash-based dithering,
so re-running produces byte-identical output.
"""
from __future__ import annotations

import json
import math
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
OUT_DIR = ROOT / "src" / "main" / "resources" / "assets" / "sweettreats" / "textures" / "block"

CLEAR = (0, 0, 0, 0)


# --------------------------------------------------------------------------- helpers
def hx(h: str, a: int = 255):
    h = h.lstrip("#")
    return (int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), a)


def mix(c1, c2, t: float):
    return tuple(int(round(c1[i] + (c2[i] - c1[i]) * t)) for i in range(4))


def alpha(c, a: int):
    return (c[0], c[1], c[2], a)


def hash01(x: int, y: int, seed: int = 0) -> float:
    """Deterministic per-pixel hash in [0, 1)."""
    h = (x * 73856093) ^ (y * 19349663) ^ (seed * 83492791) ^ 0x9E3779B9
    h &= 0xFFFFFFFF
    h ^= h >> 13
    h = (h * 0x5BD1E995) & 0xFFFFFFFF
    h ^= h >> 15
    h = (h * 0x27D4EB2D) & 0xFFFFFFFF
    h ^= h >> 16
    return (h & 0xFFFFFF) / float(0x1000000)


class Tex:
    def __init__(self, w: int = 16, h: int = 16, fill=CLEAR):
        self.w, self.h = w, h
        self.px = [[fill for _ in range(w)] for _ in range(h)]

    def set(self, x, y, c):
        if c is not None and 0 <= x < self.w and 0 <= y < self.h:
            self.px[y][x] = c

    def setw(self, x, y, c):  # wrapped set (for tileable materials)
        self.px[y % self.h][x % self.w] = c

    def get(self, x, y):
        return self.px[y][x]

    def rect(self, x0, y0, x1, y1, c):
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1):
                self.set(x, y, c)

    def hline(self, x0, x1, y, c):
        self.rect(x0, y, x1, y, c)

    def vline(self, x, y0, y1, c):
        self.rect(x, y0, x, y1, c)

    def stamp(self, rows, pal, ox=0, oy=0, wrap=False):
        for j, row in enumerate(rows):
            for i, ch in enumerate(row):
                if ch in ". ":
                    continue
                if wrap:
                    self.setw(ox + i, oy + j, pal[ch])
                else:
                    self.set(ox + i, oy + j, pal[ch])
        return self

    def line(self, x0, y0, x1, y1, c):
        dx, dy = abs(x1 - x0), -abs(y1 - y0)
        sx, sy = (1 if x0 < x1 else -1), (1 if y0 < y1 else -1)
        err = dx + dy
        while True:
            self.set(x0, y0, c)
            if x0 == x1 and y0 == y1:
                break
            e2 = 2 * err
            if e2 >= dy:
                err += dy
                x0 += sx
            if e2 <= dx:
                err += dx
                y0 += sy

    def replace(self, src, dst, region=None, prob=1.0, seed=0):
        x0, y0, x1, y1 = region or (0, 0, self.w - 1, self.h - 1)
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1):
                if self.px[y][x] == src and (prob >= 1.0 or hash01(x, y, seed) < prob):
                    self.px[y][x] = dst

    def copy(self):
        t = Tex(self.w, self.h)
        t.px = [row[:] for row in self.px]
        return t

    def image(self) -> Image.Image:
        img = Image.new("RGBA", (self.w, self.h))
        img.putdata([p for row in self.px for p in row])
        return img


def ascii_tex(rows, pal, name="?", w=16, h=16):
    """Build a texture from an ASCII map. Short maps are bottom-aligned."""
    for i, r in enumerate(rows):
        if len(r) != w:
            raise ValueError(f"{name}: row {i} has length {len(r)} (expected {w}): {r!r}")
    if len(rows) > h:
        raise ValueError(f"{name}: {len(rows)} rows (max {h})")
    rows = ["." * w] * (h - len(rows)) + list(rows)
    t = Tex(w, h)
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch == ".":
                continue
            if ch not in pal:
                raise KeyError(f"{name}: unknown char {ch!r} at ({x},{y})")
            t.set(x, y, pal[ch])
    return t


def check_sprite(rows, name):
    wd = len(rows[0])
    for i, r in enumerate(rows):
        if len(r) != wd:
            raise ValueError(f"sprite {name}: row {i} width {len(r)} != {wd}")
    return rows


# --------------------------------------------------------------------------- palette
C = {
    "pink": hx("FFC4D9"), "pink_l": hx("FFE0EC"), "pink_m": hx("F59AB9"), "pink_s": hx("D9739A"), "pink_d": hx("A64B6E"),
    "cream": hx("FFF3D6"), "cream_l": hx("FFFBEF"), "cream_s": hx("EBD3A5"), "cream_d": hx("B8956A"),
    "straw": hx("E8384F"), "straw_l": hx("FF6B7E"), "straw_s": hx("B5203A"), "straw_d": hx("7A1428"), "seed": hx("FFE58A"),
    "lav": hx("C3A6EB"), "lav_l": hx("E3D4FA"), "lav_s": hx("9B79D1"), "lav_d": hx("6A4C9C"),
    "choc": hx("7B4A35"), "choc_l": hx("A0674B"), "choc_s": hx("5A3324"), "choc_d": hx("3B2016"),
    "leaf": hx("6FBF5A"), "leaf_l": hx("9BDB7B"), "leaf_s": hx("4A9440"), "leaf_d": hx("2E6B2E"),
    "stem": hx("8A5A3C"), "stem_d": hx("5E3B25"),
    "peach": hx("FFB38A"), "peach_l": hx("FFD0A8"), "peach_s": hx("F08C5E"),
    "cherry": hx("C8102E"), "cherry_l": hx("F0405A"), "cherry_d": hx("8A0A1F"),
    "blossom": hx("FFB7D5"), "blossom_w": hx("FFF0F6"),
    "crust": hx("E6B36E"), "crust_s": hx("C98D45"), "crust_d": hx("96612E"),
    "white": hx("FFFFFF"), "porc": hx("F2EEF2"), "porc_s": hx("D8CCD8"),
    "gold": hx("F2C94C"), "gold_s": hx("C99A2E"), "gold_l": hx("FFE89A"),
    "oak": hx("C8A06A"), "oak_s": hx("A47E4E"), "oak_l": hx("D9B57E"), "oak_d": hx("86643A"),
}

# Shared plant palette
PLANT = {
    "D": C["leaf_d"], "d": C["leaf_s"], "G": C["leaf"], "g": C["leaf_l"],
    "b": C["stem"], "B": C["stem_d"], "n": hx("A87550"),
    "R": C["straw"], "r": C["straw_l"], "S": C["straw_s"], "X": C["straw_d"], "y": C["seed"],
    "w": C["white"], "W": hx("EADDE6"), "o": hx("F2C94C"),
    # lavender family
    "a": hx("93A882"), "A": hx("6C8660"), "q": hx("BCCCAA"),
    "L": C["lav"], "l": C["lav_l"], "V": C["lav_s"], "v": C["lav_d"], "P": hx("9A66DB"), "p": hx("B48BEA"),
    # vanilla family
    "c": hx("FFF6CC"), "C": hx("EFDB8E"), "k": hx("D9B44A"),
    "m": hx("A3A64A"), "M": hx("7D7A33"), "u": hx("6B4A2A"),
    # peach / cherry / blossom
    "e": C["peach_l"], "E": C["peach"], "f": C["peach_s"], "F": hx("C9623E"), "h": hx("FF8F86"),
    "K": C["cherry"], "j": C["cherry_l"], "Q": C["cherry_d"],
    "i": C["blossom_w"], "s": C["blossom"], "t": hx("F28DB8"), "z": hx("E0609A"),
    # cherry bark
    "x": hx("8C5A48"), "Z": hx("55302A"), "Y": hx("B07F6A"),
}


# --------------------------------------------------------------------------- plants: strawberry
SP_STRAW_FLOWER = check_sprite([".w.", "wow", ".W."], "straw_flower")
SP_BERRY = check_sprite(["gGd", "rRy", "RyS", "SRX", ".X."], "berry")
SP_BERRY_SMALL = check_sprite(["Gd", "rR", "Sy", "X."], "berry_small")

STRAW_BUSH = [
    "..gG............",
    "gGGdgG....gG....",
    "Gd.DGd..gGGdgG..",
    ".gGd.gG.Gd.DGd.g",
    "gGGdgGGd..gGd.gG",
    "Gd.DdGd.gGGdgGGd",
    ".gGd.Dd.Gd.DdDd.",
    ".Gd.d.gG..gGd.d.",
    "...dd.Gd..Gd.dd.",
    "...D.d....d..D..",
]


def strawberry_crop(stage: int) -> Tex:
    if stage == 0:
        return ascii_tex([
            "..g.g...........",
            ".gGdGg.....g.g..",
            "...d..gdG.gGdGg.",
            "...d...d....d...",
        ], PLANT, "straw0")
    if stage == 1:
        return ascii_tex([
            "...gG...........",
            ".gGGdgG.........",
            ".Gd..Gd...gG....",
            "...Dd...gGGdgG..",
            "...d.gG.Gd..Gd..",
            ".gGddGd...Dd....",
            ".Gdd......d.gG..",
            "...D......DdGd..",
        ], PLANT, "straw1")
    if stage == 2:
        t = ascii_tex(STRAW_BUSH, PLANT, "straw2")
        t.set(3, 6, PLANT["d"]); t.set(11, 7, PLANT["d"])
        t.stamp(SP_STRAW_FLOWER, PLANT, 2, 3)
        t.stamp(SP_STRAW_FLOWER, PLANT, 10, 4)
        t.stamp(SP_STRAW_FLOWER, PLANT, 5, 9)
        return t
    t = ascii_tex(["...gG.....gG...."] + STRAW_BUSH, PLANT, "straw3")
    t.stamp(SP_BERRY, PLANT, 0, 10)
    t.stamp(SP_BERRY, PLANT, 6, 11)
    t.stamp(SP_BERRY, PLANT, 12, 10)
    t.stamp(SP_BERRY_SMALL, PLANT, 9, 7)
    t.stamp(SP_STRAW_FLOWER, PLANT, 2, 4)
    return t


# --------------------------------------------------------------------------- plants: vanilla
VANILLA_VINE = [
    "...g............",
    "...d.......g....",
    ".gGd.......dD...",
    "gGGd.......d.gG.",
    ".ddDd.....gdGGGd",
    "....dgG..gGd.dd.",
    "....dGGg.GGd....",
    "...dDdd...dD....",
    ".gGd......d.gG..",
    "gGGd......dGGGd.",
    ".ddD.....gd.dd..",
    "...d....gGGd....",
    "...dD...Gdd.....",
    "....d.....d.....",
]
SP_ORCHID = check_sprite([".c.", "ckc", "C.C"], "orchid")
SP_POD = check_sprite(["M", "m", "m", "m", "u"], "pod")
SP_POD_S = check_sprite(["M", "m", "m", "u"], "pod_s")


def vanilla_crop(stage: int) -> Tex:
    if stage == 0:
        return ascii_tex([
            ".....g..........",
            "....gd..........",
            ".gG.d......g....",
            "gGGdd......dgG..",
            ".dd.d......dGGd.",
            "....d......d.dd.",
        ], PLANT, "van0")
    t = ascii_tex(VANILLA_VINE, PLANT, "vanilla")
    if stage == 2:
        t.stamp(SP_ORCHID, PLANT, 4, 2)
        t.stamp(SP_ORCHID, PLANT, 0, 8)
        t.stamp(SP_ORCHID, PLANT, 12, 2)
        t.stamp(SP_ORCHID, PLANT, 7, 9)
        t.stamp(SP_ORCHID, PLANT, 13, 8)
    elif stage == 3:
        # hanging bunches of thin pods (1px wide, 1px gaps so they never merge)
        t.stamp(SP_POD, PLANT, 5, 9)
        t.stamp(SP_POD_S, PLANT, 7, 9)
        t.stamp(SP_POD_S, PLANT, 1, 7)
        t.stamp(SP_POD_S, PLANT, 12, 7)
        t.stamp(SP_POD, PLANT, 14, 7)
        t.stamp(SP_POD_S, PLANT, 9, 10)
    return t


# --------------------------------------------------------------------------- plants: lavender
LAV_STEMS = [  # (x, top_y at stage 1)
    (2, 6), (5, 8), (8, 5), (11, 7), (13, 9),
]


def _lav_base(t: Tex, lift: int):
    P = PLANT
    for (x, top) in LAV_STEMS:
        top -= lift
        for y in range(top, 16):
            t.set(x, y, P["G"] if y < top + 3 else P["d"])
        t.set(x, top, P["g"])
    # grey-green basal leaves
    t.stamp([
        ".q.......q....q.",
        "qaA..q..aAq..aA.",
        ".Aa.qaA.aA..qaAa",
        "aAAaAAaAAAaaAAA.",
    ], P, 0, 12)


def lav_spike(t: Tex, x: int, top: int, h: int, vivid: bool = True):
    P = PLANT
    if vivid:
        cyc = [("l", "L"), ("P", "V"), ("L", "P"), ("V", "v"), ("p", "P"), ("v", "V")]
    else:
        cyc = [("l", "L"), ("L", "l"), ("l", "V"), ("L", "l")]
    t.set(x + 1, top, P["l"])
    for i in range(1, h):
        a, b = cyc[(i - 1) % len(cyc)]
        t.set(x, top + i, P[a])
        t.set(x + 1, top + i, P[b])


def lavender_crop(stage: int) -> Tex:
    t = Tex()
    P = PLANT
    if stage == 0:
        return ascii_tex([
            "..q..........q..",
            ".aAq...q...aqA..",
            "..aA..qa....aAq.",
            "..AA...A....AA..",
        ], P, "lav0")
    if stage == 1:
        _lav_base(t, 0)
        return t
    if stage == 2:
        _lav_base(t, 1)
        for (x, top) in LAV_STEMS:
            top -= 1
            t.set(x, top - 1, P["l"])
            t.set(x, top, P["L"])
            t.set(x - 1, top + 1, P["l"])
            t.set(x, top + 1, P["G"])
            t.set(x + 1, top + 2, P["L"])
        return t
    _lav_base(t, 2)
    for i, (x, top) in enumerate(LAV_STEMS):
        top -= 2
        sx = x - 1 if i % 2 == 0 else x
        lav_spike(t, sx, top - 2, 7)
    return t


# --------------------------------------------------------------------------- plants: trees
def canopy(t: Tex, lobes, seed: int):
    """Leafy canopy built from round lobes (drawn back-to-front). Each lobe is lit from the
    top-left; front lobes cast a 1px shadow onto the lobes behind them, undersides darken."""
    owner = {}
    for i, (cx, cy, r) in enumerate(lobes):
        for y in range(16):
            for x in range(16):
                dx, dy = x + 0.5 - cx, y + 0.5 - cy
                if dx * dx + dy * dy <= r * r:
                    owner[(x, y)] = i
    darker = {"g": "G", "G": "d", "d": "D", "D": "D"}
    lighter = {"g": "g", "G": "g", "d": "G", "D": "d"}
    for (x, y), i in owner.items():
        cx, cy, r = lobes[i]
        dx, dy = x + 0.5 - cx, y + 0.5 - cy
        u = (dx + dy) / (r * 1.414)
        dist = math.hypot(dx, dy) / r
        ch = "g" if u < -0.38 else "d" if u > 0.42 else "G"
        if dist > 0.78 and u > 0.25:
            ch = "D"
        for (nx, ny) in ((x - 1, y), (x, y - 1), (x - 1, y - 1)):
            j = owner.get((nx, ny))
            if j is not None and j > i:
                ch = darker[ch]
                break
        if (x, y + 1) not in owner:
            ch = darker[ch]
        h = hash01(x, y, seed)
        if h < 0.07:
            ch = lighter[ch]
        elif h > 0.94:
            ch = darker[ch]
        t.set(x, y, PLANT[ch])
    return owner


PEACH_LOBES = [(7.6, 3.3, 3.2), (4.0, 5.6, 3.4), (11.6, 5.4, 3.3), (7.9, 7.1, 2.9)]
PEACH_TRUNK = [
    "......b..B......",
    ".......nB.......",
    ".......nB.......",
    ".......nB.......",
    ".......nB.......",
    "......bnBB......",
    ".....bBnBBb.....",
]
SP_PEACH = check_sprite([".eE", "eEf", "Ehf", ".fF"], "peach")
SP_PEACH_BLOSSOM = check_sprite([".s.", "szs", ".t."], "peach_blossom")

CHERRY_LOBES = [(8.0, 3.9, 3.3), (3.3, 5.7, 3.0), (12.7, 5.5, 3.0), (5.9, 7.4, 2.6), (10.5, 7.6, 2.6)]
CHERRY_TRUNK = [
    "......Y..Z......",
    "......xYxZ......",
    ".......YZ.......",
    ".......xZ.......",
    ".......YZ.......",
    "......xxZZ......",
    ".....xZxZZx.....",
]


def _tree(lobes, trunk, seed) -> Tex:
    t = Tex()
    owner = canopy(t, lobes, seed)
    for j, row in enumerate(trunk):
        y = 16 - len(trunk) + j
        for x, ch in enumerate(row):
            if ch != "." and ((x, y) not in owner or y >= 10):
                t.set(x, y, PLANT[ch])
    return t
SP_CHERRY_PAIR = check_sprite(["..B..", ".B.B.", "jK.jK", "KQ.KQ"], "cherry_pair")
SP_CHERRY_BLOSSOM = check_sprite([".i.", "isi", ".s."], "cherry_blossom")


def peach_tree(stage: int) -> Tex:
    if stage == 0:
        return ascii_tex([
            "........gG......",
            ".......gGGd.....",
            "...gG...dd......",
            "..gGGd..b.......",
            "...dDDb.b.......",
            ".......bb.......",
            "........b.......",
            "........b.......",
            ".......bBb......",
        ], PLANT, "peach0")
    t = _tree(PEACH_LOBES, PEACH_TRUNK, 101)
    if stage == 2:
        for (x, y) in [(2, 1), (8, 2), (4, 5), (11, 4), (7, 7), (0, 6), (12, 8)]:
            t.stamp(SP_PEACH_BLOSSOM, PLANT, x, y)
    elif stage == 3:
        for (x, y) in [(1, 6), (5, 2), (10, 4), (4, 9), (13, 8)]:
            t.stamp(SP_PEACH, PLANT, x, y)
    return t


def cherry_tree(stage: int) -> Tex:
    if stage == 0:
        return ascii_tex([
            ".......gG.......",
            "......gGd.gG....",
            ".......dx.Gd....",
            "........xx......",
            "........x.......",
            "...gG...x.......",
            "...Gdxx.x.......",
            "......xxx.......",
            "........x.......",
            ".......xZx......",
        ], PLANT, "cherry0")
    t = _tree(CHERRY_LOBES, CHERRY_TRUNK, 202)
    if stage == 2:
        for (x, y) in [(3, 2), (9, 1), (1, 5), (6, 4), (12, 3), (10, 6), (4, 7), (13, 7), (7, 8)]:
            t.stamp(SP_CHERRY_BLOSSOM, PLANT, x, y)
    elif stage == 3:
        for (x, y) in [(0, 4), (5, 1), (11, 3), (2, 7), (9, 7)]:
            t.stamp(SP_CHERRY_PAIR, PLANT, x, y)
    return t


# --------------------------------------------------------------------------- plants: wild
def wild_strawberries() -> Tex:
    t = ascii_tex([
        "......gG........",
        "....gGGdgG......",
        "..gG.Gd.Gd.gG...",
        ".gGGdgG..gGGdgG.",
        ".Gd.DGd..Gd.DGd.",
        "..gGd.DdDd..gG..",
        ".gGGdgG.dgG.GGd.",
        ".Gd..Gd.dGGd.d..",
        "......Dd.d.dD...",
        ".......DdD......",
    ], PLANT, "wild_straw")
    t.stamp(SP_STRAW_FLOWER, PLANT, 6, 4)
    t.stamp(SP_BERRY, PLANT, 1, 11)
    t.stamp(SP_BERRY, PLANT, 11, 11)
    t.stamp(SP_BERRY_SMALL, PLANT, 12, 6)
    return t


def wild_lavender() -> Tex:
    t = Tex()
    P = PLANT
    # stems (x_top, y_top) -> base around (7..8, 15)
    stems = [((3, 8), (6, 14)), ((7, 6), (7, 14)), ((11, 7), (9, 14)), ((1, 11), (5, 14)), ((14, 10), (10, 14))]
    for (x0, y0), (x1, y1) in stems:
        t.line(x0, y0, x1, y1, P["d"])
        t.line(x0, y0, x0, y0 + 1, P["G"])
    lav_spike(t, 2, 2, 7)
    lav_spike(t, 6, 0, 7)
    lav_spike(t, 10, 1, 7)
    lav_spike(t, 0, 6, 6)
    lav_spike(t, 13, 5, 6)
    t.stamp([
        "....q.....q.....",
        "..qaA.q..aAq.q..",
        ".aAAaAAaAAaAaAq.",
    ], P, 0, 13)
    return t


def wild_vanilla() -> Tex:
    t = ascii_tex([
        "........g.......",
        "........d.......",
        "....gG..dD......",
        "...gGGd.d.gG....",
        "....ddDdd.GGGd..",
        ".......d.dGdd...",
        "...gG..dDd......",
        "..gGGGd.d.......",
        "...ddDdd...gG...",
        "........d.gGGd..",
        ".......dDdGdd...",
        "....gG..d.......",
        "...gGGd.dD......",
        "....dd.Dd.......",
    ], PLANT, "wild_van")
    t.stamp(SP_ORCHID, PLANT, 9, 1)
    t.stamp(SP_ORCHID, PLANT, 1, 4)
    t.stamp(SP_ORCHID, PLANT, 12, 9)
    t.stamp(SP_POD, PLANT, 11, 10)
    t.stamp(SP_POD_S, PLANT, 5, 10)
    return t


# --------------------------------------------------------------------------- bakery oven
OVEN = {
    "O": C["pink_d"], "p": C["pink_s"], "Q": C["pink_m"], "P": C["pink"], "l": C["pink_l"],
    "c": C["cream"], "C": C["cream_l"], "e": C["cream_s"], "E": C["cream_d"],
    "g": C["gold"], "G": C["gold_s"], "y": hx("FFF0B0"),
    "k": hx("2A1B24"), "K": hx("3D2933"), "r": hx("5A4552"), "h": hx("7E6A7A"),
    "w": C["white"], "v": hx("8E3F60"), "V": hx("6E2E4A"),
    # glow / goodies
    "Y": hx("FFF4BE"), "j": hx("FFD86B"), "o": hx("FFA94D"), "u": hx("F27A3A"), "f": hx("E8562A"),
    "t": hx("ECE4EC"), "T": hx("A89AA8"), "z": hx("FF9EC4"), "Z": hx("E26F9E"),
    "b": C["crust"], "B": C["crust_d"], "n": hx("F5D296"),
    "a": hx("FFD2A6"),
}

def _oven_front_rows(glass, frame_bottom="lllllll"):
    rows = [
        "OppppppppppppppO",
        "pCclQcclQcclQccp",   # cream control panel with three pink knobs
        "pccQOecQOecQOeep",
        "peeeeeeeeeeeeeep",
        "plPPPPPPPPPPPPQp",
        "plCCCCCCCCCCCcQp",
        "plCPyggggggGPeQp",
        "plCPOOOOOOOOPeQp",
    ]
    for g in glass:
        rows.append("plCPO" + g + "lPeQp")
    rows.append("plCPQ" + frame_bottom + "PeQp")
    rows.append("pl" + "e" * 12 + "Qp")
    rows.append("OppppppppppppppO")
    return rows


def oven_front(on: bool = False, frame: int = 0) -> Tex:
    if not on:
        glass = ["hKKkkk", "Kkkkkk", "kkkkkk", "rrrrrr", "kkkkkK"]
        return ascii_tex(_oven_front_rows(glass), OVEN, "oven_front")
    glows = [
        ["YjYYjY", "zZjnjz", "BbjbBb", "tTtttT", "oufouo"],
        ["jYYjYY", "zZYnjz", "BbjbBb", "tTtttT", "uofuof"],
        ["YYjYYj", "zZjnYz", "BbYbBb", "tTtttT", "ofouuo"],
        ["YjYjYY", "zZjnjz", "Bbjbbb", "tTtttT", "fuoofu"],
    ]
    glass = glows[frame % 4]
    rows = _oven_front_rows(glass, frame_bottom="aaaaaaa")
    return ascii_tex(rows, OVEN, "oven_front_on")


def oven_side() -> Tex:
    rows = [
        "OppppppppppppppO",
        "plllllllllllllQp",
        "plCCCCCCCCCCCcQp",
        "plCPPPPPPPPPPeQp",
        "plCPPPPPPPPPPeQp",
        "plCPPvv..vvPPeQp",
        "plCPvVVvvVVvPeQp",
        "plCPvvvvvvvvPeQp",
        "plCPPvVVVVvPPeQp",
        "plCPPPvvvvPPPeQp",
        "plCPPPPVVPPPPeQp",
        "plCPPPPPPPPPPeQp",
        "plCPPPPPPPPPPeQp",
        "plceeeeeeeeeeeQp",
        "plQQQQQQQQQQQQQp",
        "OppppppppppppppO",
    ]
    t = ascii_tex(rows, OVEN, "oven_side")
    # heart vent: fill the gap at the top cleft with body pink, add a lit rim on the lower-right
    t.set(7, 5, OVEN["P"]); t.set(8, 5, OVEN["P"])
    for (x, y) in [(12, 6), (12, 7), (11, 8), (10, 9), (9, 10)]:
        if t.get(x, y) == OVEN["P"]:
            t.set(x, y, OVEN["l"])
    # subtle enamel sheen
    t.set(4, 3, OVEN["l"]); t.set(3, 4, OVEN["l"]); t.set(4, 4, OVEN["l"]); t.set(3, 3, OVEN["l"])
    return t


def oven_top() -> Tex:
    t = Tex()
    for y in range(16):
        for x in range(16):
            t.set(x, y, OVEN["c"])
    for y in range(2, 14):
        for x in range(2, 14):
            r = hash01(x, y, 11)
            if r < 0.10:
                t.set(x, y, OVEN["C"])
            elif r > 0.93:
                t.set(x, y, OVEN["e"])
    # pink rim
    for i in range(16):
        for (x, y) in [(i, 0), (i, 15), (0, i), (15, i)]:
            t.set(x, y, OVEN["p"])
        for (x, y) in [(i, 1), (1, i)]:
            if 0 < i < 15:
                t.set(x, y, OVEN["P"])
        for (x, y) in [(i, 14), (14, i)]:
            if 0 < i < 15:
                t.set(x, y, OVEN["Q"])
    for (x, y) in [(0, 0), (15, 0), (0, 15), (15, 15)]:
        t.set(x, y, OVEN["O"])
    t.set(1, 1, OVEN["l"])
    burner = check_sprite([
        ".kkkk.",
        "khKKKk",
        "kKkkKk",
        "kKkkKk",
        "kKKKrk",
        ".kkkk.",
    ], "burner")
    pal = dict(OVEN)
    pal["K"] = hx("6A5662"); pal["h"] = hx("8E7A88"); pal["k"] = hx("33242D"); pal["r"] = hx("4E3D48")
    t.stamp(burner, pal, 2, 2)
    t.stamp(burner, pal, 8, 8)
    # tiny cream-shadow below burners
    for x in range(3, 8):
        t.set(x, 8, OVEN["e"])
    for x in range(9, 14):
        t.set(x, 14, OVEN["Q"])
    # little heart decals in the free corners
    heart = [ "Q.Q", "QQQ", ".Q." ]
    t.stamp(heart, OVEN, 10, 3)
    t.stamp(heart, OVEN, 3, 10)
    return t


def oven_bottom() -> Tex:
    base, lite, dark, edge = hx("9C4A6A"), hx("B05C7C"), hx("86405C"), hx("6E3049")
    t = Tex()
    for y in range(16):
        for x in range(16):
            r = hash01(x, y, 23)
            t.set(x, y, lite if r < 0.12 else dark if r > 0.86 else base)
    for i in range(16):
        for (x, y) in [(i, 0), (i, 15), (0, i), (15, i)]:
            t.set(x, y, edge)
    foot = [C["choc_l"], C["choc"], C["choc"], C["choc_s"]]
    for (fx, fy) in [(1, 1), (13, 1), (1, 13), (13, 13)]:
        t.set(fx, fy, foot[0]); t.set(fx + 1, fy, foot[1])
        t.set(fx, fy + 1, foot[2]); t.set(fx + 1, fy + 1, foot[3])
    return t


# --------------------------------------------------------------------------- decoration station
DS = {
    "e": C["cream_s"], "E": C["cream_d"], "c": C["cream"], "C": C["cream_l"],
    "P": C["pink"], "Q": C["pink_m"], "p": C["pink_s"], "O": C["pink_d"], "l": C["pink_l"],
    "g": C["gold"], "G": C["gold_s"], "y": hx("FFF0B0"),
}


def _ds_frame(t: Tex):
    for i in range(16):
        t.set(i, 0, DS["e"]); t.set(0, i, DS["e"])
        t.set(i, 15, DS["E"]); t.set(15, i, DS["E"])
    for i in range(1, 15):
        t.set(i, 1, DS["C"]); t.set(1, i, DS["C"])
        t.set(i, 14, DS["c"]); t.set(14, i, DS["c"])
    t.set(0, 15, DS["e"]); t.set(15, 0, DS["e"])


def _pink_wood(t: Tex, x0, y0, x1, y1, seed):
    """Vertical-grain painted wood."""
    for x in range(x0, x1 + 1):
        streak = hash01(x, 0, seed)
        for y in range(y0, y1 + 1):
            r = hash01(x, y // 3, seed + 1)
            c = DS["P"]
            if streak > 0.6 and r < 0.55:
                c = DS["Q"]
            elif r > 0.92:
                c = DS["l"]
            t.set(x, y, c)


def deco_side() -> Tex:
    t = Tex()
    _ds_frame(t)
    _pink_wood(t, 2, 2, 13, 13, 5)
    for i in range(2, 14):
        t.set(i, 2, DS["p"]); t.set(2, i, DS["p"])
        t.set(i, 13, DS["l"]); t.set(13, i, DS["l"])
    t.set(2, 13, DS["Q"]); t.set(13, 2, DS["Q"])
    return t


def deco_front() -> Tex:
    t = Tex()
    _ds_frame(t)
    heart = check_sprite(["y.g", "ggG", ".G."], "knob_heart")
    for (y0, y1) in [(2, 7), (8, 13)]:
        _pink_wood(t, 2, y0, 13, y1, 9 + y0)
        for x in range(2, 14):
            t.set(x, y0, DS["l"])
            t.set(x, y1, DS["p"])
        for y in range(y0, y1 + 1):
            t.set(2, y, DS["l"])
            t.set(13, y, DS["p"])
        t.set(2, y1, DS["Q"]); t.set(13, y0, DS["Q"])
        t.stamp(heart, DS, 6, y0 + 2)
    return t


DST = {
    "e": C["cream_s"], "c": C["cream"], "C": C["cream_l"], "k": hx("F6E5C3"),
    "P": C["pink"], "Q": C["pink_m"], "p": C["pink_s"], "l": C["pink_l"], "O": C["pink_d"],
    "w": C["white"], "W": hx("F1E7EE"),
    "s": hx("E2E2EA"), "S": hx("A3A3B2"), "m": hx("C6C6D2"),
    "L": C["lav"], "V": C["lav_s"], "R": C["straw"], "r": C["straw_l"], "y": C["seed"], "g": C["leaf_l"],
    "b": C["crust"], "B": C["crust_s"],
}


def deco_top() -> Tex:
    rows = [
        "eeeeeeeeeeeeeeee",
        "ewcccccccccLLVce",   # piping bag lying on its side   cupcake, top view
        "eWlPccccccLlPQVe",   # (frosting at the open end,
        "ewPPlPccccLPrQVe",   #  silver nozzle pointing right)
        "eWPPPPQQScLQQpVe",
        "ewQQQpcycccVVVce",
        "eWQpccccrcccccce",
        "eccccccccccSSSce",   #                                whisk
        "eccrccccccSsmsSe",
        "ecccccycccScscSe",
        "ecLcccccgcScscSe",
        "eccccrcccccSsSce",
        "ecyccccLccccScce",
        "ecccrccccyccpcce",
        "ecgcccccccccOcce",
        "eeeeeeeeeeeeeeee",
    ]
    t = ascii_tex(rows, DST, "deco_top")
    # subtle counter speckle on plain cream pixels
    t.replace(DST["c"], DST["C"], region=(1, 1, 14, 14), prob=0.10, seed=41)
    t.replace(DST["c"], DST["k"], region=(1, 1, 14, 14), prob=0.07, seed=42)
    return t


# --------------------------------------------------------------------------- bakery counter
def counter_top() -> Tex:
    """Cream marble, seamless. One wandering diagonal vein (wraps) + a short branch."""
    t = Tex()
    base, light, shade = hx("FFF8F0"), hx("FFFFFF"), hx("F7EDE4")
    vein_d, vein, vein_l = hx("EBA6BC"), hx("F3C2D1"), hx("FADDE6")
    tau = 2 * math.pi
    for y in range(16):
        for x in range(16):
            v = (0.5 * math.sin(tau * (x + 2 * y) / 16.0)
                 + 0.3 * math.sin(tau * (3 * x - y) / 16.0 + 1.3)
                 + 0.5 * (hash01(x, y, 77) - 0.5))
            t.set(x, y, light if v > 0.6 else shade if v < -0.6 else base)

    def path(fy, xs):
        pts = [(x, fy(x)) for x in xs]
        out = []
        for (x0, y0), (x1, y1) in zip(pts, pts[1:]):
            out.append((x0, y0))
            step = 1 if y1 > y0 else -1
            for yy in range(y0 + step, y1, step):  # keep the line 8-connected
                out.append((x0 if abs(yy - y0) <= abs(yy - y1) else x1, yy))
        out.append(pts[-1])
        return out

    main = path(lambda x: x + 2 + int(round(1.8 * math.sin(tau * x / 16.0))), range(17))
    for i, (x, y) in enumerate(main):
        t.setw(x, y, vein_d if i % 5 else vein)
        if i % 4 == 1:
            t.setw(x + 1, y, vein_l)
    # short branch peeling off the main vein (stays inside the tile)
    for (x, y) in [(10, 13), (9, 14), (8, 14), (7, 15)]:
        t.setw(x, y, vein)
    for (x, y) in [(3, 9), (4, 10), (5, 10), (6, 11)]:
        t.setw(x, y, vein_l)
    return t


CT = {
    "C": C["cream_l"], "c": C["cream"], "e": C["cream_s"], "E": C["cream_d"],
    "P": C["pink"], "Q": C["pink_m"], "p": C["pink_s"], "l": C["pink_l"], "O": C["pink_d"],
    "g": C["gold"], "G": C["gold_s"], "y": hx("FFF0B0"),
}


def _counter_shell(t: Tex):
    for x in range(16):
        t.set(x, 0, CT["C"])
        t.set(x, 1, CT["e"])
        t.set(x, 2, CT["p"])
        t.set(x, 14, CT["c"])
        t.set(x, 15, CT["e"])


def _beadboard(t: Tex, x0, y0, x1, y1, seed):
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            m = x % 4
            c = [CT["Q"], CT["l"], CT["P"], CT["P"]][m]
            if m >= 2 and hash01(x, y, seed) < 0.08:
                c = CT["l"]
            t.set(x, y, c)


def counter_side() -> Tex:
    t = Tex()
    _beadboard(t, 0, 3, 15, 13, 3)
    _counter_shell(t)
    return t


def counter_front() -> Tex:
    t = Tex()
    for y in range(3, 14):
        for x in range(16):
            t.set(x, y, CT["P"])
    _counter_shell(t)
    # door with cream frame
    _beadboard(t, 3, 4, 12, 12, 8)
    for x in range(2, 14):
        t.set(x, 3, CT["C"])
        t.set(x, 13, CT["e"])
    for y in range(3, 14):
        t.set(2, y, CT["C"])
        t.set(13, y, CT["e"])
    t.set(13, 3, CT["c"]); t.set(2, 13, CT["c"])
    # inner recess shadow along the top of the door panel
    for x in range(3, 13):
        t.set(x, 4, CT["Q"])
    # gold handle + shadow
    t.set(11, 6, CT["y"]); t.set(11, 7, CT["g"]); t.set(11, 8, CT["g"]); t.set(11, 9, CT["G"])
    t.set(12, 7, CT["p"]); t.set(12, 8, CT["p"]); t.set(12, 9, CT["p"]); t.set(11, 10, CT["Q"])
    # body sides next to the door
    for y in range(3, 14):
        t.set(0, y, CT["l"]); t.set(1, y, CT["P"])
        t.set(14, y, CT["P"]); t.set(15, y, CT["Q"])
    return t


# --------------------------------------------------------------------------- strawberry cake
CAKE = {
    "w": C["white"], "W": hx("FFF7FA"), "s": hx("F2E2EA"), "S": hx("DEC6D2"),
    "r": C["straw_l"], "R": C["straw"], "x": C["straw_s"], "X": C["straw_d"],
    "G": C["leaf"], "g": C["leaf_l"], "y": C["seed"],
    "k": hx("FFD3DE"), "K": hx("F5B3C5"), "z": hx("E395AD"),
    "v": hx("FFE8C4"), "V": hx("F0CE9A"), "c": C["cream"],
    "j": C["straw"], "J": C["straw_s"], "h": C["straw_l"],
    "b": hx("F0AE88"), "B": hx("D98F68"), "n": hx("FFCBA6"), "N": hx("C27552"),
}


def cake_top() -> Tex:
    t = Tex()
    interior = [
        "WWWWWWWWWWWW",
        "WWWWWrRWWWWW",
        "WWrRWRxWrRWW",
        "WWRxWWWWRxWW",
        "WWWWWwsWWWWW",
        "WrRWwrRsWrRW",
        "WRxWwRxsWRxW",
        "WWWWWssWWWWW",
        "WWrRWWWWrRWW",
        "WWRxWrRWRxWW",
        "WWWWWRxWWWWW",
        "WWWWWWWWWWWW",
    ]
    t.stamp(interior, CAKE, 2, 2)
    t.replace(CAKE["W"], CAKE["s"], region=(2, 2, 13, 13), prob=0.10, seed=5)
    t.replace(CAKE["W"], CAKE["w"], region=(2, 2, 13, 13), prob=0.18, seed=6)
    # little green hulls pointing outwards so the red dots read as strawberries
    for (x, y) in [(7, 2), (8, 13), (2, 8), (13, 7), (3, 3), (12, 3), (3, 12), (12, 12)]:
        t.set(x, y, CAKE["G"])
    for (x, y) in [(8, 2), (7, 13), (2, 7), (13, 8)]:
        t.set(x, y, CAKE["g"])
    for i in range(1, 15):
        for (x, y) in [(i, 1), (i, 14), (1, i), (14, i)]:
            t.set(x, y, CAKE["w"] if (x + y) % 2 == 0 else CAKE["s"])
    for (x, y) in [(1, 1), (14, 1), (1, 14), (14, 14)]:
        t.set(x, y, CAKE["S"])
    return t


def cake_side() -> Tex:
    rows = [
        "wwwWwwwwWwwwwW",
        "WWsWWWWsWWWsWW",
        "kWkKkWWkKkWkWk",
        "KKkKKsKKKkKKKK",
        "jhjJjjhjjJjhjj",
        "vvVvvvvVvvvvVv",
        "VvvvVvvvvVvvvV",
        "cscccsccscccsc",
    ]
    t = Tex()
    t.stamp(rows, CAKE, 1, 8)
    return t


def cake_inner() -> Tex:
    rows = [
        "wwWwwwWwwwwWww",
        "KkKKzKKkKKKzKK",
        "KKKkKKzKKkKKKk",
        "wRhRwwRhRwRhRw",
        "kKKzKKKkKKzKKK",
        "KKkKKKzKKKKkKz",
        "RhRwwRhRwwwRhR",
        "bnbbBbbnbbbBbb",
    ]
    t = Tex()
    t.stamp(rows, CAKE, 1, 8)
    return t


def cake_bottom() -> Tex:
    t = Tex()
    for y in range(1, 15):
        for x in range(1, 15):
            r = hash01(x, y, 31)
            c = CAKE["b"]
            if r < 0.14:
                c = CAKE["n"]
            elif r > 0.88:
                c = CAKE["B"]
            t.set(x, y, c)
    for i in range(1, 15):
        for (x, y) in [(i, 1), (1, i)]:
            t.set(x, y, CAKE["B"])
        for (x, y) in [(i, 14), (14, i)]:
            t.set(x, y, CAKE["N"])
    return t


# --------------------------------------------------------------------------- cherry pie
PIE = {
    "L": hx("FFE7B5"), "C": hx("F5CF8E"), "c": C["crust"], "k": C["crust_s"], "K": C["crust_d"],
    "R": C["cherry"], "r": C["cherry_l"], "Q": C["cherry_d"], "q": hx("5C0614"), "h": hx("FF8A9A"),
    "s": hx("C9C9D3"), "S": hx("9C9CAA"),
}


def pie_top() -> Tex:
    t = Tex()
    P = PIE
    # filling interior (cols/rows 3..12)
    for y in range(3, 13):
        for x in range(3, 13):
            t.set(x, y, P["R"])
    # windows: 2x2 shading
    for wy in (3, 7, 11):
        for wx in (3, 7, 11):
            t.set(wx, wy, P["r"]); t.set(wx + 1, wy, P["R"])
            t.set(wx, wy + 1, P["R"]); t.set(wx + 1, wy + 1, P["Q"])
    t.set(7, 7, P["h"]); t.set(11, 3, P["h"]); t.set(3, 11, P["h"])
    # lattice strips
    vert_over = {(5, 5): True, (9, 5): False, (5, 9): False, (9, 9): True}

    def vstrip(x):
        for y in range(3, 13):
            t.set(x, y, P["C"]); t.set(x + 1, y, P["c"])

    def hstrip(y):
        for x in range(3, 13):
            t.set(x, y, P["C"]); t.set(x, y + 1, P["c"])

    for x in (5, 9):
        vstrip(x)
    for y in (5, 9):
        hstrip(y)
    for (x, y), v_on_top in vert_over.items():
        if v_on_top:
            for yy in (y, y + 1):
                t.set(x, yy, P["C"]); t.set(x + 1, yy, P["c"])
            t.set(x - 1, y, P["k"]); t.set(x - 1, y + 1, P["k"])
            t.set(x + 2, y, P["k"]); t.set(x + 2, y + 1, P["k"])
        else:
            for xx in (x, x + 1):
                t.set(xx, y, P["C"]); t.set(xx, y + 1, P["c"])
            t.set(x, y - 1, P["k"]); t.set(x + 1, y - 1, P["k"])
            t.set(x, y + 2, P["k"]); t.set(x + 1, y + 2, P["k"])
    # strip highlights / cast shadows onto filling
    for y in range(3, 13):
        for x in (7, 11):
            if t.get(x, y) in (P["R"], P["r"]):
                t.set(x, y, P["Q"])
    # rim: ring 1 crust, ring 0 crimped
    for i in range(1, 15):
        for (x, y) in [(i, 2), (2, i), (i, 13), (13, i)]:
            if 2 <= x <= 13 and 2 <= y <= 13:
                t.set(x, y, P["c"])
        for (x, y) in [(i, 1), (1, i), (i, 14), (14, i)]:
            t.set(x, y, P["C"] if i % 2 == 1 else P["k"])
    for i in range(2, 14):
        t.set(i, 2, P["L"] if i % 2 == 0 else P["C"])
        t.set(2, i, P["C"])
        t.set(i, 13, P["k"])
        t.set(13, i, P["k"])
    for (x, y) in [(1, 1), (14, 1), (1, 14), (14, 14)]:
        t.set(x, y, P["K"])
    return t


def pie_side() -> Tex:
    rows = [
        "CckCckCckCckCc",
        "cCcccCcccCcccC",
        "kkckkkkckkkkck",
        "KKKKKKKKKKKKKK",
    ]
    return Tex().stamp(rows, PIE, 1, 12)


def pie_inner() -> Tex:
    rows = [
        "CcCCcCCcCCcCCc",
        "RrQRRqRhRQRrRR",
        "QRqRhRQRRqRQrR",
        "kkKkkkKkkkkKkk",
    ]
    return Tex().stamp(rows, PIE, 1, 12)


def pie_bottom() -> Tex:
    t = Tex()
    P = PIE
    for y in range(1, 15):
        for x in range(1, 15):
            r = hash01(x, y, 57)
            c = P["k"]
            if r < 0.16:
                c = P["c"]
            elif r > 0.94:
                c = P["K"]
            t.set(x, y, c)
    for i in range(1, 15):
        for (x, y) in [(i, 1), (1, i)]:
            t.set(x, y, P["s"])
        for (x, y) in [(i, 14), (14, i)]:
            t.set(x, y, P["S"])
    return t


# --------------------------------------------------------------------------- mixing bowl
def bowl_side() -> Tex:
    t = Tex()
    P, Q, l, w, W, p = C["pink"], C["pink_m"], C["pink_l"], C["white"], hx("FFF3F8"), C["pink_s"]
    for y in range(16):
        for x in range(16):
            c = P
            if y % 4 == 0 and x % 4 == 1:
                c = w
            elif y % 4 == 2 and x % 4 == 3:
                c = w
            t.set(x, y, c)
    for x in range(16):
        t.set(x, 10, W)          # glazed rim
        t.set(x, 15, Q)          # base shadow
    return t


def bowl_inside() -> Tex:
    t = Tex()
    base, lt, sh = hx("FFF8EA"), hx("FFFFFF"), hx("F8EEDB")
    for y in range(16):
        for x in range(16):
            r = hash01(x, y, 91)
            t.set(x, y, lt if r < 0.10 else sh if r > 0.92 else base)
    return t


def bowl_bottom() -> Tex:
    t = Tex()
    for y in range(16):
        for x in range(16):
            t.set(x, y, C["pink_m"] if hash01(x, y, 13) > 0.1 else C["pink"])
    for i in range(3, 13):
        for (x, y) in [(i, 3), (3, i), (i, 12), (12, i)]:
            t.set(x, y, C["pink_s"])
    for y in range(4, 12):
        for x in range(4, 12):
            t.set(x, y, C["pink"] if hash01(x, y, 14) > 0.12 else C["pink_l"])
    # maker's mark: tiny embossed heart
    t.stamp(["l.l", "llQ", ".Q."], {"l": C["pink_l"], "Q": C["pink_m"]}, 6, 6)
    return t


# --------------------------------------------------------------------------- cake display
def display_glass() -> Tex:
    t = Tex()
    for y in range(16):
        for x in range(16):
            a = int(round(88 - (x + y) * 1.0))
            t.set(x, y, (232, 244, 252, max(60, a)))
    # glints (diagonal, "/" direction)
    for i in range(7):
        t.set(2 + i, 9 - i, (255, 255, 255, 195))
        if i < 6:
            t.set(3 + i, 9 - i, (255, 255, 255, 150))
    for i in range(4):
        t.set(2 + i, 12 - i, (255, 255, 255, 160))
    for i in range(4):
        t.set(10 + i, 13 - i, (255, 255, 255, 170))
    gl, gm, gs, gc = hx("FFE89A"), hx("F2C94C"), hx("D4A93A"), hx("FFF6D2")
    for i in range(16):
        t.set(i, 0, gl); t.set(0, i, gl)
        t.set(i, 15, gs); t.set(15, i, gs)
    for (x, y) in [(0, 0), (15, 0), (0, 15), (15, 15)]:
        t.set(x, y, gc)
    t.set(1, 0, gc); t.set(0, 1, gc)
    return t


def display_base() -> Tex:
    t = Tex()
    pink, pl, pm = C["pink"], C["pink_l"], C["pink_m"]
    gold, golds = C["gold"], C["gold_s"]
    for y in range(16):
        for x in range(16):
            r = hash01(x, y, 61)
            t.set(x, y, pl if r < 0.10 else pink)
    # pink marble vein
    for (x, y) in [(2, 12), (3, 11), (4, 11), (5, 10), (11, 3), (12, 3), (13, 2), (10, 4)]:
        t.set(x, y, pm)
    for i in range(16):
        t.set(i, 14, pm); t.set(i, 1, pm); t.set(1, i, pm); t.set(14, i, pm)
        t.set(i, 0, gold); t.set(0, i, gold); t.set(i, 15, gold); t.set(15, i, gold)
    for i in range(0, 16, 3):
        t.set(i, 15, golds)
    for i in range(1, 15):
        t.set(i, 14, pm)
    # lace doily: scalloped white edge, ring of pierced holes, cream band, white centre
    cw, cc, cs = C["white"], C["cream_l"], C["cream"]
    cx = cy = 7.5
    for y in range(2, 14):
        for x in range(2, 14):
            dx, dy = x - cx, y - cy
            d = math.hypot(dx, dy)
            ang = math.atan2(dy, dx)
            edge = 5.75 + 0.5 * math.cos(ang * 12)
            if d > edge:
                continue
            seg = int((ang + math.pi) / (2 * math.pi) * 16) % 2
            if d > 4.7:
                c = cw
            elif d > 3.8:
                c = pink if seg == 0 else cs
            elif d > 2.3:
                c = cc
            elif d > 1.5:
                c = cs
            else:
                c = cw
            t.set(x, y, c)
    return t


# --------------------------------------------------------------------------- cupcake stand
def stand_plate() -> Tex:
    t = Tex()
    w, W, ws = C["white"], hx("F7F2F6"), C["porc"]
    rim, rim_l, rim_d = C["pink_m"], C["pink"], C["pink_s"]
    for y in range(16):
        for x in range(16):
            t.set(x, y, w)
    # well shading
    for i in range(3, 13):
        t.set(i, 3, ws); t.set(3, i, ws)
        t.set(i, 12, W); t.set(12, i, W)
    for i in range(16):
        t.set(i, 0, rim); t.set(0, i, rim); t.set(i, 15, rim_d); t.set(15, i, rim_d)
    for i in range(1, 15):
        t.set(i, 1, rim_l); t.set(1, i, rim_l); t.set(i, 14, rim); t.set(14, i, rim)
    # scallops (2px pink bumps with 1px gaps)
    for i in range(2, 14):
        if i % 3 != 1:
            t.set(i, 2, rim_l); t.set(2, i, rim_l); t.set(i, 13, rim_l); t.set(13, i, rim_l)
    t.set(0, 15, rim); t.set(15, 0, rim)
    # rounder corners
    for (x, y) in [(1, 1), (14, 1), (1, 14), (14, 14)]:
        t.set(x, y, rim)
    for (x, y) in [(3, 3), (12, 3), (3, 12), (12, 12)]:
        t.set(x, y, w)
    return t


def stand_pole() -> Tex:
    t = Tex()
    cols = [hx("C77F6A"), hx("E8A58C"), hx("F7C6AE"), hx("FFE6D8")]
    for y in range(16):
        for x in range(16):
            c = cols[x % 4]
            t.set(x, y, c)
    for x in range(16):
        if x % 4 == 0:
            for y in range(16):
                if hash01(x, y // 2, 3) < 0.25:
                    t.set(x, y, hx("B06A58"))
    return t


# --------------------------------------------------------------------------- dessert plate
def dessert_plate() -> Tex:
    t = Tex()
    w, w2, ws = C["white"], hx("F8F4F8"), hx("EEE6EE")
    for y in range(16):
        for x in range(16):
            t.set(x, y, w)
    for i in range(16):
        t.set(i, 0, C["pink_m"]); t.set(0, i, C["pink_m"])
        t.set(i, 15, C["pink_s"]); t.set(15, i, C["pink_s"])
    for i in range(3, 13):  # pink band, corners skipped so it reads rounded
        t.set(i, 2, C["pink"]); t.set(2, i, C["pink"]); t.set(i, 13, C["pink"]); t.set(13, i, C["pink"])
    for i in range(5, 11):  # shallow well: shaded inner wall top-left, lit bottom-right
        t.set(i, 4, ws); t.set(4, i, ws)
        t.set(i, 11, w2); t.set(11, i, w2)
    for y in range(5, 11):
        for x in range(5, 11):
            t.set(x, y, w if (x + y) > 11 else w2)
    for (x, y) in [(1, 1), (14, 1), (1, 14), (14, 14)]:
        t.set(x, y, C["pink_m"] if (x, y) != (14, 14) else C["pink_s"])
    t.set(2, 1, hx("FFFFFF"))
    for (x, y) in [(0, 0), (15, 0), (0, 15), (15, 15)]:
        t.set(x, y, CLEAR)
    return t


# --------------------------------------------------------------------------- glass jar
def jar_side() -> Tex:
    t = Tex()
    glass = (226, 242, 252)
    for y in range(6, 16):
        for x in range(4, 12):
            a = 82
            if x in (4, 11):
                a = 150
            t.set(x, y, (*glass, a))
    for x in range(4, 12):
        t.set(x, 6, (238, 248, 255, 175))
        t.set(x, 15, (210, 230, 245, 170))
    for y in range(9, 15):
        t.set(5, y, (255, 255, 255, 190))
    t.set(6, 10, (255, 255, 255, 120)); t.set(6, 11, (255, 255, 255, 120))
    t.set(10, 12, (255, 255, 255, 110))
    # ribbon
    for x in range(4, 12):
        t.set(x, 7, C["pink_m"])
        t.set(x, 8, C["pink_s"])
    t.set(4, 7, C["pink"]); t.set(5, 7, C["pink_l"])
    t.set(7, 7, C["pink_d"]); t.set(8, 7, C["pink_d"])
    t.set(7, 9, C["pink_s"]); t.set(8, 9, C["pink_m"])
    return t


def jar_top() -> Tex:
    t = Tex()
    for y in range(4, 12):
        for x in range(4, 12):
            ring = min(x - 4, y - 4, 11 - x, 11 - y)
            if ring == 0:
                c = (214, 234, 248, 255)
                if x == 4 or y == 4:
                    c = (240, 250, 255, 255)
            elif ring == 1:
                c = (225, 242, 252, 150)
            else:
                c = (232, 246, 254, 70)
            t.set(x, y, c)
    for (x, y) in [(4, 4), (11, 4), (4, 11), (11, 11)]:
        t.set(x, y, (196, 220, 238, 255))
    t.set(5, 5, (255, 255, 255, 200))
    return t


def jar_lid() -> Tex:
    t = Tex()
    w, m, d = C["white"], C["pink"], hx("EE8FB0")
    for y in range(16):
        for x in range(16):
            a = (x // 2) % 2 == 0
            b = (y // 2) % 2 == 0
            c = d if (a and b) else m if (a or b) else w
            t.set(x, y, c)
    return t


# --------------------------------------------------------------------------- tea set
def tea_tray() -> Tex:
    t = Tex()
    pl, pk, w = C["pink_l"], C["pink"], C["white"]
    for y in range(16):
        for x in range(16):
            t.set(x, y, pl)
    for i in range(16):
        t.set(i, 0, C["gold"]); t.set(0, i, C["gold"])
        t.set(i, 15, C["gold_s"]); t.set(15, i, C["gold_s"])
    for i in range(1, 15):
        t.set(i, 1, C["gold_s"]); t.set(1, i, C["gold_s"])
        t.set(i, 14, C["gold_l"]); t.set(14, i, C["gold_l"])
    t.set(0, 15, C["gold"]); t.set(15, 0, C["gold"])
    # lace ring
    for i in range(2, 14):
        for (x, y) in [(i, 2), (2, i), (i, 13), (13, i)]:
            t.set(x, y, w if i % 2 == 0 else pk)
        for (x, y) in [(i, 3), (3, i), (i, 12), (12, i)]:
            if 3 <= x <= 12 and 3 <= y <= 12:
                t.set(x, y, w if i % 4 == 1 else pl)
    for (x, y) in [(7, 5), (8, 5), (5, 7), (5, 8), (10, 7), (10, 8), (7, 10), (8, 10)]:
        t.set(x, y, w)
    for (x, y) in [(6, 6), (9, 6), (6, 9), (9, 9)]:
        t.set(x, y, pk)
    return t


def tea_porcelain() -> Tex:
    t = Tex(fill=C["white"])
    pal = {"P": C["pink"], "Q": C["pink_m"], "p": C["pink_s"], "l": C["pink_l"],
           "G": C["leaf"], "g": C["leaf_l"], "d": C["leaf_s"]}
    rose = ["lQP", "QpQ", "PQQ"]
    t.stamp(rose, pal, 2, 2, wrap=True)
    for (x, y, k) in [(5, 3, "g"), (5, 4, "d"), (1, 5, "G"), (2, 5, "d")]:
        t.setw(x, y, pal[k])
    t.stamp(rose, pal, 10, 10, wrap=True)
    for (x, y, k) in [(13, 11, "g"), (13, 12, "d"), (9, 13, "G"), (10, 13, "d")]:
        t.setw(x, y, pal[k])
    for (x, y, k) in [(12, 3, "Q"), (11, 4, "G"), (13, 2, "l"), (4, 12, "Q"), (5, 13, "G"), (3, 11, "l")]:
        t.setw(x, y, pal[k])
    return t


def tea_tea() -> Tex:
    """Calm liquid surface: flat amber-rose with thin ripple lines and two glints."""
    base, lt, sh, hi = hx("D58462"), hx("E39C7C"), hx("C37254"), hx("FFE6D6")
    t = Tex(fill=base)
    # thin, broken ripple lines (tileable: slope-1 diagonals wrap cleanly)
    for k, (off, col) in enumerate([(0, lt), (5, sh), (10, lt)]):
        for x in range(16):
            if hash01(x, k, 29) < 0.7:
                y = (x + off + (1 if (x // 4) % 2 else 0)) % 16
                t.setw(x, y, col)
    for (x, y) in [(3, 3), (4, 3), (3, 4)]:
        t.set(x, y, hi)
    t.set(5, 3, lt); t.set(11, 11, hi); t.set(12, 11, lt)
    return t


# --------------------------------------------------------------------------- wood materials
def warm_wood(vertical=False, seed=0, pal=None) -> Tex:
    base, light, shade, dark = pal or (C["oak"], C["oak_l"], C["oak_s"], C["oak_d"])
    t = Tex()
    row_tone = [0, 0, 1, 0, 2, 0, 0, 1, 0, 0, 2, 0, 1, 0, 0, 2]
    tones = [base, light, shade]
    for a in range(16):          # a = across-grain index
        for b in range(16):      # b = along-grain index
            c = tones[row_tone[a]]
            r = hash01(a, b // 3, seed)
            if r < 0.14:
                c = shade if c != shade else dark
            elif r > 0.9 and c == base:
                c = light
            x, y = (a, b) if vertical else (b, a)
            t.set(x, y, c)
    # plank seam every 8
    for b in range(16):
        x, y = (7, b) if vertical else (b, 7)
        if hash01(b, 5, seed + 3) < 0.85:
            t.set(x, y, dark)
    return t


# --------------------------------------------------------------------------- bakery sign
def sign_front() -> Tex:
    t = warm_wood(seed=4)
    SG = {
        "Q": C["pink_m"], "p": C["pink_s"], "w": C["white"], "W": hx("F2E6EC"),
        "c": C["cream"], "C": C["cream_l"], "e": C["cream_s"],
        "f": C["oak_s"], "F": C["oak_d"], "o": C["oak"],
        "P": C["pink"], "l": C["pink_l"], "O": C["pink_d"],
        "L": C["lav"], "V": C["lav_s"], "R": C["straw"], "r": C["straw_l"],
    }
    rows = [
        "QQwwQQwwQQwwQQww",
        "ppWWppWWppWWppWW",
        "fpCcpccpccpccpcf",
        "fccccccrRccccccf",
        "fccccclPPQcccccf",
        "frcrcclPPPQcrcrf",
        "fRRRcQPQPQpcRRRf",
        "fcRccLVLVLVccRcf",
        "fcccccLVLVcccccf",
        "FFFFFFFFFFFFFFFF",
    ]
    t.stamp(rows, SG, 0, 3)
    return t


def sign_back() -> Tex:
    return warm_wood(seed=4)


# --------------------------------------------------------------------------- decorative menu
def menu_frame() -> Tex:
    return warm_wood(vertical=True, seed=8,
                     pal=(hx("E2C38E"), hx("F0D8AC"), hx("CDA86F"), hx("AE8A55")))


def menu_front() -> Tex:
    t = menu_frame()
    MN = {
        "f": hx("F0D8AC"), "F": hx("CDA86F"), "o": hx("E2C38E"),
        "k": hx("2F3D35"), "K": hx("3A4A40"), "j": hx("26312B"),
        "w": hx("F4F4EC"), "W": hx("BFC6BC"), "p": hx("FFB7D5"), "P": hx("E68AB2"),
        "y": hx("FFE58A"), "Y": hx("CDBA6E"), "L": hx("D9C6F5"), "c": hx("C9A06A"),
    }
    rows = [
        ".ffffffffffffff.",
        ".fkkkkkkkkkkkkF.",
        ".fkkwWwkwwWwkkF.",   # title squiggle
        ".fkkkkkkkkkkkkF.",
        ".fkppkpPpkkpkpF.",   # pink line          + chalk heart
        ".fkkkkkkkkkpppF.",
        ".fkwWkwwkWkkpkF.",   # white line
        ".fkkkkkkkkkkkkF.",
        ".fkyYykyykkkpkF.",   # yellow line        + cupcake doodle
        ".fkkkkkkkkkwwWF.",
        ".fkWwkWwkkkyYyF.",   # faint white line
        ".FFFFFFFFFFFFFF.",
    ]
    t.stamp(rows, MN, 0, 0)
    # chalk dust on the board
    t.replace(MN["k"], MN["K"], region=(2, 1, 13, 10), prob=0.10, seed=3)
    t.replace(MN["k"], MN["j"], region=(2, 1, 13, 10), prob=0.08, seed=4)
    return t


# --------------------------------------------------------------------------- registry
def build_all():
    tex = {}
    for s in range(4):
        tex[f"strawberry_crop_stage{s}"] = strawberry_crop(s)
        tex[f"vanilla_crop_stage{s}"] = vanilla_crop(s)
        tex[f"lavender_crop_stage{s}"] = lavender_crop(s)
        tex[f"peach_tree_stage{s}"] = peach_tree(s)
        tex[f"cherry_tree_stage{s}"] = cherry_tree(s)
    tex["wild_strawberries"] = wild_strawberries()
    tex["wild_lavender"] = wild_lavender()
    tex["wild_vanilla"] = wild_vanilla()

    tex["bakery_oven_front"] = oven_front(False)
    tex["bakery_oven_side"] = oven_side()
    tex["bakery_oven_top"] = oven_top()
    tex["bakery_oven_bottom"] = oven_bottom()

    tex["decoration_station_top"] = deco_top()
    tex["decoration_station_side"] = deco_side()
    tex["decoration_station_front"] = deco_front()

    tex["bakery_counter_top"] = counter_top()
    tex["bakery_counter_side"] = counter_side()
    tex["bakery_counter_front"] = counter_front()

    tex["strawberry_cake_top"] = cake_top()
    tex["strawberry_cake_side"] = cake_side()
    tex["strawberry_cake_inner"] = cake_inner()
    tex["strawberry_cake_bottom"] = cake_bottom()

    tex["cherry_pie_top"] = pie_top()
    tex["cherry_pie_side"] = pie_side()
    tex["cherry_pie_inner"] = pie_inner()
    tex["cherry_pie_bottom"] = pie_bottom()

    tex["mixing_bowl_side"] = bowl_side()
    tex["mixing_bowl_inside"] = bowl_inside()
    tex["mixing_bowl_bottom"] = bowl_bottom()

    tex["cake_display_glass"] = display_glass()
    tex["cake_display_base"] = display_base()

    tex["cupcake_stand_plate"] = stand_plate()
    tex["cupcake_stand_pole"] = stand_pole()

    tex["dessert_plate"] = dessert_plate()

    tex["glass_jar_side"] = jar_side()
    tex["glass_jar_top"] = jar_top()
    tex["glass_jar_lid"] = jar_lid()

    tex["tea_set_tray"] = tea_tray()
    tex["tea_set_porcelain"] = tea_porcelain()
    tex["tea_set_tea"] = tea_tea()

    tex["bakery_sign_front"] = sign_front()
    tex["bakery_sign_back"] = sign_back()

    tex["decorative_menu_front"] = menu_front()
    tex["decorative_menu_frame"] = menu_frame()
    return tex


def build_oven_animation() -> Image.Image:
    strip = Image.new("RGBA", (16, 64))
    for f in range(4):
        strip.paste(oven_front(True, f).image(), (0, f * 16))
    return strip


def main():
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    tex = build_all()
    for name, t in tex.items():
        t.image().save(OUT_DIR / f"{name}.png")
    build_oven_animation().save(OUT_DIR / "bakery_oven_front_on.png")
    with open(OUT_DIR / "bakery_oven_front_on.png.mcmeta", "w", encoding="utf-8") as fh:
        json.dump({"animation": {"frametime": 4}}, fh, indent=2)
        fh.write("\n")
    print(f"wrote {len(tex) + 1} textures (+1 .mcmeta) to {OUT_DIR}")


if __name__ == "__main__":
    main()

#!/usr/bin/env python3
"""
Sweet Treats - item texture generator (16x16 pixel art, vanilla-style).

Run from the project root (or anywhere):
    python tools/textures/gen_item_textures.py
    python tools/textures/gen_item_textures.py --sheet out.png [--only a,b,c]

Writes RGBA PNGs to src/main/resources/assets/sweettreats/textures/item/

Sprites are hand-authored 16x16 ASCII grids, one char per pixel:
  '.'  transparent
  'o'  auto-outline: takes the outline colour of the colour family of the
       neighbouring filled pixel (a darker, saturated shade, like vanilla food)
Every other char is looked up in the sprite palette.  Palette values are
'family.tone' references into FAM (e.g. 'red.l') or literal '#RRGGBB[AA]'.
A palette may define 'o' itself to force one explicit outline colour.
Deterministic: re-running produces byte-identical output.
"""
from __future__ import annotations

import argparse
import os
import sys
from collections import Counter, deque

from PIL import Image, ImageDraw, ImageFont

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.normpath(os.path.join(HERE, "..", ".."))
OUT_DIR = os.path.join(ROOT, "src", "main", "resources", "assets", "sweettreats", "textures", "item")

# --------------------------------------------------------------------------- colour families
# tones: h=highlight, l=light, b=base, m=mid, d=shade, x=deep shade, o=outline
FAM = {
    "pink":    dict(h="#FFF4F8", l="#FFE0EC", b="#FFC4D9", m="#F59AB9", d="#D9739A", o="#A64B6E"),
    "hot":     dict(h="#FFB8D2", l="#FF8FB8", b="#F26A9E", d="#D04880", o="#8E2A58"),
    "cream":   dict(h="#FFFFFF", l="#FFFBEF", b="#FFF3D6", d="#EBD3A5", m="#D9BC8A", o="#B8956A"),
    "red":     dict(h="#FFB3BE", l="#FF6B7E", b="#E8384F", d="#B5203A", o="#7A1428"),
    "seed":    dict(l="#FFF4C2", b="#FFE58A", d="#E3BF55", o="#9A7A2A"),
    "lav":     dict(h="#F5EFFD", l="#E3D4FA", b="#C3A6EB", d="#9B79D1", x="#8262BC", o="#6A4C9C"),
    "choc":    dict(h="#C08A6A", l="#A0674B", b="#7B4A35", d="#5A3324", o="#3B2016"),
    "green":   dict(h="#C6EFA8", l="#9BDB7B", b="#6FBF5A", d="#4A9440", o="#2E6B2E"),
    "peach":   dict(h="#FFE8D2", l="#FFD0A8", b="#FFB38A", d="#F08C5E", r="#F7837A", x="#E0644E", o="#B5562E"),
    "cherry":  dict(h="#FFB0BC", l="#F0405A", b="#C8102E", d="#8A0A1F", o="#5C0614"),
    "gold":    dict(h="#FBE7BC", l="#F2CC8A", b="#E6B36E", d="#C98D45", x="#AE7536", o="#86521F"),
    "white":   dict(h="#FFFFFF", b="#F2EEF2", d="#D8CCD8", x="#BFAFC2", o="#8C7A8E"),
    "glass":   dict(h="#FFFFFF", l="#F4FAFFD2", b="#DCEEFABE", d="#B4D2EAD2", o="#7C9AB8"),
    "yellow":  dict(h="#FFFBE0", l="#FFF2A8", b="#FFE27A", d="#F2C744", o="#B08414"),
    "butter":  dict(h="#FFFFF0", l="#FFF8CC", b="#FFEFA0", d="#F5D873", o="#B8963A"),
    "cinn":    dict(h="#E09A66", l="#C9773F", b="#A6562D", d="#7E3D1E", o="#4E2210"),
    "honey":   dict(h="#FFF2B8", l="#FFD860", b="#F5B42E", d="#D98E14", o="#8F560A"),
    "blue":    dict(l="#D0F0FF", b="#8FD3FF", d="#5AAFE6", o="#2E6EA6"),
    "mint":    dict(l="#D2F7E4", b="#98E6BE", d="#62C496", o="#2E8A5E"),
    "lemon":   dict(l="#FFF8C0", b="#FFE45C", d="#F2C21E", o="#B08A0A"),
    "orange":  dict(l="#FFC070", b="#FF9A3C", d="#E07A1E", o="#9A4A0A"),
    "batter":  dict(h="#FFF9DE", l="#FFF0BE", b="#FFE29A", d="#F2C870", o="#C2964A"),
    "dough":   dict(h="#FBEEDA", l="#F2DFC0", b="#E6C89C", d="#CFAA78", o="#93703F"),
    "pearl":   dict(h="#A68474", l="#5E4238", b="#3C2620", d="#261612", o="#140A08"),
    "tea":     dict(h="#FFD89A", l="#FFC56E", b="#F0A040", d="#D07E22", o="#8A4E10"),
    "milktea": dict(h="#F7E4CC", l="#EDD3B0", b="#DDB98E", d="#C49A6A", o="#8A6440"),
    "rose":    dict(h="#FFD6E0", l="#FF9DB5", b="#F06A8E", d="#C94770", o="#8A2848"),
    "bean":    dict(h="#9A6A4E", l="#74472F", b="#563220", d="#3E2216", o="#22120A"),
    "pit":     dict(h="#D9A27A", l="#C08055", b="#9C6040", d="#7A4630", o="#4E2A1A"),
    "cpit":    dict(h="#FFFAEE", l="#F7EAD0", b="#E8D4AE", d="#CDB287", o="#8C7450"),
    "burlap":  dict(h="#F7EBD0", l="#EBD9B4", b="#D9C08E", d="#BFA06C", o="#7E6440"),
    "lavseed": dict(l="#BCA9D0", b="#9480AD", d="#6F5C8A", o="#45385C"),
    "steam":   dict(b="#FFFFFF", d="#E6E0EE", o="#B0A8C0"),
    "star":    dict(h="#FFFDE6", b="#FFF0A0", d="#FFD84A", o="#C9A227"),
}


def rgba(h: str):
    h = h.lstrip("#")
    a = int(h[6:8], 16) if len(h) == 8 else 255
    return (int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), a)


def darker(c, k: float = 0.5):
    return (int(c[0] * k), int(c[1] * k), int(c[2] * k), 255)


def resolve(spec: str):
    """palette spec -> (fill rgba, outline rgba)"""
    if spec.startswith("#"):
        c = rgba(spec)
        return c, darker(c)
    fam, tone = spec.split(".")
    f = FAM[fam]
    return rgba(f[tone]), rgba(f["o"])


def lum(c):
    return 0.299 * c[0] + 0.587 * c[1] + 0.114 * c[2]


# --------------------------------------------------------------------------- sprite registry
SPRITES: dict[str, tuple[list[str], dict[str, str]]] = {}


def rows_of(art: str) -> list[str]:
    return [ln.strip() for ln in art.strip().splitlines()]


def S(name: str, pal: dict, art):
    """Register a sprite.  art: ASCII string or list of rows."""
    rows = rows_of(art) if isinstance(art, str) else list(art)
    if name in SPRITES:
        raise ValueError(f"duplicate sprite {name}")
    SPRITES[name] = (rows, pal)


def ov(base, patch: str) -> list[str]:
    """Overlay patch art on base art; '_' in the patch keeps the base pixel."""
    b = rows_of(base) if isinstance(base, str) else list(base)
    p = rows_of(patch)
    out = []
    for y in range(16):
        out.append("".join(bc if pc == "_" else pc for bc, pc in zip(b[y], p[y])))
    return out


def P(*dicts, **kw) -> dict:
    """Merge palettes."""
    out = {}
    for d in dicts:
        out.update(d)
    out.update(kw)
    return out


N4 = ((0, -1), (-1, 0), (1, 0), (0, 1))
N8 = N4 + ((-1, -1), (1, -1), (-1, 1), (1, 1))


def render(name: str) -> Image.Image:
    rows, pal = SPRITES[name]
    errs = []
    if len(rows) != 16:
        errs.append(f"{len(rows)} rows")
    for y, r in enumerate(rows):
        if len(r) != 16:
            errs.append(f"row {y} has {len(r)} chars: '{r}'")
        for ch in r:
            if ch not in pal and ch not in ".o":
                errs.append(f"row {y}: unknown char '{ch}'")
    if errs:
        raise ValueError(f"sprite '{name}': " + "; ".join(errs))

    auto = "o" not in pal
    res = {k: resolve(v) for k, v in pal.items()}
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    outl = [[None] * 16 for _ in range(16)]
    pending = []
    for y in range(16):
        for x in range(16):
            ch = rows[y][x]
            if ch == ".":
                continue
            if ch == "o" and auto:
                pending.append((x, y))
                continue
            fill, ol = res[ch]
            px[x, y] = fill
            outl[y][x] = ol
    for x, y in pending:
        for nb in (N4, N8):
            cands = Counter()
            for dx, dy in nb:
                nx, ny = x + dx, y + dy
                if 0 <= nx < 16 and 0 <= ny < 16 and outl[ny][nx] is not None:
                    cands[outl[ny][nx]] += 1
            if cands:
                break
        if not cands:
            raise ValueError(f"sprite '{name}': isolated outline pixel at {x},{y}")
        best = max(cands.items(), key=lambda kv: (kv[1], -lum(kv[0])))[0]
        px[x, y] = best
    return img


# --------------------------------------------------------------------------- QA helpers
def contact_sheet(names, path, scale=8, cols=8):
    cell_w, label_h, pad = 16 * scale, 14, 10
    rows_n = (len(names) + cols - 1) // cols
    W = cols * (cell_w + pad) + pad
    H = rows_n * (cell_w + label_h + pad) + pad
    sheet = Image.new("RGBA", (W, H), (128, 128, 128, 255))
    d = ImageDraw.Draw(sheet)
    font = ImageFont.load_default()
    for i, n in enumerate(names):
        cx = pad + (i % cols) * (cell_w + pad)
        cy = pad + (i // cols) * (cell_w + label_h + pad)
        d.rectangle([cx - 1, cy - 1, cx + cell_w, cy + cell_w], outline=(100, 100, 100, 255))
        img = render(n).resize((cell_w, cell_w), Image.NEAREST)
        sheet.alpha_composite(img, (cx, cy))
        d.text((cx, cy + cell_w + 2), n, fill=(255, 255, 255, 255), font=font)
    sheet.save(path)


def write_all(names):
    os.makedirs(OUT_DIR, exist_ok=True)
    for n in names:
        render(n).save(os.path.join(OUT_DIR, n + ".png"))


def verify(names):
    ok = True
    for n in names:
        p = os.path.join(OUT_DIR, n + ".png")
        if not os.path.isfile(p):
            print("MISSING", p)
            ok = False
            continue
        im = Image.open(p)
        if im.size != (16, 16) or im.mode != "RGBA":
            print("BAD FORMAT", n, im.size, im.mode)
            ok = False
        alphas = set(im.getchannel("A").tobytes())
        odd = sorted(a for a in alphas if a not in (0, 255))
        if any(a < 170 for a in odd):
            print("ODD ALPHA", n, odd)
            ok = False
    return ok


# =========================================================================== SPRITES
# ---------------------------------------------------------------- crops & seeds
LEAF = dict(G="green.b", g="green.l", k="green.d", K="green.o")

S("strawberry", P(LEAF, r="red.b", l="red.l", d="red.d", h="red.h", s="seed.b", S="seed.d"), """
    ................
    ........oo......
    .......oGo......
    ...oo.ooGoo.oo..
    ..ogGooggGGoGGo.
    .oggGGGgGGGGGko.
    .okGkGGkGGkGkko.
    .orrkrrkrrkrrdo.
    .olhrrrsrrrsrdo.
    .ohlsrrrrrrrrdo.
    ..olrrrsrrsrdo..
    ..orsrrrrrrddo..
    ...orrrsrrSdo...
    ....ordrrddo....
    .....oddddo.....
    ......oooo......
""")

S("strawberry_seeds", dict(l="seed.l", b="seed.b", d="seed.d", L="cinn.l", B="cinn.b", D="cinn.d"), """
    ................
    ................
    .........oo.....
    ........olbo....
    ...oo...obdo....
    ..olbo...oo.....
    ..obdo......oo..
    ...oo..oo..oLBo.
    ......oLBo.oBDo.
    ......oBDo..oo..
    .......oo.......
    ..oo........oo..
    .olbo......olbo.
    .obdo......obdo.
    ..oo........oo..
    ................
""")

FLOWER7 = [".oo.oo.", "oyyoyyo", "oyyyyyo", ".oyCyo.", "oyyyyyo", "oyyoyyo", ".oo.oo."]

S("vanilla_pod", dict(h="bean.h", l="bean.l", b="bean.b", d="bean.d", y="yellow.l", C="orange.b"), """
    .oo.oo..........
    oyyoyyo......oo.
    oyyyyyo.....ohbo
    .oyCyo.....olbdo
    oyyyyyo...ohbdo.
    oyyoyyo..olbdo..
    .oo.oo..olbdo...
    .......ohbdo....
    ......olbdo.....
    .....olbdo......
    ....ohbdo.......
    ...olbdo........
    ..olbdo.........
    .olbdo..........
    .obdo...........
    ..oo............
""")

S("vanilla_seeds", P(LEAF, h="bean.h", l="bean.l", b="bean.b", d="bean.d"), """
    ................
    ....oo...oo.....
    ...ogGo.oGgo....
    ....oggGggo.....
    .....ooGoo......
    ......oGo.......
    .....ooboo......
    ....ohbbbo..oo..
    ....olbbdo.olbo.
    ....obbddo.obdo.
    ..oo.oooo...oo..
    .olbo...........
    .obdo....oo.....
    ..oo....olbo....
    ........obdo....
    .........oo.....
""")

S("lavender", P(LEAF, L="lav.h", v="lav.l", V="lav.b", x="lav.d", P="pink.m", p="pink.l", d="pink.d"), """
    ..oo...oo...oo..
    .oLVo.oLVo.oLVo.
    .ovxo.ovxo.ovxo.
    ..oVxooVxooVxo..
    ..ovVoovVoovVo..
    ...oVxoVxoVxo...
    ....ovVvVvxo....
    .....ogkgko.....
    ..oo.ogkgko.oo..
    .oPPoPPPPPPoPPo.
    .oPdPPpPPdPPdPo.
    ..oo.oPddPo.oo..
    .....ogkgko.....
    ....ogkoogko....
    ...ogko..ogko...
    ....oo....oo....
""")

S("lavender_seeds", dict(l="lavseed.l", b="lavseed.b", d="lavseed.d"), """
    ................
    ................
    ......oo........
    .....olbo...oo..
    .....obdo..olbo.
    ..oo..oo...obdo.
    .olbo.......oo..
    .obdo..oo.......
    ..oo..olbo......
    ......obdo..oo..
    .......oo..olbo.
    ...oo......obdo.
    ..olbo......oo..
    ..obdo..........
    ...oo...........
    ................
""")

S("peach", P(LEAF, h="peach.h", l="peach.l", b="peach.b", d="peach.d", r="peach.r", R="peach.x",
             c="peach.d", s="cinn.d"), """
    .........ooo....
    .......oogGgo...
    ......osgGko....
    ...oooosooooo...
    ..olhllclbbbbo..
    .olhhlclbbbrbdo.
    olhhllclbbbbbbdo
    olhllclbbbbrrbdo
    olllclbbbbrrRrdo
    ollbbbbbbrrRRrdo
    olbbbbbbbrrRrddo
    obbbbbbbbbrrdddo
    .obbbbbbbbbbddo.
    ..obbbbbbddddo..
    ...obdddddddo...
    ....oooooooo....
""")

S("peach_pit", dict(h="pit.h", l="pit.l", b="pit.b", d="pit.d"), """
    ................
    .......oo.......
    ......olbo......
    .....olhbdo.....
    ....olhldbdo....
    ...olhbldbbdo...
    ...olldbbdldo...
    ..olhdlbbdlbdo..
    ..olbldbbldbdo..
    ..oldbbldbbddo..
    ..olbldbbdlddo..
    ...obdlbbdbdo...
    ...obbdbddddo...
    ....obdddddo....
    .....oooooo.....
    ................
""")

S("cherry", P(LEAF, h="cherry.h", l="cherry.l", b="cherry.b", d="cherry.d"), """
    ...........ooo..
    ..........olgGo.
    .........ogGGko.
    ........kokkoo..
    .......k.koo....
    ......k...k.....
    .....k....k.....
    .....k.....k....
    ..oooo.....k....
    .ohlbbo..oooo...
    ohlbbbdoohlbbo..
    olbbbbdohlbbbdo.
    obbbbddolbbbbdo.
    .obdddoobbbbddo.
    ..oooo..obdddo..
    .........oooo...
""")

S("cherry_pit", dict(h="cpit.h", l="cpit.l", b="cpit.b", d="cpit.d"), """
    ................
    ................
    ..oooo..........
    .ohlbbo.........
    .olbbdo...oooo..
    .obbddo..ohlbbo.
    ..oooo...olbbdo.
    .........obbddo.
    ..........oooo..
    .....oooo.......
    ....ohlbbo......
    ....olbbdo......
    ....obbddo......
    .....oooo.......
    ................
    ................
""")

# ---------------------------------------------------------------- pantry ingredients
FAM["metal"] = dict(h="#FFFFFF", l="#E4E8EE", b="#C2CAD4", d="#9AA4B2", o="#5E6878")
BOWL = dict(W="white.h", w="white.b", x="white.d", X="white.x", p="pink.m")


def canvas():
    return [["."] * 16 for _ in range(16)]


def stamp(cv, glyph, x, y, remap=None):
    for j, row in enumerate(glyph):
        for i, ch in enumerate(row):
            if ch == "." or not (0 <= x + i < 16 and 0 <= y + j < 16):
                continue
            cv[y + j][x + i] = remap.get(ch, ch) if remap else ch
    return cv


def done(cv):
    return ["".join(r) for r in cv]


S("cocoa_powder", P(BOWL, h="choc.h", l="choc.l", b="choc.b", d="choc.d"), """
    ................
    ................
    ................
    ......oooo......
    ....oohlbboo....
    ...olhllbbddo...
    ..olhlbbbbbddo..
    .olhbbbbbbbbddo.
    oWWWWWWWWWWWWWxo
    .owwwwwwwwwwwxo.
    .oppppppppppppo.
    ..owwwwwwwwwxo..
    ...owwwwwwwxo...
    ....oxxxxxxo....
    ....oooooooo....
    ................
""")

S("chocolate", dict(h="choc.h", l="choc.l", b="choc.b", d="choc.d", F="metal.l", f="metal.d",
                    L="pink.l", P="pink.b", D="pink.d", c="cream.b", r="hot.b"), """
    ................
    ..oooooooooooo..
    ..ohlllbhlllbo..
    ..olbbbdlbbbdo..
    ..olbbbdlbbbdo..
    ..obddddbddddo..
    ..ohlllbhlllbo..
    ..olbbbdlbbbdo..
    ..oFFfFFFfFFfo..
    ..oLPPPPPPPPDo..
    ..oLcccrcrccDo..
    ..oLcccrrrccDo..
    ..oLccccrcccDo..
    ..oLPPPPPPPPDo..
    ..oDDDDDDDDDDo..
    ..oooooooooooo..
""")

FLOWER7S = [".oo.oo.", "ohaoabo", "oaaaabo", ".obCdo.", "oabbbdo", "obdobdo", ".oo.oo."]
_sf = canvas()
stamp(_sf, FLOWER7S, 1, 2, {"h": "1", "a": "2", "b": "3", "d": "4"})   # pink
stamp(_sf, FLOWER7S, 8, 1, {"h": "5", "a": "6", "b": "7", "d": "8"})   # lavender
stamp(_sf, FLOWER7S, 4, 8)                                             # white (front)
S("sugar_flowers", {"1": "pink.h", "2": "pink.l", "3": "pink.b", "4": "pink.m",
                    "5": "lav.h", "6": "lav.l", "7": "lav.b", "8": "lav.d",
                    "h": "white.h", "a": "white.h", "b": "white.b", "d": "white.d",
                    "C": "yellow.b"}, done(_sf))

S("cream", dict(h="cream.h", l="cream.l", b="cream.b", d="cream.d", q="glass.h", G="glass.d",
                P="pink.m", H="white.h", W="cream.l", x="cream.d", r="hot.b"), """
    .......oo.......
    ......ohlo......
    .....ohllbo.....
    ....ohhllbbdo...
    ...ohlllbbbddo..
    ..oPPPPPPPPPPo..
    ..oqWWWWWWWWGo..
    .oqHWWWWWWWWxGo.
    .oqHWWWWWWWWxGo.
    .oqWWWWrWrWWxGo.
    .oqWWWWrrrWWxGo.
    .oqWWWWWrWWxxGo.
    .oqWWWWWWWWxxGo.
    .oGxxxxxxxxxxGo.
    ..oGGGGGGGGGGo..
    ...oooooooooo...
""")

S("flour", dict(h="burlap.h", l="burlap.l", b="burlap.b", d="burlap.d", w="white.b", H="white.h",
                P="pink.m", R="hot.b"), """
    ................
    ......oooo......
    ....oowHHwoo....
    ...owHHHwwwwo...
    ..olwwwwwwwwbo..
    ..oblllllllbdo..
    ...oPPPPPPPPo...
    ..olbbbbbbbbdo..
    .olhbbbbbbbbbdo.
    .olbbbbbbbbbbdo.
    olbbbbbRbRbbbddo
    olbbbbbRRRbbbddo
    olbbbbbbRbbbbddo
    obbbbbbbbbbbdddo
    .obbbbbbbbbdddo.
    ..oooooooooooo..
""")

S("butter", P(BOWL, h="butter.h", l="butter.l", b="butter.b", d="butter.d", P="pink.m"), """
    ................
    ................
    ................
    ................
    ..oooooooooooo..
    .ohhhhhhhhhhhlo.
    .ohllllllllllbo.
    .olbbbbbbbdlbdo.
    .obbbbbbbbdlbdo.
    .obbbbbbbbdlddo.
    .oddddddddddddo.
    oWooooooooooooWo
    oWwwwwwwwwwwwwxo
    .oPPPPPPPPPPPPo.
    ..oooooooooooo..
    ................
""")

S("cinnamon", dict(e="cinn.h", l="cinn.l", b="cinn.b", d="cinn.d", x="cinn.o"), """
    ..........oooo..
    .........oexeeo.
    ........olbbdo..
    .......olbbdo...
    ......olbbdoooo.
    .....olbbdoexeeo
    ....olbbdolbbdo.
    ...olbbdolbbdo..
    ..olbbdolbbdo...
    .olbbdolbbdo....
    ..oooolbbdo.....
    ....olbbdo......
    ...olbbdo.......
    ..olbbdo........
    ...oooo.........
    ................
""")

PIPING_BAG = """
    ..............o.
    ........o....oto
    .......oho..otto
    .......ohbo.oRo.
    ......ohlbboRo..
    ......ohlbbbo...
    ......ohlbbbbo..
    .....ohlbbbbbdo.
    .....olbbbbbdo..
    .....obbbddoo...
    ....obddooo.....
    ...oSSoo........
    ..oSso..........
    .oIIo...........
    oIiIo...........
    .ooo............
"""
BAG = dict(S="metal.l", s="metal.d", R="hot.b", t="white.b")
S("icing", P(BAG, h="white.h", l="white.h", b="white.b", d="white.d", I="white.h", i="white.d"), PIPING_BAG)
S("pink_icing", P(BAG, h="pink.h", l="pink.l", b="pink.b", d="pink.m", I="pink.b", i="pink.m", R="lav.b"),
  PIPING_BAG)
S("chocolate_frosting", P(BAG, h="choc.h", l="choc.l", b="choc.b", d="choc.d", I="choc.l", i="choc.b"),
  PIPING_BAG)

# ---------------------------------------------------------------- prepared ingredients & bases
FAM["cookie"] = dict(h="#FFF6E0", l="#FBE6BC", b="#F2D49A", d="#DDB273", o="#A87A40")
FAM["pastry"] = dict(h="#FFF8E8", l="#FFF0D0", b="#F7DFB0", d="#E8C68A", o="#A87E46")
FAM["wood"] = dict(h="#F2CB94", l="#E3B072", b="#CC9050", d="#A86E34", o="#6B4220")
FAM["pearl"] = dict(h="#E8D0C0", l="#8A6050", b="#5C3A2E", d="#3E241C", o="#24120C")


def shift(rows, dx=0, dy=0):
    rows = rows_of(rows) if isinstance(rows, str) else list(rows)
    cv = canvas()
    for y, r in enumerate(rows):
        for x, ch in enumerate(r):
            if ch != "." and 0 <= x + dx < 16 and 0 <= y + dy < 16:
                cv[y + dy][x + dx] = ch
    return done(cv)


S("sprinkles", dict(l="pink.l", P="pink.m", k="pink.d", D="pink.d", q="glass.h", g="glass.l", G="glass.d",
                    w="cream.l", **{"1": "hot.b", "2": "yellow.b", "3": "blue.b", "4": "mint.b", "5": "lav.b"}), """
    ................
    ...oooooooooo...
    ..olPkPPkPPkDo..
    ..olPPPPPPPPDo..
    ..oDDDDDDDDDDo..
    ..oqggggggggGo..
    ..oqw11ww33wGo..
    ..oqwwww5wwwGo..
    ..oq4ww25ww1Go..
    ..oq4ww2www1Go..
    ..oqwww3ww4wGo..
    ..oq1ww3ww4wGo..
    ..oq1w5wwwwwGo..
    ..oqww5w22wwGo..
    ..oGGGGGGGGGGo..
    ...oooooooooo...
""")

PEARL5 = [".ooo.", "ohlbo", "olbdo", "obddo", ".ooo."]
_tp = canvas()
for _x, _y in ((2, 3), (6, 2), (10, 3), (0, 6), (4, 5), (8, 5), (12, 6)):
    stamp(_tp, PEARL5, _x, _y)
stamp(_tp, rows_of("""
    oWWWWWWWWWWWWWxo
    .ommmmmmmmmmmno.
    .oMmmmmmmmmmnno.
    ..oMmmmmmmmnno..
    ...ommmmmmnno...
    ....oooooooo....
"""), 0, 9)
S("tapioca_pearls", dict(h="pearl.h", l="pearl.l", b="pearl.b", d="pearl.d", W="white.h", x="white.d",
                         M="mint.l", m="mint.b", n="mint.d"), done(_tp))

MIX_BOWL = """
    ................
    ..............o.
    .............owo
    ............owo.
    ...........owo..
    ...oooooooowo...
    ..oPPPPPPPwPPo..
    .oPyyyyyywyyyPo.
    oPyYYyyyyyyyqyPo
    oPPyyyyyyyyyyPPo
    olPPPPPPPPPPPPdo
    .olPPPPPPPPPPdo.
    .olPPPPPPPPPddo.
    ..olPPPPPPPddo..
    ...oddddddddo...
    ....oooooooo....
"""
S("cake_batter", dict(P="pink.m", l="pink.l", d="pink.d", y="batter.b", Y="batter.h", q="batter.d",
                      w="wood.b"), MIX_BOWL)
S("macaron_batter", dict(P="lav.b", l="lav.l", d="lav.d", y="pink.l", Y="pink.h", q="pink.b",
                         w="mint.b"), MIX_BOWL)

S("cookie_dough", dict(h="dough.h", l="dough.l", b="dough.b", d="dough.d", k="choc.b", K="choc.d"), """
    ................
    ................
    ......ooo.oo....
    ....oohllolho...
    ...ohllllllllo..
    ..ohllkKllllbdo.
    .ohlllllllkKbdo.
    .ollkKlllbbbbdo.
    olllllbbbbkKbddo
    olllbbkKbbbbbddo
    olbbbbbbbbbbdddo
    .obbbbbbbkKbddo.
    .obbkKbbbbbdddo.
    ..obbbbbbddddo..
    ...oddddddddo...
    ....oooooooo....
""")

S("pastry_dough", dict(h="pastry.h", l="pastry.l", b="pastry.b", d="pastry.d", W="wood.l", w="wood.b",
                       x="wood.d", H="wood.d"), """
    ................
    ................
    ..oooooooooooo..
    .ohhhhhhhhhhhlo.
    .ohlllllllllbbo.
    .olllbbbllllbbo.
    .olllllllbbbbdo.
    .obbbbbbbbbbbdo.
    .oddddddddddddo.
    ..oooooooooooo..
    ooooWWWWWWWWoooo
    oHHowwwwwwwwoHHo
    ooooxxxxxxxxoooo
    ...oooooooooo...
    ................
    ................
""")

CUP_LINER = """
    ________________
    ________________
    ________________
    ________________
    ________________
    ________________
    ________________
    ________________
    ________________
    ________________
    ..oLPLPLPLPLDo..
    ..oLPLPLPLPLDo..
    ...oPLPLPLPDo...
    ...oPLPLPLPDo...
    ....oooooooo....
    ................
"""
S("plain_cupcake", dict(h="gold.h", l="gold.l", b="gold.b", d="gold.d", L="pink.l", P="pink.b", D="pink.m"),
  ov("""
    ................
    ................
    ................
    ......oooo......
    ....oohhlloo....
    ...ohhlllllbo...
    ..ohlllllllbdo..
    .ollllllllbbbdo.
    .olllllllbbbbdo.
    .obbbbbbbbbbbdo.
    ................
    ................
    ................
    ................
    ................
    ................
""", CUP_LINER))

S("sugar_cookie", dict(h="cookie.h", l="cookie.l", b="cookie.b", d="cookie.d", w="white.h"), """
    ................
    .....oooooo.....
    ...oolhhhlloo...
    ..olhhhlllllbo..
    ..olhhbbbbbbdo..
    .olhbbbbbbbbbdo.
    .olbbbbwbbbbbdo.
    .olbbbbbbbbbbdo.
    .olbbbbbbbbwbdo.
    .olbbwbbbbbbbdo.
    .olbbbbbbbbbddo.
    ..obbbbbbbbddo..
    ..odbbbbbbbddo..
    ...oodddddddoo..
    .....oooooo.....
    ................
""")

FAM["almond"] = dict(h="#FFFFFF", l="#FFF5EC", b="#F9E2D2", d="#EBC6B0", m="#DDAF96", o="#A47860")
SHELL = ["..oooooo..", ".ohhlllbo.", "ohlllllbdo", "obbbbbbddo", "oFfFfFfFfo", "ofFfFfFfFo", ".oooooooo."]
_ms = canvas()
stamp(_ms, SHELL, 5, 2)
stamp(_ms, SHELL, 1, 8)
S("macaron_shells", dict(h="almond.h", l="almond.l", b="almond.b", d="almond.d", f="almond.m", F="almond.l"),
  done(_ms))

TEACUP = """
    ................
    ................
    ................
    ................
    ...ooooooooo....
    ..oWjjjjjjjWo...
    .oWiiiiiiiiiWo..
    .oWWiiiiiiiWWooo
    .oPPPPPPPPPPPoWo
    .oWwwwwwwwwwxoWo
    ..oWwwwwwwwxooo.
    ...oxxxxxxxo....
    .oooooooooooooo.
    oWWWWWWWWWWWWWWo
    .oppppppppppppo.
    ..oooooooooooo..
"""
CUP = dict(P="pink.m", p="pink.m", W="white.h", w="white.b", x="white.d")
S("teacup", P(CUP, j="white.x", i="white.d"), shift(TEACUP, 0, -2))

# ---------------------------------------------------------------- cakes & pastries


def ringify(mask, rings, keep=True):
    """Shade a silhouette by distance from its edge.
    mask: 16 rows, '#' = silhouette to shade, other non-'.' chars are kept (painted details)
    rings: list indexed by distance (0 = edge -> usually 'o'); each entry is a char or a
    (lit, base, shade) tuple chosen by which side the edge is on (top/left lit, bottom/right shade)."""
    m = rows_of(mask) if isinstance(mask, str) else list(mask)
    inside = [[m[y][x] != "." for x in range(16)] for y in range(16)]
    dist = [[None] * 16 for _ in range(16)]
    q = deque()
    for y in range(16):
        for x in range(16):
            if inside[y][x] and any(not (0 <= x + dx < 16 and 0 <= y + dy < 16) or not inside[y + dy][x + dx]
                                    for dx, dy in N4):
                dist[y][x] = 0
                q.append((x, y))
    while q:
        x, y = q.popleft()
        for dx, dy in N4:
            nx, ny = x + dx, y + dy
            if 0 <= nx < 16 and 0 <= ny < 16 and inside[ny][nx] and dist[ny][nx] is None:
                dist[ny][nx] = dist[y][x] + 1
                q.append((nx, ny))
    out = canvas()
    for y in range(16):
        for x in range(16):
            if not inside[y][x]:
                continue
            if m[y][x] != "#" and keep:
                out[y][x] = m[y][x]
                continue
            d = dist[y][x]
            r = rings[min(d, len(rings) - 1)]
            if isinstance(r, str):
                out[y][x] = r
                continue
            lit, base, shade = r
            dirs = [(dx, dy) for dx, dy in N4 if 0 <= x + dx < 16 and 0 <= y + dy < 16
                    and dist[y + dy][x + dx] is not None and dist[y + dy][x + dx] < d]
            if not dirs:
                dirs = [(dx, dy) for dx, dy in N4 if not (0 <= x + dx < 16 and 0 <= y + dy < 16)
                        or not inside[y + dy][x + dx]]
            tl = any(dx < 0 or dy < 0 for dx, dy in dirs)
            br = any(dx > 0 or dy > 0 for dx, dy in dirs)
            out[y][x] = lit if tl and not br else shade if br and not tl else base
    return done(out)


BERRY6 = ["..oo..", ".okko.", "olhrro", "orrrdo", ".ordo.", "..oo.."]
_sc = canvas()
stamp(_sc, rows_of("""
    ................
    ................
    ................
    ................
    ..oooooooooooo..
    .oHWWWWWWWWWWxo.
    oHWWWWWWWWWWWWxo
    oWWWWWWWWWWWWxxo
    oWWWWWWWWWWWWxxo
    oLWSSSWSSSSWSSdo
    oLSSSSSSSSSSSSdo
    oCCCCCCCCCCCCCco
    oLSSSSSSSSSSSSdo
    .oLSSSSSSSSSSdo.
    ..oooooooooooo..
    ................
"""), 0, 1)
for _x, _y in ((5, 1), (1, 3), (10, 3)):
    stamp(_sc, BERRY6, _x, _y)
S("strawberry_cake", dict(H="cream.h", W="cream.l", x="cream.d", L="pink.l", S="pink.b", d="pink.m",
                          C="cream.l", c="cream.d", k="green.b", h="red.h", l="red.l", r="red.b"), done(_sc))

FROST_TIERS = """
    ________________
    ________________
    ________________
    ________________
    ...ohlPPPPdo....
    ..ommmmmmmmmo...
    ..ohlPPPPPPdo...
    .ohllPPPPPPPddo.
    .ommmmmmmmmmmmo.
    ohlPPPPPPPPPPddo
    .ooLCLCLCLCLDoo.
    ________________
    ________________
    ________________
    ________________
    ________________
"""
LINER_ONLY = CUP_LINER.replace("..oLPLPLPLPLDo..", "..oLCLCLCLCLDo..").replace(
    "...oPLPLPLPDo...", "...oCLCLCLCDo...")
_pv = ov(ov(LINER_ONLY, FROST_TIERS), """
    .......o........
    ......owo.......
    .....owywo......
    ....ooPwPoo.....
    ________________
    ________________
    ________________
    ________________
    ________________
    ________________
    ________________
    ________________
    ________________
    ________________
    ________________
    ________________
""")
S("pink_vanilla_cupcake", dict(h="pink.h", l="pink.l", P="pink.b", d="pink.m", m="pink.m",
                               w="white.h", y="yellow.b", L="cream.h", C="cream.b", D="cream.d"), _pv)

_cc = ov(ov(CUP_LINER, FROST_TIERS), """
    ........k.......
    ......ooko......
    .....ohrrro.....
    ....oorrrdoo....
    ________________
    ________________
    ________________
    ________________
    ________________
    ________________
    .ooLPLPLPLPLDoo.
    ________________
    ________________
    ________________
    ________________
    ________________
""")
S("chocolate_cupcake", dict(h="choc.h", l="choc.l", P="choc.b", d="choc.d", m="choc.d", k="green.d",
                            r="cherry.b", L="pink.l", C="pink.b", D="pink.m"), _cc)

MACARON = """
    ................
    ................
    ....oooooooo....
    ..oohhllllbboo..
    .ohhllllllllbdo.
    ohlllllllllllbdo
    obbbbbbbbbbbbddo
    ofFfFfFfFfFfFfFo
    oWWWWWWWWWWWWWxo
    oWWWWWWWWWWWWxxo
    ofFfFfFfFfFfFfFo
    obbbbbbbbbbbbddo
    .obbbbbbbbbbbdo.
    ..oddddddddddo..
    ...oooooooooo...
    ................
"""
S("lavender_macaron", dict(h="lav.h", l="lav.l", b="lav.b", d="lav.d", f="lav.d", F="lav.b",
                           W="white.h", x="white.d"), MACARON)
S("strawberry_macaron", dict(h="pink.l", l="rose.l", b="rose.b", d="rose.d", f="rose.d", F="rose.b",
                             W="pink.l", x="pink.b"), MACARON)

S("heart_cookie", dict(C="cookie.l", c="cookie.b", k="cookie.d", w="white.h", W="white.b",
                       h="pink.l", p="pink.b", P="pink.m"), ringify("""
    ................
    ................
    ..####....####..
    .######..######.
    ################
    ################
    ################
    .##############.
    ..############..
    ...##########...
    ....########....
    .....######.....
    ......####......
    .......##.......
    ................
    ................
""", ["o", ("C", "c", "k"), ("w", "w", "W"), ("h", "p", "P"), ("h", "p", "p")]))

S("cinnamon_roll", dict(l="gold.l", b="gold.b", d="gold.d", c="cinn.b", w="white.h"), """
    ................
    .....oooooo.....
    ...oolllllloo...
    ..ollcccccclbo..
    ..olcbwwwwbcbo..
    .olclbccccbbcbo.
    .olclclbbbcbcdo.
    .olcwcbbcbcwcdo.
    .obcwbccbbcwcdo.
    .obcbbbbbbcbcdo.
    .obbccccccbbcdo.
    ..obbbbbbbdcdo..
    ..obbccccccddo..
    ...ooddddddoo...
    .....oooooo.....
    ................
""")

DONUT = ["..oooo..", ".ohpppo.", "ohp1ppPo", "oppooPPo", "op2ooPPo", "ogppPPdo", ".oggGGo.", "..oooo.."]
_md = canvas()
stamp(_md, DONUT, 0, 2, {"1": "3", "2": "4"})
stamp(_md, DONUT, 8, 1, {"1": "4", "2": "5"})
stamp(_md, DONUT, 4, 7, {"1": "5", "2": "3"})
S("mini_donuts", {"h": "pink.l", "p": "pink.b", "P": "pink.m", "d": "gold.d", "g": "gold.b", "G": "gold.d",
                  "3": "yellow.b", "4": "blue.b", "5": "white.h"}, done(_md))

_pp = ringify("""
    ................
    ...##########...
    ..############..
    .##############.
    .##############.
    .##############.
    .##############.
    .##############.
    .##############.
    .##############.
    .##############.
    .##############.
    .##############.
    ..############..
    ...##########...
    ................
""", ["o", ("h", "l", "d"), ("l", "b", "x"), ("Y", "y", "q"), "y"])
PEACH_HALF = [".oooo.", "oepPPo", "opPPRo", "oPPRro", "oPRrro", ".oooo."]
S("peach_pastry", dict(h="gold.h", l="gold.l", b="gold.b", d="gold.d", x="gold.x", Y="batter.l", y="batter.b",
                       q="batter.d", e="peach.h", p="peach.l", P="peach.b", r="peach.d", R="peach.r"),
  done(stamp([list(r) for r in _pp], PEACH_HALF, 5, 5)))

S("cherry_pie", dict(C="gold.l", c="gold.d", g="gold.b", R="cherry.b", h="cherry.l", T="pink.m", t="pink.d"), """
    ................
    ................
    ....oooooooo....
    ..ooCcCcCcCcoo..
    .oCcRgRRgRRgcCo.
    oCcggggggggggcCo
    oCcRRgRRgRRgRcCo
    oCcRhgRRgRhgRcCo
    oCcggggggggggcCo
    oCcRRgRRgRRgRcCo
    ocCcCcCcCcCcCcCo
    oTTTTTTTTTTTTTto
    .otttttttttttto.
    ..oooooooooooo..
    ................
    ................
""")

# ---------------------------------------------------------------- drinks
GLASS = dict(q="glass.h", g="glass.l", G="glass.d")

S("strawberry_milk", P(GLASS, R="hot.b", r="hot.l", d="hot.d", m="pink.b", M="pink.l", W="white.h",
                       k="green.b", s="red.b"), """
    ................
    .....oooooo.....
    ....orRRRRRo....
    ....oddddddo....
    .....oqmmGo.....
    ....oqMmmmGo....
    ...oqMmmmmmGo...
    ..oqMmmmmmmmGo..
    ..oqmWWWWWWmGo..
    ..oqmWWkkWWmGo..
    ..oqmWssssWmGo..
    ..oqmWWssWWmGo..
    ..oqmWWWWWWmGo..
    ..oqmmmmmmmmGo..
    ..oGGGGGGGGGGo..
    ...oooooooooo...
""")

S("vanilla_milkshake", P(GLASS, h="cherry.h", r="cherry.b", d="cherry.d", k="green.d", S="hot.b", s="white.h",
                         H="white.h", W="cream.l", x="cream.d", y="batter.l", Y="batter.h"), """
    ......ook....oo.
    .....ohro...oSso
    .....ordo..oSso.
    ....ooHWWoSso...
    ..ooHWWWWSsWWoo.
    .oHWWWWWWWWWWxo.
    oqYyyyyyyyyyyyGo
    .oqyyyyyyyyyyGo.
    .oqyyyyyyyyyyGo.
    ..oqyyyyyyyyGo..
    ..oqyyyyyyyyGo..
    ...oqyyyyyyGo...
    ....oqyyyyGo....
    ......oGGo......
    ....oqqGGGGo....
    ....oooooooo....
""")

S("lavender_tea", P(CUP, j="lav.d", i="lav.b", V="lav.b", v="lav.l", g="green.b", z="steam.b"), ov(TEACUP, """
    ....z..z...ooo..
    .....z..z.oVvo..
    .....z..z.ovVo..
    ....z..z..ogo...
    ...ooooooogo....
    ________________
    ________________
    ________________
    ________________
    ________________
    ________________
    ________________
    ________________
    ________________
    ________________
    ________________
"""))

S("peach_tea", P(CUP, j="tea.d", i="tea.b", r="peach.x", f="peach.b"), ov(TEACUP, """
    ________________
    ...........ooo..
    ..........orrro.
    .........orfffro
    ...oooooorfffoo.
    ..oWjjjjjoffo...
    ________________
    ________________
    ________________
    ________________
    ________________
    ________________
    ________________
    ________________
    ________________
    ________________
"""))

S("rose_tea", P(CUP, j="rose.d", i="rose.b", R="red.b", r="red.d", e="pink.l", g="green.b"), ov(TEACUP, """
    ________________
    .oooo...........
    oRrrRo..........
    orRRro..........
    oRrrRooooooo....
    .oooogjjjjjWo...
    .oWiiieeiiiiWo..
    .oWWiiiieiiWWooo
    ________________
    ________________
    ________________
    ________________
    ________________
    ________________
    ________________
    ________________
"""))

S("bubble_tea", P(GLASS, S="hot.b", s="hot.l", T="milktea.b", k="pearl.b", h="pearl.h", K="pearl.d"), """
    ..........oo....
    .........oSso...
    ....oooooSsooo..
    ...oqggggSsggo..
    ..oqgggggSsgggo.
    .oGGGGGGGSsGGGGo
    ..oqTTTTTSsTTGo.
    ..oqTTTTSsTTTGo.
    ..oqTTTTSsTTTGo.
    ...oqTTSsTTTGo..
    ...oqTTSsTTTGo..
    ...oqkhSskhkGo..
    ...okkhkkhkkGo..
    ....okhkkhkGo...
    ....oGGGGGGGo...
    .....ooooooo....
""")

S("pink_lemonade", P(GLASS, Y="lemon.d", w="lemon.b", l="lemon.l", c="white.h", S="white.h", s="hot.b",
                     P="pink.b", I="blue.l", i="white.h"), """
    ...........oo...
    ..ooo.....oSso..
    .oYYYo....oSso..
    oYwlwYo...oSso..
    oYlclYoooooSsoo.
    oYwlwYoPPPPSsGo.
    .oYYYoPPPPPSsGo.
    ..oooPPPPPPSsGo.
    ...oqPIIPPPSsGo.
    ...oqPIiPPPSsGo.
    ...oqPPPPPPSsGo.
    ...oqPPPPIIPPGo.
    ...oqPPPPIiPPGo.
    ...oqPPPPPPPPGo.
    ...oGGGGGGGGGGo.
    ....oooooooooo..
""")

S("hot_chocolate", dict(z="steam.b", P="pink.m", L="pink.b", d="pink.d", C="choc.b", W="white.h", w="white.b",
                        x="white.d", Q="pink.l", q="pink.b", h="white.h", H="pink.m"), """
    ....z...z.......
    .....z...z......
    .....z...z......
    ....z...z.......
    ..oooooooooo....
    .oPCWwCCQqCPo...
    .oPCwxCCqqCPo...
    .oPPPPPPPPPPoo..
    .oLPPPPPPPPdoHHo
    .oLPPhPhPPPdooHo
    .oLPPhhhPPPdoHHo
    .oLPPPhPPPPdooo.
    .oLPPPPPPPPdo...
    ..oddddddddo....
    ...oooooooo.....
    ................
""")

# ---------------------------------------------------------------- special treats
HEART9 = [".ooo.ooo.", "ohhlolbbo", "ohllbbbbo", "olbbbbbdo", ".obbbbdo.", "..obbdo..", "...odo...", "....o...."]
_hc = canvas()
stamp(_hc, HEART9, 7, 1, {"l": "4", "b": "5", "d": "6"})   # pink (back)
stamp(_hc, HEART9, 0, 2, {"l": "1", "b": "2", "d": "3"})   # red
stamp(_hc, HEART9, 4, 8, {"l": "7", "b": "8", "d": "9"})   # white (front)
S("heart_candy", {"h": "white.h", "1": "red.l", "2": "red.b", "3": "red.d", "4": "hot.l", "5": "hot.b",
                  "6": "hot.d", "7": "white.h", "8": "white.b", "9": "pink.b"}, done(_hc))

S("strawberry_lollipop", dict(l="red.l", r="red.b", d="red.d", q="white.h", p="pink.l", P="pink.b",
                              S="white.h"), ringify("""
    ................
    ........#####...
    .......#######..
    ......#########.
    ......#########.
    .....###########
    .....###########
    ......#########.
    ......#########.
    .......#######..
    .....oSo#####...
    ....oSo.........
    ...oSo..........
    ..oSo...........
    .oSo............
    .oo.............
""", ["o", ("l", "r", "d"), ("q", "p", "P"), ("l", "r", "d"), ("q", "p", "p"), "r"]))

S("marshmallow_cloud", dict(h="white.h", W="pink.h", p="pink.l", P="pink.b", e="choc.d", b="pink.m",
                            k="pink.d", y="star.b", Y="white.h"), """
    .............y..
    ............yYy.
    .......ooo...y..
    ..ooo.ohhho.ooo.
    .ohhhoWWWWWoWWWo
    ohhhhhWWWWWWWWpo
    ohhWWWWWWWWWWWpo
    ohWWWeWWWWeWWWpo
    oWWWbWWkkWWbWppo
    oWWWWWWWWWWWpppo
    .oWWWWWWWWWpppo.
    ..oPpppppppPPo..
    ...oooooooooo...
    ................
    ................
    ................
""")

S("rainbow_cookie", {"C": "cookie.l", "c": "cookie.b", "k": "cookie.d", "w": "white.h", "W": "white.d",
                     "1": "hot.b", "2": "yellow.b", "3": "blue.b", "4": "mint.d", "5": "lav.d", "6": "orange.b"},
  ringify("""
    ................
    .....######.....
    ...##########...
    ..############..
    ..########2###..
    .####11###2####.
    .##############.
    .#######4######.
    .###33##4######.
    .#########55###.
    .##############.
    ..###66##11###..
    ..############..
    ...##########...
    .....######.....
    ................
""", ["o", ("C", "c", "k"), ("c", "c", "k"), ("w", "w", "W"), "w"]))

_hy = ov(ov(LINER_ONLY, FROST_TIERS), """
    .....oo.oo......
    ....oWWoWWo.....
    ....oYKYKYo.....
    ...ooooooooo....
    ________________
    ________________
    ________________
    ________________
    ________________
    ________________
    ________________
    ________________
    ________________
    ________________
    ________________
    ________________
""")
S("honey_cupcake", dict(h="cream.h", l="cream.l", P="cream.b", d="cream.d", m="honey.b", W="white.h",
                        Y="yellow.b", K="choc.d", L="honey.h", C="honey.l", D="honey.d"), _hy)

_mc = ringify("""
    ................
    .....##.........
    ...####.........
    ..#####.........
    .#####..........
    .##w##..........
    .######.........
    .######.........
    .##w####........
    .########.......
    ..##########....
    ..####w#######..
    ...#######w###..
    ....#########...
    ......#####.....
    ................
""", ["o", ("C", "c", "k"), ("h", "y", "Y")])
_mc = done(stamp([list(r) for r in _mc], ["..o..", ".oyo.", "oyhyo", ".oyo.", "..o.."], 9, 2))
S("moon_cookie", dict(C="cookie.l", c="cookie.b", k="cookie.d", h="star.h", y="star.b", Y="star.d",
                      w="white.h"), _mc)

# ---------------------------------------------------------------- tool
S("recipe_book", dict(D="hot.d", d="hot.b", L="hot.h", P="hot.l", Q="hot.b", c="cream.b", C="cream.d",
                      w="cream.l", R="red.b"), """
    ................
    ..oooooooooooo..
    .oDdLLLLLLLLLco.
    .oDdPPPPPPPPPCo.
    .oDdPPPPPPPPPco.
    .oDdPPwwPwwPPCo.
    .oDdPwwwwwwwPco.
    .oDdPwwwwwwwPCo.
    .oDdPPwwwwwPPco.
    .oDdPPPwwwPPPCo.
    .oDdPPPPwPPPPco.
    .oDdPPPPPPPPPCo.
    .oDdPPPPPPPPPco.
    .oDdQQQQQQQQQCo.
    ..ooooooooRRooo.
    .........oRRo...
""")

# ---------------------------------------------------------------- sponge cake (plain base)
S("sponge_cake", dict(h="gold.h", l="gold.l", b="gold.b", d="gold.d", x="gold.d", Y="batter.l", y="batter.b",
                      q="batter.d"), """
    ................
    ................
    ................
    ....oooooooo....
    ..oohhhllllloo..
    .ohhllllllllbbo.
    .olllllllllbbdo.
    .obbbbbbbbbbddo.
    .oYYyyyyyyyyyqo.
    .oYyyqyyyyqyyqo.
    .oYyyyyyqyyyyqo.
    .oyyqyyyyyyqyqo.
    ..oxxxxxxxxxxo..
    ...oooooooooo...
    ................
    ................
""")


# =========================================================================== main
EXPECTED = [
    "strawberry", "strawberry_seeds", "vanilla_pod", "vanilla_seeds", "lavender", "lavender_seeds", "peach",
    "peach_pit", "cherry", "cherry_pit", "cocoa_powder", "chocolate", "sugar_flowers", "cream", "flour", "butter",
    "cinnamon", "icing", "pink_icing", "chocolate_frosting", "sprinkles", "tapioca_pearls", "cake_batter",
    "cookie_dough", "pastry_dough", "macaron_batter", "sponge_cake", "plain_cupcake", "sugar_cookie",
    "macaron_shells", "teacup", "strawberry_cake", "pink_vanilla_cupcake", "chocolate_cupcake", "lavender_macaron",
    "strawberry_macaron", "heart_cookie", "cinnamon_roll", "mini_donuts", "peach_pastry", "cherry_pie",
    "strawberry_milk", "vanilla_milkshake", "lavender_tea", "peach_tea", "bubble_tea", "pink_lemonade",
    "hot_chocolate", "rose_tea", "heart_candy", "strawberry_lollipop", "marshmallow_cloud", "rainbow_cookie",
    "honey_cupcake", "moon_cookie", "recipe_book",
]


def main(argv=None):
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--sheet", help="also write an 8x contact sheet PNG to this path")
    ap.add_argument("--only", help="comma separated subset of sprite names")
    args = ap.parse_args(argv)
    missing = [n for n in EXPECTED if n not in SPRITES]
    if missing:
        print("sprites not defined:", ", ".join(missing))
        return 1
    names = args.only.split(",") if args.only else EXPECTED
    write_all(names)
    ok = verify(names)
    if args.sheet:
        contact_sheet(names, args.sheet)
        print("contact sheet:", args.sheet)
    print(f"wrote {len(names)} item textures to {OUT_DIR}")
    return 0 if ok else 1


if __name__ == "__main__":
    sys.exit(main())

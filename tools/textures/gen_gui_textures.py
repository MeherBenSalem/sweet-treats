"""
Generates the Sweet Treats GUI textures (pastel, vanilla-style panels). Slot coordinates match the menu classes.

Run from the project root:   python tools/textures/gen_gui_textures.py
"""
import os

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
OUT = os.path.join(ROOT, "1.20.1", "common", "src", "main", "resources", "assets", "sweettreats", "textures", "gui")


def hexc(h, a=255):
    h = h.lstrip("#")
    return (int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), a)


# Palette
OUTLINE = hexc("5E2F45")
HILITE = hexc("FFF8FB")
SHADOW = hexc("D28FAB")
FILL = hexc("F8D3E1")
FILL_DOT = hexc("F4C3D6")
SLOT_DARK = hexc("8E4F6C")
SLOT_LIGHT = hexc("FFF8FB")
SLOT_IN = hexc("C998AE")
HEART = hexc("E8384F")
HEART_LIGHT = hexc("FF8A9C")
CLEAR = (0, 0, 0, 0)


def px(img, x, y, c):
    if 0 <= x < img.width and 0 <= y < img.height:
        img.putpixel((x, y), c)


def rect(img, x, y, w, h, c):
    for yy in range(y, y + h):
        for xx in range(x, x + w):
            px(img, xx, yy, c)


def stamp(img, x, y, rows, palette):
    for dy, row in enumerate(rows):
        for dx, ch in enumerate(row):
            if ch in palette:
                px(img, x + dx, y + dy, palette[ch])


def panel(img, x, y, w, h):
    """Vanilla-shaped panel: rounded 1px outline, 2px highlight/shadow bevel, pastel fill with a soft dot pattern."""
    rect(img, x, y, w, h, CLEAR)
    rect(img, x + 3, y + 3, w - 6, h - 6, FILL)
    # subtle polka dots
    for yy in range(y + 6, y + h - 6, 8):
        for xx in range(x + 6 + ((yy // 8) % 2) * 4, x + w - 6, 8):
            px(img, xx, yy, FILL_DOT)
    # outline
    rect(img, x + 2, y, w - 4, 1, OUTLINE)
    rect(img, x + 2, y + h - 1, w - 4, 1, OUTLINE)
    rect(img, x, y + 2, 1, h - 4, OUTLINE)
    rect(img, x + w - 1, y + 2, 1, h - 4, OUTLINE)
    px(img, x + 1, y + 1, OUTLINE)
    px(img, x + w - 2, y + 1, OUTLINE)
    px(img, x + 1, y + h - 2, OUTLINE)
    px(img, x + w - 2, y + h - 2, OUTLINE)
    # highlight (top/left)
    rect(img, x + 2, y + 1, w - 4, 2, HILITE)
    rect(img, x + 1, y + 2, 2, h - 4, HILITE)
    px(img, x + 3, y + 3, HILITE)
    # shadow (bottom/right)
    rect(img, x + 3, y + h - 3, w - 5, 2, SHADOW)
    rect(img, x + w - 3, y + 3, 2, h - 5, SHADOW)
    px(img, x + w - 4, y + h - 4, SHADOW)
    # the two mixed corners
    px(img, x + w - 3, y + 2, FILL)
    px(img, x + 2, y + h - 3, FILL)


def slot(img, x, y, inner=SLOT_IN):
    """A slot whose item sits at (x, y); the 18x18 border starts at (x-1, y-1)."""
    rect(img, x - 1, y - 1, 18, 18, inner)
    rect(img, x - 1, y - 1, 17, 1, SLOT_DARK)
    rect(img, x - 1, y - 1, 1, 17, SLOT_DARK)
    rect(img, x, y + 16, 17, 1, SLOT_LIGHT)
    rect(img, x + 16, y, 1, 17, SLOT_LIGHT)


def big_slot(img, x, y):
    """Result slot with a 26x26 frame around the item position (x, y)."""
    rect(img, x - 5, y - 5, 26, 26, SLOT_IN)
    rect(img, x - 5, y - 5, 25, 1, SLOT_DARK)
    rect(img, x - 5, y - 5, 1, 25, SLOT_DARK)
    rect(img, x - 4, y + 20, 25, 1, SLOT_LIGHT)
    rect(img, x + 20, y - 4, 1, 25, SLOT_LIGHT)
    # a cute inner ring
    rect(img, x - 1, y - 1, 18, 18, hexc("D3A5B9"))
    rect(img, x, y, 16, 16, SLOT_IN)


def player_inventory(img, x=8, y=84):
    for row in range(3):
        for col in range(9):
            slot(img, x + col * 18, y + row * 18)
    for col in range(9):
        slot(img, x + col * 18, y + 58)


HEART_ROWS = [
    ".oo.oo.",
    "ohhrrro",
    "ohrrrro",
    ".orrro.",
    "..oro..",
    "...o...",
]
HEART_PAL = {"o": hexc("A3203A"), "r": HEART, "h": HEART_LIGHT}


def hearts(img, w):
    stamp(img, 7, 5, HEART_ROWS, HEART_PAL)
    stamp(img, w - 14, 5, HEART_ROWS, HEART_PAL)


ARROW_ROWS = [
    "..............x.........",
    "..............xx........",
    "..............xxx.......",
    "..............xxxx......",
    "..............xxxxx.....",
    "xxxxxxxxxxxxxxxxxxxx....",
    "xxxxxxxxxxxxxxxxxxxxx...",
    "xxxxxxxxxxxxxxxxxxxxxx..",
    "xxxxxxxxxxxxxxxxxxxxxx..",
    "xxxxxxxxxxxxxxxxxxxxx...",
    "xxxxxxxxxxxxxxxxxxxx....",
    "..............xxxxx.....",
    "..............xxxx......",
    "..............xxx.......",
    "..............xx........",
    "..............x.........",
]


def arrow(img, x, y, colors):
    """colors: function(dx, dy) -> rgba for filled arrow pixels."""
    for dy, row in enumerate(ARROW_ROWS):
        for dx, ch in enumerate(row):
            if ch == "x":
                px(img, x + dx, y + dy, colors(dx, dy))


def arrow_empty(dx, dy):
    return hexc("D9A3BA")


def arrow_full_pink(dx, dy):
    t = dx / 23
    base = (int(255 - 23 * t), int(138 - 82 * t), int(156 - 76 * t), 255)
    if dy in (5, 6) and dx < 20:
        return hexc("FFC4D0")
    return base


def arrow_full_lavender(dx, dy):
    t = dx / 23
    if dy in (5, 6) and dx < 20:
        return hexc("EFE4FF")
    return (int(200 - 60 * t), int(170 - 70 * t), int(240 - 30 * t), 255)


FLAME_ROWS = [
    "......o.......",
    ".....oyo......",
    ".....oyyo.....",
    "....oyyyo.....",
    "...oyyyyyo....",
    "...oyywyyo..o.",
    "..oyywwyyyo.oo",
    ".oyyywwwyyoooy",
    ".oyyywwwyyyyyo",
    "oyyywwwwwyyyyo",
    "oyyyywwwwyyyo.",
    ".oyyyyyyyyyo..",
    "..ooooooooo...",
]


def flame(img, x, y, lit):
    if lit:
        pal = {"o": hexc("E0464E"), "y": hexc("FF9F4A"), "w": hexc("FFE680")}
    else:
        pal = {"o": hexc("C98AA4"), "y": hexc("E2AFC4"), "w": hexc("E8BBCD")}
    stamp(img, x, y, FLAME_ROWS, pal)


BOTTLE_GHOST = [
    "....oo....",
    "....oo....",
    "...o..o...",
    "...o..o...",
    "..o....o..",
    ".o......o.",
    ".o......o.",
    ".o......o.",
    ".o......o.",
    "..oooooo..",
]

PLUS = [
    "...xx...",
    "...xx...",
    "...xx...",
    "xxxxxxxx",
    "xxxxxxxx",
    "...xx...",
    "...xx...",
    "...xx...",
]

CROSS = [
    "oo..........oo..",
    "orro......orro..",
    ".orro....orro...",
    "..orro..orro....",
    "...orroorro.....",
    "....orrrro......",
    "....orrrro......",
    "...orroorro.....",
    "..orro..orro....",
    ".orro....orro...",
    "orro......orro..",
    "oo..........oo..",
]


def save(img, name):
    os.makedirs(OUT, exist_ok=True)
    img.save(os.path.join(OUT, name))
    print("wrote", name)


def bakery_oven():
    img = Image.new("RGBA", (256, 256), CLEAR)
    panel(img, 0, 0, 176, 166)
    hearts(img, 176)
    for (x, y) in ((44, 17), (62, 17), (44, 35), (62, 35)):
        slot(img, x, y)
    slot(img, 12, 53)
    flame(img, 13, 36, lit=False)
    arrow(img, 88, 34, arrow_empty)
    big_slot(img, 124, 35)
    player_inventory(img)
    # sprites
    flame(img, 176, 0, lit=True)
    arrow(img, 176, 14, arrow_full_pink)
    save(img, "bakery_oven.png")


def mixing_bowl():
    img = Image.new("RGBA", (256, 256), CLEAR)
    panel(img, 0, 0, 176, 166)
    hearts(img, 176)
    for (x, y) in ((26, 17), (44, 17), (62, 17), (26, 35), (44, 35), (62, 35)):
        slot(img, x, y)
    slot(img, 92, 56)
    stamp(img, 95, 59, BOTTLE_GHOST, {"o": hexc("B4849A")})
    arrow(img, 88, 35, arrow_empty)
    big_slot(img, 124, 35)
    player_inventory(img)
    arrow(img, 176, 0, arrow_full_lavender)
    save(img, "mixing_bowl.png")


def decoration_station():
    img = Image.new("RGBA", (256, 256), CLEAR)
    panel(img, 0, 0, 176, 166)
    hearts(img, 176)
    slot(img, 26, 35)
    slot(img, 62, 26)
    slot(img, 62, 44)
    stamp(img, 46, 39, PLUS, {"x": hexc("C2577F")})
    arrow(img, 88, 35, arrow_full_pink)
    big_slot(img, 124, 35)
    player_inventory(img)
    stamp(img, 176, 2, CROSS, {"o": hexc("7A1428"), "r": hexc("E8384F")})
    save(img, "decoration_station.png")


# ------------------------------------------------------------------------------------------------- recipe book
COVER = hexc("E8728F")
COVER_DARK = hexc("A64B6E")
COVER_LIGHT = hexc("F7A8C0")
PAGE = hexc("FFF6E6")
PAGE_SHADE = hexc("F3E2C8")
PAGE_EDGE = hexc("E2C9A6")
SPINE = hexc("D9B98F")


def recipe_book():
    img = Image.new("RGBA", (256, 256), CLEAR)
    W, H = 256, 180
    # cover with rounded corners
    rect(img, 1, 0, W - 2, H, COVER)
    rect(img, 0, 1, W, H - 2, COVER)
    rect(img, 1, 0, W - 2, 1, COVER_DARK)
    rect(img, 1, H - 1, W - 2, 1, COVER_DARK)
    rect(img, 0, 1, 1, H - 2, COVER_DARK)
    rect(img, W - 1, 1, 1, H - 2, COVER_DARK)
    rect(img, 1, 1, W - 2, 1, COVER_LIGHT)
    rect(img, 1, 1, 1, H - 3, COVER_LIGHT)
    # stitched border on the cover
    for x in range(4, W - 4, 3):
        px(img, x, 3, COVER_LIGHT)
        px(img, x, H - 4, COVER_DARK)
    # page block (stacked page edges at the bottom)
    rect(img, 6, 6, W - 12, H - 10, PAGE_EDGE)
    rect(img, 7, 6, W - 14, H - 12, PAGE)
    for i in range(3):
        rect(img, 8, H - 6 + i - 2, W - 16, 1, PAGE_SHADE if i % 2 == 0 else PAGE_EDGE)
    # gentle shading toward the spine
    spine_x = W // 2
    for i in range(10):
        shade = tuple(int(PAGE[k] - (PAGE[k] - SPINE[k]) * (1 - i / 10) ** 2) for k in range(3)) + (255,)
        rect(img, spine_x - 1 - i, 6, 1, H - 12, shade)
        rect(img, spine_x + i, 6, 1, H - 12, shade)
    rect(img, spine_x - 1, 6, 2, H - 12, hexc("C9A577"))
    # tiny heart doodles in page corners
    small_heart = [".o.o.", "ooooo", ".ooo.", "..o.."]
    stamp(img, 112, 160, small_heart, {"o": hexc("F2B8C8")})
    stamp(img, 140, 160, small_heart, {"o": hexc("F2B8C8")})
    # title underline on the left page
    for x in range(16, 121, 2):
        px(img, x, 23, hexc("F0C6D4"))
    # ribbon bookmark peeking out of the bottom-right page edge
    for y in range(H - 13, H):
        rect(img, 228, y, 6, 1, hexc("C8102E"))
        px(img, 228, y, hexc("8A0A1F"))
    stamp(img, 228, H - 3, ["o....o", "oo..oo", "ooo.oo"], {"o": hexc("C8102E")})
    stamp(img, 228, H - 3, [".oooo.", "..oo..", "......"], {"o": CLEAR})

    # ---- sprites at y = 184
    def cell(u, border, inner, light):
        rect(img, u, 184, 20, 20, inner)
        rect(img, u, 184, 20, 1, border)
        rect(img, u, 203, 20, 1, border)
        rect(img, u, 184, 1, 20, border)
        rect(img, u + 19, 184, 1, 20, border)
        rect(img, u + 1, 185, 18, 1, light)
        rect(img, u + 1, 185, 1, 18, light)
        for c in ((u, 184), (u + 19, 184), (u, 203), (u + 19, 203)):
            px(img, c[0], c[1], CLEAR)

    cell(0, hexc("E6C7AE"), hexc("FCEBDD"), hexc("FFF8F0"))       # normal
    cell(20, hexc("E8728F"), hexc("FFE0EA"), hexc("FFF4F8"))      # hover
    cell(40, hexc("D8C3B6"), hexc("EFE1D6"), hexc("F7EEE6"))      # undiscovered
    cell(60, hexc("C2577F"), hexc("FFD1E1"), hexc("FFF0F5"))      # selected
    rect(img, 61, 185, 18, 1, hexc("C2577F"))
    rect(img, 61, 202, 18, 1, hexc("C2577F"))

    prev_rows = [
        ".....oo...........",
        "....oxo...........",
        "...oxxooooooooooo.",
        "..oxxxxxxxxxxxxxxo",
        ".oxxxxxxxxxxxxxxxo",
        "oxxxxxxxxxxxxxxxxo",
        "oxxxxxxxxxxxxxxxxo",
        ".oxxxxxxxxxxxxxxxo",
        "..oxxxxxxxxxxxxxxo",
        "...oxxooooooooooo.",
        "....oxo...........",
        ".....oo...........",
    ]
    next_rows = [row[::-1] for row in prev_rows]
    stamp(img, 80, 184, prev_rows, {"o": hexc("A64B6E"), "x": hexc("F59AB9")})
    stamp(img, 98, 184, prev_rows, {"o": hexc("A64B6E"), "x": hexc("FFC4D9")})
    stamp(img, 116, 184, next_rows, {"o": hexc("A64B6E"), "x": hexc("F59AB9")})
    stamp(img, 134, 184, next_rows, {"o": hexc("A64B6E"), "x": hexc("FFC4D9")})

    # ingredient frame 18x18
    rect(img, 152, 184, 18, 18, hexc("F3DCC6"))
    rect(img, 152, 184, 17, 1, hexc("C9A07E"))
    rect(img, 152, 184, 1, 17, hexc("C9A07E"))
    rect(img, 153, 201, 17, 1, hexc("FFFDF8"))
    rect(img, 169, 185, 1, 17, hexc("FFFDF8"))

    # discovered marker: tiny heart 6x6
    stamp(img, 170, 184, [".o.o..", "oxoxo.", "oxxxo.", ".oxo..", "..o...", "......"],
          {"o": hexc("A3203A"), "x": hexc("FF6B7E")})

    # ---- tabs at y = 206
    def tab(u, h, fill, light, dark):
        rect(img, u + 1, 206, 26, h, fill)
        rect(img, u + 2, 206, 24, 1, dark)
        px(img, u + 1, 207, dark)
        px(img, u + 26, 207, dark)
        rect(img, u, 208, 1, h - 2, dark)
        rect(img, u + 27, 208, 1, h - 2, dark)
        rect(img, u + 2, 207, 24, 1, light)
        rect(img, u + 1, 208, 1, h - 2, light)
        rect(img, u + 26, 208, 1, h - 2, hexc("000000", 0) if False else dark)

    tab(0, 24, hexc("D9C2EE"), hexc("EFE4FA"), hexc("6A4C9C"))
    tab(28, 27, hexc("FFC4D9"), hexc("FFE8F0"), hexc("A64B6E"))
    save(img, "recipe_book.png")


if __name__ == "__main__":
    bakery_oven()
    mixing_bowl()
    decoration_station()
    recipe_book()

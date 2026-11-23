"""Converts the showcase captures (source/captures/*.png, 1920x1080) to the JPEG screenshots in images/screenshots/
and copies the counter shot to source/backdrop.jpg, the card background. Needs Pillow. Run from anywhere."""
from pathlib import Path

from PIL import Image

HERE = Path(__file__).resolve().parent
CAPTURES = HERE / "captures"
SHOTS = HERE.parent / "images" / "screenshots"


def main():
    SHOTS.mkdir(parents=True, exist_ok=True)
    for png in sorted(CAPTURES.glob("*.png")):
        jpg = SHOTS / (png.stem + ".jpg")
        Image.open(png).convert("RGB").save(jpg, quality=88, optimize=True, progressive=True)
        print(jpg.relative_to(HERE.parent.parent), f"{jpg.stat().st_size // 1024} KB")
    Image.open(CAPTURES / "counter.png").convert("RGB").save(HERE / "backdrop.jpg", quality=80, optimize=True)


if __name__ == "__main__":
    main()

"""make-icons.py: the logo's sizes from couchside-source.jpeg (Gemini's full-size render).

The tile is masked to clean rounded corners (21.5% radius, drawn at 4x) after pulling the edge in 1.2%, so no light
fringe from the page shows. Writes couchside-{1024,512,256,128,64,48,32}.png here and the window icon resource.
"""
from pathlib import Path
from PIL import Image, ImageDraw

here = Path(__file__).parent
tile = Image.open(here / "couchside-source.jpeg").convert("RGB")
side = min(tile.size)
tile = tile.crop((0, 0, side, side))
inset = round(side * 0.012)
tile = tile.crop((inset, inset, side - inset, side - inset)).resize((1024, 1024), Image.LANCZOS)
big = Image.new("L", (4096, 4096), 0)
ImageDraw.Draw(big).rounded_rectangle((0, 0, 4095, 4095), radius=round(4096 * 0.215), fill=255)
logo = tile.convert("RGBA")
logo.putalpha(big.resize((1024, 1024), Image.LANCZOS))
logo.save(here / "couchside-1024.png")
for n in (512, 256, 128, 64, 48, 32):
    logo.resize((n, n), Image.LANCZOS).save(here / f"couchside-{n}.png")
logo.resize((256, 256), Image.LANCZOS).save(here.parent / "runtime/src/main/resources/couchside/icon.png")

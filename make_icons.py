import struct, os, io
from PIL import Image, ImageDraw

src = r"shared\src\jvmMain\resources\sprites\makebead.png"
tray_path = r"shared\src\jvmMain\resources\icons\tray.png"
ico_path = r"shared\src\jvmMain\resources\icons\icon.ico"

img = Image.open(src).convert("RGBA")

def round_corners(im, r):
    mask = Image.new("L", im.size, 0)
    draw = ImageDraw.Draw(mask)
    draw.rounded_rectangle([(0, 0), im.size], radius=r, fill=255)
    result = im.copy()
    result.putalpha(mask)
    return result

# ── tray.png ──
tray = round_corners(img.resize((256, 256), Image.LANCZOS), 40)
tray.save(tray_path)
print(f"tray.png: 256x256, {os.path.getsize(tray_path)} bytes")

# ── icon.ico ──
sizes = [(16,3), (24,4), (32,5), (48,7), (64,10), (128,20), (256,40)]
png_data = []
for w, r in sizes:
    s = round_corners(img.resize((w,w), Image.LANCZOS), r)
    buf = io.BytesIO()
    s.save(buf, format="PNG")
    png_data.append(buf.getvalue())
    buf.close()

header = struct.pack("<HHH", 0, 1, len(sizes))
offset = 6 + len(sizes) * 16
entries = []
for i, (w, r) in enumerate(sizes):
    data = png_data[i]
    bW = 0 if w == 256 else w
    bH = 0 if w == 256 else w
    entries.append(struct.pack("<BBBBHHII", bW, bH, 0, 0, 1, 32, len(data), offset))
    offset += len(data)

with open(ico_path, "wb") as f:
    f.write(header)
    for e in entries:
        f.write(e)
    for data in png_data:
        f.write(data)

print(f"icon.ico: {os.path.getsize(ico_path)} bytes, {len(sizes)} sizes")

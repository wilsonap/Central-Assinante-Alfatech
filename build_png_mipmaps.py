import subprocess
import os

res_dir = "app/src/main/res"
mipmap_dirs = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}

# ImageMagick command to render 512x512 master icon image
# Coordinates scaled x5 relative to 108dp canvas (108 * 4.74 = 512)
# Center = (256, 256)
draw_cmd = """
convert -size 512x512 xc:white \
  -fill none -stroke "#52B2F0" -strokewidth 8.5 -draw "circle 256,256 256,106" \
  -fill none -stroke "#B2E2FF" -strokewidth 6.6 -draw "circle 256,256 256,113" \
  -fill none -stroke "#247EC6" -strokewidth 3.8 -draw "circle 256,256 256,119" \
  -fill "#0071C5" -stroke none -draw "circle 256,204 256,121" \
  -fill none -stroke white -strokewidth 5.7 -draw "circle 256,204 256,121" \
  -stroke white -strokewidth 5.7 -draw "line 256,121 256,287" \
  -stroke white -strokewidth 4.7 -draw "path 'M 256,121 C 223,156 223,251 256,287'" \
  -stroke white -strokewidth 3.8 -draw "path 'M 256,121 C 190,147 190,261 256,287'" \
  -stroke white -strokewidth 4.7 -draw "path 'M 256,121 C 289,156 289,251 256,287'" \
  -stroke white -strokewidth 3.8 -draw "path 'M 256,121 C 322,147 322,261 256,287'" \
  -stroke white -strokewidth 4.7 -draw "path 'M 194,161 Q 256,180 318,161'" \
  -stroke white -strokewidth 4.7 -draw "path 'M 173,204 Q 256,228 339,204'" \
  -stroke white -strokewidth 4.7 -draw "path 'M 194,247 Q 256,266 318,247'" \
  -fill "#363B44" -stroke none -draw "path 'M 346,133 L 356,175 L 332,166 C 356,223 346,299 299,346 C 256,389 199,394 171,394 C 128,394 90,365 71,327 C 52,289 57,242 81,209 C 81,242 95,280 123,313 C 152,342 180,356 209,356 C 242,356 280,337 304,294 C 327,251 323,204 304,171 L 318,152 Z'" \
  -fill "#1D2026" -stroke none -draw "path 'M 171,394 C 228,394 285,370 323,327 C 352,294 346,251 332,223 C 323,247 304,275 275,299 C 237,332 194,342 161,342 C 123,342 95,323 81,294 C 95,332 128,370 171,394 Z'" \
  /tmp/master_icon_512.png

convert /tmp/master_icon_512.png \
  \( +clone -threshold -1 -fill white -draw "circle 256,256 256,0" \) \
  -alpha off -compose copy_opacity -composite /tmp/master_icon_512_round.png
"""

subprocess.run(draw_cmd, shell=True, check=True)

for folder, size in mipmap_dirs.items():
    folder_path = os.path.join(res_dir, folder)
    os.makedirs(folder_path, exist_ok=True)
    
    cmd_sq = f"convert /tmp/master_icon_512.png -resize {size}x{size} {folder_path}/ic_launcher.png"
    subprocess.run(cmd_sq, shell=True, check=True)
    
    cmd_rd = f"convert /tmp/master_icon_512_round.png -resize {size}x{size} {folder_path}/ic_launcher_round.png"
    subprocess.run(cmd_rd, shell=True, check=True)

print("All PNG mipmap icons regenerated successfully!")

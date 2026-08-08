import os
import subprocess

res_dir = "app/src/main/res"
mipmap_dirs = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}

# ImageMagick script to generate 512x512 master app icon
# 1. Base 512x512 image with white background
# 2. Globe circle at cx=242, cy=213, r=90 (radius 90) -> top=123, bot=303, left=152, right=332
# 3. White grid lines
# 4. Charcoal swoosh path wrapping around bottom and pointing to top-right
draw_script = """
convert -size 512x512 xc:white \
  -fill "#0077C8" -draw "circle 242,213 242,123" \
  -stroke white -strokewidth 6 -fill none -draw "circle 242,213 242,123" \
  -stroke white -strokewidth 6 -draw "line 242,123 242,303" \
  -stroke white -strokewidth 5 -draw "path 'M 242,123 C 200,160 200,266 242,303'" \
  -stroke white -strokewidth 4 -draw "path 'M 242,123 C 160,150 160,276 242,303'" \
  -stroke white -strokewidth 5 -draw "path 'M 242,123 C 284,160 284,266 242,303'" \
  -stroke white -strokewidth 4 -draw "path 'M 242,123 C 324,150 324,276 242,303'" \
  -stroke white -strokewidth 5 -draw "path 'M 170,165 Q 242,190 314,165'" \
  -stroke white -strokewidth 5 -draw "path 'M 152,213 Q 242,242 332,213'" \
  -stroke white -strokewidth 5 -draw "path 'M 170,261 Q 242,285 314,261'" \
  -stroke none -fill "#363A42" -draw "path 'M 384,109 L 393,152 L 370,142 C 393,204 384,294 332,351 C 289,398 227,403 194,403 C 147,403 104,374 81,332 C 62,290 66,237 90,199 C 90,237 104,280 137,318 C 166,351 199,365 227,365 C 265,365 308,341 336,294 C 360,246 351,190 332,156 L 346,133 Z'" \
  /tmp/master_icon.png
"""

subprocess.run(draw_script, shell=True, check=True)

# Generate round version
round_script = """
convert /tmp/master_icon.png \
  \( +clone -threshold -1 -fill white -draw 'circle 256,256 256,0' \) \
  -alpha off -compose copy_opacity -composite /tmp/master_icon_round.png
"""
subprocess.run(round_script, shell=True, check=True)

for folder, size in mipmap_dirs.items():
    folder_path = os.path.join(res_dir, folder)
    os.makedirs(folder_path, exist_ok=True)
    
    # Save ic_launcher.png
    cmd_sq = f"convert /tmp/master_icon.png -resize {size}x{size} {folder_path}/ic_launcher.png"
    subprocess.run(cmd_sq, shell=True, check=True)
    
    # Save ic_launcher_round.png
    cmd_rd = f"convert /tmp/master_icon_round.png -resize {size}x{size} {folder_path}/ic_launcher_round.png"
    subprocess.run(cmd_rd, shell=True, check=True)

print("Master icon and mipmaps generated!")

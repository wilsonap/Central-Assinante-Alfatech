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

# ImageMagick script to generate 512x512 master app icon matching user attachment
draw_cmd = """
convert -size 512x512 xc:white \
  -fill none -stroke "#5BB5F0" -strokewidth 12 -draw "circle 256,256 256,36" \
  -fill none -stroke "#BBE4FF" -strokewidth 8  -draw "circle 256,256 256,42" \
  -fill none -stroke "#2082C8" -strokewidth 4  -draw "circle 256,256 256,46" \
  -fill "#0072C6" -stroke none -draw "circle 256,205 256,90" \
  -fill none -stroke white -strokewidth 5 -draw "circle 256,205 256,90" \
  -stroke white -strokewidth 5 -draw "line 256,90 256,320" \
  -stroke white -strokewidth 4 -draw "path 'M 256,90 C 205,140 205,270 256,320'" \
  -stroke white -strokewidth 3 -draw "path 'M 256,90 C 160,130 160,280 256,320'" \
  -stroke white -strokewidth 4 -draw "path 'M 256,90 C 307,140 307,270 256,320'" \
  -stroke white -strokewidth 3 -draw "path 'M 256,90 C 352,130 352,280 256,320'" \
  -stroke white -strokewidth 4.5 -draw "path 'M 162,148 Q 256,178 350,148'" \
  -stroke white -strokewidth 4.5 -draw "path 'M 141,205 Q 256,238 371,205'" \
  -stroke white -strokewidth 4.5 -draw "path 'M 162,262 Q 256,292 350,262'" \
  -fill "#2D323B" -stroke none -draw "path 'M 358,132 L 368,175 L 345,165 C 370,218 368,295 325,342 C 285,385 220,392 185,392 C 140,392 102,365 82,325 C 64,285 68,235 90,200 C 90,235 104,275 135,312 C 162,342 195,355 222,355 C 258,355 298,332 324,288 C 346,242 338,190 320,158 L 332,138 Z'" \
  -fill "#3E4450" -stroke none -draw "path 'M 185,392 C 228,392 280,380 320,335 C 358,290 355,230 338,190 C 328,230 310,270 270,308 C 228,348 180,342 150,330 C 180,360 210,370 250,370 C 290,370 330,345 352,305 C 335,348 290,385 230,388 Z'" \
  /tmp/exact_master.png
"""

subprocess.run(draw_cmd, shell=True, check=True)

# Generate round image
round_cmd = """
convert /tmp/exact_master.png \
  \( +clone -threshold -1 -fill white -draw "circle 256,256 256,0" \) \
  -alpha off -compose copy_opacity -composite /tmp/exact_master_round.png
"""
subprocess.run(round_cmd, shell=True, check=True)

for folder, size in mipmap_dirs.items():
    folder_path = os.path.join(res_dir, folder)
    os.makedirs(folder_path, exist_ok=True)
    
    # Save ic_launcher.png
    cmd_sq = f"convert /tmp/exact_master.png -resize {size}x{size} {folder_path}/ic_launcher.png"
    subprocess.run(cmd_sq, shell=True, check=True)
    
    # Save ic_launcher_round.png
    cmd_rd = f"convert /tmp/exact_master_round.png -resize {size}x{size} {folder_path}/ic_launcher_round.png"
    subprocess.run(cmd_rd, shell=True, check=True)

print("Icons generated successfully!")

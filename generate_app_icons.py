import os
import subprocess

# Define directory paths
res_dir = "app/src/main/res"
mipmap_dirs = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}

# 1. Create vector foreground for Android Adaptive Icon (ic_launcher_foreground.xml)
foreground_xml = """<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:aapt="http://schemas.android.com/aapt"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">

    <!-- GLOBE SPHERE (BLUE GRADIENT) -->
    <path
        android:pathData="M 51,26 A 19,19 0 1,0 51,64 A 19,19 0 1,0 51,26 Z">
        <aapt:attr name="android:fillColor">
            <gradient
                android:startX="62"
                android:startY="28"
                android:endX="38"
                android:endY="62"
                android:type="linear">
                <item android:offset="0.0" android:color="#00A2E8" />
                <item android:offset="0.4" android:color="#0077C8" />
                <item android:offset="1.0" android:color="#004A8B" />
            </gradient>
        </aapt:attr>
    </path>

    <!-- GLOBE OUTER WHITE BORDER -->
    <path
        android:strokeColor="#FFFFFF"
        android:strokeWidth="1.2"
        android:pathData="M 51,26 A 19,19 0 1,0 51,64 A 19,19 0 1,0 51,26 Z" />

    <!-- GLOBE GRID LINES (WHITE) -->
    <path
        android:strokeColor="#FFFFFF"
        android:strokeWidth="1.4"
        android:pathData="M 51,26 L 51,64" />
    <path
        android:strokeColor="#FFFFFF"
        android:strokeWidth="1.2"
        android:pathData="M 51,26 C 43,34 43,56 51,64" />
    <path
        android:strokeColor="#FFFFFF"
        android:strokeWidth="1.0"
        android:pathData="M 51,26 C 36,32 36,58 51,64" />
    <path
        android:strokeColor="#FFFFFF"
        android:strokeWidth="1.2"
        android:pathData="M 51,26 C 59,34 59,56 51,64" />
    <path
        android:strokeColor="#FFFFFF"
        android:strokeWidth="1.0"
        android:pathData="M 51,26 C 66,32 66,58 51,64" />

    <path
        android:strokeColor="#FFFFFF"
        android:strokeWidth="1.2"
        android:strokeLineCap="round"
        android:pathData="M 36,35 Q 51,40 66,35" />
    <path
        android:strokeColor="#FFFFFF"
        android:strokeWidth="1.2"
        android:strokeLineCap="round"
        android:pathData="M 32,45 Q 51,51 70,45" />
    <path
        android:strokeColor="#FFFFFF"
        android:strokeWidth="1.2"
        android:strokeLineCap="round"
        android:pathData="M 36,54 Q 51,59 66,54" />

    <!-- CHARCOAL SWOOSH RING WITH ARROWHEAD -->
    <path
        android:pathData="M 81,23 L 83,32 L 78,30 C 83,43 81,62 70,74 C 61,84 48,85 41,85 C 31,85 22,79 17,70 C 13,61 14,50 19,42 C 19,50 22,59 29,67 C 35,74 42,77 48,77 C 56,77 65,72 71,62 C 76,52 74,40 70,33 L 73,28 Z">
        <aapt:attr name="android:fillColor">
            <gradient
                android:startX="20"
                android:startY="85"
                android:endX="81"
                android:endY="23"
                android:type="linear">
                <item android:offset="0.0" android:color="#1A1C20" />
                <item android:offset="0.5" android:color="#363A42" />
                <item android:offset="1.0" android:color="#4F545E" />
            </gradient>
        </aapt:attr>
    </path>

</vector>
"""

with open(f"{res_dir}/drawable/ic_launcher_foreground.xml", "w") as f:
    f.write(foreground_xml)

# 2. Create SVG for generating crisp PNG mipmaps
svg_content = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 108 108" width="512" height="512">
  <defs>
    <linearGradient id="globeGrad" x1="62" y1="28" x2="38" y2="62" gradientUnits="userSpaceOnUse">
      <stop offset="0%" stop-color="#00A2E8"/>
      <stop offset="40%" stop-color="#0077C8"/>
      <stop offset="100%" stop-color="#004A8B"/>
    </linearGradient>
    <linearGradient id="swooshGrad" x1="20" y1="85" x2="81" y2="23" gradientUnits="userSpaceOnUse">
      <stop offset="0%" stop-color="#1A1C20"/>
      <stop offset="50%" stop-color="#363A42"/>
      <stop offset="100%" stop-color="#4F545E"/>
    </linearGradient>
  </defs>

  <!-- Solid White Background -->
  <rect width="108" height="108" fill="#FFFFFF"/>

  <!-- GLOBE SPHERE -->
  <circle cx="51" cy="45" r="19" fill="url(#globeGrad)" stroke="#FFFFFF" stroke-width="1.2"/>

  <!-- GLOBE GRID LINES -->
  <path d="M 51,26 L 51,64" stroke="#FFFFFF" stroke-width="1.4" fill="none"/>
  <path d="M 51,26 C 43,34 43,56 51,64" stroke="#FFFFFF" stroke-width="1.2" fill="none"/>
  <path d="M 51,26 C 36,32 36,58 51,64" stroke="#FFFFFF" stroke-width="1.0" fill="none"/>
  <path d="M 51,26 C 59,34 59,56 51,64" stroke="#FFFFFF" stroke-width="1.2" fill="none"/>
  <path d="M 51,26 C 66,32 66,58 51,64" stroke="#FFFFFF" stroke-width="1.0" fill="none"/>

  <path d="M 36,35 Q 51,40 66,35" stroke="#FFFFFF" stroke-width="1.2" stroke-linecap="round" fill="none"/>
  <path d="M 32,45 Q 51,51 70,45" stroke="#FFFFFF" stroke-width="1.2" stroke-linecap="round" fill="none"/>
  <path d="M 36,54 Q 51,59 66,54" stroke="#FFFFFF" stroke-width="1.2" stroke-linecap="round" fill="none"/>

  <!-- CHARCOAL SWOOSH RING WITH ARROWHEAD -->
  <path d="M 81,23 L 83,32 L 78,30 C 83,43 81,62 70,74 C 61,84 48,85 41,85 C 31,85 22,79 17,70 C 13,61 14,50 19,42 C 19,50 22,59 29,67 C 35,74 42,77 48,77 C 56,77 65,72 71,62 C 76,52 74,40 70,33 L 73,28 Z" fill="url(#swooshGrad)"/>
</svg>
"""

with open("/tmp/app_icon.svg", "w") as f:
    f.write(svg_content)

# Render PNGs using ImageMagick 'convert'
for folder, size in mipmap_dirs.items():
    folder_path = os.path.join(res_dir, folder)
    os.makedirs(folder_path, exist_ok=True)
    
    # Generate ic_launcher.png (square with slight rounded corners or full)
    cmd_sq = f"convert -background white /tmp/app_icon.svg -resize {size}x{size} {folder_path}/ic_launcher.png"
    subprocess.run(cmd_sq, shell=True, check=True)
    
    # Generate ic_launcher_round.png (circular masked)
    # Create circular mask for round icon
    cmd_round = (
        f"convert /tmp/app_icon.svg -resize {size}x{size} "
        f"\( +clone -threshold -1 -fill white -draw 'circle {size/2},{size/2} {size/2},1' \) "
        f"-alpha off -compose copy_opacity -composite {folder_path}/ic_launcher_round.png"
    )
    subprocess.run(cmd_round, shell=True, check=True)

print("Icons generated successfully!")

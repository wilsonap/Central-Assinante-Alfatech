import os
import subprocess

res_dir = "app/src/main/res"

# 1. Update Background XML (Pure White)
bg_xml = """<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path
        android:fillColor="#FFFFFF"
        android:pathData="M0,0h108v108h-108z" />
</vector>
"""

with open(f"{res_dir}/drawable/ic_launcher_background.xml", "w") as f:
    f.write(bg_xml)

# 2. Update Foreground XML (Perfectly centered inside 66dp safe zone: x=21..87, y=21..87, center=54,54)
fg_xml = """<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:aapt="http://schemas.android.com/aapt"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">

    <!-- 1. OUTER LIGHT BLUE DOUBLE CIRCULAR FRAME (CENTERED AT 54, 54, RADIUS 31.5) -->
    <!-- Outer light blue ring -->
    <path
        android:strokeColor="#52B2F0"
        android:strokeWidth="1.8"
        android:pathData="M54,22.5 A31.5,31.5 0 1,1 53.99,22.5 Z" />
    
    <!-- Middle soft cyan ring -->
    <path
        android:strokeColor="#B2E2FF"
        android:strokeWidth="1.4"
        android:pathData="M54,24 A30,30 0 1,1 53.99,24 Z" />

    <!-- Inner dark blue accent line -->
    <path
        android:strokeColor="#247EC6"
        android:strokeWidth="0.8"
        android:pathData="M54,25.2 A28.8,28.8 0 1,1 53.99,25.2 Z" />

    <!-- 2. BLUE GLOBE SPHERE (CENTERED AT 54, 43, RADIUS 17.5) -->
    <path
        android:pathData="M54,25.5 A17.5,17.5 0 1,1 53.99,25.5 Z">
        <aapt:attr name="android:fillColor">
            <gradient
                android:startX="65"
                android:startY="27"
                android:endX="43"
                android:endY="59"
                android:type="linear">
                <item android:offset="0.0" android:color="#00A5EC" />
                <item android:offset="0.4" android:color="#0071C5" />
                <item android:offset="1.0" android:color="#004385" />
            </gradient>
        </aapt:attr>
    </path>

    <!-- Globe White Outer Border -->
    <path
        android:strokeColor="#FFFFFF"
        android:strokeWidth="1.2"
        android:pathData="M54,25.5 A17.5,17.5 0 1,1 53.99,25.5 Z" />

    <!-- Globe White Grid Lines -->
    <path
        android:strokeColor="#FFFFFF"
        android:strokeWidth="1.2"
        android:pathData="M54,25.5 L54,60.5" />
    <path
        android:strokeColor="#FFFFFF"
        android:strokeWidth="1.0"
        android:pathData="M54,25.5 C47,33 47,53 54,60.5" />
    <path
        android:strokeColor="#FFFFFF"
        android:strokeWidth="0.8"
        android:pathData="M54,25.5 C40,31 40,55 54,60.5" />
    <path
        android:strokeColor="#FFFFFF"
        android:strokeWidth="1.0"
        android:pathData="M54,25.5 C61,33 61,53 54,60.5" />
    <path
        android:strokeColor="#FFFFFF"
        android:strokeWidth="0.8"
        android:pathData="M54,25.5 C68,31 68,55 54,60.5" />

    <path
        android:strokeColor="#FFFFFF"
        android:strokeWidth="1.0"
        android:strokeLineCap="round"
        android:pathData="M41,34 Q54,38 67,34" />
    <path
        android:strokeColor="#FFFFFF"
        android:strokeWidth="1.0"
        android:strokeLineCap="round"
        android:pathData="M36.5,43 Q54,48 71.5,43" />
    <path
        android:strokeColor="#FFFFFF"
        android:strokeWidth="1.0"
        android:strokeLineCap="round"
        android:pathData="M41,52 Q54,56 67,52" />

    <!-- 3. CHARCOAL METALLIC SWOOSH RIBBON & ARROW -->
    <!-- Main Outer Swoosh Ribbon with Arrowhead -->
    <path
        android:pathData="M 73,28 L 75,37 L 70,35 C 75,47 73,63 63,73 C 54,82 42,83 36,83 C 27,83 19,77 15,69 C 11,61 12,51 17,44 C 17,51 20,59 26,66 C 32,72 38,75 44,75 C 51,75 59,71 64,62 C 69,53 68,43 64,36 L 67,32 Z">
        <aapt:attr name="android:fillColor">
            <gradient
                android:startX="15"
                android:startY="83"
                android:endX="75"
                android:endY="28"
                android:type="linear">
                <item android:offset="0.0" android:color="#1A1C20" />
                <item android:offset="0.5" android:color="#363B44" />
                <item android:offset="1.0" android:color="#505662" />
            </gradient>
        </aapt:attr>
    </path>

    <!-- Lower Loop Accent Shading -->
    <path
        android:pathData="M 36,83 C 48,83 60,78 68,69 C 74,62 73,53 70,47 C 68,52 64,58 58,63 C 50,70 41,72 34,72 C 26,72 20,68 17,62 C 20,70 27,78 36,83 Z">
        <aapt:attr name="android:fillColor">
            <gradient
                android:startX="17"
                android:startY="83"
                android:endX="74"
                android:endY="47"
                android:type="linear">
                <item android:offset="0.0" android:color="#2A2E38" />
                <item android:offset="1.0" android:color="#121316" />
            </gradient>
        </aapt:attr>
    </path>

</vector>
"""

with open(f"{res_dir}/drawable/ic_launcher_foreground.xml", "w") as f:
    f.write(fg_xml)

print("Vector drawables updated!")

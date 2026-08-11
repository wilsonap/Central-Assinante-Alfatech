# Regenera ic_launcher*.png a partir de branding/alfatech-app-icon-source.png
# Uso: powershell -ExecutionPolicy Bypass -File scripts/sync-app-icon.ps1

$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.Drawing

$root = Split-Path -Parent $PSScriptRoot
$srcPath = Join-Path $root "branding\alfatech-app-icon-source.png"
if (-not (Test-Path -LiteralPath $srcPath)) {
    throw "Fonte nao encontrada: $srcPath"
}

$src = [System.Drawing.Image]::FromFile($srcPath)

function New-SquareIcon([System.Drawing.Image]$source, [int]$size, [bool]$circular) {
    $bmp = New-Object System.Drawing.Bitmap $size, $size
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $g.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
    $g.Clear([System.Drawing.Color]::White)

    $scale = [Math]::Min($size / $source.Width, $size / $source.Height)
    $w = [int]($source.Width * $scale)
    $h = [int]($source.Height * $scale)
    $x = [int](($size - $w) / 2)
    $y = [int](($size - $h) / 2)
    $g.DrawImage($source, $x, $y, $w, $h)

    if (-not $circular) {
        $g.Dispose()
        return $bmp
    }

    $mask = New-Object System.Drawing.Bitmap $size, $size
    $mg = [System.Drawing.Graphics]::FromImage($mask)
    $mg.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $mg.Clear([System.Drawing.Color]::Transparent)
    $mg.FillEllipse([System.Drawing.Brushes]::White, 0, 0, $size - 1, $size - 1)
    $mg.Dispose()

    $out = New-Object System.Drawing.Bitmap $size, $size
    $og = [System.Drawing.Graphics]::FromImage($out)
    $og.Clear([System.Drawing.Color]::Transparent)
    $og.DrawImage($bmp, 0, 0)
    for ($yy = 0; $yy -lt $size; $yy++) {
        for ($xx = 0; $xx -lt $size; $xx++) {
            if ($mask.GetPixel($xx, $yy).A -lt 128) {
                $out.SetPixel($xx, $yy, [System.Drawing.Color]::Transparent)
            }
        }
    }
    $og.Dispose(); $mask.Dispose(); $bmp.Dispose(); $g.Dispose()
    return $out
}

function New-Foreground([System.Drawing.Image]$source, [int]$size) {
    $bmp = New-Object System.Drawing.Bitmap $size, $size
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $g.Clear([System.Drawing.Color]::Transparent)
    $safe = [int]($size * 0.72)
    $scale = [Math]::Min($safe / $source.Width, $safe / $source.Height)
    $w = [int]($source.Width * $scale)
    $h = [int]($source.Height * $scale)
    $x = [int](($size - $w) / 2)
    $y = [int](($size - $h) / 2)
    $g.DrawImage($source, $x, $y, $w, $h)
    $g.Dispose()
    return $bmp
}

$sizes = @{
    "mipmap-mdpi"    = 48
    "mipmap-hdpi"    = 72
    "mipmap-xhdpi"   = 96
    "mipmap-xxhdpi"  = 144
    "mipmap-xxxhdpi" = 192
}
$fgSizes = @{
    "mipmap-mdpi"    = 108
    "mipmap-hdpi"    = 162
    "mipmap-xhdpi"   = 216
    "mipmap-xxhdpi"  = 324
    "mipmap-xxxhdpi" = 432
}

foreach ($folder in $sizes.Keys) {
    $size = $sizes[$folder]
    $dir = Join-Path $root "app\src\main\res\$folder"
    New-Item -ItemType Directory -Force -Path $dir | Out-Null

    $sq = New-SquareIcon $src $size $false
    $sq.Save((Join-Path $dir "ic_launcher.png"), [System.Drawing.Imaging.ImageFormat]::Png)
    $sq.Dispose()

    $rd = New-SquareIcon $src $size $true
    $rd.Save((Join-Path $dir "ic_launcher_round.png"), [System.Drawing.Imaging.ImageFormat]::Png)
    $rd.Dispose()
}

foreach ($folder in $fgSizes.Keys) {
    $size = $fgSizes[$folder]
    $dir = Join-Path $root "app\src\main\res\$folder"
    $fg = New-Foreground $src $size
    $fg.Save((Join-Path $dir "ic_launcher_foreground.png"), [System.Drawing.Imaging.ImageFormat]::Png)
    $fg.Dispose()
    Write-Host "OK $folder"
}

$src.Dispose()

# Adaptive icons devem apontar para mipmap (PNG), nao para vetor generico.
$adaptive = @"
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_background" />
    <foreground android:drawable="@mipmap/ic_launcher_foreground" />
</adaptive-icon>
"@
$anydpi = Join-Path $root "app\src\main\res\mipmap-anydpi-v26"
New-Item -ItemType Directory -Force -Path $anydpi | Out-Null
Set-Content -Encoding UTF8 (Join-Path $anydpi "ic_launcher.xml") $adaptive
Set-Content -Encoding UTF8 (Join-Path $anydpi "ic_launcher_round.xml") $adaptive

$vecFg = Join-Path $root "app\src\main\res\drawable\ic_launcher_foreground.xml"
if (Test-Path -LiteralPath $vecFg) {
    Remove-Item -LiteralPath $vecFg -Force
    Write-Host "Removed drawable/ic_launcher_foreground.xml"
}

Write-Host "Icones sincronizados a partir de branding/alfatech-app-icon-source.png"

Add-Type -AssemblyName PresentationCore, PresentationFramework, WindowsBase

function Render-ExactIcon($targetSize, $isRound = $false)
{
    $visual = New-Object System.Windows.Media.DrawingVisual
    $dc = $visual.RenderOpen()

    $scale = $targetSize / 108.0

    # If round icon, clip drawing to a circle
    if ($isRound)
    {
        $radius = $targetSize / 2.0
        $clipGeom = New-Object System.Windows.Media.EllipseGeometry(
        (New-Object System.Windows.Point $radius, $radius),
        $radius,
        $radius
        )
        $dc.PushClip($clipGeom)
    }

    # 1. Background Rectangle with Linear Gradient (#BA68C8 -> #7B1FA2)
    $gradBrush = New-Object System.Windows.Media.LinearGradientBrush(
    [System.Windows.Media.ColorConverter]::ConvertFromString("#FFBA68C8"),
    [System.Windows.Media.ColorConverter]::ConvertFromString("#FF7B1FA2"),
    (New-Object System.Windows.Point 0, 0),
    (New-Object System.Windows.Point $targetSize, $targetSize)
    )
    $dc.DrawRectangle($gradBrush, $null, (New-Object System.Windows.Rect 0, 0, $targetSize, $targetSize))

    # White brush for foreground paths
    $whiteBrush = [System.Windows.Media.Brushes]::White

    # Helper function to render a pathData with transform
    function Draw-VectorPath($pathData, $tx, $ty, $sx, $sy)
    {
        $geom = [System.Windows.Media.Geometry]::Parse($pathData)

        # Apply transformation: scaling for resolution + translation/scale from group
        $transformGroup = New-Object System.Windows.Media.TransformGroup
        $transformGroup.Children.Add((New-Object System.Windows.Media.ScaleTransform $sx, $sy))
        $transformGroup.Children.Add((New-Object System.Windows.Media.TranslateTransform $tx, $ty))
        $transformGroup.Children.Add((New-Object System.Windows.Media.ScaleTransform $scale, $scale))

        $dc.PushTransform($transformGroup)
        $dc.DrawGeometry($whiteBrush, $null, $geom)
        $dc.Pop()
    }

    # Group 1: Center Circle with Shuffle Cutout
    $p1 = "M54,54m-13,0a13,13 0,1 1,26 0a13,13 0,1 1,-26 0 M55.8,61.2L55.8,59.4L58.14,59.4L55.28,56.55L56.57,55.26L59.4,58.1L59.4,55.8L61.2,55.8L61.2,61.2L55.8,61.2ZM48.06,61.2L46.8,59.94L58.14,48.6L55.8,48.6L55.8,46.8L61.2,46.8L61.2,52.2L59.4,52.2L59.4,49.86L48.06,61.2ZM51.45,52.72L46.8,48.06L48.06,46.8L52.72,51.45L51.45,52.72Z"
    Draw-VectorPath $p1 0 -14 1.0 1.0

    # Avatar path data
    $pAvatar = "M12,12c2.21,0 4,-1.79 4,-4s-1.79,-4 -4,-4 -4,1.79 -4,4 1.79,4 4,4z M12,14c-2.67,0 -8,1.34 -8,4v2h16v-2c0,-2.66 -5.33,-4 -8,-4z"

    # Group 2: Top Left Player Avatar
    Draw-VectorPath $pAvatar 27 43 0.75 0.75

    # Group 3: Top Right Player Avatar
    Draw-VectorPath $pAvatar 63 43 0.75 0.75

    # Group 4: Bottom Left Player Avatar
    Draw-VectorPath $pAvatar 34 60 0.75 0.75

    # Group 5: Bottom Right Player Avatar
    Draw-VectorPath $pAvatar 56 60 0.75 0.75

    # Group 6: Vertical Divider Line
    $pLine = "M53,59 L55,59 L55,82 L53,82 Z"
    Draw-VectorPath $pLine 0 -2 1.0 1.0

    if ($isRound)
    {
        $dc.Pop()
    }

    $dc.Close()

    # Render to Bitmap
    $rtb = New-Object System.Windows.Media.Imaging.RenderTargetBitmap($targetSize, $targetSize, 96, 96, [System.Windows.Media.PixelFormats]::Pbgra32)
    $rtb.Render($visual)

    return $rtb
}

function Save-BitmapToPng($rtb, $outputPath)
{
    $dir = [System.IO.Path]::GetDirectoryName($outputPath)
    if (-not (Test-Path $dir))
    {
        New-Item -ItemType Directory -Path $dir -Force | Out-Null
    }
    $encoder = New-Object System.Windows.Media.Imaging.PngBitmapEncoder
    $encoder.Frames.Add([System.Windows.Media.Imaging.BitmapFrame]::Create($rtb))
    $stream = [System.IO.File]::Create($outputPath)
    $encoder.Save($stream)
    $stream.Close()
}

# Project Root Directory
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$rootDir = [System.IO.Path]::GetFullPath((Join-Path $scriptDir ".."))

# 1. Generate iOS 1024x1024 AppIcon
$iosBmp = Render-ExactIcon 1024 $false
Save-BitmapToPng $iosBmp "$rootDir\iosApp\iosApp\Assets.xcassets\AppIcon.appiconset\app-icon-1024.png"

# 2. Generate Android Mipmaps
$mipmaps = @{
    "mipmap-mdpi" = 48
    "mipmap-hdpi" = 72
    "mipmap-xhdpi" = 96
    "mipmap-xxhdpi" = 144
    "mipmap-xxxhdpi" = 192
}

foreach ($entry in $mipmaps.GetEnumerator())
{
    $folder = $entry.Key
    $sz = $entry.Value

    # Standard Square / Adaptive PNG
    $bmpSquare = Render-ExactIcon $sz $false
    $p1 = "$rootDir\shared\src\androidMain\res\$folder\ic_launcher.png"
    Save-BitmapToPng $bmpSquare $p1

    # Truly Circular Round Icon PNG
    $bmpRound = Render-ExactIcon $sz $true
    $p2 = "$rootDir\shared\src\androidMain\res\$folder\ic_launcher_round.png"
    Save-BitmapToPng $bmpRound $p2
}

Write-Output "SUCCESS: Generated iOS icon & Android icons (including truly circular ic_launcher_round.png)!"

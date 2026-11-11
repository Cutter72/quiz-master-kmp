Add-Type -AssemblyName PresentationCore, PresentationFramework, WindowsBase

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$rootDir = [System.IO.Path]::GetFullPath((Join-Path $scriptDir ".."))
$resDir = Join-Path $rootDir "shared\src\androidMain\res"

# Parse ic_launcher.xml to resolve background and foreground drawables
$launcherXmlPath = Join-Path $resDir "mipmap-anydpi-v26\ic_launcher.xml"
[xml]$launcherXml = Get-Content -Path $launcherXmlPath -Raw

$androidNs = "http://schemas.android.com/apk/res/android"
$bgRef = $launcherXml.'adaptive-icon'.background.GetAttribute("drawable", $androidNs)
$fgRef = $launcherXml.'adaptive-icon'.foreground.GetAttribute("drawable", $androidNs)

function Resolve-DrawableXmlPath($drawableRef)
{
    $name = ($drawableRef -replace "^@drawable/", "") + ".xml"
    $candidates = @(
        (Join-Path $resDir "drawable\$name"),
        (Join-Path $resDir "drawable-v24\$name")
    )
    foreach ($candidate in $candidates)
    {
        if (Test-Path $candidate)
        {
            return $candidate
        }
    }
    throw "Drawable XML not found for reference: $drawableRef"
}

$bgXmlPath = Resolve-DrawableXmlPath $bgRef
$fgXmlPath = Resolve-DrawableXmlPath $fgRef

[xml]$bgXml = Get-Content -Path $bgXmlPath -Raw
[xml]$fgXml = Get-Content -Path $fgXmlPath -Raw

# Extract background color from ic_launcher_background.xml
$bgColorHex = "#FFC45100"
if ($bgXml.color)
{
    $bgColorHex = $bgXml.color.GetAttribute("color", $androidNs)
}

# Extract foreground vector metadata from ic_launcher_foreground.xml
$viewportWidth = [double]$fgXml.vector.GetAttribute("viewportWidth", $androidNs)
$viewportHeight = [double]$fgXml.vector.GetAttribute("viewportHeight", $androidNs)

$groupNode = $fgXml.vector.group
$pivotX = [double]$groupNode.GetAttribute("pivotX", $androidNs)
$pivotY = [double]$groupNode.GetAttribute("pivotY", $androidNs)
$scaleX = [double]$groupNode.GetAttribute("scaleX", $androidNs)
$scaleY = [double]$groupNode.GetAttribute("scaleY", $androidNs)

$pathNode = $groupNode.path
$fgPathData = $pathNode.GetAttribute("pathData", $androidNs)
$fgColorHex = $pathNode.GetAttribute("fillColor", $androidNs)
if (-not $fgColorHex)
{
    $fgColorHex = "#FFFFFFFF"
}

function Render-ExactIcon($targetSize, $isRound = $false)
{
    $visual = New-Object System.Windows.Media.DrawingVisual
    $dc = $visual.RenderOpen()

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

    # 1. Background from ic_launcher_background.xml
    $bgColor = [System.Windows.Media.ColorConverter]::ConvertFromString($bgColorHex)
    $bgBrush = New-Object System.Windows.Media.SolidColorBrush($bgColor)
    $dc.DrawRectangle($bgBrush, $null, (New-Object System.Windows.Rect 0, 0, $targetSize, $targetSize))

    # 2. Foreground vector from ic_launcher_foreground.xml
    $fgColor = [System.Windows.Media.ColorConverter]::ConvertFromString($fgColorHex)
    $fgBrush = New-Object System.Windows.Media.SolidColorBrush($fgColor)

    $geom = [System.Windows.Media.Geometry]::Parse("F1 " + $fgPathData)

    $viewportScaleX = $targetSize / $viewportWidth
    $viewportScaleY = $targetSize / $viewportHeight

    $transformGroup = New-Object System.Windows.Media.TransformGroup
    $transformGroup.Children.Add((New-Object System.Windows.Media.ScaleTransform($scaleX, $scaleY, $pivotX, $pivotY)))
    $transformGroup.Children.Add((New-Object System.Windows.Media.ScaleTransform($viewportScaleX, $viewportScaleY)))

    $dc.PushTransform($transformGroup)
    $dc.DrawGeometry($fgBrush, $null, $geom)
    $dc.Pop()

    if ($isRound)
    {
        $dc.Pop()
    }

    $dc.Close()

    # Render to Bitmap
    $rtb = New-Object System.Windows.Media.Imaging.RenderTargetBitmap(
    $targetSize,
    $targetSize,
    96,
    96,
    [System.Windows.Media.PixelFormats]::Pbgra32
    )
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
    $p1 = "$resDir\$folder\ic_launcher.png"
    Save-BitmapToPng $bmpSquare $p1

    # Circular Round Icon PNG
    $bmpRound = Render-ExactIcon $sz $true
    $p2 = "$resDir\$folder\ic_launcher_round.png"
    Save-BitmapToPng $bmpRound $p2
}

Write-Output "SUCCESS: Generated iOS icon & Android mipmap icons from ic_launcher.xml!"

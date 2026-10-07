param(
    [Parameter(Mandatory = $true)][string]$Por,
    [string]$Valor,
    [switch]$Parcial,
    [switch]$Dump
)

$ErrorActionPreference = 'Continue'
$adb = 'C:\Users\sistemas\AppData\Local\Android\Sdk\platform-tools\adb.exe'
$xml = 'C:\Users\sistemas\AppData\Local\Temp\opencode\flow.xml'

function Dump {
    if (Test-Path $xml) { Remove-Item $xml -Force }
    for ($i = 0; $i -lt 3; $i++) {
        & $adb shell uiautomator dump /sdcard/flow.xml 2>$null | Out-Null
        & $adb pull /sdcard/flow.xml $xml 2>$null | Out-Null
        if (Test-Path $xml) {
            $c = Get-Content $xml -Raw -ErrorAction SilentlyContinue
            if ($c) { return $c }
        }
        Start-Sleep -Milliseconds 800
    }
    return ''
}

$ui = Dump
if (-not $ui) { throw 'No se pudo leer la pantalla' }

$attr = switch ($Por) {
    'texto' { 'text' }
    'desc' { 'content-desc' }
    'clase' { 'class' }
    default { 'text' }
}

if ($Parcial) {
    $pat = '<node[^>]*' + $attr + '="[^"]*' + [regex]::Escape($Valor) + '[^"]*"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"'
} else {
    $pat = '<node[^>]*' + $attr + '="' + [regex]::Escape($Valor) + '"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"'
}
$matches = [regex]::Matches($ui, $pat)

Write-Host "Coincidencias para $Por='$Valor': $($matches.Count)"

$chosen = $null
foreach ($m in $matches) { if ($m.Value -match 'clickable="true"') { $chosen = $m } }
if (-not $chosen -and $matches.Count -gt 0) { $chosen = $matches[0] }

if ($chosen) {
    $bx = [int](([int]$chosen.Groups[1].Value + [int]$chosen.Groups[3].Value) / 2)
    $by = [int](([int]$chosen.Groups[2].Value + [int]$chosen.Groups[4].Value) / 2)
    Write-Host "Tocando en $bx,$by"
    & $adb shell input tap $bx $by 2>$null | Out-Null
    Start-Sleep -Seconds 3
}

if ($Dump) {
    Write-Host ''
    Write-Host '--- pantalla resultante ---'
    $after = Dump
    [regex]::Matches($after, 'text="([^"]+)"') | ForEach-Object { $_.Groups[1].Value } |
        Where-Object { $_ -ne '' } | ForEach-Object { "   '$_'" }
}

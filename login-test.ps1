param(
    [string]$Email = 'admin@ticketapp.com',
    [string]$Pass  = 'Admin123'
)

$ErrorActionPreference = 'Continue'
$adb = 'C:\Users\sistemas\AppData\Local\Android\Sdk\platform-tools\adb.exe'
$xml = 'C:\Users\sistemas\AppData\Local\Temp\opencode\flow.xml'

function Adb([string[]]$a) { & $adb @a 2>$null | Out-Null }

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

function Texts($x) {
    if (-not $x) { return @() }
    [regex]::Matches($x, 'text="([^"]+)"') | ForEach-Object { $_.Groups[1].Value }
}

function Edits($x) {
    if (-not $x) { return @() }
    [regex]::Matches($x, '<node[^>]*class="android\.widget\.EditText"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"') | ForEach-Object {
        [pscustomobject]@{ X1 = [int]$_.Groups[1].Value; Y1 = [int]$_.Groups[2].Value; X2 = [int]$_.Groups[3].Value; Y2 = [int]$_.Groups[4].Value }
    }
}

function Center($f) { [pscustomobject]@{ X = [int](($f.X1 + $f.X2) / 2); Y = [int](($f.Y1 + $f.Y2) / 2) } }

Write-Host '== 0. reinicio limpio =='
Adb @('shell', 'pm', 'clear', 'com.ticket')
& $adb logcat -c 2>$null | Out-Null
Adb @('shell', 'am', 'start', '-n', 'com.ticket/.MainActivity')

Write-Host '== 1. espera a la pantalla de login =='
$ef = @()
for ($i = 0; $i -lt 15; $i++) {
    Start-Sleep -Seconds 2
    $ef = Edits (Dump)
    if ($ef.Count -ge 2) { Write-Host "   listo tras $($i + 1) intentos"; break }
}

# Android 13+ pide permiso de notificaciones al arrancar: hay que aceptarlo.
$ui = Dump
if ($ui -match 'send you notifications') {
    Write-Host '   aceptando el permiso de notificaciones'
    $m = [regex]::Match($ui, '<node[^>]*text="Allow"[^>]*clickable="true"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"')
    if ($m.Success) {
        $bx = [int](([int]$m.Groups[1].Value + [int]$m.Groups[3].Value) / 2)
        $by = [int](([int]$m.Groups[2].Value + [int]$m.Groups[4].Value) / 2)
        Adb @('shell', 'input', 'tap', $bx, $by)
        Start-Sleep -Seconds 2
    }
    $ef = Edits (Dump)
}

Write-Host '== 2. pantalla detectada =='
(Texts (Dump)) | Where-Object { $_ -ne '' } | ForEach-Object { "   '$_'" }

$ef = Edits (Dump)
if ($ef.Count -lt 2) { throw "Se esperaban 2 campos, hay $($ef.Count)" }

Write-Host '== 3. correo =='
$c = Center $ef[0]
Adb @('shell', 'input', 'tap', $c.X, $c.Y)
Start-Sleep -Milliseconds 1200
Adb @('shell', 'input', 'text', $Email)
Start-Sleep -Milliseconds 1200

Write-Host '== 4. contrasena =='
$ef = Edits (Dump)
$c = Center $ef[1]
Adb @('shell', 'input', 'tap', $c.X, $c.Y)
Start-Sleep -Milliseconds 1200
Adb @('shell', 'input', 'text', $Pass)
Start-Sleep -Milliseconds 1200

Write-Host '== 5. contenido de los campos =='
(Texts (Dump)) | Where-Object { $_ -ne '' } | ForEach-Object { "   '$_'" }

Write-Host '== 6. cerrar teclado =='
Adb @('shell', 'input', 'keyevent', '111')
Start-Sleep -Seconds 2

Write-Host '== 7. pulsar Iniciar Sesion =='
$ui = Dump
$matches = [regex]::Matches($ui, '<node[^>]*text="Iniciar Sesi[^"]*"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"')
$m = $null
foreach ($c in $matches) { if ($c.Value -match 'clickable="true"') { $m = $c } }
if (-not $m -and $matches.Count -gt 0) { $m = $matches[$matches.Count - 1] }
if ($m) {
    $bx = [int](([int]$m.Groups[1].Value + [int]$m.Groups[3].Value) / 2)
    $by = [int](([int]$m.Groups[2].Value + [int]$m.Groups[4].Value) / 2)
    Write-Host "   boton en $bx,$by"
    Adb @('shell', 'input', 'tap', $bx, $by)
} else { throw 'No se encontro el boton Iniciar Sesion' }

Start-Sleep -Seconds 5

Write-Host '== 8. log del servicio =='
& $adb logcat -d -s TiDBService:V

Write-Host '== 9. pantalla final =='
(Texts (Dump)) | Where-Object { $_ -ne '' } | Select-Object -First 30 | ForEach-Object { "   '$_'" }

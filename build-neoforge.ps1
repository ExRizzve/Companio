param([Parameter(ValueFromRemainingArguments = $true)][string[]]$Versions)

$root = $PSScriptRoot

if (-not $Versions) {
    $Versions = Get-ChildItem "$root\versions\*.properties" | Where-Object {
        (Get-Content $_.FullName) -match '^neoforge_version='
    } | ForEach-Object { $_.BaseName } | Sort-Object
}

$modVersion = (Select-String -Path "$root\gradle.properties" -Pattern '^mod_version=(.+)$').Matches[0].Groups[1].Value.Trim()

$dist = Join-Path $root 'dist'
New-Item -ItemType Directory -Force $dist | Out-Null

$failed = @()
foreach ($version in $Versions) {
    Write-Host "=== NeoForge $version ===" -ForegroundColor Cyan
    & "$root\gradlew.bat" -p "$root\neoforge" build --console=plain "-Pmc=$version"
    if ($LASTEXITCODE -ne 0) {
        $failed += $version
        continue
    }
    Copy-Item -LiteralPath "$root\neoforge\build\libs\companio-$modVersion+$version-neoforge.jar" -Destination $dist -Force
}

Write-Host ''
Get-ChildItem $dist -Filter "*-neoforge.jar" | Select-Object Name, Length | Format-Table -AutoSize
if ($failed.Count -gt 0) {
    Write-Host "Failed: $($failed -join ', ')" -ForegroundColor Red
    exit 1
}

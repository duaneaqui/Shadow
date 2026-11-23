# Shadow Gmail reader - one-time install (creates a private Python environment)
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot
py -m venv .venv
.\.venv\Scripts\python.exe -m pip install --upgrade pip --quiet
.\.venv\Scripts\python.exe -m pip install -r requirements.txt --quiet
$dir = Join-Path $env:LOCALAPPDATA "shadow-secrets\gmail"
New-Item -ItemType Directory -Force -Path $dir | Out-Null
Write-Host ""
Write-Host "Gmail reader installed."
Write-Host "Save your Google credentials file here as credentials.json:"
Write-Host "  $dir"

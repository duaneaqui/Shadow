# Shadow phone-inbox - one-time setup
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

# 1. Private Python environment
py -m venv .venv
.\.venv\Scripts\python.exe -m pip install --upgrade pip --quiet
.\.venv\Scripts\python.exe -m pip install -r requirements.txt --quiet

# 2. Shared secret between phone and laptop (kept in .env, never committed)
$envFile = Join-Path $PSScriptRoot ".env"
if (-not (Test-Path $envFile)) {
    $bytes = New-Object byte[] 24
    [System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
    $secret = [Convert]::ToBase64String($bytes).Replace("+","A").Replace("/","B").Replace("=","")
    Set-Content -Path $envFile -Value "SHADOW_PHONE_SECRET=$secret`nSHADOW_PHONE_PORT=8645" -Encoding UTF8
}

# 3. Start automatically at login (hidden window)
$startup = [Environment]::GetFolderPath("Startup")
$shell = New-Object -ComObject WScript.Shell
$lnk = $shell.CreateShortcut((Join-Path $startup "Shadow Phone Inbox.lnk"))
$lnk.TargetPath = (Join-Path $PSScriptRoot ".venv\Scripts\pythonw.exe")
$lnk.Arguments = "`"$(Join-Path $PSScriptRoot 'receiver.py')`""
$lnk.WorkingDirectory = $PSScriptRoot
$lnk.Save()

# 4. Start it now
Start-Process -FilePath (Join-Path $PSScriptRoot ".venv\Scripts\pythonw.exe") `
    -ArgumentList "`"$(Join-Path $PSScriptRoot 'receiver.py')`"" -WorkingDirectory $PSScriptRoot
Start-Sleep -Seconds 3

Write-Host ""
Write-Host "Phone inbox installed and running on http://127.0.0.1:8645"
Write-Host "It will start automatically when you log in."
Write-Host "Check it:  Invoke-RestMethod http://127.0.0.1:8645/health"

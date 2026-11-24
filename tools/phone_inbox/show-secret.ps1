# Shows the phone secret so you can type it into the Shadow Companion app. Don't share it.
Get-Content (Join-Path $PSScriptRoot ".env") | Select-String "SHADOW_PHONE_SECRET"

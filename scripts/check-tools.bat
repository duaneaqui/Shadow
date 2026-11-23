@echo off
rem Shadow - checks which build tools are installed. Safe: only reads versions.
cd /d "%~dp0"
set OUT=check-results.txt
echo Shadow tool check %date% %time% > %OUT%
for %%C in (python py git node npm code ollama claude codex winget) do (
  echo.>> %OUT%
  echo === %%C ===>> %OUT%
  where %%C >> %OUT% 2>&1
  call %%C --version >> %OUT% 2>&1
)
echo.>> %OUT%
echo === GPU ===>> %OUT%
nvidia-smi --query-gpu=name,memory.total,driver_version --format=csv >> %OUT% 2>&1
echo.>> %OUT%
echo === Ollama models ===>> %OUT%
ollama list >> %OUT% 2>&1
echo.>> %OUT%
echo === git config ===>> %OUT%
git config --global user.name >> %OUT% 2>&1
echo.>> %OUT%
echo Done.>> %OUT%
echo Check finished. You can close this window.
timeout /t 3 >nul

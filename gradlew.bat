@echo off
setlocal
set "GRADLE_VERSION=9.3.1"
if "%GRADLE_USER_HOME%"=="" set "GRADLE_USER_HOME=%USERPROFILE%\.gradle"
set "CACHE_ROOT=%GRADLE_USER_HOME%\manual-distributions"
set "GRADLE_DIR=%CACHE_ROOT%\gradle-%GRADLE_VERSION%"
set "GRADLE_BIN=%GRADLE_DIR%\bin\gradle.bat"
set "ZIP_PATH=%CACHE_ROOT%\gradle-%GRADLE_VERSION%-bin.zip"
set "TMP_DIR=%CACHE_ROOT%\.gradle-%GRADLE_VERSION%-extract"
set "DIST_URL=https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip"
if exist "%GRADLE_BIN%" goto run_gradle
if not exist "%CACHE_ROOT%" mkdir "%CACHE_ROOT%"
echo Gradle %GRADLE_VERSION% nao encontrado. Baixando distribuicao oficial...
where curl.exe >nul 2>nul
if %ERRORLEVEL% EQU 0 (
  curl.exe -fL --retry 3 "%DIST_URL%" -o "%ZIP_PATH%"
  if errorlevel 1 exit /b %ERRORLEVEL%
) else (
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri '%DIST_URL%' -OutFile '%ZIP_PATH%'"
  if errorlevel 1 exit /b %ERRORLEVEL%
)
if exist "%TMP_DIR%" rmdir /s /q "%TMP_DIR%"
powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -LiteralPath '%ZIP_PATH%' -DestinationPath '%TMP_DIR%' -Force"
if errorlevel 1 exit /b %ERRORLEVEL%
if exist "%GRADLE_DIR%" rmdir /s /q "%GRADLE_DIR%"
move "%TMP_DIR%\gradle-%GRADLE_VERSION%" "%GRADLE_DIR%" >nul
if errorlevel 1 exit /b %ERRORLEVEL%
if exist "%TMP_DIR%" rmdir /s /q "%TMP_DIR%"
:run_gradle
call "%GRADLE_BIN%" %*
exit /b %ERRORLEVEL%

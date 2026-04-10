@echo off
setlocal

cd /d "%~dp0"

where git >nul 2>&1
if errorlevel 1 (
    echo [ERROR] git was not found in PATH.
    exit /b 1
)

git rev-parse --is-inside-work-tree >nul 2>&1
if errorlevel 1 (
    echo [ERROR] This script must be run inside a git repository.
    exit /b 1
)

if /I "%~1"=="-h" goto usage
if /I "%~1"=="--help" goto usage

set "commit_message="
if "%~1"=="" goto prompt_message

set "commit_message=%~1"
shift

:collect_args
if "%~1"=="" goto message_ready
set "commit_message=%commit_message% %~1"
shift
goto collect_args

:prompt_message
set /p "commit_message=Commit message: "

:message_ready
if not defined commit_message (
    echo [ERROR] Commit message is required.
    exit /b 1
)

echo.
echo [INFO] Current changes:
git status --short
echo.

git add -A
if errorlevel 1 (
    echo [ERROR] Failed to stage changes.
    exit /b 1
)

git diff --cached --quiet
set "diff_exit=%errorlevel%"
if "%diff_exit%"=="0" (
    echo [INFO] There are no staged changes to commit.
    exit /b 0
)
if not "%diff_exit%"=="1" (
    echo [ERROR] Failed to inspect staged changes.
    exit /b %diff_exit%
)

git commit -m "%commit_message%"
if errorlevel 1 (
    echo [ERROR] Commit failed.
    exit /b 1
)

echo.
echo [INFO] Commit completed successfully.
git log -1 --oneline
exit /b 0

:usage
echo Usage:
echo   git-commit.bat "Your commit message"
echo.
echo If no message is provided, the script prompts for one.
exit /b 0
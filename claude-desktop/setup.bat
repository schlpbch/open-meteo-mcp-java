@echo off
REM Claude Desktop MCP Server Setup Script for Windows
REM This script configures and starts the Open-Meteo MCP Java server for Claude Desktop

setlocal enabledelayedexpansion

set SCRIPT_DIR=%~dp0
set PROJECT_ROOT=%SCRIPT_DIR%..
set CONFIG_FILE=%SCRIPT_DIR%claude-desktop.json

echo [INFO] Starting Claude Desktop MCP Server setup...

REM Check if Java is installed
java -version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java is not installed or not in PATH
    echo [INFO] Please install Java 21 or later
    exit /b 1
)

for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i version') do (
    set JAVA_VERSION=%%g
    goto :java_version_found
)
:java_version_found
echo [INFO] Java version: %JAVA_VERSION%

REM Check if Maven wrapper exists
if not exist "%PROJECT_ROOT%\mvnw.cmd" (
    where mvn >nul 2>&1
    if errorlevel 1 (
        echo [ERROR] Maven is not installed and mvnw wrapper not found
        exit /b 1
    )
)
echo [INFO] Maven check passed

REM Build the project
echo [INFO] Building Open-Meteo MCP Java server...
cd /d "%PROJECT_ROOT%"

if exist "mvnw.cmd" (
    call mvnw.cmd clean install -DskipTests
) else (
    mvn clean install -DskipTests
)

if errorlevel 1 (
    echo [ERROR] Build failed
    exit /b 1
)
echo [SUCCESS] Project built successfully

REM Install Claude Desktop configuration
set CLAUDE_CONFIG_DIR=%USERPROFILE%\AppData\Roaming\Claude
if not exist "%CLAUDE_CONFIG_DIR%" (
    mkdir "%CLAUDE_CONFIG_DIR%"
)

REM Backup existing config if it exists
if exist "%CLAUDE_CONFIG_DIR%\mcp_servers.json" (
    echo [WARNING] Backing up existing Claude Desktop configuration
    copy "%CLAUDE_CONFIG_DIR%\mcp_servers.json" "%CLAUDE_CONFIG_DIR%\mcp_servers.json.backup" >nul
)

REM Copy new configuration
copy "%CONFIG_FILE%" "%CLAUDE_CONFIG_DIR%\mcp_servers.json" >nul
echo [SUCCESS] Claude Desktop configuration installed
echo [INFO] Configuration location: %CLAUDE_CONFIG_DIR%\mcp_servers.json

REM Test the MCP server
echo [INFO] Testing MCP server startup...
cd /d "%PROJECT_ROOT%"

REM Find the JAR file
for %%f in (target\open-meteo-mcp-*.jar) do set JAR_FILE=%%f

if not exist "%JAR_FILE%" (
    echo [ERROR] JAR file not found in target directory
    exit /b 1
)

REM Start server in background for testing
start /b java -Dspring.profiles.active=dev -Dspring.main.banner-mode=off -jar "%JAR_FILE%"

REM Wait for server to start
timeout /t 10 >nul

REM Check if server is running (simplified check)
netstat -an | findstr ":8080" >nul
if errorlevel 1 (
    echo [WARNING] Could not verify server is running on port 8080
) else (
    echo [SUCCESS] MCP server appears to be running
)

echo [SUCCESS] Setup completed successfully!
echo.
echo Next steps:
echo 1. Restart Claude Desktop
echo 2. Start a new conversation
echo 3. Test with: 'What's the weather like in London?'
echo.
echo Troubleshooting:
echo - Check logs in Claude Desktop for connection issues
echo - Verify the JAR file exists in %PROJECT_ROOT%\target\
echo - Check Windows Firewall settings for port 8080

pause
@echo off
chcp 65001 >nul
title Music Project - Starting...

echo ============================================
echo        Music Project Startup Script
echo ============================================
echo.

:: Check if Java is available
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Java is not installed or not in PATH.
    echo         Please install Java 21 or later.
    pause
    exit /b 1
)

:: Check if Node.js is available
node -v >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Node.js is not installed or not in PATH.
    pause
    exit /b 1
)

set PROJECT_DIR=%~dp0

:: Create logs directory if not exists
if not exist "%PROJECT_DIR%logs" mkdir "%PROJECT_DIR%logs"

:: ---------- Start Backend (Spring Boot) ----------
echo [1/3] Starting Backend on port 8080...
start "Music-Backend" cmd /k "cd /d %PROJECT_DIR% && mvnw.cmd spring-boot:run"

:: Wait for backend to be ready
echo Waiting for backend to start...
set RETRY_COUNT=0
:WAIT_BACKEND
timeout /t 3 /nobreak >nul
curl -s -o nul -w "%%{http_code}" http://localhost:8080/ >"%PROJECT_DIR%logs\health_check.tmp" 2>nul
set /p HTTP_CODE=<"%PROJECT_DIR%logs\health_check.tmp"
if not "%HTTP_CODE%"=="" if not "%HTTP_CODE%"=="000" (
    echo Backend is ready ^(HTTP %HTTP_CODE%^)!
    goto BACKEND_DONE
)
set /a RETRY_COUNT+=1
if %RETRY_COUNT% geq 40 (
    echo [WARN] Backend did not start within 120 seconds, continuing anyway...
    goto BACKEND_DONE
)
echo   ... still waiting (%RETRY_COUNT%/40)
goto WAIT_BACKEND
:BACKEND_DONE

:: ---------- Start Frontend (Vite) ----------
echo [2/3] Starting Frontend on port 5173...
start "Music-Frontend" /min cmd /c "cd /d %PROJECT_DIR%frontend && npm run dev > %PROJECT_DIR%logs\frontend.log 2>&1"

:: Wait for frontend to be ready
echo Waiting for frontend to start...
set RETRY_COUNT=0
:WAIT_FRONTEND
timeout /t 2 /nobreak >nul
curl -s -o nul -w "%%{http_code}" http://localhost:5173/ >"%PROJECT_DIR%logs\health_check.tmp" 2>nul
set /p HTTP_CODE=<"%PROJECT_DIR%logs\health_check.tmp"
if not "%HTTP_CODE%"=="" if not "%HTTP_CODE%"=="000" (
    echo Frontend is ready!
    goto FRONTEND_DONE
)
set /a RETRY_COUNT+=1
if %RETRY_COUNT% geq 30 (
    echo [WARN] Frontend did not start within 60 seconds, continuing anyway...
    goto FRONTEND_DONE
)
echo   ... still waiting (%RETRY_COUNT%/30)
goto WAIT_FRONTEND
:FRONTEND_DONE

:: ---------- Start Admin (Vite) ----------
echo [3/3] Starting Admin on port 5174...
start "Music-Admin" /min cmd /c "cd /d %PROJECT_DIR%admin && npm run dev > %PROJECT_DIR%logs\admin.log 2>&1"
timeout /t 3 /nobreak >nul

echo.
echo ============================================
echo   All services started successfully!
echo   Backend:  http://localhost:8080
echo   Frontend: http://localhost:5173
echo   Admin:    http://localhost:5174
echo ============================================
echo.
echo Logs are saved in the "logs" folder.
echo Run "stop.bat" to stop all services.
echo.

:: Open browser after everything is ready
start http://localhost:5173

pause

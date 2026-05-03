@echo off
chcp 65001 >nul
title Music Project - Stopping...

echo ============================================
echo      Music Project Shutdown Script
echo ============================================
echo.

:: ---------- Kill by window title ----------
echo [1/3] Stopping Backend (port 8080)...
taskkill /FI "WINDOWTITLE eq Music-Backend*" /F >nul 2>&1
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8080 ^| findstr LISTENING') do (
    taskkill /PID %%a /F >nul 2>&1
)

echo [2/3] Stopping Frontend (port 5173)...
taskkill /FI "WINDOWTITLE eq Music-Frontend*" /F >nul 2>&1
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :5173 ^| findstr LISTENING') do (
    taskkill /PID %%a /F >nul 2>&1
)

echo [3/3] Stopping Admin (port 5174)...
taskkill /FI "WINDOWTITLE eq Music-Admin*" /F >nul 2>&1
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :5174 ^| findstr LISTENING') do (
    taskkill /PID %%a /F >nul 2>&1
)

:: Cleanup any remaining java processes related to Spring Boot
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8080 ^| findstr LISTENING') do (
    taskkill /PID %%a /F >nul 2>&1
)

echo.
echo ============================================
echo   All services have been stopped.
echo ============================================
echo.

pause

@echo off
echo ========================================
echo TeraBox API - Spring Boot
echo ========================================
echo.

echo Building the project...
call mvn clean package -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo Build failed!
    pause
    exit /b 1
)

echo.
echo Build successful!
echo.
echo Starting the application...
echo.

java -jar target\terabox-api-sp-1.0.0.jar

pause


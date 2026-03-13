@echo off
setlocal

REM Simple launcher for Thai ID Bridge
REM This file is optional - you can also run directly with:
REM java --add-opens java.smartcardio/sun.security.smartcardio=ALL-UNNAMED -jar target\thai-id-bridge-1.0.0.jar

set JAR=target\thai-id-bridge-1.0.0.jar

if not exist "%JAR%" (
  echo Jar not found: %JAR%
  echo Build first: mvn -DskipTests package
  exit /b 1
)

java ^
  --add-opens java.smartcardio/sun.security.smartcardio=ALL-UNNAMED ^
  -jar "%JAR%"

endlocal

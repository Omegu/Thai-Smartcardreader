@echo off
setlocal

REM Runs Thai ID Bridge with required module opens so the app can clear JDK smartcard cache after USB unplug/replug.
REM If you normally launch via an EXE wrapper or shortcut, copy these JVM args into that launch config.

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

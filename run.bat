@echo off
rem Compiles and starts SDAMS. Double-click, or run from the project folder.
if not exist out mkdir out
javac -d out src\sdams\*.java
if errorlevel 1 (
  echo.
  echo Compile failed - see the errors above.
  pause
  exit /b 1
)
java -cp out sdams.Main

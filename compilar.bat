@echo off
REM Compila y ejecuta LOC Clicker (requiere JDK 11 o superior en el PATH)
if exist out rmdir /s /q out
mkdir out
javac -encoding UTF-8 -d out ConsolesLocClicker\*.java || exit /b 1
xcopy /E /I /Y assets out\assets >nul
echo Main-Class: ConsolesLocClicker.Frame> manifest.txt
jar cfm LOCClicker.jar manifest.txt -C out .
del manifest.txt
java -jar LOCClicker.jar

#!/bin/sh
# Compila y ejecuta LOC Clicker (requiere JDK 11 o superior)
set -e
rm -rf out && mkdir out
javac -encoding UTF-8 -d out ConsolesLocClicker/*.java
cp -r assets out/
printf 'Main-Class: ConsolesLocClicker.Frame\n' > manifest.txt
jar cfm LOCClicker.jar manifest.txt -C out .
rm manifest.txt
java -jar LOCClicker.jar

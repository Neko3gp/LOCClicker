# LOC Clicker

Juego de escritorio en Java Swing sobre productividad PSP, LOC y gestión de defectos.

## Requisitos

- JDK 11 o superior
- Linux/macOS: `./compilar.sh`
- Windows: `compilar.bat`

Los scripts compilan el juego, crean el JAR y lo ejecutan. Las pruebas de balance pueden ejecutarse después de compilar:

```sh
javac -encoding UTF-8 -cp out -d out tests/BalanceTest.java
java -cp out BalanceTest
```

La prueba de interfaz `UiSmokeTest` requiere una pantalla disponible o Xvfb.

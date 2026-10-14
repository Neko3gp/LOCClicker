package tests;

import ConsolesLocClicker.audio.SoundManager;
import java.lang.reflect.Field;

public class SoundManagerTest {
    private static Object getPrivateStatic(String fieldName) throws Exception {
        Field f = SoundManager.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        return f.get(null);
    }

    public static void main(String[] args) {
        try {
            System.out.println("Iniciando pruebas de SoundManager...");
            SoundManager.init();
            Thread.sleep(500); // Dar tiempo a cargar hilos de audio

            // 1. Probar sonidos de clic
            SoundManager.playClick();
            System.out.println("PASS: playClick ejecutado");

            // 2. Probar sonido de mejora de equipo y mejora de tienda
            SoundManager.playEquipmentUpgrade();
            System.out.println("PASS: playEquipmentUpgrade ejecutado");
            SoundManager.playUpgrade();
            System.out.println("PASS: playUpgrade ejecutado");

            // 3. Probar lógica de velocidades de música
            SoundManager.setFever(false);
            SoundManager.setBugEvent(false);
            double speedNormal = (double) getPrivateStatic("targetSpeed");
            if (Math.abs(speedNormal - 1.0) > 0.001) {
                throw new AssertionError("La velocidad normal debe ser 1.0 pero fue " + speedNormal);
            }
            System.out.println("PASS: Velocidad normal = 1.0x");

            // Activar modo fiebre (1.5x)
            SoundManager.setFever(true);
            double speedFever = (double) getPrivateStatic("targetSpeed");
            if (Math.abs(speedFever - 1.5) > 0.001) {
                throw new AssertionError("La velocidad en fiebre debe ser 1.5 pero fue " + speedFever);
            }
            System.out.println("PASS: Modo fiebre = 1.5x");

            // Activar evento de bugs (modo slow 0.75x) mientras fiebre sigue activa
            SoundManager.setBugEvent(true);
            double speedBug = (double) getPrivateStatic("targetSpeed");
            if (Math.abs(speedBug - 0.75) > 0.001) {
                throw new AssertionError("La velocidad en evento de bugs debe ser 0.75 pero fue " + speedBug);
            }
            System.out.println("PASS: Evento de bugs en slow = 0.75x (prioridad sobre fiebre)");

            // Termina el evento de bugs -> debe retomar fiebre (1.5x)
            SoundManager.setBugEvent(false);
            double speedFeverResumed = (double) getPrivateStatic("targetSpeed");
            if (Math.abs(speedFeverResumed - 1.5) > 0.001) {
                throw new AssertionError("Al terminar bug, debe volver a 1.5x si fiebre sigue activa");
            }
            System.out.println("PASS: Al terminar bug, fiebre se retoma a 1.5x");

            // Termina la fiebre -> debe volver a normal (1.0x)
            SoundManager.setFever(false);
            double speedNormalResumed = (double) getPrivateStatic("targetSpeed");
            if (Math.abs(speedNormalResumed - 1.0) > 0.001) {
                throw new AssertionError("Al terminar fiebre, debe volver a 1.0x");
            }
            System.out.println("PASS: Al terminar fiebre, velocidad vuelve a 1.0x");

            // 4. Probar arranque y parada de BGM
            SoundManager.startBgm();
            Thread.sleep(600);
            boolean isRunning = (boolean) getPrivateStatic("bgmRunning");
            if (!isRunning) {
                throw new AssertionError("BGM debería estar corriendo");
            }
            System.out.println("PASS: startBgm inició el hilo de reproducción");

            SoundManager.stopBgm();
            Thread.sleep(200);
            boolean isStopped = (boolean) getPrivateStatic("bgmRunning");
            if (isStopped) {
                throw new AssertionError("BGM debería estar detenido");
            }
            System.out.println("PASS: stopBgm detuvo la música");

            System.out.println("TODAS LAS PRUEBAS DE SOUNDMANAGER COMPLETADAS CON EXITO.");
            System.exit(0);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }
}


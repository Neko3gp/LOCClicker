package ConsolesLocClicker.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;

/**
 * Gestor centralizado de audio para LOC Clicker.
 * Maneja efectos de sonido (SFX) concurrentes y música de fondo (BGM)
 * en bucle con modulación dinámica de velocidad (resampling):
 * - Velocidad normal: 1.0x
 * - Modo Fiebre (Frenesí): 1.5x
 * - Evento de resolver bugs: 0.75x (modo slow)
 */
public final class SoundManager {
    public static final double NORMAL_SPEED = 1.0;
    public static final double FEVER_SPEED  = 1.5;
    public static final double SLOW_SPEED   = 0.75;

    private static final int CLICK_POOL_SIZE = 6;
    private static final int UPGRADE_POOL_SIZE = 2;

    private static Clip[] clickClips;
    private static int clickIndex = 0;

    private static Clip[] equipmentClips;
    private static int equipmentIndex = 0;

    // Estado de la música
    private static volatile boolean bgmRunning = false;
    private static volatile boolean feverActive = false;
    private static volatile boolean bugEventActive = false;
    private static volatile double targetSpeed = NORMAL_SPEED;
    private static volatile double currentSpeed = NORMAL_SPEED;
    private static volatile float bgmVolume = 0.75f;

    // Datos PCM de la música en memoria
    private static short[] bgmLeft;
    private static short[] bgmRight;
    private static int bgmTotalFrames = 0;
    private static Thread bgmThread;
    private static boolean initialized = false;

    private SoundManager() {}

    /** Inicializa los recursos de audio de forma segura. */
    public static synchronized void init() {
        if (initialized) return;
        initialized = true;

        new Thread(() -> {
            loadSfx();
            loadBgm();
        }, "SoundManager-Init").start();
    }

    private static void loadSfx() {
        try {
            AudioFormat mono16 = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 1, 2, 44100, false);
            byte[] clickData = loadPcmBytes("single_click.wav", mono16);
            if (clickData != null) {
                clickClips = new Clip[CLICK_POOL_SIZE];
                for (int i = 0; i < CLICK_POOL_SIZE; i++) {
                    Clip c = AudioSystem.getClip();
                    c.open(new AudioInputStream(new ByteArrayInputStream(clickData), mono16, clickData.length / mono16.getFrameSize()));
                    clickClips[i] = c;
                }
            }
        } catch (Throwable t) {
            System.err.println("Aviso: No se pudo cargar single_click.wav: " + t.getMessage());
        }

        try {
            AudioFormat stereo16 = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);
            byte[] equipData = loadPcmBytes("equipment_upgrade.wav", stereo16);
            if (equipData != null) {
                equipmentClips = new Clip[UPGRADE_POOL_SIZE];
                for (int i = 0; i < UPGRADE_POOL_SIZE; i++) {
                    Clip c = AudioSystem.getClip();
                    c.open(new AudioInputStream(new ByteArrayInputStream(equipData), stereo16, equipData.length / stereo16.getFrameSize()));
                    equipmentClips[i] = c;
                }
            }
        } catch (Throwable t) {
            System.err.println("Aviso: No se pudo cargar equipment_upgrade.wav: " + t.getMessage());
        }
    }

    private static void loadBgm() {
        try {
            AudioFormat stereo16 = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);
            byte[] bgmData = loadPcmBytes("bg_chill_music.wav", stereo16);
            if (bgmData != null) {
                int totalFrames = bgmData.length / 4;
                short[] left = new short[totalFrames];
                short[] right = new short[totalFrames];
                for (int i = 0; i < totalFrames; i++) {
                    int b0 = bgmData[i * 4] & 0xFF;
                    int b1 = bgmData[i * 4 + 1];
                    left[i] = (short) ((b1 << 8) | b0);

                    int b2 = bgmData[i * 4 + 2] & 0xFF;
                    int b3 = bgmData[i * 4 + 3];
                    right[i] = (short) ((b3 << 8) | b2);
                }
                synchronized (SoundManager.class) {
                    bgmLeft = left;
                    bgmRight = right;
                    bgmTotalFrames = totalFrames;
                }
            }
        } catch (Throwable t) {
            System.err.println("Aviso: No se pudo decodificar bg_chill_music.wav: " + t.getMessage());
        }
    }

    private static byte[] loadPcmBytes(String fileName, AudioFormat targetFormat) {
        try {
            URL url = SoundManager.class.getResource("/assets/" + fileName);
            if (url == null) {
                url = SoundManager.class.getClassLoader().getResource("assets/" + fileName);
            }
            if (url == null) {
                File f = new File("assets/" + fileName);
                if (f.exists()) url = f.toURI().toURL();
            }
            if (url == null) {
                File f = new File(fileName);
                if (f.exists()) url = f.toURI().toURL();
            }
            if (url == null) {
                throw new FileNotFoundException("Recurso no encontrado: " + fileName);
            }

            try (AudioInputStream ais = AudioSystem.getAudioInputStream(url);
                 AudioInputStream conv = AudioSystem.getAudioInputStream(targetFormat, ais);
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                byte[] buf = new byte[8192];
                int r;
                while ((r = conv.read(buf)) != -1) {
                    baos.write(buf, 0, r);
                }
                return baos.toByteArray();
            }
        } catch (Throwable t) {
            System.err.println("Error leyendo " + fileName + ": " + t.getMessage());
            return null;
        }
    }

    /** Reproduce el sonido de clic sencillo usando un pool rotativo para evitar latencia o bloqueos. */
    public static void playClick() {
        if (!initialized) init();
        if (clickClips == null || clickClips.length == 0) return;
        try {
            int idx;
            synchronized (SoundManager.class) {
                idx = clickIndex;
                clickIndex = (clickIndex + 1) % clickClips.length;
            }
            Clip c = clickClips[idx];
            if (c != null) {
                if (c.isRunning()) c.stop();
                c.setFramePosition(0);
                c.start();
            }
        } catch (Throwable ignored) {}
    }

    /** Reproduce el sonido al comprar una mejora en la pestaña de equipo. */
    public static void playEquipmentUpgrade() {
        if (!initialized) init();
        if (equipmentClips == null || equipmentClips.length == 0) return;
        try {
            int idx;
            synchronized (SoundManager.class) {
                idx = equipmentIndex;
                equipmentIndex = (equipmentIndex + 1) % equipmentClips.length;
            }
            Clip c = equipmentClips[idx];
            if (c != null) {
                if (c.isRunning()) c.stop();
                c.setFramePosition(0);
                c.start();
            }
        } catch (Throwable ignored) {}
    }

    /**
     * Inicia la música de fondo en loop si no está ya reproduciéndose.
     */
    public static synchronized void startBgm() {
        if (!initialized) init();
        if (bgmRunning) return;
        bgmRunning = true;

        bgmThread = new Thread(() -> {
            // Esperar a que se carguen los datos si todavía se están leyendo
            int waitCount = 0;
            while (bgmRunning && (bgmLeft == null || bgmTotalFrames == 0) && waitCount < 50) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    return;
                }
                waitCount++;
            }
            if (!bgmRunning || bgmLeft == null || bgmTotalFrames == 0) return;

            AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            SourceDataLine line;
            try {
                line = (SourceDataLine) AudioSystem.getLine(info);
                line.open(format, 8192);
                line.start();
            } catch (Throwable t) {
                System.err.println("No se pudo abrir línea de audio para BGM: " + t.getMessage());
                return;
            }

            double cursor = 0.0;
            final int chunkSize = 512;
            byte[] outBuf = new byte[chunkSize * 4];

            try {
                while (bgmRunning) {
                    short[] left = bgmLeft;
                    short[] right = bgmRight;
                    int totalFrames = bgmTotalFrames;
                    if (left == null || totalFrames == 0) break;

                    for (int i = 0; i < chunkSize; i++) {
                        // Transición suave de velocidad (glide / portamento) para evitar chasquidos
                        currentSpeed += (targetSpeed - currentSpeed) * 0.0003;

                        int idx = (int) cursor;
                        double frac = cursor - idx;
                        int nextIdx = (idx + 1) % totalFrames;

                        short l = (short) (((1.0 - frac) * left[idx] + frac * left[nextIdx]) * bgmVolume);
                        short r = (short) (((1.0 - frac) * right[idx] + frac * right[nextIdx]) * bgmVolume);

                        outBuf[i * 4]     = (byte) (l & 0xFF);
                        outBuf[i * 4 + 1] = (byte) ((l >> 8) & 0xFF);
                        outBuf[i * 4 + 2] = (byte) (r & 0xFF);
                        outBuf[i * 4 + 3] = (byte) ((r >> 8) & 0xFF);

                        cursor += currentSpeed;
                        if (cursor >= totalFrames) cursor -= totalFrames;
                        if (cursor < 0) cursor += totalFrames;
                    }
                    line.write(outBuf, 0, outBuf.length);
                }
            } catch (Throwable t) {
                // Silencioso al detenerse
            } finally {
                try {
                    line.stop();
                    line.close();
                } catch (Throwable ignored) {}
            }
        }, "BGM-PlaybackThread");

        bgmThread.setDaemon(true);
        bgmThread.start();
    }

    /** Detiene la música de fondo. */
    public static synchronized void stopBgm() {
        bgmRunning = false;
        if (bgmThread != null) {
            bgmThread.interrupt();
            bgmThread = null;
        }
    }

    /**
     * Notifica el estado del modo de fiebre (frenesí).
     * Si no hay un bug activo, acelera a 1.5x cuando está en fiebre.
     */
    public static void setFever(boolean active) {
        feverActive = active;
        recalculateSpeed();
    }

    /**
     * Notifica si hay un evento aleatorio de solucionar bugs activo.
     * Mientras esté activo, la música pasa a modo slow (0.75x).
     * Al terminar, regresa a velocidad de fiebre si sigue activa, o a 1.0x normal.
     */
    public static void setBugEvent(boolean active) {
        bugEventActive = active;
        recalculateSpeed();
    }

    /** Calcula la velocidad objetivo según la prioridad de eventos. */
    private static void recalculateSpeed() {
        if (bugEventActive) {
            targetSpeed = SLOW_SPEED;
        } else if (feverActive) {
            targetSpeed = FEVER_SPEED;
        } else {
            targetSpeed = NORMAL_SPEED;
        }
    }

    /** Ajusta el volumen de la música de fondo (0.0f a 1.0f). */
    public static void setBgmVolume(float volume) {
        bgmVolume = Math.max(0.0f, Math.min(1.0f, volume));
    }
}


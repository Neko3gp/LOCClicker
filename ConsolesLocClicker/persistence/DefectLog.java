package ConsolesLocClicker.persistence;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Bitácora de defectos al estilo PSP (Defect Recording Log).
 * Cada defecto del juego se agrega como una fila en bitacora_defectos_psp.csv (abre en Excel).
 */
public final class DefectLog {
    public static final File FILE = new File("bitacora_defectos_psp.csv");
    private static int counter = 0;
    private DefectLog() {}

    public static synchronized void record(String type, String phaseInjected, String phaseRemoved,
                                           double fixSeconds, String description) {
        boolean isNew = !FILE.exists();
        try (PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(FILE, true), StandardCharsets.UTF_8))) {
            if (isNew) {
                w.print('\uFEFF'); // BOM para que Excel respete los acentos
                w.println("Fecha,No,Tipo,Fase_Inyeccion,Fase_Remocion,Tiempo_Correccion_s,Descripcion");
            }
            counter++;
            w.println(String.join(",",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                String.valueOf(counter),
                "\"" + type + "\"",
                "\"" + phaseInjected + "\"",
                "\"" + phaseRemoved + "\"",
                String.format(Locale.US, "%.1f", fixSeconds),
                "\"" + description.replace("\"", "'") + "\""));
        } catch (IOException e) {
            System.err.println("No se pudo escribir la bitácora: " + e.getMessage());
        }
    }
}

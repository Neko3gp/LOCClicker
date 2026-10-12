package ConsolesLocClicker.persistence;

import ConsolesLocClicker.core.Stats;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/** Persistencia local de campañas en la carpeta partidas del juego. */
public final class SaveManager {
    public static final Path DIRECTORY = Paths.get("partidas");
    private SaveManager() {}

    public static Path save(String companyName, Stats stats) throws IOException {
        Files.createDirectories(DIRECTORY);
        String safeName = sanitize(companyName);
        Path file = DIRECTORY.resolve(safeName + ".properties");
        Properties properties = new Properties();
        properties.setProperty("companyName", companyName.trim());
        properties.setProperty("formatVersion", "1");
        stats.saveProperties(properties);
        try (OutputStream out = Files.newOutputStream(file)) { properties.store(out, "LOC Clicker campaign"); }
        return file;
    }

    public static void load(String companyName, Stats stats) throws IOException {
        Path file = DIRECTORY.resolve(sanitize(companyName) + ".properties");
        Properties properties = new Properties();
        try (InputStream in = Files.newInputStream(file)) { properties.load(in); }
        stats.loadProperties(properties);
    }

    public static List<String> list() throws IOException {
        if (!Files.exists(DIRECTORY)) return Collections.emptyList();
        List<String> names = new ArrayList<>();
        try (java.util.stream.Stream<Path> files = Files.list(DIRECTORY)) {
            files.filter(path -> path.getFileName().toString().endsWith(".properties"))
                    .map(path -> path.getFileName().toString().replaceFirst("\\.properties$", ""))
                    .sorted().forEach(names::add);
        }
        return names;
    }

    public static String sanitize(String name) {
        String safe = name == null ? "" : name.trim().replaceAll("[^a-zA-Z0-9 _-]", "_");
        safe = safe.replaceAll("\\s+", "_");
        return safe.isEmpty() ? "empresa_sin_nombre" : safe.substring(0, Math.min(48, safe.length()));
    }
}

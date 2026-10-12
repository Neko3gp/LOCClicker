package ConsolesLocClicker;

/**
 * TODA la configuración del juego en un solo lugar.
 * - Carlos (Lógica de Negocio): edita los números de balanceo.
 * - Samuel (UI): edita los textos.
 * - José (Calidad): cambia los tiempos de los defectos para pruebas de estrés.
 */
public final class GameConfig {
    private GameConfig() {}

    // ===================== PRODUCTORES (nivel 1 a 5) =====================
    public static final String[] NAMES = {
        "Becario de 1er Semestre",
        "Gestor de Calidad TSP",
        "Modelo MoProSoft (Nivel 2 - Gestionado)",
        "Evaluación CMMI (Nivel 4 - Cuant. Gestionado)",
        "Certificación CMMI Nivel 5 (Optimizado)"
    };
    public static final String[] DESCRIPTIONS = {
        "Escribe código sin proceso definido. Rápido, pero sin medir nada.",
        "Rol de TSP que define el plan de calidad del equipo y vigila los defectos con datos.",
        "Modelo de Procesos para la Industria de Software: el estándar mexicano para organizaciones pequeñas.",
        "CMMI Nivel 4: los procesos se controlan con métricas y estadística.",
        "CMMI Nivel 5: mejora continua basada en datos cuantitativos. ¡La meta del proyecto!"
    };
    public static final int[]    LOC_PER_SEC = {1, 10, 50, 200, 1000};
    public static final int[]    BASE_PRICE  = {15, 100, 500, 2000, 6500};
    /** Cada unidad comprada encarece la siguiente: precio = base * GROWTH^unidades */
    public static final double   GROWTH      = 1.15;

    // ===================== MEJORA DE CLIC (teclado) =====================
    public static final String CLICK_NAME = "Teclado Mecánico";
    public static final String CLICK_DESCRIPTION = "Mejor herramienta de trabajo: +1 LOC por cada clic.";
    public static final int    CLICK_BASE_PRICE = 100;
    public static final double CLICK_GROWTH = 1.5;
    public static final int    CLICK_MAX = 8;
    public static final int    CLICK_POWER_PER_LEVEL = 1;

    // ===================== CONDICIÓN DE VICTORIA =====================
    /** Índice del productor que hay que comprar para ganar (4 = CMMI Nivel 5). */
    public static final int WIN_ITEM = 4;
    public static final int WIN_AMOUNT = 1;

    // ===================== DEFECTOS ALEATORIOS (PSP) =====================
    public static final int FIRST_DEFECT_MS = 20_000;     // primer defecto
    public static final int DEFECT_MIN_DELAY_MS = 20_000; // espera mínima entre defectos
    public static final int DEFECT_MAX_DELAY_MS = 35_000; // espera máxima entre defectos
    public static final int DEFECT_DEADLINE_MS = 5_000;   // tiempo para parchear
    public static final double DEFECT20_LOSS_PCT = 0.10;  // Tipo 20 (Sintaxis): pierdes 10% de tus LOC...
    public static final int DEFECT20_MIN_PRODUCTION_SECONDS = 10; // ...o 10 s de producción, lo que sea mayor (para que duela al inicio)
    public static final int DEFECT90_FREEZE_SECONDS = 8;  // Tipo 90 (Sistema): producción detenida N s
}

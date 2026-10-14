package ConsolesLocClicker.core;


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

    // ===================== CALIDAD Y PROGRESION =====================
    public static final String QUALITY_NAME = "Proceso de Calidad PSP";
    public static final String QUALITY_DESCRIPTION = "Invertir en calidad reduce la probabilidad de nuevos defectos.";
    public static final int QUALITY_BASE_PRICE = 250;
    public static final double QUALITY_GROWTH = 1.8;
    public static final int QUALITY_MAX = 6;
    public static final int QUALITY_DEFECT_REDUCTION_PERCENT = 12;
    public static final int BONUS_CLICKS = 25;
    public static final int BONUS_LOC = 40;
    public static final int COMPANY_FIRST_LEVEL_LOC = 1_000;
    public static final double COMPANY_LEVEL_GROWTH = 2.0;
    public static final int PLAN_LOC_PER_SECOND = 5;
    public static final int PHASE_TWO_GOAL_LOC = 50_000;
    public static final double PHASE_GOAL_GROWTH = 3.0;
    public static final double PHASE_TWO_COST_MULTIPLIER = 2.5;
    public static final double PHASE_COST_GROWTH = 1.35;
    public static final int STABILITY_START = 100;
    public static final int STABILITY_MAX = 120;
    public static final int STABILITY_MIN_FOR_PHASE = 40;
    public static final int STABILITY_DEFECT_LOSS = 15;
    public static final int STABILITY_PATCH_REWARD = 5;
    public static final int STABILITY_MINIGAME_REWARD = 15;
    public static final int DEBT_DEFECT_PENALTY = 8;
    public static final int DEBT_QUALITY_REDUCTION = 4;
    public static final int EVENT_MIN_DELAY_MS = 45_000;
    public static final int EVENT_MAX_DELAY_MS = 75_000;
    public static final int EVENT_BONUS_LOC = 250;
    public static final int EVENT_EXPENSE_BASE = 400;
    public static final int EVENT_EXPENSE_DEBT_FACTOR = 20;
    public static final int FRENZY_SECONDS = 8;
    public static final double FRENZY_PRODUCTION_MULTIPLIER = 2.0;
    public static final int FRENZY_START_CHANCE_PERCENT = 4;
    public static final int FRENZY_MIN_STABILITY = 110;
    public static final int FRENZY_COOLDOWN_SECONDS = 45;
    public static final int FRENZY_QTE_MS = 4_000;
    public static final int FRENZY_QTE_ROUNDS = 3;
    public static final int FRENZY_MISSED_COOLDOWN_SECONDS = 30;
    public static final int UNEXPECTED_MIN_DELAY_MS = 60_000;
    public static final int UNEXPECTED_MAX_DELAY_MS = 90_000;
    public static final int UNEXPECTED_FIRST_DELAY_MS = 10_000;
    public static final double UNEXPECTED_LOC_LOSS_PCT = 0.12;
    public static final int UNEXPECTED_MIN_PRODUCTION_SECONDS = 15;
    public static final int UNEXPECTED_STABILITY_LOSS = 35;
    public static final int MARKET_SHOCK_STABILITY_LOSS = 25;
    public static final double MARKET_SHOCK_LOC_LOSS_PCT = 0.08;
    public static final int PRICE_SURGE_PERCENT = 30;
    public static final int BUDGET_CUT_PRODUCTION_PERCENT = 25;
    public static final String COMPUTER_NAME = "Computadora de trabajo";
    public static final String COMPUTER_DESCRIPTION = "Mejora el rendimiento del equipo y aporta +2 LOC por clic.";
    public static final int COMPUTER_BASE_PRICE = 800;
    public static final double COMPUTER_GROWTH = 1.65;
    public static final int COMPUTER_PRODUCTION_PERCENT = 10;
    public static final int COMPUTER_CLICK_POWER = 2;
    public static final String TRAINING_NAME = "Capacitacion continua";
    public static final String TRAINING_DESCRIPTION = "Mejora la productividad de los empleados.";
    public static final int TRAINING_BASE_PRICE = 650;
    public static final double TRAINING_GROWTH = 1.7;
    public static final int TRAINING_PRODUCTION_PERCENT = 8;
    public static final String RETENTION_NAME = "Plan de retencion";
    public static final String RETENTION_DESCRIPTION = "Reduce el impacto de las crisis de personal.";
    public static final int RETENTION_BASE_PRICE = 900;
    public static final double RETENTION_GROWTH = 1.8;
    public static final int RETENTION_STABILITY_PROTECTION = 5;

    // ===================== CONDICIÓN DE VICTORIA =====================
    /** Índice del productor que hay que comprar para ganar (4 = CMMI Nivel 5). */
    public static final int WIN_ITEM = 4;
    public static final int WIN_AMOUNT = 1;

    // ===================== DEFECTOS ALEATORIOS (PSP) =====================
    public static final int FIRST_DEFECT_MS = 20_000;     // primer defecto
    public static final int DEFECT_MIN_DELAY_MS = 20_000; // espera mínima entre defectos
    public static final int DEFECT_MAX_DELAY_MS = 35_000; // espera máxima entre defectos
    public static final int DEFECT_DEADLINE_MS = 10_000;  // tiempo para leer y resolver
    public static final double DEFECT20_LOSS_PCT = 0.10;  // Tipo 20 (Sintaxis): pierdes 10% de tus LOC...
    public static final int DEFECT20_MIN_PRODUCTION_SECONDS = 10; // ...o 10 s de producción, lo que sea mayor (para que duela al inicio)
    public static final int DEFECT90_FREEZE_SECONDS = 8;  // Tipo 90 (Sistema): producción detenida N s
}

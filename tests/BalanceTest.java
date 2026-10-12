import ConsolesLocClicker.core.GameConfig;
import ConsolesLocClicker.persistence.SaveManager;
import ConsolesLocClicker.core.Stats;
import ConsolesLocClicker.events.catalog.NegativeEventCatalog;
import ConsolesLocClicker.events.catalog.RepairEventCatalog;
import java.nio.file.Files;

/**
 * Prueba de balanceo SIN interfaz (para Carlos y José).
 * Simula jugadores con distinto ritmo de clic usando las clases reales del juego
 * y reporta cuántos segundos tardan en comprar la Certificación CMMI Nivel 5.
 *
 * Compilar: javac -encoding UTF-8 -cp out -d out tests/BalanceTest.java
 * Ejecutar: java -cp out BalanceTest
 */
public class BalanceTest {
    /** estrategia: "optimo" compra lo más rentable; "casual" compra lo más caro que pueda. */
    static int segundosParaGanar(int clicsPorSegundo, String estrategia, int pierdeProduccionCadaN) {
        Stats s = new Stats();
        int t = 0;
        while (!s.isFinished() && t < 3600) {
            for (int i = 0; i < clicsPorSegundo; i++) s.click();
            comprar(s, estrategia, clicsPorSegundo);
            // defecto sin parchar: congela producción 8 s cada N segundos
            if (pierdeProduccionCadaN > 0 && t > 0 && t % pierdeProduccionCadaN == 0) s.freezeProduction(GameConfig.DEFECT90_FREEZE_SECONDS);
            s.tick();
            t++;
        }
        return t;
    }

    static void comprar(Stats s, String estrategia, int cps) {
        boolean compro = true;
        while (compro && !s.isFinished()) {
            compro = false;
            int mejor = -2; double mejorValor = -1;
            for (int i = 0; i <= GameConfig.NAMES.length; i++) {
                boolean teclado = (i == GameConfig.NAMES.length);
                int precio = teclado ? s.getKeyboard().getPrice() : s.getProducers()[i].getPrice();
                if (teclado && s.getKeyboard().getAmount() >= GameConfig.CLICK_MAX) continue;
                if (precio > s.getLoc()) continue;
                double ganancia = teclado ? cps * GameConfig.CLICK_POWER_PER_LEVEL : GameConfig.LOC_PER_SEC[i];
                double valor = estrategia.equals("casual") ? precio : ganancia / precio;
                if (i == GameConfig.WIN_ITEM) valor = Double.MAX_VALUE; // la meta siempre se compra
                if (valor > mejorValor) { mejorValor = valor; mejor = i; }
            }
            if (mejor >= 0) {
                compro = (mejor == GameConfig.NAMES.length) ? s.buyKeyboard() : s.buyProducer(mejor);
            }
        }
    }

    static String fmt(int seg) { return String.format("%d:%02d", seg / 60, seg % 60); }

    static void comprobarNuevasMecanicas() {
        Stats s = new Stats();
        for (int i = 0; i < GameConfig.BONUS_CLICKS; i++) s.click();
        if (s.getBonusesAwarded() != 1 || s.getBonusLoc() != GameConfig.BONUS_LOC) {
            throw new AssertionError("el bonus por racha no se otorgo correctamente");
        }
        s.setLoc(10_000);
        if (!s.buyQuality() || s.getDefectChancePercent() != 100 - GameConfig.QUALITY_DEFECT_REDUCTION_PERCENT) {
            throw new AssertionError("la inversion en calidad no reduce el riesgo de defectos");
        }
        s.setLocMade(GameConfig.COMPANY_FIRST_LEVEL_LOC);
        if (s.getCompanyLevel() != 2 || s.getCompanyProgressPercent() != 0) {
            throw new AssertionError("la progresion de empresa no escala correctamente");
        }
        s.getProducers()[GameConfig.WIN_ITEM].setAmount(GameConfig.WIN_AMOUNT);
        if (!s.isPhaseComplete()) throw new AssertionError("CMMI no completa la fase 1");
        s.advancePhase();
        if (s.getPhase() != 2 || s.getPhaseTargetLoc() <= s.getPhaseStartLoc()) {
            throw new AssertionError("la fase 2 no crea un objetivo creciente");
        }
        if (s.getPlannedLoc(10) != 10 * GameConfig.PLAN_LOC_PER_SECOND) {
            throw new AssertionError("el plan acumulado no coincide con el ritmo configurado");
        }
        Stats stability = new Stats();
        stability.getProducers()[3].setAmount(1);
        int normalProduction = stability.getEffectiveProduction();
        stability.damageStability(60);
        if (stability.getEffectiveProduction() >= normalProduction) {
            throw new AssertionError("la estabilidad baja no reduce la produccion");
        }
        stability.recoverStability(80);
        if (stability.getStability() <= GameConfig.STABILITY_START
                || stability.getEffectiveProduction() <= normalProduction) {
            throw new AssertionError("la estabilidad alta no mejora la produccion");
        }
        Stats incident = new Stats();
        incident.setLoc(10_000);
        int beforeIncident = incident.getLoc();
        int lost = incident.applyUnexpectedEvent();
        if (lost <= 0 || incident.getLoc() >= beforeIncident
                || incident.getStability() != GameConfig.STABILITY_START - GameConfig.UNEXPECTED_STABILITY_LOSS
                || incident.getTechnicalDebt() != GameConfig.DEBT_DEFECT_PENALTY) {
            throw new AssertionError("el evento inesperado no aplica su penalizacion completa");
        }
        if (RepairEventCatalog.size() < 2 || NegativeEventCatalog.size() < 3) {
            throw new AssertionError("los catalogos de eventos no contienen las opciones esperadas");
        }
        int originalPrice = incident.getPrice(incident.getKeyboard());
        incident.applyPriceSurge();
        if (incident.getPrice(incident.getKeyboard()) <= originalPrice) {
            throw new AssertionError("la subida de precios no afecta la tienda");
        }
        incident.getProducers()[0].setAmount(1);
        int productionBeforeCut = incident.getEffectiveProduction();
        incident.applyBudgetCut();
        if (incident.getEffectiveProduction() >= productionBeforeCut) {
            throw new AssertionError("el recorte presupuestario no afecta la produccion");
        }
        System.out.println("MECANICAS NUEVAS: OK");
    }

    static void comprobarGuardado() throws Exception {
        Stats original = new Stats();
        original.setLoc(1234);
        original.setLocMade(5678);
        original.setRawClicks(42);
        original.getQuality().setAmount(2);
        SaveManager.save("Prueba balance", original);
        Stats loaded = new Stats();
        SaveManager.load("Prueba balance", loaded);
        if (loaded.getLoc() != 1234 || loaded.getLocMade() != 5678
                || loaded.getRawClicks() != 42 || loaded.getQuality().getAmount() != 2) {
            throw new AssertionError("el guardado no restaura el estado completo");
        }
        Files.deleteIfExists(SaveManager.DIRECTORY.resolve("Prueba_balance.properties"));
        try (java.util.stream.Stream<java.nio.file.Path> files = Files.list(SaveManager.DIRECTORY)) {
            if (!files.findAny().isPresent()) Files.deleteIfExists(SaveManager.DIRECTORY);
        }
        System.out.println("GUARDADO: OK");
    }

    public static void main(String[] args) {
        comprobarNuevasMecanicas();
        try {
            comprobarGuardado();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("Tiempo para ganar (min:seg)  - objetivo: entre 3:00 y 5:00");
        System.out.println("estrategia  clics/s   normal   ignora la mitad de defectos   ignora TODOS (solo informativo)");
        boolean ok = true;
        for (String est : new String[]{"optimo", "casual"}) {
            for (int cps : new int[]{3, 5, 7}) {
                int normal = segundosParaGanar(cps, est, 0);
                int mitad = segundosParaGanar(cps, est, 60);   // 8 s congelado cada 60 s ~ ignora 1 de cada 2 defectos
                int todos = segundosParaGanar(cps, est, 30);   // 8 s congelado cada 30 s ~ ignora todos
                System.out.printf("%-10s  %d         %s     %s                         %s%n", est, cps, fmt(normal), fmt(mitad), fmt(todos));
                if (normal < 180 || mitad > 300) ok = false;
            }
        }
        System.out.println(ok ? "RESULTADO: OK, todos los escenarios dentro de 3 a 5 min"
                              : "RESULTADO: FALLA, hay escenarios fuera del rango");
        System.exit(ok ? 0 : 1);
    }
}

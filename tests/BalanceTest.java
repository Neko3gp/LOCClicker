import ConsolesLocClicker.GameConfig;
import ConsolesLocClicker.Stats;

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

    public static void main(String[] args) {
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

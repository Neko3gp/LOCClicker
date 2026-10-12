package ConsolesLocClicker.events.catalog;

import ConsolesLocClicker.core.Stats;

/** Recorte presupuestario: reduce el rendimiento de todos los equipos. */
public class BudgetCutEvent implements NegativeEvent {
    @Override public String title() { return "Recorte presupuestario"; }
    @Override public String description() {
        return "La direccion congelo parte del presupuesto operativo de la empresa.";
    }
    @Override public String apply(Stats stats) {
        stats.applyBudgetCut();
        return "Produccion reducida un 25% y estabilidad reducida.";
    }
}

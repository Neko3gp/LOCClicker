package ConsolesLocClicker.events.catalog;

import ConsolesLocClicker.core.Stats;

/** Rotacion forzada por crecimiento: el empleado de mayor nivel abandona el equipo. */
public class EmployeeTurnoverEvent implements NegativeEvent {
    @Override public String title() { return "Rotacion de personal"; }
    @Override public String description() {
        return "El equipo crecio demasiado rapido: una persona clave renuncio y hay que reorganizarse.";
    }
    @Override public String apply(Stats stats) {
        return "Salio: " + stats.applyEmployeeTurnover() + ". La estabilidad baja y se pierde produccion.";
    }
}

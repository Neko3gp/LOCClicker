package ConsolesLocClicker.events.catalog;

import ConsolesLocClicker.core.Stats;


/** Evento inevitable que aplica una consecuencia operativa sin minijuego. */
public interface NegativeEvent {
    String title();
    String description();
    String apply(Stats stats);
}

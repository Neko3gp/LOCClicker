package ConsolesLocClicker.events.catalog;

import javax.swing.JFrame;
import ConsolesLocClicker.core.Stats;
import ConsolesLocClicker.minigames.StabilityMinigame;

/** Evento de reparacion que pide estabilizar el build con un reto rapido. */
public class StabilityRepairEvent implements RepairEvent {
    @Override public String name() { return "Estabilizacion del build"; }
    @Override public void open(JFrame owner, Stats stats, String description, int line,
                               Runnable onChange, Runnable onSuccess, Runnable onFailure) {
        StabilityMinigame.show(owner, stats, onChange, onSuccess, onFailure);
    }
}

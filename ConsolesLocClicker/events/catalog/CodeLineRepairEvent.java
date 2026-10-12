package ConsolesLocClicker.events.catalog;

import javax.swing.JFrame;
import ConsolesLocClicker.core.Stats;
import ConsolesLocClicker.minigames.CodeFixMinigame;

/** Evento de reparacion que pide seleccionar la linea exacta. */
public class CodeLineRepairEvent implements RepairEvent {
    @Override public String name() { return "Correccion de linea"; }
    @Override public void open(JFrame owner, Stats stats, String description, int line,
                               Runnable onChange, Runnable onSuccess, Runnable onFailure) {
        CodeFixMinigame.show(owner, stats, description, line, onChange, onSuccess, onFailure);
    }
}

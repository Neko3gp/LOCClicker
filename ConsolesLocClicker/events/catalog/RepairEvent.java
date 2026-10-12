package ConsolesLocClicker.events.catalog;

import ConsolesLocClicker.core.Stats;

import javax.swing.JFrame;

/** Contrato para cualquier minijuego que pueda resolver un defecto. */
public interface RepairEvent {
    String name();
    void open(JFrame owner, Stats stats, String description, int line,
              Runnable onChange, Runnable onSuccess, Runnable onFailure);
}

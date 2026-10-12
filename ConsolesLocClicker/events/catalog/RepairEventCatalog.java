package ConsolesLocClicker.events.catalog;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/** Registro central de minijuegos disponibles para resolver defectos. */
public final class RepairEventCatalog {
    private static final List<RepairEvent> EVENTS = Arrays.asList(
            new CodeLineRepairEvent(),
            new StabilityRepairEvent());

    private RepairEventCatalog() {}

    public static RepairEvent random(Random random) {
        return EVENTS.get(random.nextInt(EVENTS.size()));
    }

    public static int size() { return EVENTS.size(); }
}

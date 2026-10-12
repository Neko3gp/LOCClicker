package ConsolesLocClicker.events.catalog;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/** Registro de consecuencias inevitables que pueden aparecer durante la escala. */
public final class NegativeEventCatalog {
    private static final List<NegativeEvent> EVENTS = Arrays.asList(
            new MarketCrashEvent(),
            new PriceSurgeEvent(),
            new BudgetCutEvent(),
            new EmployeeTurnoverEvent());

    private NegativeEventCatalog() {}

    public static NegativeEvent random(Random random) {
        return EVENTS.get(random.nextInt(EVENTS.size()));
    }

    public static int size() { return EVENTS.size(); }
}

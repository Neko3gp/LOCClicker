package ConsolesLocClicker.events.catalog;

import ConsolesLocClicker.core.Stats;

/** Caida de mercado: quema LOC acumuladas y estabilidad. */
public class MarketCrashEvent implements NegativeEvent {
    @Override public String title() { return "Caida del mercado"; }
    @Override public String description() {
        return "Un cliente importante cancelo su contrato y el mercado se contrajo.";
    }
    @Override public String apply(Stats stats) {
        return "LOC perdidas: " + stats.applyMarketShock()
                + "; estabilidad reducida por el desplome del mercado.";
    }
}

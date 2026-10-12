package ConsolesLocClicker.events.catalog;

import ConsolesLocClicker.core.Stats;

/** Inflacion de proveedores: encarece las compras para el resto de la campaña. */
public class PriceSurgeEvent implements NegativeEvent {
    @Override public String title() { return "Subida de precios"; }
    @Override public String description() {
        return "Los proveedores aumentaron sus tarifas y el presupuesto ya no alcanza igual.";
    }
    @Override public String apply(Stats stats) {
        stats.applyPriceSurge();
        return "Compras encarecidas un 30% y estabilidad reducida.";
    }
}

package ConsolesLocClicker;

/** Una mejora comprable. El precio crece de forma geométrica: base * growth^cantidad. */
public class Item {
    private final String name;
    private final String description;
    private final int locPerSecond;
    private final int basePrice;
    private final double growth;
    private int amount;

    public Item(String name, String description, int locPerSecond, int basePrice, double growth) {
        this.name = name;
        this.description = description;
        this.locPerSecond = locPerSecond;
        this.basePrice = basePrice;
        this.growth = growth;
        this.amount = 0;
    }
    public String getName() {return name;}
    public String getDescription() {return description;}
    public int getLocPerSecond() {return locPerSecond;}
    public int getAmount() {return amount;}
    public void setAmount(int amount) {this.amount = Math.max(0, amount);}
    public int getPrice() {return (int) Math.ceil(basePrice * Math.pow(growth, amount));}
    /** LOC/s que aporta este productor con la cantidad actual. */
    public int getProduction() {return locPerSecond * amount;}
}

package flc.model;

/**
 * Represents a type of group exercise offered at the leisure centre.
 * Price is fixed per exercise type regardless of time.
 */
public class Exercise {
    private String name;
    private double price;
    private String description;

    public Exercise(String name, double price, String description) {
        this.name = name;
        this.price = price;
        this.description = description;
    }

    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return name + " (£" + String.format("%.2f", price) + ")";
    }
}

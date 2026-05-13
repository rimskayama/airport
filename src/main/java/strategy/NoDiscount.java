package strategy;

public class NoDiscount implements DiscountStrategy {
    @Override
    public String getName() { return "Без скидки"; }

    @Override
    public double applyDiscount(double price) { return price; }
}

package strategy;

public class SeniorDiscount implements DiscountStrategy {
    @Override
    public String getName() { return "Пенсионер (15%)"; }

    @Override
    public double applyDiscount(double price) { return price * 0.85; }
}

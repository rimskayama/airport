package strategy;

public class StudentDiscount implements DiscountStrategy {
    @Override
    public String getName() { return "Студенческая (10%)"; }

    @Override
    public double applyDiscount(double price) { return price * 0.9; }
}

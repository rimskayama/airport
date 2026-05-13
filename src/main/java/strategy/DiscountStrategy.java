package strategy;

public interface DiscountStrategy {
    String getName();
    double applyDiscount(double basePrice);
}
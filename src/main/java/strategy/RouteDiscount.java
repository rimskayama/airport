package strategy;

public class RouteDiscount implements DiscountStrategy {
    private final double percent;

    // установить процент при создании тарифа
    public RouteDiscount(double percent) {
        this.percent = percent;
    }

    @Override
    public String getName() {
        return "Скидка направления (" + percent + "%)";
    }

    @Override
    public double applyDiscount(double price) {
        return price * (1 - percent/100);
    }
    
    public double getPercent() { return percent; }
}

package entity;

import strategy.DiscountStrategy;
import strategy.NoDiscount;
import strategy.RouteDiscount;

public class Fare {
    private int id;
    private String fromLocation;
    private String toLocation;
    private double price;
    private FareClass classChoice;
    private DiscountStrategy routeDiscount;

    public Fare(
            String fromLocation, String toLocation, double price,
            FareClass classChoice, DiscountStrategy routeDiscount) {
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
        this.price = price;
        this.classChoice = classChoice;
        this.routeDiscount = routeDiscount;
    }

    public int getId() { return id; }
    public String fromLocation() { return fromLocation; }
    public String toLocation() { return toLocation; }
    public double getPrice() { return price; }
    public FareClass getClassChoice() { return classChoice; }

    public void setId(int id) { this.id = id; }

    public String getClassName() {
        return classChoice.getName();
    }

    public double getPriceWithRouteDiscount() {
        return routeDiscount.applyDiscount(price);
    }

    public int getRouteDiscountPercent() {
        if (routeDiscount instanceof RouteDiscount) {
            return (int) ((RouteDiscount) routeDiscount).getPercent();
        }
        return 0;
    }

    @Override
    public String toString() {
        int percent = getRouteDiscountPercent();

        String discountInfo = percent > 0 ?
                " | Скидка: " + percent + "%" : "";

        return fromLocation() + " → " + toLocation() +
                " | Класс: " + getClassName() +
                " | Цена: " + getPrice() + " руб." +
                discountInfo;
    }

    public static DiscountStrategy createDiscountStrategy(int percent) {
        return (percent > 0) ? new RouteDiscount(percent) : new NoDiscount();
    }

}
package org.example;

public class Order {
    Operation operation;
    int quantity;
    double price;

    public Order(Operation operation, int quantity, double price) {
        this.operation = operation;
        this.quantity = quantity;
        this.price = price;
    }
}

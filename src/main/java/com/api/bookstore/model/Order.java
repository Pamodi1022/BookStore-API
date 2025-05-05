package com.api.bookstore.model;

import java.util.Map;

public class Order {
    private int id;
    private int customerId;
    private Map<Integer, Integer> items; // bookId -> quantity

    public Order() {
    }

    public Order(int id, int customerId, Map<Integer, Integer> items) {
        this.id = id;
        this.customerId = customerId;
        this.items = items;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public Map<Integer, Integer> getItems() {
        return items;
    }

    public void setItems(Map<Integer, Integer> items) {
        this.items = items;
    }
}
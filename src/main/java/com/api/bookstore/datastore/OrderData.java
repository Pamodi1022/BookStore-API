package com.api.bookstore.datastore;

import com.api.bookstore.exception.BookNotFoundException;
import com.api.bookstore.exception.CartNotFoundException;
import com.api.bookstore.exception.InvalidInputException;
import com.api.bookstore.exception.OutOfStockException;
import com.api.bookstore.exception.OrderNotFoundException;
import com.api.bookstore.model.Cart;
import com.api.bookstore.model.CartItem;
import com.api.bookstore.model.Order;
import com.api.bookstore.model.Book;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class OrderData {
    private static final Map<Integer, Order> orders = new HashMap<>();
    private static final AtomicInteger idCounter = new AtomicInteger(1);

    public Order createOrder(int customerId, List<Integer> bookIds) throws OutOfStockException {
        // Check if customer has a cart
        Cart cart = CartData.findCartByCustomerId(customerId);
        if (cart == null) {
            throw new CartNotFoundException("Cart not found for customer ID: " + customerId);
        }

        // Create order with multiple items
        Map<Integer, Integer> orderItems = new HashMap<>();
        List<CartItem> remainingItems = new ArrayList<>(cart.getItems());
        
        for (Integer bookId : bookIds) {
            // Check if book exists in cart
            CartItem cartItem = null;
            for (CartItem item : remainingItems) {
                if (item.getBookId() == bookId) {
                    cartItem = item;
                    break;
                }
            }

            if (cartItem == null) {
                throw new BookNotFoundException("Book with ID " + bookId + " not found in cart");
            }

            // Add to order items
            orderItems.put(bookId, cartItem.getQuantity());
            // Remove from remaining items
            remainingItems.remove(cartItem);
        }

        // Create and save order
        int orderId = idCounter.getAndIncrement();
        Order order = new Order(orderId, customerId, orderItems);
        orders.put(orderId, order);

        // Update cart with remaining items
        if (remainingItems.isEmpty()) {
            CartData.deleteCart(customerId);
        } else {
            cart.setItems(remainingItems);
            CartData.updateCart(cart);
        }

        return order;
    }

    public Order getOrder(int orderId) {
        return orders.get(orderId);
    }

    public List<Order> getCustomerOrders(int customerId) {
        List<Order> customerOrders = new ArrayList<>();
        for (Order order : orders.values()) {
            if (order.getCustomerId() == customerId) {
                customerOrders.add(order);
            }
        }
        return customerOrders;
    }

    public void deleteOrder(int orderId) {
        orders.remove(orderId);
    }

    public boolean exists(int orderId) {
        return orders.containsKey(orderId);
    }

    public Order updateOrder(int orderId, Map<Integer, Integer> items) throws OutOfStockException {
        Order order = orders.get(orderId);
        if (order == null) {
            throw new OrderNotFoundException("Order not found with id: " + orderId);
        }

        // Validate all books exist and have sufficient stock
        for (Map.Entry<Integer, Integer> entry : items.entrySet()) {
            int bookId = entry.getKey();
            int quantity = entry.getValue();

            if (quantity <= 0) {
                throw new InvalidInputException("Quantity must be positive for book ID: " + bookId);
            }

            Book book = BookData.findBookById(bookId);
            if (book == null) {
                throw new BookNotFoundException("Book with ID " + bookId + " not found");
            }

            if (quantity > book.getStock()) {
                throw new OutOfStockException("Insufficient stock for book ID " + bookId + 
                    ". Available: " + book.getStock() + ", Requested: " + quantity);
            }
        }

        // Update order items
        order.setItems(items);
        orders.put(orderId, order);
        return order;
    }
}
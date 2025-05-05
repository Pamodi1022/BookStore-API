package com.api.bookstore.model;

import com.api.bookstore.datastore.BookData;
import com.api.bookstore.exception.InvalidInputException;
import com.api.bookstore.exception.OutOfStockException;

public class CartItem {
    private int bookId;
    private int quantity;
    private double price; // Store the price at the time of adding to cart

    // Default constructor
    public CartItem() {
    }

    // Constructor with bookId and quantity
    public CartItem(int bookId, int quantity) {
        this.setBookId(bookId); // Use setter for validation
        this.setQuantity(quantity); // Use setter for validation
    }

    // Getters and setters
    public int getBookId() {
        return bookId;
    }

    public final void setBookId(int bookId) {
        if (bookId <= 0) {
            throw new InvalidInputException("Book ID must be positive");
        }
        // Verify book exists
        Book book = BookData.findBookById(bookId);
        if (book == null) {
            throw new InvalidInputException("Book with ID " + bookId + " does not exist");
        }
        this.bookId = bookId;
        this.price = book.getPrice(); // Update price when book ID changes
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity <= 0) {
            throw new InvalidInputException("Quantity must be positive");
        }
        
        // Get current book and its stock
        Book book = BookData.findBookById(this.bookId);
        if (book == null) {
            throw new InvalidInputException("Book with ID " + bookId + " does not exist");
        }
        
        int currentStock = book.getStock();
        if (quantity > currentStock) {
            throw new OutOfStockException("Insufficient stock for book ID " + bookId + 
                ". Available: " + currentStock + ", Requested: " + quantity);
        }
        
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        if (price < 0) {
            throw new InvalidInputException("Price cannot be negative");
        }
        this.price = price;
    }

    // Calculate subtotal for this item
    public double getSubtotal() {
        return price * quantity;
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "bookId=" + bookId +
                ", quantity=" + quantity +
                ", price=" + price +
                ", subtotal=" + getSubtotal() +
                '}';
    }

    // Equals and hashCode for proper collection handling
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItem cartItem = (CartItem) o;
        return bookId == cartItem.bookId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(bookId);
    }
}
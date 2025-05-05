package com.api.bookstore.datastore;

import com.api.bookstore.exception.InvalidInputException;
import com.api.bookstore.model.Cart;

import java.util.HashMap;
import java.util.Map;

public class CartData {
    private static final Map<Integer, Cart> carts = new HashMap<>();
    
    // --- Cart methods ---
    public static Cart addCart(Cart cart) {
        // Validate customer exists
        if (CustomerData.findCustomerById(cart.getCustomerId()) == null) {
            throw new InvalidInputException("Cannot create cart: customer with ID " + cart.getCustomerId() + " does not exist.");
        }
        carts.put(cart.getCustomerId(), cart);
        return cart;
    }
    
    public static Cart findCartByCustomerId(int customerId) {
        return carts.get(customerId);
    }
    
    public static void deleteCart(int customerId) {
        carts.remove(customerId);
    }
    
    // Add this method to update an existing cart
    public static Cart updateCart(Cart cart) {
        if (cart == null) {
            throw new InvalidInputException("Cannot update null cart");
        }
        if (carts.get(cart.getCustomerId()) == null) {
            throw new InvalidInputException("Cannot update cart: cart for customer ID " + cart.getCustomerId() + " does not exist.");
        }
        carts.put(cart.getCustomerId(), cart);
        return cart;
    }
}




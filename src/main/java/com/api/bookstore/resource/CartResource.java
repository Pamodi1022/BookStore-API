package com.api.bookstore.resource;

import com.api.bookstore.datastore.BookData;
import com.api.bookstore.datastore.CartData;
import com.api.bookstore.datastore.CustomerData;
import com.api.bookstore.exception.BookNotFoundException;
import com.api.bookstore.exception.CartNotFoundException;
import com.api.bookstore.exception.CustomerNotFoundException;
import com.api.bookstore.exception.InvalidInputException;
import com.api.bookstore.exception.OutOfStockException;
import com.api.bookstore.model.Cart;
import com.api.bookstore.model.CartItem;
import com.api.bookstore.model.Book;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/customers/{customerId}/cart")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CartResource {

    private void validateCustomerId(int customerId) {
        if (customerId <= 0) {
            throw new InvalidInputException("Customer ID must be a positive integer");
        }
        if (CustomerData.findCustomerById(customerId) == null) {
            throw new CustomerNotFoundException("Customer with ID " + customerId + " not found");
        }
    }

    private void validateCartItem(CartItem item) {
        if (item == null) {
            throw new InvalidInputException("Cart item cannot be null");
        }
        if (item.getBookId() <= 0) {
            throw new InvalidInputException("Book ID must be a positive integer");
        }
        if (item.getQuantity() <= 0) {
            throw new InvalidInputException("Quantity must be a positive integer");
        }
        
        // Check if book exists
        if (BookData.findBookById(item.getBookId()) == null) {
            throw new BookNotFoundException("Book with ID " + item.getBookId() + " not found");
        }
    }

    @GET
    public Response getCart(@PathParam("customerId") int customerId) {
        validateCustomerId(customerId);
        
        Cart cart = CartData.findCartByCustomerId(customerId);
        if (cart == null) {
            throw new CartNotFoundException("Cart not found for customer ID: " + customerId);
        }
        return Response.ok(cart).build();
    }

    @POST
    @Path("/items")
    public Response addItemToCart(@PathParam("customerId") int customerId, CartItem item) {
        validateCustomerId(customerId);
        validateCartItem(item);

        // Check if book exists
        Book book = BookData.findBookById(item.getBookId());
        if (book == null) {
            throw new BookNotFoundException("Book with ID " + item.getBookId() + " not found");
        }

        Cart cart = CartData.findCartByCustomerId(customerId);
        if (cart == null) {
            // Create new cart if it doesn't exist
            cart = new Cart(customerId);
            CartData.addCart(cart);
        }

        List<CartItem> items = cart.getItems();
        
        // Check if the book already exists in cart
        boolean itemExists = false;
        for (CartItem cartItem : items) {
            if (cartItem.getBookId() == item.getBookId()) {
                // Get current stock
                int currentStock = book.getStock();
                int totalQuantity = cartItem.getQuantity() + item.getQuantity();
                
                if (totalQuantity > currentStock) {
                    throw new OutOfStockException("Insufficient stock for book ID " + item.getBookId() + 
                        ". Available: " + currentStock + ", Total requested: " + totalQuantity);
                }
                
                // Update stock and quantity
                BookData.updateStock(item.getBookId(), currentStock - item.getQuantity());
                cartItem.setQuantity(totalQuantity);
                itemExists = true;
                break;
            }
        }
        
        // Add new item if it doesn't exist
        if (!itemExists) {
            // Check stock for new item
            int currentStock = book.getStock();
            if (item.getQuantity() > currentStock) {
                throw new OutOfStockException("Insufficient stock for book ID " + item.getBookId() + 
                    ". Available: " + currentStock + ", Requested: " + item.getQuantity());
            }
            
            // Update stock and add item
            BookData.updateStock(item.getBookId(), currentStock - item.getQuantity());
            items.add(item);
        }
        
        cart.setItems(items);
        CartData.updateCart(cart);
        return Response.ok(cart).build();
    }

    @PUT
    @Path("/items/{bookId}")
    public Response updateCartItemQuantity(
            @PathParam("customerId") int customerId,
            @PathParam("bookId") int bookId,
            CartItem updatedItem) {
        
        validateCustomerId(customerId);
        if (bookId <= 0) {
            throw new InvalidInputException("Book ID must be a positive integer");
        }
        if (updatedItem == null) {
            throw new InvalidInputException("Cart item cannot be null");
        }
        if (updatedItem.getQuantity() <= 0) {
            throw new InvalidInputException("Quantity must be a positive integer");
        }

        Cart cart = CartData.findCartByCustomerId(customerId);
        if (cart == null) {
            throw new CartNotFoundException("Cart not found for customer ID: " + customerId);
        }

        // Check if book exists
        Book book = BookData.findBookById(bookId);
        if (book == null) {
            throw new BookNotFoundException("Book with ID " + bookId + " not found");
        }

        List<CartItem> items = cart.getItems();
        boolean itemFound = false;

        for (CartItem item : items) {
            if (item.getBookId() == bookId) {
                // Get current stock
                int currentStock = book.getStock();
                int quantityDifference = updatedItem.getQuantity() - item.getQuantity();
                
                if (quantityDifference > currentStock) {
                    throw new OutOfStockException("Insufficient stock for book ID " + bookId + 
                        ". Available: " + currentStock + ", Additional requested: " + quantityDifference);
                }
                
                // Update stock and quantity
                BookData.updateStock(bookId, currentStock - quantityDifference);
                item.setQuantity(updatedItem.getQuantity());
                itemFound = true;
                break;
            }
        }

        if (!itemFound) {
            throw new InvalidInputException("Book with ID " + bookId + " not found in cart");
        }

        cart.setItems(items);
        CartData.updateCart(cart);
        return Response.ok(cart).build();
    }

    @DELETE
    @Path("/items/{bookId}")
    public Response removeItemFromCart(
            @PathParam("customerId") int customerId,
            @PathParam("bookId") int bookId) {
        
        validateCustomerId(customerId);
        if (bookId <= 0) {
            throw new InvalidInputException("Book ID must be a positive integer");
        }
        
        Cart cart = CartData.findCartByCustomerId(customerId);
        if (cart == null) {
            throw new CartNotFoundException("Cart not found for customer ID: " + customerId);
        }

        List<CartItem> items = cart.getItems();
        CartItem removedItem = null;
        for (CartItem item : items) {
            if (item.getBookId() == bookId) {
                removedItem = item;
                break;
            }
        }

        if (removedItem == null) {
            throw new InvalidInputException("Book with ID " + bookId + " not found in cart");
        }

        // Restore stock
        Book book = BookData.findBookById(bookId);
        int currentStock = book.getStock();
        BookData.updateStock(bookId, currentStock + removedItem.getQuantity());

        // Remove item from cart
        items.remove(removedItem);
        cart.setItems(items);
        CartData.updateCart(cart);
        return Response.ok(cart).build();
    }
}





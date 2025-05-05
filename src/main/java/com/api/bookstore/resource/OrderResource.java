package com.api.bookstore.resource;

import com.api.bookstore.datastore.CustomerData;
import com.api.bookstore.datastore.OrderData;
import com.api.bookstore.exception.CustomerNotFoundException;
import com.api.bookstore.exception.InvalidInputException;
import com.api.bookstore.exception.OrderNotFoundException;
import com.api.bookstore.exception.OutOfStockException;
import com.api.bookstore.model.Order;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Path("/customers/{customerId}/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderResource {
    private final OrderData orderData = new OrderData();

    private void validateCustomerId(int customerId) {
        if (customerId <= 0) {
            throw new InvalidInputException("Customer ID must be a positive integer");
        }
        if (CustomerData.findCustomerById(customerId) == null) {
            throw new CustomerNotFoundException("Customer with ID " + customerId + " not found");
        }
    }

    @POST
    public Response createOrder(@PathParam("customerId") int customerId, OrderRequest request) {
        validateCustomerId(customerId);
        
        if (request == null || request.getBookIds() == null || request.getBookIds().isEmpty()) {
            throw new InvalidInputException("At least one book ID must be provided");
        }
        
        try {
            Order order = orderData.createOrder(customerId, request.getBookIds());
            return Response.status(Response.Status.CREATED).entity(order).build();
        } catch (OutOfStockException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Out of Stock");
            error.put("message", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(error)
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
    }

    @GET
    public Response getCustomerOrders(@PathParam("customerId") int customerId) {
        validateCustomerId(customerId);
        List<Order> orders = orderData.getCustomerOrders(customerId);
        return Response.ok(orders).build();
    }

    @GET
    @Path("/{orderId}")
    public Response getOrder(@PathParam("customerId") int customerId, @PathParam("orderId") int orderId) {
        validateCustomerId(customerId);
        
        if (orderId <= 0) {
            throw new InvalidInputException("Order ID must be a positive integer");
        }
        
        Order order = orderData.getOrder(orderId);
        if (order == null) {
            throw new OrderNotFoundException("Order with ID " + orderId + " not found");
        }
        if (order.getCustomerId() != customerId) {
            throw new OrderNotFoundException("Order with ID " + orderId + " not found for customer " + customerId);
        }
        return Response.ok(order).build();
    }

    @DELETE
    @Path("/{orderId}")
    public Response deleteOrder(@PathParam("customerId") int customerId, @PathParam("orderId") int orderId) {
        validateCustomerId(customerId);
        
        if (orderId <= 0) {
            throw new InvalidInputException("Order ID must be a positive integer");
        }
        
        Order order = orderData.getOrder(orderId);
        if (order == null) {
            throw new OrderNotFoundException("Order with ID " + orderId + " not found");
        }
        if (order.getCustomerId() != customerId) {
            throw new OrderNotFoundException("Order with ID " + orderId + " not found for customer " + customerId);
        }
        
        orderData.deleteOrder(orderId);
        return Response.noContent().build();
    }

    public static class OrderRequest {
        private List<Integer> bookIds;

        public List<Integer> getBookIds() {
            return bookIds;
        }

        public void setBookIds(List<Integer> bookIds) {
            this.bookIds = bookIds;
        }
    }
}
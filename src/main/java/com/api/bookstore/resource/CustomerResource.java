package com.api.bookstore.resource;

import com.api.bookstore.datastore.CustomerData;
import com.api.bookstore.exception.CustomerNotFoundException;
import com.api.bookstore.exception.InvalidInputException;
import com.api.bookstore.model.Customer;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.regex.Pattern;

@Path("/customers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CustomerResource {

    private static final Pattern LETTERS_ONLY = Pattern.compile("^[a-zA-Z\\s]+$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    private void validateCustomer(Customer customer) {
        // Validate name
        if (customer.getName() == null || customer.getName().trim().isEmpty()) {
            throw new InvalidInputException("Customer name cannot be null or empty");
        }
        if (!LETTERS_ONLY.matcher(customer.getName()).matches()) {
            throw new InvalidInputException("Customer name should contain only letters and spaces");
        }

        // Validate email
        if (customer.getEmail() == null || customer.getEmail().trim().isEmpty()) {
            throw new InvalidInputException("Email cannot be null or empty");
        }
        if (!EMAIL_PATTERN.matcher(customer.getEmail()).matches()) {
            throw new InvalidInputException("Invalid email format");
        }

        // Validate password
        if (customer.getPassword() == null || customer.getPassword().trim().isEmpty()) {
            throw new InvalidInputException("Password cannot be null or empty");
        }
    }

    @GET
    public List<Customer> getAllCustomers() {
        return CustomerData.getAllCustomers();
    }

    @GET
    @Path("/{id}")
    public Customer getCustomerById(@PathParam("id") int id) {
        Customer customer = CustomerData.findCustomerById(id);
        if (customer == null) {
            throw new CustomerNotFoundException("Customer with ID " + id + " not found");
        }
        return customer;
    }

    @POST
    public Response addCustomer(Customer customer) {
        validateCustomer(customer);
        
        // Check for duplicate email
        Customer existingCustomer = CustomerData.findCustomerByEmail(customer.getEmail());
        if (existingCustomer != null) {
            throw new InvalidInputException("Email " + customer.getEmail() + " is already registered");
        }
        
        Customer addedCustomer = CustomerData.addCustomer(customer);
        return Response.status(Response.Status.CREATED)
                .entity(addedCustomer)
                .build();
    }

    @PUT
    @Path("/{id}")
    public Response updateCustomer(@PathParam("id") int id, Customer customer) {
        Customer existingCustomer = CustomerData.findCustomerById(id);
        if (existingCustomer == null) {
            throw new CustomerNotFoundException("Customer with ID " + id + " not found");
        }
        
        validateCustomer(customer);
        
        // Check for duplicate email (excluding current customer)
        Customer customerWithSameEmail = CustomerData.findCustomerByEmail(customer.getEmail());
        if (customerWithSameEmail != null && customerWithSameEmail.getId() != id) {
            throw new InvalidInputException("Email " + customer.getEmail() + " is already registered by another customer");
        }
        
        customer.setId(id);
        Customer updatedCustomer = CustomerData.updateCustomer(customer);
        return Response.ok(updatedCustomer).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteCustomer(@PathParam("id") int id) {
        Customer existingCustomer = CustomerData.findCustomerById(id);
        if (existingCustomer == null) {
            throw new CustomerNotFoundException("Customer with ID " + id + " not found");
        }
        
        CustomerData.deleteCustomer(id);
        return Response.noContent().build();
    }
}
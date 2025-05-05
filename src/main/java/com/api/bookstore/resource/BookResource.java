package com.api.bookstore.resource;

import com.api.bookstore.datastore.AuthorData;
import com.api.bookstore.datastore.BookData;
import com.api.bookstore.exception.AuthorNotFoundException;
import com.api.bookstore.exception.BookNotFoundException;
import com.api.bookstore.exception.InvalidInputException;
import com.api.bookstore.model.Book;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/books")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BookResource {

    private static final int CURRENT_YEAR = java.time.Year.now().getValue();

    private void validateBook(Book book) {
        // Validate title
        if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
            throw new InvalidInputException("Book title cannot be null or empty");
        }

        // Validate ISBN
        if (book.getIsbn() == null || book.getIsbn().trim().isEmpty()) {
            throw new InvalidInputException("ISBN cannot be null or empty");
        }

        // Validate author ID
        if (book.getAuthorId() <= 0) {
            throw new InvalidInputException("Author ID must be a positive integer");
        }
        if (AuthorData.findAuthorById(book.getAuthorId()) == null) {
            throw new AuthorNotFoundException("Author with ID " + book.getAuthorId() + " not found");
        }

        // Validate publication year
        if (book.getPublicationYear() <= 0) {
            throw new InvalidInputException("Publication year must be a positive integer");
        }
        if (book.getPublicationYear() > CURRENT_YEAR) {
            throw new InvalidInputException("Publication year cannot be in the future");
        }

        // Validate price
        if (book.getPrice() <= 0) {
            throw new InvalidInputException("Price must be a positive number");
        }

        // Validate stock
        if (book.getStock() < 0) {
            throw new InvalidInputException("Stock cannot be negative");
        }
    }

    @GET
    public List<Book> getAllBooks() {
        return BookData.getAllBooks();
    }

    @GET
    @Path("/{id}")
    public Book getBookById(@PathParam("id") int id) {
        Book book = BookData.findBookById(id);
        if (book == null) {
            throw new BookNotFoundException("Book with ID " + id + " not found");
        }
        return book;
    }

    @POST
    public Response addBook(Book book) {
        validateBook(book);
        Book addedBook = BookData.addBook(book);
        return Response.status(Response.Status.CREATED)
                .entity(addedBook)
                .build();
    }

    @PUT
    @Path("/{id}")
    public Book updateBook(@PathParam("id") int id, Book book) {
        Book existingBook = BookData.findBookById(id);
        if (existingBook == null) {
            throw new BookNotFoundException("Book with ID " + id + " not found");
        }
        validateBook(book);
        book.setId(id);
        return BookData.updateBook(book);
    }

    @DELETE
    @Path("/{id}")
    public Response deleteBook(@PathParam("id") int id) {
        Book existingBook = BookData.findBookById(id);
        if (existingBook == null) {
            throw new BookNotFoundException("Book with ID " + id + " not found");
        }
        BookData.deleteBook(id);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @PUT
    @Path("/{id}/stock")
    public Book updateBookStock(@PathParam("id") int id, @QueryParam("stock") int stock) {
        Book book = BookData.findBookById(id);
        if (book == null) {
            throw new BookNotFoundException("Book with ID " + id + " not found");
        }
        if (stock < 0) {
            throw new InvalidInputException("Stock cannot be negative");
        }
        BookData.updateStock(id, stock);
        return BookData.findBookById(id);
    }
}
package com.api.bookstore.resource;

import com.api.bookstore.datastore.AuthorData;
import com.api.bookstore.datastore.BookData;
import com.api.bookstore.exception.AuthorNotFoundException;
import com.api.bookstore.exception.InvalidInputException;
import com.api.bookstore.model.Author;
import com.api.bookstore.model.Book;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

@Path("/authors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthorResource {

    private void validateAuthorName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidInputException("Author name cannot be null or empty");
        }
        if (!name.matches("^[a-zA-Z\\s]+$")) {
            throw new InvalidInputException("Author name can only contain letters and spaces");
        }
    }

    @GET
    public List<Author> getAllAuthors() {
        return AuthorData.getAllAuthors();
    }

    @GET
    @Path("/{id}")
    public Author getAuthorById(@PathParam("id") int id) {
        Author author = AuthorData.findAuthorById(id);
        if (author == null) {
            throw new AuthorNotFoundException("Author with ID " + id + " not found");
        }
        return author;
    }

    @GET
    @Path("/{id}/books")
    public List<Book> getBooksByAuthor(@PathParam("id") int id) {
        // First verify author exists
        Author author = AuthorData.findAuthorById(id);
        if (author == null) {
            throw new AuthorNotFoundException("Author with ID " + id + " not found");
        }
        
        // Get all books and filter by author ID
        return BookData.getAllBooks().stream()
                .filter(book -> book.getAuthorId() == id)
                .collect(Collectors.toList());
    }

    @POST
    public Response createAuthor(Author author) {
        // Validate author input
        validateAuthorName(author.getName());
        if (author.getBiography() == null || author.getBiography().trim().isEmpty()) {
            throw new InvalidInputException("Author biography cannot be null or empty");
        }

        Author createdAuthor = AuthorData.addAuthor(author);
        return Response.status(Response.Status.CREATED)
                .entity(createdAuthor)
                .build();
    }

    @PUT
    @Path("/{id}")
    public Author updateAuthor(@PathParam("id") int id, Author author) {
        // Validate author input
        validateAuthorName(author.getName());
        if (author.getBiography() == null || author.getBiography().trim().isEmpty()) {
            throw new InvalidInputException("Author biography cannot be null or empty");
        }

        Author existingAuthor = AuthorData.findAuthorById(id);
        if (existingAuthor == null) {
            throw new AuthorNotFoundException("Author with ID " + id + " not found");
        }
        author.setId(id);
        return AuthorData.updateAuthor(author);
    }

    @DELETE
    @Path("/{id}")
    public Response deleteAuthor(@PathParam("id") int id) {
        Author existingAuthor = AuthorData.findAuthorById(id);
        if (existingAuthor == null) {
            throw new AuthorNotFoundException("Author with ID " + id + " not found");
        }
        try {
            AuthorData.deleteAuthor(id);
            return Response.status(Response.Status.NO_CONTENT).build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
    }
}
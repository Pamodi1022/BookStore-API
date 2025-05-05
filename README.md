# 📚 Bookstore API

Welcome to the **Bookstore API** – your one-stop backend solution for managing books, authors, customers, orders, and shopping carts. Whether you're building a full-scale e-commerce bookshop or a small reading corner, this RESTful API has got you covered.

---

## 🚀 Endpoints

### 📖 Books

* `GET /books` – Fetch the entire bookshelf.
* `GET /books/{id}` – Peek inside a specific book by its ID.
* `POST /books` – Add a brand new title to the collection.
* `PUT /books/{id}` – Revise a book’s details.
* `DELETE /books/{id}` – Remove a book from the shelf.
* `PUT /books/{id}/stock` – Update how many copies are available.

### ✍️ Authors

* `GET /authors` – View the library of authors.
* `GET /authors/{id}` – Get to know an author by ID.
* `GET /authors/{id}/books` – See all books written by a specific author.
* `POST /authors` – Add a new writer to the records.
* `PUT /authors/{id}` – Update author details.
* `DELETE /authors/{id}` – Remove an author from the system.

### 👤 Customers

* `GET /customers` – Browse all registered readers.
* `GET /customers/{id}` – Look up a specific customer.
* `POST /customers` – Register a new customer.
* `PUT /customers/{id}` – Edit customer information.
* `DELETE /customers/{id}` – Delete a customer profile.

### 🛒 Orders

* `GET /customers/{customerId}/orders` – View all past orders by a customer.
* `GET /customers/{customerId}/orders/{orderId}` – Dive into the details of a specific order.
* `POST /customers/{customerId}/orders` – Place a fresh order.
* `DELETE /customers/{customerId}/orders/{orderId}` – Cancel an order.

### 🧺 Shopping Cart

* `GET /customers/{customerId}/cart` – Check what’s currently in the cart.
* `POST /customers/{customerId}/cart/items` – Add a book to the cart.
* `PUT /customers/{customerId}/cart/items/{bookId}` – Change the quantity of a book in the cart.
* `DELETE /customers/{customerId}/cart/items/{bookId}` – Remove a book from the cart.

---

## 🛠️ Getting Started

### ✅ Prerequisites

Before you get turning pages in the backend, make sure you have:

* **Java 17** or newer
* **Maven 3.6** or later
* **Apache Tomcat 9** or higher

### 📦 Installation

1. Clone the repository to your local machine:

   ```bash
   git clone https://github.com/Pamodi1022/bookstore-api.git
   cd bookstore-api
   ```


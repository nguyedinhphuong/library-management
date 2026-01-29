# Library Management System – Backend

A backend RESTful API built with Spring Boot for managing students, books, and borrowing/returning workflows in a library system.

---

## Tech Stack
- Java 17
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Swagger / OpenAPI
- Maven

---

## Core Features
- Manage students and books (CRUD)
- Borrow and return books with business validation
- Limit number of borrowed books per student
- Prevent borrowing when students have overdue books
- Transaction management to ensure data consistency
- Dynamic search and filtering with pagination

---

## Business Rules
- A student can borrow a maximum number of books at the same time
- A student cannot borrow new books if there are overdue borrow records
- Borrowing and returning operations are executed within transactions
- Transactions will rollback when business rules are violated

---

## API Overview
- POST `/api/borrow` – Borrow a book
- POST `/api/return` – Return a book
- GET `/api/books` – Search and filter books with pagination
- GET `/api/students` – List students with pagination

---

## Validation & Exception Handling
- Request validation using **Spring Validation**
- Global exception handling with `@ControllerAdvice`
- Custom exceptions for business logic errors

---

## Dynamic Search & Filtering
- Implemented flexible searching and filtering using **JPA Criteria / Specification**
- Supports optional parameters without writing multiple queries
- Improves maintainability and scalability of search logic

---

## Transaction Management
- Applied `@Transactional` to critical business operations
- Ensures data consistency when borrowing or returning books
- Automatically rollback transactions on runtime exceptions or business rule violations

---

## Database Design
### Main Entities
- Student
- Book
- BorrowRecord

### Relationships
- Student – BorrowRecord: One-to-Many
- Book – BorrowRecord: Many-to-One

---

## API Documentation
Swagger UI is available at:

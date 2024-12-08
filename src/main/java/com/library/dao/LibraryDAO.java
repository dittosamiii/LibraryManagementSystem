package com.library.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

import com.library.databaseutil.LibraryDButil;
import com.library.exception.LibraryExceptions;

public class LibraryDAO {
	public LibraryDAO() {
		System.out.println("------------------------");
		System.out.println(" Welcome to the Library");
		System.out.println("------------------------");
	}

	// Fetching books from database using this showAvailableBooks Method
	public void showAvailableBooks() {
		String query = "SELECT Book_id, Book_name, Authors_name, Price, Available FROM books";
		try (Connection mycon = LibraryDButil.LibraryConnection();
				PreparedStatement pstmt = mycon.prepareStatement(query);
				ResultSet myrs = pstmt.executeQuery()) {

			System.out.println("\nBooks Available in the Library:");
			System.out.println("--------------------------------------------------------------------------------------------------");
			System.out.printf("%-5s %-30s %-30s %-10s %s%n", "ID", "Name", "Author", "Price", "Availability");
			System.out.println("--------------------------------------------------------------------------------------------------");

			boolean hasBooks = false;
			while (myrs.next()) {
				hasBooks = true;
				String availability = (myrs.getInt("Available") == 1) ? "Available" : "Unavailable";
				System.out.printf("%-5s %-30s %-30s %-10d %s%n", myrs.getInt("Book_id"), myrs.getString("Book_name"),
						myrs.getString("Authors_name"), myrs.getInt("Price"), availability);
			}

			if (!hasBooks) {
				System.out.println("No books found in the library.");
			}
		} catch (Exception e) {
			System.err.println("An error occurred while fetching the books: " + e.getMessage());
		}
	}

	// Adding books to our database using addBook Method
	public void addBook(String name, String author, int price) {
		try (Connection mycon = LibraryDButil.LibraryConnection();
				PreparedStatement pstmt = mycon.prepareStatement(
						"insert into books (Book_name, Authors_name, Price, Available) values (?,?,?,1)");) {

			pstmt.setString(1, name);
			pstmt.setString(2, author);
			pstmt.setInt(3, price);
			pstmt.executeUpdate();

			System.out.println("Book has been added");

		} catch (Exception e) {
			System.out.println("An error occurred: " + e.getMessage());
		}
	}

	// Issuing book to Student from the Library
	public void issueBook(int bookId, int studentId, String studentName, String issueDate, String returnDate) {
		String selectQuery = "SELECT Book_name, Available FROM books WHERE book_id = ?";
		String updateQuery = "UPDATE books SET Available = 0 WHERE book_id = ?";
		String insertQuery = "INSERT INTO Student (St_id, St_name, Book_id, Issue_date, Return_date, Borrow) VALUES (?, ?, ?, ?, ?, 'Borrowed')";

		try (Connection mycon = LibraryDButil.LibraryConnection();
				PreparedStatement pstmtSelect = mycon.prepareStatement(selectQuery)) {

			// Step 1: Check if the book exists and is available
			pstmtSelect.setInt(1, bookId);
			try (ResultSet rs = pstmtSelect.executeQuery()) {
				if (!rs.next()) {
					System.out
							.println("Book with ID " + bookId + " does not exist. Please check the ID and try again.");
					return;
				}

				String bookName = rs.getString("Book_name");
				boolean isAvailable = rs.getInt("Available") == 1;

				if (!isAvailable) {
					System.out.println("The book '" + bookName + "' is currently unavailable.");
					return;
				}

				// Step 2: Update the book's availability
				try (PreparedStatement pstmtUpdate = mycon.prepareStatement(updateQuery)) {
					pstmtUpdate.setInt(1, bookId);
					int rowsUpdated = pstmtUpdate.executeUpdate();

					if (rowsUpdated == 0) {
						System.out.println("Failed to update the book's availability. Please try again.");
						return;
					}
				}

				// Step 3: Insert the issue record into the Student table
				try (PreparedStatement pstmtInsert = mycon.prepareStatement(insertQuery)) {
					pstmtInsert.setInt(1, studentId);
					pstmtInsert.setString(2, studentName);
					pstmtInsert.setInt(3, bookId);
					pstmtInsert.setString(4, issueDate);
					pstmtInsert.setString(5, returnDate);
					pstmtInsert.executeUpdate();

					System.out.println(
							"The book '" + bookName + "' has been successfully issued to " + studentName + ".");
				}
			}
		} catch (Exception e) {
			System.err.println("An error occurred while issuing the book: " + e.getMessage());
		}
	}

	// Return Book to the Library
	public void returnBook(int id, String returning_date) throws LibraryExceptions {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		try (Connection mycon = LibraryDButil.LibraryConnection()) {
			// Validate book availability
			PreparedStatement pstmt = mycon.prepareStatement("SELECT * FROM books WHERE book_id = ? AND available = 0");
			pstmt.setInt(1, id);
			ResultSet rs = pstmt.executeQuery();
			if (!rs.next()) {
				System.out.println("Book is already available or invalid ID.");
				return;
			}

			// Update availability
			pstmt = mycon.prepareStatement("UPDATE books SET available = 1 WHERE book_id = ?");
			pstmt.setInt(1, id);
			pstmt.executeUpdate();

			// Get student and return details
			pstmt = mycon.prepareStatement("SELECT * FROM student WHERE book_id = ?");
			pstmt.setInt(1, id);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				String studentName = rs.getString("St_name");
				int studentId = rs.getInt("St_id");
				String return_date = rs.getString("Return_date");

				// Validate and calculate fines
				LocalDate startDate = LocalDate.parse(return_date, formatter);
				LocalDate endDate = LocalDate.parse(returning_date, formatter);
				long days = ChronoUnit.DAYS.between(startDate, endDate);

				if (days < 0) {
					System.out.println("Invalid return date.");
					pstmt = mycon.prepareStatement("UPDATE books SET available = 0 WHERE book_id = ?");
					pstmt.setInt(1, id);
					pstmt.executeUpdate();
					return;
				}

				if (days > 0) {
					int fees = (int) days * 10;
					pstmt = mycon.prepareStatement(
							"INSERT INTO Fine (St_id, St_name, Book_id, Returned_date, Days_delayed, Fine_fees) VALUES (?, ?, ?, ?, ?, ?)");
					pstmt.setInt(1, studentId);
					pstmt.setString(2, studentName);
					pstmt.setInt(3, id);
					pstmt.setString(4, returning_date);
					pstmt.setLong(5, days);
					pstmt.setInt(6, fees);
					pstmt.executeUpdate();
					System.out.println("Book returned after " + days + " day(s). Fine: Rs. " + fees);
				} else {
					System.out.println("Book has been returned on time.");
				}

				// Update student borrow status
				pstmt = mycon.prepareStatement("UPDATE student SET borrow = 'Returned' WHERE book_id = ?");
				pstmt.setInt(1, id);
				pstmt.executeUpdate();
			} else {
				System.out.println("Student record not found for the returned book.");
			}
		} catch (DateTimeParseException e) {
			throw new LibraryExceptions("Invalid date format. Use dd/MM/yyyy.");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("An error occurred: " + e.getMessage());
		}
	}

	// Updating Existing Book in the Library
	public void updateExistingBook(int id, String name, String author, int price) {
		try (Connection mycon = LibraryDButil.LibraryConnection();
				PreparedStatement pstmt = mycon.prepareStatement(
						"UPDATE books SET Book_name = ?, Authors_name = ?, Price = ? WHERE book_id = ? AND Available = 1")) {

			// Set parameters for the query
			pstmt.setString(1, name);
			pstmt.setString(2, author);
			pstmt.setInt(3, price);
			pstmt.setInt(4, id);

			// Execute the update query
			int rowsAffected = pstmt.executeUpdate();
			if (rowsAffected > 0) {
				System.out.println("Book has been updated successfully.");
			} else {
				System.out.println("Book update failed. Either the ID is invalid or the book is not available.");
			}
		} catch (Exception e) {
			System.out.println("An error occurred: " + e.getMessage());
		}
	}

	// Delete Existing Book from the Library
	public void deletingBook(int bookId) throws LibraryExceptions {
		String selectQuery = "SELECT Available FROM Books WHERE Book_id = ?";
		String deleteQuery = "DELETE FROM Books WHERE Book_id = ? AND Available = 1";

		try (Connection mycon = LibraryDButil.LibraryConnection();
				PreparedStatement pstmtSelect = mycon.prepareStatement(selectQuery);
				PreparedStatement pstmtDelete = mycon.prepareStatement(deleteQuery)) {

			// Step 1: Check if the book exists and is available
			pstmtSelect.setInt(1, bookId);
			try (ResultSet rs = pstmtSelect.executeQuery()) {
				if (!rs.next()) {
					throw new LibraryExceptions("Invalid Book ID. Please enter a valid ID and try again.");
				}

				int available = rs.getInt("Available");
				if (available == 1) {
					// Step 2: Delete the book if it's available
					pstmtDelete.setInt(1, bookId);
					int rowsAffected = pstmtDelete.executeUpdate();
					if (rowsAffected > 0) {
						System.out.println("Book deleted from the Library.");
					} else {
						System.out.println("Failed to delete the book. Please try again.");
					}
				} else {
					System.out.println("Book is not available for deletion.");
				}
			}
		} catch (LibraryExceptions e) {
			System.err.println(e.getMessage());
		} catch (Exception e) {
			System.err.println("An error occurred: " + e.getMessage());
		}
	}

}

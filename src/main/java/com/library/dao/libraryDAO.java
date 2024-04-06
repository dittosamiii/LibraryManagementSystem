package com.library.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import com.library.databaseutil.libraryDButil;
import com.library.exception.libraryExceptions;

public class libraryDAO {
	public libraryDAO() {
		System.out.println("------------------------");
		System.out.println(" Welcome to the Library");
		System.out.println("------------------------");
	}

	// Fetching books from database using this showAvailableBooks Method
	public void showAvailableBooks() {
		try (Connection mycon = libraryDButil.LibraryConnection();
				PreparedStatement pstmt = mycon.prepareStatement("SELECT * FROM books");
				ResultSet myrs = pstmt.executeQuery();) {

			System.out.println("Books Available in the Library");
			System.out.println("ID\t\tName\t\t\tAuthor\t\t\t  Price\t    Availability");
			while (myrs.next()) {
				String availability = (myrs.getInt("Available") == 1) ? "Available" : "Unavailable";
				System.out.printf("%-3s %-30s %-30s %-10s %s%n", myrs.getString("Book_id"), myrs.getString("Book_name"),
						myrs.getString("Authors_name"), myrs.getString("Price"), availability);
			}
		} catch (Exception e) {
			System.out.println("An error occurred: " + e.getMessage());
		}
	}

	// Adding books to our database using addBook Method
	public void addBook(String name, String author, int price) {
		try (Connection mycon = libraryDButil.LibraryConnection();
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
	public void issueBook(int id, int st_id, String st_name, String issue_date, String return_date) {
		try (Connection mycon = libraryDButil.LibraryConnection();
				PreparedStatement pstmtSelect = mycon.prepareStatement("SELECT * FROM books WHERE book_id = ?");) {

			// Set the book_id parameter for the SELECT statement
			pstmtSelect.setInt(1, id);
			ResultSet imyrs = pstmtSelect.executeQuery();
			if (imyrs.next()) {
				String bookName = imyrs.getString("Book_name"); // Save the book name before closing the ResultSet
				int availability = imyrs.getInt("Available");

				if (availability == 1) {
					// Update the availability of the book
					PreparedStatement pstmtUpdate = mycon
							.prepareStatement("UPDATE books SET available = 0 WHERE book_id = ?");
					pstmtUpdate.setInt(1, id);
					int rows = pstmtUpdate.executeUpdate();
					if (rows > 0) {
						PreparedStatement pstmtInsert = mycon.prepareStatement(
								"INSERT INTO Student (St_id, St_name, Book_id, Issue_date, Return_date, Borrow) VALUES (?, ?, ?, ?, ?, 'Borrowed')");
						System.out.println(bookName + " Book has been issued.");
						// Insert the record into the Student table
						pstmtInsert.setInt(1, st_id);
						pstmtInsert.setString(2, st_name);
						pstmtInsert.setInt(3, id);
						pstmtInsert.setString(4, issue_date);
						pstmtInsert.setString(5, return_date);
						pstmtInsert.executeUpdate();
					}
				} else {
					System.out.println("Book is Unavailable");
				}
			} else {
				throw new libraryExceptions("Invalid Id. Please Enter the valid id and try again.");
			}
		} catch (Exception e) {
			System.out.println("An error occurred: " + e.getMessage());
		}
	}

	// Return Book to the Library
	public void returnBook(int id, String returning_date) throws libraryExceptions {
	    try (Connection mycon = libraryDButil.LibraryConnection()) {
	    	PreparedStatement pstmt = mycon.prepareStatement("SELECT * FROM books WHERE book_id = ?");
	        pstmt.setInt(1, id);
	        ResultSet imyrs1 = pstmt.executeQuery();
	        if (imyrs1.next()) {
	            int availability = imyrs1.getInt("Available");
	            if (availability == 0) {
	                pstmt = mycon.prepareStatement("UPDATE books SET available = 1 WHERE book_id = ?");
	                pstmt.setInt(1, id);
	                int rowsAffected = pstmt.executeUpdate();
	                if (rowsAffected > 0) {
	                    pstmt = mycon.prepareStatement("SELECT * FROM student WHERE book_id = ?");
	                    pstmt.setInt(1, id);
	                    ResultSet imyrs = pstmt.executeQuery();
	                    if (imyrs.next()) {
	                        String studentName = imyrs.getString("St_name");
	                        int studentId = imyrs.getInt("St_id");
	                        String return_date = imyrs.getString("Return_date");
	                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	                        LocalDate startDate = LocalDate.parse(return_date, formatter);
	                        LocalDate endDate = LocalDate.parse(returning_date, formatter);
	                        long days = ChronoUnit.DAYS.between(startDate, endDate);
	                        if (days > 0) {
	                            int fees = (int) days * 10;
	                            // Insert fine record into the Fine table
	                            pstmt = mycon.prepareStatement("INSERT INTO Fine (St_id, St_name, Book_id, Returned_date, Days_delayed, Fine_fees) "
	                                    + "VALUES (?, ?, ?, ?, ?, ?)");
	                            pstmt.setInt(1, studentId);
	                            pstmt.setString(2, studentName);
	                            pstmt.setInt(3, id);
	                            pstmt.setString(4, returning_date);
	                            pstmt.setLong(5, days);
	                            pstmt.setInt(6, fees);
	                            pstmt.executeUpdate();
	                            // Update borrow status to 'Returned' in the Student table
	                            pstmt = mycon.prepareStatement("UPDATE student SET borrow = 'Returned' WHERE book_id = ?");
	                            pstmt.setInt(1, id);
	                            pstmt.executeUpdate();
	                            System.out.println("Book has been returned after " + days
	                                    + " day(s), and the fine will be Rs." + fees);
	                        } else {
	                            System.out.println("Book has been returned.");
	                        }
	                    }
	                    imyrs.close();
	                } else {
	                    throw new libraryExceptions("Invalid Id. Please enter a valid id and try again.");
	                }
	            } else {
	                System.out.println("Book is already Available.");
	            }
	        } else {
	            throw new libraryExceptions("Invalid Id. Please enter a valid id and try again.");
	        }
	    } catch (Exception e) {
	        System.out.println("An error occurred: " + e.getMessage());
	    }
	}


	// Updating Existing Book in the Library
	public void updateExistingBook(int id, String name, String author, int price) {
		try (Connection mycon = libraryDButil.LibraryConnection();
				PreparedStatement pstmt = mycon.prepareStatement(
						"UPDATE books SET Book_name = ?, Authors_name = ?, Price = ? WHERE book_id = ?")) {

			pstmt.setString(1, name);
			pstmt.setString(2, author);
			pstmt.setInt(3, price);
			pstmt.setInt(4, id);

			int rowsAffected = pstmt.executeUpdate();
			if (rowsAffected > 0) {
				System.out.println("Book has been updated");
			} else {
				throw new libraryExceptions("Invalid ID. Please enter a valid ID and try again.");
			}
		} catch (Exception e) {
			System.out.println("An error occurred: " + e.getMessage());
		}
	}

	// Delete Existing Book from the Library
	public void deletingBook(int id) throws libraryExceptions {
		try (Connection mycon = libraryDButil.LibraryConnection();
				PreparedStatement pstmtSelect = mycon.prepareStatement("SELECT Available FROM Books WHERE Book_id = ?");
				PreparedStatement pstmtDelete = mycon
						.prepareStatement("DELETE FROM Books WHERE Book_id = ? AND Available = 1")) {

			// Check if the book with the given ID exists and is available
			pstmtSelect.setInt(1, id);
			ResultSet rs = pstmtSelect.executeQuery();
			if (rs.next()) {
				int available = rs.getInt("Available");
				if (available == 1) {
					// Delete the book if it's available
					pstmtDelete.setInt(1, id);
					int rowsAffected = pstmtDelete.executeUpdate();
					if (rowsAffected > 0) {
						System.out.println("Book deleted from the Library");
					} else {
						System.out.println("Failed to delete the book. Please try again.");
					}
				} else {
					System.out.println("Book is not available");
				}
			} else {
				throw new libraryExceptions("Invalid Book ID. Please enter a valid ID and try again.");
			}
		} catch (Exception e) {
			System.out.println("An error occurred: " + e.getMessage());
		}
	}

}
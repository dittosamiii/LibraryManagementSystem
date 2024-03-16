package com.library.dao;

import java.sql.Connection;
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
		try (Connection mycon = libraryDButil.LibraryConnection()) {
			Statement mystmt = mycon.createStatement();
			ResultSet myrs = mystmt.executeQuery("Select * from books");
			System.out.println("Books Available in the Library");
			System.out.println("ID\t\tName\t\t\tAuthor\t\t\t  Price\t    Availability");
			while (myrs.next()) {
				String availability = (myrs.getInt(5) == 1) ? "Available" : "Unavailable";
				System.out.printf("%-3s %-30s %-30s %-10s %s%n", myrs.getString(1), myrs.getString(2),
						myrs.getString(3), myrs.getString(4), availability);
			}
		} catch (Exception e) {
			System.out.println("An error occurred: " + e.getMessage());
		}
	}

	// Adding books to our database using addBook Method
	public void addBook(String name, String author, int price) {
		try (Connection mycon = libraryDButil.LibraryConnection();) {
			Statement mystmt = mycon.createStatement();

			mystmt.executeUpdate("insert into books (Book_name, Authors_name, Price, Available) " + "values ('" + name
					+ "', '" + author + "', " + price + ", 1)");
			System.out.println("Book has been added");

		} catch (Exception e) {
			System.out.println("An error occurred: " + e.getMessage());
		}
	}

	// Issuing book to Student from the Library
	public void issueBook(int id, int st_id, String st_name, String issue_date, String return_date) {
		try (Connection mycon = libraryDButil.LibraryConnection();) {
			Statement mystmt = mycon.createStatement();
			ResultSet imyrs = mystmt.executeQuery("Select * from books where book_id = " + id);
			if (imyrs.next()) {
				String bookName = imyrs.getString(2); // Save the book name before closing the ResultSet
				int availability = imyrs.getInt(5);

				if (availability == 1) {
					int rows = mystmt.executeUpdate("update books set available = 0 where book_id = " + id);
					if (rows > 0) {
						System.out.println(bookName + " Book has been issued.");
						mystmt.executeUpdate(
								"insert into Student(St_id, St_name, Book_id, Book_name, Issue_date, Return_date, Borrow) "
										+ "values(" + st_id + ", '" + st_name + "', " + id + ", '" + bookName + "', '"
										+ issue_date + "', '" + return_date + "', 'Borrowed')");
					}
				} else {
					System.out.println("Book is Unavailable");
				}
			} else {
				throw new libraryExceptions("Invalid Id. Please Enter the valid id and try again.");
			}
			imyrs.close();
		} catch (Exception e) {
			System.out.println("An error occurred: " + e.getMessage());
		}
	}

	// Return Book to the Library
	public void returnBook(int id, String returning_date) throws libraryExceptions {
		try (Connection mycon = libraryDButil.LibraryConnection()) {
			Statement mystmt = mycon.createStatement();
			ResultSet imyrs1 = mystmt.executeQuery("select * from books where book_id = " + id);
			if (imyrs1.next()) {
				int availability = imyrs1.getInt(5);
				if (availability == 0) {

					int rowsAffected = mystmt.executeUpdate("update books set available = 1 where book_id = " + id);

					if (rowsAffected > 0) {
						ResultSet imyrs = mystmt.executeQuery("select * from student where book_id = " + id);

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
								mystmt.executeUpdate(
										"insert into Fine(St_id, St_name, Returned_date, Days_delayed, Fine_fees) "
												+ "values(" + studentId + ", '" + studentName + "', '" + returning_date
												+ "', " + days + ", " + fees + ")");
								mystmt.executeUpdate("update student set borrow='Returned' where book_id = " + id);

								System.out.println("Book has been returned after " + days
										+ " day/days, and the fine will be Rs." + fees);
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
		try (Connection mycon = libraryDButil.LibraryConnection()) {
			Statement mystmt = mycon.createStatement();
			int rowsAffected = mystmt.executeUpdate("update books set Book_name = '" + name + "', Authors_name= '"
					+ author + "'," + " Price= " + price + " WHERE book_id = " + id);
			if (rowsAffected > 0) {
				System.out.println("Book has been updated");
			} else {
				throw new libraryExceptions("Invalid Id. Please enter a valid id and try again.");
			}
		} catch (Exception e) {
			System.out.println("An error occurred: " + e.getMessage());
		}
	}

	// Delete Existing Book from the Library
	public void deletingBook(int id) throws libraryExceptions {
		try (Connection mycon = libraryDButil.LibraryConnection();) {
			Statement mystmt = mycon.createStatement();
			ResultSet rs = mystmt.executeQuery("Select available from books where book_id = " + id);
			if (rs.next()) {
				int available = rs.getInt("Available");
				if (available == 1) {
					int rowsAffected = mystmt
							.executeUpdate("delete from books where book_id = " + id + " and available = 1");
					if (rowsAffected > 0) {
						System.out.println("Book deleted from the Library");
					} else {
						System.out.println("Book is not available");
					}
				} else {
					System.out.println("Book is not available");
				}
			} else {
				throw new libraryExceptions("Invalid Id. Please enter a valid id and try again.");
			}
		} catch (Exception e) {
			System.out.println("An error occurred: " + e.getMessage());
		}
	}

}
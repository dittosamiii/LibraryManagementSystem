package com.library.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

import com.library.dao.LibraryDAO;
import com.library.exception.LibraryExceptions;

public class LibraryController {

	Scanner sc = new Scanner(System.in);
	LibraryDAO lib = new LibraryDAO();

	// Helper function for date Validation
	public boolean isValidDate(String dateString) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		try {
			LocalDate.parse(dateString, formatter);
		} catch (DateTimeParseException e) {
			return false;
		}
		return true;
	}

	// Helper name function
	public boolean validateName(String name) {
		return name.matches("[a-zA-Z .]+");
	}

	// Helper Input name Validation function
	public boolean validateInputName(String name) {
		return (name.trim().isEmpty() || !validateName(name));
	}

	// Show Available Books Method
	public void showBooks() {
		lib.showAvailableBooks();
	}

	// Read Books Method
	public void addBooks() {
		int price;
		String name, author;

		// Validate name
		System.out.print("Enter the name of the book: ");
		while (validateInputName(name = sc.nextLine())) {
			System.out.print("Enter a valid book name: ");
		}

		// Validate author
		System.out.print("Enter the author of the book: ");
		while (validateInputName(author = sc.nextLine())) {
			System.out.print("Enter a valid author: ");
		}

		// Validate price
		System.out.print("Enter the price of the book: ");
		while (!sc.hasNextInt() || (price = sc.nextInt()) <= 0) {
			System.out.print("Enter a valid price: ");
			sc.nextLine();
		}
		sc.nextLine();

		lib.addBook(name, author, price);
	}

	// Issue Book Method
	public void issueBook() throws LibraryExceptions {
		int id, st_id;
		String st_name, issue_date, return_date;

		// Validate id
		System.out.print("Enter the book id you want to Issue: ");
		while (!sc.hasNextInt() || (id = sc.nextInt()) <= 0) {
			System.out.print("Enter a valid id: ");
			sc.nextLine(); // Discard the invalid input
		}
		sc.nextLine();

		// Validate Student id
		System.out.print("Enter the Student id: ");
		while (!sc.hasNextInt() || (st_id = sc.nextInt()) <= 0) {
			System.out.print("Enter a valid student id: ");
			sc.nextLine(); // Discard the invalid input
		}
		sc.nextLine();

		// Validate Student name
		System.out.print("Enter the Student name: ");
		while (validateInputName(st_name = sc.nextLine())) {
			System.out.print("Enter a valid student name: ");
		}

		// Validate issue date
		System.out.print("Enter the Issue Date (dd/MM/yyyy): ");
		while (!isValidDate(issue_date = sc.nextLine())) {
			System.out.print("Enter a valid date in dd/MM/yyyy format: ");
		}

		// Validate return date
		System.out.print("Enter the Return Date (dd/MM/yyyy): ");
		while (!isValidDate(return_date = sc.nextLine())
				|| LocalDate.parse(return_date, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
						.isBefore(LocalDate.parse(issue_date, DateTimeFormatter.ofPattern("dd/MM/yyyy")))) {
			System.out.print("Enter a valid return date in dd/MM/yyyy format that is after the issue date: ");
		}

		lib.issueBook(id, st_id, st_name, issue_date, return_date);
	}

	// Return Book Method
	public void returnBook() throws LibraryExceptions {
		int id;
		String days;

		// Validate id
		System.out.print("Enter the id of the book: ");
		while (!sc.hasNextInt() || (id = sc.nextInt()) <= 0) {
			System.out.print("Enter a valid id: ");
			sc.nextLine(); // discard the invalid input
		}
		sc.nextLine();

		// Validate issue date
		System.out.print("Enter the Returning Date (dd/MM/yyyy): ");
		while (!isValidDate(days = sc.nextLine())) {
			System.out.print("Enter a valid date in dd/MM/yyyy format: ");
		}

		lib.returnBook(id, days);
	}

	// Update Book Method
	public void updateExistingBook() {
		int id, price;
		String name, author;

		// Validate id
		System.out.print("Enter the id of the book: ");
		while (!sc.hasNextInt() || (id = sc.nextInt()) <= 0) {
			System.out.print("Enter a valid id: ");
			sc.nextLine(); // discard the invalid input
		}
		sc.nextLine();

		// Validate name
		System.out.print("Enter the name of the book: ");
		while (validateInputName(name = sc.nextLine())) {
			System.out.print("Enter a valid book name: ");
		}

		// Validate author
		System.out.print("Enter the author of the book: ");
		while (validateInputName(author = sc.nextLine())) {
			System.out.print("Enter a valid author: ");
		}

		// Validate price
		System.out.print("Enter the price of the book: ");
		while (!sc.hasNextInt() || (price = sc.nextInt()) <= 0) {
			System.out.print("Enter a valid price: ");
			sc.nextLine(); // discard the invalid input
		}
		sc.nextLine();

		lib.updateExistingBook(id, name, author, price);
	}

	public void deletingBook() throws LibraryExceptions {
		int id;

		// Validate id
		System.out.print("Enter the id of the book: ");
		while (!sc.hasNextInt() || (id = sc.nextInt()) <= 0) {
			System.out.print("Enter a valid id: ");
			sc.nextLine(); // discard the invalid input
		}
		sc.nextLine();

		lib.deletingBook(id);
	}

}
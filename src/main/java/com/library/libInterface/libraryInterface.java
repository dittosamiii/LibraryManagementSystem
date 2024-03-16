package com.library.libInterface;

import java.util.Scanner;

import com.library.controller.libraryController;
import com.library.exception.libraryExceptions;

public class libraryInterface {
	public void Interface() throws libraryExceptions {
		libraryController libraryController = new libraryController();
		Scanner sc = new Scanner(System.in);
		int choice = 0, temp_choice = 0;

		while (true) {
			// Interface for the Library
			System.out.println();
			System.out.println("Enter your choices:");
			System.out.println("1. Show Available Books");
			System.out.println("2. Add Book");
			System.out.println("3. Issue Book");
			System.out.println("4. Return Book");
			System.out.println("5. Update Existing Book");
			System.out.println("6. Delete Existing Book");
			System.out.println("Press 0 to Exit");
			System.out.println();
			System.out.print("Enter your choice: ");

			// Validate Choice
			while (!sc.hasNextInt()) {
				System.out.print("Enter a Valid Choice: ");
				sc.nextLine(); // discard the invalid input
			}
			temp_choice = sc.nextInt();
			choice = temp_choice;

			// Show Available Books Functionality
			if (choice == 1) {
				libraryController.showBooks();
			}

			// Add Book Functionality
			else if (choice == 2) {
				libraryController.addBooks();
			}

			// Issue Book Functionality
			else if (choice == 3) {
				libraryController.issueBook();
			}

			// Return Book Functionality
			else if (choice == 4) {
				libraryController.returnBook();
			}
			// Update Book Functionality
			else if (choice == 5) {
				libraryController.updateExistingBook();
			}
			// Delete Book Functionality
			else if (choice == 6) {
				libraryController.deletingBook();
			}
			// Exiting from the Library
			else if (choice == 0) {
				System.out.println("Exiting from the Library");
				break;
			}
			// Invalid choice
			else {
				System.out.println("Invalid Id. Please Enter the valid id and try again.");
			}
		}
	}
}
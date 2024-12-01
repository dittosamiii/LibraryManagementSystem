package com.library.libInterface;

import java.util.Scanner;

import com.library.controller.LibraryController;
import com.library.exception.LibraryExceptions;

public class LibraryInterface {
	public void Interface() throws LibraryExceptions {
		LibraryController libraryController = new LibraryController();
		try (Scanner sc = new Scanner(System.in)) {
			int choice, temp_choice;

                    OUTER:
                    while (true) {
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
                        while (!sc.hasNextInt()) {
                            System.out.print("Enter a Valid Choice: ");
                            sc.nextLine(); // discard the invalid input
                        }
                        temp_choice = sc.nextInt();
                        choice = temp_choice;
                        // Show Available Books Functionality
                        switch (choice) {
                            case 1:
                                libraryController.showBooks();
                                break;
                            case 2:
                                libraryController.addBooks();
                                break;
                            case 3:
                                libraryController.issueBook();
                                break;
                            case 4:
                                libraryController.returnBook();
                                break;
                            case 5:
                                libraryController.updateExistingBook();
                                break;
                            case 6:
                                libraryController.deletingBook();
                                break;
                            case 0:
                                System.out.println("Exiting from the Library");
                                break OUTER;
                            default:
                                System.out.println("Invalid Id. Please Enter the valid id and try again.");
                                break;
                        }
                    }
		}
	}
}
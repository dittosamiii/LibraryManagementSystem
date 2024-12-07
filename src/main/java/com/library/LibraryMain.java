package com.library;

import com.library.exception.LibraryExceptions;
import com.library.libInterface.LibraryInterface;

public class LibraryMain {
	public static void main(String[] args) throws LibraryExceptions {
		LibraryInterface libraryInterface = new LibraryInterface();
		libraryInterface.Interface();
	}
}
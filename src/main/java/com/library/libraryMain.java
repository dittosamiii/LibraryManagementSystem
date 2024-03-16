package com.library;

import com.library.exception.libraryExceptions;
import com.library.libInterface.libraryInterface;

public class libraryMain {
	public static void main(String[] args) throws libraryExceptions {
		libraryInterface libraryInterface = new libraryInterface();
		libraryInterface.Interface();
	}
}
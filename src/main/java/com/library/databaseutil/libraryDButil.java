package com.library.databaseutil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class libraryDButil {
	public static Connection LibraryConnection() {
		Connection mycon = null;

		try {
			String dbUrl = "jdbc:mysql://localhost:3306/Library";
			String user = "root";
			String pass = "toor";
			mycon = DriverManager.getConnection(dbUrl, user, pass);

		} catch (SQLException e) {
			System.out.println("Connection ERROR!");
			e.printStackTrace();
		}

		return mycon;
	}
}
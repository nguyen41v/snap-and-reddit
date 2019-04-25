package project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.HashMap;

@SpringBootApplication
public class Project {
	// variables for establishing connections to the database
	public static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
	public static final String DB_URL = "jdbc:mysql://localhost:3306/project?";
	public static final String USER = "root";
	public static final String PASSWORD = null;
	public static HashMap<String, User> tokens = new HashMap<String, User>();
	public static ArrayList<User> tokensArrayList = new ArrayList<User>(); // keep track of which tokens have been used earlier/later

	public static void main(String[] args) {
		SpringApplication.run(Project.class, args);

// aws.com/connectToDB?username=bob

	}
}

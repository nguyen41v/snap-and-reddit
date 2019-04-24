package snap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.HashMap;

@SpringBootApplication
public class App {
    // variables for establishing connections to the database
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost/final?useSSL=NO";
    static final String USER = "root";
    static final String PASSWORD = "whoareyou!";
    public static HashMap<String, User> tokens = new HashMap<String, User>();
    public static ArrayList<User> tokensArrayList = new ArrayList<User>(); // keep track of which tokens have been used earlier/later

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
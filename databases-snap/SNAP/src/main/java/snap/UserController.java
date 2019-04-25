package snap;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.json.JSONObject;
import org.json.JSONException;

import javax.servlet.http.*;
import java.nio.charset.Charset;
import java.sql.*;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Random;

@RestController
@CrossOrigin
public class UserController {
    static private final String startTransaction = "START TRANSACTION";
    static private final String commit = "COMMIT";
    static private final String username = "username";
    static private final String email = "email";
    static private final String password = "password";
    static private final String average = "average";
    static private final String sub_name = "sub_name";
    static private final String info = "info";
    static private final String p_number = "p_number";
    static private final String title = "title";
    static private final String date = "date";
    static private final String content = "content";
    static private final String edited = "edited";
    static private final String edit_date = "edit_date";
    static private final String num_of_comments = "num_of_comments";
    static private final String number = "number";
    static private final String past_benefits = "past_benefits";
    static private final String past_spent = "past_spent";
    static private final String name = "name";
    static private final String reaction = "reaction";
    static private final String amount = "amount";
    static private final String reactions = "reactions";
    static private final String deleted = "deleted";
    static private final String token = "token";
    static private final String balance = "balance";


    public static void close(PreparedStatement ps, Connection conn) {
        try {
            ps = conn.prepareStatement(commit);
            ps.execute();
            ps.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String generateRandomString(int length) {
        byte[] array = new byte[length];
        new Random().nextBytes(array);
        String generatedString = new String(array, Charset.defaultCharset());
        System.out.println(generatedString);
        return generatedString;

    }

    public Boolean checkToken(String username, String token) {
        System.out.println("checking the token with username " + username + "and token " + token);
        if (App.tokens.containsKey(username)) {
            User stored = App.tokens.get(username);
            System.out.println(stored.token);
            if (stored.token.equals(token)) {
                if (App.tokensArrayList.size() >= 100) {
                    App.tokensArrayList.remove(99);
                    App.tokens.remove(username);
                }
                App.tokensArrayList.add(0, stored);
                System.out.println("token validated");
                return true;
            }
        } else {
            System.out.println("no user with that name found...");
        }
        System.out.println("token not validated");
        return false;
    }


    @RequestMapping(value = "/validate", method = RequestMethod.GET) // <-- setup the endpoint URL at /validate with the HTTP GET method
    public ResponseEntity<String> validate(HttpServletRequest request) {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Content-Type", "application/json");
        String username = request.getParameter(UserController.username);
        String token = request.getParameter(UserController.token);
        System.out.println(token);
        System.out.println(username);
        if (!checkToken(username, token)) {
            return new ResponseEntity("{\"message\": \"invalid token\"}", responseHeaders, HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity("{\"message\": \"valid token\"}", responseHeaders, HttpStatus.OK);
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST) // <-- setup the endpoint URL at /register with the HTTP POST method
    public ResponseEntity<String> register(@RequestBody String body, HttpServletRequest request) {
        System.out.println(body); // debugging
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Content-Type", "application/json");
        try {
            JSONObject temp = new JSONObject(body);
            // Grabbing username, password, and email from request body
            String username = temp.getString(UserController.username);
            String password = temp.getString(UserController.password);
            String email = temp.getString(UserController.email);
            // Initializing a MessageDigest object which will allow us to digest a String with SHA-256
            MessageDigest digest = null;
            String hashedKey = null;
            try {
                digest = MessageDigest.getInstance("SHA-256"); // digest algorithm set to SHA-256
                // Converts the password to SHA-256 bytes. Then the bytes are converted to
                // hexadecimal with the helper method written below
                hashedKey = bytesToHex(digest.digest(password.getBytes("UTF-8")));
            } catch (Exception e) {
                e.printStackTrace();
            }
            Connection conn = null;
            PreparedStatement ps = null;
            String query;
            try {
                Class.forName(App.JDBC_DRIVER);
                conn = DriverManager.getConnection(App.DB_URL, App.USER, App.PASSWORD);
                ps = conn.prepareStatement(startTransaction);
                ps.execute();
                // check if username or email have already been registered
                query = ("SELECT * FROM Users WHERE username = ? UNION SELECT * FROM Users WHERE email = ?;");
                ps = conn.prepareStatement(query);
                ps.setString(1, username);
                ps.setString(2, email);
                ResultSet resultSet = ps.executeQuery();
                if (resultSet.next()) {
                    if (resultSet.getString(email).equals(email)) {
                        System.out.println("email"); // debugging
                        return new ResponseEntity("{\"message\":\"email already registered\"}", responseHeaders,
                                HttpStatus.BAD_REQUEST);
                    } else {
                        System.out.println("user");	// debugging
                        return new ResponseEntity("{\"message\":\"username taken\"}", responseHeaders, HttpStatus.BAD_REQUEST);
                    }
                }

                // add new user to User table
                query = "INSERT INTO Users (username, email, password) VALUES (?,?,?)";
                ps = conn.prepareStatement(query);
                ps.setString(1, username);
                ps.setString(2, email);
                ps.setString(3, hashedKey);

                System.out.println(ps); // debugging
                ps.executeUpdate();
                close(ps, conn);
                return new ResponseEntity("{\"message\": \"successfully registered\"}", responseHeaders, HttpStatus.OK);
            } catch (SQLException e) {
                e.printStackTrace();
                return new ResponseEntity("{\"message\": \"error occurred\"}", responseHeaders, HttpStatus.BAD_REQUEST);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return new ResponseEntity("{\"message\": \"error occurred\"}", responseHeaders, HttpStatus.BAD_REQUEST);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            System.out.println("not JSON");
            return new ResponseEntity(
                    "{\"message\":\"response body was not in a proper JSON format\", \"original message\": \"" + body
                            + "\"}",
                    responseHeaders, HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET) // <-- setup the endpoint URL at /login with the HTTP GET method
    public ResponseEntity<String> login(HttpServletRequest request) {
        // Grabbing username and password parameters from URL
        String username = request.getParameter(UserController.username);
        String password = request.getParameter(UserController.password);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Content-Type", "application/json");
        MessageDigest digest = null;
        String hashedKey = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            //Hashing the input password so that we have something to compare with the stored hashed password
            hashedKey = bytesToHex(digest.digest(password.getBytes("UTF-8")));
        } catch(Exception e) {
            e.printStackTrace();
        }
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            Class.forName(App.JDBC_DRIVER);
            conn = DriverManager.getConnection(App.DB_URL, App.USER, App.PASSWORD);
            String query = ("SELECT * FROM Users WHERE username = ?;");
            ps = conn.prepareStatement(query);
            ps.setString(1, username);
            System.out.print(ps);
            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()) {
                if (resultSet.getString(UserController.password).equals(hashedKey)) {
                    String newToken = generateRandomString(10);
                    System.out.println(newToken);
                    User user = new User(username, newToken);
                    App.tokensArrayList.add(0, user);
                    App.tokens.put(username, user);
                    return new ResponseEntity("{\"message\":\"user logged in\",\"token\":\"" + newToken +"\"}", responseHeaders, HttpStatus.OK);
                } else {
                    return new ResponseEntity("{\"message\":\"username/password combination is incorrect\"}", responseHeaders, HttpStatus.BAD_REQUEST);
                }
            }
            ps.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return new ResponseEntity("{\"message\":\"username not registered\"}", responseHeaders, HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value = "/balance", method = RequestMethod.GET) // <-- setup the endpoint URL at /balance with the HTTP GET method
    public ResponseEntity<String> getBalance(HttpServletRequest request) {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Content-Type", "application/json");
        String username = request.getParameter(UserController.username);
//h
        JSONObject balanceInfo = new JSONObject();

        Connection conn = null;
        PreparedStatement ps = null;
        try {
            String query;
            ResultSet resultSet;
            Class.forName(App.JDBC_DRIVER);
            conn = DriverManager.getConnection(App.DB_URL, App.USER, App.PASSWORD);
            query = ("SELECT current_balance as balance, average_meals, ROUND(current_balance/(average_meals * (DAY(LAST_DAY(NOW())) - DAY(NOW()))),2) as average\n" +
                    "FROM Users\n" +
                    "WHERE name = ?;");
            ps = conn.prepareStatement(query);
            ps.setString(1, username);
            System.out.print(ps);
            resultSet = ps.executeQuery();
            resultSet.next();
            balanceInfo.put(UserController.balance, resultSet.getBigDecimal(UserController.balance).toString());
            balanceInfo.put(UserController.average, resultSet.getBigDecimal(UserController.average).toString());

            query = "SELECT SUM(amount) as past_benefits\n" +
                    "FROM Transactions\n" +
                    "WHERE name = ? AND MONTH(date) = MONTH(NOW()) - 1 AND NOT spend;";
            ps = conn.prepareStatement(query);
            ps.setString(1, username);
            System.out.print(ps);
            resultSet = ps.executeQuery();
            resultSet.next();
            balanceInfo.put(UserController.past_benefits, resultSet.getBigDecimal(UserController.past_benefits).toString()); // 0 if null

            query = ("SELECT ROUND(SUM(amount) / (DAY(LAST_DAY(now() - INTERVAL 1 MONTH)) * average_meals),2) as past_spent\n" +
                    "FROM Transactions, (SELECT average_meals FROM Users WHERE name = ?) as A\n" +
                    "WHERE name = ? AND MONTH(date) = MONTH(NOW()) - 1 AND spend;");
            ps = conn.prepareStatement(query);
            ps.setString(1, username);
            ps.setString(2, username);
            System.out.print(ps);
            resultSet = ps.executeQuery();
            resultSet.next();
            balanceInfo.put(UserController.past_spent, resultSet.getBigDecimal(UserController.past_spent).toString());

            ps.close();
            conn.close();

            return new ResponseEntity(balanceInfo.toString(), responseHeaders, HttpStatus.OK);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new ResponseEntity("{\"message\":\"username not registered\"}", responseHeaders, HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value = "/message", method = RequestMethod.POST) // <-- setup the endpoint URL at /message with the HTTP POST method
    public ResponseEntity<String> message(@RequestBody String message, HttpServletRequest request) {
        // String filename = request.getParameter("filename");
        /*
         * Creating http headers object to place into response entity the server will
         * return. This is what allows us to set the content-type to application/json or
         * any other content-type we would want to return
         */
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Content-Type", "application/json");
        try {
            JSONObject temp = new JSONObject(message);
            message = temp.toString();
        } catch (JSONException je) {
            return new ResponseEntity(
                    "{\"message\":\"response body was not in a proper JSON format\", \"original message\": " + message
                            + "}",
                    responseHeaders, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity("{\"message\":\"file created\", \"content\":" + message + "}", responseHeaders,
                HttpStatus.OK);

    }

    @RequestMapping(value = "/getMessage", method = RequestMethod.GET) // <-- setup the endpoint URL at /getMessage with
                                                                       // the HTTP GET method
    public ResponseEntity<String> getMessage(HttpServletRequest request) {
        // String filename = request.getParameter("filename");
        String message = "";

        /*
         * Creating http headers object to place into response entity the server will
         * return. This is what allows us to set the content-type to application/json or
         * any other content-type we would want to return
         */
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Content-Type", "application/json");

        return new ResponseEntity(message, responseHeaders, HttpStatus.OK);

    }

    // Helper method to convert bytes into hexadecimal
    public static String bytesToHex(byte[] in) {
        StringBuilder builder = new StringBuilder();
        for (byte b : in) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }
}
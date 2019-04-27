package snap;

import org.json.JSONArray;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.json.JSONObject;
import org.json.JSONException;

import javax.servlet.http.*;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.sql.*;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
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
    static private final String number = "number";
    static private final String past_benefits = "past_benefits";
    static private final String past_spent = "past_spent";
    static private final String name = "name";
    static private final String state = "state";
    static private final String amount = "amount";
    static private final String type = "type";
    static private final String token = "token";
    static private final String balance = "balance";
    static private final String state_hotline = "state_hotline";
    static private final String state_only_hotline = "state_only_hotline";
    static private final String eligibility = "eligibility";
    static private final String uniform = "uniform";
    static private final String first_day = "first_day";
    static private final String last_day = "last_day";
    static private final String condition1 = "condition1";
    static private final String condition2 = "condition2";
    static private final String condition3 = "condition3";
    static private final String condition4 = "condition4";
    static private final String condition5 = "condition5";
    static private final String phone_number = "phone_number";
    static private final String street = "street";
    static private final String city = "city";
    static private final String zip_code = "zip_code";
    static private final String county = "county";
    static private final String longitude = "longitude";
    static private final String latitude = "latitude";
    static private final String address_line2 = "address_line2";
    static private final String zip4 = "zip4";
    static private final String last_two_ssn = "last_two_ssn";
    static private final String current_balance = "current_balance";
    static private final String average_meals = "average_meals";
    static private final String case_number = "case_number";
    static private final String first_name = "first_name";
    static private final String last_name = "last_name";
    static private final String num_transactions = "num_transactions";
    static private final String birthday = "birthday";
    static private final String middle_initial = "middle_initial";
    static private final String spend = "spend";


    static private final String sub_name = "sub_name";
    static private final String info = "info";
    static private final String p_number = "p_number";
    static private final String title = "title";
    static private final String date = "date";
    static private final String content = "content";
    static private final String edited = "edited";
    static private final String edit_date = "edit_date";
    static private final String num_of_comments = "num_of_comments";
    static private final String deleted = "deleted";

    static private final String day = "day";
    static private final String benefits = "benefits";
    static private final String miles = "miles";
//    static private final String last_day14 = "last_day14";
//    static private final String last_day15 = "last_day15";
//    static private final String last_day16 = "last_day16";
//    static private final String last_day17 = "last_day17";
//    static private final String last_day18 = "last_day18";
//    static private final String last_day19 = "last_day19";
//    static private final String last_day10 = "last_day10";
//    static private final String last_day21 = "last_day21";
//    static private final String last_day31 = "last_day31";
//    static private final String last_day41 = "last_day41";
//    static private final String last_day51 = "last_day51";
//    static private final String last_day61 = "last_day61";


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
            String phone_number = temp.getString(UserController.phone_number);
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
                ps.execute();
                // check if username or email have already been registered
                query = ("SELECT * FROM Users WHERE username = ? UNION SELECT * FROM Users WHERE phone_number = ?;");
                ps = conn.prepareStatement(query);
                ps.setString(1, username);
                ps.setString(2, phone_number);
                ResultSet resultSet = ps.executeQuery();
                if (resultSet.next()) {
                    if (resultSet.getString(phone_number).equals(phone_number)) {
                        System.out.println("email"); // debugging
                        return new ResponseEntity("{\"message\":\"phone number already registered\"}", responseHeaders,
                                HttpStatus.BAD_REQUEST);
                    } else {
                        System.out.println("user");	// debugging
                        return new ResponseEntity("{\"message\":\"username taken\"}", responseHeaders, HttpStatus.BAD_REQUEST);
                    }
                }

                // add new user to User table
                query = "INSERT INTO Users (username, phone_number, password) VALUES (?,?,?)";
                ps = conn.prepareStatement(query);
                ps.setString(1, username);
                ps.setString(2, phone_number);
                ps.setString(3, hashedKey);
                String newToken = generateRandomString(10);
                System.out.println(newToken);
                User user = new User(username, newToken);
                if (App.tokensArrayList.size() == 100) {
                    App.tokens.remove(App.tokensArrayList.remove(99).username); // look at this again fixme
                }
                App.tokensArrayList.add(0, user);
                App.tokens.put(username, user);

                System.out.println(ps); // debugging
                ps.executeUpdate();
                ps.close();
                conn.close();
                return new ResponseEntity("{\"message\": \"successfully registered\",\"token\":" + newToken +"\"}", responseHeaders, HttpStatus.OK);
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
                    if (App.tokensArrayList.size() == 100) {
                        App.tokens.remove(App.tokensArrayList.remove(99).username); // look at this again fixme
                    }
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

        JSONObject balanceInfo = new JSONObject();

        Connection conn = null;
        PreparedStatement ps = null;
        try {
            String query;
            ResultSet resultSet;
            Class.forName(App.JDBC_DRIVER);
            conn = DriverManager.getConnection(App.DB_URL, App.USER, App.PASSWORD);
            query = ("SELECT current_balance as balance, ROUND(current_balance/(average_meals * (DAY(LAST_DAY(NOW())) - DAY(NOW()))),2) as average\n" +
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

    @RequestMapping(value = "/transactions", method = RequestMethod.GET) // <-- setup the endpoint URL at /message with the HTTP POST method
    public ResponseEntity<String> getAllTransactions (HttpServletRequest request) {

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Content-Type", "application/json");
        JSONArray transactions = new JSONArray();
        String username = request.getParameter(UserController.username);
        String token = request.getParameter(UserController.token);
        if (!checkToken(username, token)) {
            return new ResponseEntity("{\"message\": \"invalid token\"}", responseHeaders, HttpStatus.UNAUTHORIZED);
        }
        Connection conn = null;
        PreparedStatement ps = null;
        JSONObject transaction;
        try {
            String query;
            ResultSet resultSet;
            Class.forName(App.JDBC_DRIVER);
            conn = DriverManager.getConnection(App.DB_URL, App.USER, App.PASSWORD);
            query = ("SELECT * FROM Transactions WHERE name = ?;");
            ps = conn.prepareStatement(query);
            System.out.print(ps);
            resultSet = ps.executeQuery();
            while (resultSet.next()){
                transaction = new JSONObject();
                transaction.put(UserController.date, resultSet.getString(UserController.date));
                transaction.put(UserController.spend, resultSet.getBoolean(UserController.spend));
                transaction.put(UserController.amount, resultSet.getString(UserController.amount));
                transactions.put(transaction);
            }
            ps.close();
            conn.close();
            return new ResponseEntity(transactions.toString(), responseHeaders, HttpStatus.OK);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new ResponseEntity("{\"message\":\"invalid state abbreviation used\"}", responseHeaders, HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value = "/transactions", method = RequestMethod.POST) // <-- setup the endpoint URL at /message with the HTTP POST method
    public ResponseEntity<String> postTransactions (@RequestBody String body, HttpServletRequest request) {

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Content-Type", "application/json");
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            JSONObject temp = new JSONObject(body);
            // Grabbing post info from request body
            String username = temp.getString(UserController.username);
            BigDecimal amount = new BigDecimal(temp.getString(UserController.amount));
            int number;
            Boolean spend = temp.getBoolean(UserController.spend);
            String token = temp.getString(UserController.token);
            if (!checkToken(username, token)) {
                return new ResponseEntity("{\"message\": \"invalid token\"}", responseHeaders, HttpStatus.UNAUTHORIZED);
            }
            String query;
            ResultSet resultSet;
            Class.forName(App.JDBC_DRIVER);
            conn = DriverManager.getConnection(App.DB_URL, App.USER, App.PASSWORD);
            query = ("SELECT * FROM Users WHERE username = ?;");
            ps = conn.prepareStatement(query);
            ps.setString(1, username);
            resultSet = ps.executeQuery();
            if (resultSet.next()) {
                number = resultSet.getInt(num_transactions) + 1;
            } else {
                return new ResponseEntity("{\"message\": \"something went wrong\"}", responseHeaders, HttpStatus.BAD_REQUEST);
            }
            if (temp.has(UserController.date)) {
                query = ("INSERT INTO Transactions (username, number, spend, amount, date) " +
                        "VALUES (?, ?, ?, ?, ?);");
                ps = conn.prepareStatement(query);
                ps.setString(1, username);
                ps.setInt(2, number);
                ps.setBoolean(3, spend);
                ps.setBigDecimal(4, amount);
                ps.setDate(5, Date.valueOf(temp.getString(UserController.date)));
            } else {
                query = ("INSERT INTO Transactions (username, number, spend, amount) " +
                        "VALUES (?, ?, ?, ?);");
                ps = conn.prepareStatement(query);
                ps.setString(1, username);
                ps.setInt(2, number);
                ps.setBoolean(3, spend);
                ps.setBigDecimal(4, amount);
            }
            System.out.print(ps);
            ps.executeUpdate();
            ps.close();
            conn.close();
            return new ResponseEntity("{\"message\": \"transaction made\"}", responseHeaders, HttpStatus.OK);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new ResponseEntity("{\"message\":\"transaction not made\"}", responseHeaders, HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value = "/stateInfo", method = RequestMethod.GET) // <-- setup the endpoint URL at /message with the HTTP POST method
    public ResponseEntity<String> state_info(HttpServletRequest request) {

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Content-Type", "application/json");
        JSONObject state_info = new JSONObject();
        String state = request.getParameter(UserController.state);
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            String query;
            ResultSet resultSet;
            Class.forName(App.JDBC_DRIVER);
            conn = DriverManager.getConnection(App.DB_URL, App.USER, App.PASSWORD);
            query = ("SELECT *\n" +
                    "FROM States\n" +
                    "WHERE state=?;");
            ps = conn.prepareStatement(query);
            ps.setString(1, state);
            System.out.print(ps);
            resultSet = ps.executeQuery();
            resultSet.next();
            state_info.put(UserController.name, resultSet.getString(UserController.name));
            state_info.put(UserController.state_hotline, resultSet.getString(UserController.state_hotline));
            state_info.put(UserController.eligibility, resultSet.getString(UserController.eligibility));
            state_info.put(UserController.type, resultSet.getString(UserController.type));
            state_info.put(UserController.uniform, resultSet.getString(UserController.uniform));
            state_info.put(UserController.first_day, resultSet.getString(UserController.first_day));
            state_info.put(UserController.last_day, resultSet.getString(UserController.last_day));

            query = "SELECT state_only_hotline\n" +
                    "FROM State_specific\n" +
                    "WHERE state=?;";
            ps = conn.prepareStatement(query);
            ps.setString(1, state);
            System.out.print(ps);
            resultSet = ps.executeQuery();
            ArrayList<String> s_only_hotlines = new ArrayList<>();
            while (resultSet.next()) {
                s_only_hotlines.add(resultSet.getString(UserController.state_only_hotline));
            }
            state_info.put(UserController.state_only_hotline, s_only_hotlines);

            query = "SELECT *\n" +
                    "FROM Benefits\n" +
                    "WHERE state=?;";
            ps = conn.prepareStatement(query);
            ps.setString(1, state);
            System.out.print(ps);
            resultSet = ps.executeQuery();
            JSONObject benefits = new JSONObject();
            while (resultSet.next()) {
                JSONArray day = new JSONArray();
                day.put(resultSet.getString(UserController.condition1));
                day.put(resultSet.getString(UserController.condition2));
                day.put(resultSet.getString(UserController.condition3));
                day.put(resultSet.getString(UserController.condition4));
                day.put(resultSet.getString(UserController.condition5));
                benefits.put(Integer.toString(resultSet.getInt(UserController.day)), day);
            }
            state_info.put(UserController.benefits, benefits);
            ps.close();
            conn.close();

            return new ResponseEntity(state_info.toString(), responseHeaders, HttpStatus.OK);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new ResponseEntity("{\"message\":\"invalid state abbreviation used\"}", responseHeaders, HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value = "/countyInfo", method = RequestMethod.GET) // <-- setup the endpoint URL at /message with the HTTP POST method
    public ResponseEntity<String> county_info (HttpServletRequest request) {

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Content-Type", "application/json");
        JSONObject state_info = new JSONObject();
        String state = request.getParameter(UserController.state);
        String county = request.getParameter(UserController.county);
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            String query;
            ResultSet resultSet;
            Class.forName(App.JDBC_DRIVER);
            conn = DriverManager.getConnection(App.DB_URL, App.USER, App.PASSWORD);
            query = ("SELECT *\n" +
                    "FROM Counties\n" +
                    "WHERE state=? AND county=?;");
            ps = conn.prepareStatement(query);
            ps.setString(1, state);
            ps.setString(2, county);2
            System.out.print(ps);
            resultSet = ps.executeQuery();
            resultSet.next();
            state_info.put(UserController.phone_number, resultSet.getString(UserController.phone_number));
            state_info.put(UserController.street, resultSet.getString(UserController.street));
            state_info.put(UserController.city, resultSet.getString(UserController.city));
            state_info.put(UserController.zip_code, resultSet.getString(UserController.zip_code));
            ps.close();
            conn.close();
            return new ResponseEntity(state_info.toString(), responseHeaders, HttpStatus.OK);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new ResponseEntity("{\"message\":\"invalid state abbreviation used\"}", responseHeaders, HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value = "/stores", method = RequestMethod.GET) // <-- setup the endpoint URL at /message with the HTTP POST method
    public ResponseEntity<String> stores (HttpServletRequest request) {

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Content-Type", "application/json");
        JSONObject stores = new JSONObject();
        BigDecimal longitude = new BigDecimal(request.getParameter(UserController.longitude));
        BigDecimal latitude = new BigDecimal(request.getParameter(UserController.latitude));
        int miles = Integer.parseInt(request.getParameter(UserController.miles));
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            String query;
            ResultSet resultSet;
            Class.forName(App.JDBC_DRIVER);
            conn = DriverManager.getConnection(App.DB_URL, App.USER, App.PASSWORD);
            query = ("SELECT * " +
                    "FROM Stores " +
                    "WHERE ? >= (57 * SQRT(POW(longitude - ?, 2) + POW(latitude - ?, 2)));");
            ps = conn.prepareStatement(query);
            ps.setInt(1, miles);
            ps.setBigDecimal(2, longitude);
            ps.setBigDecimal(2, latitude);
            System.out.print(ps);
            resultSet = ps.executeQuery();
            while (resultSet.next()){
                stores.put(UserController.phone_number, resultSet.getString(UserController.phone_number));
                stores.put(UserController.street, resultSet.getString(UserController.street));
                stores.put(UserController.city, resultSet.getString(UserController.city));
                stores.put(UserController.zip_code, resultSet.getString(UserController.zip_code));
            }
            ps.close();
            conn.close();
            return new ResponseEntity(stores.toString(), responseHeaders, HttpStatus.OK);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new ResponseEntity("{\"message\":\"invalid state abbreviation used\"}", responseHeaders, HttpStatus.BAD_REQUEST);
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

    @RequestMapping(value = "/getMessage", method = RequestMethod.GET) // <-- setup the endpoint URL at /getMessage with     // the HTTP GET method
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
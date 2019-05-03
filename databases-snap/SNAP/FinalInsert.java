import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.FileNotFoundException;
import java.io.*;
import java.util.*;
import java.sql.*;


public class FinalInsert {

    // establish connections to the database says Celia
    static private final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static private final String DATABASE = "snap"; // name of database
    static private final String DB_URL = "jdbc:mysql://localhost:3306/" + DATABASE;

    // can also save login inform in a dbparam.txt and parse into program
    static private final String USER = "root";
    static private final String PASSWORD = null;

    // keep track of types
    static private final int isString = 0; // String
    static private final int isInt = 1; // tinyint, smallint, mediumint, int
    static private final int isDate = 2; // date
    static private final int isBoolean = 3; // boolean
    static private final int isFloat = 4; // float


    // so I can use one method for all the tables
    static private final Attribute state = new Attribute("state", isString);
    static private final Attribute name = new Attribute("name", isString);
    static private final Attribute state_hotline = new Attribute("state_hotline", isString);
    static private final Attribute state_only_hotline = new Attribute("state_only_hotline", isString);
    static private final Attribute eligibility = new Attribute("eligibility", isBoolean);
    static private final Attribute type = new Attribute("type", isString);
    static private final Attribute uniform = new Attribute("uniform", isBoolean);
    static private final Attribute first_day = new Attribute("first_day", isInt);
    static private final Attribute last_day = new Attribute("last_day", isInt);
    static private final Attribute day = new Attribute("day", isInt);
    static private final Attribute condition1 = new Attribute("condition1", isString);
    static private final Attribute condition2 = new Attribute("condition2", isString);
    static private final Attribute condition3 = new Attribute("condition3", isString);
    static private final Attribute condition4 = new Attribute("condition4", isString);
    static private final Attribute condition5 = new Attribute("condition5", isString);
    static private final Attribute phone_number = new Attribute("phone_number", isString);
    static private final Attribute street = new Attribute("street", isString);
    static private final Attribute city = new Attribute("city", isString);
    static private final Attribute zip_code = new Attribute("zip_code", isString);
    static private final Attribute county = new Attribute("county", isString);
    static private final Attribute longitude = new Attribute("longitude", isFloat);
    static private final Attribute latitude = new Attribute("latitude", isFloat);
    static private final Attribute password = new Attribute("password", isString);
    static private final Attribute first_name = new Attribute("first_name", isString);
    static private final Attribute last_name = new Attribute("last_name", isString);
    static private final Attribute middle_initial = new Attribute("middle_initial", isString);
    static private final Attribute email = new Attribute("email", isString);
    static private final Attribute average_meals = new Attribute("average_meals", isInt);
    static private final Attribute address_line2 = new Attribute("address_line2", isString);
    static private final Attribute zip4 = new Attribute("zip4", isString);
    static private final Attribute application = new Attribute("application", isString);



    private static class Attribute {
        String name;
        int type;

        Attribute(String name, int type) {
            this.name = name;
            this.type = type;
        }
    }

    static final HashMap<String, ArrayList<Attribute>> tables = new HashMap<String, ArrayList<Attribute>>(){
        {
            put("States", new ArrayList<>(Arrays.asList(state, name, state_hotline, eligibility, type, uniform, first_day, last_day, application)));
            put("State_specific", new ArrayList<>(Arrays.asList(state, state_only_hotline)));
            put("Benefits", new ArrayList<>(Arrays.asList(state, day, condition1, condition2, condition3, condition4, condition5)));
            put("Local_offices", new ArrayList<>(Arrays.asList(phone_number, street, city, state, zip_code, county)));
            put("Stores", new ArrayList<>(Arrays.asList(name, longitude, latitude, street, address_line2, city, state, zip_code, zip4, county)));
            put("Users", new ArrayList<>(Arrays.asList(name, password, phone_number, email, first_name, middle_initial, last_name, city, zip_code, state, county)));
                // need to hash user's password from sheet before putting it in so that it is inputted correctly
        }};

    private static List<String> getRecordFromLine(String line) {
        List<String> values = new ArrayList<String>();
        try (Scanner rowScanner = new Scanner(line)) {
            rowScanner.useDelimiter(",");
            while (rowScanner.hasNext()) {
                values.add(rowScanner.next());
            }
        }
        return values;
    }

    private static void insert(String tablename, String filename) {
        Connection conn = null;
        PreparedStatement ps = null;
        ArrayList<String> errors = new ArrayList<>();
        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
            List<List<String>> records = new ArrayList<>();
            try (Scanner scan = new Scanner (new File (filename));) {
                while (scan.hasNextLine()) {
                    records.add(getRecordFromLine(scan.nextLine()));
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            System.out.println("Insert into the " + tablename + " table...");
            ArrayList<Attribute> currentAtts = tables.get(tablename); // reference for current att in table
            ArrayList<String> atts; // array of atts in current tuple
            String theAtts; // string of atts in current tuple for sql insert statement
            for (int i = 0; i < records.size(); i++) {
                atts = new ArrayList<>();
                //
                // get the attributes that are present in data so it can be in the sql statement
                // (only have atts in insert that is present)
//                System.out.println(records);
//                for (Attribute att : currentAtts) {
//                    System.out.println(att.name);
//                }
                for (int k = 0; k < records.get(i).size(); k++) {
                    if (!records.get(i).get(k).isEmpty()) { // I put in isEmpty because what if you wanted "   " as a value . . .
                        atts.add(currentAtts.get(k).name);
                    }
                }
                theAtts = String.join(", ", atts);
                String questionMarks = new String(new char[atts.size() - 1]).replace("\0", "?, ");
                String insertTableSQL = "INSERT INTO " + tablename + " (" + theAtts + ")" +
                        " VALUES (" + questionMarks +  "?)";
                ps = conn.prepareStatement(insertTableSQL);
                // replace ? with actual values
                int x = 0; // keep track of which index of ps we're at
                // since we don't want to increase index if there's no value for an att
                for (int j = 0; j < records.get(i).size(); j++) {
                    if (!records.get(i).get(j).isEmpty()) {
                        x++;
                        if (currentAtts.get(j).type == 0) {
                            ps.setString(x, records.get(i).get(j).replace("\"", ""));
                        } else if (currentAtts.get(j).type == 1) {
                            ps.setInt(x, Integer.parseInt(records.get(i).get(j)));
                        } else if (currentAtts.get(j).type == 2) {
                            ps.setDate(x, Date.valueOf(records.get(i).get(j).replace("\"", "")));
                        } else if (currentAtts.get(j).type == 3) {
                            ps.setBoolean(x, Boolean.parseBoolean(records.get(i).get(j)));
                        } else if (currentAtts.get(j).type == 4) {
                            ps.setFloat(x, Float.parseFloat(records.get(i).get(j)));
                        }
                    }
                }
                try {
                    ps.executeUpdate();
                } catch (SQLIntegrityConstraintViolationException e){
                    errors.add(records.get(i).get(1));
                }
            }
            ps.close();
            conn.close();
        }catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        for (String s : errors) {
            System.out.println(s);
        }
    }


    public static void main( String[] args ) {
        if (args.length == 0 || args.length % 2 != 0) {
            System.err.println("Usage: java Insert <table name> <table csv file>\nNeeds an even number of arguments");
            System.exit(1);
        }
        for (int i = 0; i < args.length; i += 2) {
            insert(args[i], args[i + 1]);
        }
    }
}


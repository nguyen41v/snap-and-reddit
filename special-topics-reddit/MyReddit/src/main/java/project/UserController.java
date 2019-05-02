package project;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import javax.servlet.http.*;
import java.nio.charset.Charset;
import java.sql.*;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Random;

@RestController
@CrossOrigin
public class UserController {
    static private final String startTransaction = "START TRANSACTION";
    static private final String commit = "COMMIT";
    static private final String username = "username";
    static private final String email = "email";
    static private final String password = "password";
    static private final String nickname = "nickname";
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
    static private final String c_number = "c_number";
    static private final String item = "item";
    static private final String name = "name";
    static private final String reaction = "reaction";
    static private final String amount = "amount";
    static private final String reactions = "reactions";
    static private final String deleted = "deleted";
    static private final String token = "token";

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
	    if (Project.tokens.containsKey(username)) {
	        User stored = Project.tokens.get(username);
            System.out.println(stored.token);
            if (stored.token.equals(token)) {
	            if (Project.tokensArrayList.size() >= 100) {
	                Project.tokensArrayList.remove(99);
	                Project.tokens.remove(username);
                }
                Project.tokensArrayList.add(0, stored);
	            System.out.println("token validated");
	            return true;
            }
        } else {
	        System.out.println("no user with that name found...");
        }
        System.out.println("token not validated");
        return false;
    }


    @RequestMapping(value = "/validate", method = RequestMethod.GET) // <-- setup the endpoint URL at /post with the HTTP POST method
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
		JSONObject response = new JSONObject();
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
				Class.forName(Project.JDBC_DRIVER);
				conn = DriverManager.getConnection(Project.DB_URL, Project.USER, Project.PASSWORD);
				ps = conn.prepareStatement(startTransaction);
				ps.execute();
				// check if username or email have already been registered
				query = ("SELECT * FROM User WHERE username = ? UNION SELECT * FROM User WHERE email = ?;");
				ps = conn.prepareStatement(query);
				ps.setString(1, username);
				ps.setString(2, email);
				ResultSet resultSet = ps.executeQuery();
				if (resultSet.next()) {
					if (resultSet.getString(email).equals(email)) {
						System.out.println("email"); // debugging
                        response.put("message", "Email already registered");
						return new ResponseEntity(response.toString(), responseHeaders,
								HttpStatus.BAD_REQUEST);
					} else {
						System.out.println("user");	// debugging
                        response.put("message", "Username already taken");
                        return new ResponseEntity(response.toString(), responseHeaders, HttpStatus.BAD_REQUEST);
					}
				}

				// add new user to User table
				query = "INSERT INTO User (username, email, password) VALUES (?,?,?)";
				ps = conn.prepareStatement(query);
				ps.setString(1, username);
				ps.setString(2, email);
				ps.setString(3, hashedKey);

				System.out.println(ps); // debugging
                String newToken = generateRandomString(10);
                System.out.println(newToken);
                User user = new User(username, newToken);
                if (Project.tokensArrayList.size() == 100) {
                    Project.tokens.remove(Project.tokensArrayList.remove(99).username); // look at this again fixme
                }
                Project.tokensArrayList.add(0, user);
                Project.tokens.put(username, user);
				ps.executeUpdate();
				close(ps, conn);
                response.put("message", "Successfully registered");
                response.put("token", newToken);
                return new ResponseEntity(response.toString(), responseHeaders, HttpStatus.OK);
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		} catch (JSONException e) {
			e.printStackTrace();
			System.out.println("not JSON");
		}
        response.put("message", "An error occurred, please try again later");
        return new ResponseEntity(response.toString(), responseHeaders, HttpStatus.BAD_REQUEST);

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
			Class.forName(Project.JDBC_DRIVER);
			conn = DriverManager.getConnection(Project.DB_URL, Project.USER, Project.PASSWORD);
			String query = ("SELECT * FROM User WHERE username = ?;");
			ps = conn.prepareStatement(query);
			ps.setString(1, username);
			System.out.print(ps);
			ResultSet resultSet = ps.executeQuery();
			if (resultSet.next()) {
				if (resultSet.getString(UserController.password).equals(hashedKey)) {
				    String newToken = generateRandomString(10);
				    System.out.println(newToken);
				    User user = new User(username, newToken);
                    if (Project.tokensArrayList.size() == 100) {
                        Project.tokens.remove(Project.tokensArrayList.remove(99).username); // look at this again fixme
                    }
				    Project.tokensArrayList.add(0, user);
				    Project.tokens.put(username, user);
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


    @RequestMapping(value = "/post", method = RequestMethod.POST) // <-- setup the endpoint URL at /post with the HTTP POST method
    public ResponseEntity<String> post(@RequestBody String body, HttpServletRequest request) {
        System.out.println(body); // debugging
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Content-Type", "application/json");
        try {
            JSONObject temp = new JSONObject(body);
            // Grabbing post info from request body
            String username = temp.getString(UserController.username);
            String sub_name = temp.getString(UserController.sub_name);
            String title = temp.getString(UserController.title);
            String content = temp.getString(UserController.content);
            String token = temp.getString(UserController.token);
            if (!checkToken(username, token)) {
                return new ResponseEntity("{\"message\": \"invalid token\"}", responseHeaders, HttpStatus.UNAUTHORIZED);
            }
            Connection conn = null;
            PreparedStatement ps = null;
            String query;
            try {
                Class.forName(Project.JDBC_DRIVER);
                conn = DriverManager.getConnection(Project.DB_URL, Project.USER, Project.PASSWORD);
                // add new post
                query = "INSERT INTO Post (sub_name, title, content, username) VALUES (?,?,?,?)";
                ps = conn.prepareStatement(query);
                ps.setString(1, sub_name);
                ps.setString(2, title);
                ps.setString(3, content);
                ps.setString(4, username);
                System.out.println(ps); // debugging
                ps.executeUpdate();
                ps.close();
                conn.close();
                return new ResponseEntity("{\"message\": \"post was posted\"}", responseHeaders, HttpStatus.OK);
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            System.out.println("not JSON");
            return new ResponseEntity(
                    "{\"message\":\"response body was not in a proper JSON format\", \"original message\": \"" + body
                            + "\"}",
                    responseHeaders, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity("{\"message\": \"post was not posted due to an error\"}", responseHeaders, HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value = "/post", method = RequestMethod.GET) // <-- setup the endpoint URL at /post with the HTTP GET method
    public ResponseEntity<String> getPosts(HttpServletRequest request) {
        String sub = request.getParameter(UserController.sub_name);
        JSONArray posts = new JSONArray();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Content-Type", "application/json");
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        String query;
        JSONObject postContent;
        try {
            Class.forName(Project.JDBC_DRIVER);
            conn = DriverManager.getConnection(Project.DB_URL, Project.USER, Project.PASSWORD);

            // get reactions of post
            query = "SELECT * FROM PReaction WHERE sub_name = ?;";
            ps = conn.prepareStatement(query);
            ps.setString(1, sub);
            System.out.println(ps);
            resultSet = ps.executeQuery();
            HashMap<Integer, HashMap<Integer, Integer>> preactions = new HashMap<Integer, HashMap<Integer, Integer>>();
            int reaction, amount, p_number;
            while (resultSet.next()) {
                p_number = resultSet.getInt(UserController.p_number);
                reaction = resultSet.getInt(UserController.reaction);
                amount = resultSet.getInt(UserController.amount);
                if (preactions.containsKey(p_number)) {
                    preactions.get(p_number).put(reaction, amount);
                } else {
                    HashMap<Integer, Integer> temp = new HashMap<>();
                    temp.put(reaction, amount);
                    preactions.put(p_number, temp);
                }
            }

            // get post content
            query = "SELECT * FROM Post WHERE sub_name = ? ORDER BY date DESC;";
            ps = conn.prepareStatement(query);
            ps.setString(1, sub);
            System.out.println(ps);
            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                postContent = new JSONObject();
                postContent.put(UserController.p_number, resultSet.getString(UserController.p_number));
                postContent.put(UserController.sub_name, resultSet.getString(UserController.sub_name));
                postContent.put(UserController.title, resultSet.getString(UserController.title));
                postContent.put(UserController.date, resultSet.getString(UserController.date));
                postContent.put(UserController.username, resultSet.getString(UserController.username));
                postContent.put(UserController.content, resultSet.getString(UserController.content));
                if (resultSet.getBoolean(UserController.edited)) {
                    postContent.put(UserController.edited, true);
                    postContent.put(UserController.edit_date, resultSet.getString(UserController.edit_date));
                } else {
                    postContent.put(UserController.edited, false);
                }
                postContent.put(UserController.num_of_comments, resultSet.getInt(UserController.num_of_comments));
                posts.put(postContent);
            }
            close(ps, conn);
            return new ResponseEntity(posts.toString(), responseHeaders, HttpStatus.OK);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return new ResponseEntity("{\"message\":\"could not get messages\"}", responseHeaders, HttpStatus.BAD_REQUEST);

    }

    // /post?sub_name=sfad&p_number=1&deleted=true
    @RequestMapping(value = "/post", method = RequestMethod.DELETE) // <-- setup the endpoint URL at /delete with the HTTP DELETE method
    public ResponseEntity<String> deletePost(@RequestBody String body, HttpServletRequest request) {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Content-Type", "application/json");
        try {
            JSONObject temp = new JSONObject(body);
            String sub_name = temp.getString(UserController.sub_name);
            int p_number = temp.getInt(UserController.p_number);
            Boolean deleted = temp.getBoolean(UserController.deleted); // let people undelete their comments
            String username = temp.getString(UserController.username);
            String token = temp.getString(UserController.token);
            if (!checkToken(username, token)) {
                return new ResponseEntity("{\"message\": \"invalid token\"}", responseHeaders, HttpStatus.UNAUTHORIZED);
            }
            Connection conn = null;
            PreparedStatement ps = null;
            String query;
            try {
                Class.forName(Project.JDBC_DRIVER);
                conn = DriverManager.getConnection(Project.DB_URL, Project.USER, Project.PASSWORD);
                // add new post
                query = "UPDATE Post SET deleted = ? WHERE p_number = ? AND sub_name = ? AND username = ?";
                ps = conn.prepareStatement(query);
                ps.setBoolean(1, deleted);
                ps.setInt(2, p_number);
                ps.setString(3, sub_name);
                ps.setString(4, username);
                System.out.println(ps); // debugging
                ps.executeUpdate();
                ps.close();
                conn.close();
                return new ResponseEntity("{\"message\": \"comment was posted\"}", responseHeaders, HttpStatus.OK);
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            System.out.println("not JSON");
        }
        return new ResponseEntity("{\"message\": \"comment was not edited due to an error\"}", responseHeaders, HttpStatus.BAD_REQUEST);
    }


    @RequestMapping(value = "/editPost", method = RequestMethod.POST) // <-- setup the endpoint URL at /editPost with the HTTP POST method
    public ResponseEntity<String> editPost(@RequestBody String body, HttpServletRequest request) {
        System.out.println(body); // debugging
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Content-Type", "application/json");
        try {
            JSONObject temp = new JSONObject(body);
            // Grabbing post info from request body
            int p_number = temp.getInt(UserController.p_number);
            String content = temp.getString(UserController.content);
            String username = temp.getString(UserController.username);
            String token = temp.getString(UserController.token);
            if (!checkToken(username, token)) {
                return new ResponseEntity("{\"message\": \"invalid token\"}", responseHeaders, HttpStatus.UNAUTHORIZED);
            }

            Connection conn = null;
            PreparedStatement ps = null;
            String query;
            try {
                Class.forName(Project.JDBC_DRIVER);
                conn = DriverManager.getConnection(Project.DB_URL, Project.USER, Project.PASSWORD);
                // add new post
                query = "UPDATE Post SET content = ?, edited = ? WHERE p_number = ? AND username = ?";
                ps = conn.prepareStatement(query);
                ps.setString(1, content);
                ps.setBoolean(2, true);
                ps.setInt(3, p_number);
                ps.setString(4, username);
                System.out.println(ps); // debugging
                ps.executeUpdate();
                ps.close();
                conn.close();
                return new ResponseEntity("{\"message\": \"comment was posted\"}", responseHeaders, HttpStatus.OK);
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            System.out.println("not JSON");
            return new ResponseEntity(
                    "{\"message\":\"response body was not in a proper JSON format\", \"original message\": \"" + body
                            + "\"}",
                    responseHeaders, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity("{\"message\": \"comment was not edited due to an error\"}", responseHeaders, HttpStatus.BAD_REQUEST);
    }


    @RequestMapping(value = "/comment", method = RequestMethod.POST) // <-- setup the endpoint URL at /comment with the HTTP POST method
    public ResponseEntity<String> comment(@RequestBody String body, HttpServletRequest request) {
        System.out.println(body); // debugging
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Content-Type", "application/json");
        try {
            JSONObject temp = new JSONObject(body);
            // Grabbing post info from request body
            String username = temp.getString(UserController.username);
            int p_number = temp.getInt(UserController.p_number);
            int number;
            String content = temp.getString(UserController.content);
            String token = temp.getString(UserController.token);
            if (!checkToken(username, token)) {
                return new ResponseEntity("{\"message\": \"invalid token\"}", responseHeaders, HttpStatus.UNAUTHORIZED);
            }

            Connection conn = null;
            PreparedStatement ps = null;
            String query;
            try {
                Class.forName(Project.JDBC_DRIVER);
                conn = DriverManager.getConnection(Project.DB_URL, Project.USER, Project.PASSWORD);
                query = "SELECT * FROM Post WHERE p_number = ?";
                ps = conn.prepareStatement(query);
                ps.setInt(1, p_number);
                ResultSet resultSet;
                resultSet = ps.executeQuery();
                if (resultSet.next()) {
                    number = resultSet.getInt("num_of_comments") + 1;
                } else {
                    return new ResponseEntity("{\"message\": \"comment was not posted because that post doesn't exist\"}", responseHeaders, HttpStatus.BAD_REQUEST);
                }
                try {
                    // add comment reply
                    int c_number = temp.getInt(UserController.c_number);
                    query = "INSERT INTO Comment (p_number, number, content, username, c_number) VALUES (?,?,?,?,?)";
                    ps = conn.prepareStatement(query);
                    ps.setInt(1, p_number);
                    ps.setInt(2, number);
                    ps.setString(3, content);
                    ps.setString(4, username);
                    ps.setInt(5, c_number);
                } catch (JSONException e) {
                    // add comment
                    query = "INSERT INTO Comment (p_number, number, content, username) VALUES (?,?,?,?)";
                    ps = conn.prepareStatement(query);
                    ps.setInt(1, p_number);
                    ps.setInt(2, number);
                    ps.setString(3, content);
                    ps.setString(4, username);
                }
                System.out.println(ps); // debugging
                ps.executeUpdate();
                ps.close();
                conn.close();
                return new ResponseEntity("{\"message\": \"comment was posted\"}", responseHeaders, HttpStatus.OK);
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            System.out.println("not JSON");
            return new ResponseEntity(
                    "{\"message\":\"response body was not in a proper JSON format\", \"original message\": \"" + body
                            + "\"}",
                    responseHeaders, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity("{\"message\": \"comment was not posted due to an error\"}", responseHeaders, HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value = "/replyComment", method = RequestMethod.POST) // <-- setup the endpoint URL at /replyComment with the HTTP POST method
    public ResponseEntity<String> replyComment(@RequestBody String body, HttpServletRequest request) {
        System.out.println(body); // debugging
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Content-Type", "application/json");
        try {
            JSONObject temp = new JSONObject(body);
            // Grabbing post info from request body
            String username = temp.getString(UserController.username);
            int p_number = temp.getInt(UserController.p_number);
            int number = temp.getInt(UserController.number);
            String content = temp.getString(UserController.content);
            int c_number = temp.getInt(UserController.c_number);
            String token = temp.getString(UserController.token);
            if (!checkToken(username, token)) {
                return new ResponseEntity("{\"message\": \"invalid token\"}", responseHeaders, HttpStatus.UNAUTHORIZED);
            }


            Connection conn = null;
            PreparedStatement ps = null;
            String query;
            try {
                Class.forName(Project.JDBC_DRIVER);
                conn = DriverManager.getConnection(Project.DB_URL, Project.USER, Project.PASSWORD);
                // add new post

                ps.executeUpdate();
                ps.close();
                conn.close();
                return new ResponseEntity("{\"message\": \"comment was posted\"}", responseHeaders, HttpStatus.OK);
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            System.out.println("not JSON");
            return new ResponseEntity(
                    "{\"message\":\"response body was not in a proper JSON format\", \"original message\": \"" + body
                            + "\"}",
                    responseHeaders, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity("{\"message\": \"comment was not posted due to an error\"}", responseHeaders, HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value = "/editComment", method = RequestMethod.POST) // <-- setup the endpoint URL at /editComment with the HTTP POST method
    public ResponseEntity<String> editComment(@RequestBody String body, HttpServletRequest request) {
        System.out.println(body); // debugging
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Content-Type", "application/json");
        try {
            JSONObject temp = new JSONObject(body);
            // Grabbing post info from request body
            int p_number = temp.getInt(UserController.p_number);
            int number = temp.getInt(UserController.number);
            String content = temp.getString(UserController.content);
            String username = temp.getString(UserController.username);
            String token = temp.getString(UserController.token);
            if (!checkToken(username, token)) {
                return new ResponseEntity("{\"message\": \"invalid token\"}", responseHeaders, HttpStatus.UNAUTHORIZED);
            }

            Connection conn = null;
            PreparedStatement ps = null;
            String query;
            try {
                Class.forName(Project.JDBC_DRIVER);
                conn = DriverManager.getConnection(Project.DB_URL, Project.USER, Project.PASSWORD);
                // add new post
                query = "UPDATE Comment SET content = ?, edited = ? WHERE p_number = ? AND number = ? AND username = ?";
                ps = conn.prepareStatement(query);
                ps.setString(1, content);
                ps.setBoolean(2, true);
                ps.setInt(3, p_number);
                ps.setInt(4, number);
                ps.setString(5, username);
                System.out.println(ps); // debugging
                ps.executeUpdate();
                ps.close();
                conn.close();
                return new ResponseEntity("{\"message\": \"comment was posted\"}", responseHeaders, HttpStatus.OK);
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            System.out.println("not JSON");
            return new ResponseEntity(
                    "{\"message\":\"response body was not in a proper JSON format\", \"original message\": \"" + body
                            + "\"}",
                    responseHeaders, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity("{\"message\": \"comment was not edited due to an error\"}", responseHeaders, HttpStatus.BAD_REQUEST);
    }

    // /comment?p_number=1&number=1&deleted=true
    @RequestMapping(value = "/comment", method = RequestMethod.DELETE) // <-- setup the endpoint URL at /delete with the HTTP DELETE method
    public ResponseEntity<String> deleteComment(@RequestBody String body, HttpServletRequest request) {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Content-Type", "application/json");
        System.out.println(body);
        try {
            System.out.println("hello");
            JSONObject temp = new JSONObject(body);
            System.out.println("hello again");
            int p_number = temp.getInt(UserController.p_number);
            int number = temp.getInt(UserController.number);
            Boolean deleted = temp.getBoolean(UserController.deleted); // let people undelete their comments
            String username = temp.getString(UserController.username);
            String token = temp.getString(UserController.token);
            System.out.println(p_number);

            System.out.println(number);
            System.out.println(deleted);
            System.out.println(username);
            System.out.println(token);

            if (!checkToken(username, token)) {
                return new ResponseEntity("{\"message\": \"invalid token\"}", responseHeaders, HttpStatus.UNAUTHORIZED);
            }

            Connection conn = null;
            PreparedStatement ps = null;
            String query;
            try {
                Class.forName(Project.JDBC_DRIVER);
                conn = DriverManager.getConnection(Project.DB_URL, Project.USER, Project.PASSWORD);
                // add new post
                System.out.println("sad life");
                query = "UPDATE Comment SET deleted = ? WHERE p_number = ? AND number = ? AND username = ?";
                System.out.println(query);
                ps = conn.prepareStatement(query);
                ps.setBoolean(1, deleted);
                ps.setInt(2, p_number);
                ps.setInt(3, number);
                ps.setString(4, username);
                System.out.println(ps); // debugging
                ps.executeUpdate();
                ps.close();
                conn.close();
                return new ResponseEntity("{\"message\": \"comment was posted\"}", responseHeaders, HttpStatus.OK);
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            System.out.println("not JSON");
        }
        return new ResponseEntity("{\"message\": \"comment was not edited due to an error\"}", responseHeaders, HttpStatus.BAD_REQUEST);
    }

	@RequestMapping(value = "/comment", method = RequestMethod.GET) // <-- setup the endpoint URL at /comment with the HTTP GET method
	public ResponseEntity<String> getComments(HttpServletRequest request) {
		int p_number = Integer.parseInt(request.getParameter(UserController.p_number));
		JSONArray comments = new JSONArray();
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("Content-Type", "application/json");
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet resultSet = null;
		String query;
		JSONObject content;
		try {
			Class.forName(Project.JDBC_DRIVER);
			conn = DriverManager.getConnection(Project.DB_URL, Project.USER, Project.PASSWORD);

			// get post comments are for
            query = "SELECT * FROM Post WHERE p_number = ?;";
            ps = conn.prepareStatement(query);
            ps.setInt(1, p_number);
            System.out.println(ps);
            resultSet = ps.executeQuery();
            if (resultSet.next()) {
                content = new JSONObject();
                content.put(UserController.date, resultSet.getString(UserController.date));
                content.put(UserController.content, resultSet.getString(UserController.content));
                content.put(UserController.deleted, resultSet.getBoolean(UserController.deleted));
                content.put(UserController.edited, resultSet.getBoolean(UserController.edited));
                content.put(UserController.edit_date, resultSet.getString(UserController.edit_date)); // is null if not there
                content.put(UserController.num_of_comments, resultSet.getInt(UserController.num_of_comments));
                comments.put(content);
            }

			// get reactions of comments and store into a hashmap
			query = "SELECT * FROM CReaction WHERE p_number = ?;";
			ps = conn.prepareStatement(query);
			ps.setInt(1, p_number);
			resultSet = ps.executeQuery();
			HashMap<Integer, HashMap<Integer, Integer>> reactions = new HashMap<Integer, HashMap<Integer, Integer>>();
			int number, reaction, amount;

			while (resultSet.next()) {
				number = resultSet.getInt(UserController.c_number);
				reaction = resultSet.getInt(UserController.reaction);
				amount = resultSet.getInt(UserController.amount);
				if (reactions.containsKey(number)) {
					reactions.get(number).put(reaction, amount);
				} else {
					HashMap<Integer, Integer> temp = new HashMap<>();
					temp.put(reaction, amount);
					reactions.put(number, temp);
				}
			}

			// get comment content
			query = "SELECT * FROM Comment WHERE p_number = ?;";
			ps = conn.prepareStatement(query);
			ps.setInt(1, p_number);
			System.out.println(ps);
			resultSet = ps.executeQuery();
			JSONArray tempy = new JSONArray();
			String replies = "replies";
			while (resultSet.next()) {
				content = new JSONObject();
				number = resultSet.getInt(UserController.number);
				content.put(UserController.number, number);
				content.put(UserController.c_number, resultSet.getString(UserController.c_number));
				content.put(UserController.date, resultSet.getString(UserController.date));
				content.put(UserController.username, resultSet.getString(UserController.username));
				content.put(UserController.content, resultSet.getString(UserController.content));
                content.put(UserController.deleted, resultSet.getBoolean(UserController.deleted));
                content.put(replies, new JSONArray());
				if (resultSet.getBoolean(UserController.edited)) {
					content.put(UserController.edited, true);
					content.put(UserController.edit_date, resultSet.getString(UserController.edit_date));
				} else {
					content.put(UserController.edited, false);
				}
				if (reactions.containsKey(number)) {
					content.put(UserController.reactions, reactions.get(number));
				} else {
					content.put(UserController.reactions, new HashMap<>());
				}
				tempy.put(content);
			}
			int i = 0;
			int j;
			// put comment replies as JSONObject in JSONArray of replies field of comments
			while (i < tempy.length()) {
					j = 0;
					while (j < tempy.length()) {

						if (i != j && tempy.getJSONObject(i).getInt(UserController.number) == (tempy.getJSONObject(j).getInt(UserController.c_number))) {
							tempy.getJSONObject(i).getJSONArray(replies).put(tempy.getJSONObject(j));
						}
						j++;
					}
				if (tempy.getJSONObject(i).getInt(UserController.number) == tempy.getJSONObject(i).getInt(UserController.c_number)) {
				    comments.put(tempy.getJSONObject(i));
                }
				i++;
			}
			ps.close();
			conn.close();
			return new ResponseEntity(comments.toString(), responseHeaders, HttpStatus.OK);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return new ResponseEntity("{\"message\":\"could not get messages\"}", responseHeaders, HttpStatus.BAD_REQUEST);

	}



	@RequestMapping(value = "/message", method = RequestMethod.POST) // <-- setup the endpoint URL at /message with the HTTP POST method
	public ResponseEntity<String> message(@RequestBody String body, HttpServletRequest request) {

		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("Content-Type", "application/json");
		try {
			JSONObject temp = new JSONObject(body);
			String name = temp.getString(UserController.name);
			String content = temp.getString(UserController.content);
			String username = temp.getString(UserController.username);
            String token = temp.getString(UserController.token);
            if (!checkToken(username, token)) {
                return new ResponseEntity("{\"message\": \"invalid token\"}", responseHeaders, HttpStatus.UNAUTHORIZED);
            }
			Connection conn = null;
			PreparedStatement ps = null;
			try {
				Class.forName(Project.JDBC_DRIVER);
				conn = DriverManager.getConnection(Project.DB_URL, Project.USER, Project.PASSWORD);
				ps = conn.prepareStatement(startTransaction);
				ps.execute();
				String insert = "INSERT INTO Message (name, content, username) VALUES (?, ?, ?);";
				ps = conn.prepareStatement(insert);
				ps.setString(1, name);
				ps.setString(2, content);
				ps.setString(3, username);
				System.out.println(ps);
				ps.executeUpdate();
				close(ps, conn);
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		} catch (JSONException je) {
			return new ResponseEntity(
					"{\"message\":\"response body was not in a proper JSON format\", \"original message\": " + body
							+ "}",
					responseHeaders, HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity("{\"message\":\"message sent\", \"content\":" + body + "}", responseHeaders, HttpStatus.OK);
	}

	@RequestMapping(value = "/message", method = RequestMethod.GET) // <-- setup the endpoint URL at /message with the HTTP GET method
	public ResponseEntity<String> getMessage(HttpServletRequest request) {
		String name = request.getParameter(UserController.name);
		JSONArray messages = new JSONArray();


		/*Creating http headers object to place into response entity the server will return.
		This is what allows us to set the content-type to application/json or any other content-type
		we would want to return */
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("Content-Type", "application/json");

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet resultSet = null;
		String query;
		try {
			Class.forName(Project.JDBC_DRIVER);
			conn = DriverManager.getConnection(Project.DB_URL, Project.USER, Project.PASSWORD);
			ps = conn.prepareStatement(startTransaction);
			ps.execute();

			// get reactions of messages and store into a hashmap
			query = "SELECT m.name, m.date, reaction, amount " +
					"FROM (SELECT name, date FROM Message WHERE name = ?) as m " +
					"NATURAL JOIN MReaction;";
			ps = conn.prepareStatement(query);
			ps.setString(1, name);
			System.out.println(ps);
			resultSet = ps.executeQuery();
			HashMap<String, HashMap<Integer, Integer>> reactions = new HashMap<String, HashMap<Integer, Integer>>();
			String date;
			int reaction, amount;
			while (resultSet.next()) {
				date = resultSet.getString(UserController.date);
				reaction = resultSet.getInt(UserController.reaction);
				amount = resultSet.getInt(UserController.amount);
				System.out.println(date);

				if (reactions.containsKey(date)) {
					reactions.get(date).put(reaction, amount);
				} else {
					HashMap<Integer, Integer> temp = new HashMap<>();
					temp.put(reaction, amount);
					reactions.put(date, temp);
				}
			}

			// get message content
			query = "SELECT * FROM Message WHERE name = ?";
			ps = conn.prepareStatement(query);
			ps.setString(1, name);
			System.out.println(ps);
			resultSet = ps.executeQuery();
			JSONObject messageContent;
			while (resultSet.next()) {
				messageContent = new JSONObject();
				date = resultSet.getString(UserController.date);
				messageContent.put(UserController.date, date);
				messageContent.put(UserController.username, resultSet.getString(UserController.username));
				messageContent.put(UserController.content, resultSet.getString(UserController.content));
				if (resultSet.getBoolean(UserController.edited)) {
					messageContent.put(UserController.edited, true);
					messageContent.put(UserController.edit_date, resultSet.getString(UserController.edit_date));
				} else {
					messageContent.put(UserController.edited, false);
				}
				if (reactions.containsKey(date)) {
					messageContent.put(UserController.reactions, reactions.get(date));
				} else {
					messageContent.put(UserController.reactions, new HashMap<>());
				}
				messages.put(messageContent);
			}
			close(ps, conn);
			return new ResponseEntity(messages.toString(), responseHeaders, HttpStatus.OK);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return new ResponseEntity("{\"message\":\"could not get messages\"}", responseHeaders, HttpStatus.BAD_REQUEST);

	}
    
    //Helper method to convert bytes into hexadecimal
	public static String bytesToHex(byte[] in) {
		StringBuilder builder = new StringBuilder();
		for(byte b: in) {
			builder.append(String.format("%02x", b));
		}
		return builder.toString();
	}
}
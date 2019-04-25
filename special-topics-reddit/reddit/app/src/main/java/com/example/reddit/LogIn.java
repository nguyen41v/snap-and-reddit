package com.example.reddit;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LogIn extends AppCompatActivity {

    private String username;
    private String password;
    private Button logIn;
    private EditText user;
    private EditText pass;
    private ProgressBar progressBar;
    private final String valid = "Successfully logged in";
    private final String invalid = "Invalid username/password combo";
    private final String noConnection = "Could not connect to the server at this moment";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        System.out.println(MainActivity.username + " " + MainActivity.token);
        progressBar = findViewById(R.id.progressBar);
        user = findViewById(R.id.username);
        pass = findViewById(R.id.password);


        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        logIn = findViewById(R.id.logInButton);
        logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logIn.setEnabled(false);
                progressBar.setVisibility(View.VISIBLE);
                username = user.getText().toString();
                password = pass.getText().toString();
                System.out.println(username);
                System.out.println(password);
                ValidateLogin validateLogin = new ValidateLogin();
                validateLogin.execute();
            }
        });
    }
    class ValidateLogin extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            logIn.setEnabled(true);
            progressBar.setVisibility(View.GONE);
            if (s.contentEquals("0")) {
                Toast toast = Toast.makeText(getApplicationContext(), invalid, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER,0,64);
                toast.show();
            } else if (s.contentEquals("-1")) {
                Toast toast = Toast.makeText(getApplicationContext(), noConnection, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER,0,64);
                toast.show();
            }
            else {
                Toast toast = Toast.makeText(getApplicationContext(), valid, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL,0,64);
                toast.show();
                try {
                    JSONObject json = new JSONObject(s);
                    for (int i = 0; i < json.length(); i++) {
                        String tokenInfo = json.getString("token");
                        MainActivity.username = username;
                        MainActivity.token = tokenInfo;
                        System.out.println(tokenInfo);
                        MainActivity.editor.putString("username", username);
                        MainActivity.editor.putString("token", tokenInfo);
                        MainActivity.editor.apply();
                        MainActivity.loggedIn = true;
                        MainActivity.recreate = true;
                        System.out.println(MainActivity.loggedIn);
                        setResult(RESULT_OK,getIntent());
                        finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                System.out.println("hello");
                System.out.println(MainActivity.URL);
                URL url = new URL(MainActivity.URL + "/login?username=" + username + "&password=" + password);
                System.out.println("uuhhh");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                System.out.println("am i connecting?");

                con.setRequestMethod("GET");
                System.out.println("uuhhh");

                con.connect();
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);
                int responseCode = con.getResponseCode();
                System.out.println(responseCode);
                if (responseCode == 200) {
                    StringBuilder sb = new StringBuilder();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String json;
                    System.out.println("got data");
                    while ((json = bufferedReader.readLine()) != null) {
                        sb.append(json + "\n");
                    }
                    return sb.toString().trim();
                } else if (responseCode == 400) {
                    return "0";
                }
            } catch (Exception e) {
                System.out.println("Connection probably failed :3\ngo start the server");
                return "-1";
            }
        return "-1";
        }
    }
}

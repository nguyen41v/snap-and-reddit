package com.example.SNAPapp;

import android.app.Activity;
import android.app.AppComponentFactory;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import org.json.JSONArray;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.AppCompatActivity;

public class Launcher extends AppCompatActivity {

    public static final String URL = "http://ec2-3-18-191-129.us-east-2.compute.amazonaws.com";
    public static String username = "";
    public static String token = "";
    public static String benefitsType;
    public static String state;
    public static String benefitsDay;
    public static String applicationURL;
    public static Boolean loggedIn = false;
    public static final int UNAUTHORIZED = 401;
    public static final int BAD_REQUEST = 400;
    public static final int OK = 200;
    public static final String PREFS_NAME = "login.txt";
    public static SharedPreferences pref;
    public static SharedPreferences.Editor editor;
    public static final HashMap<String, String> benefitsKey = new HashMap<String, String>(){
        {
            put("c", "last digit of case number");
            put("mc", "last two digits of case number");
            put("e", "8th and 9th digits of case number");
            put("v", "7th digit of case number");
            put("d", "birthday");
            put("j", "birth month and last name");
            put("y", "birth year");
            put("l", "last name");
            put("ml", "last name");
            put("s", "last digit of ssn");
            put("ms", "last two digits of ssn");
        }};
    public static HashMap<String, ArrayList<String>> userBenefits = new HashMap<>();
    public static final String noConnection = "Could not connect to the server at this moment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        System.out.println("new activity, launcher");

        // get shared preferences: username, token
        pref = getApplicationContext().getSharedPreferences(PREFS_NAME, 0); // 0 - for private mode
        username = pref.getString("username", "");
        token = pref.getString("token", "");
        state = pref.getString("state", "");
        benefitsType = pref.getString("benefitsType", "");
        benefitsDay = pref.getString("benefitsDay", "");
        applicationURL = pref.getString("applicationURL", "");
        System.out.println(username + " " + token);
        System.out.println(state + " " + benefitsType);

        System.out.println(applicationURL + " " + benefitsDay);

        System.out.println("got shared preferences");
        editor = pref.edit();
        System.out.println("hello, launcher");
        // I wanted the launcher to show for a bit :3 # google and stackoverflow
        Thread timer= new Thread()
        {
            public void run()
            {
                try
                {
                    sleep(1000);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        };
        timer.start();
        if (!username.isEmpty() && !token.isEmpty()) {
            ValidateToken validateToken = new ValidateToken();
            validateToken.execute();
        } else {
            System.out.println("new activity, state info");
            startActivity(new Intent(getApplicationContext(), StateInfo.class).setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
            finish();
        }
    }

    public static void write() {
        editor.clear();
        editor.putString("username", username);
        editor.putString("token", token);
        editor.putString("state", state);
        editor.putString("benefitsType", benefitsType);
        editor.putString("benefitsDay", benefitsDay);
        editor.putString("applicationURL", applicationURL);
    }
    private void update(Boolean b) {
        if (b == null) {
            startActivity(new Intent(this, StateInfo.class).setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
        } else if (b) {
            Launcher.loggedIn = true;
            startActivity(new Intent(this, Overview.class).setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
        } else {
            startActivity(new Intent(this, LoginSignup.class).setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
        }
        finish();
    }


    class ValidateToken extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean s) {
            super.onPostExecute(s);
            if (s == null) {
                Toast toast = Toast.makeText(getApplicationContext(), noConnection, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER,0,64);
                toast.show();
            } else if (!s){
                Toast toast = Toast.makeText(getApplicationContext(), "Please log in again", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER,0,64);
                toast.show();
            }
            update(s);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                URL url = new URL(URL + "/validate?username=" + username + "&token=" + URLEncoder.encode(token, "UTF-8"));
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);
                con.connect();
                int responseCode = con.getResponseCode();
                if (responseCode == 200) {
                    return true;
                }
            } catch (Exception e) {
                Log.e("Exception", "Sad life");
                e.printStackTrace();
                return null;
            }
            return false;
        }
    }
}

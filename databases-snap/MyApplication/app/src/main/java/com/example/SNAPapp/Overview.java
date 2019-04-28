package com.example.SNAPapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Overview extends Navigation {


    private final String noConnection = "Could not connect to the server at this moment";

    private TextView balance;
    private TextView average;
    private TextView past_benefits;
    private TextView past_spent;
    public Boolean recreate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);
        activity = "Overview";
        makeMenu();

        balance = findViewById(R.id.state_text);
        average = findViewById(R.id.average);
        past_benefits = findViewById(R.id.pastBenefits);
        past_spent = findViewById(R.id.pastSpent);
        update();
    }

    private void update() {
        GetData getData = new GetData();
        getData.execute();

    }

    @Override
    public void onResume() {
        super.onResume();
        if (recreate) {
            recreate();
            recreate = false;
        }
    }

    private void loadInfo(String s) {
//        balance = findViewById(R.id.balance);
//        average = findViewById(R.id.average);
//        past_benefits = findViewById(R.id.past_benefits);
//        past_spent = findViewById(R.id.past_spent);
//        android:text="You can spend $0.00 per meal until you receive your next benefits on the 5th"
        String temp;
        try {
            JSONObject jsonObject = new JSONObject(s);
            temp = "$" + jsonObject.getString("balance");
            System.out.println(temp);
            balance.setText(temp);
            temp = "You can spend $" +  jsonObject.getString("average") +" per meal until you receive your next benefits";
            System.out.println(temp);
            average.setText(temp);
            temp = "$" + jsonObject.getString("past_benefits");
            System.out.println(temp);
            past_benefits.setText(temp);
            temp = "$" + jsonObject.getString("past_spent");
            System.out.println(temp);
            past_spent.setText(temp);


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    class GetData extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (!s.isEmpty()) {
                loadInfo(s);
            }
            // server is down fixme
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                System.out.println("start of try");
                System.out.println(Launcher.URL);
                URL url = new URL(Launcher.URL + "/balance?username=" + Launcher.username);
                System.out.println("made url");
                System.out.println(url);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                System.out.println("made connection");
                con.setRequestMethod("GET");
                System.out.println("set GET");
                con.connect();
                System.out.println("connected");
                System.out.println("clickAction");

                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);

                StringBuilder sb = new StringBuilder();
                int responseCode = con.getResponseCode();
                System.out.println(responseCode);
                if (responseCode == 200) {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String json;
                    System.out.println("got data");
                    while ((json = bufferedReader.readLine()) != null) {
                        sb.append(json + "\n");
                    }
                    return sb.toString().trim();
                }
                return "";

            } catch (Exception e) {
                System.out.println("Connection probably failed :3\ngo start the server");
                e.printStackTrace();
                return "";
            }
        }
    }
}

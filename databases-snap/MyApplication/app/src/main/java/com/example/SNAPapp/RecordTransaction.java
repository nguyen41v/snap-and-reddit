package com.example.SNAPapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class RecordTransaction extends AppCompatActivity {
    Boolean spend;
    TextView descriptionInfo;
    String description;
    TextView amountInfo;
    String amount;
    TextView dateInfo;
    String date;
    RadioGroup radioGroup;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_transaction);
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        descriptionInfo = findViewById(R.id.description_entry);
        amountInfo = findViewById(R.id.amount_entry);
        dateInfo = findViewById(R.id.date_entry);
        radioGroup = findViewById(R.id.radioGroup);
        findViewById(R.id.sendTransaction).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton radioButton = findViewById(radioGroup.getCheckedRadioButtonId());
                switch (radioButton.getText().toString()) {
                    case "Spending": {
                        spend = true;
                        break;
                    }
                    case "Benefits": {
                        spend = false;
                        break;
                    }
                }
                if (checkInputs()) {
                    send();
                }
            }
        });


    }

    public Boolean checkInputs() {
        description = descriptionInfo.getText().toString();
        if (description.length() > 45) {
            Toast toast = Toast.makeText(this, "Your description is too long. It has to be 45 characters or less.", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER,0,64);
            toast.show();
            return false;
        }
        // people aren't going to have thousands of dollars in benefits.... the , is gonna be used as a . if anything
        amount = amountInfo.getText().toString().replace("$", "").replace(",", ".");
        try {
            new BigDecimal(amount);
        } catch (NumberFormatException nfe) {
            Toast toast = Toast.makeText(this, "Your amount needs to be in a numerical format like 3.45 or 21.", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER,0,64);
            toast.show();
            System.out.println("NFE for amount set as: " + amount);
            return false;
        }
        date = dateInfo.getText().toString();
        if (!date.isEmpty()) {
            try {
                Date.valueOf(date);
            } catch (IllegalArgumentException iae) {
                Toast toast = Toast.makeText(this, "Your date is formatted incorrectly. Some examples of correct dates are 2019-04-07, 2019-04-7, 2019-4-07, or 2019-4-7", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER,0,64);
                toast.show();
                return false;
            }
        }
        return true;
    }

    public void send() {
        MakeTransaction makeTransaction = new MakeTransaction();
        makeTransaction.execute();
    }

    public void respond(Boolean b) {
        if (b == null){
            Toast toast = Toast.makeText(getApplicationContext(), "Could not connect to the server at the moment. Please try again in a bit.", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER,0,64);
            toast.show();
        } else if (!b) {
            Toast toast = Toast.makeText(getApplicationContext(), "Please log in again", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER,0,64);
            toast.show();
            startActivity(new Intent(getApplication(), LoginSignup.class));
        } else {
            RecentTransactions.recreate = true;
            finish();
        }
    }
    class MakeTransaction extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean b) {
            super.onPostExecute(b);
            respond(b);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                System.out.println("start of try");
                URL url = new URL(Launcher.URL + "/transactions");
                System.out.println("made url");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                System.out.println("made connection");
                con.setRequestMethod("POST");
                System.out.println("set POST");
                JSONObject requestBody = new JSONObject();
                requestBody.put("username", Launcher.username);
                requestBody.put("token", Launcher.token);
                requestBody.put("amount", amount);
                requestBody.put("description", description);
                requestBody.put("spend", spend);
                requestBody.put("date", date);
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);
                con.setDoOutput(true);
                con.setRequestProperty("Content-Type","application/json");
                OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
                writer.write(requestBody.toString());
                writer.flush();
                con.connect();
                System.out.println("connected");

                StringBuilder sb = new StringBuilder();
                int responseCode = con.getResponseCode();
                System.out.println(responseCode);
                if (responseCode == Launcher.OK) {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String json;
                    System.out.println("got data");
                    while ((json = bufferedReader.readLine()) != null) {
                        sb.append(json + "\n");
                        System.out.print(json);
                    }
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                Log.e("Exception", "Sad life");
                e.printStackTrace();
            }
            return null;
        }
    }

}

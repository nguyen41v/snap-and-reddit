package com.example.reddit;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class MakeSub extends AppCompatActivity {

    private String sub_name;
    private String info;
    private EditText sub;
    private EditText sub_info;
    private ProgressBar progressBar;
    private TextView create;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!MainActivity.username.isEmpty() && !MainActivity.token.isEmpty()) {
            // fixme
        } else {
            startActivity(new Intent(this, LogIn.class));
        }
        setContentView(R.layout.activity_make_sub);
        progressBar = findViewById(R.id.progressBar);
        sub = findViewById(R.id.title);
        sub_info = findViewById(R.id.sub_info);
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        create = (TextView) findViewById(R.id.save);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                create.setEnabled(false);
                progressBar.setVisibility(View.VISIBLE);
                sub_name = sub.getText().toString();
                info = sub_info.getText().toString();
                SendSubInfo sendSubInfo = new SendSubInfo();
                sendSubInfo.execute();
            }
        });
    }
    class SendSubInfo extends AsyncTask<Void, Void, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Integer s) {
            super.onPostExecute(s);
            create.setEnabled(true);
            progressBar.setVisibility(View.GONE);
            switch (s) {
                case HttpURLConnection.HTTP_OK:
                    Toast.makeText(getApplicationContext(), "Sub created!", Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                case HttpURLConnection.HTTP_UNAUTHORIZED:
                    Toast.makeText(getApplicationContext(), "Please log in again", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), LogIn.class));
                    break;
                case HttpURLConnection.HTTP_FORBIDDEN:
                    Toast.makeText(getApplicationContext(), "That sub already exists", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(getApplicationContext(), "Could not create to the server\nPlease try again", Toast.LENGTH_SHORT).show();
                    break;
            }
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            try {
                URL url = new URL(MainActivity.URL + "/sub");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                JSONObject requestBody = new JSONObject();
                requestBody.put("username", MainActivity.username);
                requestBody.put("token", MainActivity.token);
                requestBody.put("sub_name", sub_name);
                requestBody.put("info", info);
                con.setDoOutput(true);
                con.setRequestProperty("Content-Type","application/json");
                OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
                writer.write(requestBody.toString());
                writer.flush();
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);
                con.connect();
                return con.getResponseCode();
            } catch (Exception e) {
                Log.e("Exception", "Sad life");
                e.printStackTrace();
            }
            return -1;
        }
    }
}
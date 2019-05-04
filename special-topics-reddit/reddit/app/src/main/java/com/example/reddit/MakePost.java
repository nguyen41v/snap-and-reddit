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

public class MakePost extends AppCompatActivity {

    private String sub_name;
    private String info;
    private EditText sub;
    private EditText sub_info;
    private ProgressBar progressBar;
    private TextView create;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        progressBar = findViewById(R.id.progressBar);
        sub = findViewById(R.id.subname);
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
    class SendSubInfo extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean s) {
            super.onPostExecute(s);
            create.setEnabled(true);
            progressBar.setVisibility(View.GONE);
            if (s) {
                finish();
            } else if (!s) {
                Toast.makeText(getApplicationContext(), "Please log in again", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(getApplicationContext(), "Could not create to the server\nPlease try again", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
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
                int responseCode = con.getResponseCode();
                if (responseCode == MainActivity.OK) {
                    return true;
                } else if (responseCode == MainActivity.UNAUTHORIZED) {
                    startActivity(new Intent(getApplicationContext(), LogIn.class));
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
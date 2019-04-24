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

public class Edit extends AppCompatActivity {

    private int p_number;
    private String message;
    private int number = 0;
    private EditText edit;
    private ProgressBar progressBar;
    private TextView save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        Intent intent = getIntent();
        String type = intent.getStringExtra("edit");
        p_number = intent.getIntExtra("p_number", -1);
        number = intent.getIntExtra("number", 0);
        String content = intent.getStringExtra("content");
        progressBar = findViewById(R.id.progressBar);
        edit = findViewById(R.id.editText);
        final TextView response = (TextView) findViewById(R.id.response);
        edit.setText(content);
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (type.contains("comment")) {
            response.setText("Edit comment");
        } else {
            response.setText("Edit comment");
        }
        save = (TextView) findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save.setEnabled(false);
                progressBar.setVisibility(View.VISIBLE);
                message = edit.getText().toString();
                Post myComment = new Post();
                myComment.execute();
            }
        });
    }
    class Post extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean s) {
            super.onPostExecute(s);
            save.setEnabled(true);
            progressBar.setVisibility(View.GONE);
            if (s) {
                ViewPost.postMade = true;
                finish();
            } else if (!s) {
                Toast.makeText(getApplicationContext(), "Please log in again", Toast.LENGTH_LONG).show();
            }
            else {
            Toast.makeText(getApplicationContext(), "Could not save to the server\nPlease try again", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                URL url = new URL(MainActivity.URL + "/editComment");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                JSONObject requestBody = new JSONObject();
                requestBody.put("username", MainActivity.username);
                requestBody.put("token", MainActivity.token);
                requestBody.put("p_number", p_number);
                requestBody.put("content", message);
                requestBody.put("number", number);
                con.setDoOutput(true);
                con.setRequestProperty("Content-Type","application/json");
                OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
                writer.write(requestBody.toString());
                writer.flush();
                con.connect();
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);
                int responseCode = con.getResponseCode();
                if (responseCode == 200) {
                    return true;
                } else if (responseCode == 400) {
                    startActivity(new Intent(Edit.this, LogIn.class));
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
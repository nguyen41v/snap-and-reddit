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

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class Reply extends AppCompatActivity {

    private int p_number;
    private String message;
    private int c_number = 0;
    private TextView post;
    private ProgressBar progressBar;
    // fixme need to get token and post info

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply);
        Intent intent = getIntent();
        p_number = intent.getIntExtra("p_number", -1);
        String type = intent.getStringExtra("reply_to");
        String content = intent.getStringExtra("content");
        progressBar = findViewById(R.id.progressBar);
        TextView response = (TextView) findViewById(R.id.response);
        TextView user = (TextView) findViewById(R.id.user);
        TextView text = (TextView) findViewById(R.id.content);
        final TextView responseType = (TextView) findViewById(R.id.editText);
        text.setText(content);
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        if (type.contains("post")) {
            response.setText("Add comment");
            String temp = intent.getStringExtra("extra_content"); //fixme
            user.setVisibility(View.GONE);
            responseType.setHint("Your comment");
        } else {
            c_number = intent.getIntExtra("number", 0);
            System.out.println(c_number);
            String username = intent.getStringExtra("username");
            String time = intent.getStringExtra("time");
            response.setText("Reply to comment");
            String temp = username + " \t\t " + time;
            user.setText(temp);
            responseType.setHint("Your reply");
        }
        post = (TextView) findViewById(R.id.post);
        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                post.setEnabled(false);
                progressBar.setVisibility(View.VISIBLE);
                message = responseType.getText().toString();
                Post myComment = new Post();
                myComment.execute();
            }
        });
    }
    // fixme
    class Post extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean s) {
            super.onPostExecute(s);
            post.setEnabled(true);
            progressBar.setVisibility(View.GONE);
            if (s) {
                ViewPost.postMade = true;
                finish();
            } else {
            Toast.makeText(getApplicationContext(), "Could not post comment to the server\nPlease try again", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                System.out.println(c_number);
                URL url = new URL(MainActivity.URL + "/comment");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                JSONObject requestBody = new JSONObject();
                requestBody.put("username", MainActivity.username);
                requestBody.put("token", MainActivity.token);
                requestBody.put("p_number", p_number);
                requestBody.put("content", message);
                if (c_number != 0) {
                    requestBody.put("c_number", c_number);
                }
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
                }
            } catch (Exception e) {
                Log.e("Exception", "Sad life");
                e.printStackTrace();
            }
            return false;
        }
    }
}

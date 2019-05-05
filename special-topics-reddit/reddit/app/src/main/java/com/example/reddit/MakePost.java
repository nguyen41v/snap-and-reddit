package com.example.reddit;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class MakePost extends AppCompatActivity {

    public static String sub_name;
    private String content;
    private String title;
    public static Button sub;
    private EditText title_info;
    private EditText content_info;
    private ProgressBar progressBar;
    private TextView post;
    private int responseCode = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_make_post);
        Intent intent = getIntent();
        sub_name = intent.getStringExtra("sub_name");
        progressBar = findViewById(R.id.progressBar);
        sub = findViewById(R.id.sub_name);
        content_info = findViewById(R.id.content);
        title_info = findViewById(R.id.title);
        System.out.println(sub_name);
        if (sub_name != null) {
            sub.setText(sub_name);
        }
        sub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ChooseSub.class));
            }
        });
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        post = (TextView) findViewById(R.id.post);
        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                post.setEnabled(false);
                System.out.println("clicked on POST");
                if (sub_name == null) {
                    Toast.makeText(getApplicationContext(), "Pick a sub to post to", Toast.LENGTH_SHORT).show();
                    post.setEnabled(true);
                    startActivity(new Intent(getApplicationContext(), ChooseSub.class));
                    return;
                }
                Toast toast;
                title = title_info.getText().toString();
                if (title.isEmpty()) {
                    toast = Toast.makeText(getApplication(), "Your post must have a title in order to be created", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 64);
                    toast.show();
                    return;
                }
                if (title.length() > 255) {
                    toast = Toast.makeText(getApplication(), "Post titles must be less than 255 characters", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 64);
                    toast.show();
                    return;
                }
                content = content_info.getText().toString();
                content_info.onEditorAction(EditorInfo.IME_ACTION_DONE);
                title_info.onEditorAction(EditorInfo.IME_ACTION_DONE);
                progressBar.setVisibility(View.VISIBLE);
                SendPostInfo sendPostInfo = new SendPostInfo();
                sendPostInfo.execute();
            }
        });
    }



    class SendPostInfo extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            post.setEnabled(true);
            progressBar.setVisibility(View.GONE);
            try {
                JSONObject jsonObject = new JSONObject(s);
                Toast toast;
                switch (responseCode) {
                    case HttpURLConnection.HTTP_OK:
                        toast = Toast.makeText(getApplication(), jsonObject.getString("message"), Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 64);
                        toast.show();
                        MainActivity.recreate = true;
                        finish();
                        break;
                    case -1:
                        toast = Toast.makeText(getApplication(), "Could not post to the server\nPlease try again", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 64);
                        toast.show();
                        break;
                    case HttpURLConnection.HTTP_UNAUTHORIZED:
                        MainActivity.loggedIn = false;
                        toast = Toast.makeText(getApplication(), jsonObject.getString("message"), Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 64);
                        toast.show();
                        startActivity(new Intent(getApplicationContext(), LogIn.class));
                        break;
                    default:
                        toast = Toast.makeText(getApplication(), jsonObject.getString("message"), Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 64);
                        toast.show();
                        finish();
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(MainActivity.URL + "/post");
                System.out.println(url);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                JSONObject requestBody = new JSONObject();
                requestBody.put("username", MainActivity.username);
                requestBody.put("token", MainActivity.token);
                requestBody.put("sub_name", sub_name);
                requestBody.put("content", content);
                requestBody.put("title", title);
                con.setDoOutput(true);
                con.setRequestProperty("Content-Type","application/json");
                OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
                writer.write(requestBody.toString());
                writer.flush();
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);
                con.connect();
                responseCode = con.getResponseCode();
                StringBuilder sb = new StringBuilder();
                InputStream in;
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    in = con.getInputStream();
                } else {
                    in = con.getErrorStream();
                }
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                String json;
                System.out.println("got data");
                while ((json = bufferedReader.readLine()) != null) {
                    sb.append(json + "\n");
                }
                return sb.toString().trim();
            } catch (Exception e) {
                Log.e("Exception", "Sad life");
                e.printStackTrace();
            }
            return "{}";
        }
    }
}
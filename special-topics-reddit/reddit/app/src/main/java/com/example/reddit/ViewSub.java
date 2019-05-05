package com.example.reddit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class ViewSub extends Navigation {


    private static RecyclerView recyclerView;
    private static RecyclerView.Adapter adapter;
    private static List<PostItem> postItems;
    private CardView endOfPosts;
    private SwipeRefreshLayout swipeRefreshLayout;
    String sub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_sub);
        Intent intent = getIntent();
        sub = intent.getStringExtra("sub");

        endOfPosts = findViewById(R.id.endCard);
        // temporary adapter for recycler view
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new PostAdapter(new ArrayList<PostItem>(), this));
        postItems = new ArrayList<>();
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        update();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
        );

        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(true);
                }
                update();
            }
        });

    }


    private void loadIntoRecyclerView(String json) {
        System.out.println(json);
        postItems = new ArrayList<>();
        System.out.println("loading into recylcer view");
        System.out.println(json);
        if (json.isEmpty()) {
            System.out.println("hello");
            noConnection.setVisibility(View.VISIBLE);
            endOfPosts.setVisibility(View.INVISIBLE);
            return;
        }
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject post = jsonArray.getJSONObject(i);
                PostItem postItem = new PostItem(post.getString("title"),
                        post.getString("content"),
                        post.getString("date"),
                        post.getInt("num_of_comments"),
                        post.getString("username"),
                        post.getString("sub_name"),
                        post.getInt("p_number"));
                postItems.add(postItem);
            }
            System.out.println("done getting data");
            noConnection.setVisibility(View.INVISIBLE);
            endOfPosts.setVisibility(View.VISIBLE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        adapter = new PostAdapter(postItems, this);
        System.out.println("done making adapter");
        recyclerView.setAdapter(adapter);
        System.out.println("set adapted");
    }


    private void update() {
        GetSubPosts getSubPosts = new GetSubPosts();
        getSubPosts.execute();

    }

    class GetSubPosts extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            loadIntoRecyclerView(s);
            swipeRefreshLayout.setRefreshing(false);
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(MainActivity.URL + "/post?sub_name=" + sub);
                System.out.println(url);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);
                con.connect();
                StringBuilder sb = new StringBuilder();
                int responseCode = con.getResponseCode();
                System.out.println(responseCode);
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String json;
                    while ((json = bufferedReader.readLine()) != null) {
                        sb.append(json + "\n");
                    }
                    return sb.toString().trim();
                }
                return "";

            } catch (Exception e) {
                System.out.println("Connection probably failed :3\ngo start the server");
                return "";
            }
        }
    }

}

package com.example.reddit;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.SearchView;

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

public class SearchResults extends Navigation {

    private static RecyclerView recyclerView;
    private static RecyclerView.Adapter adapter;
    private static List<PostItem> postItems;
    private static List<SubItem> subItems;
    private static List<UserItem> userItems;

    private CardView endOfPosts;
    private SwipeRefreshLayout swipeRefreshLayout;
    String query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);
        Intent intent = getIntent();
        query = intent.getStringExtra("query");

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new SubAdapterToViewSubs(new ArrayList<SubItem>(), this));
        subItems = new ArrayList<>();


        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ((SearchView) findViewById(R.id.searchView)).setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                startActivity(new Intent(getApplication(), SearchResults.class).putExtra("query", query));
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });
    }


    private void loadIntoRecyclerView(String json) {
        System.out.println(json);
        postItems = new ArrayList<>();
        System.out.println("loading into precylcer view");
        System.out.println(json);
        if (json.isEmpty()) {
            System.out.println("hello");
            noConnection.setVisibility(View.VISIBLE);
            endOfPosts.setVisibility(View.INVISIBLE);
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray("posts");
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
            // fixme
            jsonArray = jsonObject.getJSONArray("subs");

            jsonArray = jsonObject.getJSONArray("users");

            System.out.println("pdone getting data");
            noConnection.setVisibility(View.INVISIBLE);
            endOfPosts.setVisibility(View.VISIBLE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        adapter = new PostAdapter(postItems, this);
        System.out.println("pdone making adapter");
        recyclerView.setAdapter(adapter);
        System.out.println("pset adapted");
    }


    private void update() {
        GetSearchResults getSearchResults = new GetSearchResults();
        getSearchResults.execute();

    }

    class GetSearchResults extends AsyncTask<Void, Void, String> {

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
                URL url = new URL(MainActivity.URL + "/search?username=" + MainActivity.username +
                        "&token=" + URLEncoder.encode(MainActivity.token, "UTF-8") +
                        "&query=" + URLEncoder.encode(query, "UTF-8") );
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

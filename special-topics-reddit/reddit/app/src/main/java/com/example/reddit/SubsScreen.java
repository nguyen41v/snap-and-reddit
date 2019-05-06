package com.example.reddit;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class SubsScreen extends Navigation {

    private static RecyclerView recyclerView;
    private static RecyclerView.Adapter adapter;
    private List<SubItem> subItems;
    private CardView endOfPosts;
    private SwipeRefreshLayout swipeRefreshLayout;
    private SearchView searchView;
    private int responseCode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subs_screen);
        makeMenu();
        setBotBarClickListeners();
        activity = "communities";
        if (!MainActivity.loggedIn) {
            ((TextView) findViewById(R.id.subscribed)).setText("All subs");
        }

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new SubAdapterToViewSubs(new ArrayList<SubItem>(), this));
        subItems = new ArrayList<>();

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        swipeRefreshLayout.setRefreshing(true);
                        update();
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
        searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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

    private void update() {
        GetSubs getSubs = new GetSubs();
        getSubs.execute();
    }


    private void loadIntoRecyclerView(String json) {
        System.out.println(json);
        subItems = new ArrayList<>();
        System.out.println("loading into recylcer view");
        System.out.println(json);
        if (json.isEmpty()) {
            System.out.println("hello");
            return;
        }
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                SubItem subItem = new SubItem(jsonArray.getString(i));
                subItems.add(subItem);
            }
            System.out.println("done getting data");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        adapter = new SubAdapterToViewSubs(subItems, this);
        System.out.println("done making adapter");
        recyclerView.setAdapter(adapter);
        System.out.println("set adapted");
    }

    class GetSubs extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast toast;
            swipeRefreshLayout.setRefreshing(false);
            if (s == null) {
                toast = Toast.makeText(getApplication(), "Could not connect to the server\nPlease try again", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 64);
                toast.show();
            } else {
                loadIntoRecyclerView(s);
            }
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(MainActivity.URL + "/subs?username=" + MainActivity.username + "&token=" + URLEncoder.encode(MainActivity.token, "UTF-8"));
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                System.out.println(url);
                con.setRequestMethod("GET");
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);
                con.connect();
                int responseCode = con.getResponseCode();
                System.out.println(responseCode);
                StringBuilder sb = new StringBuilder();
                InputStream in;
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    in = con.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                    String json;
                    System.out.println("got data");
                    while ((json = bufferedReader.readLine()) != null) {
                        sb.append(json);
                    }
                    return sb.toString().trim();
                }
                return "-1";
            } catch (Exception e) {
                Log.e("Exception", "Sad life");
                e.printStackTrace();
            }
            return null;
        }
    }

}

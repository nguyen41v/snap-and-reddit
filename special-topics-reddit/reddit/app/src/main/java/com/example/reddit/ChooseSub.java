package com.example.reddit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.SearchView;
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

public class ChooseSub extends AppCompatActivity {

    private List<SubItem> subItems;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private SearchView searchView;
    private String subQuery;
    private int responseCode = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_sub);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new SubAdapter(new ArrayList<SubItem>(), this));
        subItems = new ArrayList<>();
        SendSubInfo sendSubInfo = new SendSubInfo();
        sendSubInfo.execute();
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                subQuery = query;
                SearchSubs searchSubs = new SearchSubs();
                searchSubs.execute();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                subQuery = newText;
                SearchSubs searchSubs = new SearchSubs();
                searchSubs.execute();
                return true;
            }
        });


    }


    class SendSubInfo extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
//            create.setEnabled(true);
//            create.setEnabled(true);
            Toast toast;
            switch (responseCode) {
                case -1:
                    toast = Toast.makeText(getApplication(), "Could not connect to the server\nPlease try again", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 64);
                    toast.show();
                    break;
                case HttpURLConnection.HTTP_OK:
                    loadIntoRecyclerView(s);
                    break;
                case HttpURLConnection.HTTP_UNAUTHORIZED:
                    try {
                        JSONObject jsonObject = new JSONObject(s);
                        toast = Toast.makeText(getApplication(), jsonObject.getString("message"), Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 64);
                        toast.show();
                        startActivity(new Intent(getApplicationContext(), LogIn.class));
                    } catch (JSONException je) {
                        je.printStackTrace();
                    }
                    break;
                default:
                    try {
                        JSONObject jsonObject = new JSONObject(s);
                        Toast.makeText(getApplicationContext(), jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                    } catch (JSONException je) {
                        je.printStackTrace();
                    }
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
                responseCode = con.getResponseCode();
                System.out.println(responseCode);
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
                    sb.append(json);
                }
                return sb.toString().trim();
            } catch (Exception e) {
                Log.e("Exception", "Sad life");
                e.printStackTrace();
            }
            return "-1";
        }
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
        adapter = new SubAdapter(subItems, this);
        System.out.println("done making adapter");
        recyclerView.setAdapter(adapter);
        System.out.println("set adapted");
    }


    class SearchSubs extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
//            create.setEnabled(true);
            switch (s) {
                case "-1":
                    Toast.makeText(getApplicationContext(), "Could not connect to the server\nPlease try again", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    loadIntoRecyclerView(s);
                    break;
            }
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(MainActivity.URL + "/searchSubs?search=" + URLEncoder.encode(subQuery, "UTF-8"));
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
            } catch (Exception e) {
                Log.e("Exception", "Sad life");
                e.printStackTrace();
            }
            return "{}";
        }
    }
}

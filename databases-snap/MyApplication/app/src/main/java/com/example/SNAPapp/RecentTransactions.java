package com.example.SNAPapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class RecentTransactions extends Navigation {

    ArrayList<TransactionItem> transactions;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    public static DateFormat dateFormat = new SimpleDateFormat("EEE, MMM d");
    public static Boolean recreate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_transactions);
        makeMenu();
        recreate = false;
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new TransactionAdapter(new ArrayList<TransactionItem>(), this));
        transactions = new ArrayList<>();
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), RecordTransaction.class));
            }
        });
        update();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (recreate) {
            update();
            recreate = false;
        }
    }

    public void update() {
        GetData getData = new GetData();
        getData.execute();
    }

    private void loadIntoRecyclerView(String json) {
        try {
            transactions = new ArrayList<>();
            JSONArray jsonArray = new JSONArray(json);
            String prevDate = "";
            TransactionItem prevTransaction = new TransactionItem();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject singleTransaction = jsonArray.getJSONObject(i);
                System.out.println(prevDate);
                System.out.println(singleTransaction.getString("date"));
                String tempDate = singleTransaction.getString("date");
                if (!prevDate.equals(tempDate = tempDate.substring(0, tempDate.length() - 9))) {
                    System.out.println("different date!");
                    TransactionItem transaction = new TransactionItem(singleTransaction.getString("date"),
                            singleTransaction.getBoolean("spend"),
                            new BigDecimal(singleTransaction.getString("amount")),
                            singleTransaction.getString("description"));
                    prevDate = tempDate;
                    prevTransaction = transaction;
                    transactions.add(transaction);
                } else {
                    System.out.println("same date!");
                    prevTransaction.addTransaction(singleTransaction.getBoolean("spend"),
                            new BigDecimal(singleTransaction.getString("amount")),
                            singleTransaction.getString("description"));
                }
                System.out.println("done getting data");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        adapter = new TransactionAdapter(transactions, getApplicationContext());
        System.out.println("done making adapter");
        recyclerView.setAdapter(adapter);
        System.out.println("set adapted");
    }

    class GetData extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s != null) {
                loadIntoRecyclerView(s);
            }
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                System.out.println("start of try");
                URL url = new URL(Launcher.URL + "/transactions?username=" + Launcher.username + "&token=" + URLEncoder.encode(Launcher.token, "UTF-8"));
                System.out.println("made url");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                System.out.println("made connection");
                con.setRequestMethod("GET");
                System.out.println("set GET");
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);
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
                    return sb.toString().trim();
                }
            } catch (Exception e) {
                Log.e("Exception", "Sad life");
                e.printStackTrace();
            }
            return null;
        }
    }

}

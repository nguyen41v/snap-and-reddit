package com.example.myapplication;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Overview extends Navigation {

    public static final String URL = "localhost";
    public static String username = "";
    public static String token = "";
    public static Boolean loggedIn = false;
    public static Boolean recreate = false;

    private MenuItem signUp;
    private MenuItem account;
    private MenuItem logout;
    private MenuItem communicate;

    private TextView balance;
    private TextView average;
    private TextView pastBenefits;
    private TextView pastSpent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Menu menu = navigationView.getMenu();
        for (int menuItemIndex = 0; menuItemIndex < menu.size(); menuItemIndex++) {
            MenuItem menuItem= menu.getItem(menuItemIndex);
            if(menuItem.getItemId() == R.id.sign_up){
                signUp = menuItem;
            } else if (menuItem.getItemId() == R.id.account) {
                account = account;
            } else if (menuItem.getItemId() == R.id.communicate) {
                communicate = menuItem;
            } else if (menuItem.getItemId() == R.id.logout) {
                logout = menuItem;
            }
        }
        balance = findViewById(R.id.balance);
        average = findViewById(R.id.average);
        pastBenefits = findViewById(R.id.pastBenefits);
        pastSpent = findViewById(R.id.pastSpent);
        GetData getData = new GetData();
        getData.execute();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_overview_navigation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle menu_overview_navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.sign_up) {
            startActivity(new Intent(this, LoginSignup.class));
        } else if (id == R.id.overview) {
            // already on
        } else if (id == R.id.recent_transactions) {

        } else if (id == R.id.home) {
            startActivity(new Intent(this, TestScrollingActivity.class));
        } else if (id == R.id.nearby) {
            startActivity(new Intent(this, NearbyStores.class));
        } else if (id == R.id.message) {

        } else if (id == R.id.forum) {

        } else if (id == R.id.faq) {

        } else if (id == R.id.logout) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void loadInfo(String s) {
//        balance = findViewById(R.id.balance);
//        average = findViewById(R.id.average);
//        pastBenefits = findViewById(R.id.pastBenefits);
//        pastSpent = findViewById(R.id.pastSpent);
//        android:text="You can spend $0.00 per meal until you receive your next benefits on the 5th"
        String temp;
        try {
            JSONObject jsonObject = new JSONObject(s);
            temp = "$" + Double.toString(jsonObject.getDouble("balance"));
            balance.setText(temp);
            temp = "You can spend $0.00 per meal until you receive your next benefits on the 5th";
            average.setText(temp);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    class GetData extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                System.out.println("start of try");
                java.net.URL url = new URL(URL + "/overview");
                System.out.println("made url");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                System.out.println("made connection");
                con.setRequestMethod("GET");
                System.out.println("set GET");
                con.connect();
                System.out.println("connected");
                System.out.println("clickAction");

                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);

                StringBuilder sb = new StringBuilder();
                int responseCode = con.getResponseCode();
                System.out.println(responseCode);
                if (responseCode == 200) {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String json;
                    System.out.println("got data");
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

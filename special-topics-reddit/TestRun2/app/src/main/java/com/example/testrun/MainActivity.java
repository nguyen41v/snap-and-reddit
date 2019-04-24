package com.example.testrun;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

//    ListView listView;
    public static final String URL = "http://ec2-18-222-132-93.us-east-2.compute.amazonaws.com";
    private static RecyclerView recyclerView;
    private static RecyclerView.Adapter adapter;
    private static List<PostItem> postItems;
    private static NavigationView navigationView;
    public static String filename = "login.txt";
    public static String username = "";
    public static String token = "";
    public static File file;
    public static Boolean loggedIn = false;
    public static Boolean recreate = false;
    private Button name;
    private View signUpMessage;
    private MenuItem signUp;
    private MenuItem profile;
    private MenuItem logout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View noConnection;
    private CardView endOfPosts;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        noConnection = findViewById(R.id.NoConnection);
        endOfPosts = findViewById(R.id.endCard);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        toolbar.setNavigationIcon(R.drawable.ic_person_purple_24dp);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        Menu menu = navigationView.getMenu();
        for (int menuItemIndex = 0; menuItemIndex < menu.size(); menuItemIndex++) {
            MenuItem menuItem= menu.getItem(menuItemIndex);
            if(menuItem.getItemId() == R.id.Sign_up){
                signUp = menuItem;
            } else if (menuItem.getItemId() == R.id.profile) {
                profile = menuItem;
            } else if (menuItem.getItemId() == R.id.logout) {
                logout = menuItem;
            }
        }


        System.out.println("\n\nclickAction\nahh");
        file = new File(this.getFilesDir(), filename);
        System.out.println("clickAction");
        System.out.println(file.getAbsolutePath());
        try {
            // first time installing app
            if (file.createNewFile()) {
                BufferedWriter bufferedWriter = new BufferedWriter( new OutputStreamWriter(new FileOutputStream(file)));
                bufferedWriter.write("{\"username\":\"\",\"token\":\"\"}");
                bufferedWriter.close();
                System.out.println("a file was made");
            // try to get login info
            } else {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                JSONObject info = new JSONObject(bufferedReader.readLine());
                System.out.println(info.toString());
                username = info.getString("username");
                token = info.getString("token");
                System.out.println(username + " " + token);
                System.out.println("a file was read");
                bufferedReader.close();
                if (!username.isEmpty() && !token.isEmpty()) {
                    ValidateToken validateToken = new ValidateToken();
                    validateToken.execute();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

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
                if(swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(true);
                }
                update();
            }
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new PostAdapter(new ArrayList<PostItem>(), this));
        postItems = new ArrayList<>();
    }


    private void update() {
        GetData getData = new GetData();
        getData.execute();

    }
    private void loadIntoRecyclerView(String json) {
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
            System.out.println(postItems.get(0).getTitle());
            noConnection.setVisibility(View.INVISIBLE);
            endOfPosts.setVisibility(View.VISIBLE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        adapter = new PostAdapter(postItems, getApplicationContext());
        System.out.println("done making adapter");
        recyclerView.setAdapter(adapter);
        System.out.println("set adapted");
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (recreate) {
            recreate();
            recreate = false;
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();

        if (id == R.id.Sign_up) {
            Intent intent = new Intent(this, LogIn.class);
            startActivity(intent);
        } else if (id == R.id.profile) {
            Intent intent = new Intent(this, ViewProfile.class); //fixme make profile activity
            startActivity(intent);
        } else if (id == R.id.logout) {
            setContentView(R.layout.activity_navigation);
            loggedIn = false;
            try {
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false)));
                bufferedWriter.write("{\"username\":\"\",\"token\":\"\"}");

            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("hellloooooooooooooooooooooooooooo");
            recreate();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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

    class GetData extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
//            Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
            loadIntoRecyclerView(s);
            swipeRefreshLayout.setRefreshing(false);
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                System.out.println("start of try");
                URL url = new URL(URL + "/post?sub_name=hehe");
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

    class ValidateToken extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean s) {
            super.onPostExecute(s);
            name = (Button) findViewById(R.id.username);
            signUpMessage = findViewById(R.id.SignUpMessage);
            if (s) {
                String temp = "u\\" + username;
                name.setText(temp);
                name.setVisibility(View.VISIBLE);
                signUpMessage.setVisibility(View.GONE);
                signUp.setVisible(false);
                profile.setVisible(true);
                logout.setVisible(true);
                System.out.println("changing drawer");
                loggedIn = true;
            } else {
                noConnection.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                URL url = new URL(URL + "/validate?username=" + username + "&token=" + URLEncoder.encode(token, "UTF-8"));
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
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
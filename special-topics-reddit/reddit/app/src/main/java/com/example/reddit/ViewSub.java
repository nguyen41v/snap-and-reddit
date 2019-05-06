package com.example.reddit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
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
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class ViewSub extends AppCompatActivity {


    private static RecyclerView recyclerView;
    private static RecyclerView.Adapter adapter;
    private static List<PostItem> postItems;
    private CardView endOfPosts;
    public TextView noConnection;
    private SwipeRefreshLayout swipeRefreshLayout;
    String sub;
    private Boolean follow;
    private TextView sub_name;
    private TextView sub_info;
    private String newActivity;
    public static Boolean recreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_sub);
        setBotBarClickListeners();
        recreate = false;
        Intent intent = getIntent();
        sub = intent.getStringExtra("sub");
        sub_name = findViewById(R.id.sub);
        sub_info = findViewById(R.id.info);
        String temp = "s/" + sub;
        SearchView searchView = findViewById(R.id.searchView);
        searchView.setQueryHint("Search in " + temp);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                startActivity(new Intent(getApplication(), SearchResults.class).putExtra("query", query).putExtra("sub", sub));
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });
        sub_name.setText(temp);
        noConnection = findViewById(R.id.NoConnection);
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
        findViewById(R.id.follow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                follow = true;
                ValidateToken validateToken = new ValidateToken();
                validateToken.execute();
            }
        });
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        System.out.println("clickAction?");
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("starting reply?");
        System.out.println(MainActivity.loggedIn);
        if (MainActivity.loggedIn) {
            update();
            switch (newActivity) {
                case "post":
                    startActivity(new Intent(getApplicationContext(), MakePost.class).putExtra("sub_name", sub));
                    break;
                case "sub":
                    startActivity(new Intent(getApplicationContext(), MakeSub.class));
                    break;
                default:
                    break;
            }
        }
    }

    public void setBotBarClickListeners() {
        final CardView home = findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });

        final CardView communitites = findViewById(R.id.communities);
        communitites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    startActivity(new Intent(getApplicationContext(), SubsScreen.class));

            }
        });
        final CardView addSub = findViewById(R.id.addSub);
        addSub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newActivity = "sub";
                AnotherValidateToken anotherValidateToken = new AnotherValidateToken();
                anotherValidateToken.execute();
            }
        });
        final CardView chat = findViewById(R.id.chat);
        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    startActivity(new Intent(getApplicationContext(), Chat.class));

            }
        });
        final CardView makePost = findViewById(R.id.makePost);
        makePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newActivity = "post";
                AnotherValidateToken anotherValidateToken = new AnotherValidateToken();
                anotherValidateToken.execute();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (recreate) {
            recreate();
            MainActivity.recreate = true;
        }
    }

    class AnotherValidateToken extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean s) {
            super.onPostExecute(s);
            if (!s) {
                MainActivity.loggedIn = false;
                startActivityForResult(new Intent(getApplicationContext(), LogIn.class),1);
            } else {
                switch (newActivity) {
                    case "post":
                        startActivity(new Intent(getApplicationContext(), MakePost.class).putExtra("sub_name", sub));
                        break;
                    case "sub":
                        startActivity(new Intent(getApplicationContext(), MakeSub.class));
                        break;
                    default:
                        break;
                }
            }
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                URL url = new URL(MainActivity.URL + "/validate?username=" + MainActivity.username + "&token=" + URLEncoder.encode(MainActivity.token, "UTF-8"));
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);
                con.connect();
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
            JSONObject sub = jsonArray.getJSONObject(0);
            if (sub.has("info")) {
                sub_info.setVisibility(View.VISIBLE);
                sub_info.setText(sub.getString("info"));
            } else {
                sub_info.setVisibility(View.INVISIBLE);
            }
            for (int i = 1; i < jsonArray.length(); i++) {
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



    class Follow extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s.isEmpty()) {
                Toast toast = Toast.makeText(getApplicationContext(), "Could not connect to the server", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 64);
                toast.show();
            } else {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    Toast toast = Toast.makeText(getApplicationContext(), jsonObject.getString("message"), Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 64);
                    toast.show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(MainActivity.URL + "/follow");
                System.out.println(url);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");

                JSONObject requestBody = new JSONObject();
                requestBody.put("username", MainActivity.username);
                requestBody.put("token", MainActivity.token);
                requestBody.put("sub_name", sub);
                System.out.println(requestBody);
                con.setDoOutput(true);
                con.setRequestProperty("Content-Type","application/json");
                OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
                writer.write(requestBody.toString());
                writer.flush();
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);
                con.connect();
                StringBuilder sb = new StringBuilder();
                int responseCode = con.getResponseCode();
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
            if (s) {
                if (follow) {
                    Follow follow = new Follow();
                    follow.execute();
                }
                // if i have unfollow button
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), "Log in to follow subs!", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 64);
                toast.show();
                startActivity(new Intent(getApplicationContext(), LogIn.class));
            }
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                URL url = new URL(MainActivity.URL + "/validate?username=" + MainActivity.username + "&token=" + URLEncoder.encode(MainActivity.token, "UTF-8"));
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);
                con.connect();
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

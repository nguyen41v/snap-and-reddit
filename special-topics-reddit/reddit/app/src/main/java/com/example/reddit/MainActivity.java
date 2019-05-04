package com.example.reddit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabItem;

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

public class MainActivity extends Navigation {

    //    ListView listView;
    public static final String URL = "http://ec2-3-18-206-131.us-east-2.compute.amazonaws.com";
    public static final String PREFS_NAME = "login.txt";
    public static SharedPreferences pref;
    public static String username = "";
    public static String token = "";
    public static Editor editor;
    public static Boolean loggedIn = false;
    public static Boolean recreate = false;
    public static int OK = 200;
    public static int UNAUTHORIZED = 401;
    private SectionsPagerAdapter sectionsPagerAdapter;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        setBotBarClickListeners();
        noConnection = findViewById(R.id.NoConnection);
        // get shared preferences: username, token
        pref = getApplicationContext().getSharedPreferences(PREFS_NAME, 0); // 0 - for private mode
        username = pref.getString("username", "");
        token = pref.getString("token", "");
        System.out.println(username + " " + token);
        System.out.println("got shared preferences");
        editor = pref.edit();
        makeMenu();

        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(sectionsPagerAdapter);
    }

    private void setBotBarClickListeners() {
        final CardView home = findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
                startActivity(new Intent(getApplicationContext(), MakeSub.class));
            }
        });
        final CardView chat = findViewById(R.id.home);
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
                startActivity(new Intent(getApplicationContext(), MakePost.class));
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (recreate) {
            recreate();
            recreate = false;
        }
    }


    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            if (position == 0) {
                return new Home();
            }
            if (position == 1) {
                return new Popular();
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }
    }


    public static class Home extends Fragment {

        private static RecyclerView recyclerView;
        private static RecyclerView.Adapter adapter;
        private static List<PostItem> postItems;
        private CardView endOfPosts;
        private SwipeRefreshLayout swipeRefreshLayout;

        public Home() {

        }

        public static Home newInstance(int sectionNumber) {
            Home fragment = new Home();
            Bundle args = new Bundle();
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.main_page_post, container, false);
            endOfPosts = rootView.findViewById(R.id.endCard);
            // temporary adapter for recycler view
            recyclerView = rootView.findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.setAdapter(new PostAdapter(new ArrayList<PostItem>(), getActivity()));
            postItems = new ArrayList<>();

            swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
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


            return rootView;
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
            adapter = new PostAdapter(postItems, getActivity());
            System.out.println("done making adapter");
            recyclerView.setAdapter(adapter);
            System.out.println("set adapted");
        }


        private void update() {
            if (!MainActivity.username.isEmpty() && !MainActivity.token.isEmpty()) {
                GetData getData = new GetData();
                getData.execute();
            } else {
                System.out.println("sign up!");
                // show view telling user to sign up
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
                loadIntoRecyclerView(s);
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            protected String doInBackground(Void... voids) {
                try {
                    System.out.println("start of try");
                    URL url = new URL(URL + "/home?username=" + MainActivity.username + "&token=" + URLEncoder.encode(MainActivity.token, "UTF-8"));
                    System.out.println(url);
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
                    if (responseCode == OK) {
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


    public static class Popular extends Fragment {

        private static RecyclerView recyclerView;
        private static RecyclerView.Adapter adapter;
        private static List<PostItem> postItems;
        private CardView endOfPosts;
        private SwipeRefreshLayout swipeRefreshLayout;

        private static final String valid = "Successfully logged in";
        private static final String invalid = "Invalid username/password combo";

        public Popular() {

        }

        public static Popular newInstance(int sectionNumber) {
            Popular fragment = new Popular();
            Bundle args = new Bundle();
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.main_page_post, container, false);
            endOfPosts = rootView.findViewById(R.id.endCard);
            // temporary adapter for recycler view
            recyclerView = rootView.findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.setAdapter(new PostAdapter(new ArrayList<PostItem>(), getActivity()));
            postItems = new ArrayList<>();

            swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
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


            return rootView;
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
            adapter = new PostAdapter(postItems, getActivity());
            System.out.println("done making adapter");
            recyclerView.setAdapter(adapter);
            System.out.println("set adapted");
        }


        private void update() {
            GetData getData = new GetData();
            getData.execute();

        }

        class GetData extends AsyncTask<Void, Void, String> {

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
                    System.out.println("start of try");
                    URL url = new URL(URL + "/popular?username=" + MainActivity.username + "&token=" + URLEncoder.encode(MainActivity.token, "UTF-8"));
                    System.out.println(url);
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
                    if (responseCode == OK) {
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


}
package com.example.reddit;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayout.Tab;

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
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // get shared preferences: username, token
        pref = getApplicationContext().getSharedPreferences(PREFS_NAME, 0); // 0 - for private mode
        username = pref.getString("username", "");
        token = pref.getString("token", "");
        System.out.println(username + " " + token);
        System.out.println("got shared preferences");
        editor = pref.edit();
        activity = "main";
        makeMenu();
        setBotBarClickListeners();
        ((SearchView)findViewById(R.id.searchView)).setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
        noConnection = findViewById(R.id.NoConnection);
        tabLayout = findViewById(R.id.tab_layout);

        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(sectionsPagerAdapter);
        /**
         * on swiping the viewpager make respective tab selected
         * */
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                // on changing the page
                // make respected tab selected
                Tab tab = tabLayout.getTabAt(position);
                tab.select();
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabReselected(Tab tab) {
            }

            @Override
            public void onTabSelected(Tab tab) {
                // on tab selected
                // show respected fragment view
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(Tab tab) {
            }
        });
        switchTab();
    }

    private void switchTab() {
        System.out.println(loggedIn);
        if (!loggedIn) {
            tabLayout.getTabAt(1).select();
            viewPager.setCurrentItem(1);
        } else {
            tabLayout.getTabAt(0).select();
            viewPager.setCurrentItem(0);
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
            rootView.findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getActivity(), LogIn.class));
                }
            });
            rootView.findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getActivity(), LogIn.class).putExtra("type", "signup"));
                }
            });

            swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
            swipeRefreshLayout.setOnRefreshListener(
                    new SwipeRefreshLayout.OnRefreshListener() {
                        @Override
                        public void onRefresh() {
                            swipeRefreshLayout.setRefreshing(true);
                            home_update();
                        }
                    }
            );

            swipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(true);
                    }
                    home_update();
                }
            });
            return rootView;
        }

        private void homeLoadIntoRecyclerView(String json) {
            System.out.println(json);
            postItems = new ArrayList<>();
            System.out.println("loading into recylcer view");
            System.out.println(json);
            if (json.isEmpty()) {
                noConnection.setVisibility(View.VISIBLE);
                endOfPosts.setVisibility(View.INVISIBLE);
                return;
            }
            if (json.equals("0")) {
                System.out.print("hello");
                swipeRefreshLayout.setVisibility(View.INVISIBLE);
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
                swipeRefreshLayout.setVisibility(View.VISIBLE);
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


        private void home_update() {
            if (!MainActivity.username.isEmpty() && !MainActivity.token.isEmpty()) {
                GetHomeData getHomeData = new GetHomeData();
                getHomeData.execute();
            } else {
                swipeRefreshLayout.setVisibility(View.INVISIBLE);
                swipeRefreshLayout.setRefreshing(false);
            }

        }

        class GetHomeData extends AsyncTask<Void, Void, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                homeLoadIntoRecyclerView(s);
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
                    return "0";

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
            View rootView = inflater.inflate(R.layout.popular, container, false);
            endOfPosts = rootView.findViewById(R.id.pendCard);
            // temporary adapter for recycler view
            recyclerView = rootView.findViewById(R.id.precyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.setAdapter(new PostAdapter(new ArrayList<PostItem>(), getActivity()));
            postItems = new ArrayList<>();
            swipeRefreshLayout = rootView.findViewById(R.id.pswipeRefreshLayout);
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


            return rootView;
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
                System.out.println("pdone getting data");
                noConnection.setVisibility(View.INVISIBLE);
                endOfPosts.setVisibility(View.VISIBLE);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            adapter = new PostAdapter(postItems, getActivity());
            System.out.println("pdone making adapter");
            recyclerView.setAdapter(adapter);
            System.out.println("pset adapted");
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
                    URL url = new URL(URL + "/popular?username=" + MainActivity.username + "&token=" + URLEncoder.encode(MainActivity.token, "UTF-8"));
                    System.out.println(url);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(5000);
                    con.setReadTimeout(5000);
                    con.connect();
                    StringBuilder sb = new StringBuilder();
                    int responseCode = con.getResponseCode();
                    System.out.println(responseCode);
                    if (responseCode == OK) {
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


}
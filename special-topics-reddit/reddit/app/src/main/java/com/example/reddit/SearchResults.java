package com.example.reddit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.google.android.material.tabs.TabLayout;

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

public class SearchResults extends AppCompatActivity {


    private static SectionsPagerAdapter sectionsPagerAdapter;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private static String query;
    private BestFrag bestFrag;
    private PostFrag postFrag;
    private SubFrag subFrag;
    private ProfFrag profFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);
        Intent intent = getIntent();
        query = intent.getStringExtra("query");
        sectionsPagerAdapter = new SearchResults.SectionsPagerAdapter(getSupportFragmentManager());
        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                // on changing the page
                // make respected tab selected
                TabLayout.Tab tab = tabLayout.getTabAt(position);
                tab.select();
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });

        tabLayout = findViewById(R.id.tab_layout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // on tab selected
                // show respected fragment view
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }
        });
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
                finish();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });
        update();
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private Fragment[] fragments;


        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            this.fragments = new Fragment[4];
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    bestFrag = new BestFrag();
                    return bestFrag;
                case 1:
                    postFrag = new PostFrag();
                    return postFrag;
                case 2:
                    subFrag = new SubFrag();
                    return subFrag;
                case 3:
                    profFrag = new ProfFrag();
                    return profFrag;
            }
            return null;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
            // save the appropriate reference depending on position
            switch (position) {
                case 0:
                    fragments[0] = createdFragment;
                    break;
                case 1:
                    fragments[1] = createdFragment;
                    break;
                case 2:
                    fragments[2] = createdFragment;
                    break;
                case 3:
                    fragments[3] =  createdFragment;
            }
            return createdFragment;
        }

        public void loadResults(String s) {
            // do work on the referenced Fragments, but first check if they
            // even exist yet, otherwise you'll get an NPE.
            try {
                JSONObject jsonObject = new JSONObject(s);
                if (fragments[0] != null) {
                    ((BestFrag) fragments[0]).loadIntoRecyclerView(jsonObject);
                    ((BestFrag) fragments[0]).swipeRefreshLayout.setRefreshing(false);
                }
                if (fragments[1] != null) {
                    ((PostFrag) fragments[1]).loadIntoRecyclerView(jsonObject.getJSONArray("posts"));
                    ((PostFrag) fragments[1]).swipeRefreshLayout.setRefreshing(false);

                }
                if (fragments[2] != null) {
                    ((SubFrag) fragments[2]).loadIntoRecyclerView(jsonObject.getJSONArray("subs"));
                    ((SubFrag) fragments[2]).swipeRefreshLayout.setRefreshing(false);

                }
                if (fragments[3] != null) {
                    ((ProfFrag) fragments[3]).loadIntoRecyclerView(jsonObject.getJSONArray("users"));
                    ((ProfFrag) fragments[3]).swipeRefreshLayout.setRefreshing(false);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }

        @Override
        public int getCount() {
            // Show 4 total pages.
            return 4;
        }
    }


    public static class BestFrag extends Fragment {

        private static RecyclerView recyclerView;
        private static RecyclerView.Adapter adapter;
        private static RecyclerView recyclerView2;
        private static RecyclerView.Adapter adapter2;
        private static List<PostItem> postItems;
        private static List<SubItem> subItems;
        private SwipeRefreshLayout swipeRefreshLayout;


        private static final String valid = "Successfully logged in";
        private static final String invalid = "Invalid username/password combo";

        public BestFrag() {

        }

        public static BestFrag newInstance(int sectionNumber) {
            BestFrag fragment = new BestFrag();
            Bundle args = new Bundle();
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.search_results_best, container, false);
            // temporary adapter for recycler view
            recyclerView = rootView.findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.setAdapter(new PostAdapter(new ArrayList<PostItem>(), getActivity()));
            recyclerView2 = rootView.findViewById(R.id.recyclerView2);
            recyclerView2.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView2.setAdapter(new SubAdapterToViewSubs(new ArrayList<SubItem>(), getActivity()));

            swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
            swipeRefreshLayout.setOnRefreshListener(
                    new SwipeRefreshLayout.OnRefreshListener() {
                        @Override
                        public void onRefresh() {
                            update();
                        }
                    }
            );
            return rootView;
        }



        private void loadIntoRecyclerView(JSONObject json) {
            System.out.println(json);
            postItems = new ArrayList<>();
            subItems = new ArrayList<>();
            System.out.println("loading into recylcer view");
            System.out.println(json);
            try {
                JSONArray jsonArray = json.getJSONArray("posts");
                for (int i = 0; i < jsonArray.length() && i < 10; i++) {
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
                jsonArray = json.getJSONArray("subs");
                for (int i = 0; i < jsonArray.length() && i < 10; i++) {
                    JSONObject sub = jsonArray.getJSONObject(i);
                    subItems.add(new SubItem(sub.getString("sub_name")));
                }
                System.out.println("done getting data");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            adapter2 = new PostAdapter(postItems, getActivity());
            System.out.println("done making adapter");
            recyclerView2.setAdapter(adapter2);
            adapter = new SubAdapterToViewSubs(subItems, getActivity());
            System.out.println("done making adapter");
            recyclerView.setAdapter(adapter);
            System.out.println("set adapted");
        }

    }

    public static class PostFrag extends Fragment {

        private static RecyclerView recyclerView;
        private static RecyclerView.Adapter adapter;
        private static List<PostItem> postItems;
        private SwipeRefreshLayout swipeRefreshLayout;


        public PostFrag() {

        }

        public static PostFrag newInstance(int sectionNumber) {
            PostFrag fragment = new PostFrag();
            Bundle args = new Bundle();
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.search_results_posts, container, false);
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
                        }
                    }
            );
            return rootView;
        }

        private void loadIntoRecyclerView(JSONArray jsonArray) {
            postItems = new ArrayList<>();
            System.out.println("loading into recylcer view");
            try {
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
            } catch (JSONException e) {
                e.printStackTrace();
            }
            adapter = new PostAdapter(postItems, getActivity());
            System.out.println("done making adapter");
            recyclerView.setAdapter(adapter);
            System.out.println("set adapted");
        }

    }

    public static class SubFrag extends Fragment {

        private static RecyclerView recyclerView;
        private static RecyclerView.Adapter adapter;
        private static List<SubItem> subItems;
        private SwipeRefreshLayout swipeRefreshLayout;


        public SubFrag() {

        }

        public static SubFrag newInstance(int sectionNumber) {
            SubFrag fragment = new SubFrag();
            Bundle args = new Bundle();
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.search_results_subs, container, false);
            // temporary adapter for recycler view
            recyclerView = rootView.findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.setAdapter(new PostAdapter(new ArrayList<PostItem>(), getActivity()));
            subItems = new ArrayList<>();

            swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
            swipeRefreshLayout.setOnRefreshListener(
                    new SwipeRefreshLayout.OnRefreshListener() {
                        @Override
                        public void onRefresh() {
                            update();
                        }
                    }
            );
            return rootView;
        }


        private void loadIntoRecyclerView(JSONArray jsonArray) {
            subItems = new ArrayList<>();
            System.out.println("loading into recylcer view");
            try {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject sub = jsonArray.getJSONObject(i);
                    subItems.add(new SubItem(sub.getString("sub_name")));
                }
                System.out.println("done getting data");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            adapter = new SubAdapterToViewSubs(subItems, getActivity());
            System.out.println("done making adapter");
            recyclerView.setAdapter(adapter);
            System.out.println("set adapted");
        }

    }

    public static class ProfFrag extends Fragment {

        private static RecyclerView recyclerView;
        private static RecyclerView.Adapter adapter;
        private static List<UserItem> userItems;
        private SwipeRefreshLayout swipeRefreshLayout;


        public ProfFrag() {

        }

        public static ProfFrag newInstance(int sectionNumber) {
            ProfFrag fragment = new ProfFrag();
            Bundle args = new Bundle();
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.search_results_profiles, container, false);
            // temporary adapter for recycler view
            recyclerView = rootView.findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.setAdapter(new PostAdapter(new ArrayList<PostItem>(), getActivity()));

            swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
            swipeRefreshLayout.setOnRefreshListener(
                    new SwipeRefreshLayout.OnRefreshListener() {
                        @Override
                        public void onRefresh() {
                            update();
                        }
                    }
            );
            return rootView;
        }

        private void loadIntoRecyclerView(JSONArray jsonArray) {
            userItems = new ArrayList<>();
            System.out.println("loading into recylcer view");
            try {
                for (int i = 0; i < jsonArray.length(); i++) {
                    userItems.add(new UserItem(jsonArray.getJSONObject(i).getString("username")));
                }
                System.out.println("done getting data");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            adapter = new UserAdapter(userItems, getActivity());
            System.out.println("done making adapter");
            recyclerView.setAdapter(adapter);
            System.out.println("set adapted");
        }

    }

    static private void update() {
        GetSearchResults getSearchResults = new GetSearchResults();
        getSearchResults.execute();

    }

    static class GetSearchResults extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            sectionsPagerAdapter.loadResults(s);
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(MainActivity.URL + "/search?username=" + MainActivity.username +
                        "&token=" + URLEncoder.encode(MainActivity.token, "UTF-8") +
                        "&search=" + URLEncoder.encode(query, "UTF-8") );
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

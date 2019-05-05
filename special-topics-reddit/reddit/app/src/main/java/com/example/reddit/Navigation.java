package com.example.reddit;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import android.util.Log;
import android.view.View;

import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class Navigation extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public Button name;
    public View signUpMessage;
    public MenuItem signUp;
    public MenuItem profile;
    public MenuItem logout;
    public static TextView noConnection;
    public String activity;
    public String newActivity = "";


    public static NavigationView navigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }



    public void makeMenu () {
        // navigation drawer set up
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        toolbar.setNavigationIcon(R.drawable.ic_person_purple_24dp);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);
        name = header.findViewById(R.id.username);
        signUpMessage = header.findViewById(R.id.SignUpMessage);

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
        if (!MainActivity.username.isEmpty() && !MainActivity.token.isEmpty()) {
            ValidateToken validateToken = new ValidateToken();
            validateToken.execute();
        }

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
        getMenuInflater().inflate(R.menu.navigation, menu);
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();

        if (id == R.id.Sign_up) {
            Intent intent = new Intent(this, LogIn.class);
            startActivity(intent);
        } else if (id == R.id.profile) {
            Intent intent = new Intent(this, ViewProfile.class);
            startActivity(intent);
        } else if (id == R.id.logout) {
            setContentView(R.layout.activity_main);
            MainActivity.loggedIn = false;
            MainActivity.editor.clear();
            MainActivity.editor.apply();
            System.out.println("hellloooooooooooooooooooooooooooo");
            recreate();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public void setBotBarClickListeners() {
        final CardView home = findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!activity.equals("main")) {
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                }
            }
        });

        final CardView communitites = findViewById(R.id.communities);
        communitites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!activity.equals("communities")) {
                    startActivity(new Intent(getApplicationContext(), SubsScreen.class));
                }
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
                if (!activity.equals("chat")) {
                    startActivity(new Intent(getApplicationContext(), Chat.class));
                }
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
        if (MainActivity.recreate) {
            recreate();
            MainActivity.recreate = false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        System.out.println("clickAction?");
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("starting reply?");
        System.out.println(MainActivity.loggedIn);
        if (MainActivity.loggedIn) {
            switch (newActivity) {
                case "post":
                    startActivity(new Intent(getApplicationContext(), MakePost.class));
                    break;
                case "sub":
                    startActivity(new Intent(getApplicationContext(), MakeSub.class));
                    break;
                default:
                    break;
            }
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
                        startActivity(new Intent(getApplicationContext(), MakePost.class));
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



    class ValidateToken extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean s) {
            super.onPostExecute(s);
            if (s) {
                name.setVisibility(View.VISIBLE);
                signUpMessage.setVisibility(View.GONE);
                String temp = "u\\" + MainActivity.username;
                name.setText(temp);
                signUp.setVisible(false);
                profile.setVisible(true);
                logout.setVisible(true);
                System.out.println("changing drawer");
                MainActivity.loggedIn = true;
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

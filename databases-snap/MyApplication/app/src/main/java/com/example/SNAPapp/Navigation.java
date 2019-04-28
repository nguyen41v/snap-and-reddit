package com.example.SNAPapp;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.navigation.NavigationView;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class Navigation extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    public Button name;
    public View signUpMessage;
    public String activity = "NA";


    public MenuItem signUp;
    public MenuItem account;
    public MenuItem logout;
    public MenuItem message;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
    }

    public void makeMenu() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        name = findViewById(R.id.username);
        signUpMessage = findViewById(R.id.SignUpMessage);

        Menu menu = navigationView.getMenu();
        for (int menuItemIndex = 0; menuItemIndex < menu.size(); menuItemIndex++) {
            MenuItem menuItem= menu.getItem(menuItemIndex);
            if(menuItem.getItemId() == R.id.sign_up){
                signUp = menuItem;
            } else if (menuItem.getItemId() == R.id.account) {
                account = menuItem;
            } else if (menuItem.getItemId() == R.id.message) {
                message = menuItem;
            } else if (menuItem.getItemId() == R.id.logout) {
                logout = menuItem;
            }
        }
        if (Launcher.loggedIn) {
            name.setVisibility(View.VISIBLE);
            signUpMessage.setVisibility(View.GONE);
            String temp = "u\\" + Launcher.username;
            name.setText(temp);
            signUp.setVisible(false);
            account.setVisible(true);
            logout.setVisible(true);
            message.setVisible(true);
            System.out.println("changing drawer");
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
        // Inflate the menu; this adds state_names to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_overview_navigation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Launcher/Up button, so long
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

        if (id == R.id.sign_up && !activity.equals("LoginSignup")) {
            startActivity(new Intent(this, LoginSignup.class));
        } else if (id == R.id.overview && !activity.equals("Overview")) {
            startActivity(new Intent(this, Overview.class));
        } else if (id == R.id.recent_transactions && !activity.equals("RecentTransactions")) {
            startActivity(new Intent(this, RecentTransactions.class));
        } else if (id == R.id.home && !activity.equals("Launcher")) {
            startActivity(new Intent(this, Launcher.class));
        } else if (id == R.id.nearby && !activity.equals("NearbyStores")) {
            startActivity(new Intent(this, NearbyStores.class));
        } else if (id == R.id.message && !activity.equals("Message")) {

        } else if (id == R.id.forum && !activity.equals("Forum")) {

        } else if (id == R.id.faq && !activity.equals("FAQ")) {

        } else if (id == R.id.logout) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}

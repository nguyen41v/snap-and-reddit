package com.example.SNAPapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class Message extends Navigation {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        makeMenu();
    }
}

package com.example.reddit;

public class UserItem {
    // in the future, could store the user icon location like reddit
    private String username;

    public UserItem(String username) {
        this.username = username;
    }

    public String getUsername() {
        return this.username;
    }
}

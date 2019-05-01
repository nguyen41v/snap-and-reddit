package com.example.SNAPapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import androidx.fragment.app.Fragment;

import static android.app.Activity.RESULT_OK;

public class LoginTab extends Fragment{


    private static String username;
    private static String password;
    private static Button logIn;
    private static EditText user;
    private static EditText pass;
    private static TextView signUp;
    private static ProgressBar progressBar;
    private static final String valid = "Successfully logged in";
    private static final String invalid = "Invalid username/password combo";
    private static final String noConnection = "Could not connect to the server at this moment";

    private OnButtonClickListener mOnButtonClickListener;

    interface OnButtonClickListener{
        void onButtonClicked(View view);
    }

    public LoginTab() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static LoginTab newInstance(int sectionNumber) {
        LoginTab fragment = new LoginTab();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.login, container, false);

        progressBar = rootView.findViewById(R.id.progressBar);
        user = rootView.findViewById(R.id.username);
        pass = rootView.findViewById(R.id.new_password);
        signUp = rootView.findViewById(R.id.sign_up);
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnButtonClickListener.onButtonClicked(v);
            }
        });

        logIn = rootView.findViewById(R.id.logInButton);
        logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = user.getText().toString();
                password = pass.getText().toString();
                System.out.println(username);
                System.out.println(password);
                if (username.length() == 0) {
                    Toast toast = Toast.makeText(getActivity(), "You must have a username", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER,0,64);
                    toast.show();
                } else if (password.length() == 0) {
                    Toast toast = Toast.makeText(getActivity(), "You must have a password", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER,0,64);
                    toast.show();
                } else if (username.length() > 16) {
                    Toast toast = Toast.makeText(getActivity(), "A username is 16 characters or less", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER,0,64);
                    toast.show();
                } else {
                    logIn.setEnabled(false);
                    progressBar.setVisibility(View.VISIBLE);
                    ValidateLogin validateLogin = new ValidateLogin();
                    validateLogin.execute();
                }
            }
        });

        return rootView;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mOnButtonClickListener = (OnButtonClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(((Activity) context).getLocalClassName()
                    + " must implement OnButtonClickListener");
        }
    }



    class ValidateLogin extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            logIn.setEnabled(true);
            progressBar.setVisibility(View.GONE);
            if (s.contentEquals("0")) {
                Toast toast = Toast.makeText(getActivity(), invalid, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER,0,64);
                toast.show();
            } else if (s.contentEquals("-1")) {
                Toast toast = Toast.makeText(getActivity(), noConnection, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER,0,64);
                toast.show();
            }
            else {
                Toast toast = Toast.makeText(getActivity(), valid, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL,0,64);
                toast.show();
                try {
                    JSONObject json = new JSONObject(s);
                    String tokenInfo = json.getString("token");
                    Launcher.username = username;
                    Launcher.token = tokenInfo;
                    System.out.println(tokenInfo);
                    Launcher.write();
                    Launcher.editor.putString("username", username);
                    Launcher.editor.putString("token", tokenInfo);
                    Launcher.editor.apply();
                    Launcher.loggedIn = true;
                    Overview.recreate = true;
                    System.out.println(Launcher.loggedIn);
                    getActivity().setResult(RESULT_OK, getActivity().getIntent());
                    Intent intent = new Intent(getContext(), Overview.class);
                    startActivity(intent);
                    getActivity().finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                System.out.println("hello");
                System.out.println(Launcher.URL);
                URL url = new URL(Launcher.URL + "/login?username=" + username + "&password=" + password);
                System.out.println("uuhhh");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                System.out.println("am i connecting?");

                con.setRequestMethod("GET");
                System.out.println("uuhhh");

                con.connect();
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);
                int responseCode = con.getResponseCode();
                System.out.println(responseCode);
                if (responseCode == Launcher.OK) {
                    StringBuilder sb = new StringBuilder();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String json;
                    System.out.println("got data");
                    while ((json = bufferedReader.readLine()) != null) {
                        sb.append(json + "\n");
                    }
                    String temp = sb.toString().trim();
                    System.out.print(temp);
                    return temp;
                } else if (responseCode == Launcher.UNAUTHORIZED) {
                    return "0";
                }
            } catch (Exception e) {
                System.out.println("Connection probably failed :3\ngo start the server");
                return "-1";
            }
            return "-1";
        }
    }
}

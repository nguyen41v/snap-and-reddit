package com.example.reddit;

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
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import androidx.fragment.app.Fragment;

import static android.app.Activity.RESULT_OK;

public class SignupTab extends Fragment {


    private static String username;
    private static String password;
    private static String email;
    private static Button signUp;
    private static EditText user;
    private static EditText pass;
    private static EditText phone;
    private static ProgressBar progressBar;
    private static final String valid = "Successfully registered in";
    private static final String noConnection = "Could not connect to the server at this moment";


    public SignupTab() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static SignupTab newInstance(int sectionNumber) {
        SignupTab fragment = new SignupTab();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.signup, container, false);

        progressBar = rootView.findViewById(R.id.progressBar);
        user = rootView.findViewById(R.id.username);
        pass = rootView.findViewById(R.id.new_password);
        phone = rootView.findViewById(R.id.new_email);

        signUp = rootView.findViewById(R.id.signupButton);
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = user.getText().toString();
                password = pass.getText().toString();
                email = phone.getText().toString();
                System.out.println(username);
                System.out.println(password);
                System.out.println(email);
                if (!email.matches("^.*@.*\\..*$")) {
                    Toast toast = Toast.makeText(getActivity(), "Your email is not valid", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 64);
                    toast.show();
                } else if (username.length() == 0) {
                    Toast toast = Toast.makeText(getActivity(), "You must have a username", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER,0,64);
                    toast.show();
                } else if (username.length() > 16) {
                    Toast toast = Toast.makeText(getActivity(), "A username is 16 characters or less", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER,0,64);
                    toast.show();
                } else if (password.length() == 0) {
                    Toast toast = Toast.makeText(getActivity(), "You must have a password", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER,0,64);
                    toast.show();
                } else {
                    signUp.setEnabled(false);
                    progressBar.setVisibility(View.VISIBLE);
                    ValidateLogin validateLogin = new ValidateLogin();
                    validateLogin.execute();
                }
            }
        });

        return rootView;
    }



    class ValidateLogin extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            signUp.setEnabled(true);
            progressBar.setVisibility(View.GONE);
            if (s.contentEquals("-1")) {
                Toast toast = Toast.makeText(getActivity(), noConnection, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER,0,64);
                toast.show();
            }
            else {
                try {
                    JSONObject json = new JSONObject(s);
                    if (json.has("token")) {
                        Toast toast = Toast.makeText(getActivity(), valid, Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL,0,64);
                        toast.show();
                        String tokenInfo = json.getString("token");
                        MainActivity.username = username;
                        MainActivity.token = tokenInfo;
                        System.out.println(tokenInfo);
                        MainActivity.editor.putString("username", username);
                        MainActivity.editor.putString("token", tokenInfo);
                        MainActivity.editor.apply();
                        MainActivity.loggedIn = true;
                        MainActivity.recreate = true;
                        System.out.println(MainActivity.loggedIn);
                        getActivity().setResult(RESULT_OK, getActivity().getIntent());
                        Intent intent = new Intent(getContext(), MainActivity.class);
                        startActivity(intent);
                        getActivity().finish();
                    } else {
                        String message = json.getString("message");
                        Toast toast = Toast.makeText(getActivity(), message, Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL,0,64);
                        toast.show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                System.out.println("hello");
                System.out.println(MainActivity.URL);
                URL url = new URL(MainActivity.URL + "/register");
                System.out.println("uuhhh");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                System.out.println("am i connecting?");

                con.setRequestMethod("POST");
                System.out.println("uuhhh");
                JSONObject requestBody = new JSONObject();
                requestBody.put("username", username);
                requestBody.put("password", password);
                requestBody.put("email", email);
                System.out.println(requestBody);
                con.setDoOutput(true);
                con.setRequestProperty("Content-Type","application/json");
                OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
                writer.write(requestBody.toString());
                writer.flush();

                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);
                con.connect();
                int responseCode = con.getResponseCode();
                System.out.println(responseCode);
                StringBuilder sb = new StringBuilder();
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
                e.printStackTrace();
                System.out.println("Connection probably failed :3\ngo start the server");
                return "-1";
            }
        }
    }
}
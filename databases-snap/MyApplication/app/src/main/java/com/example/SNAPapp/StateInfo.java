package com.example.SNAPapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import androidx.cardview.widget.CardView;

public class StateInfo extends Navigation {

    private MenuItem signUp;
    private MenuItem account;
    private MenuItem logout;
    private MenuItem message;
    public static Boolean recreate = false;
    public String state;
    private Button name;
    private CardView messageCard;
    private TextView messageText;
    private ProgressBar progressBar;
    private Button viewDis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_state_info);
        makeMenu();
        activity = "StateInfo";
        state = Launcher.pref.getString("State", "");
        System.out.println(state);
        messageCard = findViewById(R.id.cardView);
        messageText = findViewById(R.id.loading_message);
        progressBar = findViewById(R.id.progressBar);
        viewDis = findViewById(R.id.getUserDistribution);
        if (state.isEmpty()) {
            System.out.println("new activity, initial state");
            startActivity(new Intent(this, InitialState.class).setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
        } else {
            update();
        }
    }

    private void update() {
        GetData getData = new GetData();
        getData.execute();
    }


    @Override
    public void onResume() {
        super.onResume();
        if (recreate) {
            recreate();
            recreate = false;
        }
    }

    private void loadInfo(String s) {
//        balance = findViewById(R.id.balance);
//        average = findViewById(R.id.average);
//        past_benefits = findViewById(R.id.past_benefits);
//        past_spent = findViewById(R.id.past_spent);
//        android:text="You can spend $0.00 per meal until you receive your next benefits on the 5th"
        try {
            JSONObject jsonObject = new JSONObject(s);
            System.out.println(jsonObject);
            String temp;
            String extra;

            temp = jsonObject.getString("name");
            TextView anotherTemp = findViewById(R.id.state_text);
            anotherTemp.setText(temp);

            temp = jsonObject.getString("state_hotline");
            anotherTemp = findViewById(R.id.number);
            anotherTemp.setText(temp);

            Boolean btemp = jsonObject.getBoolean("eligibility");
            anotherTemp = findViewById(R.id.eligibility_text);
            if (btemp) {
                anotherTemp.setText("If your household qualifies for TANF or MOE, then your household is eligible for SNAP. You can still apply for SNAP if your household does not qualify for TANF or MOE.");
            } else {
                anotherTemp.setText("This state does not have a policy to allow households that qualify for TANF or MOE to be eligible for SNAP. All households have to apply.");
            }

            temp = jsonObject.getString("type");
            Launcher.benefitsType = temp;
            Launcher.editor.putString("benefits_type", temp);
            Launcher.editor.apply();
            anotherTemp = findViewById(R.id.type_text);
            int first_day = jsonObject.getInt("first_day");
            String f_day = Integer.toString(first_day);
            switch (f_day.substring(f_day.length() - 1)) {
                case "1":
                    f_day += "st";
                    break;
                case "2":
                    f_day += "nd";
                    break;
                case "3":
                    f_day += "rd";
                    break;
                default:
                    f_day += "th";
            }
            if (temp.equals("n")) {
                temp = "Distribution occurs on the " + f_day + " for everyone.";
                anotherTemp.setText(temp);
            } else {
                btemp = jsonObject.getBoolean("uniform");
                if (btemp) {
                    extra = "uniformly";
                } else {
                    extra = "un-uniformly";
                }
                int last_day = jsonObject.getInt("last_day");
                String l_day = Integer.toString(last_day);
                switch (l_day.substring(l_day.length() - 1)) {
                    case "1":
                        l_day += "st";
                        break;
                    case "2":
                        l_day += "nd";
                        break;
                    case "3":
                        l_day += "rd";
                        break;
                    default:
                        l_day += "th";
                }
                temp = "Distribution is determined by the " + Launcher.benefitsKey.get(temp) + ", and occurs " + extra + " from the " + f_day + " to the " + l_day + ".";
                anotherTemp.setText(temp);
            }
            anotherTemp = findViewById(R.id.sonly_number);
            JSONArray so_hotlines = jsonObject.getJSONArray("state_only_hotline");
            if (so_hotlines.length() == 0) {
                temp = "No state-only hotlines in this state.";
            } else {
                temp = so_hotlines.join("/n");
            }
            anotherTemp.setText(temp);

            anotherTemp = findViewById(R.id.disDays);
            JSONObject objectTemp = jsonObject.getJSONObject("benefits");
            ArrayList<Integer> days = new ArrayList<>();
            ArrayList<String> conditions;
            Iterator<String> questionMark = objectTemp.keys();
            while (questionMark.hasNext()) {
                String day = questionMark.next();
                int iday = Integer.parseInt(day);
                days.add(iday);
                String condition;
                conditions = new ArrayList<>();
                for (int i = 0; i < objectTemp.getJSONArray(day).length(); i++) {
                    condition = objectTemp.getJSONArray(day).getString(i);
                    if (condition != null) {
                        conditions.add(condition);
                    }
                }
                Launcher.userBenefits.put(day, conditions);
            }
            Collections.sort(days);
            StringBuilder sbtemp = new StringBuilder();
            sbtemp.append(Integer.toString(days.get(0)));
            for (int i = 1; i < days.size(); i++) {
                sbtemp.append(", " + Integer.toString(days.get(i)));
            }
            anotherTemp.setText(sbtemp.toString());
        } catch (JSONException e) {
            e.printStackTrace();
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
            progressBar.setVisibility(View.GONE);
            if (!s.isEmpty()) {
                messageCard.setVisibility(View.GONE);
                viewDis.setVisibility(View.VISIBLE);
                loadInfo(s);
            } else {
                messageText.setText("Could not fetch state information");
            }
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                System.out.println("start of try");
                System.out.println(Launcher.URL);
                URL url = new URL(Launcher.URL + "/stateInfo?state=" + state);
                System.out.println("made url");
                System.out.println(url);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                System.out.println("made connection");
                con.setRequestMethod("GET");
                System.out.println("set GET");
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);
                con.connect();
                System.out.println("connected");
                System.out.println("clickAction");
                StringBuilder sb = new StringBuilder();
                int responseCode = con.getResponseCode();
                System.out.println(responseCode);
                if (responseCode == 200) {
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
                e.printStackTrace();
                return "";
            }
        }
    }

}

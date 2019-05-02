package com.example.SNAPapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class DistributionDayForm extends AppCompatActivity {


    private TextView benefits;
    private EditText case_num;
    private EditText ssn_num;
    private EditText lname;
    private EditText birthday;
    private CardView case_card;
    private CardView ssn_card;
    private CardView lname_card;
    private CardView birthday_card;
    private TextView info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_distribution_day_form);
        benefits = findViewById(R.id.benefits_info);
        case_num = findViewById(R.id.scase_entry);
        ssn_num = findViewById(R.id.sssn_entry);
        lname = findViewById(R.id.last_name_entry);
        birthday = findViewById(R.id.birthday_entry);
        case_card = findViewById(R.id.case_number_card);
        ssn_card = findViewById(R.id.ssn_card);
        lname_card = findViewById(R.id.last_name_card);
        birthday_card = findViewById(R.id.birthday_card);
        info = findViewById(R.id.dis_info);
        String temp;
        switch (Launcher.benefitsType) {
            case "n":
                temp = "Your state distributes to everyone on the " + Launcher.benefitsDay;
                benefits.setText(temp);
                temp = "Your distribution day is the " + Launcher.benefitsDay;
                info.setVisibility(View.VISIBLE);
                info.setText(temp);
                break;
            case "c":
                temp = "Your state distributes benefits based off of the last digit " +
                        "of your case number, so please provide the last digit of your case number";
                benefits.setText(temp);
                case_card.setVisibility(View.VISIBLE);
                break;
            case "mc":
                temp = "Your state distributes benefits based off of the last two digits " +
                        "of your case number, so please provide the last two digits of your case number";
                benefits.setText(temp);
                case_card.setVisibility(View.VISIBLE);
                case_num.setHint("Last two digits of case number");
                break;
            case "e":
                temp = "Your state distributes benefits based off of the 8th and 9th digits " +
                        "of your case number, so please provide the 8th and 9th digits of your case number";
                benefits.setText(temp);
                case_card.setVisibility(View.VISIBLE);
                case_num.setHint("8th and 9th digits of case number");
                break;
            case "v":
                temp = "Your state distributes benefits based off of the 7th digit " +
                        "of your case number, so please provide the 7th digit of your case number";
                benefits.setText(temp);
                case_card.setVisibility(View.VISIBLE);
                case_num.setHint("7th digit of case number");
                break;
            case "d":
                temp = "Your state distributes benefits based off of " +
                        "of your birth day, so please provide the last digit of your birth day (Ex. 0 for 10, 20, or 30)";
                benefits.setText(temp);
                birthday_card.setVisibility(View.VISIBLE);
                birthday.setHint("Last digit of birth day");
                break;
            case "j":
                temp = "Your state distributes benefits based off of " +
                        "your birth month and last name, so please provide your birth month and the first letter of your last name";
                benefits.setText(temp);
                birthday.setVisibility(View.VISIBLE);
                birthday.setHint("Birth month");
                lname_card.setVisibility(View.VISIBLE);
                lname.setHint("First letter of last name");
                break;
            case "y":
                temp = "Your state distributes benefits based off of " +
                        "your birth year, so please provide the last digit of your birth year (Ex. 0 for 2000, 1990, 1980)";
                benefits.setText(temp);
                birthday.setVisibility(View.VISIBLE);
                birthday.setHint("Last digit of birth year");
                break;
            case "l":
                temp = "Your state distributes benefits based off of " +
                        "your last name, so please provide your last name";
                benefits.setText(temp);
                lname_card.setVisibility(View.VISIBLE);
//                lname.setHint("Mylastname");
                break;
            case "ml":
                temp = "Your state distributes benefits based off of " +
                        "your last name, so please provide your last name";
                benefits.setText(temp);
                lname_card.setVisibility(View.VISIBLE);
//                lname.setHint("Last");
                break;
            case "s":
                temp = "Your state distributes benefits based off of " +
                        "of the last digit of your SSN, so please provide the last digit of your SSN";
                benefits.setText(temp);
                ssn_card.setVisibility(View.VISIBLE);
                ssn_num.setHint("Last digit of SSN");
                break;
            case "ms":
                temp = "Your state distributes benefits based off of " +
                        "of the last two digits of your SSN, so please provide the last two digits of your SSN";
                benefits.setText(temp);
                ssn_card.setVisibility(View.VISIBLE);
                ssn_num.setHint("Last two digits of SSN");
                break;
        }
        final ImageButton back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back.setEnabled(false);
                finish();
            }
        });
        final Button application = findViewById(R.id.apply_online);
        application.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                application.setEnabled(false);
                startActivity(new Intent(getApplicationContext(), OnlineApplication.class));
                application.setEnabled(true);
            }
        });
        final Button makeAccount = findViewById(R.id.make_account);
        makeAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeAccount.setEnabled(false);
                Intent intent = new Intent(getApplicationContext(), LoginSignup.class);
                intent.putExtra("type", "signup");
                startActivity(intent);
                makeAccount.setEnabled(true);
            }
        });

        /*
                {
            put("c", "last digit of case number");
            put("mc", "last two digits of case number");
            put("e", "8th and 9th digits of case number");
            put("v", "7th digit of case number");
            put("d", "birthday");
            put("j", "birth month and last name");
            put("y", "birth year");
            put("l", "last name");
            put("ml", "last name");
            put("s", "last digit of ssn");
            put("ms", "last two digits of ssn");
        }};
         */
        final Button disDay = findViewById(R.id.getUserDistribution);
        disDay.setOnClickListener(new View.OnClickListener() {
            // I'm gonna do this later cause it's too much typing ............ fixme
            @Override
            public void onClick(View v) {
                disDay.setEnabled(false);
                String temp;
                int some_num;
                String day = "";
                Boolean gotDay = false;
                switch (Launcher.benefitsType) {
                    case "c":
                        temp = case_num.getText().toString().trim();
                        if (temp.isEmpty()) {
                            Toast toast = Toast.makeText(getApplicationContext(), "Input the last digit of your case number", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, 64);
                            toast.show();
                        } else {
                            try {
                                Integer.parseInt(temp.substring(temp.length() - 1)); // see if NumberFormatException arises
                                ArrayList<String> conditions;
                                for (HashMap.Entry<String, ArrayList<String>> entry : Launcher.userBenefits.entrySet()) {
                                    conditions = entry.getValue();
                                    for (String number : conditions) {
                                        if (number.equals(temp)) {
                                            day = entry.getKey();
                                            gotDay = true;
                                            break;
                                        }
                                    }
                                    if (gotDay) {
                                        switch (day) {
                                            case "1":
                                                day += "st";
                                                break;
                                            case "2":
                                                day += "nd";
                                                break;
                                            case "3":
                                                day += "rd";
                                                break;
                                            default:
                                                day += "th";
                                        }
                                        temp = "Your distribution day is the " + day;
                                        info.setVisibility(View.VISIBLE);
                                        info.setText(temp);
                                        break;
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        if (!gotDay) {
                            Toast toast = Toast.makeText(getApplicationContext(), "You need to have a valid number", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, 64);
                            toast.show();
                        }
                        break;

                        // placeholders fixme
                    case "mc":
                        temp = "Your state distributes benefits based off of the last two digits " +
                                "of your case number, so please provide the last two digits of your case number";
                        benefits.setText(temp);
                        case_card.setVisibility(View.VISIBLE);
                        case_num.setHint("##");
                        break;
                    case "e":
                        temp = "Your state distributes benefits based off of the 8th and 9th digits " +
                                "of your case number, so please provide the 8th and 9th digits of your case number";
                        benefits.setText(temp);
                        case_card.setVisibility(View.VISIBLE);
                        case_num.setHint("##");
                        break;
                    case "v":
                        temp = "Your state distributes benefits based off of the 7th digit " +
                                "of your case number, so please provide the 7th digit of your case number";
                        benefits.setText(temp);
                        case_card.setVisibility(View.VISIBLE);
                        break;
                    case "d":
                        temp = "Your state distributes benefits based off of " +
                                "of your birth day, so please provide the last digit of your birth day (Ex. 0 for 10, 20, or 30)";
                        benefits.setText(temp);
                        birthday_card.setVisibility(View.VISIBLE);
                        birthday.setHint("#");
                        break;
                    case "j":
                        temp = "Your state distributes benefits based off of " +
                                "your birth month and last name, so please provide your birth month and the first letter of your last name";
                        benefits.setText(temp);
                        birthday.setVisibility(View.VISIBLE);
                        birthday.setHint("##");
                        lname_card.setVisibility(View.VISIBLE);
                        lname.setHint("*");
                        break;
                    case "y":
                        temp = "Your state distributes benefits based off of " +
                                "your birth year, so please provide the last digit of your birth year (Ex. 0 for 2000, 1990, 1980)";
                        benefits.setText(temp);
                        birthday.setVisibility(View.VISIBLE);
                        birthday.setHint("#");
                        break;
                    case "l":
                        temp = "Your state distributes benefits based off of " +
                                "your last name, so please provide your last name";
                        benefits.setText(temp);
                        lname_card.setVisibility(View.VISIBLE);
                        lname.setHint("Mylastname");
                        break;
                    case "ml":
                        temp = "Your state distributes benefits based off of " +
                                "your last name, so please provide your last name";
                        benefits.setText(temp);
                        lname_card.setVisibility(View.VISIBLE);
                        lname.setHint("Mylastname");
                        break;
                    case "s":
                        temp = "Your state distributes benefits based off of " +
                                "of the last digit of your SSN, so please provide the last digit of your SSN";
                        benefits.setText(temp);
                        ssn_card.setVisibility(View.VISIBLE);
                        ssn_num.setHint("#");
                        break;
                    case "ms":
                        temp = "Your state distributes benefits based off of " +
                                "of the last two digits of your SSN, so please provide the last two digits of your SSN";
                        benefits.setText(temp);
                        ssn_card.setVisibility(View.VISIBLE);
                        ssn_num.setHint("##");
                        break;
                }
                disDay.setEnabled(true);
            }
        });
    }
}

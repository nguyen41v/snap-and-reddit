package com.example.SNAPapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

public class SignupTab extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public SignupTab() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static com.example.SNAPapp.SignupTab newInstance(int sectionNumber) {
            com.example.SNAPapp.SignupTab fragment = new com.example.SNAPapp.SignupTab();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.signup, container, false);

            return rootView;
        }
}

package com.example.testrun;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import android.os.Parcel;
import android.os.Parcelable;

public class PostItem implements Parcelable{
    private String title;
    private String content;
    private String date;
    private int num_comments;
    private String username;
    private String sub;
    private int p_number;
    Date postDate;

    public PostItem (String title, String content, String date, int num_comments, String username, String sub, int p_number) {
        this.title = title;
        this.content = content;
        this.date = date.substring(0,23);
        SimpleDateFormat serverFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        serverFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            this.postDate = serverFormat.parse(this.date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        this.num_comments = num_comments;
        this.username = username;
        this.sub = sub;
        this.p_number = p_number;
    }

    public String getTitle() {
        return this.title;
    }
    public String getContent() {
        return this.content;
    }
    public String getDateDifference() {
        // get time since post/comment info
        int year, month, day, hour, minute, second;
        long postime, curtime, timediff;
        postime = this.postDate.getTime();
        curtime = System.currentTimeMillis();
        timediff = curtime - postime;

        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        c.setTimeInMillis(timediff);
        year = c.get(Calendar.YEAR)-1970;
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH)-1;
        hour = c.get(Calendar.HOUR_OF_DAY);
        minute = c.get(Calendar.MINUTE);
        second = c.get(Calendar.SECOND);
        if (year > 0) {
            return year + "y";
        }
        if (month > 0) {
            return month + "mo";
        }
        if (day > 0) {
            return day + "d";
        }
        if (hour > 0) {
            return hour + "h";
        }
        if (minute > 0) {
            return minute + "m";
        }
        return second + "s";
    }
    public String getUsername() {
        return this.username;
    }
    public int getP_number() {
        return this.p_number;
    }

    public String getSub() {
        return sub;
    }

    public int getNum_comments() {
        return this.num_comments;
    }

    public PostItem(Parcel in) {
        String[] data = new String[5];
        in.readStringArray(data);
        this.title = data[0];
        this.content = data[1];
        this.date = data[2];
        this.username = data[3];
        this.sub = data[4];
        SimpleDateFormat serverFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        serverFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            this.postDate = serverFormat.parse(this.date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        this.num_comments = in.readInt();
        this.p_number = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] {this.title, this.content, this.date, this.username, this.sub});
        dest.writeInt(this.num_comments);
        dest.writeInt(this.p_number);
    }
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public PostItem createFromParcel(Parcel in) {
            return new PostItem(in);
        }

        public PostItem[] newArray(int size) {
            return new PostItem[size];
        }
    };

}

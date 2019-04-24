package com.example.reddit;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class CommentItem {
    private String content;
    private String date;
    private String username;
    private int commentLevel;
    private int number;
    private Boolean deleted;
    private Boolean edited;
    private Date postDate;
    private Date editDate;
    private ArrayList<CommentItem> replies = new ArrayList<>();

    public CommentItem(String content, String date, String username, int commentLevel, int number, Boolean deleted, Boolean edited) {
        this.content = content;
        this.date = date.substring(0,23);
        this.number = number;
        this.deleted = deleted;
        this.edited = edited;
        SimpleDateFormat serverFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        serverFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            postDate = serverFormat.parse(this.date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        this.username = username;
        this.commentLevel = commentLevel;
    }
    public int getCommentLevel() {
        return this.commentLevel;
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

    public ArrayList<CommentItem> getReplies() {
        return this.replies;
    }
    public void setReplies(CommentItem c) {
        this.replies.add(c);
    }
    public void setReplies(ArrayList<CommentItem> c) {
        this.replies = c;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public Boolean getEdited() {
        return edited;
    }

    public String getEditDifference() {
        // get time since post/comment info
        int year, month, day, hour, minute, second;
        long editTime, curtime, timediff;
        editTime = this.editDate.getTime();
        curtime = System.currentTimeMillis();
        timediff = curtime - editTime;
        if (timediff < 0) {
            return "0s";
        }
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
        return second + "s";    }

    public void setEditDate(String editDate) {
        SimpleDateFormat serverFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        serverFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            this.editDate = serverFormat.parse(editDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}


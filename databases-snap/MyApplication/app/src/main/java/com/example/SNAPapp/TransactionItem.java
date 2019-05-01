package com.example.SNAPapp;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class TransactionItem {
    private Date date;
    private String sdate;
    private ArrayList<IndividualTransaction> transactions = new ArrayList<>();

    TransactionItem() {

    }

    TransactionItem(String date, Boolean spend, BigDecimal amount, String description) {
        sdate = date;
        SimpleDateFormat serverFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        serverFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            this.date = serverFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        this.addTransaction(spend, amount, description);
    }

    public Date getDate() {
        return date;
    }

    public String getSdate() {
        return sdate;
    }

    public class IndividualTransaction {
        public Boolean spend;
        public BigDecimal amount;
        public String description = "";


        public IndividualTransaction (Boolean spend, BigDecimal amount, String description) {
            this.spend = spend;
            this.amount = amount;
            this.description = description;
        }
    }

    public void addTransaction(Boolean spend, BigDecimal amount, String description) {
        this.transactions.add(new IndividualTransaction(spend, amount, description));
    }

    public ArrayList<IndividualTransaction> getTransactions() {
        return this.transactions;
    }

}

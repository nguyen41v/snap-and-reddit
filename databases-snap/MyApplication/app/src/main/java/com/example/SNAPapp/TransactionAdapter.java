package com.example.SNAPapp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder>  {
    Context context;
    List<TransactionItem> transactionItems;
    ViewGroup parent;

    public TransactionAdapter(List<TransactionItem> transactionItems, Context context) {
        this.transactionItems = transactionItems;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.parent = parent;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.transactions_card, parent, false);
    return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String temp;
        TransactionItem transactionItem = transactionItems.get(position);
        temp = RecentTransactions.dateFormat.format(transactionItem.getDate());
        holder.date.setText(temp);
        for (TransactionItem.IndividualTransaction individualTransaction : transactionItem.getTransactions()) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.transaction, parent, false);
            TextView textView = v.findViewById(R.id.transactionName);
            temp = individualTransaction.description;
            textView.setText(temp);
            textView = v.findViewById(R.id.amountSpent);
            if (individualTransaction.spend) {
                temp = "-$" + individualTransaction.amount.toString();
                textView.setText(temp);
            } else {
                temp = "$" + individualTransaction.amount.toString();
                textView.setText(temp);
                textView.setTextColor(ContextCompat.getColor(context, R.color.green));
            }
            holder.linearLayout.addView(v);
        }
    }

    @Override
    public int getItemCount() {
        return this.transactionItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout linearLayout;
        public TextView date;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            linearLayout = itemView.findViewById(R.id.linear);
            date = itemView.findViewById(R.id.date);
        }
    }

}

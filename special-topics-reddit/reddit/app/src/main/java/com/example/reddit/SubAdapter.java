package com.example.reddit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class SubAdapter extends RecyclerView.Adapter<SubAdapter.ViewHolder> {
    private List<SubItem> subItems;
    private Context context;

    public SubAdapter(List<SubItem> subItems, Context context) {
        this.subItems = subItems;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.subs, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SubItem subItem = this.subItems.get(position);
        holder.subItem = subItem;
        String temp = "s/" + subItem.getSubname();
        holder.sub.setText(temp);
    }

    @Override
    public int getItemCount() {
        return this.subItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView sub;
        public SubItem subItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.sub = itemView.findViewById(R.id.sub);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.out.println("i've been clicked!!");
                    MakePost.sub.setText(sub.getText().toString());
                    MakePost.sub_name = subItem.getSubname();
                    ((Activity) context).finish();
                }
            });
        }
    }
}
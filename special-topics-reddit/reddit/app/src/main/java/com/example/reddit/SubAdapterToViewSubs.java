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

public class SubAdapterToViewSubs extends RecyclerView.Adapter<SubAdapterToViewSubs.ViewHolder> {
    private List<SubItem> subItems;
    private Context context;

    public SubAdapterToViewSubs(List<SubItem> postItems, Context context) {
        this.subItems = postItems;
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
            sub.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    context.startActivity(new Intent(context, ViewSub.class).putExtra("sub", subItem.getSubname()));
                }
            });
        }
    }
}
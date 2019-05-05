package com.example.reddit;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private List<UserItem> userItems;
    private Context context;

    public UserAdapter(List<UserItem> userItems, Context context) {
        this.userItems = userItems;
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
        UserItem userItem = this.userItems.get(position);
        holder.userItem = userItem;
        String temp = "u/" + userItem.getUsername();
        holder.sub.setText(temp);
    }

    @Override
    public int getItemCount() {
        return this.userItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView sub;
        public UserItem userItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.sub = itemView.findViewById(R.id.sub);
            sub.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    context.startActivity(new Intent(context, ViewSub.class).putExtra("sub", userItem.getUsername()));
                }
            });
        }
    }
}
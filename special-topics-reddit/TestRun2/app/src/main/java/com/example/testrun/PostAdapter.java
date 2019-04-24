package com.example.testrun;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private List<PostItem> postItems;
    private Context context;
    private ViewGroup parent;

    public PostAdapter(List<PostItem> postItems, Context context) {
        this.postItems = postItems;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.parent = parent;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_list, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PostItem postItem = this.postItems.get(position);
        holder.title.setText(postItem.getTitle());
        String temp = "s/" + postItem.getSub();
        holder.sub.setText(temp);
        temp = "Posted by u/" + postItem.getUsername() + " \t\t " + postItem.getDateDifference();
        holder.username.setText(temp);
        holder.num_comments.setText(Integer.toString(postItem.getNum_comments()));
//        holder.p_number = postItem.getP_number();
//        holder.card.setTag(holder.p_number);
        holder.post = postItem;
    }

    @Override
    public int getItemCount() {
        return this.postItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView sub;
        public TextView username;
        public TextView num_comments;
        public CardView card;
        public PostItem post;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.card = (CardView) itemView;
            this.title = itemView.findViewById(R.id.title);
            this.sub = itemView.findViewById(R.id.sub);
            this.username = itemView.findViewById(R.id.user);
            this.num_comments = itemView.findViewById(R.id.num_comments);

            card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.out.println("i've been clicked!!");
                    Intent intent = new Intent(context, ViewPost.class);
                    intent.putExtra("post", post);
                    intent.putExtra("username", MainActivity.username);
                    intent.putExtra("token", MainActivity.token);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            });
        }
    }
}

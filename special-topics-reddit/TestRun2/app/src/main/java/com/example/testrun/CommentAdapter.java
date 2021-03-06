package com.example.testrun;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder>  {
    List<CommentItem> commentItems;
    Context context;
    ViewGroup parent;
    PostItem post;


    public CommentAdapter(List<CommentItem> commentItems, Context context, PostItem post) {
        this.commentItems = commentItems;
        this.context = context;
        this.post = post;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_list, parent, false);
            this.parent = parent;
        return new ViewHolder(v);
    }

    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CommentItem commentItem = this.commentItems.get(position);
        String temp = "Posted by u/" + commentItem.getUsername() + " \t\t " + commentItem.getDateDifference();
        holder.username.setText(temp);
        if (commentItem.getDeleted()) {
            holder.content.setText("[deleted]");
        } else {
            holder.content.setText(commentItem.getContent());
        }
        if (commentItem.getEdited()) {
            temp = "Edited " + commentItem.getEditDifference() + " ago";
            holder.edited.setText(temp);
            holder.edited.setVisibility(View.VISIBLE);
        }
        if (commentItem.getUsername().equals(MainActivity.username)) {
            holder.settings.setVisibility(View.VISIBLE);
            holder.createSettingsMenu();
        }
        holder.comment = commentItem;
        addReply(1, holder, commentItem);
        holder.divider.addView(LayoutInflater.from(context).inflate(R.layout.vertical_divider, parent, false));
        holder.position = position;
    }

    public void addReply(int level, @NonNull ViewHolder holder, CommentItem commentItem) {
        String temp;
        int margins = 8;
        for (CommentItem reply : commentItem.getReplies()) {
            ViewHolder v = new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_list, parent, false));
            v.comment = reply;
            temp = "Posted by u/" + reply.getUsername() + " \t\t " + reply.getDateDifference();
            v.username.setText(temp);
            if (reply.getDeleted()) {
                v.content.setText("[deleted]");
            } else {
                v.content.setText(reply.getContent());
            }
            if (reply.getEdited()) {
                temp = "Edited " + reply.getEditDifference() + " ago";
                v.edited.setText(temp);
                v.edited.setVisibility(View.VISIBLE);
            }
            if (reply.getUsername().equals(MainActivity.username)) {
                v.settings.setVisibility(View.VISIBLE);
                v.createSettingsMenu();
            }
            View x4 = LayoutInflater.from(context).inflate(R.layout.horizontal_divider, parent, false);
            ViewGroup.MarginLayoutParams divider = (ViewGroup.MarginLayoutParams) x4.getLayoutParams();
            divider.setMargins(margins, 0, 0, 0);
            v.horizontal.addView(x4, 0);
            addReply(level + 1, v, reply);
            holder.vertical.addView(v.card);
        }
    }

    @Override
    public int getItemCount() {
        return this.commentItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView reactions;
        public TextView username;
        public TextView content;
        public CardView card;
        public LinearLayout vertical;
        public LinearLayout horizontal;
        public LinearLayout divider;
        public CardView reply;
        public CommentItem comment;
        public int position;
        public TextView edited;
        public ImageButton settings;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.card = (CardView) itemView;

            this.reactions = itemView.findViewById(R.id.reactions);
            this.username = itemView.findViewById(R.id.user);
            this.content = itemView.findViewById(R.id.content);
            this.vertical = itemView.findViewById(R.id.vertical);
            this.horizontal = itemView.findViewById(R.id.horizontal);
            this.divider = itemView.findViewById(R.id.dividers);
            this.reply = itemView.findViewById(R.id.reply);
            reply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    reply.setEnabled(false);
                    clickAction();
                }
            });
            this.edited = itemView.findViewById(R.id.edited);
            this.settings = itemView.findViewById(R.id.more_options);
        }

        public void createSettingsMenu() {
            final PopupMenu dropDownMenu = new PopupMenu(context, settings);
            final Menu menu = dropDownMenu.getMenu();
            dropDownMenu.getMenuInflater().inflate(R.menu.comment_settings, menu);
            dropDownMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if(item.getItemId() == R.id.edit){
                        ViewPost.recreate = true;
                        Intent intent = new Intent(context, Edit.class);
                        intent.putExtra("edit", "comment");
                        intent.putExtra("content", comment.getContent());
                        intent.putExtra("number", comment.getNumber());
                        intent.putExtra("p_number", post.getP_number());
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                        return true;
                    } else if (item.getItemId() == R.id.delete) {
                        // have to also send info to server...
                        if (comment.getDeleted()) {
                            comment.setDeleted(false);
                            content.setText(comment.getContent());
                        } else {
                            comment.setDeleted(true);
                            content.setText("[deleted]");
                            item.setTitle("Undelete");
                        }
                        return true;
                    }
                    return false;
                }
            });
            settings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dropDownMenu.show();
                }
            });
        }


        void clickAction() {
            System.out.println("I've been clicked??");
            ViewPost.commentItem = comment;
            ViewPost.reply = reply;
            ViewPost.card = true;
            ValidateToken validateToken = new ValidateToken();
            validateToken.execute();
        }


        class ValidateToken extends AsyncTask<Void, Void, Boolean> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(Boolean s) {
                super.onPostExecute(s);
                Intent intent;
                if (!s) {
                    intent = new Intent(context, LogIn.class);
                    ((Activity) parent.getContext()).startActivityForResult(intent, 1);
                } else {
                    ViewPost.recreate = true;
                    intent = new Intent(context, Reply.class);
                    intent.putExtra("reply_to", "comment");
                    intent.putExtra("content", comment.getContent());
                    intent.putExtra("username", comment.getUsername());
                    intent.putExtra("time", comment.getDateDifference());
                    intent.putExtra("number", comment.getNumber());
                    intent.putExtra("p_number", post.getP_number());
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    System.out.println(MainActivity.loggedIn);
                    if (!MainActivity.loggedIn) {
                        return false;
                    }
                    URL url = new URL(MainActivity.URL + "/validate?username=" + MainActivity.username + "&token=" + URLEncoder.encode(MainActivity.token, "UTF-8"));
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    con.connect();
                    con.setConnectTimeout(5000);
                    con.setReadTimeout(5000);
                    int responseCode = con.getResponseCode();
                    System.out.println(responseCode);
                    if (responseCode == 200) {
                        return true;
                    }
                } catch (Exception e) {
                    Log.e("Exception", "Sad life");
                    e.printStackTrace();
                }
                return false;
            }
        }
    }
}

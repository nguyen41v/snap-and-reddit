package com.example.reddit;

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

import org.json.JSONObject;

import java.io.OutputStreamWriter;
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
        holder.comment = commentItem;

        String temp = "Posted by u/" + commentItem.getUsername() + " \t\t " + commentItem.getDateDifference();
        holder.username.setText(temp);
        if (commentItem.getUsername().equals(MainActivity.username)) {
            holder.settings.setVisibility(View.VISIBLE);
            holder.createSettingsMenu();
        } else {
            if (commentItem.getDeleted()) {
                holder.content.setText("[deleted]");
            } else {
                holder.content.setText(commentItem.getContent());
            }
        }
        if (commentItem.getEdited()) {
            temp = "Edited " + commentItem.getEditDifference() + " ago";
            holder.edited.setText(temp);
            holder.edited.setVisibility(View.VISIBLE);
        }

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
            if (reply.getUsername().equals(MainActivity.username)) {
                v.settings.setVisibility(View.VISIBLE);
                v.createSettingsMenu();
            } else {
                if (reply.getDeleted()) {
                    v.content.setText("[deleted]");
                } else {
                    v.content.setText(reply.getContent());
                }
            }
            if (reply.getEdited()) {
                temp = "Edited " + reply.getEditDifference() + " ago";
                v.edited.setText(temp);
                v.edited.setVisibility(View.VISIBLE);
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
        public MenuItem delete;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.card = (CardView) itemView;

            this.reactions = itemView.findViewById(R.id.reactions);
            this.username = itemView.findViewById(R.id.user);
            this.content = itemView.findViewById(R.id.sub);
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

        private void createView() {
            String temp = "Posted by u/" + comment.getUsername() + " \t\t " + comment.getDateDifference();
            username.setText(temp);
            if (comment.getUsername().equals(MainActivity.username)) {
                settings.setVisibility(View.VISIBLE);
                createSettingsMenu();

            } else {
                if (comment.getDeleted()) {
                    content.setText("[deleted]");
                } else {
                    content.setText(comment.getContent());
                }
            }
            if (comment.getEdited()) {
                temp = "Edited " + comment.getEditDifference() + " ago";
                edited.setText(temp);
                edited.setVisibility(View.VISIBLE);
            }

        }

        public void createSettingsMenu() {
            final PopupMenu dropDownMenu = new PopupMenu(context, settings);
            final Menu menu = dropDownMenu.getMenu();
            dropDownMenu.getMenuInflater().inflate(R.menu.comment_settings, menu);
            if (comment.getDeleted()) {
                content.setText("[deleted]");
                for (int menuItemIndex = 0; menuItemIndex < menu.size(); menuItemIndex++) {
                    MenuItem menuItem = menu.getItem(menuItemIndex);
                    if (menuItem.getItemId() == R.id.delete) {
                        delete = menuItem;
                        menuItem.setTitle("Undelete");
                    }
                }
            } else {
                content.setText(comment.getContent());
            }
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
                        delete = item;
                        Delete delete = new Delete();
                        delete.execute();
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
                    con.setConnectTimeout(5000);
                    con.setReadTimeout(5000);
                    con.connect();
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

        class Delete extends AsyncTask<Void, Void, Boolean> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(Boolean s) {
                super.onPostExecute(s);
                if (s) {
                    ViewPost.changed = true;
                    ViewPost.recreate = true;
                    comment.setDeleted(!comment.getDeleted());
                    if (comment.getDeleted()) {
                        content.setText("[deleted]");
                        delete.setTitle("Undelete");
                    } else {
                        content.setText(comment.getContent());
                        delete.setTitle("Delete");
                    }
                }
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    System.out.println(MainActivity.loggedIn);
                    if (!MainActivity.loggedIn) {
                        return false;
                    }
                    URL url = new URL(MainActivity.URL + "/comment");
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("DELETE");

                    JSONObject requestBody = new JSONObject();
                    requestBody.put("username", MainActivity.username);
                    requestBody.put("token", MainActivity.token);
                    requestBody.put("p_number", post.getP_number());
                    requestBody.put("deleted", !comment.getDeleted());
                    requestBody.put("number", comment.getNumber());
                    System.out.println(requestBody.getString("username"));
                    System.out.println(requestBody.getString("token"));
                    System.out.println(requestBody.getString("p_number"));
                    System.out.println(requestBody.getString("deleted"));
                    System.out.println(requestBody.getString("number"));

                    con.setDoOutput(true);
                    con.setRequestProperty("Content-Type","application/json");
                    OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
                    writer.write(requestBody.toString());
                    writer.flush();
                    con.connect();
                    con.setConnectTimeout(5000);
                    con.setReadTimeout(5000);
                    int responseCode = con.getResponseCode();
                    System.out.println(responseCode);
                    if (responseCode == MainActivity.OK) {
                        return true;
                    } else if (responseCode == MainActivity.UNAUTHORIZED) {
                        MainActivity.loggedIn = false;
                        Intent intent = new Intent(context, LogIn.class);
                        context.startActivity(intent);
                    }
                } catch (Exception e) {
                    Log.e("Exception", "Sad life");
                    e.printStackTrace();
                }
                return null;
            }
        }
    }
}

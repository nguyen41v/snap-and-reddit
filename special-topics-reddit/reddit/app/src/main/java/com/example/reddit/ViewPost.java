package com.example.reddit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

// fixme need to update post every refresh as well

public class ViewPost extends AppCompatActivity {

    private PostItem post;
    private List<CommentItem> commentItems;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView content;
    private TextView numC;
    private Button replyButton; // keep track of button to reset after click
    public static CardView reply;   // keep track of which card to reset after click
                                    // can be a reply card in comments

    public static CommentItem commentItem;  // null if replying to post
                                            // using it to send comment info for comment reply
                                            // and to determine whether a user is sending a comment or post reply
    public static Boolean card = false; // true if clicked on a card object,
                                        // false if clicked on button -> enableclick of item clicked
    public static Boolean recreate = false; // true if went to reply; clear commentItem for future purposes
    public static Boolean changed = false; // true if change occurred (post, delete, edit) -> update page

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_post);
        makePost();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new CommentAdapter(new ArrayList<CommentItem>(), this, post));
        commentItems = new ArrayList<>();

        findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        replyButton = findViewById(R.id.replyButton);
        replyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replyButton.setEnabled(false);
                card = false;
                ValidateToken validateToken = new ValidateToken();
                validateToken.execute();
            }
        });

        swipeRefreshLayout = findViewById(R.id.swipeContainer);
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        update();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
        );

        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if(swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(true);
                }
                update();
            }
        });

        GetData getData = new GetData();
        getData.execute();


    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("resuming");
        System.out.println(card);
        System.out.println(recreate);
        System.out.println(changed);

        if (card) {
            reply.setEnabled(true);
        } else {
            replyButton.setEnabled(true);
        }
        if (recreate) {
            commentItem = null;
            if (changed) {
                update();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        System.out.println("clickAction?");
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("starting reply?");
        System.out.println(MainActivity.loggedIn);
        if (MainActivity.loggedIn) {
            update();
            if (commentItem != null) {
                replyToPost();
            } else {
                replyToComment();
            }
        }
    }

    private void replyToPost() {
        recreate = true;
        Intent intent = new Intent(ViewPost.this, Reply.class);
        intent.putExtra("reply_to", "post");
        intent.putExtra("content", post.getTitle());
        intent.putExtra("extra_content", post.getContent());
        intent.putExtra("p_number", post.getP_number());
        startActivity(intent);
    }

    private void replyToComment() {
        recreate = true;
        Intent intent = new Intent(ViewPost.this, Reply.class);
        intent.putExtra("reply_to", "comment");
        intent.putExtra("content", commentItem.getContent());
        intent.putExtra("username", commentItem.getUsername());
        intent.putExtra("time", commentItem.getDateDifference());
        intent.putExtra("number", commentItem.getNumber());
        intent.putExtra("p_number", post.getP_number());
        startActivity(intent);
    }

    private void update() {
        GetData getData = new GetData();
        getData.execute();
    }

    private void makePost() {
        // Load the post while getting data; update post when data obtained if necessary
        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        post = (PostItem) intent.getExtras().getParcelable("post");
        TextView sub = findViewById(R.id.sub);
        String temp = "s/" + post.getSub();
        sub.setText(temp);
        System.out.print(temp);
        TextView user = findViewById(R.id.user);
        temp = "Posted by u/" + post.getUsername() + " \t\t " + post.getDateDifference();
        user.setText(temp);
        TextView title = findViewById(R.id.title);
        title.setText(post.getTitle());
        content = findViewById(R.id.content);
        content.setText(post.getContent());
        numC = findViewById(R.id.reactionsAndComments);
        numC.setText(Integer.toString(post.getNum_comments()));
        final CardView replyCard = findViewById(R.id.reply);
        replyCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replyCard.setEnabled(false);
                card = true;
                reply = replyCard;
                System.out.println("I've been clicked??");
                ValidateToken validateToken = new ValidateToken();
                validateToken.execute();
            }
        });
    }


    private void loadIntoRecyclerView(String json) {
        try {
            commentItems = new ArrayList<>();
            JSONArray jsonArray = new JSONArray(json);
            content.setText(jsonArray.getJSONObject(0).getString("content"));
            numC.setText(Integer.toString(jsonArray.getJSONObject(0).getInt("num_of_comments")));
            if (jsonArray.length() == 1) {
                TextView noComments = (TextView) findViewById(R.id.noComments);
                noComments.setVisibility(View.VISIBLE);
            } else {
                for (int i = 1; i < jsonArray.length(); i++) {
                    JSONObject comment = jsonArray.getJSONObject(i);
                    System.out.println(comment.getInt("number"));
                    CommentItem commentItem = new CommentItem(
                            comment.getString("content"),
                            comment.getString("date"),
                            comment.getString("username"),
                            comment.getJSONArray("replies").length(),
                            comment.getInt("number"),
                            comment.getBoolean("deleted"),
                            comment.getBoolean("edited"));
                    if (commentItem.getEdited()) {
                        commentItem.setEditDate(comment.getString("edit_date"));
                    }
                    commentItems.add(commentItem);
                    commentItem.setReplies(getReplies(comment.getJSONArray("replies")));
                }
                System.out.println("done getting data");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        adapter = new CommentAdapter(commentItems, getApplicationContext(), post);
        System.out.println("done making adapter");
        recyclerView.setAdapter(adapter);
        System.out.println("set adapted");
    }

    private ArrayList<CommentItem> getReplies(JSONArray jsonArray) {
        ArrayList<CommentItem> replies = new ArrayList<>();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject comment = jsonArray.getJSONObject(i);
                System.out.println(comment.getInt("number"));

                CommentItem commentItem = new CommentItem(
                        comment.getString("content"),
                        comment.getString("date"),
                        comment.getString("username"),
                        comment.getJSONArray("replies").length(),
                        comment.getInt("number"),
                        comment.getBoolean("deleted"),
                        comment.getBoolean("edited"));
                if (commentItem.getEdited()) {
                    commentItem.setEditDate(comment.getString("edit_date"));
                }
                replies.add(commentItem);
                commentItem.setReplies(getReplies(comment.getJSONArray("replies")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return replies;
    }

    class GetData extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            loadIntoRecyclerView(s);
            swipeRefreshLayout.setRefreshing(false);
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                System.out.println("start of try");
                URL url = new URL(MainActivity.URL + "/comment?p_number="  + post.getP_number());
                System.out.println("made url");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                System.out.println("made connection");
                con.setRequestMethod("GET");
                System.out.println("set GET");
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);
                con.connect();
                System.out.println("connected");

                StringBuilder sb = new StringBuilder();
                int responseCode = con.getResponseCode();
                System.out.println(responseCode);
                if (responseCode == MainActivity.OK) {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String json;
                    System.out.println("got data");
                    while ((json = bufferedReader.readLine()) != null) {
                        sb.append(json + "\n");
                        System.out.print(json);
                    }
                    return sb.toString().trim();
                }
            } catch (Exception e) {
                Log.e("Exception", "Sad life");
                e.printStackTrace();
            }
        return null;
        }
    }

    class ValidateToken extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean s) {
            super.onPostExecute(s);
            System.out.println(s);
            if (!s) {
                startActivityForResult(new Intent(ViewPost.this, LogIn.class),1);
            } else {
                replyToPost();
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

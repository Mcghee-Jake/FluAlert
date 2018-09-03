package com.example.jmcghee.flualert;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.jmcghee.flualert.data.FluTweet;

import java.util.List;

public class AdapterFluTweets extends RecyclerView.Adapter<AdapterFluTweets.FluTweetViewHolder>{

    private List<FluTweet> fluTweets;


    public AdapterFluTweets(List<FluTweet> fluTweets) {
        this.fluTweets = fluTweets;
    }

    @NonNull
    @Override
    public FluTweetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.viewholder_flutweet;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        FluTweetViewHolder fluTweetViewHolder = new FluTweetViewHolder(view);

        return fluTweetViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull FluTweetViewHolder holder, int position) {
        holder.tvUsername.setText(fluTweets.get(position).getUsername());
        holder.tvTweetText.setText(fluTweets.get(position).getTweetText());
    }

    @Override
    public int getItemCount() {
        return fluTweets.size();
    }

    public class FluTweetViewHolder extends RecyclerView.ViewHolder {

        private TextView tvUsername, tvTweetText;

        public FluTweetViewHolder(View itemView) {
            super(itemView);

            tvUsername = itemView.findViewById(R.id.tv_username);
            tvTweetText = itemView.findViewById(R.id.tv_tweet_text);
        }


    }
}

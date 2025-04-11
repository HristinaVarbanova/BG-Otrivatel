package com.example.loginscreen;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> {
    private List<String> friendsList;

    public FriendsAdapter(List<String> friendsList) {
        this.friendsList = friendsList;
    }

    @Override
    public FriendViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend, parent, false);
        return new FriendViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(FriendViewHolder holder, int position) {
        String friendEmail = friendsList.get(position);
        holder.friendEmailTextView.setText(friendEmail);
    }

    @Override
    public int getItemCount() {
        return friendsList.size();
    }

    public class FriendViewHolder extends RecyclerView.ViewHolder {
        public TextView friendEmailTextView;

        public FriendViewHolder(View itemView) {
            super(itemView);
            friendEmailTextView = itemView.findViewById(R.id.friendEmailTextView);
        }
    }
}

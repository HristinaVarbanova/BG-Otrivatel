package com.example.loginscreen.View.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.loginscreen.Model.FriendInfo;
import com.example.loginscreen.R;

import java.util.List;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> {

    private final List<FriendInfo> friends;

    public FriendsAdapter(List<FriendInfo> friends) {
        this.friends = friends;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_item, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        FriendInfo friend = friends.get(position);
        holder.username.setText(friend.getUsername());
        holder.stars.setText("\u2B50 " + friend.getStars());
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    public static class FriendViewHolder extends RecyclerView.ViewHolder {
        TextView username, stars;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.friendUsername);
            stars = itemView.findViewById(R.id.friendStars);
        }
    }
}

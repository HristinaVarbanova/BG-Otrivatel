package com.example.loginscreen.View.Adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.loginscreen.Model.ImageData;
import com.example.loginscreen.R;
import com.example.loginscreen.View.FullscreenImageActivity;

import java.util.List;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder> {

    private final List<ImageData> imageDataList;

    public GalleryAdapter(List<ImageData> imageDataList) {
        this.imageDataList = imageDataList;
    }

    @NonNull
    @Override
    public GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.gallery_item, parent, false);
        return new GalleryViewHolder(view);
    }
    public interface OnImageLongClickListener {
        void onImageLongClick(int position, String imageUrl);
    }

    private OnImageLongClickListener longClickListener;

    public void setOnImageLongClickListener(OnImageLongClickListener listener) {
        this.longClickListener = listener;
    }


    @Override
    public void onBindViewHolder(@NonNull GalleryViewHolder holder, int position) {
        ImageData imageData = imageDataList.get(position);
        Context context = holder.itemView.getContext();

        Glide.with(context)
                .load(imageData.getImageUrl())
                .placeholder(R.drawable.placeholder)
                .into(holder.imageView);

        holder.imageView.setOnClickListener(v -> {
            Intent intent = new Intent(context, FullscreenImageActivity.class);
            intent.putExtra("imageUrl", imageData.getImageUrl());
            context.startActivity(intent);
        });

        holder.imageView.setOnLongClickListener(v -> {
            Log.d("GalleryAdapter", "Long click detected!");

            if (longClickListener != null && imageData.getImageUrl() != null) {
                longClickListener.onImageLongClick(holder.getAdapterPosition(), imageData.getImageUrl());
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return imageDataList.size();
    }

    public static class GalleryViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public GalleryViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}

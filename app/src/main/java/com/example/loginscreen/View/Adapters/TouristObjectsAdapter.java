    package com.example.loginscreen.View.Adapters;

    import android.content.Context;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.ImageView;
    import android.widget.TextView;
    import android.widget.Toast;

    import androidx.annotation.NonNull;
    import androidx.recyclerview.widget.RecyclerView;

    import com.example.loginscreen.Dialogs.Options;
    import com.example.loginscreen.R;
    import com.example.loginscreen.Model.TouristObject;
    import com.example.loginscreen.View.BeenThere;
    import com.example.loginscreen.View.Wishlist;
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.firestore.FirebaseFirestore;

    import java.util.List;

    public class TouristObjectsAdapter extends RecyclerView.Adapter<TouristObjectsAdapter.TouristViewHolder> {
        private List<TouristObject> touristObjectsList;
        private Context context;
        private OnItemClickListener onItemClickListener;
        private FirebaseFirestore firestore;

        public interface OnItemClickListener {
            void onItemClick(TouristObject touristObject);
        }

        public TouristObjectsAdapter(Context context, List<TouristObject> touristObjectsList, OnItemClickListener onItemClickListener) {
            this.context = context;
            this.touristObjectsList = touristObjectsList;
            this.onItemClickListener = onItemClickListener;
            this.firestore = FirebaseFirestore.getInstance();
        }

        @NonNull
        @Override
        public TouristViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.place_item, parent, false);
            return new TouristViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TouristViewHolder holder, int position) {
            TouristObject touristObject = touristObjectsList.get(position);
            holder.objectName.setText(touristObject.getName());
            int imageResId = context.getResources().getIdentifier(touristObject.getImage(), "drawable", context.getPackageName());
            if (imageResId != 0) {
                holder.objectImage.setImageResource(imageResId);
            } else {
                holder.objectImage.setImageResource(R.drawable.default_image);
            }
            holder.itemView.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(touristObject);
                }
            });
            holder.itemView.setOnLongClickListener(v -> {
                if (context instanceof Wishlist) {
                    Options optionsDialog = Options.newInstance(touristObject);
                    optionsDialog.show(((Wishlist) context).getSupportFragmentManager(), "Options");
                } else if (context instanceof BeenThere) {
                    ((BeenThere) context).showDeleteDialog(touristObject);
                }
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return touristObjectsList.size();
        }

        public static class TouristViewHolder extends RecyclerView.ViewHolder {
            TextView objectName;
            ImageView objectImage;

            public TouristViewHolder(View itemView) {
                super(itemView);
                objectName = itemView.findViewById(R.id.objectName);
                objectImage = itemView.findViewById(R.id.objectImage);
            }
        }

        public void updateList(List<TouristObject> newList) {
            touristObjectsList = newList;
            notifyDataSetChanged();
        }


    }

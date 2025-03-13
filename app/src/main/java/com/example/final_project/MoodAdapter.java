package com.example.final_project;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.carousel.MaskableFrameLayout;

import java.util.List;

public class MoodAdapter extends RecyclerView.Adapter<MoodAdapter.MoodViewHolder> {

    private List<String> moodList;
    private List<Integer> moodImages;
    private OnMoodClickListener onMoodClickListener;
    private int selectedPosition = -1; // Keep track of the selected position

    public MoodAdapter(List<String> moodList, List<Integer> moodImages, OnMoodClickListener listener) {
        this.moodList = moodList;
        this.moodImages = moodImages;
        this.onMoodClickListener = listener;
    }

    @Override
    public MoodViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mood, parent, false);
        return new MoodViewHolder(view, onMoodClickListener);
    }

    @Override
    public void onBindViewHolder(MoodViewHolder holder, int position) {
        String mood = moodList.get(position);
        Integer imageResId = moodImages.get(position);
        holder.bind(mood, imageResId);

        // Change the color of the selected card
        if (position == selectedPosition) {
            holder.cardView.setCardBackgroundColor(Color.parseColor("#EEE2BC"));
        } else {
            holder.cardView.setCardBackgroundColor(Color.parseColor("#F4EDDF"));
        }
    }

    @Override
    public int getItemCount() {
        return moodList.size();
    }

    public interface OnMoodClickListener {
        void onMoodClick(String mood, int position); // Pass the position to the listener
    }

    public class MoodViewHolder extends RecyclerView.ViewHolder {

        private TextView moodTextView;
        private ImageView moodImageView;
        private CardView cardView; // Declare cardView
        private OnMoodClickListener onMoodClickListener;

        public MoodViewHolder(View itemView, OnMoodClickListener listener) {
            super(itemView);
            moodTextView = itemView.findViewById(R.id.mood_text);
            moodImageView = itemView.findViewById(R.id.mood_image);
            // Access the CardView directly from the layout inside MaskableFrameLayout
            MaskableFrameLayout maskableFrameLayout = itemView.findViewById(R.id.maskable_frame_layout);
            this.cardView = maskableFrameLayout.findViewById(R.id.cardView);  // Get CardView inside MaskableFrameLayout
            this.onMoodClickListener = listener;

            // Set up the click listener for each item
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    selectedPosition = position; // Store the selected position
                    onMoodClickListener.onMoodClick(moodTextView.getText().toString(), position);
                    notifyDataSetChanged(); // Refresh the card to show the selected color
                }
            });
        }

        public void bind(String mood, Integer imageResId) {
            moodTextView.setText(mood);
            moodImageView.setImageResource(imageResId);
        }

    }

    public void selectMoodByName(String moodName) {
        // Find the position of the mood by its name
        int position = moodList.indexOf(moodName);
        if (position != -1) {
            selectedPosition = position;
            notifyDataSetChanged(); // Refresh the RecyclerView to reflect the selected mood
        }
    }
}

package com.example.final_project;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MemoryAdapter extends RecyclerView.Adapter<MemoryAdapter.MemoryViewHolder> {

    private List<MoodEntryClass> moodList;

    public MemoryAdapter(List<MoodEntryClass> moodList) {
        this.moodList = moodList;
    }

    @NonNull
    @Override
    public MemoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_memory, parent, false);
        return new MemoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemoryViewHolder holder, int position) {
        MoodEntryClass mood = moodList.get(position);

        // Time stamp to date
        String formattedDate = formatDate(mood.getTimestamp());
        holder.textViewDate.setText(formattedDate);

        // Note
        holder.textViewNote.setText(mood.getNote());

        // Mood
        switch (mood.getMood().toLowerCase()) {
            case "happy":
                holder.imageViewMood.setImageResource(R.drawable.happy_face);
                break;
            case "calm":
                holder.imageViewMood.setImageResource(R.drawable.calm_face);
                break;
            case "bored":
                holder.imageViewMood.setImageResource(R.drawable.bored_face);
                break;
            case "sad":
                holder.imageViewMood.setImageResource(R.drawable.sad_face);
                break;
            case "stressed":
                holder.imageViewMood.setImageResource(R.drawable.stressed_face);
                break;
            default:
                break;
        }

        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, MoodSelectionActivity.class);
            intent.putExtra("SELECTED_MOOD", mood.getMood());
            intent.putExtra("SELECTED_DATE", mood.getTimestamp());
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return moodList.size();
    }

    public static class MemoryViewHolder extends RecyclerView.ViewHolder {
        TextView textViewDate, textViewNote;
        ImageView imageViewMood;

        public MemoryViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewNote = itemView.findViewById(R.id.textViewNote);
            imageViewMood = itemView.findViewById(R.id.imageViewMood);
        }
    }

    private String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}

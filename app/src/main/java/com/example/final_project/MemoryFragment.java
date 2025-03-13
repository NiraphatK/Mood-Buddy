package com.example.final_project;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MemoryFragment extends Fragment {
    private RecyclerView recyclerView;
    private MemoryAdapter adapter;
    private List<MoodEntryClass> moodList;
    private DatabaseReference databaseReference;

    public MemoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_memory, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewMemories);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        moodList = new ArrayList<>();
        adapter = new MemoryAdapter(moodList);
        recyclerView.setAdapter(adapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        fetchMoodEntries();

        return view;
    }

    private void fetchMoodEntries() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                moodList.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot moodSnapshot : userSnapshot.child("moodEntries").getChildren()) {
                        MoodEntryClass mood = moodSnapshot.getValue(MoodEntryClass.class);
                        if (mood != null) {
                            moodList.add(mood);
                        }
                    }
                }
                // Sort the list in descending order (newest first)
                Collections.sort(moodList, (m1, m2) -> Long.compare(m2.getTimestamp(), m1.getTimestamp()));

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
                Toast.makeText(getContext(), "Failed to load moods.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

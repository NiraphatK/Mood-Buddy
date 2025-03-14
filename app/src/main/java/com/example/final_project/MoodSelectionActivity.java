package com.example.final_project;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MoodSelectionActivity extends AppCompatActivity {

    private static final String SELECTED_MOOD_KEY = "SELECTED_MOOD";
    private static final String SELECTED_DATE_KEY = "SELECTED_DATE";

    private RecyclerView moodCarousel;
    private EditText noteEditText;
    private Button saveButton, deleteButton;
    private String selectedMood;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference usersRef;
    private String userId;
    private long selectedDateMillis;
    private TextView dateMoodSelectedTextView;
    private ImageView closeIconMoodSelection;

    private String existingMood;
    private String existingNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_selection);

        selectedMood = getIntent().getStringExtra(SELECTED_MOOD_KEY);
        selectedDateMillis = getIntent().getLongExtra(SELECTED_DATE_KEY, System.currentTimeMillis());

        moodCarousel = findViewById(R.id.moodCarousel);
        noteEditText = findViewById(R.id.noteEditText);
        saveButton = findViewById(R.id.saveButton);
        deleteButton = findViewById(R.id.deleteButton);
        dateMoodSelectedTextView = findViewById(R.id.dateMoodSelectedTextView);
        closeIconMoodSelection = findViewById(R.id.closeIconMoodSelection);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");
        userId = mAuth.getCurrentUser().getUid();


        // close Icon
        closeIconMoodSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        saveButton.setEnabled(false);
        setUpDateDisplay();

        checkIfMoodExists();

        List<String> moods = Arrays.asList("Happy", "Calm", "Bored", "Sad", "Stressed");
        List<Integer> moodImages = Arrays.asList(R.drawable.happy_face, R.drawable.calm_face, R.drawable.bored_face, R.drawable.sad_face, R.drawable.stressed_face);

        MoodAdapter adapter = new MoodAdapter(moods, moodImages, (mood, position) -> {
            selectedMood = mood;
            checkForChangesAndEnableSave();
        });

        moodCarousel.setAdapter(adapter);

        if (selectedMood != null) {
            ((MoodAdapter) moodCarousel.getAdapter()).selectMoodByName(selectedMood);
            checkForChangesAndEnableSave();
        }

        noteEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                checkForChangesAndEnableSave();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        saveButton.setOnClickListener(v -> saveMoodAndNote());
        deleteButton.setOnClickListener(v -> deleteMoodEntry());
    }

    private void setUpDateDisplay() {
        if (selectedDateMillis != -1) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
            String formattedDate = sdf.format(new Date(selectedDateMillis));
            dateMoodSelectedTextView.setText(formattedDate);
        } else {
            dateMoodSelectedTextView.setText("No date selected.");
        }
    }

    private void checkIfMoodExists() {
        usersRef.child(userId).child("moodEntries").child(String.valueOf(selectedDateMillis)).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    deleteButton.setEnabled(true);
                    MoodEntryClass existingMoodEntry = task.getResult().getValue(MoodEntryClass.class);
                    if (existingMoodEntry != null) {
                        existingMood = existingMoodEntry.getMood();
                        existingNote = existingMoodEntry.getNote();
                        selectedMood = existingMood;
                        noteEditText.setText(existingNote);
                        if (moodCarousel.getAdapter() != null) {
                            ((MoodAdapter) moodCarousel.getAdapter()).selectMoodByName(existingMood);
                        }
                        checkForChangesAndEnableSave();
                    }
                } else {
                    deleteButton.setEnabled(false);
                    if (selectedMood != null && moodCarousel.getAdapter() != null) {
                        ((MoodAdapter) moodCarousel.getAdapter()).selectMoodByName(selectedMood);
                    }
                }
            } else {
                Toast.makeText(MoodSelectionActivity.this, "Error checking data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkForChangesAndEnableSave() {
        String currentNote = noteEditText.getText().toString();
        if ((selectedMood != null && !selectedMood.equals(existingMood)) || !currentNote.equals(existingNote)) {
            saveButton.setEnabled(true);
        } else {
            saveButton.setEnabled(false);
        }
    }

    private void saveMoodAndNote() {
        if (selectedMood == null) {
            Toast.makeText(this, "Please select a mood.", Toast.LENGTH_SHORT).show();
            return;
        }

        String note = noteEditText.getText().toString();
        if (selectedDateMillis == -1) {
            Toast.makeText(this, "Invalid date selected.", Toast.LENGTH_SHORT).show();
            return;
        }

        MoodEntryClass moodEntry = new MoodEntryClass(selectedMood, note, selectedDateMillis);

        usersRef.child(userId).child("moodEntries").child(String.valueOf(selectedDateMillis)).setValue(moodEntry).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Mood and note saved successfully!", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error saving data.", Toast.LENGTH_SHORT).show();
        });
    }

    private void deleteMoodEntry() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this).setTitle("Delete Mood").setMessage("Are you sure you want to delete this mood?").setNeutralButton("Cancel", (d, which) -> d.dismiss()).setNegativeButton("Delete", (d, which) -> {
            usersRef.child(userId).child("moodEntries").child(String.valueOf(selectedDateMillis)).removeValue().addOnSuccessListener(aVoid -> {
                Toast.makeText(MoodSelectionActivity.this, "Mood entry deleted successfully!", Toast.LENGTH_SHORT).show();
                deleteButton.setEnabled(false);
                finish();
            }).addOnFailureListener(e -> {
                Toast.makeText(MoodSelectionActivity.this, "Error deleting mood entry.", Toast.LENGTH_SHORT).show();
            });
        });

        AlertDialog dialog = builder.show();
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, R.color.md_theme_error));
    }
}

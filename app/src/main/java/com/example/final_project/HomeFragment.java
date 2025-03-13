package com.example.final_project;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.applandeo.materialcalendarview.CalendarDay;
import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.DatePicker;
import com.applandeo.materialcalendarview.builders.DatePickerBuilder;
import com.applandeo.materialcalendarview.listeners.OnCalendarDayClickListener;
import com.applandeo.materialcalendarview.listeners.OnSelectDateListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.datepicker.MaterialStyledDatePickerDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HomeFragment extends Fragment {
    private FirebaseAuth mAuth;
    private CalendarView calendarView;
    private FirebaseDatabase database;
    private DatabaseReference usersRef;
    private String userId;
    private TextView seeAllTextView, recentTextView;
    private ProgressBar progressBar;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");

        // Get user ID
        userId = mAuth.getCurrentUser().getUid();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize views
        progressBar = rootView.findViewById(R.id.progressBar);
        recentTextView = rootView.findViewById(R.id.recentTextView);
        seeAllTextView = rootView.findViewById(R.id.seeAllTextView);

        // Initialize calendarView
        calendarView = rootView.findViewById(R.id.calendarView);

        // Show progress bar while data is being fetched
        progressBar.setVisibility(View.VISIBLE);

        // Recent Card View Setup
        MaterialCardView cardView = rootView.findViewById(R.id.myCardView);
        ShapeAppearanceModel shapeAppearanceModel = new ShapeAppearanceModel().toBuilder().setTopLeftCorner(CornerFamily.ROUNDED, 30f).setBottomLeftCorner(CornerFamily.ROUNDED, 30f).setTopRightCorner(CornerFamily.ROUNDED, 0f).setBottomRightCorner(CornerFamily.ROUNDED, 0f).build();
        cardView.setShapeAppearanceModel(shapeAppearanceModel);

        // Fetch the latest mood and note data
        fetchLatestMoodAndNote();

        // Handle "See All" text view click
        seeAllTextView.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).replaceFragment(new MemoryFragment(), "MEMORY_FRAGMENT");
                ((MainActivity) getActivity()).binding.bottomNavigationView.setSelectedItemId(R.id.memory);
            }
        });

        // Set max calendar date to current date
        Calendar maxDate = Calendar.getInstance();
        calendarView.setMaximumDate(maxDate);

        // Handle calendar day selection
        calendarView.setOnCalendarDayClickListener(this::onCalendarDayClick);

        // Fetch and display moods for the user
        fetchAndDisplayMoods();

        return rootView;
    }

    private void onCalendarDayClick(@NonNull CalendarDay calendarDay) {
        Calendar selectedDate = calendarDay.getCalendar();
        Calendar today = Calendar.getInstance();

        // Prevent future date selection
        if (selectedDate.after(today)) {
            Toast.makeText(getContext(), "You cannot select a future date!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Open MoodSelectionActivity
        Intent intent = new Intent(getActivity(), MoodSelectionActivity.class);
        intent.putExtra("SELECTED_DATE", selectedDate.getTimeInMillis());
        startActivity(intent);
    }

    private void fetchAndDisplayMoods() {
        progressBar.setVisibility(View.VISIBLE);

        usersRef.child(userId).child("moodEntries").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<CalendarDay> eventDays = new ArrayList<>();
                for (DataSnapshot moodSnapshot : dataSnapshot.getChildren()) {
                    long timestamp = moodSnapshot.child("timestamp").getValue(Long.class);
                    String mood = moodSnapshot.child("mood").getValue(String.class);

                    // Add event icon or color based on mood
                    if (timestamp != 0 && mood != null) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(timestamp);
                        CalendarDay calendarDay = new CalendarDay(calendar);
                        switch (mood) {
                            case "Happy":
                                calendarDay.setImageResource(R.drawable.happy_face);
                                break;
                            case "Calm":
                                calendarDay.setImageResource(R.drawable.calm_face);
                                break;
                            case "Bored":
                                calendarDay.setImageResource(R.drawable.bored_face);
                                break;
                            case "Sad":
                                calendarDay.setImageResource(R.drawable.sad_face);
                                break;
                            case "Stressed":
                                calendarDay.setImageResource(R.drawable.stressed_face);
                                break;
                        }
                        eventDays.add(calendarDay); // Add the event to the list
                    }
                }

                // Set events to the calendar
                calendarView.setCalendarDays(eventDays);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error fetching moods.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchLatestMoodAndNote() {
        progressBar.setVisibility(View.VISIBLE);

        // Fetch the latest mood entry
        usersRef.child(userId).child("moodEntries").orderByChild("timestamp").limitToLast(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.GONE);

                if (dataSnapshot.exists()) {
                    DataSnapshot latestMoodSnapshot = dataSnapshot.getChildren().iterator().next();
                    long timestamp = latestMoodSnapshot.child("timestamp").getValue(Long.class);
                    String mood = latestMoodSnapshot.child("mood").getValue(String.class);
                    String note = latestMoodSnapshot.child("note").getValue(String.class);

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(timestamp);
                    String formattedDate = android.text.format.DateFormat.format("dd MMM yyyy", calendar).toString();

                    if (getView() != null) {
                        TextView dateRecent = getView().findViewById(R.id.dateRecent);
                        TextView noteRecent = getView().findViewById(R.id.noteRecent);

                        dateRecent.setText(formattedDate);
                        noteRecent.setText(!note.isEmpty() ? note : "No notes available");
                    }
                } else {
                    if (getView() != null) {
                        TextView dateRecent = getView().findViewById(R.id.dateRecent);
                        TextView noteRecent = getView().findViewById(R.id.noteRecent);

                        dateRecent.setText("No date recorded");
                        noteRecent.setText("No mood entry available");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error fetching latest mood data.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

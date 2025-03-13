package com.example.final_project;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.webkit.WebHistoryItem;
import android.widget.Button;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.final_project.databinding.ActivityMainBinding;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    FloatingActionButton fab, fabMenu1, fabMenu2, fabMenu3, fabMenu4, fabMenu5;
    boolean isMenuOpen = false;

    @Override
    protected void onStart() {
        super.onStart();

        checkIfMoodDataExists();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.bottomNavigationView.setBackground(null);
        EdgeToEdge.enable(this);


        fab = findViewById(R.id.fab);
        fabMenu1 = findViewById(R.id.fab_menu_1);
        fabMenu2 = findViewById(R.id.fab_menu_2);
        fabMenu3 = findViewById(R.id.fab_menu_3);
        fabMenu4 = findViewById(R.id.fab_menu_4);
        fabMenu5 = findViewById(R.id.fab_menu_5);


        // Hide menu buttons initially
        fabMenu1.setVisibility(View.GONE);
        fabMenu2.setVisibility(View.GONE);
        fabMenu3.setVisibility(View.GONE);
        fabMenu4.setVisibility(View.GONE);
        fabMenu5.setVisibility(View.GONE);

        replaceFragment(new HomeFragment(), "HOME_FRAGMENT");

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home) {
                replaceFragment(new HomeFragment(), "HOME_FRAGMENT");
            } else if (item.getItemId() == R.id.memory) {
                replaceFragment(new MemoryFragment(), "MEMORY_FRAGMENT");
            }
            return true;
        });

        // Top App Bar
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }
        Drawable newIcon = ContextCompat.getDrawable(this, R.drawable.profile_icon);
        topAppBar.setOverflowIcon(newIcon);


        fab.setOnClickListener(v -> toggleMenu());

        fabMenu1.setOnClickListener(v -> {
            navigateToMoodSelectionActivity("Happy");
        });

        fabMenu2.setOnClickListener(v -> {
            navigateToMoodSelectionActivity("Calm");
        });

        fabMenu3.setOnClickListener(v -> {
            navigateToMoodSelectionActivity("Bored");
        });

        fabMenu4.setOnClickListener(v -> {
            navigateToMoodSelectionActivity("Sad");
        });

        fabMenu5.setOnClickListener(v -> {
            navigateToMoodSelectionActivity("Stressed");
        });

        checkIfMoodDataExists();


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_profile) {
            ProfileDialog.showProfileDialog(this);
            return true;
        } else if (item.getItemId() == R.id.action_logout) {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setNeutralButton("Cancel", (d, which) -> d.dismiss())
                    .setNegativeButton("Logout", (d, which) -> logoutUser());

            AlertDialog dialog = builder.show();
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                    .setTextColor(ContextCompat.getColor(this, R.color.md_theme_error));

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkIfMoodDataExists() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userMoodRef = database.getReference("users").child(userId).child("moodEntries");

        // Get the current date in Unix timestamp (start of today)
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long startOfDayTimestamp = calendar.getTimeInMillis(); // Start of today's timestamp
        long endOfDayTimestamp = startOfDayTimestamp + (24 * 60 * 60 * 1000); // End of today's timestamp

        // Query mood data to find an entry within today's timestamp range
        userMoodRef.orderByChild("timestamp")
                .startAt(startOfDayTimestamp)
                .endAt(endOfDayTimestamp)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {
                            // Mood data for today exists, disable the FAB
                            disableFab();
                            closeFabMenu();
                        } else {
                            // No mood data for today, enable the FAB
                            enableFab();
                        }
                    } else {
                        Log.e("MoodDataCheck", "Error checking data", task.getException());
                    }
                });
    }


    private void disableFab() {
        fab.setEnabled(false);
    }

    private void enableFab() {
        // Enable the FAB and make it clickable again
        fab.setEnabled(true);
    }

    private void closeFabMenu() {
        if (isMenuOpen) {
            toggleMenu();
        }
    }


    private void toggleMenu() {
        float translationY = 200f;

        if (isMenuOpen) {
            fabMenu5.postDelayed(() -> animateFab(fabMenu5, 0f, 1f, true), 0);
            fabMenu4.postDelayed(() -> animateFab(fabMenu4, 0f, 1f, true), 100);
            fabMenu3.postDelayed(() -> animateFab(fabMenu3, 0f, 1f, true), 200);
            fabMenu2.postDelayed(() -> animateFab(fabMenu2, 0f, 1f, true), 300);
            fabMenu1.postDelayed(() -> animateFab(fabMenu1, 0f, 1f, true), 400);
            animateFabIconChange(fab, R.drawable.plus_icon);
        } else {

            fabMenu1.postDelayed(() -> animateFab(fabMenu1, translationY, 0f, false), 0);
            fabMenu2.postDelayed(() -> animateFab(fabMenu2, translationY, 0f, false), 100);
            fabMenu3.postDelayed(() -> animateFab(fabMenu3, translationY, 0f, false), 200);
            fabMenu4.postDelayed(() -> animateFab(fabMenu4, translationY, 0f, false), 300);
            fabMenu5.postDelayed(() -> animateFab(fabMenu5, translationY, 0f, false), 400);
            animateFabIconChange(fab, R.drawable.close_icon);
        }
        isMenuOpen = !isMenuOpen;
    }





    private void navigateToMoodSelectionActivity(String mood) {
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long selectedDateMillis = calendar.getTimeInMillis();

        Intent intent = new Intent(this, MoodSelectionActivity.class);
        intent.putExtra("SELECTED_MOOD", mood);
        intent.putExtra("SELECTED_DATE", selectedDateMillis);
        startActivity(intent);
    }




    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();

        Intent intent = new Intent(this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void animateFab(View fab, float fromY, float toY, boolean isClosing) {
        fab.setVisibility(View.VISIBLE);
        AnimatorSet animatorSet = new AnimatorSet();

        ObjectAnimator translationY = ObjectAnimator.ofFloat(fab, "translationY", fromY, toY);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(fab, "alpha", isClosing ? 1f : 0f, isClosing ? 0f : 1f);

        animatorSet.playTogether(translationY, alpha);
        animatorSet.setDuration(300);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());

        animatorSet.start();

        if (isClosing) {
            fab.postDelayed(() -> fab.setVisibility(View.GONE), 300);
        }
    }

    private void animateFabIconChange(FloatingActionButton fab, int newIconRes) {
        ObjectAnimator rotateOut = ObjectAnimator.ofFloat(fab, "rotation", 0f, 360f);
        rotateOut.setDuration(450);

        rotateOut.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                fab.setImageResource(newIconRes); // Change the icon when the rotation is finished.
            }
        });

        rotateOut.start();
    }


    protected void replaceFragment(Fragment fragment, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment, tag);
        fragmentTransaction.commit();
    }
}

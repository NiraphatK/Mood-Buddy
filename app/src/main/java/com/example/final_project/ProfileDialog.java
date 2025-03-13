package com.example.final_project;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileDialog {

    public static void showProfileDialog(Context context) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Toast.makeText(context, "User is not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(user.getUid());

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Toast.makeText(context, "User data not found", Toast.LENGTH_SHORT).show();
                    return;
                }

                String userName = dataSnapshot.child("username").getValue(String.class);
                if (userName == null) userName = "No name available";

                showEditProfileDialog(context, userRef, userName);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(context, "Failed to retrieve user data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static void showEditProfileDialog(Context context, DatabaseReference userRef, String currentUserName) {
        // Layout for the dialog
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 30, 50, 10);

        // EditText for the user name
        EditText editText = new EditText(context);
        editText.setText(currentUserName);
        editText.setHint("Enter your new name");
        editText.setSingleLine(true);
        editText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(20) });  // Limit input to 20 characters
        layout.addView(editText);

        // TextView for displaying character count
        TextView charCountTextView = new TextView(context);
        charCountTextView.setText("0/20");

        // Set Gravity to RIGHT (end) to align it to the right side
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.END;  // Align the TextView to the right
        charCountTextView.setLayoutParams(params);

        layout.addView(charCountTextView);

        // Initialize MaterialAlertDialogBuilder
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context)
                .setTitle("Profile")
                .setMessage("Hello, " + currentUserName + "\nUpdate your name below:")
                .setView(layout)
                .setNeutralButton("Cancel", (dialog, which) -> dialog.dismiss());

        // Initialize 'Save' button as disabled initially
        builder.setPositiveButton("Save", (dialog, which) -> {
            String newUserName = editText.getText().toString().trim();
            if (!TextUtils.isEmpty(newUserName) && !newUserName.equals(currentUserName)) {
                updateUserName(context, userRef, newUserName);
            }
        });

        // Show the dialog
        AlertDialog dialog = builder.show();

        // Get the positive button and disable it initially
        Button saveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        saveButton.setEnabled(false);

        // Add TextWatcher to update character count and enable/disable "Save" button
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int after) {
                // Update character count (e.g., "X/20")
                int length = charSequence.length();
                charCountTextView.setText(length + "/20");

                // Enable "Save" button if the new name is not empty and different from the current name
                String newUserName = charSequence.toString().trim();
                saveButton.setEnabled(!TextUtils.isEmpty(newUserName) && !newUserName.equals(currentUserName));
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    private static void updateUserName(Context context, DatabaseReference userRef, String newUserName) {
        if (TextUtils.isEmpty(newUserName)) {
            Toast.makeText(context, "Please enter a valid name", Toast.LENGTH_SHORT).show();
            return;
        }

        userRef.child("username").setValue(newUserName)
                .addOnSuccessListener(aVoid -> Toast.makeText(context, "Username updated successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(context, "Failed to update username: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private static void logoutUser(Context context) {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Redirect to the login activity
        Intent intent = new Intent(context, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
}

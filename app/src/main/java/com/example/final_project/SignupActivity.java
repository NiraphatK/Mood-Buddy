package com.example.final_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SignupActivity extends AppCompatActivity {
    EditText emailSignupEditText, usernameSignupEditText, passwordSignupEditText, confirmPasswordSignupEditText;
    Button signupSignupButton;
    TextInputLayout emailSignupTextInputLayout, usernameSignupTextInputLayout, passwordSignupTextInputLayout, confirmPasswordSignupTextInputLayout;
    FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();

        emailSignupEditText = findViewById(R.id.emailSignupEditText);
        usernameSignupEditText = findViewById(R.id.usernameSignupEditText);
        passwordSignupEditText = findViewById(R.id.passwordSignupEditText);
        confirmPasswordSignupEditText = findViewById(R.id.confirmPasswordSignupEditText);
        signupSignupButton = findViewById(R.id.signupSignupButton);
        emailSignupTextInputLayout = findViewById(R.id.emailSignupTextInputLayout);
        usernameSignupTextInputLayout = findViewById(R.id.usernameSignupTextInputLayout);
        passwordSignupTextInputLayout = findViewById(R.id.passwordLoginTextInputLayout);
        confirmPasswordSignupTextInputLayout = findViewById(R.id.confirmPasswordSignupTextInputLayout);

        // Signup Button
        signupSignupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                database = FirebaseDatabase.getInstance();
                reference = database.getReference("users");

                String email = emailSignupEditText.getText().toString().trim();
                String username = usernameSignupEditText.getText().toString().trim();
                String password = passwordSignupEditText.getText().toString().trim();
                String confirmPassword = confirmPasswordSignupEditText.getText().toString().trim();

                emailSignupTextInputLayout.setHelperTextEnabled(false);
                usernameSignupTextInputLayout.setHelperTextEnabled(false);
                passwordSignupTextInputLayout.setHelperTextEnabled(false);
                confirmPasswordSignupTextInputLayout.setHelperTextEnabled(false);

                boolean isValid = true;

                // Email validation
                if (email.isEmpty()) {
                    emailSignupTextInputLayout.setHelperText("Required*");
                    isValid = false;
                } else {
                    emailSignupTextInputLayout.setHelperText(" ");
                }

                // Username validation
                if (username.isEmpty()) {
                    usernameSignupTextInputLayout.setHelperText("Required*");
                    isValid = false;
                } else {
                    usernameSignupTextInputLayout.setHelperText(" ");
                }

                // Password validation
                if (password.isEmpty()) {
                    passwordSignupTextInputLayout.setHelperText("Required*");
                    isValid = false;
                } else {
                    passwordSignupTextInputLayout.setHelperText(" ");
                }

                // Confirm Password validation
                if (confirmPassword.isEmpty()) {
                    confirmPasswordSignupTextInputLayout.setHelperText("Required*");
                    isValid = false;
                } else if (!password.equals(confirmPassword)) {
                    confirmPasswordSignupTextInputLayout.setHelperText("Passwords do not match");
                    isValid = false;
                } else {
                    confirmPasswordSignupTextInputLayout.setHelperText(" ");
                }

                if (!isValid) return;

                // Check if the username already exists in the database
                reference.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            usernameSignupTextInputLayout.setHelperText("Username already exists. Please choose another.");
                        } else {
                            registerUser(email, username, password);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        usernameSignupTextInputLayout.setHelperText("Database Error: " + error.getMessage());
                    }
                });

            }

            private void registerUser(String email, String username, String password) {
                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser currentUser = mAuth.getCurrentUser();
                            if (currentUser != null) {
                                String userId = currentUser.getUid();

                                // Send verification email
                                currentUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            // Save user details to Firebase Database
                                            UserClass userClass = new UserClass(email, username);
                                            reference.child(userId).setValue(userClass).addOnSuccessListener(aVoid -> {
                                                mAuth.signOut();
                                                Toast.makeText(SignupActivity.this, "Signup Successful. Please verify your email.", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                                                startActivity(intent);
                                                finish(); // Close SignupActivity to prevent navigating back
                                            }).addOnFailureListener(e -> Toast.makeText(SignupActivity.this, "Database Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                        } else {
                                            Toast.makeText(SignupActivity.this, "Error sending verification email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        } else {
                            String errorMsg = task.getException().getMessage();
                            if (errorMsg.contains("email")) {
                                emailSignupTextInputLayout.setHelperText(errorMsg);
                                emailSignupTextInputLayout.setHelperTextEnabled(true);
                            } else {
                                passwordSignupTextInputLayout.setHelperText(errorMsg);
                                passwordSignupTextInputLayout.setHelperTextEnabled(true);
                            }
                        }
                    }
                });
            }

        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
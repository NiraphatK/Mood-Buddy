package com.example.final_project;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

public class LoginActivity extends AppCompatActivity {
    EditText emailLoginEditText, passwordLoginEditText;
    Button loginLoginButton, backButtonLogin;
    TextInputLayout emailLoginTextInputLayout, passwordLoginTextInputLayout;
    TextView forgotPasswordTextView;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        emailLoginEditText = findViewById(R.id.emailLoginEditText);
        passwordLoginEditText = findViewById(R.id.passwordLoginEditText);
        loginLoginButton = findViewById(R.id.loginLoginButton);
        emailLoginTextInputLayout = findViewById(R.id.emailLoginTextInputLayout);
        passwordLoginTextInputLayout = findViewById(R.id.passwordLoginTextInputLayout);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);
        backButtonLogin = findViewById(R.id.backButtonLogin);

        emailLoginTextInputLayout.setHelperTextTextAppearance(R.style.HelperTextStyle);
        passwordLoginTextInputLayout.setHelperTextTextAppearance(R.style.HelperTextStyle);

        // Back Button
        backButtonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Forgot Password
        forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });

        // Login Button
        loginLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailLoginEditText.getText().toString().trim();
                String password = passwordLoginEditText.getText().toString().trim();

                emailLoginTextInputLayout.setHelperTextEnabled(false);
                passwordLoginTextInputLayout.setHelperTextEnabled(false);

                boolean isValid = true;

                // Email validation
                if (email.isEmpty()) {
                    emailLoginTextInputLayout.setHelperText("Required*");
                    isValid = false;
                } else {
                    emailLoginTextInputLayout.setHelperText(" ");
                }

                // Password validation
                if (password.isEmpty()) {
                    passwordLoginTextInputLayout.setHelperText("Required*");
                    isValid = false;
                } else {
                    passwordLoginTextInputLayout.setHelperText(" ");
                }

                if (!isValid) return;

                // Sign in with email and password
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, check if email is verified
                            FirebaseUser currentUser = mAuth.getCurrentUser();
                            if (currentUser != null && currentUser.isEmailVerified()) {
                                // Email is verified, proceed to the main activity
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish(); // Finish the LoginActivity so the user can't navigate back
                            } else {
                                // Email is not verified, show a message to the user
                                mAuth.signOut();  // Optionally sign the user out
                                Toast.makeText(LoginActivity.this, "Please verify your email first.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                // If email format is invalid, show the email format error
                                emailLoginTextInputLayout.setHelperText("The email address is badly formatted.");
                                emailLoginTextInputLayout.setHelperTextEnabled(true);
                            } else {
                                // If the email format is correct, hide the email helper text
                                emailLoginTextInputLayout.setHelperText(" ");
                                emailLoginTextInputLayout.setHelperTextEnabled(true);

                                passwordLoginTextInputLayout.setHelperText("Incorrect email or password. Please try again.");
                                passwordLoginTextInputLayout.setHelperTextEnabled(true);
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

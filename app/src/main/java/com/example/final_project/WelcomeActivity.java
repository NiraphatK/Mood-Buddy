package com.example.final_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class WelcomeActivity extends AppCompatActivity {
    Button signupWelcomeButton, loginWelcomeButton;
    FirebaseAuth mAuth;

    @Override
    public void onStart() {
        super.onStart();

        // Check if the user is signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // If the user is already logged in, navigate to MainActivity
            Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Close WelcomeActivity to prevent navigating back to it
        }
        // If not logged in, stay on the WelcomeActivity and wait for the user to log in
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome);

        mAuth = FirebaseAuth.getInstance();

        signupWelcomeButton = findViewById(R.id.signupWelcomeButton);
        loginWelcomeButton = findViewById(R.id.loginWelcomeButton);

        // Signup Button onClick
        signupWelcomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });

        // Login Button onClick
        loginWelcomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
package com.example.final_project;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {
    TextInputLayout emailForgotPasswordTextInputLayout;
    EditText emailForgotPasswordEditText;
    Button sendForgotPasswordButton;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);

        mAuth = FirebaseAuth.getInstance();

        emailForgotPasswordTextInputLayout = findViewById(R.id.emailForgotPasswordTextInputLayout);
        emailForgotPasswordEditText = findViewById(R.id.emailForgotPasswordEditText);
        sendForgotPasswordButton = findViewById(R.id.sendForgotPasswordButton);

        sendForgotPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailForgotPasswordEditText.getText().toString().trim();
                emailForgotPasswordTextInputLayout.setHelperTextEnabled(false);

                boolean isValid = true;

                // Email validation
                if (email.isEmpty()) {
                    emailForgotPasswordTextInputLayout.setHelperText("Required*");
                    isValid = false;
                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailForgotPasswordTextInputLayout.setHelperText("The email address is badly formatted.");
                    isValid = false;
                } else {
                    emailForgotPasswordTextInputLayout.setHelperText(" ");
                }

                if (!isValid) return;

                mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
                    new MaterialAlertDialogBuilder(ForgotPasswordActivity.this)
                            .setTitle("Reset Password")
                            .setMessage("If an account with this email exists, you will receive a password reset link.")
                            .setPositiveButton("OK", (dialog, which) -> {
                                Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            })
                            .show();
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
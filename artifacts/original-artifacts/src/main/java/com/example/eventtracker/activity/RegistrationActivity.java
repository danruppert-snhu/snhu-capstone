package com.example.eventtracker.activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventtracker.R;
import com.example.eventtracker.db.DatabaseHelper;
import com.example.eventtracker.utils.SecurityUtils;

public class RegistrationActivity extends AppCompatActivity {

    private EditText usernameText, passwordText, confirmPasswordText;
    private Button registerButton;
    private DatabaseHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        usernameText = findViewById(R.id.username_text);
        passwordText = findViewById(R.id.register_password_text);
        confirmPasswordText = findViewById(R.id.register_confirm_password_text);
        registerButton = findViewById(R.id.register_button);

        dbHelper = new DatabaseHelper(this);

        registerButton.setOnClickListener(v -> registerUser());
    }
    private void registerUser() {
        String username = usernameText.getText().toString().trim();
        String password = passwordText.getText().toString().trim();
        String confirmPassword = confirmPasswordText.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }
        if (dbHelper.checkUserExists(username)) {
            Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!dbHelper.checkPasswordStrength(password)) {
            new AlertDialog.Builder(this)
                    .setTitle("Password Requirements")
                    .setMessage("Password must be at least 8 characters long and include:\n" +
                            "- An uppercase letter\n" +
                            "- A lowercase letter\n" +
                            "- A digit\n" +
                            "- A special character")
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();
            return;
        }



        try {
            String salt = SecurityUtils.generateSalt();
            String hashedPassword = SecurityUtils.hashPassword(password, salt);
            boolean userCreated = dbHelper.addUser(username, hashedPassword, salt);
            if (userCreated) {
                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show();
                usernameText.setText("");
                passwordText.setText("");
                confirmPasswordText.setText("");
                Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Internal error with registration!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error registering user", Toast.LENGTH_SHORT).show();
        }
    }

}




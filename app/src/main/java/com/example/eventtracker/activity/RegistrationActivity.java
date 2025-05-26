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
import com.example.eventtracker.utils.DatabaseConstants;
import com.example.eventtracker.utils.ErrorUtils;
import com.example.eventtracker.utils.SecurityUtils;

public class RegistrationActivity extends AppCompatActivity {

    private EditText usernameText, passwordText, confirmPasswordText;
    private Button registerButton;
    private DatabaseHelper dbHelper;
    /**
     * Initializes the registration form and its event handlers.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        //Link UI elements to their respective views
        usernameText = findViewById(R.id.username_text);
        passwordText = findViewById(R.id.register_password_text);
        confirmPasswordText = findViewById(R.id.register_confirm_password_text);
        registerButton = findViewById(R.id.register_button);

        //Initialize the database helper instance
        dbHelper = new DatabaseHelper(this);

        //Set listener to handle registration when button is clicked.
        registerButton.setOnClickListener(v -> registerUser());
    }

    /**
     * Handles the logic for registering a new user.
     */
    private void registerUser() {
        // Extract and sanitize user input from form fields
        String username = usernameText.getText().toString().trim();
        String password = passwordText.getText().toString().trim();
        String confirmPassword = confirmPasswordText.getText().toString().trim();

        // Validate all fields are filled
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if passwords match
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the username is already taken
        if (dbHelper.checkUserExists(username)) {
            Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show();
            return;
        }

        // Enforce strong password policy
        if (!dbHelper.checkPasswordStrength(password)) {
            new AlertDialog.Builder(this)
                    .setTitle("Password Requirements")
                    .setMessage("Password must be at least " + DatabaseConstants.PASSWORD_LENGTH_REQUIREMENT + " characters long and include:\n" +
                            "- An uppercase letter\n" +
                            "- A lowercase letter\n" +
                            "- A digit\n" +
                            "- A special character")
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();
            return;
        }



        try {
            // Generate salt and hash password securely
            String salt = SecurityUtils.generateSalt();
            String hashedPassword = SecurityUtils.hashPassword(password, salt);
            // Attempt to insert new user into the database
            boolean userCreated = dbHelper.addUser(username, hashedPassword, salt);
            if (userCreated) {
                // Notify success and reset form fields
                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show();
                usernameText.setText("");
                passwordText.setText("");
                confirmPasswordText.setText("");

                // Redirect user to the login screen
                Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                // Registration attempt failed, notify user
                //CS-499 - Software Engineering: Improved logging and error handling.
                ErrorUtils.showAndLogError(this, "RegistrationError", "Internal error with registration!", new Exception("Internal error with registration!"));
            }
        } catch (Exception e) {
            // Catch and log any unexpected exceptions during registration
            //CS-499 - Software Engineering: Improved logging and error handling.
            ErrorUtils.showAndLogError(this, "RegistrationError", "Registration failed. Please try again.", e);
            Toast.makeText(this, "Error registering user", Toast.LENGTH_SHORT).show();
        }
    }

}




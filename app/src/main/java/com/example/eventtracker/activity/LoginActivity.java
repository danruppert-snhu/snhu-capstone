package com.example.eventtracker.activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventtracker.R;
import com.example.eventtracker.db.DatabaseHelper;
import com.example.eventtracker.entity.User;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameText, passwordText;
    private Button loginButton;
    private TextView registerText;
    private DatabaseHelper dbHelper;

    private User user;


    /**
     * Initializes the Login form UI and button event handlers.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Define form components and database helper initialization
        usernameText = findViewById(R.id.username_text);
        passwordText = findViewById(R.id.confirm_password_text);
        registerText = findViewById(R.id.register_text);
        loginButton = findViewById(R.id.login_button);
        dbHelper = new DatabaseHelper(this);

        //Bind login button to a function to handle the authentication
        loginButton.setOnClickListener(v -> loginUser());
        //Bind a function to the register button to load the registration form
        registerText.setOnClickListener(v -> showRegistration());
    }

    private void showRegistration() {
        Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
        startActivity(intent);
    }

    /*
     * Handles login attempt by validating user input and calling the database for authentication.
     */
    private void loginUser() {
        //Extract username and password from user input fields
        String username = usernameText.getText().toString().trim();
        String password = passwordText.getText().toString().trim();

        //Validate that the user credentials have been entered on button press
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        //Attempt to authenticate the user
        int userId = dbHelper.authenticateUser(username, password);
        boolean isUserValid = userId != -1;
        if (isUserValid) {
            //Instantiate the Person object to represent this user.
            User user = new User(userId, username);

            //Storing as a variable in this class in the event we need it for future functionality.
            this.user = user;

            //Redirect the user to the home screen (post-login) if authentication is successful
            Intent intent = new Intent(LoginActivity.this, EventListActivity.class);
            intent.putExtra("user", user);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            //Do not progress, prompt user for re-authentication
            Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
        }
    }
}


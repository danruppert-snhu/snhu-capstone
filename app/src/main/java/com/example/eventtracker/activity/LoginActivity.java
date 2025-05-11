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

public class LoginActivity extends AppCompatActivity {

    private EditText usernameText, passwordText;
    private Button loginButton;
    private TextView registerText;
    private DatabaseHelper dbHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameText = findViewById(R.id.username_text);
        passwordText = findViewById(R.id.confirm_password_text);
        registerText = findViewById(R.id.register_text);
        loginButton = findViewById(R.id.login_button);
        dbHelper = new DatabaseHelper(this);

        loginButton.setOnClickListener(v -> loginUser());
        registerText.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String username = usernameText.getText().toString().trim();
        String password = passwordText.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isUserValid = dbHelper.authenticateUser(username, password);
        if (isUserValid) {
            Intent intent = new Intent(LoginActivity.this, EventListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
        }
    }
}


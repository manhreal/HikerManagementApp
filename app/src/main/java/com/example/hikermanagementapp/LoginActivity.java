package com.example.hikermanagementapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText et_log_email, et_log_password;
    private Button btn_login;
    private TextView tv_link_register;
    private Database db;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // check login
        sharedPreferences = getSharedPreferences("MHikePrefs", MODE_PRIVATE);
        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
            // login success
            goToMainActivity();
            return;
        }

        setContentView(R.layout.activity_login);

        db = new Database(this);

        et_log_email = findViewById(R.id.et_log_email);
        et_log_password = findViewById(R.id.et_log_password);
        btn_login = findViewById(R.id.btn_login);
        tv_link_register = findViewById(R.id.tv_link_register);

        // login event
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        tv_link_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loginUser() {
        String email = et_log_email.getText().toString().trim();
        String password = et_log_password.getText().toString().trim();

        // Validation
        if (email.isEmpty()) {
            et_log_email.setError("Please enter email");
            et_log_email.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            et_log_password.setError("Please enter password");
            et_log_password.requestFocus();
            return;
        }

        // check login info
        if (db.checkUser(email, password)) {

            // login successfully
            int userId = db.getUserIdByEmail(email);

            // save login status
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isLoggedIn", true);
            editor.putString("email", email);
            editor.putInt("userId", userId);
            editor.apply();

            Toast.makeText(LoginActivity.this,
                    "Login successfully! Welcome " + email,
                    Toast.LENGTH_SHORT).show();

            goToMainActivity();
        } else {
            // Login failed
            Toast.makeText(LoginActivity.this,
                    "Incorrect email or password!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void goToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}

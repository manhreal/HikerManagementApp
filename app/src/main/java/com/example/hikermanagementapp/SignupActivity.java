package com.example.hikermanagementapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class SignupActivity extends AppCompatActivity {
    private TextInputEditText et_reg_email, et_reg_password, et_reg_password_cf;
    private Button btn_register;
    private TextView tv_link_login;
    private Database db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        db = new Database(this);

        et_reg_email = findViewById(R.id.et_reg_email);
        et_reg_password = findViewById(R.id.et_reg_password);
        et_reg_password_cf = findViewById(R.id.et_reg_password_cf);
        btn_register = findViewById(R.id.btn_register);
        tv_link_login = findViewById(R.id.tv_link_login);

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        tv_link_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void registerUser() {
        String email = et_reg_email.getText().toString().trim();
        String password = et_reg_password.getText().toString().trim();
        String password_cf = et_reg_password_cf.getText().toString().trim();
        String name = "User";
        String avatar = "default_avatar";

        // Validation
        if (email.isEmpty()) {
            et_reg_email.setError("Please enter email");
            et_reg_email.requestFocus();
            return;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            et_reg_email.setError("Invalid email format");
            et_reg_email.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            et_reg_password.setError("Please enter password");
            et_reg_password.requestFocus();
            return;
        } else if (password.length() < 8) {
            et_reg_password.setError("Password must be at least 8 characters");
            et_reg_password.requestFocus();
            return;
        }

        if (password_cf.isEmpty()) {
            et_reg_password_cf.setError("Please confirm password");
            et_reg_password_cf.requestFocus();
            return;
        }

        if (!password.equals(password_cf)) {
            et_reg_password_cf.setError("Password confirmation does not match");
            et_reg_password_cf.requestFocus();
            return;
        }

        // FIX: Check if email exists before registering
        if (db.getUserIdByEmail(email) != -1) {
            Toast.makeText(this, "Email already exists!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Register user
        boolean reg_success = db.registerUser(email, password, name, avatar);

        if (reg_success) {
            Toast.makeText(this, "Register successfully! Please login.", Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}
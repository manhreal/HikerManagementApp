package com.example.hikermanagementapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView img_avatar;
    private TextView tv_p_name, tv_p_email, tv_p_total_trip;
    private MaterialButton btn_p_update, btn_p_change_pass, btn_p_update_avt;
    private Database db;
    private SharedPreferences sharedPreferences;
    private int userId;
    private String currentAvatarPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.p_tool_bar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Profile");
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Initialize views
        img_avatar = findViewById(R.id.img_avatar);
        tv_p_name = findViewById(R.id.tv_p_name);
        tv_p_email = findViewById(R.id.tv_p_email);
        tv_p_total_trip = findViewById(R.id.tv_p_total_trip);
        btn_p_update = findViewById(R.id.btn_p_update);
        btn_p_change_pass = findViewById(R.id.btn_p_change_pass);
        btn_p_update_avt = findViewById(R.id.btn_p_update_avt);

        db = new Database(this);
        sharedPreferences = getSharedPreferences("MHikePrefs", MODE_PRIVATE);

        // FIX: Get userId with correct key
        userId = sharedPreferences.getInt("user_id", -1);

        if (userId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadUserProfile();

        // Update name button
        btn_p_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUpdateNameDialog();
            }
        });

        // Change password button
        btn_p_change_pass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangePasswordDialog();
            }
        });

        // Update avatar button
        btn_p_update_avt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile();
    }

    private void loadUserProfile() {
        Cursor cursor = db.getUserById(userId);
        if (cursor != null && cursor.moveToFirst()) {
            String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            currentAvatarPath = cursor.getString(cursor.getColumnIndexOrThrow("avatar"));

            tv_p_email.setText(email);
            tv_p_name.setText(name != null && !name.isEmpty() ? name : "User");

            // Load avatar
            if (currentAvatarPath != null && !currentAvatarPath.equals("default_avatar")) {
                File imgFile = new File(currentAvatarPath);
                if (imgFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    img_avatar.setImageBitmap(bitmap);
                } else {
                    img_avatar.setImageResource(R.drawable.ic_avt_default);
                }
            } else {
                img_avatar.setImageResource(R.drawable.ic_avt_default);
            }

            cursor.close();
        }

        // Load trip count
        int tripCount = db.getTripCountByUser(userId);
        tv_p_total_trip.setText(String.valueOf(tripCount));
    }

    private void showUpdateNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Name");

        final EditText input = new EditText(this);
        input.setText(tv_p_name.getText().toString());
        input.setPadding(50, 30, 50, 30);
        builder.setView(input);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = input.getText().toString().trim();
                if (newName.isEmpty()) {
                    Toast.makeText(ProfileActivity.this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (db.updateUserName(userId, newName)) {
                    tv_p_name.setText(newName);
                    Toast.makeText(ProfileActivity.this, "Name updated successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to update name", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Password");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        builder.setView(dialogView);

        final TextInputEditText et_old_pass = dialogView.findViewById(R.id.et_old_password);
        final TextInputEditText et_new_pass = dialogView.findViewById(R.id.et_new_password);
        final TextInputEditText et_confirm_pass = dialogView.findViewById(R.id.et_confirm_password);

        builder.setPositiveButton("Change", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String oldPass = et_old_pass.getText().toString().trim();
                String newPass = et_new_pass.getText().toString().trim();
                String confirmPass = et_confirm_pass.getText().toString().trim();

                // Validation
                if (oldPass.isEmpty()) {
                    Toast.makeText(ProfileActivity.this, "Please enter old password", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (newPass.isEmpty()) {
                    Toast.makeText(ProfileActivity.this, "Please enter new password", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (newPass.length() < 8) {
                    Toast.makeText(ProfileActivity.this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!newPass.equals(confirmPass)) {
                    Toast.makeText(ProfileActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (db.changePassword(userId, oldPass, newPass)) {
                    Toast.makeText(ProfileActivity.this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileActivity.this, "Old password is incorrect", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                // Save image to internal storage
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                // Create file path
                String fileName = "avatar_" + userId + ".jpg";
                File file = new File(getFilesDir(), fileName);
                FileOutputStream outputStream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
                outputStream.close();

                String avatarPath = file.getAbsolutePath();

                // Update database
                if (db.updateUserAvatar(userId, avatarPath)) {
                    img_avatar.setImageBitmap(bitmap);
                    currentAvatarPath = avatarPath;
                    Toast.makeText(this, "Avatar updated successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to update avatar", Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
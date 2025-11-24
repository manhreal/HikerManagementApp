package com.example.hikermanagementapp;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;

public class ProfileActivity extends AppCompatActivity {

    private Toolbar p_tool_bar;

    private ImageView img_avatar;
    private MaterialButton btn_p_update_avt, btn_p_update, btn_p_change_pass;

    private TextView tv_p_name, tv_p_email, tv_p_total_trip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Toolbar
        p_tool_bar = findViewById(R.id.p_tool_bar);
        setSupportActionBar(p_tool_bar);
        // Hiển thị nút back trên toolbar
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Profile Management");
        }

        // Ánh xạ Views
        img_avatar = findViewById(R.id.img_avatar);
        btn_p_update_avt = findViewById(R.id.btn_p_update_avt);
        btn_p_update = findViewById(R.id.btn_p_update);
        btn_p_change_pass = findViewById(R.id.btn_p_change_pass);

        tv_p_name = findViewById(R.id.tv_p_name);
        tv_p_email = findViewById(R.id.tv_p_email);
        tv_p_total_trip = findViewById(R.id.tv_p_total_trip);

        // TODO: Load dữ liệu profile thật vào các TextView, ví dụ từ Intent hoặc SharedPreferences

        // Các xử lý button ví dụ:
        btn_p_update_avt.setOnClickListener(v -> {
            Toast.makeText(ProfileActivity.this, "Update Avatar clicked", Toast.LENGTH_SHORT).show();
            // TODO: thêm logic cập nhật avatar
        });

        btn_p_update.setOnClickListener(v -> {
            Toast.makeText(ProfileActivity.this, "Update Profile clicked", Toast.LENGTH_SHORT).show();
            // TODO: thêm logic cập nhật profile
        });

        btn_p_change_pass.setOnClickListener(v -> {
            Toast.makeText(ProfileActivity.this, "Change Password clicked", Toast.LENGTH_SHORT).show();
            // TODO: thêm logic đổi mật khẩu
        });
    }

    // Xử lý nút back trên toolbar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

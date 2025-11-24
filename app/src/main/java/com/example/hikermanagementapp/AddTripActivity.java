package com.example.hikermanagementapp;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Calendar;

public class AddTripActivity extends AppCompatActivity {

    private TextInputEditText et_a_trip_name, et_a_trip_destination, et_a_trip_date,
            et_a_trip_length, et_a_trip_description;
    private CheckBox cb_a_trip_parking;
    private Spinner sp_a_trip_difficulty;
    private Button btn_a_trip_add, btn_a_trip_cancel;
    private Database db;
    private int user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_trip);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Add a trip");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        SharedPreferences sharedPreferences = getSharedPreferences("MHikePrefs", MODE_PRIVATE);
        user_id = sharedPreferences.getInt("user_id", -1);

        db = new Database(this);

        et_a_trip_name = findViewById(R.id.et_a_trip_name);
        et_a_trip_destination = findViewById(R.id.et_a_trip_destination);
        et_a_trip_date = findViewById(R.id.et_a_trip_date);
        et_a_trip_length = findViewById(R.id.et_a_trip_length);
        et_a_trip_description = findViewById(R.id.et_a_trip_description);
        cb_a_trip_parking = findViewById(R.id.cb_a_trip_parking);
        sp_a_trip_difficulty = findViewById(R.id.sp_a_trip_difficulty);
        btn_a_trip_add = findViewById(R.id.btn_a_trip_add);
        btn_a_trip_cancel = findViewById(R.id.btn_a_trip_cancel);

        String[] difficulties = {"Easy", "Medium", "Hard"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, difficulties);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_a_trip_difficulty.setAdapter(adapter);

        et_a_trip_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        btn_a_trip_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTrip();
            }
        });

        btn_a_trip_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String date = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year);
                        et_a_trip_date.setText(date);
                    }
                }, year, month, day);

        datePickerDialog.show();
    }

    private void saveTrip() {
        String name = et_a_trip_name.getText().toString().trim();
        String destination = et_a_trip_destination.getText().toString().trim();
        String date = et_a_trip_date.getText().toString().trim();
        boolean require_parking = cb_a_trip_parking.isChecked();
        String difficulty = sp_a_trip_difficulty.getSelectedItem().toString();
        String description = et_a_trip_description.getText().toString().trim();

        if (name.isEmpty()) {
            et_a_trip_name.setError("Please enter trip name");
            et_a_trip_name.requestFocus();
            return;
        }

        if (destination.isEmpty()) {
            et_a_trip_destination.setError("Please enter a destination");
            et_a_trip_destination.requestFocus();
            return;
        }

        if (date.isEmpty()) {
            et_a_trip_date.setError("Please pick date");
            et_a_trip_date.requestFocus();
            return;
        }

        String lengthStr = et_a_trip_length.getText().toString().trim();

        if (lengthStr.isEmpty()) {
            et_a_trip_length.setError("Please enter length");
            et_a_trip_length.requestFocus();
            return;
        }

        final double length;
        try {
            length = Double.parseDouble(lengthStr);
        } catch (NumberFormatException e) {
            et_a_trip_length.setError("Invalid length");
            et_a_trip_length.requestFocus();
            return;
        }

        if (length <= 0) {
            et_a_trip_length.setError("Length must be greater than 0");
            et_a_trip_length.requestFocus();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xác nhận thông tin");
        builder.setMessage(
                "Name: " + name + "\n" +
                        "Destination: " + destination + "\n" +
                        "Date: " + date + "\n" +
                        "Length: " + length + " km\n" +
                        "Parking require: " + (require_parking ? "Yes" : "No") + "\n" +
                        "Difficulty: " + difficulty
        );

        builder.setPositiveButton("Confirm", (dialog, which) -> {
            long result = db.addTrip(user_id, name, destination, date,
                    require_parking, length, difficulty, description);

            if (result != -1) {
                Toast.makeText(AddTripActivity.this,
                        "Add trip successfully!",
                        Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(AddTripActivity.this,
                        "Error when add trip!",
                        Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Edit", null);
        builder.show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
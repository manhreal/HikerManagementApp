package com.example.hikermanagementapp;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

public class UpdateTripActivity extends AppCompatActivity {

    private TextInputEditText et_u_trip_name, et_u_trip_destination, et_u_trip_date,
            et_u_trip_length, et_u_trip_description;
    private CheckBox cb_u_trip_parking;
    private Spinner sp_u_trip_difficulty;
    private MaterialButton btn_u_trip_update, btn_u_trip_cancel;
    private Database db;
    private int trip_id;
    private int user_id;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_trip);

        // Initialize views
        et_u_trip_name = findViewById(R.id.et_u_trip_name);
        et_u_trip_destination = findViewById(R.id.et_u_trip_destination);
        et_u_trip_date = findViewById(R.id.et_u_trip_date);
        et_u_trip_length = findViewById(R.id.et_u_trip_length);
        et_u_trip_description = findViewById(R.id.et_u_trip_description);
        cb_u_trip_parking = findViewById(R.id.cb_u_trip_parking);
        sp_u_trip_difficulty = findViewById(R.id.sp_u_trip_difficulty);
        btn_u_trip_update = findViewById(R.id.btn_u_trip_update);
        btn_u_trip_cancel = findViewById(R.id.btn_u_trip_cancel);

        db = new Database(this);
        sharedPreferences = getSharedPreferences("MHikePrefs", MODE_PRIVATE);
        user_id = sharedPreferences.getInt("user_id", -1);

        // Get trip_id from intent
        trip_id = getIntent().getIntExtra("trip_id", -1);

        // Setup difficulty spinner
        setupDifficultySpinner();

        // Load trip data
        if (trip_id != -1) {
            loadTripData();
        } else {
            Toast.makeText(this, "Error loading trip", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Date picker
        et_u_trip_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        // Update button
        btn_u_trip_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUpdateConfirmDialog();
            }
        });

        // Cancel button
        btn_u_trip_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setupDifficultySpinner() {
        String[] difficulties = {"Easy", "Medium", "Hard"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                difficulties
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_u_trip_difficulty.setAdapter(adapter);
    }

    private void loadTripData() {
        Cursor cursor = db.getTripById(trip_id);

        if (cursor != null && cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String destination = cursor.getString(cursor.getColumnIndexOrThrow("destination"));
            String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
            boolean require_parking = cursor.getInt(cursor.getColumnIndexOrThrow("require_parking")) == 1;
            double length = cursor.getDouble(cursor.getColumnIndexOrThrow("length"));
            String difficulty = cursor.getString(cursor.getColumnIndexOrThrow("difficulty"));
            String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));

            // Set data to views
            et_u_trip_name.setText(name);
            et_u_trip_destination.setText(destination);
            et_u_trip_date.setText(date);
            cb_u_trip_parking.setChecked(require_parking);
            et_u_trip_length.setText(String.valueOf(length));
            et_u_trip_description.setText(description);

            // Set spinner selection
            String[] difficulties = {"Easy", "Medium", "Hard"};
            for (int i = 0; i < difficulties.length; i++) {
                if (difficulties[i].equalsIgnoreCase(difficulty)) {
                    sp_u_trip_difficulty.setSelection(i);
                    break;
                }
            }

            cursor.close();
        } else {
            Toast.makeText(this, "Trip not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        String currentDate = et_u_trip_date.getText().toString();
        if (!currentDate.isEmpty()) {
            try {
                String[] parts = currentDate.split("/");
                if (parts.length == 3) {
                    int day = Integer.parseInt(parts[0]);
                    int month = Integer.parseInt(parts[1]) - 1; // Month from 0
                    int year = Integer.parseInt(parts[2]);
                    calendar.set(year, month, day);
                }
            } catch (Exception e) {
                e.printStackTrace();
                // todây
                calendar = Calendar.getInstance();
            }
        } else {
            // today
            calendar = Calendar.getInstance();
        }

        int mDay = calendar.get(Calendar.DAY_OF_MONTH);
        int mMonth = calendar.get(Calendar.MONTH) + 1;
        int mYear = calendar.get(Calendar.YEAR);

        et_u_trip_date.setText(String.format("%02d/%02d/%04d", mDay, mMonth, mYear));


        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String date = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year);
                        et_u_trip_date.setText(date);
                    }
                },
                year, month, day
        );

        datePickerDialog.show();
    }

    private void showUpdateConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Update");
        builder.setMessage("Are you sure you want to update this trip?");

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                updateTrip();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateTrip() {
        String name = et_u_trip_name.getText().toString().trim();
        String destination = et_u_trip_destination.getText().toString().trim();
        String date = et_u_trip_date.getText().toString().trim();
        String lengthStr = et_u_trip_length.getText().toString().trim();
        String description = et_u_trip_description.getText().toString().trim();
        boolean require_parking = cb_u_trip_parking.isChecked();
        String difficulty = sp_u_trip_difficulty.getSelectedItem().toString();

        // Validation
        if (name.isEmpty()) {
            et_u_trip_name.setError("Trip name is required");
            et_u_trip_name.requestFocus();
            return;
        }

        if (destination.isEmpty()) {
            et_u_trip_destination.setError("Destination is required");
            et_u_trip_destination.requestFocus();
            return;
        }

        if (date.isEmpty()) {
            et_u_trip_date.setError("Date is required");
            et_u_trip_date.requestFocus();
            return;
        }

        if (lengthStr.isEmpty()) {
            et_u_trip_length.setError("Length is required");
            et_u_trip_length.requestFocus();
            return;
        }

        double length;
        try {
            length = Double.parseDouble(lengthStr);
            if (length <= 0) {
                et_u_trip_length.setError("Length must be greater than 0");
                et_u_trip_length.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            et_u_trip_length.setError("Invalid length format");
            et_u_trip_length.requestFocus();
            return;
        }


        // Update trip in db
        boolean result = db.updateTrip(trip_id, name, destination, date, require_parking,
                length, difficulty, description);

        if (result) {
            Toast.makeText(this, "Trip updated successfully", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        }
        else {
            Toast.makeText(this, "Failed to update trip", Toast.LENGTH_SHORT).show();
        }
    }
}
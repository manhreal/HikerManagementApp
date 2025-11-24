package com.example.hikermanagementapp;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Calendar;

public class AddObservationActivity extends AppCompatActivity {

    private Spinner sp_a_obs_type;
    private TextInputEditText st_a_obs_time, et_a_obs_note;
    private Button btn_a_obs_add, btn_a_obs_cancel;
    private Database db;
    private int trip_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_observation);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Add Observation");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        trip_id = getIntent().getIntExtra("trip_id", -1);

        if (trip_id == -1) {
            Toast.makeText(this, "Error: Trip not found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = new Database(this);

        // Ánh xạ views
        sp_a_obs_type = findViewById(R.id.sp_a_obs_type);
        st_a_obs_time = findViewById(R.id.st_a_obs_time);
        et_a_obs_note = findViewById(R.id.et_a_obs_note);
        btn_a_obs_add = findViewById(R.id.btn_a_obs_add);
        btn_a_obs_cancel = findViewById(R.id.btn_a_obs_cancel);

        String[] observationTypes = {
                "Animal",
                "Plant",
                "View",
                "Weather",
                "Terrain",
                "Local culture",
                "Other"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, observationTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_a_obs_type.setAdapter(adapter);

        st_a_obs_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker();
            }
        });

        btn_a_obs_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveObservation();
            }
        });

        btn_a_obs_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String time = String.format("%02d:%02d", hourOfDay, minute);
                        st_a_obs_time.setText(time);
                    }
                }, hour, minute, true);

        timePickerDialog.show();
    }

    private void saveObservation() {
        String type = sp_a_obs_type.getSelectedItem().toString();
        String time = st_a_obs_time.getText().toString().trim();
        String note = et_a_obs_note.getText().toString().trim();

        if (time.isEmpty()) {
            st_a_obs_time.setError("Please pick time");
            st_a_obs_time.requestFocus();
            return;
        }

        if (note.isEmpty()) {
            et_a_obs_note.setError("Please enter note");
            et_a_obs_note.requestFocus();
            return;
        }

        if (note.length() < 10) {
            et_a_obs_note.setError("Note must be at least 10 characters");
            et_a_obs_note.requestFocus();
            return;
        }

        long result = db.addObservation(trip_id, type, time, note);

        if (result != -1) {
            Toast.makeText(AddObservationActivity.this,
                    "Add observation successfully!",
                    Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(AddObservationActivity.this,
                    "Error when add observation!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
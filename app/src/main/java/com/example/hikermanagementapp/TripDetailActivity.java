package com.example.hikermanagementapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class TripDetailActivity extends AppCompatActivity {

    private TextView tv_d_trip_name, tv_d_trip_destination, tv_d_trip_date, tv_d_trip_length,
            tv_d_trip_difficulty, tv_d_trip_require_parking, tv_d_trip_description;
    private Button btn_d_trip_edit, btn_d_trip_delete, btn_d_trip_add;
    private RecyclerView rv_observation;
    private LinearLayout observation_state_empty;
    private ObservationAdapter observationAdapter;
    private List<Observation> observationList;
    private Database db;
    private int trip_id;
    private Trip currentTrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_detail);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.d_trip_tool_bar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Trip detail");
        }

        // get trip_id from Intent
        trip_id = getIntent().getIntExtra("trip_id", -1);

        if (trip_id == -1) {
            Toast.makeText(this, "Error: Trip not found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = new Database(this);

        tv_d_trip_name = findViewById(R.id.tv_d_trip_name);
        tv_d_trip_destination = findViewById(R.id.tv_d_trip_destination);
        tv_d_trip_date = findViewById(R.id.tv_d_trip_date);
        tv_d_trip_length = findViewById(R.id.tv_d_trip_length);
        tv_d_trip_difficulty = findViewById(R.id.tv_d_trip_difficulty);
        tv_d_trip_require_parking = findViewById(R.id.tv_d_trip_require_parking);
        tv_d_trip_description = findViewById(R.id.tv_d_trip_description);
        btn_d_trip_edit = findViewById(R.id.btn_d_trip_edit);
        btn_d_trip_delete = findViewById(R.id.btn_d_trip_delete);
        btn_d_trip_add = findViewById(R.id.btn_d_trip_add);
        rv_observation = findViewById(R.id.rv_observation);
        observation_state_empty = findViewById(R.id.observation_state_empty);

        observationList = new ArrayList<>();
        observationAdapter = new ObservationAdapter(this, observationList);
        rv_observation.setLayoutManager(new LinearLayoutManager(this));
        rv_observation.setAdapter(observationAdapter);

        loadTripDetails();
        loadObservations();

        btn_d_trip_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Implement edit functionality
                Toast.makeText(TripDetailActivity.this,
                        "Chức năng sửa sẽ được cập nhật sau!",
                        Toast.LENGTH_SHORT).show();
            }
        });

        btn_d_trip_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmation();
            }
        });

        btn_d_trip_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddObservationDialog();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadObservations();
    }

    private void loadTripDetails() {
        Cursor cursor = db.getTripById(trip_id);

        if (cursor != null && cursor.moveToFirst()) {
            int user_id = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"));
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String destination = cursor.getString(cursor.getColumnIndexOrThrow("destination"));
            String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
            boolean require_parking = cursor.getInt(cursor.getColumnIndexOrThrow("require_parking")) == 1;
            double length = cursor.getDouble(cursor.getColumnIndexOrThrow("length"));
            String difficulty = cursor.getString(cursor.getColumnIndexOrThrow("difficulty"));
            String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));

            currentTrip = new Trip(trip_id, user_id, name, destination, date,
                    require_parking, length, difficulty, description);

            tv_d_trip_name.setText(name);
            tv_d_trip_destination.setText(destination);
            tv_d_trip_date.setText(date);
            tv_d_trip_length.setText(length + " km");
            tv_d_trip_difficulty.setText(difficulty);
            tv_d_trip_require_parking.setText(require_parking ? "Yes" : "No");
            tv_d_trip_description.setText(description.isEmpty() ? "No description" : description);

            switch (difficulty.toLowerCase()) {
                case "Easy":
                    tv_d_trip_difficulty.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    break;
                case "Medium":
                    tv_d_trip_difficulty.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                    break;
                case "Hard":
                    tv_d_trip_difficulty.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    break;
            }

            cursor.close();
        }
    }

    private void loadObservations() {
        observationList.clear();
        Cursor cursor = db.getObservations(trip_id);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                String time = cursor.getString(cursor.getColumnIndexOrThrow("time"));
                String note = cursor.getString(cursor.getColumnIndexOrThrow("note"));

                Observation observation = new Observation(id, trip_id, type, time, note);
                observationList.add(observation);
            } while (cursor.moveToNext());
            cursor.close();
        }

        if (observationList.isEmpty()) {
            rv_observation.setVisibility(View.GONE);
            observation_state_empty.setVisibility(View.VISIBLE);
        } else {
            rv_observation.setVisibility(View.VISIBLE);
            observation_state_empty.setVisibility(View.GONE);
        }

        observationAdapter.notifyDataSetChanged();
    }

    private void showDeleteConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete confirm");
        builder.setMessage("Are you sure you want to delete this trip?\nAll observations will also be deleted..");
        builder.setIcon(android.R.drawable.ic_dialog_alert);

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                boolean success = db.deleteTrip(trip_id);
                if (success) {
                    Toast.makeText(TripDetailActivity.this,
                            "Trip deleted successfully!",
                            Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(TripDetailActivity.this,
                            "Error when deleting trip!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showAddObservationDialog() {
        Intent intent = new Intent(TripDetailActivity.this, AddObservationActivity.class);
        intent.putExtra("trip_id", trip_id);
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
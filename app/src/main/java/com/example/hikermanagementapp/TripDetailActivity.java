package com.example.hikermanagementapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

public class TripDetailActivity extends AppCompatActivity {

    private TextView tv_d_trip_name, tv_d_trip_destination, tv_d_trip_date, tv_d_trip_length,
            tv_d_trip_difficulty, tv_d_trip_require_parking, tv_d_trip_description;
    private Button btn_d_trip_edit, btn_d_trip_delete, btn_d_trip_add;
    private RecyclerView rv_observation;
    private LinearLayout observation_state_empty, page_size_container;
    private CardView pagination_container, pagination_mode_card;
    private MaterialButton btn_prev_page, btn_next_page, btn_mode_all, btn_mode_pagination;
    private TextView tv_page_info, tv_items_info;
    private Spinner spinner_page_size;

    private ObservationAdapter observationAdapter;
    private List<Observation> observationList;
    private Database db;
    private int trip_id;
    private Trip currentTrip;

    // Pagination
    private boolean isPaginationMode = false; // false = View All, true = Pagination
    private int currentPage = 1;
    private int pageSize = 2; // Default
    private int totalItems = 0;
    private int totalPages = 0;

    // size options
    private Integer[] pageSizeOptions = {2, 3, 5, 10, 20};

    private static final int REQUEST_UPDATE_TRIP = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_detail);

        Toolbar toolbar = findViewById(R.id.d_trip_tool_bar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Trip detail");
        }

        trip_id = getIntent().getIntExtra("trip_id", -1);

        if (trip_id == -1) {
            Toast.makeText(this, "Error: Trip not found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = new Database(this);

        initViews();
        setupPageSizeSpinner();
        loadTripDetails();
        loadObservations();
        setupListeners();
    }

    private void initViews() {
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

        pagination_container = findViewById(R.id.pagination_container);
        pagination_mode_card = findViewById(R.id.pagination_mode_card);
        page_size_container = findViewById(R.id.page_size_container);
        btn_prev_page = findViewById(R.id.btn_prev_page);
        btn_next_page = findViewById(R.id.btn_next_page);
        tv_page_info = findViewById(R.id.tv_page_info);
        tv_items_info = findViewById(R.id.tv_items_info);
        btn_mode_all = findViewById(R.id.btn_mode_all);
        btn_mode_pagination = findViewById(R.id.btn_mode_pagination);
        spinner_page_size = findViewById(R.id.spinner_page_size);

        observationList = new ArrayList<>();
        observationAdapter = new ObservationAdapter(this, observationList);
        rv_observation.setLayoutManager(new LinearLayoutManager(this));
        rv_observation.setAdapter(observationAdapter);
    }

    private void setupPageSizeSpinner() {
        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                pageSizeOptions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_page_size.setAdapter(adapter);

        spinner_page_size.setSelection(0);

        spinner_page_size.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                pageSize = pageSizeOptions[position];
                currentPage = 1;
                loadObservations();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupListeners() {
        // Mode toggle buttons
        btn_mode_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPaginationMode) return;

                isPaginationMode = false;
                updateModeUI();
                currentPage = 1;
                loadObservations();
            }
        });

        btn_mode_pagination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPaginationMode) return;

                isPaginationMode = true;
                updateModeUI();
                currentPage = 1;
                loadObservations();
            }
        });

        // Pagination buttons
        btn_prev_page.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPage > 1) {
                    currentPage--;
                    loadObservations();
                }
            }
        });

        btn_next_page.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPage < totalPages) {
                    currentPage++;
                    loadObservations();
                }
            }
        });

        // Trip action buttons
        btn_d_trip_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TripDetailActivity.this, UpdateTripActivity.class);
                intent.putExtra("trip_id", trip_id);
                startActivityForResult(intent, REQUEST_UPDATE_TRIP);            }
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

    private void updateModeUI() {
        if (isPaginationMode) {
            // Pagination mode
            btn_mode_pagination.setBackgroundTintList(
                    getResources().getColorStateList(android.R.color.holo_blue_dark, null)
            );
            btn_mode_all.setBackgroundTintList(
                    getResources().getColorStateList(android.R.color.darker_gray, null)
            );
            btn_mode_pagination.setAlpha(1.0f);
            btn_mode_all.setAlpha(0.6f);

            page_size_container.setVisibility(View.VISIBLE);
        } else {
            // View All mode
            btn_mode_all.setBackgroundTintList(
                    getResources().getColorStateList(android.R.color.holo_green_dark, null)
            );
            btn_mode_pagination.setBackgroundTintList(
                    getResources().getColorStateList(android.R.color.darker_gray, null)
            );
            btn_mode_all.setAlpha(1.0f);
            btn_mode_pagination.setAlpha(0.6f);

            page_size_container.setVisibility(View.GONE);
            pagination_container.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        currentPage = 1;
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
                case "easy":
                    tv_d_trip_difficulty.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    break;
                case "medium":
                    tv_d_trip_difficulty.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                    break;
                case "hard":
                    tv_d_trip_difficulty.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    break;
            }

            cursor.close();
        }
    }

    private void loadObservations() {
        observationList.clear();

        totalItems = db.getObservationsCount(trip_id);

        Cursor cursor;

        if (isPaginationMode) {
            // Pagination mode
            totalPages = (int) Math.ceil((double) totalItems / pageSize);

            if (currentPage < 1) currentPage = 1;
            if (currentPage > totalPages && totalPages > 0) currentPage = totalPages;

            cursor = db.getObservationsPaginated(trip_id, currentPage, pageSize);
        } else {
            // View All mode
            cursor = db.getObservations(trip_id);
        }

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                String time = cursor.getString(cursor.getColumnIndexOrThrow("time"));
                String note = cursor.getString(cursor.getColumnIndexOrThrow("note"));
                String image = cursor.getString(cursor.getColumnIndexOrThrow("image"));

                Observation observation = new Observation(id, trip_id, type, time, note, image);
                observationList.add(observation);
            } while (cursor.moveToNext());
            cursor.close();
        }

        if (observationList.isEmpty() && totalItems == 0) {
            rv_observation.setVisibility(View.GONE);
            observation_state_empty.setVisibility(View.VISIBLE);
            pagination_mode_card.setVisibility(View.GONE);
            pagination_container.setVisibility(View.GONE);
        } else {
            rv_observation.setVisibility(View.VISIBLE);
            observation_state_empty.setVisibility(View.GONE);
            pagination_mode_card.setVisibility(View.VISIBLE);

            if (isPaginationMode && totalPages > 1) {
                pagination_container.setVisibility(View.VISIBLE);
                updatePaginationUI();
            } else {
                pagination_container.setVisibility(View.GONE);
            }
        }

        observationAdapter.notifyDataSetChanged();
    }

    private void updatePaginationUI() {
        tv_page_info.setText("Page " + currentPage + " / " + totalPages);

        // range items
        int startItem = (currentPage - 1) * pageSize + 1;
        int endItem = Math.min(currentPage * pageSize, totalItems);
        tv_items_info.setText("(" + startItem + "-" + endItem + " of " + totalItems + " items)");

        btn_prev_page.setEnabled(currentPage > 1);
        btn_next_page.setEnabled(currentPage < totalPages);

        if (currentPage <= 1) {
            btn_prev_page.setAlpha(0.5f);
        } else {
            btn_prev_page.setAlpha(1.0f);
        }

        if (currentPage >= totalPages) {
            btn_next_page.setAlpha(0.5f);
        } else {
            btn_next_page.setAlpha(1.0f);
        }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_UPDATE_TRIP && resultCode == RESULT_OK) {
            // Reload trip info and observations
            loadTripDetails();
            loadObservations();
        }
    }

}
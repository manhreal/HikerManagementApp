package com.example.hikermanagementapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rv_trip;
    private TripAdapter adapter;
    private List<Trip> trip_list;
    private Database db;
    private SharedPreferences sharedPreferences;
    private int user_id;
    private TextInputEditText et_trip_search;
    private LinearLayout trip_state_empty;
    private FloatingActionButton btn_trip_add;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.trip_tool_bar);
        setSupportActionBar(toolbar);

        sharedPreferences = getSharedPreferences("MHikePrefs", MODE_PRIVATE);
        user_id = sharedPreferences.getInt("user_id", -1);
        String email = sharedPreferences.getString("email", "User");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(email);
        }

        db = new Database(this);
        trip_list = new ArrayList<>();

        rv_trip = findViewById(R.id.rv_trip);
        et_trip_search = findViewById(R.id.et_trip_search);
        trip_state_empty = findViewById(R.id.trip_state_empty);
        btn_trip_add = findViewById(R.id.btn_trip_add);

        rv_trip.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TripAdapter(this, trip_list);
        rv_trip.setAdapter(adapter);

        loadTrips();

        btn_trip_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddTripActivity.class);
                startActivity(intent);
            }
        });

        et_trip_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchTrips(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTrips();
    }

    private void loadTrips() {
        trip_list.clear();
        Cursor cursor = db.getAllTripsByUser(user_id);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String destination = cursor.getString(cursor.getColumnIndexOrThrow("destination"));
                String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                boolean require_parking = cursor.getInt(cursor.getColumnIndexOrThrow("require_parking")) == 1;
                double length = cursor.getDouble(cursor.getColumnIndexOrThrow("length"));
                String difficulty = cursor.getString(cursor.getColumnIndexOrThrow("difficulty"));
                String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));

                Trip trip = new Trip(id, user_id, name, destination, date,
                        require_parking, length, difficulty, description);
                trip_list.add(trip);
            } while (cursor.moveToNext());
            cursor.close();
        }

        if (trip_list.isEmpty()) {
            rv_trip.setVisibility(View.GONE);
            trip_state_empty.setVisibility(View.VISIBLE);
        } else {
            rv_trip.setVisibility(View.VISIBLE);
            trip_state_empty.setVisibility(View.GONE);
        }

        adapter.notifyDataSetChanged();
    }

    private void searchTrips(String keyword) {
        if (keyword.isEmpty()) {
            loadTrips();
            return;
        }

        trip_list.clear();
        Cursor cursor = db.searchTripByName(user_id, keyword);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String destination = cursor.getString(cursor.getColumnIndexOrThrow("destination"));
                String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                boolean require_parking = cursor.getInt(cursor.getColumnIndexOrThrow("require_parking")) == 1;
                double length = cursor.getDouble(cursor.getColumnIndexOrThrow("length"));
                String difficulty = cursor.getString(cursor.getColumnIndexOrThrow("difficulty"));
                String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));

                Trip trip = new Trip(id, user_id, name, destination, date,
                        require_parking, length, difficulty, description);
                trip_list.add(trip);
            } while (cursor.moveToNext());
            cursor.close();
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            logout();
            return true;
        }
        if (id == R.id.action_profile) {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
package com.example.hikermanagementapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Calendar;

public class AddObservationActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;
    private static final int CAMERA_PERMISSION_CODE = 100;

    private Spinner sp_a_obs_type;
    private TextInputEditText st_a_obs_time, et_a_obs_note;
    private MaterialButton btn_a_obs_add, btn_a_obs_cancel, btn_add_image;
    private ImageView img_preview;
    private MaterialCardView card_image_preview;
    private Database db;
    private int trip_id;
    private String imagePath = null;

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

        // Initialize views
        sp_a_obs_type = findViewById(R.id.sp_a_obs_type);
        st_a_obs_time = findViewById(R.id.st_a_obs_time);
        et_a_obs_note = findViewById(R.id.et_a_obs_note);
        btn_a_obs_add = findViewById(R.id.btn_a_obs_add);
        btn_a_obs_cancel = findViewById(R.id.btn_a_obs_cancel);
        btn_add_image = findViewById(R.id.btn_add_image);
        img_preview = findViewById(R.id.img_preview);
        card_image_preview = findViewById(R.id.card_image_preview);

        // Setup spinner
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

        // Time picker
        st_a_obs_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker();
            }
        });

        // Add image button
        btn_add_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageSourceDialog();
            }
        });

        // Add observation button
        btn_a_obs_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveObservation();
            }
        });

        // Cancel button
        btn_a_obs_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Remove image on long click
        img_preview.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new AlertDialog.Builder(AddObservationActivity.this)
                        .setTitle("Remove Image")
                        .setMessage("Do you want to remove this image?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                removeImage();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
                return true;
            }
        });
    }

    private void showImageSourceDialog() {
        String[] options = {"Take Photo", "Choose from Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Image")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            // Camera
                            if (checkCameraPermission()) {
                                openCamera();
                            } else {
                                requestCameraPermission();
                            }
                        } else {
                            // Gallery
                            openGallery();
                        }
                    }
                })
                .show();
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                CAMERA_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, CAMERA_REQUEST);
        } else {
            Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            try {
                Bitmap bitmap = null;

                if (requestCode == CAMERA_REQUEST) {
                    // Get photo from camera
                    bitmap = (Bitmap) data.getExtras().get("data");
                } else if (requestCode == PICK_IMAGE_REQUEST) {
                    // Get photo from gallery
                    Uri imageUri = data.getData();
                    InputStream inputStream = getContentResolver().openInputStream(imageUri);
                    bitmap = BitmapFactory.decodeStream(inputStream);
                }

                if (bitmap != null) {
                    // Save image to internal storage
                    String fileName = "obs_" + System.currentTimeMillis() + ".jpg";
                    File file = new File(getFilesDir(), fileName);
                    FileOutputStream outputStream = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
                    outputStream.close();

                    imagePath = file.getAbsolutePath();

                    // Display preview
                    img_preview.setImageBitmap(bitmap);
                    card_image_preview.setVisibility(View.VISIBLE);

                    Toast.makeText(this, "Image added successfully", Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void removeImage() {
        imagePath = null;
        img_preview.setImageResource(0);
        card_image_preview.setVisibility(View.GONE);
        Toast.makeText(this, "Image removed", Toast.LENGTH_SHORT).show();
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

        // Validation
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

        // Save observation with image
        long result = db.addObservation(trip_id, type, time, note, imagePath);

        if (result != -1) {
            Toast.makeText(this, "Observation added successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Error adding observation!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
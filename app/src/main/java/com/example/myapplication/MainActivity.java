package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends BaseActivity {

    private ActivityMainBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private ValueEventListener lecturasListener;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_main);
        binding = ActivityMainBinding.bind(findViewById(R.id.main));

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();

        // Initialize Firebase listener setup
        setupFirebaseListener();

        // Setup Bottom Navigation (from BaseActivity)
        setupBottomNavigation();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
            return;
        }

        if (databaseReference != null && lecturasListener != null) {
            databaseReference.addValueEventListener(lecturasListener);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (databaseReference != null && lecturasListener != null) {
            databaseReference.removeEventListener(lecturasListener);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_help) {
            startActivity(new Intent(this, HelpActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupFirebaseListener() {
        databaseReference = FirebaseDatabase.getInstance().getReference("sensor_status/actual");

        lecturasListener = new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Double tempAgua = parseDouble(snapshot.child("tempAgua").getValue());
                    Double ph = parseDouble(snapshot.child("phVoltaje").getValue());
                    Double tempAire = parseDouble(snapshot.child("tempAire").getValue());
                    Double humedadAire = parseDouble(snapshot.child("humedadAire").getValue());

                    if (tempAgua != null && ph != null && tempAire != null && humedadAire != null) {
                        binding.tempGauge.setValue(tempAgua);
                        binding.phGauge.setValue(ph);
                        binding.tempAireGauge.setValue(tempAire);
                        binding.humedadAireGauge.setValue(humedadAire);

                        updateGaugeProgress(tempAgua, ph, tempAire, humedadAire);
                    } else {
                        Log.w("Firebase", "Datos incompletos en sensor_status/actual");
                    }
                } else {
                    Log.w("Firebase", "El nodo 'sensor_status/actual' no existe");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("FirebaseError", "Fallo al leer los datos.", error.toException());
                Toast.makeText(MainActivity.this, "Error al leer datos de Firebase", Toast.LENGTH_SHORT).show();
            }
        };
    }

    private Double parseDouble(Object value) {
        if (value == null)
            return null;
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private void updateGaugeProgress(Double tempAgua, Double ph, Double tempAire, Double humedadAire) {
        if (tempAgua != null) {
            int tempAguaMax = 40;
            binding.tempGauge.setMax(tempAguaMax);
            binding.tempGauge.setProgress(tempAgua.intValue());
        }

        if (ph != null) {
            int phMax = 140;
            binding.phGauge.setMax(phMax);
            binding.phGauge.setProgress((int) (ph * 10));
        }

        if (tempAire != null) {
            int tempAireMax = 40;
            binding.tempAireGauge.setMax(tempAireMax);
            binding.tempAireGauge.setProgress(tempAire.intValue());
        }

        if (humedadAire != null) {
            int humedadAireMax = 100;
            binding.humedadAireGauge.setMax(humedadAireMax);
            binding.humedadAireGauge.setProgress(humedadAire.intValue());
        }
    }
}
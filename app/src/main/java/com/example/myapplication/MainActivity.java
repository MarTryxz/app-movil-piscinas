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
                        String tempAguaStr = String.format("%.1f", tempAgua);
                        String phStr = String.format("%.2f", ph);
                        String tempAireStr = String.format("%.1f", tempAire);
                        String humedadAireStr = String.format("%.1f", humedadAire);

                        binding.tempGauge.tvGaugeValue.setText(tempAguaStr + "°");
                        binding.phGauge.tvGaugeValue.setText(phStr);
                        binding.tempAireGauge.tvGaugeValue.setText(tempAireStr + "°");
                        binding.humedadAireGauge.tvGaugeValue.setText(humedadAireStr + "%");

                        updateGaugeProgress(tempAguaStr, phStr, tempAireStr, humedadAireStr);
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

    private void updateGaugeProgress(String tempAguaStr, String phStr, String tempAireStr, String humedadAireStr) {
        try {
            float tempAgua = Float.parseFloat(tempAguaStr);
            float ph = Float.parseFloat(phStr);
            float tempAire = Float.parseFloat(tempAireStr);
            float humedadAire = Float.parseFloat(humedadAireStr);

            int tempAguaMax = 40;
            binding.tempGauge.gaugeProgressBar.setMax(tempAguaMax);
            binding.tempGauge.gaugeProgressBar.setProgress((int) tempAgua);

            int phMax = 140;
            binding.phGauge.gaugeProgressBar.setMax(phMax);
            binding.phGauge.gaugeProgressBar.setProgress((int) (ph * 10));

            int tempAireMax = 40;
            binding.tempAireGauge.gaugeProgressBar.setMax(tempAireMax);
            binding.tempAireGauge.gaugeProgressBar.setProgress((int) tempAire);

            int humedadAireMax = 100;
            binding.humedadAireGauge.gaugeProgressBar.setMax(humedadAireMax);
            binding.humedadAireGauge.gaugeProgressBar.setProgress((int) humedadAire);

        } catch (NumberFormatException e) {
            Log.e("GaugeError", "No se pudo convertir el valor para la barra de progreso", e);
        }
    }
}
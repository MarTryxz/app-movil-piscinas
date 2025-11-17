package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
// import android.content.SharedPreferences; // Ya no se usa para la sesión
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.databinding.ActivityMainBinding;

// --- AÑADIR IMPORTACIONES DE FIREBASE AUTH ---
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
// ---
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    // private SharedPreferences sharedPreferences; // Eliminado

    // --- AÑADIR FIREBASE AUTH ---
    private FirebaseAuth mAuth;

    private DatabaseReference databaseReference;
    private ValueEventListener lecturasListener;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        // --- INICIALIZAR FIREBASE AUTH ---
        mAuth = FirebaseAuth.getInstance();

        // --- BLOQUE DE SESIÓN DE SHARED PREFERENCES (ELIMINADO) ---
        /*
         * sharedPreferences = getSharedPreferences("MyappName", MODE_PRIVATE);
         * if (sharedPreferences.getString("logged", "false").equals("false") ||
         * sharedPreferences.getString("name", "").isEmpty()) {
         * Intent intent = new Intent(getApplicationContext(), Login.class);
         * startActivity(intent);
         * finish();
         * return;
         * }
         */
        // --- FIN DE LA ELIMINACIÓN ---

        // Configurar etiquetas de los medidores (esto está bien)
        binding.phGauge.tvGaugeLabel.setText("Nivel de pH");
        binding.tempGauge.tvGaugeLabel.setText("Temp. Agua");
        binding.tempAireGauge.tvGaugeLabel.setText("Temp. Aire");
        binding.humedadAireGauge.tvGaugeLabel.setText("Humedad Aire");

        // Inicializar Firebase y apuntar al nodo "lecturas"
        databaseReference = FirebaseDatabase.getInstance().getReference("lecturas");

        // Definir el listener que reaccionará a los cambios
        setupFirebaseListener();

        // Configurar el listener de la barra de navegación
        setupBottomNavigation();
    }

    // --- onStart() MODIFICADO ---
    @Override
    protected void onStart() {
        super.onStart();

        // --- AÑADIR NUEVA COMPROBACIÓN DE SESIÓN ---
        // Este es el lugar correcto. Se ejecuta cada vez que la pantalla se vuelve
        // visible.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // No hay usuario logueado en Firebase, regresamos a Login.
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish(); // Cerramos MainActivity
            return; // Detenemos la ejecución de onStart
        }

        // Si el usuario SÍ está logueado, adjuntamos el listener de la base de datos
        if (databaseReference != null && lecturasListener != null) {
            databaseReference.addValueEventListener(lecturasListener);
        }
    }

    // onStop (Sin cambios)
    @Override
    protected void onStop() {
        super.onStop();
        if (databaseReference != null && lecturasListener != null) {
            databaseReference.removeEventListener(lecturasListener);
        }
    }

    // In MainActivity.java, add this method outside of any other methods
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.nav_help) {
            startActivity(new Intent(this, HelpActivity.class));
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    // setupFirebaseListener (Sin cambios, ya está correcto para 4 valores)
    private void setupFirebaseListener() {
        lecturasListener = new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Double tempAgua = snapshot.child("tempAgua").getValue(Double.class);
                    Double ph = snapshot.child("phVoltaje").getValue(Double.class);
                    Double tempAire = snapshot.child("tempAire").getValue(Double.class);
                    Double humedadAire = snapshot.child("humedadAire").getValue(Double.class);

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
                    }
                } else {
                    Log.w("Firebase", "El nodo 'lecturas' no existe");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("FirebaseError", "Fallo al leer los datos.", error.toException());
                Toast.makeText(MainActivity.this, "Error al leer datos de Firebase", Toast.LENGTH_SHORT).show();
            }

            
        };
    }

    // setupBottomNavigation (Sin cambios)
    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_monitor) {
                return true;
            } else if (itemId == R.id.nav_history) {
                startActivity(new Intent(this, TemperatureHistoryActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileEditor.class));
                return true;
            } else if (itemId == R.id.nav_help) {
                startActivity(new Intent(this, HelpActivity.class));
                return true;
            }
            return false;
        });
    }

    // updateGaugeProgress (Sin cambios, ya está correcto para 4 valores)
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
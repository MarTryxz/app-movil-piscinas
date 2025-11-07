package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SharedPreferences sharedPreferences;

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

        sharedPreferences = getSharedPreferences("MyappName", MODE_PRIVATE);

        // Revisar sesión
        if (sharedPreferences.getString("logged", "false").equals("false") || sharedPreferences.getString("name", "").isEmpty()) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
            return;
        }

        // --- CAMBIO: Añadir etiquetas para los nuevos medidores ---
        binding.phGauge.tvGaugeLabel.setText("Nivel de pH");
        binding.tempGauge.tvGaugeLabel.setText("Temp. Agua");
        binding.tempAireGauge.tvGaugeLabel.setText("Temp. Aire");
        binding.humedadAireGauge.tvGaugeLabel.setText("Humedad Aire");

        // 2. Inicializar Firebase y apuntar al nodo "lecturas"
        databaseReference = FirebaseDatabase.getInstance().getReference("lecturas");

        // 3. Definir el listener que reaccionará a los cambios
        setupFirebaseListener();

        // Configurar el listener de la barra de navegación
        setupBottomNavigation();
    }

    // 4. onStart (Sin cambios)
    @Override
    protected void onStart() {
        super.onStart();
        if (databaseReference != null && lecturasListener != null) {
            databaseReference.addValueEventListener(lecturasListener);
        }
    }

    // 5. onStop (Sin cambios)
    @Override
    protected void onStop() {
        super.onStop();
        if (databaseReference != null && lecturasListener != null) {
            databaseReference.removeEventListener(lecturasListener);
        }
    }

    // 6. --- CAMBIO: Actualizado para leer los 4 valores ---
    private void setupFirebaseListener() {
        lecturasListener = new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Leemos los 4 valores
                    Double tempAgua = snapshot.child("tempAgua").getValue(Double.class);
                    Double ph = snapshot.child("phVoltaje").getValue(Double.class);
                    Double tempAire = snapshot.child("tempAire").getValue(Double.class);
                    Double humedadAire = snapshot.child("humedadAire").getValue(Double.class);

                    // Verificamos que los valores no sean nulos
                    if (tempAgua != null && ph != null && tempAire != null && humedadAire != null) {
                        // Convertimos a String para la UI
                        String tempAguaStr = String.format("%.1f", tempAgua); // Formato con 1 decimal
                        String phStr = String.format("%.2f", ph); // Formato con 2 decimales
                        String tempAireStr = String.format("%.1f", tempAire);
                        String humedadAireStr = String.format("%.1f", humedadAire);

                        // Actualizar los 4 TextViews de los medidores
                        binding.tempGauge.tvGaugeValue.setText(tempAguaStr + "°");
                        binding.phGauge.tvGaugeValue.setText(phStr);
                        binding.tempAireGauge.tvGaugeValue.setText(tempAireStr + "°");
                        binding.humedadAireGauge.tvGaugeValue.setText(humedadAireStr + "%"); // Añadimos "%"

                        // Actualizar las 4 barras de progreso
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

    // 7. setupBottomNavigation (Sin cambios)
    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_monitor) {
                return true;
            } else if (itemId == R.id.nav_history) {
                Intent intent = new Intent(getApplicationContext(), TemperatureHistoryActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_profile) {
                Intent intent = new Intent(getApplicationContext(), ProfileEditor.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_help) {
                Toast.makeText(this, "Pantalla de Ayuda (próximamente)", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }

    // 8. --- CAMBIO: Actualizado para procesar los 4 valores ---
    private void updateGaugeProgress(String tempAguaStr, String phStr, String tempAireStr, String humedadAireStr) {
        try {
            float tempAgua = Float.parseFloat(tempAguaStr);
            float ph = Float.parseFloat(phStr);
            float tempAire = Float.parseFloat(tempAireStr);
            float humedadAire = Float.parseFloat(humedadAireStr);

            // Rango de Temp. Agua: 0°C a 40°C
            int tempAguaMax = 40;
            binding.tempGauge.gaugeProgressBar.setMax(tempAguaMax);
            binding.tempGauge.gaugeProgressBar.setProgress((int) tempAgua);

            // Rango de pH: 0 a 14 (multiplicamos por 10 para más precisión en int)
            int phMax = 140;
            binding.phGauge.gaugeProgressBar.setMax(phMax);
            binding.phGauge.gaugeProgressBar.setProgress((int) (ph * 10));

            // Rango de Temp. Aire: 0°C a 40°C (Asumiendo)
            int tempAireMax = 40;
            binding.tempAireGauge.gaugeProgressBar.setMax(tempAireMax);
            binding.tempAireGauge.gaugeProgressBar.setProgress((int) tempAire);

            // Rango de Humedad Aire: 0% a 100% (Asumiendo)
            int humedadAireMax = 100;
            binding.humedadAireGauge.gaugeProgressBar.setMax(humedadAireMax);
            binding.humedadAireGauge.gaugeProgressBar.setProgress((int) humedadAire);

        } catch (NumberFormatException e) {
            Log.e("GaugeError", "No se pudo convertir el valor para la barra de progreso", e);
        }
    }
}
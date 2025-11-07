package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.myapplication.databinding.ActivityMainBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SharedPreferences sharedPreferences;
    private RequestQueue queue;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // 1. Inflar la nueva vista
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 2. Aplicar insets al 'main' (el ID que agregamos al root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0); // Padding inferior manejado por BottomNav
            return insets;
        });

        sharedPreferences = getSharedPreferences("MyappName", MODE_PRIVATE);

        // 3. Revisar sesión (esto sigue igual)
        if (sharedPreferences.getString("logged", "false").equals("false") || sharedPreferences.getString("name", "").isEmpty()) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
            return;
        }

        // 4. Configurar etiquetas de los medidores (gauges)
        binding.phGauge.tvGaugeLabel.setText("Nivel de pH");
        binding.tempGauge.tvGaugeLabel.setText("Temperatura");

        // 5. Inicializar Volley y buscar datos
        queue = Volley.newRequestQueue(getApplicationContext());
        fetchDashboardData();

        // 6. Configurar el listener de la barra de navegación
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_monitor) {
                // Ya estamos aquí, pero podemos refrescar
                fetchDashboardData();
                return true;
            } else if (itemId == R.id.nav_history) {
                // Ir a la actividad de historial (Tú decides cuál)
                Intent intent = new Intent(getApplicationContext(), TemperatureHistoryActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_profile) {
                // Ir a la actividad de perfil
                Intent intent = new Intent(getApplicationContext(), ProfileEditor.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_help) {
                // Mostrar un Toast o una pantalla de ayuda
                Toast.makeText(this, "Pantalla de Ayuda (próximamente)", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }

    private void fetchDashboardData() {
        String url = "http://192.168.1.6/backendpiscina/datos_main.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d("Response", response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");
                        if (status.equals("success")) {
                            String temperaturaStr = jsonObject.getString("temperatura");
                            String phStr = jsonObject.getString("ph");

                            // Actualizar los TextViews de los medidores
                            binding.tempGauge.tvGaugeValue.setText(temperaturaStr + "°");
                            binding.phGauge.tvGaugeValue.setText(phStr);

                            // Actualizar las barras de progreso
                            updateGaugeProgress(temperaturaStr, phStr);

                        } else {
                            String message = jsonObject.getString("message");
                            Toast.makeText(MainActivity.this, "Error: " + message, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(MainActivity.this, "Error de respuesta: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                },
                error -> Toast.makeText(MainActivity.this, "Error de conexión: " + error.getMessage(), Toast.LENGTH_LONG).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("apiKey", sharedPreferences.getString("apiKey", ""));
                return params;
            }
        };

        queue.add(stringRequest);
    }

    private void updateGaugeProgress(String temperaturaStr, String phStr) {
        try {
            float temperatura = Float.parseFloat(temperaturaStr);
            float ph = Float.parseFloat(phStr);

            // Asumimos un rango para las barras de progreso
            // Rango de Temperatura: 0°C a 40°C
            int tempMax = 40;
            binding.tempGauge.gaugeProgressBar.setMax(tempMax);
            binding.tempGauge.gaugeProgressBar.setProgress((int) temperatura);

            // Rango de pH: 0 a 14 (multiplicamos por 10 para más precisión en int)
            int phMax = 140;
            binding.phGauge.gaugeProgressBar.setMax(phMax);
            binding.phGauge.gaugeProgressBar.setProgress((int) (ph * 10));

        } catch (NumberFormatException e) {
            Log.e("GaugeError", "No se pudo convertir el valor para la barra de progreso", e);
        }
    }

    // Ya no necesitas la función logoutUser() aquí,
    // es mejor ponerla dentro de ProfileEditor.class
}
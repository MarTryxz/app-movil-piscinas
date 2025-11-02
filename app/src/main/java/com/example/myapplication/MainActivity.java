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
    private String temperatura, ph;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sharedPreferences = getSharedPreferences("MyappName", MODE_PRIVATE);

        if (sharedPreferences.getString("logged", "false").equals("false") || sharedPreferences.getString("name", "").isEmpty()) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
            return; // Return to prevent further execution
        }

        binding.name.setText("👋 ¡Hola, " + sharedPreferences.getString("name", "") + "! 🏊");

        queue = Volley.newRequestQueue(getApplicationContext());
        fetchDashboardData();

        binding.refreshTemperatureButton.setOnClickListener(v -> fetchDashboardData());
        binding.refreshPhButton.setOnClickListener(v -> fetchDashboardData());

        binding.logout.setOnClickListener(v -> logoutUser());

        binding.userSettingsButton.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), ProfileEditor.class);
            startActivity(intent);
        });

        binding.modifyScheduleButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), PumpTimerSettingsActivity.class);
            startActivity(intent);
        });

        binding.TempCard.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), TemperatureHistoryActivity.class);
            startActivity(intent);
        });

        binding.PhCard.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), PhView.class);
            startActivity(intent);
        });
    }

    private void fetchDashboardData() {
        String url = "http://192.168.100.91/backendpiscina/datos_main.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d("Response", response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");
                        if (status.equals("success")) {
                            temperatura = jsonObject.getString("temperatura");
                            ph = jsonObject.getString("ph");
                            binding.poolTemperatureTextView.setText(temperatura + "°");
                            binding.phLevelTextView.setText(ph);
                            String schedule1 = jsonObject.getString("hora_inicio");
                            String schedule2 = jsonObject.getString("hora_fin");
                            binding.pumpScheduleTextView.setText(schedule1 + " - " + schedule2);
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

    private void logoutUser() {
        String url = "http://192.168.100.91/backendpiscina/logout.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    if (response.equals("success")) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.clear();
                        editor.putString("logged", "false");
                        editor.apply();

                        Intent intent = new Intent(getApplicationContext(), Login.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(MainActivity.this, "Error al cerrar sesión: " + response, Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(MainActivity.this, "No se pudo cerrar sesión. Verifique su conexión.", Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", sharedPreferences.getString("email", ""));
                params.put("apiKey", sharedPreferences.getString("apiKey", ""));
                return params;
            }
        };
        queue.add(stringRequest);
    }
}

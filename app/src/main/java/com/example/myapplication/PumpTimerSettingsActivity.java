package com.example.myapplication;

import android.util.Log;
import android.widget.TimePicker;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class PumpTimerSettingsActivity extends AppCompatActivity {

    private TextView userIdTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pump_timer_settings);

        // Inicializar el TextView para mostrar la ID del cliente
        userIdTextView = findViewById(R.id.userIdTextView);

        // Obtener y mostrar la ID del cliente
        SharedPreferences sharedPreferences = getSharedPreferences("MyappName", MODE_PRIVATE);
        String clientId = sharedPreferences.getString("id_cliente", "No disponible");

        // Mostrar la ID del cliente
        userIdTextView.setText(clientId);

        // Configurar el botón de retroceso
        ImageButton backButton = findViewById(R.id.backToDashboardButton);
        backButton.setOnClickListener(v -> finish());

        // Configurar el botón de cierre de sesión
        Button logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> logoutUser());

        // Inicializar los TimePickers
        TimePicker startTimePicker = findViewById(R.id.startTimePicker);
        TimePicker endTimePicker = findViewById(R.id.endTimePicker);

        // Configurar los TimePickers para usar el formato de 24 horas
        startTimePicker.setIs24HourView(true);
        endTimePicker.setIs24HourView(true);

        // Establecer la hora y minuto predeterminados a 00:00
        startTimePicker.setHour(0);
        startTimePicker.setMinute(0);
        endTimePicker.setHour(0);
        endTimePicker.setMinute(0);

        // Botón para guardar la configuración
        Button saveButton = findViewById(R.id.saveTimerSettingsButton);
        saveButton.setOnClickListener(v -> saveTimerConfig(startTimePicker, endTimePicker));
    }

    // En el metodo saveTimerConfig, cambia la forma en que se pasan los parámetros al servidor.
    private void saveTimerConfig(TimePicker startTimePicker, TimePicker endTimePicker) {
        // Obtener las horas y minutos seleccionados
        int startHour = startTimePicker.getHour();
        int startMinute = startTimePicker.getMinute();
        int endHour = endTimePicker.getHour();
        int endMinute = endTimePicker.getMinute();

        // Convertir a formato de 24 horas
        String horaInicio = String.format("%02d:%02d:00", startHour, startMinute);
        String horaFin = String.format("%02d:%02d:00", endHour, endMinute);

        // Obtener el ID de cliente desde SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyappName", MODE_PRIVATE);
        String clientId = sharedPreferences.getString("id_cliente", "No disponible");

        if ("No disponible".equals(clientId)) {
            Toast.makeText(PumpTimerSettingsActivity.this, "Cliente no encontrado", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("ClientId", "ClientId: " + clientId);  // Depuración para el clientId

        // Realizar la solicitud al servidor para guardar la configuración
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = "http://192.168.100.91/backendpiscina/savePumpTimerConfig.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    if ("success_insert".equals(response)) {
                        Toast.makeText(PumpTimerSettingsActivity.this, "Configuración guardada", Toast.LENGTH_SHORT).show();
                    } else if ("success_update".equals(response)) {
                        Toast.makeText(PumpTimerSettingsActivity.this, "Configuración actualizada", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(PumpTimerSettingsActivity.this, "Error: " + response, Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(PumpTimerSettingsActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id_cliente", clientId);  // Pasamos el id_cliente directamente desde SharedPreferences
                params.put("hora_inicio", horaInicio);
                params.put("hora_fin", horaFin);
                return params;
            }
        };

        queue.add(stringRequest);
    }

    private void logoutUser() {
        // Obtener los datos del usuario desde SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyappName", MODE_PRIVATE);
        String email = sharedPreferences.getString("email", "");
        String apiKey = sharedPreferences.getString("apiKey", "");

        if (email.isEmpty() || apiKey.isEmpty()) {
            Toast.makeText(PumpTimerSettingsActivity.this, "No se encontraron datos del usuario", Toast.LENGTH_SHORT).show();
            return;
        }

        // Realizar la petición de cierre de sesión al servidor
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = "http://192.168.100.91/backendpiscina/logout.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    if ("success".equals(response)) {
                        clearUserData(sharedPreferences);
                        navigateToLogin();
                    } else {
                        Toast.makeText(PumpTimerSettingsActivity.this, response, Toast.LENGTH_SHORT).show();
                        clearUserData(sharedPreferences);
                        navigateToLogin();
                    }
                },
                error -> {
                    Toast.makeText(PumpTimerSettingsActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("apiKey", apiKey);
                return params;
            }
        };

        queue.add(stringRequest);
    }

    private void clearUserData(SharedPreferences sharedPreferences) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();  // Limpiar los datos del usuario
        editor.apply();
    }

    private void navigateToLogin() {
        // Navegar a la pantalla de login
        Intent intent = new Intent(getApplicationContext(), Login.class);
        startActivity(intent);
        finish();
    }
}
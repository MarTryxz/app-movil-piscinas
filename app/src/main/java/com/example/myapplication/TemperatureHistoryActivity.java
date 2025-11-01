package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TemperatureHistoryActivity extends AppCompatActivity {
    SharedPreferences sharedPreferences;
    Button btnBack, btnLogout;
    TextView TitleTextView;
    RecyclerView temperatureRecyclerView;
    private TemperatureAdapter temperatureAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature_history);

        btnBack = findViewById(R.id.btnBack);
        btnLogout = findViewById(R.id.btnLogout);

        temperatureRecyclerView = findViewById(R.id.temperatureRecyclerView);
        temperatureRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        TitleTextView = findViewById(R.id.titleTextView);
        List<TemperatureRecord> temperatureHistory = getTemperatureHistory();
        temperatureAdapter = new TemperatureAdapter(temperatureHistory);
        temperatureAdapter = new TemperatureAdapter(temperatureHistory);
        temperatureRecyclerView.setAdapter(temperatureAdapter);

        sharedPreferences = getSharedPreferences("MyappName", MODE_PRIVATE);

        //aqui empieza el boton para cerrar la sesion
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //esto es para obtener los datos del usuario que el coloco en el login (copiado y pegado de chatgpt)
                sharedPreferences = getSharedPreferences("MyappName", MODE_PRIVATE);
                String email = sharedPreferences.getString("email", "");
                String apiKey = sharedPreferences.getString("apiKey", "");

                //esto es para saber si estan vacias las variables
                if (email.isEmpty() || apiKey.isEmpty()) {
                    Toast.makeText(TemperatureHistoryActivity.this, "No data user found", Toast.LENGTH_SHORT).show();
                    return;
                }

                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                String url = "http://192.168.100.91/backendpiscina/logout.php";

                StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

                                if (response.equals("success")) {
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("logged", "false");
                                    editor.putString("name", "");
                                    editor.putString("lastName", "");
                                    editor.putString("email", "");
                                    editor.putString("phone", "");
                                    editor.putString("password", "");
                                    editor.putString("apiKey", "");
                                    editor.putString("id_cliente", "");
                                    editor.apply();

                                    Intent intent = new Intent(getApplicationContext(), Login.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(TemperatureHistoryActivity.this, response, Toast.LENGTH_SHORT).show();
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("logged", "false");
                                    editor.putString("name", "");
                                    editor.putString("lastName", "");
                                    editor.putString("email", "");
                                    editor.putString("phone", "");
                                    editor.putString("password", "");
                                    editor.putString("apiKey", "");
                                    editor.putString("id_cliente", "");
                                    editor.apply();

                                    Intent intent = new Intent(getApplicationContext(), Login.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }) {
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
        });

        //aqui empieza el boton para ir al menu principal
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Crear la lista de temperaturas
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = "http://192.168.100.91/backendpiscina/historial-temp.php";

        // Lista para almacenar los registros
        List<TemperatureRecord> history = new ArrayList<>();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Response", response); // Depuración
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String status = jsonObject.getString("status");

                            // Al procesar la respuesta de la API
                            if (status.equals("success")) {
                                JSONArray recordsArray = jsonObject.getJSONArray("records");
                                List<TemperatureRecord> history = new ArrayList<>();

                                // Limpiar la lista antes de agregar los nuevos registros

                                // Procesar cada registro de la respuesta
                                for (int i = 0; i < recordsArray.length(); i++) {
                                    JSONObject record = recordsArray.getJSONObject(i);
                                    String fechaHora = record.getString("fecha_hora");
                                    float temperatura = (float) record.getDouble("temperatura");

                                    TitleTextView.setText(fechaHora+" - "+ temperatura+ "°");

                                    // Agregar a la lista
                                    history.add(new TemperatureRecord(fechaHora, temperatura));
                                }

                                // Opcional: Log para verificar
                                for (TemperatureRecord record : history) {
                                    Log.d("TemperatureRecord", record.toString());
                                }

                                // Actualizar el adaptador con los nuevos datos
                                temperatureAdapter.notifyDataSetChanged();
                            } else {
                                String message = jsonObject.getString("message");
                                Toast.makeText(TemperatureHistoryActivity.this, "Error: " + message, Toast.LENGTH_LONG).show();
                            }

                        } catch (JSONException e) {
                            Toast.makeText(TemperatureHistoryActivity.this, "Error procesando respuesta: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            TitleTextView.setText(e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(TemperatureHistoryActivity.this, "Error de conexión: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id_cliente", sharedPreferences.getString("id_cliente", ""));
                return params;
            }
        };

        // Agregar la solicitud a la cola
        queue.add(stringRequest);

    }
    private List<TemperatureRecord> getTemperatureHistory() {

        List<TemperatureRecord> history = new ArrayList<>();
        history.add(new TemperatureRecord("06-06-2024", 16.8f));
        history.add(new TemperatureRecord("20-10-2024", 19.0f));
        history.add(new TemperatureRecord("24-11-2024", 20.8f));
        history.add(new TemperatureRecord("21-12-2024", 21.8f));
        history.add(new TemperatureRecord("22-12-2024", 18.8f));
        history.add(new TemperatureRecord("11-11-2024", 20.8f));
        history.add(new TemperatureRecord("23-12-2024", 17.8f));
        history.add(new TemperatureRecord("12-11-2024", 17.8f));
        history.add(new TemperatureRecord("15-10-2024", 17.8f));
        history.add(new TemperatureRecord("16-09-2024", 17.8f));
        history.add(new TemperatureRecord("19-12-2024", 17.8f));
        history.add(new TemperatureRecord("24-11-2024", 17.8f));
        history.add(new TemperatureRecord("18-11-2024", 17.8f));
        history.add(new TemperatureRecord("19-12-2024", 17.8f));
        history.add(new TemperatureRecord("15-12-2024", 17.8f));
        history.add(new TemperatureRecord("16-12-2024", 17.8f));
        history.add(new TemperatureRecord("17-12-2024", 17.8f));
        history.add(new TemperatureRecord("19-10-2024", 17.8f));

        return history;
    }
}

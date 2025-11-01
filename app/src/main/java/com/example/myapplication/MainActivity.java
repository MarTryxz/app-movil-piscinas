package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    TextView textViewName, textViewTemperature, textViewPh, pumpScheduleTextView;
    SharedPreferences sharedPreferences;
    Button buttonLogout;
    ImageButton userSettingsButton;
    Button modifyScheduleButton, updateDataTemperatura, updateDataPh;
    String temperatura, ph;
    LinearLayout cardTemp, cardPh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        pumpScheduleTextView = findViewById(R.id.pumpScheduleTextView);
        cardTemp = findViewById(R.id.TempCard);
        cardPh = findViewById(R.id.PhCard);

        updateDataTemperatura = findViewById(R.id.refreshTemperatureButton);
        updateDataPh = findViewById(R.id.refreshPhButton);

        textViewTemperature = findViewById(R.id.poolTemperatureTextView);
        textViewPh = findViewById(R.id.phLevelTextView);

        textViewName = findViewById(R.id.name);
        buttonLogout = findViewById(R.id.logout);
        userSettingsButton = findViewById(R.id.userSettingsButton);
        modifyScheduleButton = findViewById(R.id.modifyScheduleButton); // Iniciando el botón Modificar

        sharedPreferences = getSharedPreferences("MyappName", MODE_PRIVATE);

        if (sharedPreferences.getString("logged", "false").equals("false") || sharedPreferences.getString("name", "").equals("")) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        }

        textViewName.setText("👋 ¡Hola, " + sharedPreferences.getString("name", "") + "! 🏊");

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = "http://192.168.100.91/backendpiscina/datos_main.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Response", response); // Depuración
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String status = jsonObject.getString("status");
                            if (status.equals("success")) {


                                temperatura = jsonObject.getString("temperatura");
                                ph = jsonObject.getString("ph");
                                textViewTemperature.setText(temperatura + "°");
                                textViewPh.setText(ph);
                                String schedule1 = jsonObject.getString("hora_inicio");
                                String schedule2 = jsonObject.getString("hora_fin");
                                pumpScheduleTextView.setText(schedule1+" - "+schedule2);

                            } else {
                                String message = jsonObject.getString("message");
                                Toast.makeText(MainActivity.this, "Error: " + message, Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(MainActivity.this, "Error de respuesta: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "Error de conexión: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("apiKey", sharedPreferences.getString("apiKey", ""));
                return params;
            }
        };

        queue.add(stringRequest);

        updateDataTemperatura.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                String url = "http://192.168.100.91/backendpiscina/datos_main.php";

                StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.d("Response", response); // Depuración
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    String status = jsonObject.getString("status");
                                    if (status.equals("success")) {
                                        temperatura = jsonObject.getString("temperatura");

                                        textViewTemperature.setText(temperatura + "°");
                                    } else {
                                        String message = jsonObject.getString("message");
                                        Toast.makeText(MainActivity.this, "Error: " + message, Toast.LENGTH_LONG).show();
                                    }
                                } catch (JSONException e) {
                                    Toast.makeText(MainActivity.this, "Error procesando respuesta: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(MainActivity.this, "Error de conexión: " + error.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put("apiKey", sharedPreferences.getString("apiKey", ""));
                        return params;
                    }
                };

                queue.add(stringRequest);
            }
        });
        updateDataPh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                String url = "http://192.168.100.91/backendpiscina/datos_main.php";

                StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.d("Response", response); // Depuración
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    String status = jsonObject.getString("status");
                                    if (status.equals("success")) {
                                        ph = jsonObject.getString("ph");

                                        textViewPh.setText(ph);
                                    } else {
                                        String message = jsonObject.getString("message");
                                        Toast.makeText(MainActivity.this, "Error: " + message, Toast.LENGTH_LONG).show();
                                    }
                                } catch (JSONException e) {
                                    Toast.makeText(MainActivity.this, "Error procesando respuesta: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(MainActivity.this, "Error de conexión: " + error.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put("apiKey", sharedPreferences.getString("apiKey", ""));
                        return params;
                    }
                };

                queue.add(stringRequest);
            }
        });
        //aqui empieza el boton para cerrar la sesion
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //esto es para obtener los datos del usuario que el coloco en el login (copiado y pegado de chatgpt)
                SharedPreferences sharedPreferences = getSharedPreferences("MyappName", MODE_PRIVATE);
                String email = sharedPreferences.getString("email", "");
                String apiKey = sharedPreferences.getString("apiKey", "");

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
                                    Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT).show();
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("logged", "true");
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
                        params.put("email", MainActivity.this.sharedPreferences.getString("email", ""));
                        params.put("apiKey", MainActivity.this.sharedPreferences.getString("apiKey", ""));
                        return params;
                    }
                };
                queue.add(stringRequest);
            }
        });

        // Aquí se configura el botón para ir a la pantalla de editar perfil
        userSettingsButton.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), ProfileEditor.class);
            startActivity(intent);
            finish();
        });

        // Aquí se configura el botón para ir a PumpTimerSettingsActivity
        modifyScheduleButton.setOnClickListener(v -> {
            // Cuando el botón Modificar sea presionado, se abrirá PumpTimerSettingsActivity
            Intent intent = new Intent(getApplicationContext(), PumpTimerSettingsActivity.class);
            startActivity(intent);
        });

        // Aquí se configura el botón para ir a temperatura
        cardTemp.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), TemperatureHistoryActivity.class);
            startActivity(intent);
        });

        // Aquí se configura el botón para ir a la vista del ph
        cardPh.setOnClickListener(v -> {
            // Cuando el botón Modificar sea presionado, se abrirá PumpTimerSettingsActivity
            Intent intent = new Intent(getApplicationContext(), PhView.class);
            startActivity(intent);
        });
    }
}

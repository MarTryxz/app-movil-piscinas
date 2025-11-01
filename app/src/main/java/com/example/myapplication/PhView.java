package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.myapplication.R;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class PhView extends AppCompatActivity {
    SharedPreferences SharedPreferences;
    private TextView phValueTextView;
    private TextView waterQualityTextView;
    private ProgressBar phProgressBar;
    private Handler handler;
    private Random random;
    Button buttonLogout, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ph_view);

        buttonLogout = findViewById(R.id.btnLogout);
        btnBack = findViewById(R.id.btnBack);

        phValueTextView = findViewById(R.id.phValueTextView);
        waterQualityTextView = findViewById(R.id.waterQualityTextView);
        phProgressBar = findViewById(R.id.phProgressBar);

        handler = new Handler();
        random = new Random();

        startPhMeasurement();
        SharedPreferences = getSharedPreferences("MyappName", MODE_PRIVATE);

        //aqui empieza el boton para cerrar la sesion
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //esto es para obtener los datos del usuario que el coloco en el login (copiado y pegado de chatgpt)
                SharedPreferences sharedPreferences = getSharedPreferences("MyappName", MODE_PRIVATE);
                String email = sharedPreferences.getString("email", "");
                String apiKey = sharedPreferences.getString("apiKey", "");

                //esto es para saber si estan vacias las variables
                if (email.isEmpty() || apiKey.isEmpty()) {
                    Toast.makeText(PhView.this, "No data user found", Toast.LENGTH_SHORT).show();
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
                                    Toast.makeText(PhView.this, response, Toast.LENGTH_SHORT).show();
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
                        params.put("email", SharedPreferences.getString("email", ""));
                        params.put("apiKey", SharedPreferences.getString("apiKey", ""));
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
    }

    private void startPhMeasurement() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updatePhValue();
                handler.postDelayed(this, 3000); // Actualizar cada 3 segundos
            }
        }, 0);
    }

    private void updatePhValue() {
        double ph = random.nextDouble() * 14;
        int progress = (int) (ph * 100);

        phValueTextView.setText(String.format(Locale.getDefault(), "%.2f", ph));
        phProgressBar.setProgress(progress);

        String quality;
        int color;
        if (ph < 6.5) {
            quality = "Ácida";
            color = getResources().getColor(android.R.color.holo_red_light);
        } else if (ph > 8.5) {
            quality = "Alcalina";
            color = getResources().getColor(android.R.color.holo_blue_light);
        } else {
            quality = "Neutra";
            color = getResources().getColor(android.R.color.holo_green_light);
        }

        waterQualityTextView.setText(quality);
        waterQualityTextView.setTextColor(color);

        updateWaterDetails();
    }

    private void updateWaterDetails() {
        double temperature = 20 + random.nextDouble() * 10;
        int conductivity = 400 + random.nextInt(200);
        double oxygen = 6 + random.nextDouble() * 4;

        String details = String.format(Locale.getDefault(),
                "Temperatura: %.1f°C\nConductividad: %d µS/cm\nOxígeno disuelto: %.1f mg/L",
                temperature, conductivity, oxygen);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
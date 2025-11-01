package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;

public class Registration extends AppCompatActivity {

    TextInputEditText textInputEditTextName, textInputEditTextLastname, textInputEditTextPhone, textInputEditTextEmail, textInputEditTextPassword;
    Button buttonSubmit;
    String name, lastname, phone, email, password;
    TextView textViewError, textViewLoginNow;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registration);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar los campos de entrada y el botón
        textViewLoginNow = findViewById(R.id.loginNow);
        textInputEditTextName = findViewById(R.id.name);
        textInputEditTextLastname = findViewById(R.id.lastname);
        textInputEditTextPhone = findViewById(R.id.phone);
        textInputEditTextEmail = findViewById(R.id.email);
        textInputEditTextPassword = findViewById(R.id.password);
        buttonSubmit = findViewById(R.id.submit);
        textViewError = findViewById(R.id.error);
        progressBar = findViewById(R.id.loading);

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.GONE);
                name = textInputEditTextName.getText().toString().trim();
                lastname = textInputEditTextLastname.getText().toString().trim();
                phone = textInputEditTextPhone.getText().toString().trim();
                email = textInputEditTextEmail.getText().toString().trim();
                password = textInputEditTextPassword.getText().toString().trim();

                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                String url = "http://192.168.100.91/backendpiscina/register.php";


                StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                progressBar.setVisibility(View.GONE);
                                if (response.trim().equalsIgnoreCase("Registro exitoso")) {
                                    Toast.makeText(Registration.this, "Registro exitoso", Toast.LENGTH_SHORT).show();

                                    // Redirigir al Login
                                    Intent intent = new Intent(Registration.this, Login.class);
                                    startActivity(intent);
                                    finish(); // Finalizar actividad actual
                                } else {
                                    textViewError.setVisibility(View.VISIBLE);
                                    textViewError.setText(response);
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(Registration.this, "Error de conexión: " + error.getMessage(), Toast.LENGTH_LONG).show();
                                textViewError.setVisibility(View.VISIBLE);
                                textViewError.setText(error.getLocalizedMessage());
                            }
                        }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put("nombres", name);
                        params.put("apellidos", lastname);
                        params.put("telefono", phone);
                        params.put("correo_electronico", email);
                        params.put("contrasena", password);
                        return params;
                    }
                };
                queue.add(stringRequest);
            }
        });
        textViewLoginNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });
    }
}

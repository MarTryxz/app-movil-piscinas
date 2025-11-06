package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {

    TextView textViewRegisterNow, textViewError;
    TextInputEditText textInputEditTextEmail, textInputEditTextPassword;
    Button buttonSubmit;
    ProgressBar progressBar;
    SharedPreferences sharedPreferences;

    String name, email, password, apiKey, phone, lastName, idCliente;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //Este es el boton para registrarse
        textViewRegisterNow = findViewById(R.id.registerNow);

        //Aqui empieza el boton que usamos para iniciar sesion
        textInputEditTextEmail = findViewById(R.id.email);
        textInputEditTextPassword = findViewById(R.id.password);

        //Aqui empieza el boton para enviar el formulario
        buttonSubmit = findViewById(R.id.submit);

        //Aqui empieza el boton para mostrar un error
        textViewError = findViewById(R.id.error);

        //Aqui empieza el boton para mostrar el progreso
        progressBar = findViewById(R.id.loading);

        //inicializamos la variable de sharedPreferences con el nombre de la app
        sharedPreferences = getSharedPreferences("MyappName", MODE_PRIVATE);

        //Si el usuario esta logeado y tiene un nombre, entonces iniciamos el activity principal
        if(sharedPreferences.getString("logged", "false").equals("true") && sharedPreferences.getString("name", "") != ""){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }

        //aqui empieza el boton que usamos para iniciar sesion | Existen pequeñas correcciones que se pueden hacer como por ejemplo
        //cambiar el texto de error de "Por favor, completa todos los campos." por "Por favor, completa todos los campos o registrate." |
        //o cambiar el texto de error de "Error procesando respuesta: " + e.getMessage() por "Error procesando respuesta: " + e.getLocalizedMessage()
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Aqui inicializamos la variable de sharedPreferences con el nombre de la app
                SharedPreferences sharedPreferences = getSharedPreferences("MyappName", MODE_PRIVATE);

                //Aqui obtenemos el email y el password
                email = textInputEditTextEmail.getText().toString().trim();
                password = textInputEditTextPassword.getText().toString().trim();

                //Si el usuario no ha completado el email o el password, entonces mostramos un error
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(Login.this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show();
                    return;
                }

                //Aqui mostramos el progreso
                textViewError.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);

                //Aqui inicializamos el objeto RequestQueue
                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                //Aqui obtenemos la url de la api
                String url = "http://192.168.1.6/backendpiscina/login.php";

                //Aqui inicializamos el objeto StringRequest. Este objeto se encarga de enviar la peticion a la api (asi son comunicamos con la base de datos a traves de la api)
                StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                progressBar.setVisibility(View.GONE);
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    String status = jsonObject.getString("status");
                                    String message = jsonObject.getString("message");
                                    if (status.equals("success")) {
                                        name = jsonObject.getString("nombres");
                                        lastName = jsonObject.getString("apellidos");
                                        email = jsonObject.getString("email");
                                        phone = jsonObject.getString("phone");
                                        password = jsonObject.getString("password");
                                        apiKey = jsonObject.getString("apiKey");

                                        // Usar la variable de instancia directamente
                                        idCliente = jsonObject.getString("id_cliente");

                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString("logged", "true");
                                        editor.putString("name", name);
                                        editor.putString("lastName", lastName);
                                        editor.putString("email", email);
                                        editor.putString("phone", phone);
                                        editor.putString("password", password);
                                        editor.putString("apiKey", apiKey);
                                        editor.putString("id_cliente", idCliente); // Guardar la variable de instancia
                                        editor.apply();

                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(Login.this, message, Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    Toast.makeText(Login.this, "Error procesando respuesta: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }

                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(Login.this, "Error de conexiÃ³n: " + error.getMessage(), Toast.LENGTH_LONG).show();
                                textViewError.setVisibility(View.VISIBLE);
                                textViewError.setText(error.getLocalizedMessage());
                            }
                        }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put("email", email);
                        params.put("password", password);


                        return params;
                    }
                };

                queue.add(stringRequest);
            }
        });

        //Aqui empieza el boton para ir a la pantalla de registro
        textViewRegisterNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Registration.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

public class ProfileEditor extends AppCompatActivity {
    SharedPreferences sharedPreferences;
    TextView TextViewName;
    TextInputEditText editName, inputLastName, inputEmail, inputPassword, inputPhone;
    Button buttonLogout, buttonEditName, btnBack, buttonEditLastName, buttonEditEmail, buttonEditPassword, buttonEditPhone;
    String name, lastname, email, password, phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_editor);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextViewName = findViewById(R.id.TextName);
        buttonLogout = findViewById(R.id.btnLogout);
        btnBack = findViewById(R.id.btnBack);

        buttonEditName = findViewById(R.id.btnEditName);
        editName = findViewById(R.id.editName);

        inputLastName = findViewById(R.id.inputLastName);
        buttonEditLastName = findViewById(R.id.btnEditLastName);

        inputEmail = findViewById(R.id.inputEmail);
        buttonEditEmail = findViewById(R.id.btnEditEmail);

        inputPassword = findViewById(R.id.inputPassword);
        buttonEditPassword = findViewById(R.id.btnEditPassword);

        inputPhone = findViewById(R.id.inputPhone);
        buttonEditPhone = findViewById(R.id.btnEditPhone);

        sharedPreferences = getSharedPreferences("MyappName", MODE_PRIVATE);

        TextViewName.setText("👋 Esta ventana es para editar tus datos, "+sharedPreferences.getString("name", "")+" 🔐");
        editName.setText(sharedPreferences.getString("name", ""));
        inputLastName.setText(sharedPreferences.getString("lastName", ""));
        inputEmail.setText(sharedPreferences.getString("email", ""));
        inputPhone.setText(sharedPreferences.getString("phone", ""));

        buttonEditPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                password = inputPassword.getText().toString().trim();

                //esto es para obtener los datos del usuario que el coloco en el login (copiado y pegado de chatgpt)
                SharedPreferences sharedPreferences = getSharedPreferences("MyappName", MODE_PRIVATE);
                String apiKey = sharedPreferences.getString("apiKey", "");

                //esto es para saber si estan vacias las variables
                if (apiKey.isEmpty()) {
                    Toast.makeText(ProfileEditor.this, "No data user found", Toast.LENGTH_SHORT).show();
                    return;
                } else if (password.isEmpty()) {
                    Toast.makeText(ProfileEditor.this, "data field empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                String url = "http://192.168.100.91/backendpiscina/editar/update-m.php";

                StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.equals("success")) {
                            Toast.makeText(ProfileEditor.this, response, Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(ProfileEditor.this, response, Toast.LENGTH_SHORT).show();
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

                        params.put("password", password);
                        params.put("apiKey", sharedPreferences.getString("apiKey", ""));
                        String caso4 = "4";
                        params.put("caso", caso4);
                        return params;
                    }
                };
                queue.add(stringRequest);
            }
        });

        //aqui empieza el boton para editar el telefono del usuario
        buttonEditPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phone = inputPhone.getText().toString().trim();

                //esto es para obtener los datos del usuario que el coloco en el login (copiado y pegado de chatgpt)
                SharedPreferences sharedPreferences = getSharedPreferences("MyappName", MODE_PRIVATE);
                String apiKey = sharedPreferences.getString("apiKey", "");

                //esto es para saber si estan vacias las variables
                if (apiKey.isEmpty()) {
                    Toast.makeText(ProfileEditor.this, "No data user found", Toast.LENGTH_SHORT).show();
                    return;
                } else if (phone.isEmpty()) {
                    Toast.makeText(ProfileEditor.this, "data field empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                String url = "http://192.168.100.91/backendpiscina/editar/update-m.php";

                StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.equals("success")) {
                            Toast.makeText(ProfileEditor.this, response, Toast.LENGTH_SHORT).show();
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("logged", "false");
                            editor.putString("name", "");
                            editor.putString("email", "");
                            editor.putString("apiKey", "");
                            editor.putString("phone", "");
                            editor.putString("lastName", "");
                            editor.apply();

                            Intent intent = new Intent(getApplicationContext(), Login.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(ProfileEditor.this, response, Toast.LENGTH_SHORT).show();
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("logged", "false");
                            editor.putString("name", "");
                            editor.putString("email", "");
                            editor.putString("apiKey", "");
                            editor.putString("phone", "");
                            editor.putString("lastName", "");
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

                        params.put("phone", phone);
                        params.put("apiKey", sharedPreferences.getString("apiKey", ""));
                        String caso5 = "5";
                        params.put("caso", caso5);
                        return params;
                    }
                };
                queue.add(stringRequest);
            }
        });

        //aqui empieza el boton para editar el email del usuario
        buttonEditEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = inputEmail.getText().toString().trim();

                //esto es para obtener los datos del usuario que el coloco en el login (copiado y pegado de chatgpt)
                SharedPreferences sharedPreferences = getSharedPreferences("MyappName", MODE_PRIVATE);
                String apiKey = sharedPreferences.getString("apiKey", "");

                //esto es para saber si estan vacias las variables
                if (apiKey.isEmpty()) {
                    Toast.makeText(ProfileEditor.this, "No data user found", Toast.LENGTH_SHORT).show();
                    return;
                } else if (email.isEmpty()) {
                    Toast.makeText(ProfileEditor.this, "data field empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                String url = "http://192.168.100.91/backendpiscina/editar/update-m.php";

                StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.equals("success")) {
                            Toast.makeText(ProfileEditor.this, response, Toast.LENGTH_SHORT).show();
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("logged", "false");
                            editor.putString("name", "");
                            editor.putString("email", "");
                            editor.putString("apiKey", "");
                            editor.putString("phone", "");
                            editor.putString("lastName", "");
                            editor.apply();

                            Intent intent = new Intent(getApplicationContext(), Login.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(ProfileEditor.this, response, Toast.LENGTH_SHORT).show();
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("logged", "false");
                            editor.putString("name", "");
                            editor.putString("email", "");
                            editor.putString("apiKey", "");
                            editor.putString("phone", "");
                            editor.putString("lastName", "");
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

                        params.put("email", email);
                        params.put("apiKey", sharedPreferences.getString("apiKey", ""));
                        String caso3 = "3";
                        params.put("caso", caso3);
                        return params;
                    }
                };
                queue.add(stringRequest);
            }
        });

        //aqui empieza el boton para editar el apellido del usuario
        buttonEditLastName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lastname = inputLastName.getText().toString().trim();

                //esto es para obtener los datos del usuario que el coloco en el login (copiado y pegado de chatgpt)
                SharedPreferences sharedPreferences = getSharedPreferences("MyappName", MODE_PRIVATE);
                String apiKey = sharedPreferences.getString("apiKey", "");

                //esto es para saber si estan vacias las variables
                if (apiKey.isEmpty()) {
                    Toast.makeText(ProfileEditor.this, "No data user found", Toast.LENGTH_SHORT).show();
                    return;
                } else if (lastname.isEmpty()) {
                    Toast.makeText(ProfileEditor.this, "data field empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                String url = "http://192.168.100.91/backendpiscina/editar/update-m.php";

                StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.equals("success")) {
                            Toast.makeText(ProfileEditor.this, response, Toast.LENGTH_SHORT).show();
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("logged", "false");
                            editor.putString("name", "");
                            editor.putString("email", "");
                            editor.putString("apiKey", "");
                            editor.putString("phone", "");
                            editor.putString("lastName", "");
                            editor.apply();

                            Intent intent = new Intent(getApplicationContext(), Login.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(ProfileEditor.this, response, Toast.LENGTH_SHORT).show();
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("logged", "false");
                            editor.putString("name", "");
                            editor.putString("email", "");
                            editor.putString("apiKey", "");
                            editor.putString("phone", "");
                            editor.putString("lastName", "");
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

                        params.put("lastname", lastname);
                        params.put("apiKey", sharedPreferences.getString("apiKey", ""));
                        String caso2 = "2";
                        params.put("caso", caso2);
                        return params;
                    }
                };
                queue.add(stringRequest);
            }
        });

        //aqui empieza el boton para editar el nombre del usuario
        buttonEditName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = editName.getText().toString().trim();

                //esto es para obtener los datos del usuario que el coloco en el login (copiado y pegado de chatgpt)
                SharedPreferences sharedPreferences = getSharedPreferences("MyappName", MODE_PRIVATE);
                String apiKey = sharedPreferences.getString("apiKey", "");

                //esto es para saber si estan vacias las variables
                if (apiKey.isEmpty()) {
                    Toast.makeText(ProfileEditor.this, "No data user found", Toast.LENGTH_SHORT).show();
                    return;
                } else if (name.isEmpty()) {
                    Toast.makeText(ProfileEditor.this, "data field empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                String url = "http://192.168.100.91/backendpiscina/editar/update-m.php";

                StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.equals("success")) {
                            Toast.makeText(ProfileEditor.this, response, Toast.LENGTH_SHORT).show();
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("logged", "false");
                            editor.putString("name", "");
                            editor.putString("email", "");
                            editor.putString("apiKey", "");
                            editor.putString("phone", "");
                            editor.putString("lastName", "");
                            editor.apply();

                            Intent intent = new Intent(getApplicationContext(), Login.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(ProfileEditor.this, response, Toast.LENGTH_SHORT).show();
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("logged", "false");
                            editor.putString("name", "");
                            editor.putString("email", "");
                            editor.putString("apiKey", "");
                            editor.putString("phone", "");
                            editor.putString("lastName", "");
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

                        params.put("name", name);
                        params.put("apiKey", sharedPreferences.getString("apiKey", ""));
                        String caso1 = "1";
                        params.put("caso", caso1);
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

                //esto es para saber si estan vacias las variables
                if (email.isEmpty() || apiKey.isEmpty()) {
                    Toast.makeText(ProfileEditor.this, "No data user found", Toast.LENGTH_SHORT).show();
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
                                    editor.putString("email", "");
                                    editor.putString("apiKey", "");
                                    editor.putString("phone", "");
                                    editor.putString("lastName", "");
                                    editor.apply();

                                    Intent intent = new Intent(getApplicationContext(), Login.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(ProfileEditor.this, response, Toast.LENGTH_SHORT).show();
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("logged", "false");
                                    editor.putString("name", "");
                                    editor.putString("email", "");
                                    editor.putString("apiKey", "");
                                    editor.putString("phone", "");
                                    editor.putString("password", "");
                                    editor.putString("lastName", "");
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
    }
}
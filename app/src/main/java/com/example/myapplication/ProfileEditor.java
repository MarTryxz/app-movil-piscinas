package com.example.myapplication;

import android.content.Intent;
// import android.content.SharedPreferences; // Ya no se usa para la sesión
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.appbar.MaterialToolbar;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// --- ELIMINAMOS TODAS LAS IMPORTACIONES DE VOLLEY ---
// import com.android.volley.Request;
// ... (etc)

// --- AÑADIMOS LAS IMPORTACIONES DE FIREBASE AUTH, GOOGLE Y DATABASE ---
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class ProfileEditor extends AppCompatActivity {

    // --- Variables de Firebase ---
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;
    private String uid;

    // --- Vistas ---
    TextView TextViewName;
    TextInputEditText editName, inputLastName, inputEmail, inputPassword, inputPhone;
    Button buttonLogout, buttonEditName, buttonEditLastName, buttonEditEmail, buttonEditPassword, buttonEditPhone;

    // private SharedPreferences sharedPreferences; // Ya no se usa

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_editor);

        // Configurar la barra de herramientas
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Manejar el clic en el botón de retroceso
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // --- 1. Inicializar Firebase ---
        mAuth = FirebaseAuth.getInstance();
        // Asumimos que guardaremos/leeremos datos extra (apellido, teléfono) en un nodo "users"
        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        // Configurar Google Sign-In Client (necesario para el logout de Google)
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // --- 2. Enlazar Vistas ---
        TextViewName = findViewById(R.id.TextName);
        buttonLogout = findViewById(R.id.btnLogout);
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

        // --- 3. Lógica de Botones ---

        // Botón "Logout" (Lógica de Firebase)
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 1. Cierra sesión de Firebase
                mAuth.signOut();

                // 2. Cierra sesión de Google
                mGoogleSignInClient.signOut().addOnCompleteListener(ProfileEditor.this, task -> {

                    // 3. Redirige a Login y limpia el historial
                    Intent intent = new Intent(getApplicationContext(), Login.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });
            }
        });

        // Botón "Editar Nombre"
        buttonEditName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = editName.getText().toString().trim();
                if (newName.isEmpty()) {
                    Toast.makeText(ProfileEditor.this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (currentUser != null) {
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(newName)
                            .build();

                    currentUser.updateProfile(profileUpdates)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ProfileEditor.this, "Nombre actualizado con éxito", Toast.LENGTH_SHORT).show();
                                    TextViewName.setText("👋 ¡Hola, " + newName + "! 👤");
                                } else {
                                    Toast.makeText(ProfileEditor.this, "Error al actualizar el nombre", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });

        // Botón "Editar Apellido" (Usa Realtime Database)
        buttonEditLastName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newLastName = inputLastName.getText().toString().trim();
                if (uid != null && !newLastName.isEmpty()) {
                    mDatabase.child(uid).child("lastName").setValue(newLastName)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ProfileEditor.this, "Apellido actualizado con éxito", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(ProfileEditor.this, "Error al guardar el apellido", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });

        // Botón "Editar Email"
        buttonEditEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newEmail = inputEmail.getText().toString().trim();
                if (newEmail.isEmpty()) {
                    Toast.makeText(ProfileEditor.this, "El email no puede estar vacío", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (currentUser != null) {
                    currentUser.updateEmail(newEmail)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ProfileEditor.this, "Email actualizado. Revisa tu correo para verificar.", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(ProfileEditor.this, "Error al actualizar email: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                }
            }
        });

        // Botón "Editar Contraseña"
        buttonEditPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newPassword = inputPassword.getText().toString().trim();
                if (newPassword.length() < 6) {
                    Toast.makeText(ProfileEditor.this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (currentUser != null) {
                    currentUser.updatePassword(newPassword)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ProfileEditor.this, "Contraseña actualizada con éxito", Toast.LENGTH_SHORT).show();
                                    inputPassword.setText("");
                                } else {
                                    Toast.makeText(ProfileEditor.this, "Error al actualizar contraseña: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                }
            }
        });

        // Botón "Editar Teléfono" (Usa Realtime Database)
        buttonEditPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newPhone = inputPhone.getText().toString().trim();
                if (uid != null && !newPhone.isEmpty()) {
                    mDatabase.child(uid).child("phone").setValue(newPhone)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ProfileEditor.this, "Teléfono actualizado con éxito", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(ProfileEditor.this, "Error al guardar el teléfono", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });

    } // --- FIN DE onCreate() ---

    // --- 4. AÑADIMOS onStart() PARA CARGAR DATOS DEL USUARIO ---
    @Override
    protected void onStart() {
        super.onStart();
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // Nadie ha iniciado sesión, volver a Login
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        } else {
            // Usuario encontrado, cargar sus datos
            uid = currentUser.getUid();
            loadUserProfile(currentUser);
        }
    }

    // --- 5. NUEVO MÉTODO PARA CARGAR DATOS ---
    private void loadUserProfile(FirebaseUser user) {
        // Cargar datos directos de Firebase Auth
        if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
            TextViewName.setText("👋 ¡Hola, " + user.getDisplayName() + "! 👤");
            editName.setText(user.getDisplayName());
        } else {
            TextViewName.setText("👋 ¡Hola! 👤");
        }
        inputEmail.setText(user.getEmail());

        // Deshabilitar la edición de email si inició con Google (recomendado)
        if (user.getProviderData().size() > 1 && user.getProviderData().get(1).getProviderId().equals("google.com")) {
            inputEmail.setEnabled(false);
            buttonEditEmail.setEnabled(false);
            // También deshabilitar contraseña si es de Google
            inputPassword.setEnabled(false);
            buttonEditPassword.setEnabled(false);
        }

        // Cargar datos extra (apellido, teléfono) desde Realtime Database
        mDatabase.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String lastName = snapshot.child("lastName").getValue(String.class);
                    String phone = snapshot.child("phone").getValue(String.class);

                    if (lastName != null) {
                        inputLastName.setText(lastName);
                    }
                    if (phone != null) {
                        inputPhone.setText(phone);
                    }
                } else {
                    Log.w("ProfileEditor", "No existen datos adicionales para este usuario en la BD.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ProfileEditor", "Error al leer datos de la BD", error.toException());
            }
        });
    }
}
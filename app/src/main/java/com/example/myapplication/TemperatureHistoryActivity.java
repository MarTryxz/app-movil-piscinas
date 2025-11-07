package com.example.myapplication;

import android.content.Intent;
// import android.content.SharedPreferences; // Ya no es necesario
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myapplication.databinding.ActivityTemperatureHistoryBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TemperatureHistoryActivity extends AppCompatActivity {

    private ActivityTemperatureHistoryBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private TemperatureAdapter temperatureAdapter;
    private List<TemperatureRecord> temperatureHistoryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTemperatureHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Configuración del RecyclerView
        temperatureHistoryList = new ArrayList<>();
        temperatureAdapter = new TemperatureAdapter(temperatureHistoryList);
        binding.temperatureRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.temperatureRecyclerView.setAdapter(temperatureAdapter);

        // 1. Lógica de navegación inferior
        setupBottomNavigation();

        // 2. Cargar historial de temperaturas (SE MOVIÓ A onStart())
        // loadTemperatureHistory(); // <-- Ya no se llama aquí
    }

    // --- AÑADIMOS onStart() PARA VERIFICAR LA SESIÓN ---
    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            // No hay usuario logueado, ir a Login y salir de esta actividad
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        } else {
            // Usuario SÍ está logueado, cargar su historial
            loadTemperatureHistory(currentUser.getUid());
        }
    }

    /**
     * Configura el listener para la barra de navegación inferior.
     */
    private void setupBottomNavigation() {
        // Marcamos "Historial" como el ítem seleccionado en esta actividad
        binding.bottomNavigation.setSelectedItemId(R.id.nav_history);

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_monitor) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_history) {
                return true;
            } else if (itemId == R.id.nav_profile) {
                Intent intent = new Intent(getApplicationContext(), ProfileEditor.class);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_help) {
                Toast.makeText(this, "Pantalla de Ayuda (próximamente)", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }

    /**
     * Carga los registros de temperatura (MODIFICADO)
     * Ahora asume que el usuario está validado y recibe el userId.
     */
    private void loadTemperatureHistory(String userId) { // <-- Acepta el userId

        // Ya no necesitamos la comprobación "if (currentUser != null)"
        // porque onStart() ya la hizo.

        mDatabase.child("temperaturas").child(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        temperatureHistoryList.clear();
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot recordSnapshot : dataSnapshot.getChildren()) {
                                TemperatureRecord record = recordSnapshot.getValue(TemperatureRecord.class);
                                if (record != null) {
                                    temperatureHistoryList.add(record);
                                }
                            }
                            temperatureAdapter.notifyDataSetChanged();
                            binding.emptyState.setVisibility(temperatureHistoryList.isEmpty() ? View.VISIBLE : View.GONE);
                        } else {
                            Log.d("TemperatureHistory", "No hay datos de temperatura para este usuario.");
                            temperatureAdapter.notifyDataSetChanged();
                            binding.emptyState.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("TemperatureHistory", "Error al leer datos de Firebase: " + databaseError.getMessage());
                        Toast.makeText(TemperatureHistoryActivity.this, "Error al cargar historial: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                        binding.emptyState.setVisibility(View.VISIBLE);
                    }
                });

        // La parte "else" que redirigía al Login también se eliminó
        // porque ya no es necesaria aquí.
    }

    /*
    public static class TemperatureRecord {
        public String fechaHora;
        public float temperatura;

        public TemperatureRecord() { }
    }
    */
}
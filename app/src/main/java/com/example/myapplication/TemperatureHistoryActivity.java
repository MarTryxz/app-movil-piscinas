package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
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
    private List<TemperatureRecord> temperatureHistoryList; // Usar una lista para el adaptador

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

        // 1. Lógica de navegación inferior (reemplaza los botones eliminados)
        setupBottomNavigation();

        // 2. Cargar historial de temperaturas desde Firebase
        loadTemperatureHistory();
    }

    /**
     * Configura el listener para la barra de navegación inferior.
     * Esta función maneja la navegación entre las principales actividades de la app.
     */
    private void setupBottomNavigation() {
        // Marcamos "Historial" como el ítem seleccionado en esta actividad
        binding.bottomNavigation.setSelectedItemId(R.id.nav_history);

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_monitor) {
                // Ir a la pantalla principal (Monitor)
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish(); // Cierra esta actividad para no apilarla
                return true;
            } else if (itemId == R.id.nav_history) {
                // Ya estamos aquí, no hacemos nada
                return true;
            } else if (itemId == R.id.nav_profile) {
                // Ir a la pantalla de perfil
                Intent intent = new Intent(getApplicationContext(), ProfileEditor.class);
                startActivity(intent);
                finish(); // Cierra esta actividad
                return true;
            } else if (itemId == R.id.nav_help) {
                // Mostrar un Toast o una pantalla de ayuda
                Toast.makeText(this, "Pantalla de Ayuda (próximamente)", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }

    /**
     * Carga los registros de temperatura desde Firebase Realtime Database
     * para el usuario autenticado actualmente.
     */
    private void loadTemperatureHistory() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            // Asegúrate de que esta ruta ("temperaturas" -> userId) coincide con tu estructura de Firebase
            mDatabase.child("temperaturas").child(userId)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            temperatureHistoryList.clear(); // Limpiar antes de añadir nuevos datos
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot recordSnapshot : dataSnapshot.getChildren()) {
                                    TemperatureRecord record = recordSnapshot.getValue(TemperatureRecord.class);
                                    if (record != null) {
                                        temperatureHistoryList.add(record);
                                    }
                                }
                                // Notificar al adaptador que los datos cambiaron
                                temperatureAdapter.notifyDataSetChanged();
                                // Mostrar u ocultar el estado vacío
                                binding.emptyState.setVisibility(temperatureHistoryList.isEmpty() ? View.VISIBLE : View.GONE);
                            } else {
                                Log.d("TemperatureHistory", "No hay datos de temperatura para este usuario.");
                                temperatureAdapter.notifyDataSetChanged(); // Asegura que se muestre vacío
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
        } else {
            // Si por alguna razón el usuario no está autenticado, regresarlo al Login
            Toast.makeText(this, "Usuario no autenticado, por favor inicie sesión.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        }
    }

    // Si `TemperatureRecord` es una clase separada (lo cual es recomendable),
    // puedes borrar este comentario. Si no, asegúrate de que exista y sea pública.
    /*
    public static class TemperatureRecord {
        public String fechaHora;
        public float temperatura;

        public TemperatureRecord() {
            // Constructor por defecto requerido para Firebase
        }
    }
    */
}
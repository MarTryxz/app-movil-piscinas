package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

public class TemperatureHistoryActivity extends BaseActivity {

    private ActivityTemperatureHistoryBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private TemperatureAdapter temperatureAdapter;
    private List<TemperatureRecord> temperatureHistoryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Correct ViewBinding usage
        binding = ActivityTemperatureHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Configuración del RecyclerView
        temperatureHistoryList = new ArrayList<>();
        temperatureAdapter = new TemperatureAdapter(temperatureHistoryList);
        binding.temperatureRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.temperatureRecyclerView.setAdapter(temperatureAdapter);

        // Configurar la navegación inferior
        setupBottomNavigation();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // No hay usuario logueado, ir a Login y salir de esta actividad
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        } else {
            // Usuario SÍ está logueado, cargar su historial
            loadTemperatureHistory(currentUser.getUid());
        }
    }

    private void loadTemperatureHistory(String userId) {
        // Pointing to "sensor_status/historial" as per new requirement
        mDatabase.child("sensor_status").child("historial")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        temperatureHistoryList.clear();
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot recordSnapshot : dataSnapshot.getChildren()) {
                                // The key is the Push ID (timestamp encoded)
                                String pushId = recordSnapshot.getKey();

                                // We manually map the fields because the JSON structure matches the fields
                                // but we need to inject the ID.
                                Double tempAgua = parseDouble(recordSnapshot.child("tempAgua").getValue());
                                Double tempAire = parseDouble(recordSnapshot.child("tempAire").getValue());
                                Double humedadAire = parseDouble(recordSnapshot.child("humedadAire").getValue());

                                if (tempAgua != null && tempAire != null && humedadAire != null) {
                                    TemperatureRecord record = new TemperatureRecord(
                                            pushId,
                                            tempAgua.floatValue(),
                                            tempAire.floatValue(),
                                            humedadAire.floatValue());
                                    temperatureHistoryList.add(record);
                                }
                            }
                            // Sort list by timestamp descending (newest first)
                            java.util.Collections.reverse(temperatureHistoryList);

                            temperatureAdapter.notifyDataSetChanged();
                            binding.emptyState
                                    .setVisibility(temperatureHistoryList.isEmpty() ? View.VISIBLE : View.GONE);
                        } else {
                            Log.d("TemperatureHistory", "No hay datos de historial.");
                            temperatureAdapter.notifyDataSetChanged();
                            binding.emptyState.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("TemperatureHistory", "Error al leer datos de Firebase: " + databaseError.getMessage());
                        Toast.makeText(TemperatureHistoryActivity.this,
                                "Error al cargar historial: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                        binding.emptyState.setVisibility(View.VISIBLE);
                    }
                });
    }

    private Double parseDouble(Object value) {
        if (value == null)
            return null;
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
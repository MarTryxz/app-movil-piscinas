package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public abstract class BaseActivity extends AppCompatActivity {
    protected BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void setupBottomNavigation() {
        // Find the bottom navigation view (debe estar en el layout de la actividad)
        bottomNavigation = findViewById(R.id.bottomNavigation);

        if (bottomNavigation == null) {
            return; // Si no hay bottom navigation en este layout, no hacer nada
        }

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_monitor) {
                if (!(this instanceof MainActivity)) {
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                }
                return true;
            } else if (itemId == R.id.nav_history) {
                if (!(this instanceof TemperatureHistoryActivity)) {
                    startActivity(new Intent(this, TemperatureHistoryActivity.class));
                    finish();
                }
                return true;
            } else if (itemId == R.id.nav_profile) {
                if (!(this instanceof ProfileEditor)) {
                    startActivity(new Intent(this, ProfileEditor.class));
                    finish();
                }
                return true;
            } else if (itemId == R.id.nav_bluetooth) {
                if (!(this instanceof BluetoothMacActivity)) {
                    startActivity(new Intent(this, BluetoothMacActivity.class));
                    finish();
                }
                return true;
            } else if (itemId == R.id.nav_help) {
                if (!(this instanceof HelpActivity)) {
                    startActivity(new Intent(this, HelpActivity.class));
                    finish();
                }
                return true;
            }
            return false;
        });

        // Set the selected item based on the current activity
        if (this instanceof MainActivity) {
            bottomNavigation.setSelectedItemId(R.id.nav_monitor);
        } else if (this instanceof TemperatureHistoryActivity) {
            bottomNavigation.setSelectedItemId(R.id.nav_history);
        } else if (this instanceof ProfileEditor) {
            bottomNavigation.setSelectedItemId(R.id.nav_profile);
        } else if (this instanceof BluetoothMacActivity) {
            bottomNavigation.setSelectedItemId(R.id.nav_bluetooth);
        } else if (this instanceof HelpActivity) {
            bottomNavigation.setSelectedItemId(R.id.nav_help);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

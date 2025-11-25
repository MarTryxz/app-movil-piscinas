package com.example.myapplication;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

public class HelpActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        
        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        // Add back button to the toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Ayuda");
        }
        
        // Configurar la navegación inferior
        setupBottomNavigation();
    }
}
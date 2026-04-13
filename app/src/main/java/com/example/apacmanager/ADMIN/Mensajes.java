package com.example.apacmanager.ADMIN;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.apacmanager.R;
import com.google.android.material.appbar.MaterialToolbar;

public class Mensajes extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextView tvNoMensajes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mensajes);

        toolbar = findViewById(R.id.toolbar);
        tvNoMensajes = findViewById(R.id.tvNoMensajes);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Mensajes");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Mostrar mensaje de funcionalidad en desarrollo
        tvNoMensajes.setText("Funcionalidad de mensajes en desarrollo");
        tvNoMensajes.setVisibility(android.view.View.VISIBLE);
    }
}
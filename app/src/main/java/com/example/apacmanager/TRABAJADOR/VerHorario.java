package com.example.apacmanager.TRABAJADOR;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.apacmanager.R;
import com.example.apacmanager.firebase.FirebaseDatabaseHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class VerHorario extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextView tvNotaHorario;

    private String empresaId;
    private String trabajadorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_horario);

        // Recibir extras
        if (getIntent().getExtras() != null) {
            empresaId = getIntent().getStringExtra("empresa_id");
            trabajadorId = getIntent().getStringExtra("trabajador_id");
        }

        if (empresaId == null || trabajadorId == null) {
            Toast.makeText(this, "Error: datos incompletos", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        toolbar = findViewById(R.id.toolbar);
        tvNotaHorario = findViewById(R.id.tvNotaHorario);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Mi Horario");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Opcional: cargar datos del trabajador para mostrar horario personalizado
        cargarHorarioTrabajador();
    }

    private void cargarHorarioTrabajador() {
        FirebaseDatabaseHelper.getInstance().getTrabajadoresReference(empresaId)
                .child(trabajadorId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Aquí se podría leer un campo "horario" del trabajador
                            // Por ahora, mostramos el horario estático del layout
                            // Si en el futuro se agrega un campo horario, se puede actualizar dinámicamente
                            tvNotaHorario.setText("Horario laboral de lunes a viernes. Consulte con su administrador si hay cambios.");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Ignorar, el horario estático ya está visible
                    }
                });
    }
}
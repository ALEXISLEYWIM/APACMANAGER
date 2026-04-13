package com.example.apacmanager.JEFE;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.apacmanager.MODELS.Empresa;
import com.example.apacmanager.R;
import com.example.apacmanager.firebase.FirebaseDatabaseHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class GestionEmpresa extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextView tvNombreEmpresa, tvRuc, tvDireccion, tvTelefonoEmpresa;
    private MaterialButton btnEditarEmpresa, btnVerReportes, btnAsignarAdmin;
    private ProgressDialog progressDialog;

    private String empresaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_empresa);

        // Recibir el ID de la empresa enviado desde JefeDashboard
        if (getIntent().getExtras() != null) {
            empresaId = getIntent().getStringExtra("empresa_id");
        }

        if (empresaId == null || empresaId.isEmpty()) {
            Toast.makeText(this, "Error: ID de empresa no encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inicializar vistas
        toolbar = findViewById(R.id.toolbar);
        tvNombreEmpresa = findViewById(R.id.tvNombreEmpresa);
        tvRuc = findViewById(R.id.tvRuc);
        tvDireccion = findViewById(R.id.tvDireccion);
        tvTelefonoEmpresa = findViewById(R.id.tvTelefonoEmpresa);
        btnEditarEmpresa = findViewById(R.id.btnEditarEmpresa);
        btnVerReportes = findViewById(R.id.btnVerReportes);
        btnAsignarAdmin = findViewById(R.id.btnAsignarAdmin);

        // Configurar toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Gestión de Empresa");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        // Cargar datos de la empresa
        cargarDatosEmpresa();

        // Listeners de botones
        btnEditarEmpresa.setOnClickListener(v -> {
            // Abrir actividad para editar empresa (aún no implementada)
            Intent intent = new Intent(GestionEmpresa.this, EditarEmpresa.class);
            intent.putExtra("empresa_id", empresaId);
            startActivity(intent);
        });

        btnVerReportes.setOnClickListener(v -> {
            Intent intent = new Intent(GestionEmpresa.this, VerReportes.class);
            intent.putExtra("empresa_id", empresaId);
            startActivity(intent);
        });

        btnAsignarAdmin.setOnClickListener(v -> {
            Intent intent = new Intent(GestionEmpresa.this, AsignarAdmin.class);
            intent.putExtra("empresa_id", empresaId);
            startActivity(intent);
        });
    }

    private void cargarDatosEmpresa() {
        progressDialog.setMessage("Cargando datos de la empresa...");
        progressDialog.show();

        FirebaseDatabaseHelper.getInstance().getEmpresasReference()
                .child(empresaId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        progressDialog.dismiss();
                        if (snapshot.exists()) {
                            Empresa empresa = snapshot.getValue(Empresa.class);
                            if (empresa != null) {
                                // Mostrar los datos en los TextView
                                tvNombreEmpresa.setText(empresa.getNombre() != null ? empresa.getNombre() : "Sin nombre");
                                tvRuc.setText(empresa.getRuc() != null ? empresa.getRuc() : "No registrado");
                                tvDireccion.setText(empresa.getDireccion() != null ? empresa.getDireccion() : "No registrada");
                                tvTelefonoEmpresa.setText(empresa.getTelefono() != null ? empresa.getTelefono() : "No registrado");
                            } else {
                                Toast.makeText(GestionEmpresa.this,
                                        "Error al leer los datos de la empresa",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(GestionEmpresa.this,
                                    "Empresa no encontrada en la base de datos",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressDialog.dismiss();
                        Toast.makeText(GestionEmpresa.this,
                                "Error al cargar datos: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
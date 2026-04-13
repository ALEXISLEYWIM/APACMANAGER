package com.example.apacmanager.ADMIN;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.apacmanager.R;
import com.example.apacmanager.firebase.FirebaseDatabaseHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class AdminDashboard extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextView tvBienvenida, tvTrabajadoresActivos;
    private CardView cardGestionTrabajadores, cardGestionTareas, cardAsistencia, cardCatalogo;
    private BottomNavigationView bottomNavigation;
    private ProgressDialog progressDialog;

    private String usuarioUid;
    private String usuarioNombre;
    private String empresaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Recibir datos del Intent (enviados desde LoginActivity)
        if (getIntent().getExtras() != null) {
            usuarioUid = getIntent().getStringExtra("usuario_uid");
            usuarioNombre = getIntent().getStringExtra("usuario_nombre");
            empresaId = getIntent().getStringExtra("empresa_id");
        }

        if (usuarioUid == null || empresaId == null) {
            Toast.makeText(this, "Error: datos de usuario o empresa no encontrados", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inicializar vistas
        toolbar = findViewById(R.id.toolbar);
        tvBienvenida = findViewById(R.id.tvBienvenida);
        tvTrabajadoresActivos = findViewById(R.id.tvTrabajadoresActivos);
        cardGestionTrabajadores = findViewById(R.id.cardGestionTrabajadores);
        cardGestionTareas = findViewById(R.id.cardGestionTareas);
        cardAsistencia = findViewById(R.id.cardAsistencia);
        cardCatalogo = findViewById(R.id.cardCatalogo);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Configurar toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Dashboard Admin");
        }

        // Mostrar bienvenida
        tvBienvenida.setText("Bienvenido, " + (usuarioNombre != null ? usuarioNombre : "Administrador"));

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        // Cargar número de trabajadores activos
        cargarTrabajadoresActivos();

        // Listeners de las cards
        cardGestionTrabajadores.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboard.this, GestionTrabajadores.class)
                    .putExtra("empresa_id", empresaId));
        });

        cardGestionTareas.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboard.this, GestionTareas.class)
                    .putExtra("empresa_id", empresaId));
        });

        cardAsistencia.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboard.this, Asistencias.class)
                    .putExtra("empresa_id", empresaId));
        });

        cardCatalogo.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboard.this, Catalogo.class)
                    .putExtra("empresa_id", empresaId));
        });

        // Configurar bottom navigation (si existe el menú)
        if (bottomNavigation != null) {
            bottomNavigation.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_trabajadores) {
                    startActivity(new Intent(AdminDashboard.this, GestionTrabajadores.class)
                            .putExtra("empresa_id", empresaId));
                    return true;
                } else if (id == R.id.nav_tareas) {
                    startActivity(new Intent(AdminDashboard.this, GestionTareas.class)
                            .putExtra("empresa_id", empresaId));
                    return true;
                } else if (id == R.id.nav_asistencia) {
                    startActivity(new Intent(AdminDashboard.this, Asistencias.class)
                            .putExtra("empresa_id", empresaId));
                    return true;
                } else if (id == R.id.nav_catalogo) {
                    startActivity(new Intent(AdminDashboard.this, Catalogo.class)
                            .putExtra("empresa_id", empresaId));
                    return true;
                }
                return false;
            });
        }
    }

    private void cargarTrabajadoresActivos() {
        progressDialog.setMessage("Cargando trabajadores...");
        progressDialog.show();

        FirebaseDatabaseHelper.getInstance().getTrabajadoresReference(empresaId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        progressDialog.dismiss();
                        int contador = 0;
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Boolean activo = child.child("activo").getValue(Boolean.class);
                            if (activo != null && activo) {
                                contador++;
                            }
                        }
                        tvTrabajadoresActivos.setText(String.valueOf(contador));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressDialog.dismiss();
                        Toast.makeText(AdminDashboard.this,
                                "Error al cargar trabajadores: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                        tvTrabajadoresActivos.setText("0");
                    }
                });
    }
}
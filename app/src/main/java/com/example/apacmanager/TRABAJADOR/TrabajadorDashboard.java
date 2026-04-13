package com.example.apacmanager.TRABAJADOR;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.apacmanager.MODELS.Tarea;
import com.example.apacmanager.MODELS.Trabajador;
import com.example.apacmanager.R;
import com.example.apacmanager.firebase.FirebaseDatabaseHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TrabajadorDashboard extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextView tvBienvenida, tvTareaActual, tvHoraTarea;
    private CardView cardProximaTarea, cardVerTareas, cardMarcarAsistencia, cardVerHorario, cardVerSueldo;
    private BottomNavigationView bottomNavigation;
    private ProgressDialog progressDialog;

    private String usuarioUid;
    private String usuarioNombre;
    private String empresaId;
    private Trabajador trabajadorActual;

    private final SimpleDateFormat sdfHora = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private final SimpleDateFormat sdfFecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trabajador_dashboard);

        // Recibir extras desde LoginActivity
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
        cardProximaTarea = findViewById(R.id.cardProximaTarea);
        tvTareaActual = findViewById(R.id.tvTareaActual);
        tvHoraTarea = findViewById(R.id.tvHoraTarea);
        cardVerTareas = findViewById(R.id.cardVerTareas);
        cardMarcarAsistencia = findViewById(R.id.cardMarcarAsistencia);
        cardVerHorario = findViewById(R.id.cardVerHorario);
        cardVerSueldo = findViewById(R.id.cardVerSueldo);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Configurar toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Mi Panel");
        }

        tvBienvenida.setText("Bienvenido, " + (usuarioNombre != null ? usuarioNombre : "Trabajador"));

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        // Cargar datos del trabajador (para obtener sueldo, horario, etc.)
        cargarTrabajador();

        // Cargar la próxima tarea asignada
        cargarProximaTarea();

        // Listeners de las cards
        cardVerTareas.setOnClickListener(v -> {
            Intent intent = new Intent(TrabajadorDashboard.this, VerTareas.class);
            intent.putExtra("empresa_id", empresaId);
            intent.putExtra("trabajador_id", usuarioUid);
            startActivity(intent);
        });

        cardMarcarAsistencia.setOnClickListener(v -> {
            Intent intent = new Intent(TrabajadorDashboard.this, MarcarAsistencia.class);
            intent.putExtra("empresa_id", empresaId);
            intent.putExtra("trabajador_id", usuarioUid);
            startActivity(intent);
        });

        cardVerHorario.setOnClickListener(v -> {
            Intent intent = new Intent(TrabajadorDashboard.this, VerHorario.class);
            intent.putExtra("empresa_id", empresaId);
            intent.putExtra("trabajador_id", usuarioUid);
            startActivity(intent);
        });

        cardVerSueldo.setOnClickListener(v -> {
            Intent intent = new Intent(TrabajadorDashboard.this, PerfilTrabajador.class);
            intent.putExtra("empresa_id", empresaId);
            intent.putExtra("trabajador_id", usuarioUid);
            startActivity(intent);
        });

        // Bottom Navigation (opcional)
        if (bottomNavigation != null) {
            bottomNavigation.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_tareas) {
                    startActivity(new Intent(TrabajadorDashboard.this, VerTareas.class)
                            .putExtra("empresa_id", empresaId)
                            .putExtra("trabajador_id", usuarioUid));
                    return true;
                } else if (id == R.id.nav_asistencia) {
                    startActivity(new Intent(TrabajadorDashboard.this, MarcarAsistencia.class)
                            .putExtra("empresa_id", empresaId)
                            .putExtra("trabajador_id", usuarioUid));
                    return true;
                } else if (id == R.id.nav_horario) {
                    startActivity(new Intent(TrabajadorDashboard.this, VerHorario.class)
                            .putExtra("empresa_id", empresaId)
                            .putExtra("trabajador_id", usuarioUid));
                    return true;
                } else if (id == R.id.nav_sueldo) {
                    startActivity(new Intent(TrabajadorDashboard.this, PerfilTrabajador.class)
                            .putExtra("empresa_id", empresaId)
                            .putExtra("trabajador_id", usuarioUid));
                    return true;
                }
                return false;
            });
        }
    }

    private void cargarTrabajador() {
        FirebaseDatabaseHelper.getInstance().getTrabajadoresReference(empresaId)
                .child(usuarioUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            trabajadorActual = snapshot.getValue(Trabajador.class);
                            if (trabajadorActual != null) {
                                trabajadorActual.setTrabajadorId(usuarioUid);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // No crítico, ignorar
                    }
                });
    }

    private void cargarProximaTarea() {
        progressDialog.setMessage("Cargando próximas tareas...");
        progressDialog.show();

        FirebaseDatabaseHelper.getInstance().getTareasReference(empresaId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        progressDialog.dismiss();
                        List<Tarea> misTareas = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Tarea t = child.getValue(Tarea.class);
                            if (t != null && t.getTrabajadoresIds() != null && t.getTrabajadoresIds().contains(usuarioUid)) {
                                // Solo tareas pendientes o en progreso
                                if (!"Completada".equals(t.getEstado())) {
                                    t.setTareaId(child.getKey());
                                    misTareas.add(t);
                                }
                            }
                        }
                        // Ordenar por fecha límite (más cercana primero)
                        Collections.sort(misTareas, (a, b) -> Long.compare(a.getFechaLimite(), b.getFechaLimite()));
                        if (!misTareas.isEmpty()) {
                            Tarea proxima = misTareas.get(0);
                            tvTareaActual.setText(proxima.getTitulo());
                            String fechaLimite = sdfFecha.format(new Date(proxima.getFechaLimite()));
                            tvHoraTarea.setText("Fecha límite: " + fechaLimite);
                        } else {
                            tvTareaActual.setText("No hay tareas pendientes");
                            tvHoraTarea.setText("¡Buen trabajo!");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressDialog.dismiss();
                        Toast.makeText(TrabajadorDashboard.this,
                                "Error al cargar tareas: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
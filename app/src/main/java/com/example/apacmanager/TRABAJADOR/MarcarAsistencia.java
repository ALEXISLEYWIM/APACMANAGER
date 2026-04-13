package com.example.apacmanager.TRABAJADOR;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.apacmanager.MODELS.Asistencia;
import com.example.apacmanager.R;
import com.example.apacmanager.firebase.FirebaseDatabaseHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MarcarAsistencia extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextView tvHoraActual, tvFechaActual, tvEstadoActual, tvHoraMarcada, tvInfoTolerancia;
    private Button btnMarcarEntrada, btnMarcarSalida;
    private ProgressDialog progressDialog;

    private String empresaId;
    private String trabajadorId;
    private Asistencia asistenciaHoy;
    private String fechaKey; // para la clave en Firebase

    private final SimpleDateFormat sdfHora = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    private final SimpleDateFormat sdfFecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat sdfFechaKey = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private Handler handler = new Handler();
    private Runnable actualizarHora;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marcar_asistencia);

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
        tvHoraActual = findViewById(R.id.tvHoraActual);
        tvFechaActual = findViewById(R.id.tvFechaActual);
        tvEstadoActual = findViewById(R.id.tvEstadoActual);
        tvHoraMarcada = findViewById(R.id.tvHoraMarcada);
        tvInfoTolerancia = findViewById(R.id.tvInfoTolerancia);
        btnMarcarEntrada = findViewById(R.id.btnMarcarEntrada);
        btnMarcarSalida = findViewById(R.id.btnMarcarSalida);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Marcar Asistencia");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        tvInfoTolerancia.setText("• Tolerancia de 5 minutos para entrada\n• Si marcas después de los 5 minutos se registrará tardanza");

        // Obtener fecha actual para la clave del documento
        fechaKey = sdfFechaKey.format(new Date());
        tvFechaActual.setText(sdfFecha.format(new Date()));

        cargarAsistenciaHoy();

        actualizarHora = new Runnable() {
            @Override
            public void run() {
                Calendar cal = Calendar.getInstance();
                tvHoraActual.setText(sdfHora.format(cal.getTime()));
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(actualizarHora);

        btnMarcarEntrada.setOnClickListener(v -> marcarEntrada());
        btnMarcarSalida.setOnClickListener(v -> marcarSalida());
    }

    private void cargarAsistenciaHoy() {
        progressDialog.setMessage("Cargando estado...");
        progressDialog.show();

        DatabaseReference ref = FirebaseDatabaseHelper.getInstance().getAsistenciasReference(empresaId)
                .child(trabajadorId).child(fechaKey);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressDialog.dismiss();
                if (snapshot.exists()) {
                    asistenciaHoy = snapshot.getValue(Asistencia.class);
                    actualizarUI();
                } else {
                    asistenciaHoy = null;
                    tvEstadoActual.setText("Aún no has marcado entrada");
                    tvHoraMarcada.setText("");
                    btnMarcarEntrada.setEnabled(true);
                    btnMarcarSalida.setEnabled(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                Toast.makeText(MarcarAsistencia.this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void actualizarUI() {
        if (asistenciaHoy != null) {
            Date entrada = asistenciaHoy.getHoraEntrada();
            Date salida = asistenciaHoy.getHoraSalida();
            if (entrada != null && salida == null) {
                String horaEntrada = sdfHora.format(entrada);
                tvEstadoActual.setText("Entrada marcada");
                tvHoraMarcada.setText("Hora entrada: " + horaEntrada);
                btnMarcarEntrada.setEnabled(false);
                btnMarcarSalida.setEnabled(true);
            } else if (entrada != null && salida != null) {
                String horaEntrada = sdfHora.format(entrada);
                String horaSalida = sdfHora.format(salida);
                tvEstadoActual.setText("Jornada completada");
                tvHoraMarcada.setText("Entrada: " + horaEntrada + " | Salida: " + horaSalida);
                btnMarcarEntrada.setEnabled(false);
                btnMarcarSalida.setEnabled(false);
            }
        } else {
            tvEstadoActual.setText("Aún no has marcado entrada");
            tvHoraMarcada.setText("");
            btnMarcarEntrada.setEnabled(true);
            btnMarcarSalida.setEnabled(false);
        }
    }

    private void marcarEntrada() {
        progressDialog.setMessage("Marcando entrada...");
        progressDialog.show();

        Date ahora = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(ahora);
        int hora = cal.get(Calendar.HOUR_OF_DAY);
        int minuto = cal.get(Calendar.MINUTE);

        // Hora base: 9:00 AM (540 minutos desde medianoche)
        int minutosBase = 9 * 60;
        int minutosActual = hora * 60 + minuto;
        boolean tardanza = minutosActual > (minutosBase + 5); // tolerancia 5 min

        String estado = tardanza ? "Tarde" : "Presente";

        Asistencia nueva = new Asistencia();
        nueva.setAsistenciaId(fechaKey);
        nueva.setTrabajadorId(trabajadorId);
        nueva.setEmpresaId(empresaId);
        nueva.setFecha(ahora);
        nueva.setHoraEntrada(ahora);
        nueva.setHoraSalida(null);
        nueva.setEstado(estado);

        DatabaseReference ref = FirebaseDatabaseHelper.getInstance().getAsistenciasReference(empresaId)
                .child(trabajadorId).child(fechaKey);
        ref.setValue(nueva)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(MarcarAsistencia.this,
                            tardanza ? "Entrada marcada con tardanza" : "Entrada marcada a tiempo",
                            Toast.LENGTH_SHORT).show();
                    cargarAsistenciaHoy();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(MarcarAsistencia.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void marcarSalida() {
        if (asistenciaHoy == null || asistenciaHoy.getHoraEntrada() == null) {
            Toast.makeText(this, "Debes marcar entrada primero", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Marcando salida...");
        progressDialog.show();

        Date ahora = new Date();

        DatabaseReference ref = FirebaseDatabaseHelper.getInstance().getAsistenciasReference(empresaId)
                .child(trabajadorId).child(fechaKey);
        ref.child("horaSalida").setValue(ahora)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(MarcarAsistencia.this, "Salida marcada", Toast.LENGTH_SHORT).show();
                    cargarAsistenciaHoy();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(MarcarAsistencia.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(actualizarHora);
    }
}
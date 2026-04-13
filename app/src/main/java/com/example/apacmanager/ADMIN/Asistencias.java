package com.example.apacmanager.ADMIN;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apacmanager.MODELS.Asistencia; // modelo
import com.example.apacmanager.MODELS.Trabajador;
import com.example.apacmanager.R;
import com.example.apacmanager.firebase.FirebaseDatabaseHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Asistencias extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextView tvFechaActual;
    private TextInputEditText etBuscarAsistencia;
    private RecyclerView recyclerAsistencia;
    private TextView tvNoRegistros;
    private FloatingActionButton fabReporteAsistencia;
    private ProgressDialog progressDialog;

    private String empresaId;
    private List<Asistencia> listaAsistencias = new ArrayList<>();
    private List<Asistencia> listaFiltrada = new ArrayList<>();
    private Map<String, String> mapTrabajadores = new HashMap<>();
    private AsistenciaAdapter adapter;

    private final SimpleDateFormat sdfFecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat sdfHora = new SimpleDateFormat("HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asistencias); // Asegúrate que el XML se llame así

        if (getIntent().getExtras() != null) {
            empresaId = getIntent().getStringExtra("empresa_id");
        }

        if (empresaId == null || empresaId.isEmpty()) {
            Toast.makeText(this, "Error: ID de empresa no encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        toolbar = findViewById(R.id.toolbar);
        tvFechaActual = findViewById(R.id.tvFechaActual);
        etBuscarAsistencia = findViewById(R.id.etBuscarAsistencia);
        recyclerAsistencia = findViewById(R.id.recyclerAsistencia);
        tvNoRegistros = findViewById(R.id.tvNoRegistros);
        fabReporteAsistencia = findViewById(R.id.fabReporteAsistencia);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Asistencias");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        Calendar cal = Calendar.getInstance();
        tvFechaActual.setText(sdfFecha.format(cal.getTime()));

        recyclerAsistencia.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AsistenciaAdapter();
        recyclerAsistencia.setAdapter(adapter);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        cargarTrabajadores();

        etBuscarAsistencia.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarAsistencias(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        fabReporteAsistencia.setOnClickListener(v ->
                Toast.makeText(Asistencias.this, "Reporte de asistencias (próximamente)", Toast.LENGTH_SHORT).show());
    }

    private void cargarTrabajadores() {
        progressDialog.setMessage("Cargando trabajadores...");
        progressDialog.show();

        FirebaseDatabaseHelper.getInstance().getTrabajadoresReference(empresaId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        mapTrabajadores.clear();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Trabajador t = child.getValue(Trabajador.class);
                            if (t != null) {
                                mapTrabajadores.put(child.getKey(), t.getNombre());
                            }
                        }
                        cargarAsistencias();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressDialog.dismiss();
                        Toast.makeText(Asistencias.this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        cargarAsistencias();
                    }
                });
    }

    private void cargarAsistencias() {
        FirebaseDatabaseHelper.getInstance().getAsistenciasReference(empresaId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        progressDialog.dismiss();
                        listaAsistencias.clear();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Asistencia a = child.getValue(Asistencia.class);
                            if (a != null) {
                                a.setAsistenciaId(child.getKey());
                                listaAsistencias.add(a);
                            }
                        }
                        filtrarAsistencias(etBuscarAsistencia.getText().toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressDialog.dismiss();
                        Toast.makeText(Asistencias.this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void filtrarAsistencias(String query) {
        listaFiltrada.clear();
        if (query.isEmpty()) {
            listaFiltrada.addAll(listaAsistencias);
        } else {
            String lower = query.toLowerCase();
            for (Asistencia a : listaAsistencias) {
                String nombre = mapTrabajadores.getOrDefault(a.getTrabajadorId(), "");
                if (nombre.toLowerCase().contains(lower)) {
                    listaFiltrada.add(a);
                }
            }
        }
        adapter.setAsistencias(listaFiltrada);
        tvNoRegistros.setVisibility(listaFiltrada.isEmpty() ? View.VISIBLE : View.GONE);
    }

    // Adaptador
    private class AsistenciaAdapter extends RecyclerView.Adapter<AsistenciaAdapter.ViewHolder> {
        private List<Asistencia> asistencias = new ArrayList<>();

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Asistencia a = asistencias.get(position);
            String nombre = mapTrabajadores.getOrDefault(a.getTrabajadorId(), "Desconocido");
            String estado = a.getEstado() != null ? a.getEstado() : "Ausente";

            String fechaStr = a.getFecha() != null ? sdfFecha.format(a.getFecha()) : "";
            String horaEntrada = a.getHoraEntrada() != null ? sdfHora.format(a.getHoraEntrada()) : "--:--";
            String horaSalida = a.getHoraSalida() != null ? sdfHora.format(a.getHoraSalida()) : "--:--";

            holder.text1.setText(nombre + " - " + estado);
            holder.text2.setText(fechaStr + " | E: " + horaEntrada + " | S: " + horaSalida);
        }

        @Override
        public int getItemCount() {
            return asistencias.size();
        }

        public void setAsistencias(List<Asistencia> asistencias) {
            this.asistencias = asistencias;
            notifyDataSetChanged();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView text1, text2;
            ViewHolder(@NonNull View itemView) {
                super(itemView);
                text1 = itemView.findViewById(android.R.id.text1);
                text2 = itemView.findViewById(android.R.id.text2);
            }
        }
    }
}
package com.example.apacmanager.ADMIN;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apacmanager.MODELS.Tarea;
import com.example.apacmanager.MODELS.Trabajador;
import com.example.apacmanager.R;
import com.example.apacmanager.firebase.FirebaseDatabaseHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class AgregarTarea extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextInputEditText etNombreTarea, etDescripcion, etFechaLimite;
    private RecyclerView recyclerTrabajadores;
    private MaterialButton btnGuardarTarea, btnCancelar;
    private ProgressDialog progressDialog;

    private String empresaId;
    private String adminUid;
    private List<Trabajador> listaTrabajadores = new ArrayList<>();
    private List<String> trabajadoresSeleccionados = new ArrayList<>();
    private TrabajadorAdapter adapter;

    private long fechaLimiteMillis = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_tarea);

        // Recibir datos del Intent
        if (getIntent().getExtras() != null) {
            empresaId = getIntent().getStringExtra("empresa_id");
            adminUid = getIntent().getStringExtra("usuario_uid");
        }

        if (empresaId == null) {
            Toast.makeText(this, "Error: ID de empresa no encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inicializar vistas
        toolbar = findViewById(R.id.toolbar);
        etNombreTarea = findViewById(R.id.etNombreTarea);
        etDescripcion = findViewById(R.id.etDescripcion);
        etFechaLimite = findViewById(R.id.etFechaLimite);
        recyclerTrabajadores = findViewById(R.id.recyclerTrabajadores);
        btnGuardarTarea = findViewById(R.id.btnGuardarTarea);
        btnCancelar = findViewById(R.id.btnCancelar);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Agregar Tarea");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        recyclerTrabajadores.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TrabajadorAdapter();
        recyclerTrabajadores.setAdapter(adapter);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        // Selección de fecha límite
        etFechaLimite.setOnClickListener(v -> mostrarDatePicker());

        // Cargar lista de trabajadores de la empresa
        cargarTrabajadores();

        btnGuardarTarea.setOnClickListener(v -> guardarTarea());
        btnCancelar.setOnClickListener(v -> finish());
    }

    private void mostrarDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            cal.set(year, month, dayOfMonth);
            fechaLimiteMillis = cal.getTimeInMillis();
            // Formato simple para mostrar
            String fecha = dayOfMonth + "/" + (month + 1) + "/" + year;
            etFechaLimite.setText(fecha);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void cargarTrabajadores() {
        progressDialog.setMessage("Cargando trabajadores...");
        progressDialog.show();

        FirebaseDatabaseHelper.getInstance().getTrabajadoresReference(empresaId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        progressDialog.dismiss();
                        listaTrabajadores.clear();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Trabajador t = child.getValue(Trabajador.class);
                            if (t != null && t.isActivo()) {
                                t.setTrabajadorId(child.getKey());
                                listaTrabajadores.add(t);
                            }
                        }
                        adapter.setTrabajadores(listaTrabajadores);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressDialog.dismiss();
                        Toast.makeText(AgregarTarea.this,
                                "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void guardarTarea() {
        String titulo = etNombreTarea.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();

        if (titulo.isEmpty()) {
            etNombreTarea.setError("Ingrese título");
            etNombreTarea.requestFocus();
            return;
        }
        if (descripcion.isEmpty()) {
            etDescripcion.setError("Ingrese descripción");
            etDescripcion.requestFocus();
            return;
        }
        if (fechaLimiteMillis == 0) {
            Toast.makeText(this, "Seleccione una fecha límite", Toast.LENGTH_SHORT).show();
            return;
        }
        if (trabajadoresSeleccionados.isEmpty()) {
            Toast.makeText(this, "Seleccione al menos un trabajador", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Guardando tarea...");
        progressDialog.show();

        String tareaId = UUID.randomUUID().toString();
        Tarea tarea = new Tarea();
        tarea.setTareaId(tareaId);
        tarea.setTitulo(titulo);
        tarea.setDescripcion(descripcion);
        tarea.setEmpresaId(empresaId);
        tarea.setEstado("Pendiente");
        tarea.setFechaAsignacion(System.currentTimeMillis());
        tarea.setFechaLimite(fechaLimiteMillis);
        tarea.setTrabajadoresIds(trabajadoresSeleccionados);
        tarea.setCreadoPor(adminUid != null ? adminUid : "");
        tarea.setFechaActualizacion(System.currentTimeMillis());

        FirebaseDatabaseHelper.getInstance().getTareasReference(empresaId)
                .child(tareaId)
                .setValue(tarea)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(AgregarTarea.this, "Tarea creada exitosamente", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(AgregarTarea.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // Adaptador para lista de trabajadores con checkbox
    private class TrabajadorAdapter extends RecyclerView.Adapter<TrabajadorAdapter.ViewHolder> {
        private List<Trabajador> trabajadores = new ArrayList<>();

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_multiple_choice, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Trabajador t = trabajadores.get(position);
            holder.checkBox.setText(t.getNombre());
            holder.checkBox.setChecked(trabajadoresSeleccionados.contains(t.getTrabajadorId()));
            holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (!trabajadoresSeleccionados.contains(t.getTrabajadorId())) {
                        trabajadoresSeleccionados.add(t.getTrabajadorId());
                    }
                } else {
                    trabajadoresSeleccionados.remove(t.getTrabajadorId());
                }
            });
        }

        @Override
        public int getItemCount() {
            return trabajadores.size();
        }

        public void setTrabajadores(List<Trabajador> trabajadores) {
            this.trabajadores = trabajadores;
            notifyDataSetChanged();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            CheckBox checkBox;
            ViewHolder(View itemView) {
                super(itemView);
                checkBox = itemView.findViewById(android.R.id.text1);
            }
        }
    }
}
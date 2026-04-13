package com.example.apacmanager.ADMIN;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditarTarea extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextInputEditText etNombreTarea, etDescripcion, etFechaLimite;
    private AutoCompleteTextView spinnerEstado;
    private RecyclerView recyclerTrabajadores;
    private MaterialButton btnActualizarTarea, btnEliminarTarea, btnCancelar;
    private ProgressDialog progressDialog;

    private String empresaId;
    private String tareaId;
    private Tarea tareaActual;
    private List<Trabajador> listaTrabajadores = new ArrayList<>();
    private List<String> trabajadoresSeleccionados = new ArrayList<>();
    private TrabajadorAdapter adapter;

    private long fechaLimiteMillis = 0;
    private String[] estados = {"Pendiente", "En progreso", "Completada"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_tarea);

        if (getIntent().getExtras() != null) {
            empresaId = getIntent().getStringExtra("empresa_id");
            tareaId = getIntent().getStringExtra("tarea_id");
        }

        if (empresaId == null || tareaId == null) {
            Toast.makeText(this, "Error: datos incompletos", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inicializar vistas
        toolbar = findViewById(R.id.toolbar);
        etNombreTarea = findViewById(R.id.etNombreTarea);
        etDescripcion = findViewById(R.id.etDescripcion);
        etFechaLimite = findViewById(R.id.etFechaLimite);
        spinnerEstado = findViewById(R.id.spinnerEstado);
        recyclerTrabajadores = findViewById(R.id.recyclerTrabajadores);
        btnActualizarTarea = findViewById(R.id.btnActualizarTarea);
        btnEliminarTarea = findViewById(R.id.btnEliminarTarea);
        btnCancelar = findViewById(R.id.btnCancelar);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Editar Tarea");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Configurar spinner estado
        ArrayAdapter<String> estadoAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, estados);
        spinnerEstado.setAdapter(estadoAdapter);

        recyclerTrabajadores.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TrabajadorAdapter();
        recyclerTrabajadores.setAdapter(adapter);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        etFechaLimite.setOnClickListener(v -> mostrarDatePicker());

        cargarDatos();

        btnActualizarTarea.setOnClickListener(v -> actualizarTarea());
        btnEliminarTarea.setOnClickListener(v -> eliminarTarea());
        btnCancelar.setOnClickListener(v -> finish());
    }

    private void mostrarDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            cal.set(year, month, dayOfMonth);
            fechaLimiteMillis = cal.getTimeInMillis();
            String fecha = dayOfMonth + "/" + (month + 1) + "/" + year;
            etFechaLimite.setText(fecha);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void cargarDatos() {
        progressDialog.setMessage("Cargando datos...");
        progressDialog.show();

        // Cargar tarea actual
        FirebaseDatabaseHelper.getInstance().getTareasReference(empresaId)
                .child(tareaId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            tareaActual = snapshot.getValue(Tarea.class);
                            if (tareaActual != null) {
                                tareaActual.setTareaId(tareaId);
                                cargarTrabajadores();
                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(EditarTarea.this, "Error al leer tarea", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(EditarTarea.this, "Tarea no encontrada", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressDialog.dismiss();
                        Toast.makeText(EditarTarea.this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
    }

    private void cargarTrabajadores() {
        FirebaseDatabaseHelper.getInstance().getTrabajadoresReference(empresaId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        listaTrabajadores.clear();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Trabajador t = child.getValue(Trabajador.class);
                            if (t != null && t.isActivo()) {
                                t.setTrabajadorId(child.getKey());
                                listaTrabajadores.add(t);
                            }
                        }
                        // Inicializar seleccionados con los IDs actuales de la tarea
                        trabajadoresSeleccionados.clear();
                        if (tareaActual.getTrabajadoresIds() != null) {
                            trabajadoresSeleccionados.addAll(tareaActual.getTrabajadoresIds());
                        }
                        adapter.setTrabajadores(listaTrabajadores);

                        // Mostrar datos en campos
                        etNombreTarea.setText(tareaActual.getTitulo());
                        etDescripcion.setText(tareaActual.getDescripcion());
                        // Mostrar fecha límite
                        if (tareaActual.getFechaLimite() > 0) {
                            fechaLimiteMillis = tareaActual.getFechaLimite();
                            Calendar cal = Calendar.getInstance();
                            cal.setTimeInMillis(fechaLimiteMillis);
                            String fecha = cal.get(Calendar.DAY_OF_MONTH) + "/" + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR);
                            etFechaLimite.setText(fecha);
                        }
                        // Seleccionar estado en spinner
                        String estadoActual = tareaActual.getEstado();
                        for (int i = 0; i < estados.length; i++) {
                            if (estados[i].equals(estadoActual)) {
                                spinnerEstado.setText(estados[i], false);
                                break;
                            }
                        }
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressDialog.dismiss();
                        Toast.makeText(EditarTarea.this, "Error al cargar trabajadores", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void actualizarTarea() {
        String titulo = etNombreTarea.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String estado = spinnerEstado.getText().toString();

        if (titulo.isEmpty()) {
            etNombreTarea.setError("Ingrese título");
            return;
        }
        if (descripcion.isEmpty()) {
            etDescripcion.setError("Ingrese descripción");
            return;
        }
        if (fechaLimiteMillis == 0) {
            Toast.makeText(this, "Seleccione fecha límite", Toast.LENGTH_SHORT).show();
            return;
        }
        if (trabajadoresSeleccionados.isEmpty()) {
            Toast.makeText(this, "Asigne al menos un trabajador", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Actualizando tarea...");
        progressDialog.show();

        Map<String, Object> updates = new HashMap<>();
        updates.put("titulo", titulo);
        updates.put("descripcion", descripcion);
        updates.put("estado", estado);
        updates.put("fechaLimite", fechaLimiteMillis);
        updates.put("trabajadoresIds", trabajadoresSeleccionados);
        updates.put("fechaActualizacion", System.currentTimeMillis());

        FirebaseDatabaseHelper.getInstance().getTareasReference(empresaId)
                .child(tareaId)
                .updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(EditarTarea.this, "Tarea actualizada", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(EditarTarea.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void eliminarTarea() {
        progressDialog.setMessage("Eliminando tarea...");
        progressDialog.show();

        FirebaseDatabaseHelper.getInstance().getTareasReference(empresaId)
                .child(tareaId)
                .removeValue()
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(EditarTarea.this, "Tarea eliminada", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(EditarTarea.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // Adaptador con checkbox para selección múltiple
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
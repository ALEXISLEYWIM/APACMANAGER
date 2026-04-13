package com.example.apacmanager.JEFE;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AsignarAdmin extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextInputEditText etBuscarTrabajador;
    private RecyclerView recyclerTrabajadores;
    private TextView tvNoTrabajadores;
    private MaterialButton btnAsignarAdmin;
    private ProgressDialog progressDialog;

    private String empresaId;
    private List<Trabajador> listaTrabajadores = new ArrayList<>();
    private TrabajadorAdapter adapter;
    private String trabajadorSeleccionadoId = null;
    private String trabajadorSeleccionadoNombre = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asignar_admin);

        // Recibir ID de la empresa
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
        etBuscarTrabajador = findViewById(R.id.etBuscarTrabajador);
        recyclerTrabajadores = findViewById(R.id.recyclerTrabajadores);
        tvNoTrabajadores = findViewById(R.id.tvNoTrabajadores);
        btnAsignarAdmin = findViewById(R.id.btnAsignarAdmin);

        // Configurar toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Asignar Administrador");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        recyclerTrabajadores.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TrabajadorAdapter();
        recyclerTrabajadores.setAdapter(adapter);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        // Cargar lista de trabajadores de la empresa
        cargarTrabajadores();

        // Filtro de búsqueda
        etBuscarTrabajador.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarTrabajadores(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Botón confirmar asignación
        btnAsignarAdmin.setOnClickListener(v -> {
            if (trabajadorSeleccionadoId == null) {
                Toast.makeText(AsignarAdmin.this,
                        "Selecciona un trabajador primero", Toast.LENGTH_SHORT).show();
                return;
            }
            confirmarAsignacion();
        });
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
                            Trabajador trabajador = child.getValue(Trabajador.class);
                            if (trabajador != null) {
                                trabajador.setTrabajadorId(child.getKey());
                                listaTrabajadores.add(trabajador);
                            }
                        }
                        filtrarTrabajadores(etBuscarTrabajador.getText().toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressDialog.dismiss();
                        Toast.makeText(AsignarAdmin.this,
                                "Error al cargar trabajadores: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void filtrarTrabajadores(String query) {
        List<Trabajador> filtrados = new ArrayList<>();
        if (query.isEmpty()) {
            filtrados.addAll(listaTrabajadores);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Trabajador t : listaTrabajadores) {
                if (t.getNombre() != null && t.getNombre().toLowerCase().contains(lowerQuery)) {
                    filtrados.add(t);
                }
            }
        }
        adapter.setTrabajadores(filtrados);
        tvNoTrabajadores.setVisibility(filtrados.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void confirmarAsignacion() {
        progressDialog.setMessage("Asignando rol de administrador...");
        progressDialog.show();

        // 1. Actualizar el rol del usuario en la colección "users"
        FirebaseDatabaseHelper.getInstance().getUsersReference()
                .child(trabajadorSeleccionadoId)
                .child("rol")
                .setValue("Administrador")
                .addOnSuccessListener(aVoid -> {
                    // 2. Opcional: actualizar también en la subcolección de trabajadores (si quieres guardar el rol)
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("rol", "Administrador");
                    FirebaseDatabaseHelper.getInstance().getTrabajadoresReference(empresaId)
                            .child(trabajadorSeleccionadoId)
                            .updateChildren(updates)
                            .addOnSuccessListener(aVoid2 -> {
                                progressDialog.dismiss();
                                Toast.makeText(AsignarAdmin.this,
                                        trabajadorSeleccionadoNombre + " ahora es Administrador",
                                        Toast.LENGTH_LONG).show();
                                finish(); // Volver a la pantalla anterior
                            })
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Toast.makeText(AsignarAdmin.this,
                                        "Error al actualizar trabajador: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(AsignarAdmin.this,
                            "Error al asignar administrador: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    // Adaptador interno para RecyclerView
    private class TrabajadorAdapter extends RecyclerView.Adapter<TrabajadorAdapter.TrabajadorViewHolder> {
        private List<Trabajador> trabajadores = new ArrayList<>();

        @NonNull
        @Override
        public TrabajadorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_single_choice, parent, false);
            return new TrabajadorViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TrabajadorViewHolder holder, int position) {
            Trabajador t = trabajadores.get(position);
            holder.text1.setText(t.getNombre() + " - " + t.getCargo());
            holder.itemView.setOnClickListener(v -> {
                // Limpiar selección anterior
                if (trabajadorSeleccionadoId != null) {
                    notifyDataSetChanged(); // refresca para quitar el highlight
                }
                trabajadorSeleccionadoId = t.getTrabajadorId();
                trabajadorSeleccionadoNombre = t.getNombre();
                // Opcional: mostrar un check visual
                Toast.makeText(AsignarAdmin.this,
                        "Seleccionado: " + t.getNombre(), Toast.LENGTH_SHORT).show();
                // Resaltar el item seleccionado (cambiar fondo)
                holder.itemView.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            });
        }

        @Override
        public int getItemCount() {
            return trabajadores.size();
        }

        public void setTrabajadores(List<Trabajador> trabajadores) {
            this.trabajadores = trabajadores;
            notifyDataSetChanged();
            // Limpiar selección al actualizar lista
            trabajadorSeleccionadoId = null;
            trabajadorSeleccionadoNombre = null;
        }

        class TrabajadorViewHolder extends RecyclerView.ViewHolder {
            TextView text1;
            public TrabajadorViewHolder(@NonNull View itemView) {
                super(itemView);
                text1 = itemView.findViewById(android.R.id.text1);
            }
        }
    }
}
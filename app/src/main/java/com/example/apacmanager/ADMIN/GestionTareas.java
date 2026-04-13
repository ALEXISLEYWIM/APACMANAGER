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

import com.example.apacmanager.MODELS.Tarea;
import com.example.apacmanager.MODELS.Trabajador;
import com.example.apacmanager.R;
import com.example.apacmanager.firebase.FirebaseDatabaseHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GestionTareas extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextInputEditText etBuscarTarea;
    private TabLayout tabLayoutTareas;
    private RecyclerView recyclerTareas;
    private TextView tvNoTareas;
    private FloatingActionButton fabAgregarTarea;
    private ProgressDialog progressDialog;

    private String empresaId;
    private List<Tarea> listaTareas = new ArrayList<>();
    private List<Tarea> listaFiltrada = new ArrayList<>();
    private TareaAdapter adapter;
    private Map<String, String> mapTrabajadores = new HashMap<>(); // id -> nombre

    private int currentTab = 0; // 0=Todas, 1=Pendientes, 2=Asignadas (según TabLayout)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_tareas);

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
        etBuscarTarea = findViewById(R.id.etBuscarTarea);
        tabLayoutTareas = findViewById(R.id.tabLayoutTareas);
        recyclerTareas = findViewById(R.id.recyclerTareas);
        tvNoTareas = findViewById(R.id.tvNoTareas);
        fabAgregarTarea = findViewById(R.id.fabAgregarTarea);

        // Configurar toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Gestión de Tareas");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        recyclerTareas.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TareaAdapter();
        recyclerTareas.setAdapter(adapter);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        // Cargar trabajadores (para mostrar nombres)
        cargarTrabajadores();

        // Configurar búsqueda
        etBuscarTarea.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarTareas();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Configurar tabs
        tabLayoutTareas.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition();
                filtrarTareas();
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // FAB para agregar nueva tarea
        fabAgregarTarea.setOnClickListener(v -> {
            Intent intent = new Intent(GestionTareas.this, AgregarTarea.class);
            intent.putExtra("empresa_id", empresaId);
            startActivity(intent);
        });
    }

    private void cargarTrabajadores() {
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
                        // Una vez cargados los trabajadores, cargamos las tareas
                        cargarTareas();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(GestionTareas.this,
                                "Error al cargar trabajadores: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        cargarTareas(); // Intentar cargar tareas de todas formas
                    }
                });
    }

    private void cargarTareas() {
        progressDialog.setMessage("Cargando tareas...");
        progressDialog.show();

        FirebaseDatabaseHelper.getInstance().getTareasReference(empresaId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        progressDialog.dismiss();
                        listaTareas.clear();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Tarea tarea = child.getValue(Tarea.class);
                            if (tarea != null) {
                                tarea.setTareaId(child.getKey());
                                listaTareas.add(tarea);
                            }
                        }
                        filtrarTareas();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressDialog.dismiss();
                        Toast.makeText(GestionTareas.this,
                                "Error al cargar tareas: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void filtrarTareas() {
        String query = etBuscarTarea.getText().toString().toLowerCase();
        listaFiltrada.clear();

        for (Tarea t : listaTareas) {
            // Filtro por tab
            boolean coincideTab = true;
            switch (currentTab) {
                case 1: // Pendientes (estado != "Completada")
                    coincideTab = !"Completada".equals(t.getEstado());
                    break;
                case 2: // Asignadas (estado = "Asignada" o similar)
                    coincideTab = "Asignada".equals(t.getEstado()) || "En Proceso".equals(t.getEstado());
                    break;
                default: // Todas
                    coincideTab = true;
                    break;
            }
            if (!coincideTab) continue;

            // Filtro por texto
            if (!query.isEmpty()) {
                String titulo = t.getTitulo() != null ? t.getTitulo().toLowerCase() : "";
                String desc = t.getDescripcion() != null ? t.getDescripcion().toLowerCase() : "";
                if (!titulo.contains(query) && !desc.contains(query)) {
                    continue;
                }
            }
            listaFiltrada.add(t);
        }

        adapter.setTareas(listaFiltrada);
        tvNoTareas.setVisibility(listaFiltrada.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarTrabajadores(); // Recargar al volver (si se agregó/edió)
    }

    // Adaptador de tareas
    private class TareaAdapter extends RecyclerView.Adapter<TareaAdapter.TareaViewHolder> {
        private List<Tarea> tareas = new ArrayList<>();

        @NonNull
        @Override
        public TareaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
            return new TareaViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TareaViewHolder holder, int position) {
            Tarea t = tareas.get(position);
            String nombreTrabajador = mapTrabajadores.getOrDefault(t.getTrabajadoresIds(), "Sin asignar");
            String estado = t.getEstado() != null ? t.getEstado() : "Pendiente";
            holder.text1.setText(t.getTitulo());
            holder.text2.setText(nombreTrabajador + " - " + estado);
            holder.itemView.setOnClickListener(v -> {
                // Abrir detalle/edición de tarea (a implementar)
                Toast.makeText(GestionTareas.this,
                        "Editar tarea - Próximamente", Toast.LENGTH_SHORT).show();
                // Intent intent = new Intent(GestionTareas.this, EditarTarea.class);
                // intent.putExtra("empresa_id", empresaId);
                // intent.putExtra("tarea_id", t.getTareaId());
                // startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return tareas.size();
        }

        public void setTareas(List<Tarea> tareas) {
            this.tareas = tareas;
            notifyDataSetChanged();
        }

        class TareaViewHolder extends RecyclerView.ViewHolder {
            TextView text1, text2;
            public TareaViewHolder(@NonNull View itemView) {
                super(itemView);
                text1 = itemView.findViewById(android.R.id.text1);
                text2 = itemView.findViewById(android.R.id.text2);
            }
        }
    }
}
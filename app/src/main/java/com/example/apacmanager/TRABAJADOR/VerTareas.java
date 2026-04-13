package com.example.apacmanager.TRABAJADOR;

import android.app.ProgressDialog;
import android.os.Bundle;
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
import com.example.apacmanager.R;
import com.example.apacmanager.firebase.FirebaseDatabaseHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VerTareas extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TabLayout tabLayout;
    private RecyclerView recyclerTareas;
    private TextView tvNoTareas;
    private ProgressDialog progressDialog;

    private String empresaId;
    private String trabajadorId;
    private List<Tarea> listaTareas = new ArrayList<>();
    private List<Tarea> listaFiltrada = new ArrayList<>();
    private TareaAdapter adapter;
    private int currentTab = 0; // 0=Todas, 1=Pendientes, 2=Completadas

    private final SimpleDateFormat sdfFecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_tareas);

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
        tabLayout = findViewById(R.id.tabLayout);
        recyclerTareas = findViewById(R.id.recyclerTareas);
        tvNoTareas = findViewById(R.id.tvNoTareas);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Mis Tareas");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        recyclerTareas.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TareaAdapter();
        recyclerTareas.setAdapter(adapter);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        // Configurar tabs
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
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

        cargarTareas();
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
                            Tarea t = child.getValue(Tarea.class);
                            if (t != null && t.getTrabajadoresIds() != null && t.getTrabajadoresIds().contains(trabajadorId)) {
                                t.setTareaId(child.getKey());
                                listaTareas.add(t);
                            }
                        }
                        filtrarTareas();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressDialog.dismiss();
                        Toast.makeText(VerTareas.this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void filtrarTareas() {
        listaFiltrada.clear();
        for (Tarea t : listaTareas) {
            switch (currentTab) {
                case 0: // Todas
                    listaFiltrada.add(t);
                    break;
                case 1: // Pendientes (no completadas)
                    if (!"Completada".equals(t.getEstado())) {
                        listaFiltrada.add(t);
                    }
                    break;
                case 2: // Completadas
                    if ("Completada".equals(t.getEstado())) {
                        listaFiltrada.add(t);
                    }
                    break;
            }
        }
        adapter.setTareas(listaFiltrada);
        tvNoTareas.setVisibility(listaFiltrada.isEmpty() ? View.VISIBLE : View.GONE);
    }

    // Adaptador
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
            String estado = t.getEstado() != null ? t.getEstado() : "Pendiente";
            String fechaLimite = sdfFecha.format(new Date(t.getFechaLimite()));
            holder.text1.setText(t.getTitulo());
            holder.text2.setText(estado + " | Vence: " + fechaLimite);
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
            TareaViewHolder(@NonNull View itemView) {
                super(itemView);
                text1 = itemView.findViewById(android.R.id.text1);
                text2 = itemView.findViewById(android.R.id.text2);
            }
        }
    }
}
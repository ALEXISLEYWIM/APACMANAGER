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

import com.example.apacmanager.MODELS.Trabajador;
import com.example.apacmanager.R;
import com.example.apacmanager.firebase.FirebaseDatabaseHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GestionTrabajadores extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextInputEditText etBuscarTrabajador;
    private RecyclerView recyclerTrabajadores;
    private TextView tvNoTrabajadores;
    private FloatingActionButton fabAgregarTrabajador;
    private ProgressDialog progressDialog;

    private String empresaId;
    private List<Trabajador> listaTrabajadores = new ArrayList<>();
    private TrabajadorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_trabajadores);

        // Recibir ID de la empresa desde el Intent
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
        fabAgregarTrabajador = findViewById(R.id.fabAgregarTrabajador);

        // Configurar toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Gestionar Trabajadores");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        recyclerTrabajadores.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TrabajadorAdapter();
        recyclerTrabajadores.setAdapter(adapter);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        // Cargar lista de trabajadores
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

        // FAB para agregar nuevo trabajador
        fabAgregarTrabajador.setOnClickListener(v -> {
            Intent intent = new Intent(GestionTrabajadores.this, AgregarTrabajador.class);
            intent.putExtra("empresa_id", empresaId);
            startActivity(intent);
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
                        Toast.makeText(GestionTrabajadores.this,
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

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar lista al volver (por si se agregó o editó un trabajador)
        cargarTrabajadores();
    }

    // ==================== ADAPTADOR ====================
    private class TrabajadorAdapter extends RecyclerView.Adapter<TrabajadorAdapter.TrabajadorViewHolder> {
        private List<Trabajador> trabajadores = new ArrayList<>();

        @NonNull
        @Override
        public TrabajadorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
            return new TrabajadorViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TrabajadorViewHolder holder, int position) {
            Trabajador t = trabajadores.get(position);
            String estado = t.isActivo() ? "Activo" : "Inactivo";
            holder.text1.setText(t.getNombre());
            holder.text2.setText(t.getCargo() + " - " + estado);
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(GestionTrabajadores.this, EditarTrabajador.class);
                intent.putExtra("empresa_id", empresaId);
                intent.putExtra("trabajador_id", t.getTrabajadorId());
                startActivity(intent);
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

        class TrabajadorViewHolder extends RecyclerView.ViewHolder {
            TextView text1, text2;
            public TrabajadorViewHolder(@NonNull View itemView) {
                super(itemView);
                text1 = itemView.findViewById(android.R.id.text1);
                text2 = itemView.findViewById(android.R.id.text2);
            }
        }
    }
}
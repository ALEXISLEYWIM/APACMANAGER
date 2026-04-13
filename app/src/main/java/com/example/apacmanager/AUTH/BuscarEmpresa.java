package com.example.apacmanager.AUTH;

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

import com.example.apacmanager.MODELS.Empresa;
import com.example.apacmanager.MODELS.Trabajador;
import com.example.apacmanager.MODELS.Usuario;
import com.example.apacmanager.R;
import com.example.apacmanager.TRABAJADOR.TrabajadorDashboard;
import com.example.apacmanager.firebase.FirebaseDatabaseHelper;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BuscarEmpresa extends AppCompatActivity {

    private TextInputEditText etBuscarEmpresa;
    private RecyclerView recyclerEmpresas;
    private TextView tvNoResultados;
    private ProgressDialog progressDialog;

    private String usuarioUid;
    private String usuarioNombre;
    private Usuario usuarioActual;
    private List<Empresa> listaEmpresas = new ArrayList<>();
    private EmpresaAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buscar_empresa);

        // Recibir datos del Intent
        if (getIntent().getExtras() != null) {
            usuarioUid = getIntent().getStringExtra("usuario_uid");
            usuarioNombre = getIntent().getStringExtra("usuario_nombre");
        }

        if (usuarioUid == null) {
            Toast.makeText(this, "Error: usuario no identificado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inicializar vistas
        etBuscarEmpresa = findViewById(R.id.etBuscarEmpresa);
        recyclerEmpresas = findViewById(R.id.recyclerEmpresas);
        tvNoResultados = findViewById(R.id.tvNoResultados);

        recyclerEmpresas.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EmpresaAdapter();
        recyclerEmpresas.setAdapter(adapter);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        // Cargar datos del usuario actual para verificar si ya tiene empresa
        cargarUsuarioActual();

        // Configurar búsqueda en tiempo real
        etBuscarEmpresa.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarEmpresas(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void cargarUsuarioActual() {
        progressDialog.setMessage("Cargando datos...");
        progressDialog.show();

        FirebaseDatabaseHelper.getInstance().getUsersReference()
                .child(usuarioUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        progressDialog.dismiss();
                        if (snapshot.exists()) {
                            usuarioActual = snapshot.getValue(Usuario.class);
                            if (usuarioActual != null) {
                                // Si ya tiene empresa, redirigir directamente al dashboard
                                if (usuarioActual.getEmpresaId() != null && !usuarioActual.getEmpresaId().isEmpty()) {
                                    Toast.makeText(BuscarEmpresa.this,
                                            "Ya perteneces a una empresa. Redirigiendo...",
                                            Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(BuscarEmpresa.this, TrabajadorDashboard.class)
                                            .putExtra("usuario_uid", usuarioUid)
                                            .putExtra("usuario_nombre", usuarioNombre));
                                    finish();
                                    return;
                                }
                                // Cargar lista de empresas
                                cargarEmpresas();
                            } else {
                                Toast.makeText(BuscarEmpresa.this,
                                        "Error al cargar usuario", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        } else {
                            Toast.makeText(BuscarEmpresa.this,
                                    "Usuario no encontrado", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressDialog.dismiss();
                        Toast.makeText(BuscarEmpresa.this,
                                "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void cargarEmpresas() {
        progressDialog.setMessage("Cargando empresas...");
        progressDialog.show();

        FirebaseDatabaseHelper.getInstance().getEmpresasReference()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        progressDialog.dismiss();
                        listaEmpresas.clear();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Empresa empresa = child.getValue(Empresa.class);
                            if (empresa != null) {
                                empresa.setEmpresaId(child.getKey()); // asegurar ID
                                listaEmpresas.add(empresa);
                            }
                        }
                        filtrarEmpresas(etBuscarEmpresa.getText().toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressDialog.dismiss();
                        Toast.makeText(BuscarEmpresa.this,
                                "Error al cargar empresas: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void filtrarEmpresas(String query) {
        List<Empresa> filtradas = new ArrayList<>();
        if (query.isEmpty()) {
            filtradas.addAll(listaEmpresas);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Empresa e : listaEmpresas) {
                if (e.getNombre() != null && e.getNombre().toLowerCase().contains(lowerQuery)) {
                    filtradas.add(e);
                }
            }
        }
        adapter.setEmpresas(filtradas);
        tvNoResultados.setVisibility(filtradas.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void unirseAEmpresa(Empresa empresa) {
        if (usuarioActual == null) return;

        progressDialog.setMessage("Uniéndose a la empresa...");
        progressDialog.show();

        String empresaId = empresa.getEmpresaId();

        // 1. Actualizar usuario: asignar empresaId
        FirebaseDatabaseHelper.getInstance().getUsersReference()
                .child(usuarioUid)
                .child("empresaId")
                .setValue(empresaId)
                .addOnSuccessListener(aVoid -> {
                    // 2. Crear registro de trabajador en la subcolección de la empresa
                    Trabajador trabajador = new Trabajador();
                    trabajador.setTrabajadorId(usuarioUid);
                    trabajador.setNombre(usuarioActual.getNombre());
                    trabajador.setTelefono(usuarioActual.getTelefono());
                    trabajador.setEmail(usuarioActual.getEmail());
                    trabajador.setDni(usuarioActual.getDni());
                    trabajador.setCargo("Empleado"); // valor por defecto
                    trabajador.setSueldo(0.0);
                    trabajador.setEmpresaId(empresaId);
                    trabajador.setActivo(true);
                    trabajador.setFechaIngreso(new Date());

                    FirebaseDatabaseHelper.getInstance().getTrabajadoresReference(empresaId)
                            .child(usuarioUid)
                            .setValue(trabajador)
                            .addOnSuccessListener(aVoid2 -> {
                                progressDialog.dismiss();
                                Toast.makeText(BuscarEmpresa.this,
                                        "Te has unido a " + empresa.getNombre(),
                                        Toast.LENGTH_SHORT).show();
                                // Redirigir al dashboard del trabajador
                                startActivity(new Intent(BuscarEmpresa.this, TrabajadorDashboard.class)
                                        .putExtra("usuario_uid", usuarioUid)
                                        .putExtra("usuario_nombre", usuarioActual.getNombre())
                                        .putExtra("empresa_id", empresaId));
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Toast.makeText(BuscarEmpresa.this,
                                        "Error al crear trabajador: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                                // Revertir empresaId del usuario
                                FirebaseDatabaseHelper.getInstance().getUsersReference()
                                        .child(usuarioUid)
                                        .child("empresaId")
                                        .setValue(null);
                            });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(BuscarEmpresa.this,
                            "Error al unirse: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    // Adaptador interno para RecyclerView
    private class EmpresaAdapter extends RecyclerView.Adapter<EmpresaAdapter.EmpresaViewHolder> {
        private List<Empresa> empresas = new ArrayList<>();

        @NonNull
        @Override
        public EmpresaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
            return new EmpresaViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull EmpresaViewHolder holder, int position) {
            Empresa empresa = empresas.get(position);
            holder.text1.setText(empresa.getNombre());
            holder.text2.setText(empresa.getTipo() + " - " + empresa.getRuc());
            holder.itemView.setOnClickListener(v -> unirseAEmpresa(empresa));
        }

        @Override
        public int getItemCount() {
            return empresas.size();
        }

        public void setEmpresas(List<Empresa> empresas) {
            this.empresas = empresas;
            notifyDataSetChanged();
        }

        class EmpresaViewHolder extends RecyclerView.ViewHolder {
            TextView text1, text2;
            public EmpresaViewHolder(@NonNull View itemView) {
                super(itemView);
                text1 = itemView.findViewById(android.R.id.text1);
                text2 = itemView.findViewById(android.R.id.text2);
            }
        }
    }
}
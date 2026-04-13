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

import com.example.apacmanager.MODELS.Cliente;
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

public class GestionClientes extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextInputEditText etBuscarCliente;
    private RecyclerView recyclerClientes;
    private TextView tvNoClientes;
    private FloatingActionButton fabAgregarCliente;
    private ProgressDialog progressDialog;

    private String empresaId;
    private List<Cliente> listaClientes = new ArrayList<>();
    private ClienteAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_clientes);

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
        etBuscarCliente = findViewById(R.id.etBuscarCliente);
        recyclerClientes = findViewById(R.id.recyclerClientes);
        tvNoClientes = findViewById(R.id.tvNoClientes);
        fabAgregarCliente = findViewById(R.id.fabAgregarCliente);

        // Configurar toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Gestionar Clientes");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        recyclerClientes.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ClienteAdapter();
        recyclerClientes.setAdapter(adapter);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        cargarClientes();

        // Filtro de búsqueda
        etBuscarCliente.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarClientes(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // FAB para agregar nuevo cliente
        fabAgregarCliente.setOnClickListener(v -> {
            // Aquí se abrirá la pantalla para agregar cliente
            Toast.makeText(GestionClientes.this, "Agregar cliente - Próximamente", Toast.LENGTH_SHORT).show();
            // Intent intent = new Intent(GestionClientes.this, AgregarCliente.class);
            // intent.putExtra("empresa_id", empresaId);
            // startActivity(intent);
        });
    }

    private void cargarClientes() {
        progressDialog.setMessage("Cargando clientes...");
        progressDialog.show();

        FirebaseDatabaseHelper.getInstance().getClientesReference(empresaId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        progressDialog.dismiss();
                        listaClientes.clear();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Cliente cliente = child.getValue(Cliente.class);
                            if (cliente != null) {
                                cliente.setClienteId(child.getKey());
                                listaClientes.add(cliente);
                            }
                        }
                        filtrarClientes(etBuscarCliente.getText().toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressDialog.dismiss();
                        Toast.makeText(GestionClientes.this,
                                "Error al cargar clientes: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void filtrarClientes(String query) {
        List<Cliente> filtrados = new ArrayList<>();
        if (query.isEmpty()) {
            filtrados.addAll(listaClientes);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Cliente c : listaClientes) {
                if (c.getNombre() != null && c.getNombre().toLowerCase().contains(lowerQuery)) {
                    filtrados.add(c);
                }
            }
        }
        adapter.setClientes(filtrados);
        tvNoClientes.setVisibility(filtrados.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarClientes(); // Recargar al volver (por si se agregó o editó)
    }

    // Adaptador para RecyclerView
    private class ClienteAdapter extends RecyclerView.Adapter<ClienteAdapter.ClienteViewHolder> {
        private List<Cliente> clientes = new ArrayList<>();

        @NonNull
        @Override
        public ClienteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
            return new ClienteViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ClienteViewHolder holder, int position) {
            Cliente c = clientes.get(position);
            holder.text1.setText(c.getNombre());
            holder.text2.setText(c.getEmail() + " - " + c.getTelefono());
            holder.itemView.setOnClickListener(v -> {
                // Aquí se abrirá la pantalla de edición/detalle del cliente
                Toast.makeText(GestionClientes.this,
                        "Editar cliente - Próximamente", Toast.LENGTH_SHORT).show();
                // Intent intent = new Intent(GestionClientes.this, EditarCliente.class);
                // intent.putExtra("empresa_id", empresaId);
                // intent.putExtra("cliente_id", c.getClienteId());
                // startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return clientes.size();
        }

        public void setClientes(List<Cliente> clientes) {
            this.clientes = clientes;
            notifyDataSetChanged();
        }

        class ClienteViewHolder extends RecyclerView.ViewHolder {
            TextView text1, text2;
            ClienteViewHolder(@NonNull View itemView) {
                super(itemView);
                text1 = itemView.findViewById(android.R.id.text1);
                text2 = itemView.findViewById(android.R.id.text2);
            }
        }
    }
}
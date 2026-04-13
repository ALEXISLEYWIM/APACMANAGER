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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apacmanager.MODELS.Producto;
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

public class Catalogo extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextInputEditText etBuscarProducto;
    private RecyclerView recyclerCatalogo;
    private TextView tvNoProductos;
    private FloatingActionButton fabAgregarProducto;
    private ProgressDialog progressDialog;

    private String empresaId;
    private String adminUid;  // Necesitas pasar este extra desde AdminDashboard
    private List<Producto> listaProductos = new ArrayList<>();
    private ProductoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalogo);

        if (getIntent().getExtras() != null) {
            empresaId = getIntent().getStringExtra("empresa_id");
            adminUid = getIntent().getStringExtra("usuario_uid");
        }

        if (empresaId == null) {
            Toast.makeText(this, "Error: ID de empresa no encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        toolbar = findViewById(R.id.toolbar);
        etBuscarProducto = findViewById(R.id.etBuscarProducto);
        recyclerCatalogo = findViewById(R.id.recyclerCatalogo);
        tvNoProductos = findViewById(R.id.tvNoProductos);
        fabAgregarProducto = findViewById(R.id.fabAgregarProducto);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Catálogo");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        recyclerCatalogo.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new ProductoAdapter();
        recyclerCatalogo.setAdapter(adapter);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        cargarProductos();

        etBuscarProducto.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarProductos(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        fabAgregarProducto.setOnClickListener(v -> {
            Intent intent = new Intent(Catalogo.this, AgregarProducto.class);
            intent.putExtra("empresa_id", empresaId);
            intent.putExtra("usuario_uid", adminUid);
            startActivity(intent);
        });
    }

    private void cargarProductos() {
        progressDialog.setMessage("Cargando productos...");
        progressDialog.show();

        FirebaseDatabaseHelper.getInstance().getProductosReference(empresaId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        progressDialog.dismiss();
                        listaProductos.clear();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Producto p = child.getValue(Producto.class);
                            if (p != null) {
                                p.setProductoId(child.getKey());
                                listaProductos.add(p);
                            }
                        }
                        filtrarProductos(etBuscarProducto.getText().toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressDialog.dismiss();
                        Toast.makeText(Catalogo.this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void filtrarProductos(String query) {
        List<Producto> filtrados = new ArrayList<>();
        if (query.isEmpty()) {
            filtrados.addAll(listaProductos);
        } else {
            String lower = query.toLowerCase();
            for (Producto p : listaProductos) {
                if (p.getNombre() != null && p.getNombre().toLowerCase().contains(lower)) {
                    filtrados.add(p);
                }
            }
        }
        adapter.setProductos(filtrados);
        tvNoProductos.setVisibility(filtrados.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarProductos();
    }

    private class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder> {
        private List<Producto> productos = new ArrayList<>();

        @NonNull
        @Override
        public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
            return new ProductoViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
            Producto p = productos.get(position);
            String disponible = p.isDisponible() ? "Disponible" : "Agotado";
            holder.text1.setText(p.getNombre());
            holder.text2.setText(String.format("S/ %.2f | Stock: %d | %s", p.getPrecio(), p.getStock(), disponible));
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(Catalogo.this, EditarProducto.class);
                intent.putExtra("empresa_id", empresaId);
                intent.putExtra("producto_id", p.getProductoId());
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return productos.size();
        }

        public void setProductos(List<Producto> productos) {
            this.productos = productos;
            notifyDataSetChanged();
        }

        class ProductoViewHolder extends RecyclerView.ViewHolder {
            TextView text1, text2;
            ProductoViewHolder(@NonNull View itemView) {
                super(itemView);
                text1 = itemView.findViewById(android.R.id.text1);
                text2 = itemView.findViewById(android.R.id.text2);
            }
        }
    }
}
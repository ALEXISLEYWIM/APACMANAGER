package com.example.apacmanager.CLIENTE;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
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

import com.example.apacmanager.MODELS.Pedido;
import com.example.apacmanager.R;
import com.example.apacmanager.firebase.FirebaseDatabaseHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Pedidos extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TabLayout tabLayoutPedidos;
    private RecyclerView recyclerPedidos;
    private TextView tvNoPedidos;
    private FloatingActionButton fabNuevoPedido;
    private ProgressDialog progressDialog;

    private String empresaId;
    private String usuarioUid;
    private List<Pedido> listaPedidos = new ArrayList<>();
    private List<Pedido> listaFiltrada = new ArrayList<>();
    private PedidoAdapter adapter;
    private int currentTab = 0; // 0=Todos, 1=Pendientes, 2=En Proceso, 3=Completados/Cancelados

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedidos);

        if (getIntent().getExtras() != null) {
            empresaId = getIntent().getStringExtra("empresa_id");
            usuarioUid = getIntent().getStringExtra("usuario_uid");
        }

        if (empresaId == null || usuarioUid == null) {
            Toast.makeText(this, "Error: datos incompletos", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        toolbar = findViewById(R.id.toolbar);
        tabLayoutPedidos = findViewById(R.id.tabLayoutPedidos);
        recyclerPedidos = findViewById(R.id.recyclerPedidos);
        tvNoPedidos = findViewById(R.id.tvNoPedidos);
        fabNuevoPedido = findViewById(R.id.fabNuevoPedido);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Mis Pedidos");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        recyclerPedidos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PedidoAdapter();
        recyclerPedidos.setAdapter(adapter);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        // Configurar tabs
        tabLayoutPedidos.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition();
                filtrarPedidos();
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        cargarPedidos();

        fabNuevoPedido.setOnClickListener(v -> {
            // Volver al catálogo para hacer nuevo pedido
            startActivity(new Intent(Pedidos.this, CatalogoCliente.class)
                    .putExtra("empresa_id", empresaId)
                    .putExtra("usuario_uid", usuarioUid));
        });
    }

    private void cargarPedidos() {
        progressDialog.setMessage("Cargando pedidos...");
        progressDialog.show();

        DatabaseReference ref = FirebaseDatabaseHelper.getInstance().getPedidosReference(empresaId);
        ref.orderByChild("fechaPedido").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressDialog.dismiss();
                listaPedidos.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Pedido p = child.getValue(Pedido.class);
                    if (p != null && p.getClienteId() != null && p.getClienteId().equals(usuarioUid)) {
                        p.setPedidoId(child.getKey());
                        listaPedidos.add(p);
                    }
                }
                filtrarPedidos();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                Toast.makeText(Pedidos.this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void filtrarPedidos() {
        listaFiltrada.clear();
        for (Pedido p : listaPedidos) {
            switch (currentTab) {
                case 0: // Todos
                    listaFiltrada.add(p);
                    break;
                case 1: // Pendientes
                    if ("Pendiente".equals(p.getEstado())) {
                        listaFiltrada.add(p);
                    }
                    break;
                case 2: // En Proceso
                    if ("En Proceso".equals(p.getEstado())) {
                        listaFiltrada.add(p);
                    }
                    break;
                case 3: // Completados + Cancelados
                    if ("Completado".equals(p.getEstado()) || "Cancelado".equals(p.getEstado())) {
                        listaFiltrada.add(p);
                    }
                    break;
            }
        }
        adapter.setPedidos(listaFiltrada);
        tvNoPedidos.setVisibility(listaFiltrada.isEmpty() ? View.VISIBLE : View.GONE);
    }

    // Método para cancelar pedido (con verificación de 7 días)
    private void cancelarPedido(Pedido pedido) {
        long ahora = System.currentTimeMillis();
        long fechaPedido = pedido.getFechaPedido().getTime();
        long diff = ahora - fechaPedido;
        long diasTranscurridos = diff / (24 * 60 * 60 * 1000L);

        if (diasTranscurridos >= 7) {
            Toast.makeText(this, "No se puede cancelar el pedido después de 7 días", Toast.LENGTH_LONG).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Cancelar pedido")
                .setMessage("¿Estás seguro de cancelar este pedido?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    progressDialog.setMessage("Cancelando...");
                    progressDialog.show();
                    DatabaseReference ref = FirebaseDatabaseHelper.getInstance().getPedidosReference(empresaId)
                            .child(pedido.getPedidoId());
                    ref.child("estado").setValue("Cancelado")
                            .addOnSuccessListener(aVoid -> {
                                progressDialog.dismiss();
                                Toast.makeText(Pedidos.this, "Pedido cancelado", Toast.LENGTH_SHORT).show();
                                // La lista se actualizará sola por el listener
                            })
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Toast.makeText(Pedidos.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .setNegativeButton("No", null)
                .show();
    }

    // Adaptador
    private class PedidoAdapter extends RecyclerView.Adapter<PedidoAdapter.PedidoViewHolder> {
        private List<Pedido> pedidos = new ArrayList<>();

        @NonNull
        @Override
        public PedidoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
            return new PedidoViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PedidoViewHolder holder, int position) {
            Pedido p = pedidos.get(position);
            String estado = p.getEstado() != null ? p.getEstado() : "Pendiente";
            String fecha = sdf.format(p.getFechaPedido());
            holder.text1.setText("Pedido #" + p.getPedidoId().substring(0, 8) + " - S/ " + p.getTotal());
            holder.text2.setText("Estado: " + estado + " | Fecha: " + fecha);

            // Permitir cancelar solo si está Pendiente y dentro de los 7 días
            if ("Pendiente".equals(estado)) {
                long ahora = System.currentTimeMillis();
                long fechaPedido = p.getFechaPedido().getTime();
                long dias = (ahora - fechaPedido) / (24 * 60 * 60 * 1000L);
                if (dias < 7) {
                    holder.itemView.setOnLongClickListener(v -> {
                        cancelarPedido(p);
                        return true;
                    });
                    holder.itemView.setBackgroundColor(0x20FF0000); // sutil indicador
                } else {
                    holder.itemView.setOnLongClickListener(null);
                }
            } else {
                holder.itemView.setOnLongClickListener(null);
            }
        }

        @Override
        public int getItemCount() {
            return pedidos.size();
        }

        public void setPedidos(List<Pedido> pedidos) {
            this.pedidos = pedidos;
            notifyDataSetChanged();
        }

        class PedidoViewHolder extends RecyclerView.ViewHolder {
            TextView text1, text2;
            PedidoViewHolder(@NonNull View itemView) {
                super(itemView);
                text1 = itemView.findViewById(android.R.id.text1);
                text2 = itemView.findViewById(android.R.id.text2);
            }
        }
    }
}
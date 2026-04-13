package com.example.apacmanager.CLIENTE;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.apacmanager.MODELS.Pedido;
import com.example.apacmanager.R;
import com.example.apacmanager.firebase.FirebaseDatabaseHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class DetalleProducto extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private ImageView ivProducto;
    private TextView tvNombreProducto, tvPrecio, tvStock, tvDescripcion, tvCantidadSeleccionada;
    private Button btnDisminuir, btnAumentar;
    private MaterialButton btnAgregarPedido;
    private ProgressDialog progressDialog;

    private String empresaId;
    private String usuarioUid;
    private String productoId;
    private String productoNombre;
    private double productoPrecio;
    private int productStock;
    private String productDescripcion;
    private boolean productDisponible;

    private int cantidad = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_producto);

        if (getIntent().getExtras() != null) {
            empresaId = getIntent().getStringExtra("empresa_id");
            usuarioUid = getIntent().getStringExtra("usuario_uid");
            productoId = getIntent().getStringExtra("producto_id");
            productoNombre = getIntent().getStringExtra("producto_nombre");
            productoPrecio = getIntent().getDoubleExtra("producto_precio", 0);
            productStock = getIntent().getIntExtra("producto_stock", 0);
            productDescripcion = getIntent().getStringExtra("producto_descripcion");
            productDisponible = getIntent().getBooleanExtra("producto_disponible", true);
        }

        if (empresaId == null || usuarioUid == null || productoId == null) {
            Toast.makeText(this, "Error: datos incompletos", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        toolbar = findViewById(R.id.toolbar);
        ivProducto = findViewById(R.id.ivProducto);
        tvNombreProducto = findViewById(R.id.tvNombreProducto);
        tvPrecio = findViewById(R.id.tvPrecio);
        tvStock = findViewById(R.id.tvStock);
        tvDescripcion = findViewById(R.id.tvDescripcion);
        tvCantidadSeleccionada = findViewById(R.id.tvCantidadSeleccionada);
        btnDisminuir = findViewById(R.id.btnDisminuir);
        btnAumentar = findViewById(R.id.btnAumentar);
        btnAgregarPedido = findViewById(R.id.btnAgregarPedido);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Detalle del Producto");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        tvNombreProducto.setText(productoNombre);
        tvPrecio.setText(String.format("S/ %.2f", productoPrecio));
        tvDescripcion.setText(productDescripcion != null ? productDescripcion : "Sin descripción");

        // Imagen placeholder (si no hay URL)
        Glide.with(this)
                .load(R.drawable.icontienda)
                .centerCrop()
                .into(ivProducto);

        if (!productDisponible || productStock <= 0) {
            tvStock.setText("Producto agotado");
            btnAgregarPedido.setEnabled(false);
            btnAumentar.setEnabled(false);
            btnDisminuir.setEnabled(false);
        } else {
            tvStock.setText("Stock disponible: " + productStock + " unidades");
            actualizarCantidad();
        }

        btnDisminuir.setOnClickListener(v -> {
            if (cantidad > 1) {
                cantidad--;
                actualizarCantidad();
            }
        });

        btnAumentar.setOnClickListener(v -> {
            if (cantidad < productStock) {
                cantidad++;
                actualizarCantidad();
            } else {
                Toast.makeText(this, "No puedes superar el stock disponible", Toast.LENGTH_SHORT).show();
            }
        });

        btnAgregarPedido.setOnClickListener(v -> realizarPedido());
    }

    private void actualizarCantidad() {
        tvCantidadSeleccionada.setText(String.valueOf(cantidad));
    }

    private void realizarPedido() {
        progressDialog.setMessage("Procesando pedido...");
        progressDialog.show();

        double total = productoPrecio * cantidad;

        String pedidoId = UUID.randomUUID().toString();
        Pedido pedido = new Pedido();
        pedido.setPedidoId(pedidoId);
        pedido.setClienteId(usuarioUid);
        pedido.setEmpresaId(empresaId);

        // Crear lista con el ID del producto (puede tener varios, aquí solo uno)
        List<String> productosIds = new ArrayList<>();
        productosIds.add(productoId);
        pedido.setProductosIds(productosIds);

        pedido.setTotal(total);
        pedido.setEstado("Pendiente");
        pedido.setFechaPedido(new Date());
        pedido.setFechaEntrega(null); // Por ahora nulo

        DatabaseReference ref = FirebaseDatabaseHelper.getInstance().getPedidosReference(empresaId);
        ref.child(pedidoId).setValue(pedido)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(DetalleProducto.this, "Pedido realizado con éxito", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(DetalleProducto.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
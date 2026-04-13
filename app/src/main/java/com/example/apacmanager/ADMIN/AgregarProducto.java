package com.example.apacmanager.ADMIN;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.apacmanager.MODELS.Producto;
import com.example.apacmanager.R;
import com.example.apacmanager.firebase.FirebaseDatabaseHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;

import java.util.UUID;

public class AgregarProducto extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private ImageView ivProducto;
    private FloatingActionButton fabSeleccionarImagen;
    private TextInputEditText etNombreProducto, etDescripcionProducto, etPrecio, etStock;
    private SwitchMaterial switchDisponible;
    private MaterialButton btnGuardarProducto, btnCancelar;
    private ProgressDialog progressDialog;

    private String empresaId;
    private String adminUid;
    private Uri imagenUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_producto);

        // Recibir datos del Intent
        if (getIntent().getExtras() != null) {
            empresaId = getIntent().getStringExtra("empresa_id");
            adminUid = getIntent().getStringExtra("usuario_uid");
        }

        if (empresaId == null || empresaId.isEmpty()) {
            Toast.makeText(this, "Error: ID de empresa no encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inicializar vistas
        toolbar = findViewById(R.id.toolbar);
        ivProducto = findViewById(R.id.ivProducto);
        fabSeleccionarImagen = findViewById(R.id.fabSeleccionarImagen);
        etNombreProducto = findViewById(R.id.etNombreProducto);
        etDescripcionProducto = findViewById(R.id.etDescripcionProducto);
        etPrecio = findViewById(R.id.etPrecio);
        etStock = findViewById(R.id.etStock);
        switchDisponible = findViewById(R.id.switchDisponible);
        btnGuardarProducto = findViewById(R.id.btnGuardarProducto);
        btnCancelar = findViewById(R.id.btnCancelar);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Agregar Producto");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        // Selección de imagen (opcional)
        fabSeleccionarImagen.setOnClickListener(v -> {
            Toast.makeText(this, "Selección de imagen (opcional) - Pendiente implementar", Toast.LENGTH_SHORT).show();
            // Aquí se puede implementar la selección de imagen con galería/cámara
        });

        btnGuardarProducto.setOnClickListener(v -> guardarProducto());
        btnCancelar.setOnClickListener(v -> finish());
    }

    private void guardarProducto() {
        String nombre = etNombreProducto.getText().toString().trim();
        String descripcion = etDescripcionProducto.getText().toString().trim();
        String precioStr = etPrecio.getText().toString().trim();
        String stockStr = etStock.getText().toString().trim();
        boolean disponible = switchDisponible.isChecked();

        if (nombre.isEmpty()) {
            etNombreProducto.setError("Ingrese nombre");
            etNombreProducto.requestFocus();
            return;
        }
        if (descripcion.isEmpty()) {
            etDescripcionProducto.setError("Ingrese descripción");
            etDescripcionProducto.requestFocus();
            return;
        }
        if (precioStr.isEmpty()) {
            etPrecio.setError("Ingrese precio");
            etPrecio.requestFocus();
            return;
        }
        if (stockStr.isEmpty()) {
            etStock.setError("Ingrese stock");
            etStock.requestFocus();
            return;
        }

        double precio;
        int stock;
        try {
            precio = Double.parseDouble(precioStr);
            stock = Integer.parseInt(stockStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Precio o stock inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        if (precio <= 0) {
            etPrecio.setError("Precio debe ser mayor a 0");
            etPrecio.requestFocus();
            return;
        }
        if (stock < 0) {
            etStock.setError("Stock no puede ser negativo");
            etStock.requestFocus();
            return;
        }

        progressDialog.setMessage("Guardando producto...");
        progressDialog.show();

        String productoId = UUID.randomUUID().toString();
        Producto producto = new Producto();
        producto.setProductoId(productoId);
        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);
        producto.setPrecio(precio);
        producto.setStock(stock);
        producto.setDisponible(disponible);
        producto.setEmpresaId(empresaId);
        producto.setCreadoPor(adminUid != null ? adminUid : "");
        producto.setFechaCreacion(System.currentTimeMillis());
        producto.setFechaActualizacion(System.currentTimeMillis());
        // ImagenUrl por defecto vacía, luego se puede actualizar
        producto.setImagenUrl("");

        DatabaseReference ref = FirebaseDatabaseHelper.getInstance().getProductosReference(empresaId);
        ref.child(productoId).setValue(producto)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(AgregarProducto.this, "Producto agregado exitosamente", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(AgregarProducto.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
package com.example.apacmanager.ADMIN;

import android.app.ProgressDialog;
import android.content.Intent;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class EditarProducto extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private ImageView ivProducto;
    private FloatingActionButton fabSeleccionarImagen;
    private TextInputEditText etNombreProducto, etDescripcionProducto, etPrecio, etStock;
    private SwitchMaterial switchDisponible;
    private MaterialButton btnActualizarProducto, btnEliminarProducto, btnCancelar;
    private ProgressDialog progressDialog;

    private String empresaId;
    private String productoId;
    private Producto productoActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_producto);

        if (getIntent().getExtras() != null) {
            empresaId = getIntent().getStringExtra("empresa_id");
            productoId = getIntent().getStringExtra("producto_id");
        }

        if (empresaId == null || productoId == null) {
            Toast.makeText(this, "Error: datos incompletos", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        toolbar = findViewById(R.id.toolbar);
        ivProducto = findViewById(R.id.ivProducto);
        fabSeleccionarImagen = findViewById(R.id.fabSeleccionarImagen);
        etNombreProducto = findViewById(R.id.etNombreProducto);
        etDescripcionProducto = findViewById(R.id.etDescripcionProducto);
        etPrecio = findViewById(R.id.etPrecio);
        etStock = findViewById(R.id.etStock);
        switchDisponible = findViewById(R.id.switchDisponible);
        btnActualizarProducto = findViewById(R.id.btnActualizarProducto);
        btnEliminarProducto = findViewById(R.id.btnEliminarProducto);
        btnCancelar = findViewById(R.id.btnCancelar);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Editar Producto");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        fabSeleccionarImagen.setOnClickListener(v -> {
            Toast.makeText(this, "Cambiar imagen (opcional)", Toast.LENGTH_SHORT).show();
        });

        cargarProducto();

        btnActualizarProducto.setOnClickListener(v -> actualizarProducto());
        btnEliminarProducto.setOnClickListener(v -> eliminarProducto());
        btnCancelar.setOnClickListener(v -> finish());
    }

    private void cargarProducto() {
        progressDialog.setMessage("Cargando producto...");
        progressDialog.show();

        FirebaseDatabaseHelper.getInstance().getProductosReference(empresaId)
                .child(productoId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        progressDialog.dismiss();
                        if (snapshot.exists()) {
                            productoActual = snapshot.getValue(Producto.class);
                            if (productoActual != null) {
                                productoActual.setProductoId(productoId);
                                mostrarDatos();
                            } else {
                                Toast.makeText(EditarProducto.this, "Error al leer producto", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        } else {
                            Toast.makeText(EditarProducto.this, "Producto no encontrado", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressDialog.dismiss();
                        Toast.makeText(EditarProducto.this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
    }

    private void mostrarDatos() {
        etNombreProducto.setText(productoActual.getNombre());
        etDescripcionProducto.setText(productoActual.getDescripcion());
        etPrecio.setText(String.valueOf(productoActual.getPrecio()));
        etStock.setText(String.valueOf(productoActual.getStock()));
        switchDisponible.setChecked(productoActual.isDisponible());
        // Si hay imagenUrl, se puede cargar con Glide (opcional)
    }

    private void actualizarProducto() {
        String nombre = etNombreProducto.getText().toString().trim();
        String descripcion = etDescripcionProducto.getText().toString().trim();
        String precioStr = etPrecio.getText().toString().trim();
        String stockStr = etStock.getText().toString().trim();
        boolean disponible = switchDisponible.isChecked();

        if (nombre.isEmpty()) {
            etNombreProducto.setError("Ingrese nombre");
            return;
        }
        if (descripcion.isEmpty()) {
            etDescripcionProducto.setError("Ingrese descripción");
            return;
        }
        if (precioStr.isEmpty()) {
            etPrecio.setError("Ingrese precio");
            return;
        }
        if (stockStr.isEmpty()) {
            etStock.setError("Ingrese stock");
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
            etPrecio.setError("Precio mayor a 0");
            return;
        }
        if (stock < 0) {
            etStock.setError("Stock no negativo");
            return;
        }

        progressDialog.setMessage("Actualizando producto...");
        progressDialog.show();

        Map<String, Object> updates = new HashMap<>();
        updates.put("nombre", nombre);
        updates.put("descripcion", descripcion);
        updates.put("precio", precio);
        updates.put("stock", stock);
        updates.put("disponible", disponible);
        updates.put("fechaActualizacion", System.currentTimeMillis());

        DatabaseReference ref = FirebaseDatabaseHelper.getInstance().getProductosReference(empresaId);
        ref.child(productoId).updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(EditarProducto.this, "Producto actualizado", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(EditarProducto.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void eliminarProducto() {
        progressDialog.setMessage("Eliminando producto...");
        progressDialog.show();

        FirebaseDatabaseHelper.getInstance().getProductosReference(empresaId)
                .child(productoId)
                .removeValue()
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(EditarProducto.this, "Producto eliminado", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(EditarProducto.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
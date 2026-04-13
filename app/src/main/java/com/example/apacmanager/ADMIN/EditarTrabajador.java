package com.example.apacmanager.ADMIN;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.apacmanager.MODELS.Trabajador;
import com.example.apacmanager.R;
import com.example.apacmanager.firebase.FirebaseDatabaseHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class EditarTrabajador extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextInputEditText etNombre, etTelefono, etEmail, etDni;
    private Spinner spinnerEstado;
    private MaterialButton btnGuardarCambios, btnEliminarTrabajador;
    private ProgressDialog progressDialog;

    private String empresaId;
    private String trabajadorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_trabajador);

        // Recibir datos del Intent
        if (getIntent().getExtras() != null) {
            empresaId = getIntent().getStringExtra("empresa_id");
            trabajadorId = getIntent().getStringExtra("trabajador_id");
        }

        if (empresaId == null || trabajadorId == null) {
            Toast.makeText(this, "Error: datos incompletos", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inicializar vistas
        toolbar = findViewById(R.id.toolbar);
        etNombre = findViewById(R.id.etNombre);
        etTelefono = findViewById(R.id.etTelefono);
        etEmail = findViewById(R.id.etEmail);
        etDni = findViewById(R.id.etDni);
        spinnerEstado = findViewById(R.id.spinnerEstado);
        btnGuardarCambios = findViewById(R.id.btnGuardarCambios);
        btnEliminarTrabajador = findViewById(R.id.btnEliminarTrabajador);

        // Configurar toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Editar Trabajador");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Configurar Spinner de estados
        String[] estados = {"Activo", "Inactivo"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, estados);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEstado.setAdapter(adapter);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        cargarDatosTrabajador();

        btnGuardarCambios.setOnClickListener(v -> guardarCambios());
        btnEliminarTrabajador.setOnClickListener(v -> eliminarTrabajador());
    }

    private void cargarDatosTrabajador() {
        progressDialog.setMessage("Cargando datos...");
        progressDialog.show();

        FirebaseDatabaseHelper.getInstance().getTrabajadoresReference(empresaId)
                .child(trabajadorId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        progressDialog.dismiss();
                        if (snapshot.exists()) {
                            Trabajador t = snapshot.getValue(Trabajador.class);
                            if (t != null) {
                                etNombre.setText(t.getNombre());
                                etTelefono.setText(t.getTelefono());
                                etEmail.setText(t.getEmail());
                                etDni.setText(t.getDni());
                                // Seleccionar estado en spinner
                                boolean activo = t.isActivo();
                                spinnerEstado.setSelection(activo ? 0 : 1);
                            } else {
                                Toast.makeText(EditarTrabajador.this,
                                        "Error al leer datos", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        } else {
                            Toast.makeText(EditarTrabajador.this,
                                    "Trabajador no encontrado", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressDialog.dismiss();
                        Toast.makeText(EditarTrabajador.this,
                                "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
    }

    private void guardarCambios() {
        String nombre = etNombre.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String dni = etDni.getText().toString().trim();
        boolean activo = spinnerEstado.getSelectedItemPosition() == 0; // 0=Activo, 1=Inactivo

        if (nombre.isEmpty()) {
            etNombre.setError("Ingrese nombre");
            etNombre.requestFocus();
            return;
        }
        if (telefono.isEmpty()) {
            etTelefono.setError("Ingrese teléfono");
            etTelefono.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            etEmail.setError("Ingrese email");
            etEmail.requestFocus();
            return;
        }
        if (dni.isEmpty()) {
            etDni.setError("Ingrese DNI");
            etDni.requestFocus();
            return;
        }

        progressDialog.setMessage("Guardando cambios...");
        progressDialog.show();

        Map<String, Object> updates = new HashMap<>();
        updates.put("nombre", nombre);
        updates.put("telefono", telefono);
        updates.put("email", email);
        updates.put("dni", dni);
        updates.put("activo", activo);

        FirebaseDatabaseHelper.getInstance().getTrabajadoresReference(empresaId)
                .child(trabajadorId)
                .updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    // También actualizar el usuario correspondiente en "users"
                    FirebaseDatabaseHelper.getInstance().getUsersReference()
                            .child(trabajadorId)
                            .updateChildren(updates)
                            .addOnSuccessListener(aVoid2 -> {
                                progressDialog.dismiss();
                                Toast.makeText(EditarTrabajador.this,
                                        "Cambios guardados", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Toast.makeText(EditarTrabajador.this,
                                        "Error al actualizar usuario: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(EditarTrabajador.this,
                            "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void eliminarTrabajador() {
        // No se elimina físicamente, solo se cambia a estado inactivo
        progressDialog.setMessage("Desactivando trabajador...");
        progressDialog.show();

        Map<String, Object> updates = new HashMap<>();
        updates.put("activo", false);

        FirebaseDatabaseHelper.getInstance().getTrabajadoresReference(empresaId)
                .child(trabajadorId)
                .updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    // También desactivar en users
                    FirebaseDatabaseHelper.getInstance().getUsersReference()
                            .child(trabajadorId)
                            .child("activo")
                            .setValue(false)
                            .addOnSuccessListener(aVoid2 -> {
                                progressDialog.dismiss();
                                Toast.makeText(EditarTrabajador.this,
                                        "Trabajador desactivado (inactivo)", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Toast.makeText(EditarTrabajador.this,
                                        "Error al desactivar usuario: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(EditarTrabajador.this,
                            "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
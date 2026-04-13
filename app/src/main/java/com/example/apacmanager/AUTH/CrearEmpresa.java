package com.example.apacmanager.AUTH;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.apacmanager.JEFE.JefeDashboard;
import com.example.apacmanager.MODELS.Empresa;
import com.example.apacmanager.R;
import com.example.apacmanager.firebase.FirebaseDatabaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CrearEmpresa extends AppCompatActivity {

    private TextInputEditText etNombreEmpresa, etRuc, etDireccion, etTelefonoEmpresa;
    private Spinner spinnerTipoEmpresa;
    private MaterialButton btnCrearEmpresa;
    private String usuarioUid;
    private String usuarioNombre;
    private ProgressDialog progressDialog;

    // Opciones para el spinner
    private String[] tiposEmpresa = {"Tienda", "Mayorista", "E-commerce"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_empresa);

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
        etNombreEmpresa = findViewById(R.id.etNombreEmpresa);
        etRuc = findViewById(R.id.etRuc);
        etDireccion = findViewById(R.id.etDireccion);
        etTelefonoEmpresa = findViewById(R.id.etTelefonoEmpresa);
        spinnerTipoEmpresa = findViewById(R.id.spinnerTipoEmpresa);
        btnCrearEmpresa = findViewById(R.id.btnCrearEmpresa);

        // Configurar Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tiposEmpresa);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoEmpresa.setAdapter(adapter);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        btnCrearEmpresa.setOnClickListener(v -> crearEmpresa());
    }

    private void crearEmpresa() {
        String nombre = etNombreEmpresa.getText().toString().trim();
        String ruc = etRuc.getText().toString().trim();
        String direccion = etDireccion.getText().toString().trim();
        String telefono = etTelefonoEmpresa.getText().toString().trim();
        String tipo = spinnerTipoEmpresa.getSelectedItem().toString();

        // Validaciones
        if (nombre.isEmpty()) {
            etNombreEmpresa.setError("Ingrese el nombre de la empresa");
            etNombreEmpresa.requestFocus();
            return;
        }
        if (ruc.isEmpty()) {
            etRuc.setError("Ingrese el RUC");
            etRuc.requestFocus();
            return;
        }
        if (ruc.length() < 11) {
            etRuc.setError("RUC debe tener 11 dígitos");
            etRuc.requestFocus();
            return;
        }
        if (direccion.isEmpty()) {
            etDireccion.setError("Ingrese la dirección");
            etDireccion.requestFocus();
            return;
        }
        if (telefono.isEmpty()) {
            etTelefonoEmpresa.setError("Ingrese el teléfono");
            etTelefonoEmpresa.requestFocus();
            return;
        }

        progressDialog.setMessage("Creando empresa...");
        progressDialog.show();

        // Generar ID único para la empresa
        DatabaseReference empresasRef = FirebaseDatabaseHelper.getInstance().getEmpresasReference();
        String empresaId = empresasRef.push().getKey();

        if (empresaId == null) {
            progressDialog.dismiss();
            Toast.makeText(this, "Error al generar ID de empresa", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear objeto Empresa
        Empresa empresa = new Empresa();
        empresa.setEmpresaId(empresaId);
        empresa.setNombre(nombre);
        empresa.setRuc(ruc);
        empresa.setDireccion(direccion);
        empresa.setTelefono(telefono);
        empresa.setTipo(tipo);
        empresa.setJefeId(usuarioUid);
        empresa.setFechaCreacion(new Date());

        // Guardar empresa en Realtime Database
        empresasRef.child(empresaId).setValue(empresa)
                .addOnSuccessListener(aVoid -> {
                    // Actualizar el usuario (Jefe) con el empresaId y rol
                    DatabaseReference userRef = FirebaseDatabaseHelper.getInstance().getUsersReference()
                            .child(usuarioUid);
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("empresaId", empresaId);
                    updates.put("rol", "Jefe");

                    userRef.updateChildren(updates)
                            .addOnSuccessListener(aVoid2 -> {
                                progressDialog.dismiss();
                                Toast.makeText(CrearEmpresa.this,
                                        "Empresa creada exitosamente",
                                        Toast.LENGTH_SHORT).show();
                                // Redirigir a JefeDashboard
                                Intent intent = new Intent(CrearEmpresa.this, JefeDashboard.class);
                                intent.putExtra("usuario_uid", usuarioUid);
                                intent.putExtra("usuario_nombre", usuarioNombre);
                                intent.putExtra("empresa_id", empresaId);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Toast.makeText(CrearEmpresa.this,
                                        "Error al actualizar usuario: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(CrearEmpresa.this,
                            "Error al crear empresa: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}
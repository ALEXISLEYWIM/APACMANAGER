package com.example.apacmanager.ADMIN;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.apacmanager.MODELS.Trabajador;
import com.example.apacmanager.MODELS.Usuario;
import com.example.apacmanager.R;
import com.example.apacmanager.firebase.FirebaseAuthHelper;
import com.example.apacmanager.firebase.FirebaseDatabaseHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;

public class AgregarTrabajador extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextInputEditText etNombre, etTelefono, etEmail, etDni;
    private MaterialButton btnAgregarTrabajador;
    private ProgressDialog progressDialog;

    private String empresaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_trabajador);

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
        etNombre = findViewById(R.id.etNombre);
        etTelefono = findViewById(R.id.etTelefono);
        etEmail = findViewById(R.id.etEmail);
        etDni = findViewById(R.id.etDni);
        btnAgregarTrabajador = findViewById(R.id.btnAgregarTrabajador);

        // Configurar toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Agregar Trabajador");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        btnAgregarTrabajador.setOnClickListener(v -> agregarTrabajador());
    }

    private void agregarTrabajador() {
        String nombre = etNombre.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String dni = etDni.getText().toString().trim();

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

        progressDialog.setMessage("Verificando usuario...");
        progressDialog.show();

        // Buscar si ya existe un usuario con ese email en la colección "users"
        FirebaseDatabaseHelper.getInstance().getUsersReference()
                .orderByChild("email")
                .equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // El usuario ya está registrado
                            for (DataSnapshot child : snapshot.getChildren()) {
                                Usuario usuario = child.getValue(Usuario.class);
                                if (usuario != null) {
                                    String uid = child.getKey();
                                    // Verificar si ya tiene empresa asignada
                                    if (usuario.getEmpresaId() != null && !usuario.getEmpresaId().isEmpty()) {
                                        progressDialog.dismiss();
                                        Toast.makeText(AgregarTrabajador.this,
                                                "Este usuario ya pertenece a otra empresa",
                                                Toast.LENGTH_LONG).show();
                                        return;
                                    }
                                    // Verificar si ya es trabajador de esta empresa (por si acaso)
                                    verificarSiYaEsTrabajador(uid, nombre, telefono, email, dni);
                                    return;
                                }
                            }
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(AgregarTrabajador.this,
                                    "No existe un usuario con ese email. El trabajador debe registrarse primero en la app.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressDialog.dismiss();
                        Toast.makeText(AgregarTrabajador.this,
                                "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void verificarSiYaEsTrabajador(String uid, String nombre, String telefono, String email, String dni) {
        FirebaseDatabaseHelper.getInstance().getTrabajadoresReference(empresaId)
                .child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            progressDialog.dismiss();
                            Toast.makeText(AgregarTrabajador.this,
                                    "Este usuario ya es trabajador de esta empresa",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            // Proceder a agregar
                            asignarComoTrabajador(uid, nombre, telefono, email, dni);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressDialog.dismiss();
                        Toast.makeText(AgregarTrabajador.this,
                                "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void asignarComoTrabajador(String uid, String nombre, String telefono, String email, String dni) {
        progressDialog.setMessage("Agregando trabajador...");

        // 1. Actualizar el usuario: asignar empresaId y rol si es necesario
        FirebaseDatabaseHelper.getInstance().getUsersReference()
                .child(uid)
                .child("empresaId")
                .setValue(empresaId)
                .addOnSuccessListener(aVoid -> {
                    // Asegurar que el rol sea "Trabajador" (si no lo es)
                    FirebaseDatabaseHelper.getInstance().getUsersReference()
                            .child(uid)
                            .child("rol")
                            .setValue("Trabajador")
                            .addOnSuccessListener(aVoid2 -> {
                                // 2. Crear registro en trabajadores
                                Trabajador trabajador = new Trabajador();
                                trabajador.setTrabajadorId(uid);
                                trabajador.setNombre(nombre);
                                trabajador.setTelefono(telefono);
                                trabajador.setEmail(email);
                                trabajador.setDni(dni);
                                trabajador.setCargo("Empleado"); // valor por defecto
                                trabajador.setSueldo(0.0);
                                trabajador.setEmpresaId(empresaId);
                                trabajador.setActivo(true);
                                trabajador.setFechaIngreso(new Date());

                                FirebaseDatabaseHelper.getInstance().getTrabajadoresReference(empresaId)
                                        .child(uid)
                                        .setValue(trabajador)
                                        .addOnSuccessListener(aVoid3 -> {
                                            progressDialog.dismiss();
                                            Toast.makeText(AgregarTrabajador.this,
                                                    "Trabajador agregado exitosamente",
                                                    Toast.LENGTH_SHORT).show();
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            progressDialog.dismiss();
                                            Toast.makeText(AgregarTrabajador.this,
                                                    "Error al guardar trabajador: " + e.getMessage(),
                                                    Toast.LENGTH_LONG).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Toast.makeText(AgregarTrabajador.this,
                                        "Error al actualizar rol: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(AgregarTrabajador.this,
                            "Error al asignar empresa: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}
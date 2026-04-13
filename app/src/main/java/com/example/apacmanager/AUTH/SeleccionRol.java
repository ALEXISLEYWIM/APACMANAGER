package com.example.apacmanager.AUTH;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.apacmanager.CLIENTE.BuscarEmpresaCliente;
import com.example.apacmanager.R;
import com.example.apacmanager.firebase.FirebaseDatabaseHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

public class SeleccionRol extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private CardView cardJefe, cardAdministrador, cardTrabajador, cardCliente;
    private String usuarioUid;
    private String usuarioNombre;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seleccion_rol);

        // Verificar extras
        if (getIntent().getExtras() == null) {
            Toast.makeText(this, "Error: datos de usuario no encontrados", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        usuarioUid = getIntent().getStringExtra("usuario_uid");
        usuarioNombre = getIntent().getStringExtra("usuario_nombre");

        if (usuarioUid == null) {
            Toast.makeText(this, "Error: usuario no identificado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        toolbar = findViewById(R.id.toolbar);
        cardJefe = findViewById(R.id.cardJefe);
        cardAdministrador = findViewById(R.id.cardAdministrador);
        cardTrabajador = findViewById(R.id.cardTrabajador);
        cardCliente = findViewById(R.id.cardCliente);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Selecciona tu rol");
        }
        // Flecha de retroceso: cierra esta actividad y vuelve a RegisterActivity
        toolbar.setNavigationOnClickListener(v -> {
            startActivity(new Intent(SeleccionRol.this, RegisterActivity.class));
            finish();
        });

        cardJefe.setOnClickListener(v -> seleccionarRol("Jefe"));
        cardAdministrador.setOnClickListener(v -> {
            Toast.makeText(SeleccionRol.this,
                    "El rol Administrador solo puede ser asignado por un Jefe. No está disponible para registro directo.",
                    Toast.LENGTH_LONG).show();
        });
        cardTrabajador.setOnClickListener(v -> seleccionarRol("Trabajador"));
        cardCliente.setOnClickListener(v -> seleccionarRol("Cliente"));
    }

    private void seleccionarRol(String rol) {
        progressDialog.setMessage("Guardando rol...");
        progressDialog.show();

        DatabaseReference userRef = FirebaseDatabaseHelper.getInstance().getUsersReference()
                .child(usuarioUid);

        Map<String, Object> updates = new HashMap<>();
        updates.put("rol", rol);

        userRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    switch (rol) {
                        case "Jefe":
                            startActivity(new Intent(SeleccionRol.this, CrearEmpresa.class)
                                    .putExtra("usuario_uid", usuarioUid)
                                    .putExtra("usuario_nombre", usuarioNombre));
                            finish();
                            break;
                        case "Trabajador":
                            startActivity(new Intent(SeleccionRol.this, BuscarEmpresa.class)
                                    .putExtra("usuario_uid", usuarioUid)
                                    .putExtra("usuario_nombre", usuarioNombre));
                            finish();
                            break;
                        case "Cliente":
                            startActivity(new Intent(SeleccionRol.this, BuscarEmpresaCliente.class)
                                    .putExtra("usuario_uid", usuarioUid)
                                    .putExtra("usuario_nombre", usuarioNombre));
                            finish();
                            break;
                    }
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(SeleccionRol.this,
                            "Error al guardar el rol: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}
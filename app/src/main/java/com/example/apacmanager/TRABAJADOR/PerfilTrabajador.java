package com.example.apacmanager.TRABAJADOR;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.apacmanager.AUTH.LoginActivity;
import com.example.apacmanager.MODELS.Trabajador;
import com.example.apacmanager.R;
import com.example.apacmanager.firebase.FirebaseAuthHelper;
import com.example.apacmanager.firebase.FirebaseDatabaseHelper;
import com.example.apacmanager.firebase.FirebaseStorageHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.text.NumberFormat;
import java.util.Locale;

public class PerfilTrabajador extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private ImageView ivFoto, btnCamara;
    private TextView tvNombreTrabajador, tvCargo, tvTelefono, tvEmail, tvDni, tvSueldo;
    private MaterialButton btnCerrarSesion;
    private ProgressDialog progressDialog;

    private String empresaId;
    private String trabajadorId;
    private Trabajador trabajadorActual;

    private static final int REQUEST_IMAGE_PICK = 1001;
    private Uri imagenSeleccionadaUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_trabajador);

        if (getIntent().getExtras() != null) {
            empresaId = getIntent().getStringExtra("empresa_id");
            trabajadorId = getIntent().getStringExtra("trabajador_id");
        }

        if (empresaId == null || trabajadorId == null) {
            Toast.makeText(this, "Error: datos incompletos", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        toolbar = findViewById(R.id.toolbar);
        ivFoto = findViewById(R.id.ivFoto);
        btnCamara = findViewById(R.id.btnCamara);
        tvNombreTrabajador = findViewById(R.id.tvNombreTrabajador);
        tvCargo = findViewById(R.id.tvCargo);
        tvTelefono = findViewById(R.id.tvTelefono);
        tvEmail = findViewById(R.id.tvEmail);
        tvDni = findViewById(R.id.tvDni);
        tvSueldo = findViewById(R.id.tvSueldo);
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Mi Perfil");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        cargarDatosTrabajador();

        btnCamara.setOnClickListener(v -> seleccionarImagen());
        btnCerrarSesion.setOnClickListener(v -> cerrarSesion());
    }

    private void cargarDatosTrabajador() {
        progressDialog.setMessage("Cargando perfil...");
        progressDialog.show();

        FirebaseDatabaseHelper.getInstance().getTrabajadoresReference(empresaId)
                .child(trabajadorId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        progressDialog.dismiss();
                        if (snapshot.exists()) {
                            trabajadorActual = snapshot.getValue(Trabajador.class);
                            if (trabajadorActual != null) {
                                mostrarDatos();
                                cargarFotoPerfil();
                            } else {
                                Toast.makeText(PerfilTrabajador.this, "Error al cargar datos", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(PerfilTrabajador.this, "Trabajador no encontrado", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressDialog.dismiss();
                        Toast.makeText(PerfilTrabajador.this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void mostrarDatos() {
        tvNombreTrabajador.setText(trabajadorActual.getNombre());
        tvCargo.setText(trabajadorActual.getCargo());
        tvTelefono.setText(trabajadorActual.getTelefono());
        tvEmail.setText(trabajadorActual.getEmail());
        tvDni.setText(trabajadorActual.getDni());

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));
        String sueldoFormateado = formatter.format(trabajadorActual.getSueldo());
        tvSueldo.setText(sueldoFormateado);
    }

    private void cargarFotoPerfil() {
        StorageReference fotoRef = FirebaseStorageHelper.getInstance().getProfileImageRef(trabajadorId);
        fotoRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(PerfilTrabajador.this)
                    .load(uri)
                    .circleCrop()
                    .placeholder(R.drawable.agregarusuario)
                    .error(R.drawable.agregarusuario)
                    .into(ivFoto);
        }).addOnFailureListener(e -> {
            // Sin foto, usar imagen por defecto
            ivFoto.setImageResource(R.drawable.agregarusuario);
        });
    }

    private void seleccionarImagen() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            imagenSeleccionadaUri = data.getData();
            if (imagenSeleccionadaUri != null) {
                subirImagenPerfil();
            }
        }
    }

    private void subirImagenPerfil() {
        progressDialog.setMessage("Actualizando foto...");
        progressDialog.show();

        StorageReference fotoRef = FirebaseStorageHelper.getInstance().getProfileImageRef(trabajadorId);
        fotoRef.putFile(imagenSeleccionadaUri)
                .addOnSuccessListener(taskSnapshot -> {
                    progressDialog.dismiss();
                    Toast.makeText(PerfilTrabajador.this, "Foto actualizada", Toast.LENGTH_SHORT).show();
                    cargarFotoPerfil(); // recargar
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(PerfilTrabajador.this, "Error al subir imagen: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void cerrarSesion() {
        FirebaseAuthHelper.getInstance().logout();
        Intent intent = new Intent(PerfilTrabajador.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
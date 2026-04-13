package com.example.apacmanager.AUTH;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.apacmanager.MODELS.Usuario;
import com.example.apacmanager.R;
import com.example.apacmanager.firebase.FirebaseAuthHelper;
import com.example.apacmanager.firebase.FirebaseDatabaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;

import java.util.Date;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etNombre, etTelefono, etEmail, etDni, etPassword;
    private MaterialButton btnRegister;
    private TextView tvYaTengoCuenta;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etNombre = findViewById(R.id.etNombre);
        etTelefono = findViewById(R.id.etTelefono);
        etEmail = findViewById(R.id.etEmail);
        etDni = findViewById(R.id.etDni);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvYaTengoCuenta = findViewById(R.id.tvYaTengoCuenta);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registrando usuario...");
        progressDialog.setCancelable(false);

        btnRegister.setOnClickListener(v -> registerUser());

        tvYaTengoCuenta.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void registerUser() {
        String nombre = etNombre.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String dni = etDni.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (nombre.isEmpty()) {
            etNombre.setError("Ingrese su nombre");
            etNombre.requestFocus();
            return;
        }
        if (telefono.isEmpty()) {
            etTelefono.setError("Ingrese su teléfono");
            etTelefono.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            etEmail.setError("Ingrese su correo");
            etEmail.requestFocus();
            return;
        }
        if (dni.isEmpty()) {
            etDni.setError("Ingrese su DNI");
            etDni.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            etPassword.setError("Ingrese una contraseña");
            etPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("La contraseña debe tener al menos 6 caracteres");
            etPassword.requestFocus();
            return;
        }

        progressDialog.show();

        FirebaseAuthHelper.getInstance().register(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser != null) {
                        String uid = firebaseUser.getUid();
                        Usuario usuario = new Usuario();
                        usuario.setUid(uid);
                        usuario.setNombre(nombre);
                        usuario.setTelefono(telefono);
                        usuario.setEmail(email);
                        usuario.setDni(dni);
                        usuario.setRol(null);
                        usuario.setEmpresaId(null);
                        usuario.setActivo(true);
                        usuario.setFechaRegistro(new Date());

                        FirebaseDatabaseHelper.getInstance().getUsersReference()
                                .child(uid)
                                .setValue(usuario)
                                .addOnSuccessListener(aVoid -> {
                                    progressDialog.dismiss();
                                    Toast.makeText(RegisterActivity.this,
                                            "Registro exitoso. Complete su perfil.",
                                            Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(RegisterActivity.this, SeleccionRol.class);
                                    intent.putExtra("usuario_uid", uid);
                                    intent.putExtra("usuario_nombre", nombre);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    progressDialog.dismiss();
                                    Toast.makeText(RegisterActivity.this,
                                            "Error al guardar datos: " + e.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                    firebaseUser.delete();
                                });
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "Error al crear usuario", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(RegisterActivity.this,
                            "Error de registro: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}
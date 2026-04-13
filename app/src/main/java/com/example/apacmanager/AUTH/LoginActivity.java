package com.example.apacmanager.AUTH;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.apacmanager.ADMIN.AdminDashboard;
import com.example.apacmanager.CLIENTE.BuscarEmpresaCliente;
import com.example.apacmanager.CLIENTE.ClienteDashboard;
import com.example.apacmanager.JEFE.JefeDashboard;
import com.example.apacmanager.MODELS.Usuario;
import com.example.apacmanager.R;
import com.example.apacmanager.TRABAJADOR.TrabajadorDashboard;
import com.example.apacmanager.firebase.FirebaseAuthHelper;
import com.example.apacmanager.firebase.FirebaseDatabaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private TextView tvRegister;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Iniciando sesión...");
        progressDialog.setCancelable(false);

        btnLogin.setOnClickListener(v -> loginUser());

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            etEmail.setError("Ingrese su correo");
            etEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            etPassword.setError("Ingrese su contraseña");
            etPassword.requestFocus();
            return;
        }

        progressDialog.show();

        FirebaseAuthHelper.getInstance().login(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser != null) {
                        String uid = firebaseUser.getUid();
                        // Leer desde Realtime Database
                        FirebaseDatabaseHelper.getInstance().getUsersReference()
                                .child(uid)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        progressDialog.dismiss();
                                        if (snapshot.exists()) {
                                            Usuario usuario = snapshot.getValue(Usuario.class);
                                            if (usuario != null) {
                                                if (!usuario.isActivo()) {
                                                    Toast.makeText(LoginActivity.this,
                                                            "Usuario inactivo. Contacte al administrador.",
                                                            Toast.LENGTH_LONG).show();
                                                    FirebaseAuthHelper.getInstance().logout();
                                                    return;
                                                }
                                                redirectBasedOnRole(usuario);
                                            } else {
                                                Toast.makeText(LoginActivity.this,
                                                        "Error al cargar datos", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(LoginActivity.this,
                                                    "Usuario no registrado en la base de datos",
                                                    Toast.LENGTH_SHORT).show();
                                            FirebaseAuthHelper.getInstance().logout();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        progressDialog.dismiss();
                                        Toast.makeText(LoginActivity.this,
                                                "Error en base de datos: " + error.getMessage(),
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, "Error: usuario nulo", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this,
                            "Error de autenticación: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void redirectBasedOnRole(Usuario usuario) {
        String rol = usuario.getRol();
        String empresaId = usuario.getEmpresaId();
        Intent intent;

        switch (rol) {
            case "Jefe":
                intent = new Intent(LoginActivity.this, JefeDashboard.class);
                break;
            case "Administrador":
                intent = new Intent(LoginActivity.this, AdminDashboard.class);
                break;
            case "Trabajador":
                intent = new Intent(LoginActivity.this, TrabajadorDashboard.class);
                break;
            case "Cliente":
                if (empresaId == null || empresaId.isEmpty()) {
                    intent = new Intent(LoginActivity.this, BuscarEmpresaCliente.class);
                } else {
                    intent = new Intent(LoginActivity.this, ClienteDashboard.class);
                }
                break;
            default:
                Toast.makeText(this, "Rol no reconocido", Toast.LENGTH_SHORT).show();
                return;
        }
        intent.putExtra("usuario_uid", usuario.getUid());
        intent.putExtra("usuario_nombre", usuario.getNombre());
        intent.putExtra("usuario_rol", usuario.getRol());
        intent.putExtra("empresa_id", usuario.getEmpresaId());
        startActivity(intent);
        finish();
    }
}
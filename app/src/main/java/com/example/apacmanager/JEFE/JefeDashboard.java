package com.example.apacmanager.JEFE;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.apacmanager.ADMIN.GestionTrabajadores;
import com.example.apacmanager.AUTH.CrearEmpresa;
import com.example.apacmanager.R;
import com.example.apacmanager.firebase.FirebaseDatabaseHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class JefeDashboard extends AppCompatActivity {

    private TextView tvBienvenida, tvGanancias;
    private CardView cardGestionEmpresa, cardVerTrabajadores, cardVerReportes, cardAsignarAdmin;

    private String usuarioUid;
    private String usuarioNombre;
    private String empresaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jefe_dashboard);

        // Recibir datos del Intent
        if (getIntent().getExtras() != null) {
            usuarioUid = getIntent().getStringExtra("usuario_uid");
            usuarioNombre = getIntent().getStringExtra("usuario_nombre");
            empresaId = getIntent().getStringExtra("empresa_id");
        }

        // TOAST DE DEPURACIÓN - muestra los valores recibidos
        Toast.makeText(this, "UID: " + usuarioUid + ", EmpresaId: " + empresaId, Toast.LENGTH_LONG).show();

        // Validar que llegaron los datos
        if (usuarioUid == null) {
            Toast.makeText(this, "Error: usuario no identificado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Si no tiene empresaId, el Jefe debe crearla (redirigir a CrearEmpresa)
        if (empresaId == null || empresaId.isEmpty()) {
            Toast.makeText(this, "No tienes empresa asignada. Por favor, crea una.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(JefeDashboard.this, CrearEmpresa.class);
            intent.putExtra("usuario_uid", usuarioUid);
            intent.putExtra("usuario_nombre", usuarioNombre);
            startActivity(intent);
            finish();
            return;
        }

        // Inicializar vistas
        tvBienvenida = findViewById(R.id.tvBienvenida);
        tvGanancias = findViewById(R.id.tvGanancias);
        cardGestionEmpresa = findViewById(R.id.cardGestionEmpresa);
        cardVerTrabajadores = findViewById(R.id.cardVerTrabajadores);
        cardVerReportes = findViewById(R.id.cardVerReportes);
        cardAsignarAdmin = findViewById(R.id.cardAsignarAdmin);

        tvBienvenida.setText("Bienvenido, " + (usuarioNombre != null ? usuarioNombre : "Jefe"));

        cargarGanancias();

        cardGestionEmpresa.setOnClickListener(v -> {
            startActivity(new Intent(JefeDashboard.this, GestionEmpresa.class)
                    .putExtra("empresa_id", empresaId));
        });

        cardVerTrabajadores.setOnClickListener(v -> {
            startActivity(new Intent(JefeDashboard.this, GestionTrabajadores.class)
                    .putExtra("empresa_id", empresaId)
                    .putExtra("rol", "Jefe"));
        });

        cardVerReportes.setOnClickListener(v -> {
            startActivity(new Intent(JefeDashboard.this, VerReportes.class)
                    .putExtra("empresa_id", empresaId));
        });

        cardAsignarAdmin.setOnClickListener(v -> {
            startActivity(new Intent(JefeDashboard.this, AsignarAdmin.class)
                    .putExtra("empresa_id", empresaId));
        });
    }

    private void cargarGanancias() {
        FirebaseDatabaseHelper.getInstance().getPedidosReference(empresaId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        double totalGanancias = 0;
                        for (DataSnapshot pedidoSnapshot : snapshot.getChildren()) {
                            String estado = pedidoSnapshot.child("estado").getValue(String.class);
                            Double total = pedidoSnapshot.child("total").getValue(Double.class);
                            if ((estado != null && (estado.equals("Pagado") || estado.equals("Completado"))) && total != null) {
                                totalGanancias += total;
                            }
                        }
                        tvGanancias.setText(String.format("S/ %.2f", totalGanancias));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(JefeDashboard.this,
                                "Error al cargar ganancias: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                        tvGanancias.setText("S/ 0.00");
                    }
                });
    }
}
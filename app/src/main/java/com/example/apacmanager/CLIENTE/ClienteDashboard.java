package com.example.apacmanager.CLIENTE;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.apacmanager.R;
import com.example.apacmanager.firebase.FirebaseDatabaseHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class ClienteDashboard extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextView tvBienvenida, tvMensajePromocion;
    private CardView cardBuscarEmpresa, cardCatalogoCliente, cardMisPedidos, cardMensajesCliente;
    private BottomNavigationView bottomNavigation;

    private String usuarioUid;
    private String usuarioNombre;
    private String empresaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cliente_dashboard);

        if (getIntent().getExtras() != null) {
            usuarioUid = getIntent().getStringExtra("usuario_uid");
            usuarioNombre = getIntent().getStringExtra("usuario_nombre");
            empresaId = getIntent().getStringExtra("empresa_id");
        }

        if (usuarioUid == null) {
            Toast.makeText(this, "Error: usuario no identificado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        toolbar = findViewById(R.id.toolbar);
        tvBienvenida = findViewById(R.id.tvBienvenida);
        tvMensajePromocion = findViewById(R.id.tvMensajePromocion);
        cardBuscarEmpresa = findViewById(R.id.cardBuscarEmpresa);
        cardCatalogoCliente = findViewById(R.id.cardCatalogoCliente);
        cardMisPedidos = findViewById(R.id.cardMisPedidos);
        cardMensajesCliente = findViewById(R.id.cardMensajesCliente);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Inicio");
        }

        String nombre = (usuarioNombre != null && !usuarioNombre.isEmpty()) ? usuarioNombre : "Cliente";
        tvBienvenida.setText("¡Hola, " + nombre + "!");

        // Mostrar la tarjeta "Buscar Empresa" siempre (sin ocultarla)
        cardBuscarEmpresa.setVisibility(View.VISIBLE);

        if (empresaId != null && !empresaId.isEmpty()) {
            tvMensajePromocion.setText("¡Bienvenido! Explora nuestro catálogo y haz tus pedidos.");
            // Habilitar opciones que requieren empresa
            cardCatalogoCliente.setEnabled(true);
            cardCatalogoCliente.setAlpha(1f);
            cardMisPedidos.setEnabled(true);
            cardMisPedidos.setAlpha(1f);
            cardMensajesCliente.setEnabled(true);
            cardMensajesCliente.setAlpha(1f);
        } else {
            tvMensajePromocion.setText("Aún no tienes una empresa asociada. Busca una para comenzar.");
            // Deshabilitar opciones que requieren empresa
            cardCatalogoCliente.setEnabled(false);
            cardCatalogoCliente.setAlpha(0.5f);
            cardMisPedidos.setEnabled(false);
            cardMisPedidos.setAlpha(0.5f);
            cardMensajesCliente.setEnabled(false);
            cardMensajesCliente.setAlpha(0.5f);
        }

        cardBuscarEmpresa.setOnClickListener(v -> {
            Intent intent = new Intent(ClienteDashboard.this, BuscarEmpresaCliente.class);
            intent.putExtra("usuario_uid", usuarioUid);
            intent.putExtra("usuario_nombre", usuarioNombre);
            startActivity(intent);
        });

        cardCatalogoCliente.setOnClickListener(v -> {
            if (empresaId == null || empresaId.isEmpty()) {
                Toast.makeText(this, "Primero debes asociarte a una empresa", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(ClienteDashboard.this, CatalogoCliente.class);
            intent.putExtra("empresa_id", empresaId);
            intent.putExtra("usuario_uid", usuarioUid);
            startActivity(intent);
        });

        cardMisPedidos.setOnClickListener(v -> {
            if (empresaId == null || empresaId.isEmpty()) {
                Toast.makeText(this, "Primero debes asociarte a una empresa", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(ClienteDashboard.this, Pedidos.class);
            intent.putExtra("empresa_id", empresaId);
            intent.putExtra("usuario_uid", usuarioUid);
            startActivity(intent);
        });

        cardMensajesCliente.setOnClickListener(v -> {
            Toast.makeText(ClienteDashboard.this, "Mensajes - Próximamente", Toast.LENGTH_SHORT).show();
        });

        if (bottomNavigation != null) {
            bottomNavigation.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_catalogo) {
                    if (empresaId != null && !empresaId.isEmpty()) {
                        startActivity(new Intent(ClienteDashboard.this, CatalogoCliente.class)
                                .putExtra("empresa_id", empresaId)
                                .putExtra("usuario_uid", usuarioUid));
                    } else {
                        Toast.makeText(ClienteDashboard.this, "Asóciate a una empresa primero", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                } else if (id == R.id.nav_pedidos) {
                    if (empresaId != null && !empresaId.isEmpty()) {
                        startActivity(new Intent(ClienteDashboard.this, Pedidos.class)
                                .putExtra("empresa_id", empresaId)
                                .putExtra("usuario_uid", usuarioUid));
                    } else {
                        Toast.makeText(ClienteDashboard.this, "Asóciate a una empresa primero", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                } else if (id == R.id.nav_perfil) {
                    startActivity(new Intent(ClienteDashboard.this, PerfilCliente.class)
                            .putExtra("usuario_uid", usuarioUid));
                    return true;
                }
                return false;
            });
        }
    }
}
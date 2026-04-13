package com.example.apacmanager.JEFE;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.apacmanager.R;
import com.example.apacmanager.firebase.FirebaseDatabaseHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class VerReportes extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextView tvGananciasTotales, tvPeriodo;
    private CardView cardVentasMes, cardAsistenciaReporte, cardTrabajadoresReporte, cardPedidosReporte;
    private ProgressDialog progressDialog;

    private String empresaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_reportes);

        // Recibir ID de la empresa desde el Intent
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
        tvGananciasTotales = findViewById(R.id.tvGananciasTotales);
        tvPeriodo = findViewById(R.id.tvPeriodo);
        cardVentasMes = findViewById(R.id.cardVentasMes);
        cardAsistenciaReporte = findViewById(R.id.cardAsistenciaReporte);
        cardTrabajadoresReporte = findViewById(R.id.cardTrabajadoresReporte);
        cardPedidosReporte = findViewById(R.id.cardPedidosReporte);

        // Configurar toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Reportes");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        // Cargar ganancias totales
        cargarGananciasTotales();

        // Configurar listeners de las cards
        cardVentasMes.setOnClickListener(v -> {
            // Aquí se abrirá el detalle de ventas del mes (pantalla futura)
            Toast.makeText(VerReportes.this, "Reporte de ventas del mes - Próximamente", Toast.LENGTH_SHORT).show();
            // Intent intent = new Intent(VerReportes.this, ReporteVentasMes.class);
            // intent.putExtra("empresa_id", empresaId);
            // startActivity(intent);
        });

        cardAsistenciaReporte.setOnClickListener(v -> {
            Toast.makeText(VerReportes.this, "Reporte de asistencia - Próximamente", Toast.LENGTH_SHORT).show();
        });

        cardTrabajadoresReporte.setOnClickListener(v -> {
            Toast.makeText(VerReportes.this, "Reporte de trabajadores - Próximamente", Toast.LENGTH_SHORT).show();
        });

        cardPedidosReporte.setOnClickListener(v -> {
            Toast.makeText(VerReportes.this, "Reporte de pedidos - Próximamente", Toast.LENGTH_SHORT).show();
        });
    }

    private void cargarGananciasTotales() {
        progressDialog.setMessage("Cargando ganancias...");
        progressDialog.show();

        // Obtener referencia a los pedidos de la empresa
        FirebaseDatabaseHelper.getInstance().getPedidosReference(empresaId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        progressDialog.dismiss();
                        double totalGanancias = 0;
                        int cantidadPedidos = 0;

                        for (DataSnapshot pedidoSnapshot : snapshot.getChildren()) {
                            String estado = pedidoSnapshot.child("estado").getValue(String.class);
                            Double total = pedidoSnapshot.child("total").getValue(Double.class);

                            // Sumar solo pedidos pagados o completados
                            if ((estado != null && (estado.equals("Pagado") || estado.equals("Completado"))) && total != null) {
                                totalGanancias += total;
                                cantidadPedidos++;
                            }
                        }

                        // Mostrar total formateado
                        tvGananciasTotales.setText(String.format("S/ %.2f", totalGanancias));
                        tvPeriodo.setText(cantidadPedidos + " pedido(s) pagado(s) en total");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressDialog.dismiss();
                        Toast.makeText(VerReportes.this,
                                "Error al cargar ganancias: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                        tvGananciasTotales.setText("S/ 0.00");
                        tvPeriodo.setText("No se pudieron cargar los datos");
                    }
                });
    }
}
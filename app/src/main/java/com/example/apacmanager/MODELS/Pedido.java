package com.example.apacmanager.MODELS;

import java.util.Date;
import java.util.List;

public class Pedido {
    private String pedidoId;
    private String clienteId;
    private String empresaId;
    private List<String> productosIds;
    private double total;
    private String estado;           // Pendiente, En Proceso, Completado, Cancelado
    private Date fechaPedido;
    private Date fechaEntrega;

    public Pedido() {}

    public Pedido(String pedidoId, String clienteId, String empresaId, List<String> productosIds,
                  double total, String estado, Date fechaPedido, Date fechaEntrega) {
        this.pedidoId = pedidoId;
        this.clienteId = clienteId;
        this.empresaId = empresaId;
        this.productosIds = productosIds;
        this.total = total;
        this.estado = estado;
        this.fechaPedido = fechaPedido;
        this.fechaEntrega = fechaEntrega;
    }

    // Getters y Setters
    public String getPedidoId() { return pedidoId; }
    public void setPedidoId(String pedidoId) { this.pedidoId = pedidoId; }

    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }

    public String getEmpresaId() { return empresaId; }
    public void setEmpresaId(String empresaId) { this.empresaId = empresaId; }

    public List<String> getProductosIds() { return productosIds; }
    public void setProductosIds(List<String> productosIds) { this.productosIds = productosIds; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Date getFechaPedido() { return fechaPedido; }
    public void setFechaPedido(Date fechaPedido) { this.fechaPedido = fechaPedido; }

    public Date getFechaEntrega() { return fechaEntrega; }
    public void setFechaEntrega(Date fechaEntrega) { this.fechaEntrega = fechaEntrega; }
}
package com.example.apacmanager.MODELS;

public class Producto {
    private String productoId;
    private String nombre;
    private double precio;
    private int stock;
    private String descripcion;
    private String imagenUrl;
    private boolean disponible;
    private String empresaId;
    private String creadoPor;
    private long fechaCreacion;        // milisegundos
    private long fechaActualizacion;   // milisegundos

    public Producto() {}

    public Producto(String nombre, double precio, int stock, String descripcion,
                    String imagenUrl, boolean disponible, String empresaId, String creadoPor) {
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.descripcion = descripcion;
        this.imagenUrl = imagenUrl;
        this.disponible = disponible;
        this.empresaId = empresaId;
        this.creadoPor = creadoPor;
        this.fechaCreacion = System.currentTimeMillis();
        this.fechaActualizacion = System.currentTimeMillis();
    }

    // Getters y Setters (con long)
    public String getProductoId() { return productoId; }
    public void setProductoId(String productoId) { this.productoId = productoId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }

    public String getEmpresaId() { return empresaId; }
    public void setEmpresaId(String empresaId) { this.empresaId = empresaId; }

    public String getCreadoPor() { return creadoPor; }
    public void setCreadoPor(String creadoPor) { this.creadoPor = creadoPor; }

    public long getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(long fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public long getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(long fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    public boolean sinStock() { return stock <= 0; }
    public String getPrecioFormateado() { return String.format("S/ %.2f", precio); }
}
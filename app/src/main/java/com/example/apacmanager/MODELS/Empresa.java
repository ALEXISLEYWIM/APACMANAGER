package com.example.apacmanager.MODELS;



import java.util.Date;

public class Empresa {
    private String empresaId;
    private String nombre;
    private String ruc;
    private String direccion;
    private String telefono;
    private String tipo;           // Tienda, Mayorista, E-commerce
    private String jefeId;
    private Date fechaCreacion;

    public Empresa() {}

    public Empresa(String empresaId, String nombre, String ruc, String direccion,
                   String telefono, String tipo, String jefeId, Date fechaCreacion) {
        this.empresaId = empresaId;
        this.nombre = nombre;
        this.ruc = ruc;
        this.direccion = direccion;
        this.telefono = telefono;
        this.tipo = tipo;
        this.jefeId = jefeId;
        this.fechaCreacion = fechaCreacion;
    }

    // Getters y Setters
    public String getEmpresaId() { return empresaId; }
    public void setEmpresaId(String empresaId) { this.empresaId = empresaId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getRuc() { return ruc; }
    public void setRuc(String ruc) { this.ruc = ruc; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getJefeId() { return jefeId; }
    public void setJefeId(String jefeId) { this.jefeId = jefeId; }

    public Date getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Date fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
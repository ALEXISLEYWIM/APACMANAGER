package com.example.apacmanager.MODELS;

import java.util.Date;

public class Cliente {
    private String clienteId;
    private String nombre;
    private String telefono;
    private String email;
    private String dni;
    private String empresaId;
    private Date fechaRegistro;

    public Cliente() {}

    public Cliente(String clienteId, String nombre, String telefono, String email, String dni,
                   String empresaId, Date fechaRegistro) {
        this.clienteId = clienteId;
        this.nombre = nombre;
        this.telefono = telefono;
        this.email = email;
        this.dni = dni;
        this.empresaId = empresaId;
        this.fechaRegistro = fechaRegistro;
    }

    // Getters y Setters
    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public String getEmpresaId() { return empresaId; }
    public void setEmpresaId(String empresaId) { this.empresaId = empresaId; }

    public Date getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(Date fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}
package com.example.apacmanager.MODELS;


import java.util.Date;

public class Usuario {
    private String uid;
    private String nombre;
    private String telefono;
    private String email;
    private String dni;
    private String rol;           // "Jefe", "Administrador", "Trabajador", "Cliente"
    private String empresaId;
    private boolean activo;
    private Date fechaRegistro;

    public Usuario() {} // Constructor vacío para Firestore

    public Usuario(String uid, String nombre, String telefono, String email, String dni,
                   String rol, String empresaId, boolean activo, Date fechaRegistro) {
        this.uid = uid;
        this.nombre = nombre;
        this.telefono = telefono;
        this.email = email;
        this.dni = dni;
        this.rol = rol;
        this.empresaId = empresaId;
        this.activo = activo;
        this.fechaRegistro = fechaRegistro;
    }

    // Getters y Setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public String getEmpresaId() { return empresaId; }
    public void setEmpresaId(String empresaId) { this.empresaId = empresaId; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public Date getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(Date fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}
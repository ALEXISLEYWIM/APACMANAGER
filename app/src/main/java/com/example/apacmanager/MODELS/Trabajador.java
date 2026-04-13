package com.example.apacmanager.MODELS;


import java.util.Date;

public class Trabajador {
    private String trabajadorId;
    private String nombre;
    private String telefono;
    private String email;
    private String dni;
    private String cargo;
    private double sueldo;
    private String empresaId;
    private boolean activo;
    private Date fechaIngreso;

    public Trabajador() {}

    public Trabajador(String trabajadorId, String nombre, String telefono, String email, String dni,
                      String cargo, double sueldo, String empresaId, boolean activo, Date fechaIngreso) {
        this.trabajadorId = trabajadorId;
        this.nombre = nombre;
        this.telefono = telefono;
        this.email = email;
        this.dni = dni;
        this.cargo = cargo;
        this.sueldo = sueldo;
        this.empresaId = empresaId;
        this.activo = activo;
        this.fechaIngreso = fechaIngreso;
    }

    // Getters y Setters
    public String getTrabajadorId() { return trabajadorId; }
    public void setTrabajadorId(String trabajadorId) { this.trabajadorId = trabajadorId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }

    public double getSueldo() { return sueldo; }
    public void setSueldo(double sueldo) { this.sueldo = sueldo; }

    public String getEmpresaId() { return empresaId; }
    public void setEmpresaId(String empresaId) { this.empresaId = empresaId; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public Date getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(Date fechaIngreso) { this.fechaIngreso = fechaIngreso; }
}
package com.example.apacmanager.MODELS;

import java.util.ArrayList;
import java.util.List;

public class Tarea {
    private String tareaId;
    private String titulo;
    private String descripcion;
    private String empresaId;
    private String estado;               // "Pendiente", "En progreso", "Completada"
    private long fechaAsignacion;        // milisegundos
    private long fechaLimite;             // milisegundos
    private List<String> trabajadoresIds; // múltiples trabajadores asignados
    private String creadoPor;             // UID del admin que creó la tarea
    private long fechaActualizacion;      // milisegundos

    public Tarea() {
        this.trabajadoresIds = new ArrayList<>();
    }

    public Tarea(String tareaId, String titulo, String descripcion, String empresaId,
                 String estado, long fechaAsignacion, long fechaLimite,
                 List<String> trabajadoresIds, String creadoPor, long fechaActualizacion) {
        this.tareaId = tareaId;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.empresaId = empresaId;
        this.estado = estado;
        this.fechaAsignacion = fechaAsignacion;
        this.fechaLimite = fechaLimite;
        this.trabajadoresIds = trabajadoresIds != null ? trabajadoresIds : new ArrayList<>();
        this.creadoPor = creadoPor;
        this.fechaActualizacion = fechaActualizacion;
    }

    // Getters y Setters
    public String getTareaId() { return tareaId; }
    public void setTareaId(String tareaId) { this.tareaId = tareaId; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getEmpresaId() { return empresaId; }
    public void setEmpresaId(String empresaId) { this.empresaId = empresaId; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public long getFechaAsignacion() { return fechaAsignacion; }
    public void setFechaAsignacion(long fechaAsignacion) { this.fechaAsignacion = fechaAsignacion; }

    public long getFechaLimite() { return fechaLimite; }
    public void setFechaLimite(long fechaLimite) { this.fechaLimite = fechaLimite; }

    public List<String> getTrabajadoresIds() { return trabajadoresIds; }
    public void setTrabajadoresIds(List<String> trabajadoresIds) { this.trabajadoresIds = trabajadoresIds; }

    public String getCreadoPor() { return creadoPor; }
    public void setCreadoPor(String creadoPor) { this.creadoPor = creadoPor; }

    public long getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(long fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    // Helpers
    public boolean estaAsignado(String uid) {
        return trabajadoresIds != null && trabajadoresIds.contains(uid);
    }

    public boolean isPendiente() { return "Pendiente".equals(estado); }
    public boolean isEnProgreso() { return "En progreso".equals(estado); }
    public boolean isCompletada() { return "Completada".equals(estado); }
}
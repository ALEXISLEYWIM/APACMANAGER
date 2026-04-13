package com.example.apacmanager.MODELS;

import java.util.Date;

public class Asistencia {
    private String asistenciaId;
    private String trabajadorId;
    private String empresaId;
    private Date fecha;
    private Date horaEntrada;
    private Date horaSalida;
    private String estado;        // "Presente", "Tarde", "Ausente"

    public Asistencia() {}

    public Asistencia(String asistenciaId, String trabajadorId, String empresaId, Date fecha,
                      Date horaEntrada, Date horaSalida, String estado) {
        this.asistenciaId = asistenciaId;
        this.trabajadorId = trabajadorId;
        this.empresaId = empresaId;
        this.fecha = fecha;
        this.horaEntrada = horaEntrada;
        this.horaSalida = horaSalida;
        this.estado = estado;
    }

    // Getters y Setters
    public String getAsistenciaId() { return asistenciaId; }
    public void setAsistenciaId(String asistenciaId) { this.asistenciaId = asistenciaId; }

    public String getTrabajadorId() { return trabajadorId; }
    public void setTrabajadorId(String trabajadorId) { this.trabajadorId = trabajadorId; }

    public String getEmpresaId() { return empresaId; }
    public void setEmpresaId(String empresaId) { this.empresaId = empresaId; }

    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }

    public Date getHoraEntrada() { return horaEntrada; }
    public void setHoraEntrada(Date horaEntrada) { this.horaEntrada = horaEntrada; }

    public Date getHoraSalida() { return horaSalida; }
    public void setHoraSalida(Date horaSalida) { this.horaSalida = horaSalida; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
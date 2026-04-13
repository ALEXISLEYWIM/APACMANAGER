package com.example.apacmanager.MODELS;

import java.util.Date;

public class Mensaje {
    private String mensajeId;
    private String remitenteId;
    private String receptorId;
    private String empresaId;
    private String contenido;
    private Date fechaEnvio;
    private boolean leido;

    public Mensaje() {}

    public Mensaje(String mensajeId, String remitenteId, String receptorId, String empresaId,
                   String contenido, Date fechaEnvio, boolean leido) {
        this.mensajeId = mensajeId;
        this.remitenteId = remitenteId;
        this.receptorId = receptorId;
        this.empresaId = empresaId;
        this.contenido = contenido;
        this.fechaEnvio = fechaEnvio;
        this.leido = leido;
    }

    // Getters y Setters
    public String getMensajeId() { return mensajeId; }
    public void setMensajeId(String mensajeId) { this.mensajeId = mensajeId; }

    public String getRemitenteId() { return remitenteId; }
    public void setRemitenteId(String remitenteId) { this.remitenteId = remitenteId; }

    public String getReceptorId() { return receptorId; }
    public void setReceptorId(String receptorId) { this.receptorId = receptorId; }

    public String getEmpresaId() { return empresaId; }
    public void setEmpresaId(String empresaId) { this.empresaId = empresaId; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public Date getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(Date fechaEnvio) { this.fechaEnvio = fechaEnvio; }

    public boolean isLeido() { return leido; }
    public void setLeido(boolean leido) { this.leido = leido; }
}
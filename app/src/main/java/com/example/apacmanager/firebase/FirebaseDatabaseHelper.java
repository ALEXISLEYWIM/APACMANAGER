package com.example.apacmanager.firebase;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseDatabaseHelper {

    private static FirebaseDatabaseHelper instance;
    private final FirebaseDatabase database;

    private FirebaseDatabaseHelper() {
        database = FirebaseDatabase.getInstance();
    }

    public static FirebaseDatabaseHelper getInstance() {
        if (instance == null) {
            instance = new FirebaseDatabaseHelper();
        }
        return instance;
    }

    public FirebaseDatabase getDatabase() {
        return database;
    }

    public DatabaseReference getUsersReference() {
        return database.getReference("users");
    }

    public DatabaseReference getEmpresasReference() {
        return database.getReference("empresas");
    }

    public DatabaseReference getTrabajadoresReference(String empresaId) {
        return database.getReference("empresas").child(empresaId).child("trabajadores");
    }

    public DatabaseReference getProductosReference(String empresaId) {
        return database.getReference("empresas").child(empresaId).child("productos");
    }

    public DatabaseReference getPedidosReference(String empresaId) {
        return database.getReference("empresas").child(empresaId).child("pedidos");
    }
    public DatabaseReference getClientesReference(String empresaId) {
        return database.getReference("empresas").child(empresaId).child("clientes");
    }

    public DatabaseReference getTareasReference(String empresaId) {
        return database.getReference("empresas").child(empresaId).child("tareas");
    }
    public DatabaseReference getAsistenciasReference(String empresaId) {
        return database.getReference("empresas").child(empresaId).child("asistencias");
    }
    public DatabaseReference getMensajesReference(String empresaId) {
        return database.getReference("empresas").child(empresaId).child("mensajes");
    }

}
package com.example.apacmanager.firebase;


import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirebaseStorageHelper {

    private static FirebaseStorageHelper instance;
    private final FirebaseStorage storage;

    private FirebaseStorageHelper() {
        storage = FirebaseStorage.getInstance();
    }

    public static FirebaseStorageHelper getInstance() {
        if (instance == null) {
            instance = new FirebaseStorageHelper();
        }
        return instance;
    }

    public FirebaseStorage getStorage() {
        return storage;
    }

    public StorageReference getStorageReference() {
        return storage.getReference();
    }

    public StorageReference getProfileImageRef(String userId) {
        return storage.getReference().child("perfiles/" + userId + ".jpg");
    }

    public StorageReference getProductoImageRef(String empresaId, String productoId) {
        return storage.getReference().child("empresas/" + empresaId + "/productos/" + productoId + ".jpg");
    }
}

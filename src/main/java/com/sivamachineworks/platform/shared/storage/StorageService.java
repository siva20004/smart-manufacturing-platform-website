package com.sivamachineworks.platform.shared.storage;

public interface StorageService {
    String storeFile(String key, byte[] content, String contentType);
    byte[] loadFile(String key);
    void deleteFile(String key);
    String getStorageLocation(String key);
}

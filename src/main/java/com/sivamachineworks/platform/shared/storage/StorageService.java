package com.sivamachineworks.platform.shared.storage;

import com.sivamachineworks.platform.shared.exception.BaseException;
import com.sivamachineworks.platform.shared.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
public class StorageService {

    public void storeFile(String key, byte[] content, String contentType) {
        validateStorageKey(key);
    }

    public byte[] loadFile(String key) {
        validateStorageKey(key);
        return new byte[0];
    }

    private void validateStorageKey(String key) {
        if (key == null || key.isEmpty()) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "Storage key cannot be empty");
        }

        if (key.contains("..") || key.startsWith("/") || key.startsWith("\\")) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "Path traversal detected in storage key");
        }
    }
}

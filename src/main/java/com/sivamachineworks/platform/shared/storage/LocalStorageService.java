package com.sivamachineworks.platform.shared.storage;

import com.sivamachineworks.platform.shared.exception.BaseException;
import com.sivamachineworks.platform.shared.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Set;

@Service
public class LocalStorageService implements com.sivamachineworks.platform.shared.service.StorageService {

    private static final long MAX_FILE_SIZE_BYTES = 524_288_000L; // 500 MB limit
    private static final Set<String> DANGEROUS_EXTENSIONS = Set.of(
            "exe", "bat", "cmd", "sh", "jsp", "jspx", "php", "jar", "war", "ps1", "dll", "so", "py", "js", "vbs"
    );

    private final Path baseDir;

    public LocalStorageService(@Value("${storage.local.base-dir:target/storage}") String baseDirPath) {
        this.baseDir = Paths.get(baseDirPath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.baseDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not create storage base directory", e);
        }
    }

    @Override
    public String storeFile(String key, byte[] content, String contentType) {
        validateStorageKey(key);

        if (content != null && content.length > MAX_FILE_SIZE_BYTES) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "File size exceeds maximum permitted limit of 500 MB");
        }

        try {
            Path targetPath = baseDir.resolve(key).normalize();
            if (!targetPath.startsWith(baseDir)) {
                throw new BaseException(ErrorCode.FORBIDDEN, "Security Exception: Path traversal attempt detected");
            }
            Files.createDirectories(targetPath.getParent());
            Files.write(targetPath, content != null ? content : new byte[0], StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return "local";
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    @Override
    public byte[] loadFile(String key) {
        validateStorageKey(key);
        try {
            Path targetPath = baseDir.resolve(key).normalize();
            if (!targetPath.startsWith(baseDir) || !Files.exists(targetPath)) {
                throw new BaseException(ErrorCode.NOT_FOUND, "File not found or invalid path");
            }
            return Files.readAllBytes(targetPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file", e);
        }
    }

    @Override
    public void deleteFile(String key) {
        validateStorageKey(key);
        try {
            Path targetPath = baseDir.resolve(key).normalize();
            if (targetPath.startsWith(baseDir)) {
                Files.deleteIfExists(targetPath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file", e);
        }
    }

    private void validateStorageKey(String key) {
        if (key == null || key.isBlank()) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "Storage key cannot be empty");
        }
        if (key.contains("..") || key.contains("\\") || key.startsWith("/")) {
            throw new BaseException(ErrorCode.FORBIDDEN, "Security Exception: Path traversal characters forbidden in storage key");
        }

        int dotIndex = key.lastIndexOf('.');
        if (dotIndex != -1) {
            String ext = key.substring(dotIndex + 1).toLowerCase();
            if (DANGEROUS_EXTENSIONS.contains(ext)) {
                throw new BaseException(ErrorCode.FORBIDDEN, "Security Exception: Upload of executable or script extension '." + ext + "' is strictly prohibited");
            }
        }
    }
}

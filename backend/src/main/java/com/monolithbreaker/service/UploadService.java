package com.monolithbreaker.service;

import com.monolithbreaker.dto.UploadResponse;
import com.monolithbreaker.storage.WorkspaceManager;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class UploadService {
    private static final long MAX_SIZE = 200L * 1024 * 1024;
    private final WorkspaceManager workspaceManager;

    public UploadService(WorkspaceManager workspaceManager) { this.workspaceManager = workspaceManager; }

    public UploadResponse handleUpload(UUID projectId, MultipartFile file) throws IOException {
        if (file.getSize() > MAX_SIZE) throw new IllegalArgumentException("File exceeds 200MB");
        Path zipPath = workspaceManager.uploadZipPath(projectId);
        Files.copy(file.getInputStream(), zipPath, StandardCopyOption.REPLACE_EXISTING);
        workspaceManager.deleteWorkspace(projectId);
        Path ws = workspaceManager.workspacePath(projectId);
        AtomicInteger total = new AtomicInteger();
        AtomicInteger javaCount = new AtomicInteger();
        try (InputStream is = Files.newInputStream(zipPath); ZipInputStream zis = new ZipInputStream(is)) {
            ZipEntry e;
            while ((e = zis.getNextEntry()) != null) {
                if (e.isDirectory()) continue;
                Path target = ws.resolve(e.getName()).normalize();
                if (!target.startsWith(ws)) throw new IllegalArgumentException("Unsafe zip path");
                Files.createDirectories(target.getParent());
                Files.copy(zis, target, StandardCopyOption.REPLACE_EXISTING);
                total.incrementAndGet();
                if (target.toString().endsWith(".java")) javaCount.incrementAndGet();
            }
        }
        if (javaCount.get() == 0) throw new IllegalArgumentException("No Java files found");
        return new UploadResponse(total.get(), javaCount.get());
    }
}

package com.monolithbreaker.storage;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Component
public class WorkspaceManager {
    private final Path uploadsRoot = Path.of("/data/uploads");
    private final Path workspacesRoot = Path.of("/data/workspaces");
    private final Path resultsRoot = Path.of("/data/results");

    public WorkspaceManager() throws IOException {
        Files.createDirectories(uploadsRoot);
        Files.createDirectories(workspacesRoot);
        Files.createDirectories(resultsRoot);
    }

    public Path uploadZipPath(UUID projectId) throws IOException {
        Path dir = uploadsRoot.resolve(projectId.toString());
        Files.createDirectories(dir);
        return dir.resolve("upload.zip");
    }

    public Path workspacePath(UUID projectId) throws IOException {
        Path dir = workspacesRoot.resolve(projectId.toString()).normalize();
        Files.createDirectories(dir);
        return dir;
    }

    public Path resultsPath(UUID runId) throws IOException {
        Path dir = resultsRoot.resolve(runId.toString());
        Files.createDirectories(dir);
        return dir;
    }

    public void deleteWorkspace(UUID projectId) throws IOException {
        Path root = workspacesRoot.resolve(projectId.toString());
        if (!Files.exists(root)) return;
        try (var s = Files.walk(root)) {
            s.sorted((a,b) -> b.compareTo(a)).forEach(p -> { try { Files.deleteIfExists(p); } catch (IOException ignored) {} });
        }
    }
}

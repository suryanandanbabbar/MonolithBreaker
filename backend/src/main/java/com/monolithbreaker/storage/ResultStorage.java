package com.monolithbreaker.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Component
public class ResultStorage {
    private final WorkspaceManager workspaceManager;
    private final ObjectMapper mapper = new ObjectMapper();

    public ResultStorage(WorkspaceManager workspaceManager) { this.workspaceManager = workspaceManager; }

    public void save(UUID runId, String filename, Object data) throws IOException {
        Path p = workspaceManager.resultsPath(runId).resolve(filename);
        mapper.writerWithDefaultPrettyPrinter().writeValue(p.toFile(), data);
    }

    public <T> T read(UUID runId, String filename, TypeReference<T> type) throws IOException {
        Path p = workspaceManager.resultsPath(runId).resolve(filename);
        if (!Files.exists(p)) throw new IllegalArgumentException("Result file not found: " + filename);
        return mapper.readValue(p.toFile(), type);
    }
}

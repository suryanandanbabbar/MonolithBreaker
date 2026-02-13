package com.monolithbreaker.model;

import java.util.UUID;

public class AnalysisRun {
    private final UUID runId;
    private final UUID projectId;
    private volatile RunStatus status;
    private volatile String message;

    public AnalysisRun(UUID runId, UUID projectId) {
        this.runId = runId;
        this.projectId = projectId;
        this.status = RunStatus.QUEUED;
        this.message = "Queued";
    }
    public UUID getRunId() { return runId; }
    public UUID getProjectId() { return projectId; }
    public RunStatus getStatus() { return status; }
    public void setStatus(RunStatus status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}

package com.monolithbreaker.dto;

import com.monolithbreaker.model.RunStatus;

import java.util.UUID;

public record AnalysisStatusResponse(UUID runId, RunStatus status, String message) {}

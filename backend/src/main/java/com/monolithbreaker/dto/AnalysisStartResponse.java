package com.monolithbreaker.dto;

import com.monolithbreaker.model.RunStatus;

import java.util.UUID;

public record AnalysisStartResponse(UUID runId, RunStatus status) {}

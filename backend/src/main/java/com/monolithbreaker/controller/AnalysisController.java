package com.monolithbreaker.controller;

import com.monolithbreaker.dto.*;
import com.monolithbreaker.service.AiService;
import com.monolithbreaker.service.AnalysisService;
import com.monolithbreaker.service.ProjectService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class AnalysisController {
    private final AnalysisService analysisService;
    private final ProjectService projectService;
    private final AiService aiService;

    public AnalysisController(AnalysisService analysisService, ProjectService projectService, AiService aiService) {
        this.analysisService = analysisService;
        this.projectService = projectService;
        this.aiService = aiService;
    }

    @PostMapping("/projects/{projectId}/analysis")
    public ApiResponse<AnalysisStartResponse> start(@PathVariable UUID projectId) {
        projectService.get(projectId);
        var run = analysisService.start(projectId);
        return ApiResponse.ok(new AnalysisStartResponse(run.getRunId(), run.getStatus()));
    }

    @GetMapping("/analysis/{runId}/status")
    public ApiResponse<AnalysisStatusResponse> status(@PathVariable UUID runId) {
        return ApiResponse.ok(analysisService.status(runId));
    }

    @GetMapping("/analysis/{runId}/nodes")
    public ApiResponse<Object> nodes(@PathVariable UUID runId) throws Exception { return ApiResponse.ok(analysisService.nodes(runId)); }
    @GetMapping("/analysis/{runId}/edges")
    public ApiResponse<Object> edges(@PathVariable UUID runId) throws Exception { return ApiResponse.ok(analysisService.edges(runId)); }
    @GetMapping("/analysis/{runId}/risk")
    public ApiResponse<Object> risk(@PathVariable UUID runId) throws Exception { return ApiResponse.ok(analysisService.risk(runId)); }
    @GetMapping("/analysis/{runId}/communities")
    public ApiResponse<Object> communities(@PathVariable UUID runId) throws Exception { return ApiResponse.ok(analysisService.communities(runId)); }

    @PostMapping("/analysis/{runId}/ai/split")
    public ApiResponse<AiSplitResponse> aiSplit(@PathVariable UUID runId) throws Exception {
        return ApiResponse.ok(aiService.split(analysisService.communities(runId), analysisService.risk(runId).stream().limit(30).toList()));
    }
}

package com.monolithbreaker.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.monolithbreaker.analysis.DependencyAnalyzer;
import com.monolithbreaker.dto.AnalysisStatusResponse;
import com.monolithbreaker.dto.CommunityDto;
import com.monolithbreaker.dto.RiskRecord;
import com.monolithbreaker.graph.CommunityService;
import com.monolithbreaker.metrics.RiskService;
import com.monolithbreaker.model.AnalysisRun;
import com.monolithbreaker.model.GraphEdge;
import com.monolithbreaker.model.GraphNode;
import com.monolithbreaker.model.RunStatus;
import com.monolithbreaker.storage.ResultStorage;
import com.monolithbreaker.storage.WorkspaceManager;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

@Service
public class AnalysisService {
    private final WorkspaceManager workspaceManager;
    private final DependencyAnalyzer analyzer;
    private final RiskService riskService;
    private final CommunityService communityService;
    private final ResultStorage resultStorage;
    private final Map<UUID, AnalysisRun> runs = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public AnalysisService(WorkspaceManager workspaceManager, DependencyAnalyzer analyzer, RiskService riskService, CommunityService communityService, ResultStorage resultStorage) {
        this.workspaceManager = workspaceManager;
        this.analyzer = analyzer;
        this.riskService = riskService;
        this.communityService = communityService;
        this.resultStorage = resultStorage;
    }

    public AnalysisRun start(UUID projectId) {
        UUID runId = UUID.randomUUID();
        AnalysisRun run = new AnalysisRun(runId, projectId);
        runs.put(runId, run);
        executor.submit(() -> execute(run));
        return run;
    }

    private void execute(AnalysisRun run) {
        run.setStatus(RunStatus.RUNNING);
        run.setMessage("Parsing project and building dependency graph");
        try {
            Path ws = workspaceManager.workspacePath(run.getProjectId());
            var graph = analyzer.analyze(ws);
            resultStorage.save(run.getRunId(), "nodes.json", graph.nodes());
            resultStorage.save(run.getRunId(), "edges.json", graph.edges());
            var risk = riskService.compute(graph.nodes(), graph.edges(), 6, 0.85);
            resultStorage.save(run.getRunId(), "risk.json", risk);
            var communities = communityService.communities(graph.nodes(), graph.edges());
            resultStorage.save(run.getRunId(), "communities.json", communities);
            run.setStatus(RunStatus.DONE);
            run.setMessage("Analysis complete");
        } catch (Exception e) {
            run.setStatus(RunStatus.FAILED);
            run.setMessage(e.getMessage());
        }
    }

    public AnalysisStatusResponse status(UUID runId) {
        AnalysisRun r = runs.get(runId);
        if (r == null) throw new IllegalArgumentException("Run not found");
        return new AnalysisStatusResponse(r.getRunId(), r.getStatus(), r.getMessage());
    }

    public List<GraphNode> nodes(UUID runId) throws Exception { return resultStorage.read(runId, "nodes.json", new TypeReference<>(){}); }
    public List<GraphEdge> edges(UUID runId) throws Exception { return resultStorage.read(runId, "edges.json", new TypeReference<>(){}); }
    public List<RiskRecord> risk(UUID runId) throws Exception { return resultStorage.read(runId, "risk.json", new TypeReference<>(){}); }
    public List<CommunityDto> communities(UUID runId) throws Exception { return resultStorage.read(runId, "communities.json", new TypeReference<>(){}); }
}

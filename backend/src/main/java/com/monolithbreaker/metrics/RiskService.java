package com.monolithbreaker.metrics;

import com.monolithbreaker.dto.RiskRecord;
import com.monolithbreaker.model.GraphEdge;
import com.monolithbreaker.model.GraphNode;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RiskService {
    public List<RiskRecord> compute(List<GraphNode> nodes, List<GraphEdge> edges, int depthLimit, double damping) {
        List<String> nodeIds = nodes.stream().map(GraphNode::id).toList();
        Map<String, List<String>> out = new HashMap<>(), in = new HashMap<>();
        Map<String, Double> indegreeWeighted = new HashMap<>();
        nodeIds.forEach(n -> { out.put(n, new ArrayList<>()); in.put(n, new ArrayList<>()); indegreeWeighted.put(n, 0.0); });
        for (GraphEdge e : edges) {
            out.computeIfAbsent(e.from(), k -> new ArrayList<>()).add(e.to());
            in.computeIfAbsent(e.to(), k -> new ArrayList<>()).add(e.from());
            indegreeWeighted.merge(e.to(), e.weight(), Double::sum);
        }

        Map<String, Double> pr = pagerank(nodeIds, out, damping, 25);
        Map<String, Double> blast = new HashMap<>();
        for (String n : nodeIds) blast.put(n, bfs(out, n, depthLimit) + bfs(in, n, depthLimit));

        Map<String, Double> prN = normalize(pr), inN = normalize(indegreeWeighted), blN = normalize(blast);
        return nodeIds.stream().map(n -> {
            double risk = 0.45 * prN.getOrDefault(n, 0.0) + 0.35 * inN.getOrDefault(n, 0.0) + 0.20 * blN.getOrDefault(n, 0.0);
            double score = risk * 100;
            String label = score <= 30 ? "LOW" : score <= 70 ? "MEDIUM" : "HIGH";
            return new RiskRecord(n, prN.getOrDefault(n,0.0), inN.getOrDefault(n,0.0), blN.getOrDefault(n,0.0), Math.round(score*100.0)/100.0, label);
        }).sorted(Comparator.comparingDouble(RiskRecord::riskScore).reversed()).collect(Collectors.toList());
    }

    private Map<String, Double> pagerank(List<String> nodes, Map<String, List<String>> out, double d, int iterations) {
        int n = Math.max(nodes.size(), 1);
        Map<String, Double> rank = new HashMap<>();
        nodes.forEach(v -> rank.put(v, 1.0 / n));
        for (int i = 0; i < iterations; i++) {
            Map<String, Double> next = new HashMap<>();
            nodes.forEach(v -> next.put(v, (1 - d) / n));
            for (String u : nodes) {
                List<String> outs = out.getOrDefault(u, List.of());
                if (outs.isEmpty()) continue;
                double share = d * rank.getOrDefault(u,0.0) / outs.size();
                for (String v : outs) next.merge(v, share, Double::sum);
            }
            rank = next;
        }
        return rank;
    }

    private int bfs(Map<String, List<String>> g, String src, int depthLimit) {
        Set<String> seen = new HashSet<>();
        Deque<String> q = new ArrayDeque<>();
        Deque<Integer> d = new ArrayDeque<>();
        q.add(src); d.add(0); seen.add(src);
        while (!q.isEmpty()) {
            String u = q.poll(); int depth = d.poll();
            if (depth >= depthLimit) continue;
            for (String v : g.getOrDefault(u, List.of())) if (seen.add(v)) { q.add(v); d.add(depth + 1); }
        }
        return Math.max(0, seen.size() - 1);
    }

    private Map<String, Double> normalize(Map<String, Double> source) {
        double min = source.values().stream().min(Double::compare).orElse(0.0);
        double max = source.values().stream().max(Double::compare).orElse(1.0);
        Map<String, Double> r = new HashMap<>();
        source.forEach((k,v) -> r.put(k, max == min ? 0.0 : (v - min) / (max - min)));
        return r;
    }
}

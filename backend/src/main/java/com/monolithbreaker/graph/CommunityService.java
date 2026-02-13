package com.monolithbreaker.graph;

import com.monolithbreaker.dto.CommunityDto;
import com.monolithbreaker.model.GraphEdge;
import com.monolithbreaker.model.GraphNode;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CommunityService {
    public List<CommunityDto> communities(List<GraphNode> nodes, List<GraphEdge> edges) {
        Map<String, List<GraphNode>> byPkg = nodes.stream().filter(n -> "CLASS".equals(n.type())).collect(Collectors.groupingBy(GraphNode::packageName));
        List<String> pkgs = new ArrayList<>(byPkg.keySet());
        Collections.sort(pkgs);
        List<CommunityDto> result = new ArrayList<>();
        int idx = 1;
        for (int i = 0; i < pkgs.size(); i += 2) {
            List<String> group = pkgs.subList(i, Math.min(i + 2, pkgs.size()));
            Set<String> classes = group.stream().flatMap(p -> byPkg.getOrDefault(p, List.of()).stream().map(GraphNode::id)).collect(Collectors.toCollection(LinkedHashSet::new));
            List<String> coupling = edges.stream().filter(e -> classes.contains(e.from()) && !classes.contains(e.to())).map(e -> e.from()+"->"+e.to()).distinct().limit(50).toList();
            result.add(new CommunityDto("community-" + idx++, group, new ArrayList<>(classes), coupling));
        }
        return result;
    }
}

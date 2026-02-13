package com.monolithbreaker.service;

import com.monolithbreaker.dto.AiSplitResponse;
import com.monolithbreaker.dto.CommunityDto;
import com.monolithbreaker.dto.RiskRecord;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AiService {
    public AiSplitResponse split(List<CommunityDto> communities, List<RiskRecord> risk) {
        List<Map<String, Object>> microservices = new ArrayList<>();
        for (CommunityDto c : communities) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", c.communityId());
            m.put("responsibility", "Derived from package cohesion");
            m.put("includesPackages", c.packages());
            m.put("apiEndpoints", List.of("/api/" + c.communityId()));
            m.put("dataOwned", List.of("tables_for_" + c.communityId()));
            microservices.add(m);
        }
        List<Map<String, Object>> wrappers = new ArrayList<>();
        if (communities.size() > 1) {
            Map<String, Object> w = new LinkedHashMap<>();
            w.put("from", communities.get(0).communityId());
            w.put("to", communities.get(1).communityId());
            w.put("pattern", "ACL");
            w.put("why", "Reduce coupling across extracted boundaries");
            w.put("suggestedInterface", "interface Gateway { Object call(Object input); }");
            wrappers.add(w);
        }
        String mode = System.getenv("OPENAI_API_KEY") == null ? "DETERMINISTIC_ONLY" : "AI_DISABLED_IN_MVP_FALLBACK";
        return new AiSplitResponse(microservices, wrappers, mode);
    }
}

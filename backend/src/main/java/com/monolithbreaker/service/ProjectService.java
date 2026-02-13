package com.monolithbreaker.service;

import com.monolithbreaker.model.ProjectRecord;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ProjectService {
    private final Map<UUID, ProjectRecord> projects = new ConcurrentHashMap<>();

    public ProjectRecord create(String name) {
        ProjectRecord p = new ProjectRecord(UUID.randomUUID(), name);
        projects.put(p.projectId(), p);
        return p;
    }

    public ProjectRecord get(UUID id) {
        ProjectRecord p = projects.get(id);
        if (p == null) throw new IllegalArgumentException("Project not found");
        return p;
    }
}

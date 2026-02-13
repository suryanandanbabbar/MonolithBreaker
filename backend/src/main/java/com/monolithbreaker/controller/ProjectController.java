package com.monolithbreaker.controller;

import com.monolithbreaker.dto.*;
import com.monolithbreaker.model.ProjectRecord;
import com.monolithbreaker.service.ProjectService;
import com.monolithbreaker.service.UploadService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectService projectService;
    private final UploadService uploadService;

    public ProjectController(ProjectService projectService, UploadService uploadService) {
        this.projectService = projectService;
        this.uploadService = uploadService;
    }

    @PostMapping
    public ApiResponse<ProjectResponse> create(@Valid @RequestBody CreateProjectRequest request) {
        ProjectRecord p = projectService.create(request.name());
        return ApiResponse.ok(new ProjectResponse(p.projectId(), p.name()));
    }

    @PostMapping("/{projectId}/upload")
    public ApiResponse<UploadResponse> upload(@PathVariable UUID projectId, @RequestParam("file") MultipartFile file) throws IOException {
        projectService.get(projectId);
        return ApiResponse.ok(uploadService.handleUpload(projectId, file));
    }
}

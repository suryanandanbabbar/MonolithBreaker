package com.monolithbreaker.dto;

import java.util.List;
import java.util.Map;

public record AiSplitResponse(List<Map<String, Object>> microservices, List<Map<String, Object>> wrappers, String mode) {}

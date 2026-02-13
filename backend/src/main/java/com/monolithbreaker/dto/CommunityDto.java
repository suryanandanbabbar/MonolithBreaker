package com.monolithbreaker.dto;

import java.util.List;

public record CommunityDto(String communityId, List<String> packages, List<String> classes, List<String> couplingEdges) {}

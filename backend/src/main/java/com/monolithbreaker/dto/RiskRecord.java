package com.monolithbreaker.dto;

public record RiskRecord(String node, double pagerank, double weightedIndegree, double blastRadius, double riskScore, String label) {}

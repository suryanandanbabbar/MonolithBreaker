package com.monolithbreaker.model;

public record GraphEdge(String from, String to, EdgeType type, double weight) {}

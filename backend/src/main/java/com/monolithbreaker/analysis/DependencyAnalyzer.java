package com.monolithbreaker.analysis;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithImplements;
import com.github.javaparser.ast.nodeTypes.NodeWithExtends;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.monolithbreaker.model.EdgeType;
import com.monolithbreaker.model.GraphEdge;
import com.monolithbreaker.model.GraphNode;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Component
public class DependencyAnalyzer {
    public record GraphResult(List<GraphNode> nodes, List<GraphEdge> edges) {}

    public GraphResult analyze(Path root) throws IOException {
        CombinedTypeSolver solver = new CombinedTypeSolver();
        solver.add(new JavaParserTypeSolver(root));
        StaticJavaParser.setConfiguration(new ParserConfiguration().setSymbolResolver(new JavaSymbolSolver(solver)));

        List<Path> javaFiles;
        try (var s = Files.walk(root)) { javaFiles = s.filter(p -> p.toString().endsWith(".java")).toList(); }
        Map<String, GraphNode> nodes = new HashMap<>();
        List<GraphEdge> edges = new ArrayList<>();

        for (Path file : javaFiles) {
            CompilationUnit cu;
            try { cu = StaticJavaParser.parse(file); } catch (Exception ex) { continue; }
            String pkg = cu.getPackageDeclaration().map(p -> p.getNameAsString()).orElse("default");
            for (ClassOrInterfaceDeclaration c : cu.findAll(ClassOrInterfaceDeclaration.class)) {
                String fqn = pkg + "." + c.getNameAsString();
                nodes.putIfAbsent(fqn, new GraphNode(fqn, "CLASS", pkg));
                nodes.putIfAbsent(pkg, new GraphNode(pkg, "PACKAGE", pkg));
                if (c instanceof NodeWithExtends<?> nwe) {
                    for (ClassOrInterfaceType t : nwe.getExtendedTypes()) edges.add(edge(fqn, qualify(pkg, t.getNameAsString(), cu), EdgeType.EXTENDS));
                }
                if (c instanceof NodeWithImplements<?> nwi) {
                    for (ClassOrInterfaceType t : nwi.getImplementedTypes()) edges.add(edge(fqn, qualify(pkg, t.getNameAsString(), cu), EdgeType.IMPLEMENTS));
                }
                c.getFields().forEach(f -> f.getVariables().forEach(v -> edges.add(edge(fqn, qualify(pkg, v.getTypeAsString(), cu), EdgeType.FIELD_ACCESS))));
                c.getMethods().forEach(m -> {
                    edges.add(edge(fqn, qualify(pkg, m.getTypeAsString(), cu), EdgeType.FIELD_ACCESS));
                    m.getParameters().forEach(p -> edges.add(edge(fqn, qualify(pkg, p.getTypeAsString(), cu), EdgeType.FIELD_ACCESS)));
                });
                c.findAll(MethodCallExpr.class).forEach(mc -> edges.add(edge(fqn, bestEffort(pkg, mc.getNameAsString(), cu), EdgeType.METHOD_CALL)));
                c.findAll(ObjectCreationExpr.class).forEach(oc -> edges.add(edge(fqn, qualify(pkg, oc.getTypeAsString(), cu), EdgeType.CONSTRUCTOR_CALL)));
                c.findAll(AnnotationExpr.class).forEach(an -> edges.add(edge(fqn, qualify(pkg, an.getNameAsString(), cu), EdgeType.ANNOTATION)));
                cu.getImports().forEach(im -> edges.add(edge(fqn, im.getNameAsString(), EdgeType.IMPORT)));
            }
        }
        edges.removeIf(e -> e.to().isBlank());
        edges.forEach(e -> nodes.putIfAbsent(e.to(), new GraphNode(e.to(), e.to().contains(".")?"CLASS":"PACKAGE", packageOf(e.to()))));
        return new GraphResult(new ArrayList<>(nodes.values()), edges);
    }

    private GraphEdge edge(String from, String to, EdgeType type) { return new GraphEdge(from, to, type, switch (type){case EXTENDS,IMPLEMENTS -> 1.5; case METHOD_CALL,CONSTRUCTOR_CALL -> 1.2; default -> 1.0;}); }
    private String packageOf(String fqn){ int i=fqn.lastIndexOf('.'); return i>0?fqn.substring(0,i):"default"; }
    private String qualify(String pkg, String name, CompilationUnit cu) {
        String cleaned = name.replaceAll("<.*>","").trim();
        if (cleaned.contains(".")) return cleaned;
        return cu.getImports().stream().map(i -> i.getNameAsString()).filter(n -> n.endsWith("."+cleaned)).findFirst().orElse(pkg + "." + cleaned);
    }
    private String bestEffort(String pkg, String symbol, CompilationUnit cu) { return qualify(pkg, symbol, cu); }
}

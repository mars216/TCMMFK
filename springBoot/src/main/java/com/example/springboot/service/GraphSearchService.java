// com/example/springboot/service/GraphSearchService.java
package com.example.springboot.service;

import com.example.springboot.dto.GraphFilterDTO;
import com.example.springboot.dto.GraphSearchRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GraphSearchService {

    private final Neo4jClient neo4jClient;

    private static final Map<String, String> TYPE_LABEL_MAP;
    static {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("MedicineLevel1Category", "MedicineLevel1Category");
        m.put("MedicineLevel2Category", "MedicineLevel2Category");
        m.put("Herb", "Herb");
        m.put("Ingredients", "Ingredients");
        m.put("Protein", "Protein");
        m.put("Disease", "Disease");
        m.put("Pathway", "Pathway");
        TYPE_LABEL_MAP = Collections.unmodifiableMap(m);
    }

    private static final Map<String, String> FILTER_FIELD_MAP;
    private static final Map<String, String> DISPLAY_FIELD_MAP;
    static {
        Map<String, String> f = new LinkedHashMap<>();
        f.put("Herb", "herb_cn_name");
        f.put("Protein", "id");
        FILTER_FIELD_MAP = Collections.unmodifiableMap(f);

        Map<String, String> d = new LinkedHashMap<>();
        d.put("Herb", "herb_cn_name");
        d.put("Protein", "id");
        DISPLAY_FIELD_MAP = Collections.unmodifiableMap(d);
    }

    // Exact match types (case insensitive)
    private static final Set<String> EXACT_MATCH_TYPES;
    static {
        Set<String> s = new HashSet<>();
        s.add("Protein"); // Protein exact match by id
        EXACT_MATCH_TYPES = Collections.unmodifiableSet(s);
    }

    // ====== Fixed route map: includes both forward and backward steps (automatic bidirectional support) ======
    private static final Map<String, List<Step>> MOVES; // current type -> reachable (relationship, next type, direction)
    static {
        Map<String, List<Step>> m = new LinkedHashMap<>();
        // Helper: add both from->to (->) and to->from (<-)
        addBi(m, "MedicineLevel1Category", "HAS_CHILD", "MedicineLevel2Category");
        addBi(m, "MedicineLevel2Category", "INCLUDE_OF", "Herb");
        addBi(m, "Herb", "Herb_to_Ingredient", "Ingredients");
        addBi(m, "Ingredients", "Ingredient_to_Protein", "Protein");
        addBi(m, "Protein", "Protein_to_Disease", "Disease");
        addBi(m, "Protein", "Protein_to_Pathway", "Pathway");
        MOVES = toUnmodifiable(m);
    }
    private static void addBi(Map<String, List<Step>> m, String from, String rel, String to) {
        m.computeIfAbsent(from, k -> new ArrayList<Step>()).add(new Step(rel, to, true));   // from -[:rel]-> to
        m.computeIfAbsent(to,   k -> new ArrayList<Step>()).add(new Step(rel, from, false)); // to   <-[:rel]- from
    }
    private static Map<String, List<Step>> toUnmodifiable(Map<String, List<Step>> m) {
        Map<String, List<Step>> out = new LinkedHashMap<>();
        for (Map.Entry<String, List<Step>> e : m.entrySet()) {
            out.put(e.getKey(), Collections.unmodifiableList(e.getValue()));
        }
        return Collections.unmodifiableMap(out);
    }

    // ====== Main entry ======
    public Map<String, List<Map<String, Object>>> search(GraphSearchRequest req) {
        validate(req);

        int limit = (req.getLimitPerType() == null ? 1000 : req.getLimitPerType());

        // Valid filters
        List<GraphFilterDTO> filters =
                (req.getFilters() == null ? Collections.<GraphFilterDTO>emptyList() : req.getFilters())
                        .stream()
                        .filter(f -> f != null && StringUtils.hasText(f.getType()))
                        .collect(Collectors.toList());

        // Bind filter metadata
        List<FilterBinding> bindings = new ArrayList<>();
        int i = 0;
        for (GraphFilterDTO f : filters) {
            String typeKey = f.getType();
            String label = requireLabel(typeKey);
            String alias = "f" + (i++);
            String param = "kw_" + alias;

            String keyword = (f.getName() == null ? "" : f.getName().trim());
            String field = FILTER_FIELD_MAP.containsKey(typeKey) ? FILTER_FIELD_MAP.get(typeKey) : "name";
            boolean exact = EXACT_MATCH_TYPES.contains(typeKey);

            bindings.add(new FilterBinding(alias, label, param, keyword, field, exact, typeKey));
        }

        // Return type labels list
        List<String> returnLabels = new ArrayList<>();
        if (req.getReturnTypes() != null) {
            for (String key : req.getReturnTypes()) {
                returnLabels.add(requireLabel(key));
            }
        }

        Map<String, List<Map<String, Object>>> result = new LinkedHashMap<>();
        for (String rtLabel : returnLabels) {
            List<Map<String, Object>> rows = queryByFixedRoutes(rtLabel, bindings, limit);
            result.put(rtLabel, rows);
        }
        return result;
    }

    private void validate(GraphSearchRequest req) {
        if (req == null) throw new IllegalArgumentException("Request body is empty");
        if (CollectionUtils.isEmpty(req.getReturnTypes())) {
            throw new IllegalArgumentException("returnTypes cannot be empty");
        }
    }

    private String requireLabel(String typeKey) {
        String label = TYPE_LABEL_MAP.get(typeKey);
        if (!StringUtils.hasText(label)) {
            throw new IllegalArgumentException("Unknown type: " + typeKey);
        }
        return label;
    }

    // ====== Fixed route query (supports backward steps) ======
    private List<Map<String, Object>> queryByFixedRoutes(String returnLabel,
                                                         List<FilterBinding> bindings,
                                                         int limit) {
        // No filters: direct sampling
        if (bindings.isEmpty()) {
            String displayExpr = buildDisplayNameExpr(returnLabel);
            String cypher = "MATCH (n:`" + returnLabel + "`)\n" +
                    "RETURN DISTINCT {id: coalesce(n.id, id(n)), name: " + displayExpr + ", props: properties(n)} AS item\n" +
                    "LIMIT $limit";
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("limit", limit);
            return runAndFlat(cypher, params);
        }

        // 1) First MATCH all filter points + where
        StringBuilder cy = new StringBuilder();
        Map<String, Object> params = new HashMap<String, Object>();
        for (FilterBinding b : bindings) {
            cy.append("MATCH (").append(b.alias).append(":`").append(b.label).append("`)\n");
            if (StringUtils.hasText(b.keyword)) {
                if (b.exact) {
                    cy.append("WHERE toLower(").append(b.alias).append(".").append(b.field)
                            .append(") = toLower($").append(b.param).append(")\n");
                } else {
                    cy.append("WHERE toLower(").append(b.alias).append(".").append(b.field)
                            .append(") CONTAINS toLower($").append(b.param).append(")\n");
                }
                params.put(b.param, b.keyword);
            }
        }

        // 2) Introduce n:ReturnLabel
        cy.append("MATCH (n:`").append(returnLabel).append("`)\n");

        // 3) For each filter, build "reversible" fixed chain to n (AND semantics)
        List<String> eqConstraints = new ArrayList<String>();
        for (FilterBinding b : bindings) {
            List<Step> path = findPathBi(b.typeKey, returnLabel);
            if (path == null) {
                return Collections.<Map<String, Object>>emptyList(); // unreachable
            }
            if (path.isEmpty()) {
                eqConstraints.add("n = " + b.alias); // same type
            } else {
                String prev = b.alias;
                for (int idx = 0; idx < path.size(); idx++) {
                    Step s = path.get(idx);
                    boolean lastToN = (idx == path.size() - 1) && s.nextType.equals(returnLabel);
                    String nextVar = lastToN ? "n" : (b.alias + "_x" + idx);
                    if (s.forward) {
                        // prev -[:REL]-> next
                        cy.append("MATCH (").append(prev).append(")-[:").append(s.rel).append("]->(")
                                .append(nextVar).append(":`").append(s.nextType).append("`)\n");
                    } else {
                        // prev <-[:REL]- next
                        cy.append("MATCH (").append(prev).append(")<-[:").append(s.rel).append("]-(")
                                .append(nextVar).append(":`").append(s.nextType).append("`)\n");
                    }
                    prev = nextVar;
                }
            }
        }

        if (!eqConstraints.isEmpty()) {
            cy.append("WHERE ").append(joinAnd(eqConstraints)).append("\n");
        }

        // 4) Return
        String displayExpr = buildDisplayNameExpr(returnLabel);
        cy.append("RETURN DISTINCT {id: coalesce(n.id, id(n)), name: ").append(displayExpr)
                .append(", props: properties(n)} AS item\n")
                .append("LIMIT $limit");

        params.put("limit", limit);
        return runAndFlat(cy.toString(), params);
    }

    // BFS: find shortest path from fromType to toType in MOVES (bidirectional) with step directions
    private List<Step> findPathBi(String fromType, String toType) {
        if (fromType.equals(toType)) return Collections.<Step>emptyList();
        Deque<PathState> q = new ArrayDeque<PathState>();
        q.add(new PathState(fromType, new ArrayList<Step>()));
        Set<String> visited = new HashSet<String>();
        visited.add(fromType);

        while (!q.isEmpty()) {
            PathState s = q.poll();
            List<Step> outs = MOVES.get(s.type);
            if (outs == null) continue;
            for (Step step : outs) {
                if (visited.contains(step.nextType)) continue;
                List<Step> nextPath = new ArrayList<Step>(s.path);
                nextPath.add(step);
                if (step.nextType.equals(toType)) {
                    return nextPath;
                }
                visited.add(step.nextType);
                q.add(new PathState(step.nextType, nextPath));
            }
        }
        return null; // unreachable
    }

    // Execute and flatten
    private List<Map<String, Object>> runAndFlat(String cypher, Map<String, Object> params) {
        List<Map<String, Object>> rows = (List<Map<String, Object>>) neo4jClient.query(cypher).bindAll(params).fetch().all();
        List<Map<String, Object>> out = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> row : rows) {
            @SuppressWarnings("unchecked")
            Map<String, Object> item = (Map<String, Object>) row.get("item");
            Map<String, Object> flat = new LinkedHashMap<String, Object>();
            flat.put("id", item.get("id"));
            flat.put("name", item.get("name"));
            @SuppressWarnings("unchecked")
            Map<String, Object> props = (Map<String, Object>) item.get("props");
            if (props != null) {
                for (Map.Entry<String, Object> e : props.entrySet()) {
                    String k = e.getKey();
                    if (!"id".equals(k) && !"name".equals(k)) flat.put(k, e.getValue());
                }
            }
            out.add(flat);
        }
        return out;
    }

    // Display name expression: remove n.label, use head(labels(n)) as fallback
    private String buildDisplayNameExpr(String returnLabel) {
        String typeKey = returnLabel;
        String field = DISPLAY_FIELD_MAP.containsKey(typeKey) ? DISPLAY_FIELD_MAP.get(typeKey) : "name";
        return "coalesce(n." + field + ", n.name, head(labels(n)), '')";
    }

    private static String joinAnd(List<String> parts) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.size(); i++) {
            if (i > 0) sb.append(" AND ");
            sb.append(parts.get(i));
        }
        return sb.toString();
    }

    // ==== Structures ====
    private static class Step {
        final String rel;       // relationship type
        final String nextType;  // next node type
        final boolean forward;  // true: prev-[:rel]->next; false: prev<-[:rel]-next
        Step(String rel, String nextType, boolean forward) { this.rel = rel; this.nextType = nextType; this.forward = forward; }
    }
    private static class PathState {
        final String type;
        final List<Step> path;
        PathState(String type, List<Step> path) { this.type = type; this.path = path; }
    }
    private static class FilterBinding {
        final String alias;
        final String label;
        final String param;
        final String keyword;
        final String field;
        final boolean exact;
        final String typeKey;
        FilterBinding(String alias, String label, String param, String keyword, String field, boolean exact, String typeKey) {
            this.alias = alias; this.label = label; this.param = param; this.keyword = keyword; this.field = field; this.exact = exact; this.typeKey = typeKey;
        }
    }
}
package org.zfin.zirc.service;

import org.zfin.zirc.api.uischema.Control;
import org.zfin.zirc.api.uischema.Group;
import org.zfin.zirc.api.uischema.UiSchemaElement;
import org.zfin.zirc.api.uischema.VerticalLayout;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Walks a uiSchema and produces a {@code groupLabel -> [leafFieldName, ...]}
 * map for use by the per-aggregate status computers' bySection rollups.
 * Replaces hand-coded "fields in section X" lists in each computer with
 * a single source of truth driven by the schema's own Group structure.
 *
 * <p>Leaf field names are extracted from each {@link Control}'s
 * {@code scope} by taking the segment after the final {@code /} — so
 * {@code #/properties/lesionType} contributes {@code "lesionType"}.
 * Nested-object scopes like
 * {@code #/properties/background/properties/maternalBackground}
 * resolve to the deepest leaf ({@code "maternalBackground"}); the
 * intermediate {@code properties} token never lands in the output
 * since it isn't the final segment.
 *
 * <p>Groups without a label are skipped; nested Groups inside other
 * Groups (rare) contribute their fields under their own label.
 */
public final class SchemaSections {

    private SchemaSections() {}

    public static Map<String, List<String>> groupsToFields(UiSchemaElement root) {
        Map<String, List<String>> out = new LinkedHashMap<>();
        walk(root, out);
        return out;
    }

    private static void walk(UiSchemaElement node, Map<String, List<String>> out) {
        if (node == null) return;
        if (node instanceof Group g) {
            if (g.label() != null && !g.label().isBlank()) {
                List<String> fields = new ArrayList<>();
                collectControlFields(node, fields);
                out.put(g.label(), fields);
            }
            if (g.elements() != null) {
                for (UiSchemaElement child : g.elements()) walk(child, out);
            }
            return;
        }
        if (node instanceof VerticalLayout v && v.elements() != null) {
            for (UiSchemaElement child : v.elements()) walk(child, out);
        }
    }

    private static void collectControlFields(UiSchemaElement node, List<String> out) {
        if (node == null) return;
        if (node instanceof Control c && c.scope() != null) {
            int slash = c.scope().lastIndexOf('/');
            if (slash >= 0 && slash + 1 < c.scope().length()) {
                out.add(c.scope().substring(slash + 1));
            }
            return;
        }
        if (node instanceof Group g && g.elements() != null) {
            for (UiSchemaElement child : g.elements()) collectControlFields(child, out);
            return;
        }
        if (node instanceof VerticalLayout v && v.elements() != null) {
            for (UiSchemaElement child : v.elements()) collectControlFields(child, out);
        }
    }
}

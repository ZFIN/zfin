package org.zfin.zirc.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.Test;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.Assert.fail;

/**
 * Static-analysis drift check for the hand-curated OpenAPI spec.
 *
 * <p>For every (path, HTTP method) declared in
 * {@code home/WEB-INF/openapi/zirc-api.yaml}, asserts there's a matching
 * Spring {@code @RestController} handler in {@code org.zfin.zirc.api}.
 * The inverse — every handler annotation must appear in the YAML — is
 * also checked, so adding a new endpoint without updating the spec
 * breaks CI.
 *
 * <p>No Spring context, no HTTP, no DB: pure reflection over the four
 * known ZIRC API controllers. Fast and stable.
 *
 * <p>Was originally written as a Spock spec, but Spock 2.x requires
 * {@code useJUnitPlatform()} which is commented out in this codebase —
 * the existing Spock specs are silently dormant in CI. Reverting to
 * JUnit 4 keeps this test in the path that actually runs today.
 */
public class ZircOpenApiDriftTest {

    /** Controllers in scope. Mirror in YAML or pop out of scope. */
    private static final List<Class<?>> CONTROLLERS = Arrays.asList(
            ZircSubmissionApiController.class,
            ZircMutationApiController.class,
            ZircAssayApiController.class,
            ZircAutocompleteApiController.class,
            ZircGeneApiController.class,
            ZircLesionApiController.class,
            ZircPhenotypeApiController.class,
            ZircOpenApiController.class);

    /** HTTP-method annotations Spring uses on handler methods. */
    private static final Map<Class<? extends Annotation>, String> METHOD_ANNOS = new LinkedHashMap<>();
    static {
        METHOD_ANNOS.put(GetMapping.class,    "get");
        METHOD_ANNOS.put(PostMapping.class,   "post");
        METHOD_ANNOS.put(PutMapping.class,    "put");
        METHOD_ANNOS.put(PatchMapping.class,  "patch");
        METHOD_ANNOS.put(DeleteMapping.class, "delete");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> loadYaml() throws Exception {
        // Working directory differs depending on how Gradle invokes the test;
        // probe both common roots.
        File spec = new File("home/WEB-INF/openapi/zirc-api.yaml");
        if (!spec.exists()) {
            spec = new File("../home/WEB-INF/openapi/zirc-api.yaml");
        }
        if (!spec.exists()) {
            throw new IllegalStateException(
                    "Could not locate zirc-api.yaml from cwd=" + new File(".").getAbsolutePath());
        }
        ObjectMapper m = new ObjectMapper(new YAMLFactory());
        return m.readValue(spec, Map.class);
    }

    @Test
    public void everyYamlEndpointHasASpringHandler() throws Exception {
        Set<String> specEndpoints = collectSpecEndpoints(loadYaml());
        Set<String> handlers = collectHandlerEndpoints();
        Set<String> missing = new TreeSet<>(specEndpoints);
        missing.removeAll(handlers);
        if (!missing.isEmpty()) {
            fail("Endpoints declared in the OpenAPI spec but missing a Spring handler:\n  - "
                    + String.join("\n  - ", missing));
        }
    }

    @Test
    public void everySpringHandlerHasAYamlEntry() throws Exception {
        Set<String> specEndpoints = collectSpecEndpoints(loadYaml());
        Set<String> handlers = collectHandlerEndpoints();
        Set<String> missing = new TreeSet<>(handlers);
        missing.removeAll(specEndpoints);
        if (!missing.isEmpty()) {
            fail("Spring handlers present in code but missing from the OpenAPI spec:\n  - "
                    + String.join("\n  - ", missing));
        }
    }

    @SuppressWarnings("unchecked")
    private static Set<String> collectSpecEndpoints(Map<String, Object> yaml) {
        Set<String> result = new TreeSet<>();
        Map<String, Map<String, Object>> paths = (Map<String, Map<String, Object>>) yaml.get("paths");
        for (Map.Entry<String, Map<String, Object>> e : paths.entrySet()) {
            for (String op : e.getValue().keySet()) {
                if (METHOD_ANNOS.containsValue(op)) {
                    result.add(op.toUpperCase() + " " + e.getKey());
                }
            }
        }
        return result;
    }

    /**
     * Walks {@link #CONTROLLERS} and returns the endpoints declared by their
     * mapping annotations, formatted as {@code "METHOD path"} with the
     * {@code /api/zirc} prefix stripped so they match the YAML's relative
     * paths.
     */
    private static Set<String> collectHandlerEndpoints() throws Exception {
        Set<String> result = new TreeSet<>();
        for (Class<?> controller : CONTROLLERS) {
            String classPrefix = classRequestMapping(controller);
            for (Method method : controller.getDeclaredMethods()) {
                for (Map.Entry<Class<? extends Annotation>, String> e : METHOD_ANNOS.entrySet()) {
                    Annotation anno = method.getAnnotation(e.getKey());
                    if (anno == null) {continue;}
                    for (String methodPath : pathsFor(anno)) {
                        String full = classPrefix + methodPath;
                        String relative = stripApiZircPrefix(full);
                        result.add(e.getValue().toUpperCase() + " " + relative);
                    }
                }
            }
        }
        return result;
    }

    private static String classRequestMapping(Class<?> controller) {
        RequestMapping rm = controller.getAnnotation(RequestMapping.class);
        if (rm == null) {return "";}
        String[] values = rm.path();
        if (values.length == 0) {values = rm.value();}
        return values.length == 0 ? "" : values[0];
    }

    /** Spring mapping annotations expose path() and value() as String[]; prefer path(). */
    private static String[] pathsFor(Annotation anno) throws Exception {
        try {
            String[] p = (String[]) anno.getClass().getMethod("path").invoke(anno);
            if (p != null && p.length > 0) {return p;}
        } catch (NoSuchMethodException ignored) {
            // fall through
        }
        try {
            String[] v = (String[]) anno.getClass().getMethod("value").invoke(anno);
            if (v != null && v.length > 0) {return v;}
        } catch (NoSuchMethodException ignored) {
            // fall through
        }
        return new String[]{""};
    }

    private static String stripApiZircPrefix(String full) {
        if (full.startsWith("/api/zirc")) {
            return full.substring("/api/zirc".length());
        }
        return full;
    }
}

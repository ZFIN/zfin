package org.zfin.zirc.api;

import jakarta.servlet.ServletContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;

/**
 * Serves the hand-curated OpenAPI 3 spec for the ZIRC API as a static
 * YAML file. See {@code reference/zirc-openapi-approach.md} for why we
 * picked hand-curation over springdoc-openapi here.
 *
 * <p>The spec lives at {@code /WEB-INF/openapi/zirc-api.yaml} so it ships
 * with the deployed webapp but isn't directly reachable as a static
 * resource. This controller wraps it in a normal HTTP response.
 */
@RestController
@RequestMapping("/api/zirc")
public class ZircOpenApiController {

    private static final String SPEC_PATH    = "/WEB-INF/openapi/zirc-api.yaml";
    private static final String UI_PATH      = "/WEB-INF/openapi/swagger-ui.html";
    private static final String UI_CSS_PATH  = "/WEB-INF/openapi/swagger-ui/swagger-ui.css";
    private static final String UI_JS_PATH   = "/WEB-INF/openapi/swagger-ui/swagger-ui-bundle.js";
    private static final MediaType APPLICATION_YAML = MediaType.parseMediaType("application/yaml");

    @Autowired
    private ServletContext servletContext;

    @GetMapping(value = "/openapi.yaml", produces = "application/yaml")
    public ResponseEntity<InputStreamResource> getOpenApiSpec() {
        return streamWebInfResource(SPEC_PATH, APPLICATION_YAML);
    }

    /**
     * Renders a Swagger UI shell that loads the YAML above. The UI assets
     * themselves come from a CDN for now — see the swagger-ui.html comment
     * for the vendoring follow-up.
     */
    @GetMapping(value = "/docs", produces = "text/html")
    public ResponseEntity<InputStreamResource> getDocsUi() {
        return streamWebInfResource(UI_PATH, MediaType.TEXT_HTML);
    }

    @GetMapping(value = "/docs/swagger-ui.css", produces = "text/css")
    public ResponseEntity<InputStreamResource> getSwaggerUiCss() {
        return streamWebInfResource(UI_CSS_PATH, MediaType.parseMediaType("text/css"));
    }

    @GetMapping(value = "/docs/swagger-ui-bundle.js", produces = "application/javascript")
    public ResponseEntity<InputStreamResource> getSwaggerUiBundle() {
        return streamWebInfResource(UI_JS_PATH, MediaType.parseMediaType("application/javascript"));
    }

    private ResponseEntity<InputStreamResource> streamWebInfResource(String path, MediaType type) {
        InputStream stream = servletContext.getResourceAsStream(path);
        if (stream == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok()
                .contentType(type)
                .body(new InputStreamResource(stream));
    }
}

# ZIRC OpenAPI spec: postponed

**Status**: postponed pending a comprehensive, codebase-wide approach.

The hand-curated YAML + drift test + vendored Swagger UI shipped during
the M1–M8 work has been removed. The implementation was self-contained
to the ZIRC API surface (one controller, one YAML file, one drift test,
one HTML shell, two vendored JS/CSS files) and worked, but as a
**one-off** in a codebase otherwise devoid of OpenAPI documentation it
created a maintenance asymmetry: every new ZIRC endpoint had to be
mirrored into the YAML or CI would fail, while the rest of the codebase
had no such expectation. Curators of other features who don't already
know the convention would have to discover it from this corner.

Rather than carry the asymmetry, we removed the artifact and parked
the work. The original rationale and decision-evaluation appear in the
"Original notes (May 2026)" section below for context when the project
is picked up again.

---

## Return-to-this triggers

Revisit when **any one** of the following becomes true:

- The codebase adopts Spring Boot, which makes `springdoc-openapi-starter-webmvc-ui`
  drop-in and removes the strongest argument against Option A in the
  original notes below.
- A second feature area independently grows an OpenAPI surface, making
  a shared `OpenApiController` + shared codegen pipeline cheaper than
  one-offs.
- A client-codegen pipeline is wanted for the ZIRC API specifically
  (e.g. an external partner consuming the line-submission API), at
  which point the hand-curated approach can come back narrowly.
- A code-generator that produces OpenAPI YAML from Spring MVC + Jackson
  via reflection (without springdoc's Boot dependency) reaches enough
  maturity to be worth dropping into the build.

## What was removed

- `home/WEB-INF/openapi/zirc-api.yaml` — hand-curated OpenAPI 3 spec
  covering all 31 ZIRC API endpoints.
- `home/WEB-INF/openapi/swagger-ui.html` + `swagger-ui/` directory —
  vendored swagger-ui-dist (CSS + JS bundle) used by the docs route.
- `source/org/zfin/zirc/api/ZircOpenApiController.java` — the
  controller that served `/api/zirc/openapi.yaml` and `/api/zirc/docs`.
- `test/org/zfin/zirc/api/ZircOpenApiDriftTest.java` — the JUnit 4
  reflection drift test that asserted controllers ↔ YAML alignment.

The DTOs, controllers, and form schemas they documented are unchanged.
`FormSchemaSnapshotTest` still locks down the wire shape of the
form-schema responses.

---

## Original notes (May 2026)

> The text below is the original decision document from when the
> hand-curated approach was selected. It is retained verbatim so the
> reasoning is available when the project is picked up again.

**Goal**: expose an OpenAPI 3 specification for the ZIRC JSON API so other
developers (and eventually CI codegen) can consume the contract without
reading Java source.

**Constraint**: this codebase runs plain Spring MVC 6.1 on Tomcat — there is
no Spring Boot autoconfig in the picture. That eliminates the
`springdoc-openapi-starter-*` happy path most blog posts assume.

### Options evaluated

#### Option A: `springdoc-openapi` v2 without Spring Boot

Add `org.springdoc:springdoc-openapi-starter-webmvc-api:2.7.0` (or later) as
an `implementation` dependency, then manually `@Import` the springdoc
`@Configuration` classes (`SpringDocConfiguration`,
`SpringDocWebMvcConfiguration`, `MultipleOpenApiSupportConfiguration` etc.)
into our existing `ZfinWebConfigConfiguration`. Provide explicit
`@Bean OpenAPI` and `@Bean SpringDocConfigProperties` instances since Boot's
`@ConfigurationProperties` binding isn't running.

**Pros**

- Spec is always in sync with the controllers — driven by reflection over
  `@RestController` + Jackson types.
- Free Swagger UI at `/swagger-ui/index.html` if we also pull in
  `springdoc-openapi-starter-webmvc-ui`.

**Cons**

- springdoc v2's bean wiring leans on Spring Boot's
  `@ConditionalOnMissingBean` machinery and on properties like
  `springdoc.api-docs.enabled` that come from Boot's environment binding.
  Without Boot, several of these conditionals don't evaluate the way
  the springdoc team intends. Past community reports describe ~1–3 days of
  fiddling to get a clean boot, and breakage on minor version bumps.
- The package-private `SpringDocConfiguration` makes the `@Import` path
  brittle: it's not designed as a public extension point.
- Risk of fighting Spring Security's filter chain over the
  `/v3/api-docs` and `/swagger-ui/**` paths.

#### Option B: hand-curated YAML served as a static file

Write `home/WEB-INF/openapi/zirc-api.yaml` covering all ZIRC endpoints,
serve it via a small `@RestController` that returns the file body with
`text/yaml`. Optionally embed a Swagger UI page using the standalone
`swagger-ui-dist` static assets (no Spring integration required).

**Pros**

- Zero risk to the existing app; no Spring config changes.
- Works regardless of Spring version or build environment.
- The spec is reviewable in code review when controllers change.

**Cons**

- Drift between the code and the spec is on the author to prevent.
- Need a discipline of updating the YAML when adding endpoints.

**Mitigation for drift**: a JUnit 4 test that walks the YAML and
asserts every `(method, path)` pair appears in both the YAML and the
controllers. Fails CI if a path is in the YAML but no `@*Mapping`
covers it, or vice versa.

#### Option C: swagger-core annotations + build-time YAML generation

Annotate every controller method and DTO record with `@Operation`,
`@Parameter`, `@Schema`, etc. Add a Gradle task that runs
`io.swagger.core.v3:swagger-jaxrs2-jakarta` (or the springmvc-aware
equivalent) to render YAML at build time. Commit the YAML output.

**Pros**

- Spec in sync via the annotations, without springdoc.
- No runtime cost in the application JAR.

**Cons**

- Introduces another build step.
- Significant annotation retrofit across all endpoints + DTOs.
- Tooling support for "Spring MVC without Boot, with reflection on
  controllers" is patchy compared with springdoc.

### Original decision: Option B

Rationale at the time: the ZIRC API had stabilized at ~12 endpoints
across 3 controllers; the cost of writing the YAML once was bounded,
and the spec gets us 80% of the value of springdoc with 5% of the risk.
The drift concern was real but tractable with a single drift test as
documented above. The Spring Boot migration was a known future
direction; when that lands, switching to Option A becomes near-trivial.

The implementation grew with M5–M8 to 31 paths across 8 controllers
before being removed for the asymmetry reason described at the top of
this file. Re-derive the endpoint count when picking the project up
again — it will be different.

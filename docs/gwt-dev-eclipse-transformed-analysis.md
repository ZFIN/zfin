# Analysis: gwt-dev-eclipse-transformed.jar

## What it is

A **fat/uber JAR** (41 MB, 22,672 entries, 17,622 `.class` files) containing the GWT 2.8.2 development compiler plus all transitive dependencies, with `javax.servlet` references renamed to `jakarta.servlet` using an Eclipse transformation tool.

- **GWT version**: 2.8.2 (git rev `faff18e7a`)
- **Built with**: JDK 1.8.0_112-google, Ant 1.9.3
- **Location**: `lib/Java/gwt/gwt-dev-eclipse-transformed.jar`

## Bundled dependencies

| Library | Version |
|---|---|
| Eclipse JDT Compiler | ~1,296 classes |
| Jetty (all modules) | 9.2.14.v20151106 |
| HtmlUnit | 2.19 |
| Google Guava (shaded) | `com.google.gwt.thirdparty.guava` |
| Protobuf Java | 2.5.0 |
| Gson | 2.6.2 |
| Apache BCEL | bundled |
| Apache Ant (taskdefs) | bundled |
| ICU4J | bundled |
| CERN Colt | bundled |
| Commons Codec | 1.10 |
| Commons Collections | 3.2.2 |
| Commons IO | 2.4 |
| Commons Lang3 | 3.4 |
| Commons Logging | 1.2 |
| HttpClient | 4.5.1 |
| CSS Parser | 0.9.18 |
| Apache Jasper | 8.0.9.M3 |
| Rhino JS (via htmlunit-core-js) | bundled |

A decompiled version (via fernflower) exists at the repo root: `gwt-dev-eclipse-transformed-decompiled.jar` (23 MB, 11,035 `.java` files).

## How it's used

### In `build.gradle`

The `gwtCompile` task (line ~1441) compiles all three GWT modules:
- `org.zfin.gwt.lookup.Lookup`
- `org.zfin.gwt.marker.Marker`
- `org.zfin.gwt.curation.Curation`

Its classpath is:
```groovy
classpath = files('home/WEB-INF/classes') +
            fileTree(dir: 'lib/Java/gwt', include: '*.jar') +  // <-- picks up the fat JAR
            files('home/WEB-INF/lib/restygwt-2.2.7-eclipse-transformed.jar') +
            files('source')
```

The `fileTree(dir: 'lib/Java/gwt', include: '*.jar')` glob picks up this JAR, which provides the `com.google.gwt.dev.Compiler` main class and all its runtime dependencies.

### Version mismatch is intentional

- `compileOnly "org.gwtproject:gwt-dev:2.11.0"` — used for **Java IDE compilation and type checking only**
- `gwt-dev-eclipse-transformed.jar` (GWT **2.8.2**) — used for **actual GWT→JavaScript compilation**

The `compileGwtModule` task (line ~1418) uses `configurations.compileClasspath` (Maven-resolved 2.11.0), but it's a single-module convenience task. The `gwtCompile` task that actually runs during builds uses local file JARs including the fat 2.8.2 JAR.

## Why the fat JAR exists

1. GWT 2.8.2's `gwt-dev` uses `javax.servlet` internally
2. The ZFIN project migrated to Jakarta EE (`jakarta.servlet`)
3. The JAR was created by running an Eclipse transformation tool to rename all `javax.servlet` → `jakarta.servlet` references in the bytecode
4. No Maven artifact exists for this — it's a custom one-off repackaging
5. The stock `gwt-dev-2.11.0.jar` from Maven is a **thin JAR** (16 MB, 6,466 classes) that expects transitive deps to be provided separately, unlike this fat JAR

## Attempt to replace with Maven-resolved gwt-dev 2.11.0

### What we tried

Changed the `gwtCompile` classpath from local file JARs to:
```groovy
classpath = configurations.compileClasspath +
            fileTree(dir: 'lib/Java/gwt', include: '*.jar') +
            files('source')
```

This successfully resolved all transitive dependencies (cern/colt, ASM, Eclipse JDT, etc.) via Maven.

### What happened

GWT compilation failed with errors like:
```
[ERROR] Line 6: The import org.zfin.framework.presentation.LookupStrings cannot be resolved
[ERROR] Line 53: LookupStrings cannot be resolved to a variable
```

And `javax.validation` source errors (fixed by keeping `fileTree` for `validation-api-1.0.0.GA-sources.jar`).

### Root cause: GWT 2.11.0 source resolution behavior change

**GWT 2.8.2** behavior: When both `.java` source and `.class` bytecode are on the classpath for a class, the compiler uses the **bytecode** for non-GWT-module classes.

**GWT 2.11.0** behavior: The compiler prefers the **`.java` source** and tries to GWT-compile it.

The `source/` directory is on the GWT classpath (required for GWT module source). It contains `source/org/zfin/framework/presentation/LookupStrings.java`, which:
- Is **not** inside any GWT `<source>` path (GWT modules only include `ui`, `dto`, `event`, `util` sub-packages)
- Imports `org.springframework.ui.Model`, which GWT cannot compile
- Was previously resolved from `home/WEB-INF/classes/` as bytecode by GWT 2.8.2

The `LookupStrings` class is used by GWT client code (e.g., `source/org/zfin/gwt/lookup/ui/Lookup.java` line 6 imports it). It's a constants class that holds string values shared between GWT client and server code.

## GWT module structure

```
source/org/zfin/gwt/lookup/Lookup.gwt.xml  → <source path='ui'/>
source/org/zfin/gwt/marker/Marker.gwt.xml  → (inherits Root)
source/org/zfin/gwt/curation/Curation.gwt.xml → (inherits Root)
source/org/zfin/gwt/root/Root.gwt.xml      → <source path='dto'/> <source path='event'/> <source path='ui'/> <source path='util'/>
```

## Other "eclipse-transformed" JARs in the project

These JARs also underwent javax→jakarta transformation:
- `home/WEB-INF/lib/blast-serialization-1.0-eclipse-transformed.jar`
- `home/WEB-INF/lib/jakarta.servlet.jsp.jstl-api-1.2.7-eclipse-transformed.jar`
- `home/WEB-INF/lib/rescu-2.1.0-eclipse-transformed.jar`
- `home/WEB-INF/lib/restygwt-2.2.7-eclipse-transformed.jar`
- `home/WEB-INF/lib/taglibs-standard-impl-1.2.5-eclipse-transformed.jar`
- `lib/Java/gwt/gwt-jackson-0.15.0-eclipse-transformed.jar`
- `lib/Java/gwt/jackson-annotations-2.8.2-sources-eclipse-transformed.jar`
- `lib/Java/gwt/jsinterop-annotations-2.0.0-eclipse-transformed.jar`

## Resolution: removed the fat JAR and switched to Maven-resolved gwt-dev 2.11.0

### Issues encountered and fixes applied

#### 1. Unused server-side imports in GWT-visible source files

GWT 2.11.0 is stricter about resolving imports in `.java` files found on the classpath. Three files had unused imports that caused compilation failures:

| File | Removed import | Reason |
|---|---|---|
| `LookupStrings.java` | `org.springframework.ui.Model` | Used only in `idNotFound()` method, which was server-side only. Method inlined into the 2 callers (`ShowPublicationController`, `OrthologyController`) and removed. |
| `RelatedEntityDTO.java` | `org.zfin.publication.Publication` | Completely unused import (Publication has Hibernate annotations GWT can't compile). |
| `ExpressionCurationService.java` | `org.springframework.web.bind.annotation.PathVariable` | Unused import (code uses `@PathParam` from Jakarta WS-RS instead). |
| `FeatureAddView.java` | `javassist.compiler.ast.StringL` | Completely unused import. |

#### 2. Missing CSS SAC dependency

GWT's UiBinder needs `org.w3c.css.sac.CSSException` for processing CSS in `.ui.xml` templates. The old fat JAR bundled `cssparser-0.9.18` which included this. GWT 2.11.0's transitive dependencies don't include it.

**Fix:** Added `compileOnly "org.w3c.css:sac:1.3"` to `build.gradle`.

#### 3. Jackson annotations sources JAR conflict

`gwt-jackson:0.15.4` declares a dependency on `jackson-annotations` with the `sources` classifier (standard for GWT libraries). Maven resolves this to `jackson-annotations-2.15.2-sources.jar`, which uses `String.format(String, Set<String>)` — not supported by GWT's JRE emulation.

**Fix:** Filtered `jackson-annotations-*-sources.jar` from the GWT compilation classpath. The local `lib/Java/gwt/jackson-annotations-2.8.2-sources-eclipse-transformed.jar` provides a compatible version.

#### 4. Stale UiBinder XML templates in build output

`sourceSets.main.output` includes `build/resources/main/` which contained outdated copies of `.ui.xml` template files (missing `ui:field` attributes that exist in the current source). When placed first on the classpath, GWT found the stale templates instead of the current ones.

**Fix:** Changed classpath ordering to put `files('source')` first, then `files('home/WEB-INF/classes')` for compiled bytecode, then Maven dependencies.

#### 5. Compiled classes not on GWT classpath

`configurations.compileClasspath` does not include the compiled output directory (`home/WEB-INF/classes`). Classes like `LookupStrings` and `View` that are referenced by GWT client code but live outside GWT `<source>` paths need to be available as bytecode.

**Fix:** Explicitly added `files('home/WEB-INF/classes')` to the GWT compilation classpath.

### Final classpath configuration

```groovy
// Filter out jackson-annotations-*-sources.jar from Maven;
// the local eclipse-transformed sources JAR in lib/Java/gwt/ is used instead.
def filteredCompileClasspath = configurations.compileClasspath.filter { file ->
    !file.name.matches('jackson-annotations-.*-sources\\.jar')
}
classpath = files('source') +
            files('home/WEB-INF/classes') +
            filteredCompileClasspath +
            fileTree(dir: 'lib/Java/gwt', include: '*.jar')
```

Classpath ordering matters:
1. `source/` — GWT module source files and `.ui.xml` templates (must be first to avoid stale copies)
2. `home/WEB-INF/classes/` — compiled bytecode for non-GWT classes referenced by GWT code
3. `configurations.compileClasspath` — Maven-resolved dependencies including `gwt-dev:2.11.0` and transitive deps
4. `lib/Java/gwt/*.jar` — local GWT JARs (`gwt-user`, `validation-api-sources`, `jackson-annotations-sources-eclipse-transformed`, etc.)

### Files changed

- `build.gradle` — updated GWT compilation classpath, added `org.w3c.css:sac:1.3` dependency
- `lib/Java/gwt/gwt-dev-eclipse-transformed.jar` — **deleted** (41 MB)
- `source/org/zfin/framework/presentation/LookupStrings.java` — removed Spring import and `idNotFound()` method
- `source/org/zfin/gwt/root/dto/RelatedEntityDTO.java` — removed unused `Publication` import
- `source/org/zfin/gwt/curation/ui/ExpressionCurationService.java` — removed unused `@PathVariable` import
- `source/org/zfin/gwt/curation/ui/feature/FeatureAddView.java` — removed unused `javassist` import
- `source/org/zfin/publication/presentation/ShowPublicationController.java` — inlined `idNotFound()` logic
- `source/org/zfin/orthology/presentation/OrthologyController.java` — inlined `idNotFound()` logic

## Environment notes

- The build must run in the Docker compile container (`ghcr.io/zfin/zfin-compile:main`) which has Java 21
- The local machine has Java 19, which is insufficient (`sourceCompatibility = '21'`)
- `TARGETROOT` env var must be set (defaults to `null` otherwise, causing path issues)
- The `buildSrc/` directory may have permission issues (files owned by `go` user) — fixable via `docker run --rm -v .../buildSrc:/work alpine chmod -R 777 /work/build /work/.gradle`

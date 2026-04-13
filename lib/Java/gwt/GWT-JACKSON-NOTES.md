# GWT compilation and Jackson dependency notes

This directory used to contain 9 checked-in jar files for GWT compilation.
All have been migrated to Maven dependencies. This document explains the
non-obvious constraints that future developers need to be aware of.

## Background: why GWT needs .java sources

GWT (Google Web Toolkit) compiles Java source code to JavaScript for the
browser. Unlike normal Java compilation, the GWT compiler needs **`.java`
source files** on its classpath — not just `.class` bytecode. Any library
used in GWT client-side code must have its sources available at compile time.

This is why GWT dependencies often require special handling: the standard
Maven jar (bytecode only) isn't enough. Libraries must either bundle `.java`
sources inside the jar, or a separate `-sources.jar` classifier must be
provided.

## GWT JRE emulation and String.format

GWT provides a limited emulation of the Java standard library that runs in
the browser. Notably, **`String.format()` is not supported** — not even the
basic `String.format(String, Object...)` variant.

This is a 16-year-old gap:
- GitHub issue: https://github.com/gwtproject/gwt/issues/3946 (filed 2009)
- Status: Open, marked "PatchesWelcome" and "AssumedStale"
- GWT 2.11.0 and 2.12.0 did not add it
- No indication it will be added in a future release

This has a direct impact on which library versions can be used with GWT.

## jackson-annotations version constraint

The project uses Jackson 2.15.2 (forced via resolution strategy in
build.gradle). However, **jackson-annotations 2.12.0+** introduced
`JsonIncludeProperties.java`, which calls `String.format()`. Since GWT
can't compile `String.format`, the GWT compiler fails if it sees
jackson-annotations 2.12+ sources on its classpath.

**Timeline:**
- jackson-annotations 2.11.x and earlier: GWT-compatible
- jackson-annotations 2.12.0 (November 2020): introduced `JsonIncludeProperties`
  with `String.format()` call -- **GWT-incompatible**
- jackson-annotations 2.13+, 2.14+, 2.15+: still GWT-incompatible

**Solution in build.gradle:**

1. A `gwtSources` configuration pulls `jackson-annotations:2.11.4:sources`
   from Maven — the newest GWT-compatible version.

2. A denylist filter on `compileClasspath` blocks `jackson-annotations-*-sources.jar`
   (which would be the 2.15.2 sources, pulled transitively by gwt-jackson's
   Maven metadata) from reaching the GWT compiler.

3. The `gwtSources` configuration is excluded from the `configurations.all`
   resolution strategy that forces Jackson to 2.15.2. Without this exclusion,
   Gradle would resolve `jackson-annotations:2.11.4:sources` to
   `jackson-annotations:2.15.2:sources`, defeating the purpose.

4. The `gwtSources` configuration is added to the GWT classpath separately,
   so the 2.11.4 sources are available.

```
GWT classpath order:
  1. source/             -- project GWT module source and .ui.xml templates
  2. home/WEB-INF/classes -- compiled bytecode for non-GWT classes
  3. compileClasspath     -- Maven deps (filtered: no jackson-annotations sources)
  4. gwtSources           -- jackson-annotations 2.11.4 sources (GWT-compatible)
  5. lib/Java/gwt/*.jar   -- (empty now, but available for future local jars)
```

## gwt-jackson

The `gwt-jackson` library (JSON serialization for GWT, used by RestyGWT)
is declared as `compileOnly "com.github.nmorel.gwtjackson:gwt-jackson:0.15.4"`
in build.gradle. The Maven jar includes .java sources, so no special handling
is needed.

Previously a checked-in `gwt-jackson-0.15.0-eclipse-transformed.jar` was used.
The "eclipse-transformed" name was misleading — there were no servlet namespace
transformations in this jar. The Maven 0.15.4 version works as a direct replacement.

## validation-api

GWT's built-in validation module needs `javax.validation` sources. These are
pulled from Maven via:

```groovy
compileOnly("javax.validation:validation-api:1.0.0.GA") { artifact { classifier = 'sources' } }
```

The `:sources` classifier is critical — the standard jar has only bytecode.

## When to revisit this

- **If GWT adds `String.format` support**: The `gwtSources` configuration and
  denylist filter can be removed entirely. The standard jackson-annotations
  from `compileClasspath` will work. Check gwtproject/gwt#3946.

- **If upgrading Jackson**: Versions up to 2.11.x are GWT-compatible. If
  upgrading past 2.11.x (already the case at 2.15.2), the `gwtSources`
  configuration must continue to pin an older sources version.

- **If removing GWT entirely**: All GWT-related `compileOnly` dependencies,
  the `gwtSources` configuration, and the GWT compilation tasks can be removed.
  The `home/WEB-INF/lib/restygwt-*` and `gwt-servlet-*` jars can also go.

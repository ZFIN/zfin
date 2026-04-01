## Remaining checked-in GWT jars

These 2 jars cannot be replaced by Maven dependencies (as of 2026-04-01).
All other GWT jars have been migrated to Maven — see build.gradle `compileOnly` deps.

### jackson-annotations-2.8.2-sources-eclipse-transformed.jar (58KB)

Sources-only jar (41 .java files, 0 .class files) containing jackson-annotations
2.8.2 source code. GWT compiles Java to JavaScript on the client side, so it needs
.java sources for any library used in GWT client code. The project's gwt-jackson
library depends on jackson-annotations.

**Why it can't be replaced with Maven:** The project forces jackson-annotations to
2.15.2 (via resolution strategy), and the 2.15.2 sources use
`String.format(String, Set<String>)` in `JsonIncludeProperties.java` — a method
that doesn't exist in GWT's JRE emulation. The 2.8.2 sources predate that class
entirely, so they're GWT-compatible.

The "eclipse-transformed" name is misleading — there are no servlet references to
transform in jackson-annotations. It's really just "pinned to an old GWT-compatible
version."

### gwt-jackson-0.15.0-eclipse-transformed.jar (695KB)

Hybrid jar (184 .java files + 303 .class files) — the gwt-jackson library that
provides JSON serialization/deserialization for GWT client code. Used by RestyGWT
to marshal REST responses.

**Why it can't be replaced with Maven:** Maven has gwt-jackson 0.15.4 (declared as
`compileOnly` in build.gradle), but:

1. The version is pinned to 0.15.0, not 0.15.4 — there may be a compatibility reason
2. GWT needs the .java sources bundled inside; the Maven 0.15.4 jar may or may not
   include them

The "eclipse-transformed" name is again misleading — no servlet references exist in
this jar either.

### Future work

Both jars could potentially be eliminated:

- **jackson-annotations**: Find the newest version whose sources are GWT-compatible
  (somewhere between 2.8.2 and 2.15.2), then pull that version's sources jar from Maven.
- **gwt-jackson**: Test whether Maven's 0.15.4 jar includes .java sources and compiles
  cleanly with GWT.

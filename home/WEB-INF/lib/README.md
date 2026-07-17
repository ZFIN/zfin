# home/WEB-INF/lib — vendored jars

Almost all of ZFIN's Java dependencies are declared as Maven coordinates in
`build.gradle` and resolved from remote repositories. The jars committed **here**
are the deliberate exceptions: they are vendored into the repo because there is
no equivalent artifact to pull from a public Maven repository.

They are not picked up by being in this directory. Each is listed explicitly in
`build.gradle` (`localFileDependencies`) and added to the compile/runtime
classpath via `implementation files(localFileDependencies)`. The webapp copy
step excludes `WEB-INF/lib/**`, so this file is documentation only and is never
deployed.

## Why each jar is vendored

There are two root causes.

**1. javax → jakarta namespace transformation.** During the migration to the
Jakarta EE namespace, several upstream libraries had no jakarta-native release at
the version ZFIN needed. Those jars were run through the Eclipse Transformer
(which rewrites `javax.*` references to `jakarta.*` in the bytecode) and the
result vendored here. The transformed artifact is not published on Maven, so it
cannot be expressed as a coordinate.

**2. Unpublished internal / legacy jars.** A few libraries are not available on
public Maven at all (internal builds, or old artifacts predating Maven Central
availability).

| Jar | Reason | Notes |
|---|---|---|
| `restygwt-2.2.7-eclipse-transformed.jar` | jakarta-transformed | RestyGWT (GWT REST client) |
| `gwt-servlet-jakarta-2.11.0.jar` | jakarta variant | GWT servlet runtime |
| `blast-serialization-1.0-eclipse-transformed.jar` | jakarta-transformed | shared client/server serialization (GWT) |
| `jakarta.servlet.jsp.jstl-api-1.2.7-eclipse-transformed.jar` | jakarta-transformed | JSTL API |
| `taglibs-standard-impl-1.2.5-eclipse-transformed.jar` | jakarta-transformed | JSTL implementation |
| `rescu-2.1.0-eclipse-transformed.jar` | jakarta-transformed | REST client |
| `agr_curation_api.jar` | unpublished internal | Alliance (AGR) curation API client, internal build |
| `bbop.jar` | unpublished legacy | Berkeley BBOP, v1.000 (~2009) |
| `obo.jar` | unpublished legacy | Berkeley OBO parser, v1.000 (~2009) |

## Can these be de-vendored?

Unlike the Jenkins WAR/plugins (which have canonical public download URLs and are
fetched at build time), there is no public URL to pull a transformed or internal
jar from. Removing them from the repo would require one of:

- **Publish to a ZFIN-controlled repository** (Nexus/Artifactory or GitHub
  Packages) and reference by coordinate — works for all nine, adds hosting.
- **Generate the transformed jars at build time** — add a Gradle Eclipse
  Transformer step that pulls the upstream `javax` jar from Maven and transforms
  it during the build. Removes the six transformed jars from git; the three
  internal/legacy jars would still need vendoring.
- **Upgrade to jakarta-native upstream releases** where they now exist (e.g.,
  JSTL 3.0 is jakarta-native on Maven; newer GWT may publish jakarta servlet
  variants). This is a version bump with compatibility risk, not a like-for-like
  swap.

At ~14 MB, and being genuine build dependencies rather than archival content, the
payoff is modest and there is no free "fetch from the web" path — hence they are
kept in-repo for now.

## Adding or updating a jar here

1. Place the jar in this directory.
2. Add its path to `localFileDependencies` in `build.gradle`.
3. If it is a jakarta transformation, keep the `-eclipse-transformed` suffix in
   the filename so its origin is obvious.

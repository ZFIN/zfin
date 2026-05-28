# Deploying Changes — When to Run What

Practical guide for incremental deploys during local development. For
full-pipeline details see [build-and-docker.md](build-and-docker.md).
For the underlying file flow of each Gradle task see
[gradle-deployment.md](gradle-deployment.md).

All commands run inside the `compile` container:

```bash
docker compose -f ~/zfin/docker/docker-compose.yml run --rm compile \
    bash -lc 'cd $SOURCEROOT && <gradle command>'
```

## Quick reference

| You changed... | Run | Why |
|---|---|---|
| GWT client code (`source/org/zfin/gwt/**/*.java`, `*.ui.xml`) | `gradle gwtCompile` | Apache serves the compiled JS directly from `$TARGETROOT/home/gwt/`. No Tomcat redeploy needed. |
| Server-side Java (RPC services, business logic, Hibernate entities) | `gradle dirtycopy` | Copies new `.class` files from `home/WEB-INF/classes/` into `webapps/ROOT/`. Tomcat hot-reloads the context. |
| `home/css/**` or `home/javascript/**` (CSS, SCSS, JS, TSX) | `gradle dirtydeploy` | `npmBuild` rebuilds the webpack bundles **and** `dirtycopy` syncs the new `asset-manifest.json` into Tomcat's classpath. |
| JSPs / tag files | `gradle jspcopy` (or `gradle dirtycopy`) | Tomcat recompiles JSPs on next request. |
| `*.hbm.xml`, `*.properties` under `source/` | `gradle dirtycopy` | These get copied onto the Tomcat classpath. |
| Hibernate config (`conf/hibernate.cfg.xml`), `log4j2.xml` | `gradle dirtycopy` + restart Tomcat | Read once at app startup. |
| Static assets (images, downloads, ZFIN/, zf_info/) | `gradle make` | Copies under `$TARGETROOT/home/`. |

When in doubt, `gradle dirtydeploy` covers everything except the `gradle make`
static-asset path and a full Tomcat restart.

## The asset-manifest gotcha

Webpack writes hashed bundles (e.g., `style.bundle.<hash>.css`) into
`$TARGETROOT/home/dist/`, and a manifest mapping logical names to those
hashed URLs into `$TARGETROOT/home/asset-manifest.json`.

A page request flows like this:

1. Browser → Tomcat (JSP).
2. JSP reads `asset-manifest.json` **from the Tomcat classpath**
   (`webapps/ROOT/WEB-INF/classes/asset-manifest.json`) — *not* from
   `$TARGETROOT/home/`.
3. JSP renders `<link href="/dist/style.bundle.<hash>.css">`.
4. Browser → Apache for `/dist/...` — Apache serves it from
   `$TARGETROOT/home/dist/`.

Two failure modes:

- **Manifests divergent.** TARGETROOT has the new hash, Tomcat's
  classpath copy still has the old one. JSP renders the old URL, Apache
  404s it. Fix: run `gradle dirtycopy` (or `dirtydeploy`) so the
  manifest gets re-copied.
- **`dist/` folder missing entirely** (e.g., volume cleanup wiped it
  but the manifest survived). Every `/dist/*` URL 404s. Fix:
  `gradle dirtydeploy` (or just `gradle npmBuild` to regenerate, then
  `dirtycopy` to sync the manifest).

Symptom either way: the page loads as plain unstyled HTML and the
browser console shows `GET /dist/<...>.css 404`.

## Common pitfalls

### Don't run `npm run build` directly outside the compile container

`webpack.config.js` resolves the output path with
`path.resolve(process.env.TARGETROOT, 'home/dist')`. The compile
container's entrypoint loads `zfin.properties` and exports
`TARGETROOT`. On a host shell `TARGETROOT` is unset, so webpack either
errors (newer Node) or writes the bundles into `<cwd>/home/dist/` —
not where Apache serves from. Use `gradle npmBuild` (which runs npm
inside the compile container and inherits the right env).

### `compileJava` writing classes is not the same as deploying them

`compileJava` writes to `SOURCEROOT/home/WEB-INF/classes/`. Tomcat
loads from `$CATALINA_BASE/webapps/ROOT/WEB-INF/classes/`. Until
`dirtycopy` (or `tomcatDeploy`) copies the class across, Tomcat is
still running the old version.

### Browser cache after `gwtCompile`

GWT emits a content-hashed permutation plus a `<module>.nocache.js`
bootstrap. Browsers usually re-fetch `nocache.js`, but if you don't
see your change, hard-refresh.

### Gradle `UP-TO-DATE` lying about webpack

If `gradle npmBuild` reports `UP-TO-DATE` but you definitely just
changed a CSS/JS file, check that the file's parent dir is in the
task's declared `inputs` (build.gradle, `task npmBuild`). Webpack
reads more than the entry-point dir — anything `require()`d
transitively. Currently declared: `home/javascript`, `home/css`,
`webpack.config.js`, `package.json`, `package-lock.json`, `.babelrc`,
`tsconfig.json`. Add to that list if your change lives outside.

## Verification commands

```bash
# Bundles + manifest exist
ls $TARGETROOT/home/dist/style.bundle.*.css
cat $TARGETROOT/home/asset-manifest.json | grep style.css

# Manifests in sync (should print nothing)
diff $TARGETROOT/home/asset-manifest.json \
     $CATALINA_BASE/webapps/ROOT/WEB-INF/classes/asset-manifest.json

# Server-side class deployed (look for the new method/string in the deployed .class)
javap -p $CATALINA_BASE/webapps/ROOT/WEB-INF/classes/<package>/<Class>.class \
    | grep <newMethodName>

# GWT module compiled
ls $TARGETROOT/home/gwt/<module>/*.nocache.js
```

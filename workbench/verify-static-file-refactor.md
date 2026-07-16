# static-file-refactor — compile-container verification checklist

Run inside the **compile** container. Either open a shell:

```sh
cd docker && docker compose run --rm compile bash -l   # then run the commands below
```

…or prefix each command with `cmprun '<cmd>'` (= `docker compose run --rm compile bash -lc`).
Working dir in the container is `$SOURCEROOT` (the checkout); `$TARGETROOT`,
`$JENKINS_HOME`, and network are all set. Make sure the checkout is on this
branch (`static-file-refactor`) first.

Legend: ✅ = expected pass signal.

---

## 1. Java compiles (UniProt report migration)
Covers `UniProtDiffReportBuilder`, the rewritten `UniProtCompareTask`, and the
`Report`/`ReportWriter` javadoc edits.

```sh
gradle compileJava
```
✅ `BUILD SUCCESSFUL`, no errors in `org/zfin/uniprot/UniProtDiffReportBuilder.java`
or `UniProtCompareTask.java`. (A red flag would be "cannot find symbol" on a
`Report`/`ReportTable`/summary-accessor call.)

## 2. Frontend build (frontend/ extraction + image-set + webp/avif)
Covers the `home/{css,javascript,images} → frontend/` move, the webpack `context`
change, the `webp|avif` asset rule, and the `search-background` image-set.

```sh
gradle npmBuild        # (runs npm ci + npm run build; or: npm ci && npm run build)
```
✅ `BUILD SUCCESSFUL`. The old breakage would surface here as
`Module not found: Can't resolve '../images/search-background...'`.

Then confirm the emitted assets:
```sh
ls "$TARGETROOT/home/dist" | grep -E 'style\.latest\.css|\.latest\.js|search-background'
grep -o 'search-background[^)"]*' "$TARGETROOT/home/dist/style.latest.css" | sort -u
grep -o '#192829' "$TARGETROOT/home/dist/style.latest.css"
```
✅ `style.latest.css`, `vendor-common.latest.js`, `zfin-common.latest.js` exist;
✅ hashed `search-background.<hash>.avif` **and** `.webp` exist under `dist/`;
✅ the CSS references both (image-set) and contains the `#192829` background-color;
✅ **no** `.png` variant of search-background is emitted.

## 3. TypeScript drift test (GenerateTypeScript path repoint)
Covers `GenerateTypeScript.java` writing to `frontend/javascript/react/zirc/api/types.ts`
and its drift test reading the same path.

```sh
gradle test --tests "org.zfin.zirc.dto.GenerateTypeScriptDriftTest"
```
✅ test passes. Optional stronger check — regenerate and confirm no diff:
```sh
gradle generateZircTypes && git status --short frontend/javascript/react/zirc/api/types.ts
```
✅ `git status` shows the file unchanged (generator + committed file agree at the new path).

## 4. Lint + typecheck (eslint / tsconfig path repoint)
```sh
npm run lint
npm run typecheck
```
✅ both pass. Lint runs `eslint frontend/javascript/react/**`; typecheck uses
`tsconfig.typecheck.json` (→ `frontend/javascript/react/zirc`). This also confirms
the `.githooks/pre-commit` path (it runs the same lint).

## 5. Jenkins plugins fetch (manifest download, idempotency, mirror)
Covers `server_apps/jenkins/{plugins.txt,fetch-plugins.sh}` and the rewritten
`deploy-plugins` target.

```sh
# a) fresh fetch of all 78
bash server_apps/jenkins/fetch-plugins.sh server_apps/jenkins/plugins.txt /tmp/pl
# b) idempotent re-run
bash server_apps/jenkins/fetch-plugins.sh server_apps/jenkins/plugins.txt /tmp/pl
```
✅ (a) `Plugins: 78 downloaded, 0 already current (78 total)`;
✅ (b) `Plugins: 0 downloaded, 78 already current (78 total)`.

Optional — mirror override (no network to jenkins.io):
```sh
ZFIN_JENKINS_PLUGIN_MIRROR=file:///tmp/pl-mirror \
  bash server_apps/jenkins/fetch-plugins.sh server_apps/jenkins/plugins.txt /tmp/pl2
```
(after populating `/tmp/pl-mirror` in `<name>/<version>/<name>.hpi` layout)
✅ fetches from the mirror, prints `Plugin source: file:///tmp/pl-mirror`.

End-to-end via Ant (writes into `$JENKINS_HOME/plugins`, also deploys email config):
```sh
ant deploy-plugins
ls "$JENKINS_HOME/plugins"/*.jpi | wc -l
```
✅ `ant` succeeds; ✅ 78 `.jpi` present in `$JENKINS_HOME/plugins`.

## 6. UniProt compare report renders (needs two .dat files)
```sh
UNIPROT_INPUT_FILE_1=<old>.dat UNIPROT_INPUT_FILE_2=<new>.dat \
  OUTPUT_FILE=/tmp/uniprot-diff.json gradle uniprotCompareTask
ls -la /tmp/uniprot-diff.report.html
```
✅ `/tmp/uniprot-diff.json` (unchanged behavior) **and** `/tmp/uniprot-diff.report.html`
are produced; open the HTML and confirm it renders as a standard ZFIN report with
**Summary / Added sequences / Removed sequences / Changed sequences** sections
(not the old JSON_GOES_HERE viewer). If two dat files aren't handy, at minimum
step 1 proves the code compiles.

## 7. Static-content deploy task (needs the static_data volume + network)
Covers `home;static;deployFromRelease` (dedicated volume + docroot symlinks).

```sh
gradle 'home;static;deployFromRelease'
ls -la "$TARGETROOT/home/zf_info" "$TARGETROOT/home/images"   # expect symlinks -> /opt/zfin/static/...
cat /opt/zfin/static/.zfin-static-version
```
✅ downloads pinned `zfinStaticVersion` (currently v1.0.0), verifies sha256,
extracts into `/opt/zfin/static`, and `home/{zf_info,images,ZFIN,...}` are
**symlinks** into it. Re-running prints "already ... skipping download."
NOTE: to ship the no-JS nav you must first cut zfin-static **v1.1.0** and bump
`zfinStaticVersion` in build.gradle.

## 8. Sitemap repoint (reads the deployed tree)
Run after step 7 so `$TARGETROOT/home/zf_info` exists (a symlink; `listFiles`
follows it).

```sh
gradle generateSitemap    # or the GenerateSitemapTask entrypoint used in the release
grep -c 'zf_info/' "$TARGETROOT/home/sitemaps/"*.xml
```
✅ the zf_info URLs are enumerated from the deployed tree (~388), not from the
deleted source `static/zf_info`.

## 9. Full integration — gradle make
```sh
gradle make
```
✅ `BUILD SUCCESSFUL`; the task list runs `home;static;deployFromRelease` (no
`home;{root,images,zf_info,ZFIN};deployFiles`), and there are no references to the
removed `static/` tree or the removed committed Jenkins `.jpi`.

---

## Notes
- The pre-commit hook runs `npm run lint`; it needs `node_modules`, so host-side
  commits used `--no-verify`. In compile (after `npm ci`) the hook works normally.
- Steps 1–5 are the core correctness checks and need no external inputs. 6 needs
  dat files; 7–9 need the volumes/network/release and are closer to a real deploy.

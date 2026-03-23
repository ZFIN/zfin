# Gradle Deployment Tasks — File Flow Schematic

## File Locations

```
SOURCEROOT
├── source/**/*.java          (Java source)
├── source/**/*.hbm.xml       (Hibernate mappings)
├── source/**/*.properties    (resource bundles)
├── home/                     (JSPs, web.xml, configs)
│   ├── WEB-INF/classes/      ← compileJava output goes here
│   └── WEB-INF/lib/*.jar     (legacy jars)
├── conf/                     (hibernate.cfg.xml, log4j2.xml)
├── home/javascript/react/    (React/webpack source)
├── server_apps/              (Perl, SQL, shell scripts, etc.)
└── cgi-bin/

TARGETROOT
├── home/dist/                ← npmBuild output goes here
├── home/gwt/                 ← gwtCompile output goes here
├── home/asset-manifest.json  (webpack manifest)
├── server_apps/              ← gradle make copies these here
└── cgi-bin/                  ← gradle make copies these here

Gradle runtimeClasspath       (dependency jars)
```

## Deploy Target

`$CATALINA_BASE/webapps/ROOT`

```
**/*.jsp, WEB-INF/web.xml, etc.    ← from SOURCEROOT/home/
WEB-INF/classes/*.class             ← from SOURCEROOT/home/
WEB-INF/classes/*.hbm.xml           ← from SOURCEROOT/source/
WEB-INF/classes/*.properties        ← from SOURCEROOT/source/
WEB-INF/classes/hibernate.cfg.xml   ← from SOURCEROOT/conf/
WEB-INF/classes/log4j2.xml          ← from SOURCEROOT/conf/
WEB-INF/classes/asset-manifest.json ← from TARGETROOT/home/
WEB-INF/lib/*.jar                   ← from SOURCEROOT + Gradle
```

## Tasks

### `gradle make` (default task — deploys scripts + DB functions)

```
SOURCEROOT/server_apps/  ──→ TARGETROOT/server_apps/
SOURCEROOT/cgi-bin/      ──→ TARGETROOT/cgi-bin/
SOURCEROOT/home/images/  ──→ TARGETROOT/home/images/
SOURCEROOT/home/zf_info/ ──→ TARGETROOT/home/zf_info/
SOURCEROOT/home/ZFIN/    ──→ TARGETROOT/home/ZFIN/
+ deploys Postgres functions/triggers
+ deploys git info to DB
```

### `gradle watch --continuous` (fastest — JSPs + classes + configs)

```
compileJava:  source/*.java ──→ SOURCEROOT/home/WEB-INF/classes/
copyWebAppFiles:
    SOURCEROOT/home/       ──→ webapps/ROOT/        (JSPs, web.xml)
    SOURCEROOT/conf/       ──→ webapps/ROOT/WEB-INF/classes/
    SOURCEROOT/source/     ──→ webapps/ROOT/WEB-INF/classes/
                                (*.hbm.xml, *.properties only)
    TARGETROOT/home/       ──→ webapps/ROOT/WEB-INF/classes/
                                (asset-manifest.json only)
```

Watches: `home/`, `conf/`, `source/**/*.hbm.xml`, `source/**/*.properties`

### `gradle dirtycopy` (same as watch, but runs once)

Same as `watch`, single execution.

### `gradle dirtydeploy` (+ frontend assets)

```
npmBuild:  home/javascript/react/ ──→ TARGETROOT/home/dist/
+ dirtycopy (everything above)
```

### `gradle deploy` (full clean build — everything)

```
compileJava:       source/*.java ──→ SOURCEROOT/home/WEB-INF/classes/
gwtCompile:        source/org/zfin/gwt/ ──→ TARGETROOT/home/gwt/
npmInstall:        package.json ──→ node_modules/
npmBuild:          home/javascript/react/ ──→ TARGETROOT/home/dist/
deployGitInfoFile: git rev-parse ──→ zdb_property table (DB)
tomcatDeploy:      (clean — deletes + recreates webapps/ROOT/)
    copyWebAppFiles (same as dirtycopy)
    + SOURCEROOT/home/WEB-INF/lib/ ──→ webapps/ROOT/WEB-INF/lib/
    + runtimeClasspath jars        ──→ webapps/ROOT/WEB-INF/lib/
```

## Task Dependency Graph

```
deploy
├── compileJava
├── gwtCompile ──→ compileJava
├── npmInstall
├── npmBuild ──→ npmInstall
├── deployGitInfoFile
└── tomcatDeploy ──→ [gwtCompile, npmBuild, deployGitInfoFile]

dirtydeploy
├── npmBuild
└── dirtycopy ──→ compileJava

watch --continuous
└── dirtycopy ──→ compileJava

make (default)
├── deployPostgresFunctions
├── deployPostgresTriggers
├── deployGitInfoFile
└── *;deployFiles (SimpleDirectoryCopyTask: SOURCEROOT/ ──→ TARGETROOT/)
```

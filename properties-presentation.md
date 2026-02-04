---
marp: true
theme: default
paginate: true
backgroundColor: #fff
---

# ZFIN Properties: Past, Present, and Future

## Simplifying Configuration for the Docker Era

---

# Agenda

1. **The Current System** - How properties work today
2. **The Problems** - Pain points and complexity
3. **Proposed New Approach** - One YAML to rule them all
4. **Docker Changes Everything** - Why this is the right time
5. **Migration Path** - How we get there

---

# Part 1: The Current System

---

# Property File Hierarchy

Starting from an instance file (e.g., `cell.properties`):

```properties
INSTANCE=cell
DEFAULT_EMAIL=rtaylor@zfin.org

include=all-defaults.properties
include=docker-defaults.properties
```

The `include` directive pulls in other property files...

---

# The Include Chain

`all-defaults.properties` includes:

```properties
include=makefile-default.properties
include=blast-default.properties
include=wiki-default.properties
include=indexer-default.properties
include=tt-defaults.properties
include=java-default.properties
include=zfinproperties-default.properties
include=site-switchable-default.properties
include=postgres.properties
```

Each of these may include more files...

---

# Variable Substitution

Properties can reference other properties:

```properties
# In java-default.properties
ONTOLOGY_LOADER_EMAIL=${DEFAULT_EMAIL}
```

**Question:** Where does `DEFAULT_EMAIL` come from?
**Answer:** `DEFAULT_EMAIL` is defined in the instance file (e.g., `cell.properties`)

But this isn't obvious! You have to trace through include chains to figure it out.

---

# Generated Output Files

The `ant rebuildProperties` task generates multiple formats:

| Input | Output Files |
|-------|-------------|
| `coral.properties` | `coral.env` (tcsh) |
| | `coral.bash` (bash) |
| | `coral.tt` (template toolkit) |
| | `allfilter.xml` (ant) |
| | `coral-flat.properties` (test) |

---

# The Build Process

```xml
<target name="rebuildProperties"
        depends="createEnum,createAllPropertiesFiles,exportAntTokenFilters">
</target>
```

Steps:
1. **createEnum** - Generate `ZfinPropertiesEnum.java` from properties
2. **createAllPropertiesFiles** - For every instance in `instances.properties`
3. **exportAntTokenFilters** - Generate `allfilter.xml`

---

# Part 2: The Problems

---

# Problem 1: Too Many Files

For **each instance**, we maintain:
- `{instance}.properties` - source
- `{instance}.env` - generated tcsh
- `{instance}.bash` - generated bash
- `{instance}.tt` - generated template toolkit
- `{instance}-flat.properties` - test verification

Multiply by ~15 instances = **75+ files**

---

# Problem 2: Generated Files in Git

All generated files are committed to Git:
- Creates noise in pull requests
- Merge conflicts on unrelated changes
- Confusion about what to edit

**Generated files should not be in version control!**

---

# Problem 3: Redundancy Across Instances

Non-production instances differ only in:
- `INSTANCE`
- `DEFAULT_EMAIL`
- `PRIMARY_COLOR`
- `MACHINE_NAME` / `HOSTNAME`

Yet each has its own full property file...

---

# Problem 4: Multiple Export Formats

Why do we need:
- `.env` (tcsh)
- `.bash` (bash)
- `.tt` (template toolkit)

Modern applications use **one format** that all consumers can read.

---

# Problem 5: Unclear Override Precedence

When `ANT_HOME` is defined in both:
- `java-docker.properties`
- `java-default.properties`

Which wins? Depends on include order...

---

# Part 3: Proposed New Approach

---

# One YAML to Rule Them All

[all-properties.yml](https://github.com/rtaylorzfin/zfin/blob/properties-ectomy/commons/env/all-properties.yml) - Single source of truth

```yaml
variables:
  db_name: "zfindb"
  base_path: "/opt/zfin"
  catalina_base: "${base_path}/catalina_bases/zfin.org"

common_properties:
  DBNAME: "${db_name}"
  CATALINA_BASE: "${catalina_base}"
  # ... all shared properties
```

---

# Clear Section Structure

```yaml
# Resolution Order (later overrides earlier):
1. common_properties      # Base for all instances
2. deprecated_properties  # Phase-out tracking
3. unique_properties      # Per-instance defaults
4. emails                 # Email addresses
5. email_overrides        # Dev email redirection
6. instance_environment   # Maps instance -> environment
7. defaults_by_environment # dev/staging/prod defaults
8. instance_overrides     # Final instance-specific values
```

---

# Environment-Based Defaults

```yaml
instance_environment:
  franklin: "production"
  test: "staging"
  trunk: "staging"
  schlapp: "development"
  # Unlisted -> "development"

defaults_by_environment:
  development: "dev_defaults"
  staging: "staging_defaults"
  production: "prod_defaults"
```

---

# Production Defaults

```yaml
prod_defaults:
  BLAST_CACHE_AT_STARTUP: "true"
  GA4_ANALYTICS_ID: "G-R5XJW0QW0Y"
  LOG4J_FILE: "production-site.log4j.xml"
  NCBI_LINKOUT_UPLOAD: "true"
  NODE_ENV: "production"
  PRIMARY_COLOR: "#008080"
  SEND_AUTHOR_NOTIF_EMAIL: "true"
  SMTP_HOST: "smtp.uoregon.edu"
  SOLR_CREATE_BACKUPS: "true"
```

---

# Development Defaults

```yaml
dev_defaults:
  BLAST_CACHE_AT_STARTUP: "false"
  GA4_ANALYTICS_ID: "G-5J7RMKMBWC"
  NCBI_LINKOUT_UPLOAD: "false"
  NODE_ENV: "development"
  SCHEDULE_TRIGGER_FILES: ""
  SEND_AUTHOR_NOTIF_EMAIL: "false"
  WIKI_HOST: "devwiki.zfin.org"
```

---

# Instance Overrides (Minimal!)

```yaml
instance_overrides:
  franklin: {}  # Uses prod_defaults as-is

  test:
    PRIMARY_COLOR: "#ffa000"

  trunk:
    PRIMARY_COLOR: "#f57c00"
    SCHEDULE_TRIGGER_FILES: "${env.INSTANCE}"

  schlapp:
    PRIMARY_COLOR: "#1976d2"
```

---

# Email Overrides for Safety

```yaml
emails:
  BUILD_EMAIL: "technical@zfin.org"
  CURATORS_AT_ZFIN: "curators@zfin.org"
  # ... production emails

email_overrides:
  cell: "rtaylor@zfin.org"
  schlapp: "cmpich@zfin.org"
  trunk: "informix@zfin.org"
  test: "informix@zfin.org"
```

Dev instances never accidentally email real users!

---

# Simple Groovy Processor

```bash
INSTANCE=trunk groovy PropertiesProcessor.groovy -o zfin.properties
```

Output: One flat `.properties` file

```properties
DBNAME="zfindb"
SMTP_HOST="smtp.uoregon.edu"
BUILD_EMAIL="technical@zfin.org"
PRIMARY_COLOR="#f57c00"
```

Works with Bash, Java, Perl, Spring - everything!

---

# Universal Shell Loader

**Old way:** Generate `{instance}.bash` for each instance

```bash
# coral.bash (generated, instance-specific)
export INSTANCE="coral"
export DBNAME="zfindb"
export PRIMARY_COLOR="#512da8"
# ... 200+ more exports
```

**New way:** One universal `load-properties.bash`

```bash
# load-properties.bash (universal)
source zfin.properties
```

---

# Single Source of Truth

The `load-properties.bash` script:

- Reads from `zfin.properties` at runtime
- Works for **any** instance
- No generation step needed
- Properties file is the only source

```bash
# In any script that needs env vars:
source /path/to/load-properties.bash

echo $INSTANCE    # Works!
echo $DBNAME      # Works!
```

---

# Part 4: Docker Changes Everything

---

# The Old World: VM-Specific Paths

Each VM had unique paths:

```properties
# On cell (bent.zfin.org)
CATALINA_BASE=/opt/zfin/bent/catalina_bases/cell.zfin.org
ROOT_PATH=/opt/zfin/bent/www_homes/cell.zfin.org

# On trunk (crick.zfin.org)
CATALINA_BASE=/opt/zfin/crick/catalina_bases/trunk.zfin.org
ROOT_PATH=/opt/zfin/crick/www_homes/trunk.zfin.org
```

---

# The New World: Docker Containers

Inside every container, paths are **identical**:

```properties
CATALINA_BASE=/opt/zfin/catalina_bases/zfin.org
ROOT_PATH=/opt/zfin/www_homes/zfin.org
PGDATA=/opt/postgres/data
SOLR_HOME=/var/solr/data/site_index
```

No more `MACHINE_NAME` or `HOSTNAME` variations!

---

# What This Enables

With standardized container paths:

- **No VM-specific path overrides** needed
- **Simpler property files** - just instance identity
- **Predictable deployments** - same paths everywhere
- **Easier debugging** - consistent file locations

---

# Properties We Can Remove

```properties
# These were needed for VM deployments:
MACHINE_NAME=bent        # No longer needed
HOSTNAME=bent.zfin.org   # No longer needed
WEBHOST_HOSTNAME=...     # Derived, remove

# Container paths are always the same:
TARGETROOT=/opt/zfin/www_homes/zfin.org  # Fixed
```

---

# Part 5: Migration Path

---

# Phase 1: Parallel Systems

1. Create `all-properties.yml` with all current values
2. Run Groovy processor to generate properties
3. Compare output with current `-flat.properties`
4. Fix any discrepancies

**Goal: Identical output from new system**

---

# Phase 2: Simplify Instances

1. Remove unnecessary properties (`MACHINE_NAME`, `HOSTNAME`)
2. Update code that references removed properties
3. Consolidate duplicate defaults
4. Remove unused export formats (`.tt`, `.env`)

---

# Phase 3: Clean Up

1. Remove old property files from Git
2. Remove generated files from Git
3. Add generation step to build process
4. Update documentation

---

# Benefits Summary

| Before | After |
|--------|-------|
| ~75+ property files | 1 YAML file |
| Complex include chains | Clear override hierarchy |
| Generated files in Git | Generated at build time |
| VM-specific paths | Container-standard paths |
| Multiple export formats | One `.properties` file |
| Per-instance `.bash` files | Universal `load-properties.bash` |
| Unclear precedence | Documented resolution order |

---

# The Bottom Line

**What we're removing from Git:**
- `{instance}.properties` files (15+ files)
- `{instance}.env` files (tcsh exports)
- `{instance}.bash` files (bash exports)
- `{instance}.tt` files (template toolkit)
- `{instance}-flat.properties` (test files)

---

# The Bottom Line (cont.)

**What we're adding:**

1. **`all-properties.yml`** - Single config source in Git
2. **`PropertiesProcessor.groovy`** - Generates properties at build time
3. **`load-properties.bash`** - Universal env var loader

---

# The New Flow

```
┌─────────────────────┐
│  all-properties.yml │  ← Single source in Git
└──────────┬──────────┘
           │ INSTANCE=trunk groovy PropertiesProcessor.groovy
           ▼
┌─────────────────────┐
│   zfin.properties   │  ← Generated at build/deploy time
└──────────┬──────────┘
           │
     ┌─────┴─────┐──────────────────────────────┐
     ▼           ▼                              ▼
┌─────────┐ ┌──────────────────────┐ ┌──────────────────────┐
│  Java   │ │ load-properties.bash │ │ jenkins, perl, etc.  │
│ Spring  │ │   (sources props)    │ │   (sources props)    │
└─────────┘ └──────────────────────┘ └──────────────────────┘
```

---

# `zfin.properties` = Single Source of Truth

- **Java/Spring**: Reads `zfin.properties` directly
- **Bash scripts**: `source load-properties.bash`
- **Perl scripts**: Parse `zfin.properties`
- **Ant tasks**: Read from properties file



---

# Questions?

---

# Appendix: Resolution Order Detail

```
1. common_properties       # Foundation
2. deprecated_properties   # Backwards compat
3. unique_properties       # Instance defaults
4. emails                  # Production emails
5. email_overrides         # Dev email redirect (replaces all)
6. instance_environment    # Lookup environment type
7. defaults_by_environment # Apply env defaults
8. instance_overrides      # Final tweaks
```

Each step can override previous steps.

---

# Appendix: Variable Interpolation

```yaml
variables:
  base_path: "/opt/zfin"
  catalina_base: "${base_path}/catalina_bases/zfin.org"

common_properties:
  CATALINA_BASE: "${catalina_base}"
  WAR_WEB_INF: "${catalina_base}/webapps/ROOT/WEB-INF"

  # Environment variables:
  GRADLE_HOME: "${env.HOME}/.gradle"
  INSTANCE: "${env.INSTANCE}"
```

---

# Appendix: Validation

The processor validates:

- No duplicate keys across unique-key sections
- All variable references resolve
- Environment mappings are valid
- No circular references

```bash
groovy PropertiesProcessor.groovy --validate
```

---

# Appendix: Current Instance List

```
aws, cell, confused, coral, crick,
embrionix, embryonix, franklin, schlapp,
sumo, test, trunk, zygotix
```

With the new system, most use **dev_defaults** automatically.
Only production-like instances need specific configuration.

# Appendix: Next steps

1. Remove old property files from Git
2. Confirm old .tt and .env files are no longer used
3. What to do with allfilter.xml?
4. Update build scripts to use new Groovy processor

[//]: # (Generate presentation files using Marp CLI:)
[//]: # (marp properties-presentation.md --pdf --pptx)
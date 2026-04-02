# Table Regeneration (Rename-and-Recreate Pattern)

## Overview

ZFIN maintains several **generated tables** — denormalized, pre-computed result sets that power search indexes, detail pages, and data marts. These tables are periodically regenerated from authoritative source tables to reflect the latest curated data.

Regeneration involves replacing the entire contents of a live table with freshly computed data. The naive approach (`TRUNCATE` + `INSERT`) holds exclusive locks for the duration of the data load, which can block readers (e.g., the Lucene indexer, web queries) and cause **deadlocks** under concurrent access.

The **rename-and-recreate** pattern solves this by:

1. Building the new data set in a staging table
2. Renaming the live table to a timestamped `_old_` name (fast DDL, minimal lock)
3. Promoting the staging table to the live name
4. Cleaning up old renamed tables asynchronously

The exclusive lock window is reduced to the two `ALTER TABLE ... RENAME` operations — typically milliseconds — instead of the minutes a full truncate-and-reload would take.

## Terminology

| Term | Meaning |
|------|---------|
| **Live table** | The table that application code queries (e.g., `mutant_fast_search`) |
| **Staging table** | A temporary table (`*_new` or `*_temp`) populated with fresh data before the swap |
| **Old table** | The former live table, renamed with a timestamp suffix after the swap |
| **Regen function** | A PL/pgSQL function (or DO block) that orchestrates the build-and-swap |
| **Cleanup function** | `regen_cleanup_renamed_tables()` — drops old renamed tables after the swap |

## Timestamp Suffix Format

Tables using the full cleanup-compatible pattern are renamed to:

```
<table_name>_old_<YYMMDDHH24MI>_<4-hex>
```

Example: `mutant_fast_search_old_2603251430_a7b2`

The 4-character hex suffix (from `md5(random())`) prevents collisions if two regens run within the same minute.

## Tables Using This Pattern

### Managed by `regen_genox()`

Called from `regen.sh`. Uses timestamped `_old_` naming with cleanup support.

| Live Table | Staging Table | Regen Sub-function | Source Files |
|------------|---------------|-------------------|--------------|
| `mutant_fast_search` | `mutant_fast_search_new` | `regen_genox_finish_marker()` | `lib/DB_functions/Regen_genox/regen_genox_finish_marker.sql` |
| `genotype_figure_fast_search` | `genotype_figure_fast_search_new` | `regen_genofig_finish()` | `lib/DB_functions/Regen_genox/regen_genofig_finish.sql` |

**Purpose**: `mutant_fast_search` maps markers/MOs to fish experiments for phenotype queries. `genotype_figure_fast_search` links genotypes to figures for genotype-based phenotype queries. Together they power the genotype/phenotype search and detail pages.

### Managed by `regen_term()`

Called from `regen.sh`. Uses timestamped `_old_` naming with cleanup support.

| Live Table | Staging Table | Regen Function | Source File |
|------------|---------------|----------------|-------------|
| `all_term_contains` | `all_term_contains_new` | `regen_term()` | `lib/DB_functions/regen_term.sql`, `lib/DB_functions/populate_all_term_contains.sql` |

**Purpose**: Stores the transitive closure of ontology term relationships (is_a, part_of, positively/negatively_regulates, occurs_in). Used for ancestor/descendant queries across all ontologies. Indexes are built separately after the swap via `make_alltermcontains_indexes.sql`.

### Managed by `regen_clean_expression()`

Called from `regen.sh`. Uses timestamped `_old_` naming with cleanup support.

| Live Table | Staging Table | Regen Function | Source Files |
|------------|---------------|----------------|--------------|
| `clean_expression_fast_search` | `clean_expression_fast_search_new` | `regen_clean_expression()` | `lib/DB_functions/regen_clean_expression/regen_clean_expression.sql` |

**Purpose**: Maps genes/MOs to fish experiments for expression data queries. Powers the expression search interface.

### Managed by `refreshPhenotypeMart.sql`

Called from the phenotype mart warehouse refresh (not `regen.sh`). Uses timestamped `_old_` naming with cleanup support. Wrapped in a `DO $$` block rather than a named function.

| Live Table | Staging Table | Source File |
|------------|---------------|-------------|
| `phenotype_source_generated` | `phenotype_source_generated_temp` | `server_apps/DB_maintenance/warehouse/phenotypeMart/refreshPhenotypeMart.sql` |
| `phenotype_observation_generated` | `phenotype_observation_generated_temp` | (same file) |
| `phenotype_generated_curated_mapping` | `phenotype_generated_curated_mapping_temp` | (same file) |

**Purpose**: The phenotype data mart. `phenotype_source_generated` holds source-level facts (genox, figure, stages). `phenotype_observation_generated` holds observation-level facts (entities, quality, relations). `phenotype_generated_curated_mapping` maps generated records to curated source IDs.

**FK dependency order**: These tables have foreign key relationships, so the swap order matters:
1. `phenotype_generated_curated_mapping` (no inbound FKs — renamed first)
2. `phenotype_observation_generated` (has FK to `phenotype_source_generated`)
3. `phenotype_source_generated` (parent — renamed after children are detached)

Each table also has a `*_bkup` table that is populated before the swap as a safety net.

### Using Older Simple Rename (No Timestamp, No Cleanup)

These tables use the rename-and-recreate concept but with a fixed `_old` suffix. The old table is dropped at the start of the next regen rather than cleaned up asynchronously.

| Live Table | Staging Table | Old Name | Regen Function | Source File |
|------------|---------------|----------|----------------|-------------|
| `expression_term_fast_search` | `xpatfs_temp` | `xpatfs_old` | `regen_expression_term_fast_search()` | `lib/DB_functions/regen_expression_term_fast_search.sql` |
| `feature_stats` | `feature_stats_temp` | `feature_stats_old` | `regen_feature_term_fast_search()` | `lib/DB_functions/regen_feature_term_fast_search.sql` |

**Purpose**: `expression_term_fast_search` stores expression results with transitive term ancestry for search indexing. `feature_stats` holds antibody/probe expression annotations for anatomy ontology detail pages.

**Note**: These do not use the `regen_cleanup_renamed_tables()` function. Old tables are dropped at the beginning of the next regen cycle.

### Not Using Rename-and-Recreate

| Table | Pattern Used | Source File |
|-------|-------------|-------------|
| `pheno_term_fast_search` | `DELETE` + `INSERT` (no rename) | `server_apps/DB_maintenance/pheno/pheno_term_regen.sql` |

## Cleanup

### `regen_cleanup_renamed_tables(table_prefix)`

**Location**: `lib/DB_functions/Regen_genox/regen_cleanup_renamed_tables.sql`

Drops all tables whose names start with the given prefix. Has a whitelist of allowed prefixes to prevent accidental deletion:

- `%_fast_search_old_%`
- `all_term_contains_old_%`
- `phenotype_source_generated_old_%`
- `phenotype_observation_generated_old_%`
- `phenotype_generated_curated_mapping_old_%`

**Called from**: `server_apps/DB_maintenance/warehouse/regenPhenotypeMartCleanup.sh`

The cleanup script runs after the regen completes and drops all `_old_` tables for every table group:

```bash
# fast_search tables
clean_expression_fast_search_old_
mutant_fast_search_old_
genotype_figure_fast_search_old_

# term tables
all_term_contains_old_

# phenotype mart tables
phenotype_source_generated_old_
phenotype_observation_generated_old_
phenotype_generated_curated_mapping_old_
```

## Orchestration

### `regen.sh` (Main Regen Script)

**Location**: `server_apps/DB_maintenance/regen.sh`

Run order:

1. `regen_genox()` — mutant_fast_search, genotype_figure_fast_search
2. `regen_anatomy_counts()` — (not a rename-and-recreate table)
3. `regen_term()` — all_term_contains
4. `make_alltermcontains_indexes.sql` — indexes for all_term_contains
5. `regen_expression_term_fast_search()` — expression_term_fast_search
6. `regen_clean_expression()` — clean_expression_fast_search
7. `regen_fish_components()` — (not a rename-and-recreate table)
8. `pheno_term_regen.sql` — pheno_term_fast_search (uses DELETE, not rename)
9. `regenExpressionSearchAnatomy.sql`
10. `VACUUM ANALYZE`

### `regenPhenotypeMartCleanup.sh`

Runs after phenotype mart refresh. Cleans up old renamed tables for all table groups (both regen.sh tables and phenotype mart tables).

## Adding a New Table to This Pattern

1. **Create a regen function** that builds data in a `*_new` or `*_temp` staging table, then renames the live table to `*_old_<timestamp>` and promotes the staging table
2. **Rename indexes and constraints** on the old table to avoid name collisions
3. **Add the prefix** to the whitelist in `regen_cleanup_renamed_tables()`
4. **Add a cleanup call** in `regenPhenotypeMartCleanup.sh`
5. If the table has FK relationships, ensure the swap order respects dependencies (rename children before parents)

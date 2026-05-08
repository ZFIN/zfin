#!/usr/bin/env bash
#
# ZFIN-10241 — Patch 4 runbook
# ---------------------------------------------------------------------------
# Backfill pub_volume / pub_pages for the 1,592 publications that PubMed has
# data for (validated pre-deploy). Runs after the four code/data patches have
# been deployed:
#   - server_apps/data_transfer/PUBMED/fetchPubsFromPubMed.groovy   (Patch 1)
#   - server_apps/data_transfer/PUBMED/updateExistingPubs.sql       (Patch 2)
#   - source/org/zfin/db/postGmakePostloaddb/1182/migrations/ZFIN-10241.sql  (Patch 3)
#   - tests + build.gradle registration
#
# The PMID list lives at:
#   $SOURCEROOT/source/org/zfin/db/postGmakePostloaddb/1182/zfin10241_actionable_pmids.txt
#
# The script:
#   1. Verifies SOURCEROOT / TARGETROOT are set and the deployed loader files
#      are the patched versions (defensive — refuses to run otherwise).
#   2. Captures before-counts of empty pub_volume / pub_pages.
#   3. Runs the loader in update mode against the 1,592 actionable PMIDs in
#      batches of 500 (xargs -n 500).
#   4. Captures after-counts and prints a summary.
#
# Output is tee'd to /tmp/zfin10241_backfill.log (overwritten on each run).
#
# Usage (typical):
#   ./run_zfin10241_backfill.sh
#
# To dry-run (skip the actual loader invocation, only print what would run):
#   DRY_RUN=1 ./run_zfin10241_backfill.sh

set -euo pipefail

LOG=/tmp/zfin10241_backfill.log
PMID_FILE_REL=source/org/zfin/db/postGmakePostloaddb/1182/zfin10241_actionable_pmids.txt
DRY_RUN=${DRY_RUN:-0}

red()   { printf "\033[31m%s\033[0m\n" "$*"; }
green() { printf "\033[32m%s\033[0m\n" "$*"; }
hdr()   { printf "\n=== %s ===\n" "$*"; }

# ---------------------------------------------------------------------------
# 1. Environment + file sanity checks
# ---------------------------------------------------------------------------
hdr "ZFIN-10241 backfill — preflight checks"

: "${SOURCEROOT:?SOURCEROOT must be set (e.g. /opt/zfin/source_roots/zfin.org)}"
: "${TARGETROOT:?TARGETROOT must be set (e.g. /opt/zfin/www_homes/zfin.org)}"

PMID_FILE="$SOURCEROOT/$PMID_FILE_REL"
PUBMED_DIR="$TARGETROOT/server_apps/data_transfer/PUBMED"
WAR_LIB="$TARGETROOT/home/WEB-INF"
CP="$WAR_LIB/classes:$WAR_LIB/lib/*"

[[ -f "$PMID_FILE" ]] || { red "missing PMID list: $PMID_FILE"; exit 1; }
[[ -d "$PUBMED_DIR" ]] || { red "missing PUBMED dir: $PUBMED_DIR"; exit 1; }
[[ -d "$WAR_LIB/classes" ]] || { red "missing WAR classes: $WAR_LIB/classes"; exit 1; }

# Refuse to run unless the deployed loader files are the patched versions.
GROOVY_FILE="$PUBMED_DIR/fetchPubsFromPubMed.groovy"
SQL_FILE="$PUBMED_DIR/updateExistingPubs.sql"

if ! grep -q "Pagination.MedlinePgn.text()" "$GROOVY_FILE"; then
  red "deployed Groovy file is unpatched: $GROOVY_FILE"
  red "ensure Patch 1 has been deployed before running this script"
  exit 1
fi
if ! grep -q "pub_volume = COALESCE(NULLIF(t.volume" "$SQL_FILE"; then
  red "deployed SQL file is unpatched: $SQL_FILE"
  red "ensure Patch 2 has been deployed before running this script"
  exit 1
fi

green "preflight ok"
echo "  SOURCEROOT  = $SOURCEROOT"
echo "  TARGETROOT  = $TARGETROOT"
echo "  PMID list   = $PMID_FILE  ($(wc -l <"$PMID_FILE") lines)"
echo "  log         = $LOG"

# ---------------------------------------------------------------------------
# 2. Before counts
# ---------------------------------------------------------------------------
hdr "before counts"
psql -h "${PGHOST:-db}" -U "${PGUSER:-postgres}" -d "${DB_NAME:-zfindb}" -c "
  SELECT count(*) FILTER (WHERE pub_volume IS NULL OR pub_volume = '') AS missing_vol,
         count(*) FILTER (WHERE pub_pages  IS NULL OR pub_pages  = '') AS missing_pgs
    FROM publication WHERE accession_no IS NOT NULL;"

# ---------------------------------------------------------------------------
# 3. Run the loader (xargs in 500-PMID batches)
# ---------------------------------------------------------------------------
hdr "running loader in update mode"
if [[ "$DRY_RUN" == "1" ]]; then
  echo "DRY_RUN=1 — would run:"
  echo "  cd $PUBMED_DIR && xargs -n 500 -a $PMID_FILE \\"
  echo "    groovy -cp \"$CP\" -DfetchMode=update fetchPubsFromPubMed.groovy"
  echo "(skipping)"
else
  cd "$PUBMED_DIR"
  xargs -n 500 -a "$PMID_FILE" \
    groovy -cp "$CP" -DfetchMode=update fetchPubsFromPubMed.groovy \
    2>&1 | tee "$LOG"
fi

# ---------------------------------------------------------------------------
# 4. After counts + summary
# ---------------------------------------------------------------------------
hdr "after counts"
psql -h "${PGHOST:-db}" -U "${PGUSER:-postgres}" -d "${DB_NAME:-zfindb}" -c "
  SELECT count(*) FILTER (WHERE pub_volume IS NULL OR pub_volume = '') AS missing_vol,
         count(*) FILTER (WHERE pub_pages  IS NULL OR pub_pages  = '') AS missing_pgs
    FROM publication WHERE accession_no IS NOT NULL;"

hdr "actionable subset coverage"
psql -h "${PGHOST:-db}" -U "${PGUSER:-postgres}" -d "${DB_NAME:-zfindb}" <<SQL
CREATE TEMP TABLE _zfin10241_actionable (pmid int);
\copy _zfin10241_actionable FROM '$PMID_FILE'
SELECT
  count(*) AS total_actionable,
  count(*) FILTER (WHERE p.pub_volume IS NOT NULL AND p.pub_volume <> '') AS now_has_volume,
  count(*) FILTER (WHERE p.pub_pages  IS NOT NULL AND p.pub_pages  <> '') AS now_has_pages,
  count(*) FILTER (WHERE (p.pub_volume IS NULL OR p.pub_volume = '')
                     AND (p.pub_pages  IS NULL OR p.pub_pages  = '')) AS still_empty_both
FROM publication p JOIN _zfin10241_actionable a ON a.pmid = p.accession_no;
SQL

green "done"

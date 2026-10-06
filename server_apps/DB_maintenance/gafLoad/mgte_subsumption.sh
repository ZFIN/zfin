#!/bin/bash
#
# Classify the (gene, GO) pairs lost across a cutover diff (ZFIN-10464).
#
#   mgte_subsumption.sh <outdir>
#
# Reads  <outdir>/mgte_{before,after}_ALL.csv   -- written by mgte_snapshot.sh --all
# Writes <outdir>/mgte_subsumption.xlsx         -- sheets: true_loss / subsumed / specificity_lost /
#                                                  by_bucket / by_org / by_org_source
#
# The last three are the top-line numbers for a loss report -- pairs by bucket, true-loss pairs
# by owning organization (Noctua, FP Inferences, ...), and true-loss pairs by organization AND
# source publication (e.g. UniProt's kw2go vs interpro2go vs ec2go) -- computed once here instead
# of hand-pivoted from the detail sheets on every run.
#
# Env: PGHOST, DBNAME, SOURCEROOT.
#
# Run AFTER mgte_csvdiff.sh. Of the pairs that disappeared, which does ZFIN still cover by another
# term on the same gene? The diff cannot say -- it has no ontology awareness.
#
# Needs the --all snapshot pair, not the per-org files: a pair that moved organization is not
# lost, and only the ALL view can see that.
set -euo pipefail

OUT="${1:?usage: mgte_subsumption.sh <outdir>}"
: "${SOURCEROOT:?SOURCEROOT must be set}"
SQL="$SOURCEROOT/server_apps/DB_maintenance/gafLoad"

for f in mgte_before_ALL.csv mgte_after_ALL.csv; do
    [ -s "$OUT/$f" ] || {
        echo "mgte_subsumption.sh: missing or empty $OUT/$f" >&2
        echo "  mgte_snapshot.sh must have been run with --all for both phases." >&2
        exit 1
    }
done

# cd rather than pass paths: \copy does not interpolate psql variables.
cd "$OUT"
psql -v ON_ERROR_STOP=1 -h "$PGHOST" -d "$DBNAME" -f "$SQL/mgte_subsumption.sql"

cd "$SOURCEROOT"
gradle csv2xlsx --args="$OUT/mgte_subsumption.xlsx \
    $OUT/mgte_subsumption_true_loss.csv \
    $OUT/mgte_subsumption_subsumed.csv \
    $OUT/mgte_subsumption_specificity_lost.csv \
    $OUT/mgte_subsumption_by_bucket.csv \
    $OUT/mgte_subsumption_by_org.csv \
    $OUT/mgte_subsumption_by_org_source.csv"
# Sheet order mirrors this list: detail sheets first, then the three summary sheets -- bucket
# totals, true loss by org (Noctua, FP Inferences, ...), true loss by org AND source publication
# (e.g. UniProt's kw2go vs interpro2go vs ec2go). The org/source split exists because neither
# axis alone answers both kinds of question asked of a cutover loss report: org is the load's own
# removal-scoping unit, source is the finer-grained stream within an org that org-grouping alone
# would hide.

# The workbook is the artifact; the CSVs were only its input. Mirrors CSVDIFF_XLSX_ONLY.
rm -f "$OUT"/mgte_subsumption_true_loss.csv \
      "$OUT"/mgte_subsumption_subsumed.csv \
      "$OUT"/mgte_subsumption_specificity_lost.csv \
      "$OUT"/mgte_subsumption_by_bucket.csv \
      "$OUT"/mgte_subsumption_by_org.csv \
      "$OUT"/mgte_subsumption_by_org_source.csv

echo "wrote $OUT/mgte_subsumption.xlsx"

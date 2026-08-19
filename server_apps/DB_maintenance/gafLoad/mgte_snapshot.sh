#!/bin/bash
#
# Snapshot marker_go_term_evidence for one or more GAF organizations into CSVs, for the
# before/after DB diff the GO load jobs produce (ZFIN-8948).
#
#   mgte_snapshot.sh <before|after> <outdir> [--others] <org>...
#
# Writes <outdir>/mgte_<phase>_<TAG>.csv per organization, where TAG is the org name with
# spaces replaced by underscores ("FP Inferences" -> FP_Inferences).
#
# --others
#     Additionally write mgte_<phase>_OTHER.csv holding every row whose organization is NOT one
#     of those named. It should always be empty; if it is not, some org is being written that
#     nobody is watching, and its rows would otherwise be absent from the diff entirely. Cheap
#     insurance -- naming organizations explicitly is what hid PAINT until it was added by hand.
#
# Env: PGHOST, DBNAME, SOURCEROOT -- as provided by the Jenkins job environment.
#
# >>> IMPORTANT <<<  <outdir> must NOT be the load's own report directory
# (.../gafLoad/<jobName>). GafLoadJob calls clearReportDirectory() as its first action, a
# full FileUtils.deleteDirectory() on that path, so a BEFORE snapshot written there is
# destroyed before the load even downloads and the AFTER step then fails with
# "mgte_before_GOA.csv (No such file or directory)". The jobs use a sibling
# <jobName>-dbdiff directory.
#
set -euo pipefail

PHASE="${1:?usage: mgte_snapshot.sh <before|after> <outdir> [--others] <org>...}"
OUT="${2:?outdir required}"
shift 2

case "$PHASE" in
    before|after) ;;
    *) echo "phase must be 'before' or 'after', got '$PHASE'" >&2; exit 1 ;;
esac

OTHERS=false
ARGS=()
for a in "$@"; do
    if [ "$a" = "--others" ]; then OTHERS=true; else ARGS+=("$a"); fi
done
set -- "${ARGS[@]}"

[ "$#" -ge 1 ] || { echo "at least one organization required" >&2; exit 1; }

SQL="$(cd "$(dirname "$0")" && pwd)"
PSQL=(psql -v ON_ERROR_STOP=1 -h "${PGHOST:?PGHOST must be set}" -d "${DBNAME:?DBNAME must be set}")
mkdir -p "$OUT"

for ORG in "$@"; do
    TAG="$(echo "$ORG" | tr ' ' '_')"
    STAGE="tmp_mgte_${PHASE}_${TAG}"
    CSV="$OUT/mgte_${PHASE}_${TAG}.csv"

    "${PSQL[@]}" -v org="$ORG" -v stage="$STAGE" -f "$SQL/snapshot_mgte.sql"
    "${PSQL[@]}" -c "\copy (SELECT * FROM $STAGE ORDER BY zdb_id) TO STDOUT CSV HEADER" > "$CSV"
    "${PSQL[@]}" -c "DROP TABLE IF EXISTS $STAGE"

    echo "${PHASE^^} $ORG rows: $(( $(wc -l < "$CSV") - 1 ))"
done

if [ "$OTHERS" = true ]; then
    KNOWN="$(printf '%s|' "$@")"; KNOWN="${KNOWN%|}"
    STAGE="tmp_mgte_${PHASE}_OTHER"
    CSV="$OUT/mgte_${PHASE}_OTHER.csv"

    "${PSQL[@]}" -v org_others=true -v known_orgs="$KNOWN" -v stage="$STAGE" \
                 -f "$SQL/snapshot_mgte.sql"
    "${PSQL[@]}" -c "\copy (SELECT * FROM $STAGE ORDER BY zdb_id) TO STDOUT CSV HEADER" > "$CSV"
    "${PSQL[@]}" -c "DROP TABLE IF EXISTS $STAGE"

    N=$(( $(wc -l < "$CSV") - 1 ))
    echo "${PHASE^^} (everything else) rows: $N"
    [ "$N" -eq 0 ] || echo "  WARNING: $N row(s) belong to an organization not named above" >&2
fi

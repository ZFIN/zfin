#!/bin/bash
#
# Snapshot marker_go_term_evidence for one or more GAF organizations into CSVs, for the
# before/after DB diff the GO load jobs produce (ZFIN-8948).
#
#   mgte_snapshot.sh <before|after> <outdir> [--others] [--all] <org>...
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
# --all
#     Additionally write mgte_<phase>_ALL.csv holding every row in the table regardless of
#     organization. Answers the question the per-org files structurally cannot: what did ZFIN
#     gain or lose overall? Because the owning org is the FILENAME in per-org mode, a row that
#     moves between organizations reads as a delete in one workbook and an add in another --
#     re-homing phylo GOA -> PAINT produced 39,939 deletes and 39,939 adds of byte-identical
#     rows, and only cancelling the two files by hand shows that nothing was lost. Here the org
#     is a column, so the move shows up as an update instead. Diff it with mgte_csvdiff.sh --all,
#     which pairs this file with a deliberately coarser key.
#
# Both flags are additive: the per-org files are still written and are still the per-org
# accounting. --all is a sixth view, not a replacement.
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

PHASE="${1:?usage: mgte_snapshot.sh <before|after> <outdir> [--others] [--all] <org>...}"
OUT="${2:?outdir required}"
shift 2

case "$PHASE" in
    before|after) ;;
    *) echo "phase must be 'before' or 'after', got '$PHASE'" >&2; exit 1 ;;
esac

OTHERS=false
ALL=false
ARGS=()
for a in "$@"; do
    case "$a" in
        --others) OTHERS=true ;;
        --all)    ALL=true ;;
        *)        ARGS+=("$a") ;;
    esac
done
set -- "${ARGS[@]}"

[ "$#" -ge 1 ] || { echo "at least one organization required" >&2; exit 1; }

SQL="$(cd "$(dirname "$0")" && pwd)"
PSQL=(psql -v ON_ERROR_STOP=1 -h "${PGHOST:?PGHOST must be set}" -d "${DBNAME:?DBNAME must be set}")
mkdir -p "$OUT"

# Build one snapshot CSV. $1 is the TAG (which names the file and the staging table); the rest
# are the -v selection arguments handed to snapshot_mgte.sql. Sets SNAP_ROWS to the row count --
# an out-variable rather than an echo so psql's own output keeps going to the job log instead of
# being swallowed by a command substitution.
SNAP_ROWS=0
snapshot() {
    local TAG="$1"; shift
    local STAGE="tmp_mgte_${PHASE}_${TAG}"
    local CSV="$OUT/mgte_${PHASE}_${TAG}.csv"

    "${PSQL[@]}" "$@" -v stage="$STAGE" -f "$SQL/snapshot_mgte.sql"
    "${PSQL[@]}" -c "\copy (SELECT * FROM $STAGE ORDER BY zdb_id) TO STDOUT CSV HEADER" > "$CSV"
    "${PSQL[@]}" -c "DROP TABLE IF EXISTS $STAGE"

    SNAP_ROWS=$(( $(wc -l < "$CSV") - 1 ))
}

for ORG in "$@"; do
    snapshot "$(echo "$ORG" | tr ' ' '_')" -v org="$ORG"
    echo "${PHASE^^} $ORG rows: $SNAP_ROWS"
done

if [ "$OTHERS" = true ]; then
    KNOWN="$(printf '%s|' "$@")"; KNOWN="${KNOWN%|}"
    snapshot OTHER -v org_others=true -v known_orgs="$KNOWN"
    echo "${PHASE^^} (everything else) rows: $SNAP_ROWS"
    [ "$SNAP_ROWS" -eq 0 ] || \
        echo "  WARNING: $SNAP_ROWS row(s) belong to an organization not named above" >&2
fi

if [ "$ALL" = true ]; then
    snapshot ALL -v org_all=true
    echo "${PHASE^^} (all organizations) rows: $SNAP_ROWS"
fi

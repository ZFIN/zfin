#!/bin/bash
#
# Row-level (database) diff of the before/after marker_go_term_evidence snapshots written by
# mgte_snapshot.sh, one workbook per GAF organization (ZFIN-8948).
#
#   mgte_csvdiff.sh <outdir> [--others] [--all] <org>...
#
# Reads  <outdir>/mgte_{before,after}_<TAG>.csv
# Writes <outdir>/mgte_dbdiff_<TAG>.xlsx  (sheets: deletes / adds / updated_1 / updated_2)
#
# Env: PGHOST, DBNAME, SOURCEROOT.
#
# The key/ignore lists live HERE, once. They used to be duplicated verbatim in all three GO
# load job configs, which meant any change to the diff semantics was three edits that could
# silently drift apart.
#
#   KEY    -- the identity columns a row is MATCHED on.
#   IGNORE -- zdb_id, because it is recycled on every load: ignoring it puts a row that only
#             got a new id into updates_ignored rather than delete+add. Plus the five derived
#             readable columns, which are functions of ids already in the key; they ride along
#             so the diff sheets are legible without affecting matching.
#
# protein_acc is in NEITHER list, deliberately. It is compared but not matched on, so a UniProt
# isoform reassignment (UniProtKB:E9QI36-1 -> -2) surfaces as an UPDATE rather than as a
# delete+add pair. The alternatives were both worse: in KEY (what this used to do) turned every
# reassignment into a spurious disappearance-and-reappearance, and in IGNORE would have hidden
# reassignment altogether -- if GOA started attaching annotations to the wrong isoform, nobody
# would see it. The column also gets set NULL on insert and backfilled by the cleanup, so it
# churns on its own account.
#
# Taking it out of KEY makes keys non-unique (3,175 colliding groups over 14,037 rows on the
# 2026.07.05.1 GOA snapshot). That is only safe because CSVDiff became multiplicity-aware on
# 2026-08-12; the older implementation silently dropped all but one member of a key group.
#
# --all diffs the combined all-organizations snapshot with a DELIBERATELY COARSER KEY, because it
# answers a different question. The per-org key above asks "is this the same annotation record,
# same provenance?" -- right for per-org accounting, but it makes any change of owner or of
# assigning authority read as a delete plus an add. Across a cutover that is most of the diff and
# almost none of it is loss:
#
#   phylo re-home       GOA -> PAINT        org changes, everything else identical
#   *2go handover       UniProt -> GOA      org AND created_by change (ZFIN -> InterPro/UniProt)
#
# So ALL keys on what makes two rows the same BIOLOGICAL STATEMENT -- gene, term, evidence code,
# qualifier relation -- and leaves org/source/created_by/contributed_by/inferred_from/extensions/
# noctua_model compared-but-not-matched. Both of the above then surface as updates, and `deletes`
# becomes what it should be: statements ZFIN no longer asserts from any source. That is the number
# to sign off at cutover.
#
# This key collapses many more rows onto one key than the per-org key does, so it leans even
# harder on multiplicity-aware CSVDiff. Same caveat as above, more so.
set -euo pipefail

OUT="${1:?usage: mgte_csvdiff.sh <outdir> [--others] <org>...}"
shift

# --others / --all diff the extra snapshots mgte_snapshot.sh writes under the same flags.
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

KEY="marker,term,source,evidence,relation,created_by,contributed_by,inferred_from,annotation_extensions,noctua_model"
IGNORE="zdb_id,gene,go_id,go_term,go_aspect,relation_name"

# The statement-level key used for the ALL workbook only -- see the header.
ALL_KEY="marker,term,evidence,relation"
ALL_IGNORE="zdb_id,gene,go_id,go_term,go_aspect,relation_name"

cd "${SOURCEROOT:?SOURCEROOT must be set}"

# CSVDIFF_XLSX_ONLY -> a single mgte_dbdiff_<TAG>.xlsx workbook per org; the intermediate
# per-sheet CSVs are removed.
export CSVDIFF_XLSX_ONLY=true

diff_pair() {
    local TAG="$1" K="$2" I="$3"
    gradle csvDiff --args="$OUT/mgte_dbdiff_$TAG $OUT/mgte_before_$TAG.csv $OUT/mgte_after_$TAG.csv $K $I"
}

TAGS=()
for ORG in "$@"; do TAGS+=("$(echo "$ORG" | tr ' ' '_')"); done
[ "$OTHERS" = true ] && TAGS+=(OTHER)

for TAG in "${TAGS[@]}"; do
    diff_pair "$TAG" "$KEY" "$IGNORE"
done

# An `if` rather than a `&&` one-liner: as the last statement in the script a false test would
# become the script's exit status, failing the build step whenever --all was not requested.
if [ "$ALL" = true ]; then
    diff_pair ALL "$ALL_KEY" "$ALL_IGNORE"
fi

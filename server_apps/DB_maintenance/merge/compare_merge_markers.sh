#!/usr/bin/env bash
#
# Differential equivalence test: prove the Java port (org.zfin.marker.MergeMarkersCommandLine,
# i.e. `zfin-util merge-markers`) produces the same database effect as the legacy Perl
# cgi-bin/merge_markers.pl, over many marker pairs.
#
# Strategy (runs on the host, orchestrates the containers):
#   1. Clone the live `zfindb` once into a frozen seed DB (fast CREATE DATABASE ... TEMPLATE).
#   2. Pick N gene pairs (each gene used once) from the seed.
#   3. JAVA run:  reset a test DB from the seed; merge every pair with `zfin-util merge-markers`;
#                 derive the set of touched tables from the tool's own SQL log; fingerprint them.
#   4. PERL run:  reset the test DB from the seed; merge the same pairs (same order) with a headless
#                 form of merge_markers.pl; fingerprint the same tables.
#   5. Compare per-pair success/failure and per-table content fingerprints. Dump any mismatching
#      table from both DBs for inspection.
#
# Because both runs start from the byte-identical seed and apply the same pairs in the same order,
# get_id() hands out identical new IDs on both sides; the only expected nondeterminism is wall-clock
# timestamps, so date/time columns are excluded from the fingerprint.
#
# Usage:   server_apps/DB_maintenance/merge/compare_merge_markers.sh [N_PAIRS]
# Env:     COMPOSE=<path to docker-compose.yml>  OUT=<host output dir>  SEED=<seed db>  TEST=<test db>
#
# NOTE: destructive to the TEST/SEED databases (never to zfindb). Requires zfindb to have 0 active
#       connections when the seed is first created (stop Tomcat or run when idle).
set -euo pipefail

N_PAIRS="${1:-20}"
COMPOSE="${COMPOSE:-$HOME/zfin/docker/docker-compose.yml}"
OUT="${OUT:-/tmp/merge_cmp}"
SEED="${SEED:-merge_cmp_seed}"
TEST="${TEST:-merge_cmp_test}"
SRC_DB="${SRC_DB:-zfindb}"

# Bookkeeping tables a merge always touches; always fingerprinted even if not seen in the log.
CORE_TABLES="marker,marker_history,data_alias,zdb_active_data,zdb_replaced_data,record_attribution,db_link"

mkdir -p "$OUT"
log() { printf '\n=== %s ===\n' "$*"; }

dc() { docker compose -f "$COMPOSE" "$@"; }
# psql against an admin/maintenance connection (db container, trust auth as postgres).
dbq() { dc exec -T db psql -U postgres -X -v ON_ERROR_STOP=1 "$@"; }

# ---------------------------------------------------------------------------------------------------
log "1. Ensure seed DB '$SEED' (clone of $SRC_DB)"
if dbq -d postgres -tAc "select 1 from pg_database where datname='$SEED'" | grep -q 1; then
    echo "seed already exists; reusing it"
else
    conns="$(dbq -d postgres -tAc "select count(*) from pg_stat_activity where datname='$SRC_DB'")"
    if [ "$conns" -ne 0 ]; then
        echo "ERROR: $SRC_DB has $conns active connection(s); CREATE DATABASE ... TEMPLATE needs 0." >&2
        echo "       Stop Tomcat/other clients and retry." >&2
        exit 1
    fi
    dbq -d postgres -c "create database $SEED template $SRC_DB"
fi

# ---------------------------------------------------------------------------------------------------
log "2. Select up to $N_PAIRS pairs across marker types"
# Marker types to cover (ZDB-ID infixes). The merge tool requires both markers to be the same type,
# so we pair within each type. Rarer types take their quota first; GENE is listed LAST and absorbs
# whatever the rarer types could not supply, so the run still reaches N_PAIRS total.
TYPES="${TYPES:-ATB MRPHLNO CRISPR TALEN GENEP GENE}"
ntypes="$(echo "$TYPES" | wc -w)"
quota=$(((N_PAIRS + ntypes - 1) / ntypes))   # even-ish split; GENE tops up the remainder

: > "$OUT/pairs.txt"
remaining="$N_PAIRS"
for pfx in $TYPES; do
    [ "$remaining" -le 0 ] && break
    want=$quota
    [ "$pfx" = "GENE" ] && want="$remaining"   # let genes fill whatever rarer types can't
    mapfile -t IDS < <(dbq -d "$SEED" -tAc "
        select mrkr_zdb_id from marker
         where mrkr_zdb_id like 'ZDB-$pfx-%' and mrkr_abbrev is not null
           and mrkr_zdb_id not in (select zrepld_old_zdb_id from zdb_replaced_data)
           and mrkr_zdb_id not in (select zrepld_new_zdb_id from zdb_replaced_data)
         order by mrkr_zdb_id
         limit $((want * 2))")
    cnt=0
    for ((i = 0; i + 1 < ${#IDS[@]} && remaining > 0; i += 2)); do
        printf '%s %s\n' "${IDS[i]}" "${IDS[i + 1]}" >> "$OUT/pairs.txt"
        remaining=$((remaining - 1))
        cnt=$((cnt + 1))
    done
    printf '  %-9s %d pairs\n' "$pfx" "$cnt"
done
echo "wrote $(wc -l < "$OUT/pairs.txt") pairs to $OUT/pairs.txt"

reset_test() {
    dbq -d postgres -c "drop database if exists $TEST" >/dev/null
    dbq -d postgres -c "create database $TEST template $SEED" >/dev/null
}

# ---------------------------------------------------------------------------------------------------
log "3. JAVA run"
reset_test
dc run --rm -e TESTDB="$TEST" -e OUTDIR="$OUT" -v "$OUT:$OUT" compile bash -lc '
    set -e
    cp "$ZFIN_PROPERTIES_PATH" /tmp/test.properties
    sed -i "s/^DB_NAME=.*/DB_NAME=$TESTDB/" /tmp/test.properties
    : > "$OUTDIR/java_results.txt"; : > "$OUTDIR/java.log"
    while read -r DEL INTO; do
        if ZFIN_PROPERTIES_PATH=/tmp/test.properties \
             "$TARGETROOT/utilities/bin/zfin-util" merge-markers "$DEL" "$INTO" --skip-regen >>"$OUTDIR/java.log" 2>&1; then
            echo "OK $DEL $INTO"
        else
            echo "ERR $DEL $INTO"
        fi
    done < "$OUTDIR/pairs.txt" > "$OUTDIR/java_results.txt"
'
# Derive the touched-table set from the Java tool'\''s own SQL log.
TOUCHED="$(grep -oiE '(update|delete from|insert into)[[:space:]]+(public\.)?[a-z_][a-z0-9_]*' "$OUT/java.log" \
    | sed -E 's/.*[[:space:]](public\.)?//' | sort -u | paste -sd, -)"
TABLES="$(printf '%s,%s' "$CORE_TABLES" "$TOUCHED" | tr ',' '\n' | grep . | sort -u | paste -sd, -)"
echo "fingerprinting tables: $TABLES"
fingerprint() { # $1 = output file. The `| grep '|'` drops psql command tags (e.g. CREATE FUNCTION).
    dbq -d "$TEST" -tA <<SQL | grep '|' > "$1"
create or replace function pg_temp.fp(tabs text[]) returns table(tbl text, fp text) language plpgsql as \$f\$
declare r text; cols text; q text;
begin
  foreach r in array tabs loop
    select string_agg(quote_ident(column_name), ',' order by ordinal_position) into cols
      from information_schema.columns
     where table_schema='public' and table_name=r
       and data_type not in ('timestamp without time zone','timestamp with time zone','date',
                              'time without time zone','time with time zone');
    if cols is null then tbl:=r; fp:='(missing-or-only-temporal-cols)'; return next; continue; end if;
    q := format('select coalesce(md5(string_agg(h, '''' order by h)), ''empty'') from (select md5(row(%s)::text) h from public.%I) s', cols, r);
    begin execute q into fp; exception when undefined_table then fp:='(missing)'; end;
    tbl := r; return next;
  end loop;
end \$f\$;
select tbl || '|' || fp from pg_temp.fp(string_to_array('$TABLES', ',')) order by tbl;
SQL
}
fingerprint "$OUT/java_fp.txt"

# ---------------------------------------------------------------------------------------------------
log "4. PERL run"
reset_test
dc run --rm -e TESTDB="$TEST" -e OUTDIR="$OUT" -v "$OUT:$OUT" compile bash -lc '
    set -e
    bash "$SOURCEROOT/server_apps/DB_maintenance/merge/derive_headless_merge_markers.sh" \
        "$SOURCEROOT/cgi-bin/merge_markers.pl" "$TESTDB" db > /tmp/headless_merge.pl
    : > "$OUTDIR/perl_results.txt"; : > "$OUTDIR/perl.log"
    while read -r DEL INTO; do
        if perl /tmp/headless_merge.pl "$DEL" "$INTO" >>"$OUTDIR/perl.log" 2>&1; then
            echo "OK $DEL $INTO"
        else
            echo "ERR $DEL $INTO"
        fi
    done < "$OUTDIR/pairs.txt" > "$OUTDIR/perl_results.txt"
'
fingerprint "$OUT/perl_fp.txt"

# ---------------------------------------------------------------------------------------------------
log "5. Compare"
status=0

if diff -u "$OUT/perl_results.txt" "$OUT/java_results.txt" > "$OUT/results.diff"; then
    echo "per-pair success/failure: IDENTICAL ($(wc -l < "$OUT/pairs.txt") pairs)"
else
    echo "per-pair success/failure: DIFFERS -> $OUT/results.diff"; status=1
fi

if diff -u "$OUT/perl_fp.txt" "$OUT/java_fp.txt" > "$OUT/fingerprint.diff"; then
    echo "table fingerprints: IDENTICAL across $(wc -l < "$OUT/java_fp.txt") tables"
else
    echo "table fingerprints: DIFFER -> $OUT/fingerprint.diff"; status=1
    # Dump each mismatching table from both DBs for inspection.
    mismatch_tables="$(diff "$OUT/perl_fp.txt" "$OUT/java_fp.txt" | grep -oE '[<>] [a-z_][a-z0-9_]*\|' \
        | sed -E 's/[<>] //; s/\|//' | sort -u)"
    for t in $mismatch_tables; do
        echo "  dumping mismatching table: $t"
        dbq -d "$TEST" -c "\\copy (select * from public.$t order by 1) to '$OUT/java.$t.dump'" 2>/dev/null || true
    done
    echo "  (java-side dumps written to $OUT/*.dump; rerun the perl batch to capture its side if needed)"
fi

log "Done (exit $status). Artifacts in $OUT"
exit $status

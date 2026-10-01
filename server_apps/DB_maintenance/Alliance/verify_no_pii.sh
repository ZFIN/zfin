#!/bin/bash
#
# ZFIN-10509 -- the release gate for the Alliance database dump.
#
#   ./verify_no_pii.sh <dump-file>[.gz]
#
# Exits 0 only when the dump carries no email address. Any match is a failure:
# this runs on the artifact that actually leaves the building, after
# scrub_for_alliance.sql has run, so a match means the scrub missed something.
#
# Why this and not just the scrub: addresses live in free-text columns that no
# column name advertises. Scrubbing works from a list of columns somebody wrote
# down; this works from the bytes. When the two disagree, this one is right.
#
# An allowlist is supported for addresses that are legitimately in the data --
# a role address in a controlled vocabulary, say. It is deliberately awkward:
# each entry is an extended regex in a file passed via ALLOWLIST=, and every
# suppression is reported, so a growing allowlist stays visible rather than
# quietly hollowing out the gate.
#
#   ALLOWLIST=alliance-pii-allowlist.txt ./verify_no_pii.sh dump.sql.gz

set -uo pipefail

DUMP="${1:-}"
ALLOWLIST="${ALLOWLIST:-}"
# Matches enough of RFC 5322 to catch anything a human would recognise as an
# address. Over-matching is the safe direction here: a false positive costs a
# look, a false negative ships someone's address to a public download.
EMAIL_RE='[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}'
# How many offending lines to show. Enough to identify the column, not so many
# that a broken scrub floods the console.
SAMPLE=15

if [[ -z "$DUMP" ]]; then
  echo "usage: $0 <dump-file>[.gz]" >&2
  exit 2
fi
if [[ ! -f "$DUMP" ]]; then
  echo "ERROR: no such dump file: $DUMP" >&2
  exit 2
fi

# gzip'd dumps are the norm here (every other Alliance artifact ships .gz), so
# handle both rather than making the caller gunzip a multi-GB file first.
case "$DUMP" in
  *.gz) READ_CMD=(gzip -dc --) ;;
  *)    READ_CMD=(cat --)      ;;
esac

echo "scanning $DUMP for email addresses..."

WORK=$(mktemp -d)
trap 'rm -rf "$WORK"' EXIT
HITS="$WORK/hits"

# -a: a dump with bytea or a stray non-UTF8 byte otherwise looks binary to
# grep, which then reports "binary file matches" and finds nothing useful.
"${READ_CMD[@]}" "$DUMP" | grep -aEn "$EMAIL_RE" > "$HITS"
# grep exits 1 for "no matches", which is the success case here; only a 2 or
# above is a real error worth aborting on.
rc=${PIPESTATUS[1]}
if [[ "$rc" -gt 1 ]]; then
  echo "ERROR: grep failed while reading $DUMP (exit $rc)" >&2
  exit 2
fi

TOTAL=$(wc -l < "$HITS" | tr -d ' ')

SUPPRESSED=0
if [[ -n "$ALLOWLIST" && "$TOTAL" -gt 0 ]]; then
  if [[ ! -f "$ALLOWLIST" ]]; then
    echo "ERROR: ALLOWLIST set but not readable: $ALLOWLIST" >&2
    exit 2
  fi
  # Blank lines and # comments are ignored; a blank line reaching grep -f would
  # match everything and silently disable the gate.
  grep -vE '^[[:space:]]*(#|$)' "$ALLOWLIST" > "$WORK/patterns" || true
  if [[ -s "$WORK/patterns" ]]; then
    grep -avEf "$WORK/patterns" "$HITS" > "$WORK/remaining" || true
    SUPPRESSED=$(( TOTAL - $(wc -l < "$WORK/remaining" | tr -d ' ') ))
    mv "$WORK/remaining" "$HITS"
  fi
fi

REMAINING=$(wc -l < "$HITS" | tr -d ' ')

if [[ "$SUPPRESSED" -gt 0 ]]; then
  echo "NOTE: $SUPPRESSED line(s) suppressed by $ALLOWLIST"
fi

if [[ "$REMAINING" -eq 0 ]]; then
  echo "PASS: no email addresses found in $DUMP"
  exit 0
fi

echo ""
echo "FAIL: $REMAINING line(s) in $DUMP contain an email address."
echo ""
echo "First $SAMPLE, local part masked (the point is to locate the column, not"
echo "to reproduce the addresses in a build log):"
echo ""
# Mask everything before the @ so the console log does not itself become a
# place where the addresses are readable. The domain is left intact because it
# is what identifies which table the leak came from.
head -n "$SAMPLE" "$HITS" | sed -E "s/[A-Za-z0-9._%+-]+@/***@/g" | cut -c1-200 | sed 's/^/  /'
echo ""
echo "Fix the scrub, re-run it, re-dump, and re-run this check. Do not publish"
echo "this file. If a match is genuinely not personal data, add a narrow regex"
echo "for it to an allowlist file and pass it via ALLOWLIST=."
exit 1

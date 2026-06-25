#!/usr/bin/env groovy
// Regenerate the ExternalFileField data for Solr's `popularity` field, sourced
// from Apache access-log hit counts. Run manually on a host with read access
// to /var/log/httpd/zfin_access (production web server) — no automated caller.
//
// Output (stdout): `<id>=<score>` lines, where score = 1.0 + count * 0.01.
// The two grep stages, the sort|uniq -c shell pipeline, and the scoring step
// (previously generatePopularity.sh + getIdListFromApacheLog.sh +
// getScoresFromIdList.groovy) all collapse into this one file.
//
// Deploy flow (unchanged from when this was 3 scripts):
//   1. Run this on a host with /var/log/httpd/zfin_access access; review the
//      diff vs the existing data/external_popularity.txt in this checkout.
//   2. Replace docker/solr/site_index/data/external_popularity.txt with the
//      result.
//   3. Commit, push, and rebuild the solr image (`docker compose build solr`
//      then `up -d --force-recreate solr`).
//
// Usage:
//   ./generatePopularity.groovy [logfile] > external_popularity.txt
//   ./generatePopularity.groovy /var/log/httpd/zfin_access.1 > external_popularity.txt
//
// Default logfile: /var/log/httpd/zfin_access

import java.util.regex.Pattern

def logfile = args ? args[0] : '/var/log/httpd/zfin_access'
def log = new File(logfile)
if (!log.canRead()) {
    System.err.println("cannot read ${logfile}")
    System.exit(1)
}

// Drop internal subnets, monitoring tools, bot UAs, and a few noisy URL
// patterns that are user-visible but not "look at a record" hits.
// Matches the prior getIdListFromApacheLog.sh filter chain. The legacy
// bash regex used `128.223.[56|57]`, which is a character class containing
// `5 6 | 7` (a quiet bug — it happened to also match `5` so 128.223.5X
// addresses outside the office subnet were also dropped). Fixed here:
// require the trailing dot so only the 128.223.56/57 subnets match.
def dropLine = Pattern.compile(
    /128\.223\.(56|57)\.|all-figure-view|fxallfigures|nagios|Googlebot|bingbot|Exabot|check_http/
)

// Request line URLs that probably resolve to "look at a record" pages.
def relevantUrl = Pattern.compile(/detaill|view|ZFIN_jump|^\/ZDB|^\/[A-Z]{2}:/)

// IDs we want to count. ZFIN ZDB-* and Alliance-style PREFIX:NNNN (e.g. GO:0000123).
// ZDB-PERS-* is excluded — author/curator records shouldn't influence search popularity.
def idPattern = Pattern.compile(/ZDB-[A-Z]+-[0-9]+-[0-9]+|[A-Z]{2}:[0-9]+/)
def personPrefix = 'ZDB-PERS'

// Accumulator. LinkedHashMap so output is stable across reruns where two ids
// happen to tie at the same hit count (first-seen wins position).
def counts = new LinkedHashMap<String, Integer>()

log.eachLine { line ->
    if (dropLine.matcher(line).find()) return

    // Apache combined log format: $remote $ident $user [$time] "$method $url $proto"
    // The URL is space-delimited field 7 (1-indexed) — same as `cut -d' ' -f 7`.
    def parts = line.split(' ')
    if (parts.length < 7) return
    def url = parts[6]
    if (!relevantUrl.matcher(url).find()) return

    def m = idPattern.matcher(url)
    while (m.find()) {
        def id = m.group()
        if (id.startsWith(personPrefix)) continue
        counts[id] = (counts[id] ?: 0) + 1
    }
}

// Stable output order: alphabetic by id. The legacy pipeline emitted ids
// in `uniq -c | sort -rn` order (descending by count), but the consumer
// (Solr's ExternalFileField) doesn't care about order — and alphabetic
// diffs more cleanly against the prior external_popularity.txt.
counts.keySet().sort().each { id ->
    println "${id}=${1.0 + counts[id] * 0.01}"
}

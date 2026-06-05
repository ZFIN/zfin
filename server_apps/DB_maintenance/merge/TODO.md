# Pre-PR TODO — `command-line-utility` branch (merge-markers)

**Do not open the PR until the items below are addressed or consciously deferred (with a filed
ticket).** This branch ships `ToolBootstrap`, the `zfin-util` `requiresDatabase` wiring, the
`copyClasspathTemplates` build fix, and `zfin-util merge-markers` (the Java port of
`cgi-bin/merge_markers.pl`, verified equivalent — see `README.md`).

Status legend: `[ ]` not started · `[~]` in progress · `[x]` done.

---

## Must decide before the PR

- [ ] **Plan the Perl retirement (don't delete anything yet).** Find and list every caller of the
      Perl so the cutover is deliberate:
  - `cgi-bin/merge_markers.pl` — called by `merge-marker.jsp` (per its header comment). The JSP must
    be repointed at `zfin-util merge-markers` (or a thin server-side action) before the Perl is
    removed.
  - `server_apps/DB_maintenance/merge/merge_markers_cmdline.pl` — check for cron/Jenkins/manual
    callers.
  - Decide: repoint callers **in this PR**, or land the Java tool now and repoint in a follow-up PR
    (leaving the Perl in place until then). Either is fine — but the PR description must state which,
    and **no Perl is deleted while a caller still points at it.**

- [ ] **Confirm production regen behavior.** The tool runs `regen_genox_marker` by default;
      `--skip-regen` is only for bulk/test use. Interactive single merges (the JSP path) should NOT
      pass `--skip-regen`. Make sure whatever replaces the JSP call omits it. (The minute-granular
      temp-table collision that `--skip-regen` avoids only occurs when many merges run within the
      same minute — see README.)

## Tickets to file (deliberate post-cutover improvements; the port faithfully replicates these)

- [ ] **Wide-constraint conflict resolution.** The merge algorithm only resolves unique/primary-key
      conflicts for keys of **2–4 columns** (faithfully ported from the Perl). Pairs that collide on
      a wider constraint — e.g. `expression_experiment2` (6–7 col unique), `gene_description`,
      `uq_sfclg_unique_location` — currently error and are skipped on **both** implementations (the
      24/100 mutual ERRs in the equivalence run). File a ticket to decide whether to generalize
      conflict handling in the Java going forward. Use the equivalence harness as the safety net for
      any such change.

- [ ] **`mrkr_comments` "\n\n" pollution.** The Java reproduces the Perl bug that writes `"\n\n"`
      into the surviving marker's comment when the deleted marker has no note (loose
      `undef ne 'none'`). File a ticket to fix it deliberately (in the Java) **and** clean up existing
      polluted `mrkr_comments` values. Removing it now would (correctly) fail the equivalence test
      against the Perl, so it must be a conscious post-cutover change.

## Harness / housekeeping

- [ ] **Auto-drop clones at end.** `compare_merge_markers.sh` leaves `merge_cmp_test` (and the
      reusable `merge_cmp_seed`) behind — ~15 GB each. Add an end-of-run cleanup (or a `--keep` flag
      to opt out), and document dropping them. (For now: `psql -U postgres -c 'drop database if
      exists merge_cmp_test'` and likewise `merge_cmp_seed`.)

## PR description should include

- [ ] The equivalence evidence (100 pairs across 6 marker types; identical per-pair OK/ERR; identical
      content fingerprints across 32 tables).
- [ ] The four porting discrepancies the harness caught and how each was reconciled (see README +
      commit `6926b445ec`).
- [ ] The Perl-retirement decision (this PR vs follow-up) and links to the tickets filed above.

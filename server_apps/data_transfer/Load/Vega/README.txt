Vega transcript load — REMOVED (ZFIN-10327)

This directory previously held the full Vega transcript load pipeline
(fetch_vega, novel_check, withdrawn_check, blast_transcripts,
load_transcripts, load_assembly, load_evidence, update_tscript_names,
gff3_backbone, vega_public, ...) — scripts, SQL, and R/awk helpers driven
manually via the legacy `gmake` build, plus a detailed README.help
documenting the Sanger coordination workflow and per-step gotchas.

That build system no longer exists (ZFIN-10113 removed make.include, so
the Makefile could not even parse), and no Jenkins job or other
automation triggers this load. The contents were removed in release 1182.

To consult or restore the original files — including the README.help
narrative — use git history at tag v1181 (the last release that contained
them) or earlier, e.g.:

    git show v1181:server_apps/data_transfer/Load/Vega/README.help
    git checkout v1181 -- server_apps/data_transfer/Load/Vega/

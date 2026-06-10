ReNo / Nomenclature manual load — REMOVED (ZFIN-10327)

This directory (and its Nomenclature/ subdirectory) previously held the
ReNo run-report load and the UniProt/Sanger nomenclature-run scripts
(*.sh, *.sql, *.r) that were driven manually via the legacy `gmake`
build.

That build system no longer exists (ZFIN-10113 removed make.include, so
the Makefiles could not even parse), and no Jenkins job or other
automation triggers this load. The contents were removed in release 1182.

To consult or restore the original files, use git history at tag v1181
(the last release that contained them) or earlier, e.g.:

    git show v1181:server_apps/data_transfer/Load/ReNo/load_run_report_hit.sh
    git checkout v1181 -- server_apps/data_transfer/Load/ReNo/

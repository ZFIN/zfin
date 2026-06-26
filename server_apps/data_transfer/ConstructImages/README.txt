ConstructImages load — REMOVED (ZFIN-10327)

This directory previously held a construct/figure-image load (loadFigures.sql,
images.unl, constructFigure.unl) driven manually via the legacy `gmake` build.

That build system no longer exists (ZFIN-10113 removed make.include), no
Jenkins job triggers this load, and none of the files were referenced
anywhere else in the repository. The contents were removed in release 1182.

To consult or restore the original files, use git history at tag v1181 (the
last release that contained them) or earlier, e.g.:

    git show v1181:server_apps/data_transfer/ConstructImages/loadFigures.sql
    git checkout v1181 -- server_apps/data_transfer/ConstructImages/

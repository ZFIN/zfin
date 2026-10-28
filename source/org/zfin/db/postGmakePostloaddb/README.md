# Retired changelogs and removed load files

Releases up to **1136** are commented out in `db.changelog.master.xml` here, and release **1098** is
commented out in `../load/db.changelog.master.xml`. Every database we run against has already applied
those changesets; Liquibase ignores `DATABASECHANGELOG` rows whose changesets are no longer in the
changelog. A database restored from a dump older than release 1137 can no longer be brought up to date
with these changelogs.

The large data files that only those retired changesets loaded were then removed from the repository
(October 2026). They are still in git history: every release up to the one before this change contains
them, e.g. the tag `archive/release-1185`.

| Path (under `source/org/zfin/db/`) | Loaded by |
|---|---|
| `postGmakePostloaddb/1079/shipwreck/term` | `1079/shipwreck/loadChebi.xml` (already retired) |
| `postGmakePostloaddb/1083/alienMonster/sangerPositions.csv` | `1083/alienMonster/loadChromosome.xml` (already retired) |
| `postGmakePostloaddb/1100/DLOAD-554.csv` | `1100/DLOAD-554.xml` |
| `postGmakePostloaddb/1101/DLOAD-552.csv` | `1101/DLOAD-552.xml` |
| `postGmakePostloaddb/1104/DLOAD-617b.csv` | `1104/DLOAD-617b.xml` |
| `postGmakePostloaddb/1106/DLOAD-621chr.csv`, `DLOAD-621conseq.csv` | `1106/DLOAD-621chr.xml`, `DLOAD-621conseq.xml` |
| `postGmakePostloaddb/1107/DLOAD-627chr.csv`, `DLOAD-627conseq.csv` | `1107/DLOAD-627chr.xml`, `DLOAD-627conseq.xml` |
| `postGmakePostloaddb/1112/flankseq.csv` | `1112/pmflankseq.xml` |
| `postGmakePostloaddb/1133/ZFIN-7812-01-clone-agp-grcz11.csv`, `ZFIN-7812-03-sfclg-agp-rows.csv` | `1133/ZFIN-7812.xml` |
| `postGmakePostloaddb/1136/ZFIN-8191-01-record-attribution-fixes.csv` | `1136/ZFIN-8191.xml` |
| `postGmakePostloaddb/1136/ZFIN-8230-filemaker-export-zebrafish-ish-database-2017.csv` | `1136/ZFIN-8230.xml` |
| `postGmakePostloaddb/1136/ZFIN-8231-filemaker-export-database-plates-march2017.csv` | `1136/ZFIN-8231.xml` |
| `postGmakePostloaddb/1136/ZFIN-8232-filemaker-export-database-screen-april-2017.csv` | `1136/ZFIN-8232.xml` |
| `load/1098/ChickenDance/efs.csv`, `genox.csv`, `xpatex.csv`, `xpatres.csv` | `load/1098/ChickenDance/restore-*.xml` |
| `examples/load/pearl/changelog/allele`, `examples/load/pearl/changelog/transcriptCorrected.csv`, `examples/postGmakePostloaddb/pearl/changelog/transcriptCorrected.csv` | nothing |

## Getting a file back

Restore it from the commit just before it was removed:

    f=source/org/zfin/db/postGmakePostloaddb/1112/flankseq.csv
    git restore --source="$(git log -1 --format=%H --diff-filter=D -- "$f")^" -- "$f"

or from a release tag that still has it, e.g. `git restore --source=archive/release-1185 -- "$f"`.

In history these files are stored in Git LFS. With git-lfs installed (`brew install git-lfs`) the command
above gives the real file. Without it you get a three-line placeholder (`version … / oid sha256:… /
size …`). If GitHub cannot serve the file, see `LFS-ARCHIVE.md` in the NFS archive of the repository.

To re-run one of these changesets, restore its files and move its `<include>` back out of the comment.

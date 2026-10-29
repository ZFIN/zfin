# Hand-off: choosing the genome browser rule (decision D4)

Written 2026-10-24 for whoever continues the ZFIN-10529 follow-up work. The ticket's own requirements are met and are on branch `annotation-status-view`. What is left is one decision, **how a page picks its assembly for the genome browser**, plus the work that follows from it. This file is meant to be enough to make that call without reading the whole history.

**Update 2026-10-28:** option B was explored with a prototype admin page, then reverted. The reworked table, a screenshot and the archived prototype source are in [`page-assembly-table.md`](page-assembly-table.md). The other options have not been ruled out, and the decision record at the end is still blank. That file also records the direction to move the browser data from code into the database.

Read next, in this order if you have the time:

1. [`genome-browser-issues.md`](genome-browser-issues.md): every place a browser appears, 29 production screenshots, 13 numbered issues. This is the evidence.
2. [`../assembly-blast-radius.md`](../assembly-blast-radius.md): the wider brief, with decisions D1 to D10 and their status.

---

## 1. Where things stand

**The branch.** `annotation-status-view`, 15 commits on top of `origin/main`, 54 files, +1,879/−318. Nothing is pushed (the sandbox it was written in cannot push). Every commit is prefixed `ZFIN-10529:`; the ticket number was supplied, the branch name was a placeholder.

| Commit | What it did |
|---|---|
| `085c0aede3` | Genome Resources order on the gene page; `assembly.a_is_current`; `foreign_db.fdb_assembly_id` |
| `350ca56b65` | `load` and `load_publication` tables |
| `4261e2a8fa` | `external_resource.ncbi_not_in_current_release`, refreshed by the NCBI load with a guard against an empty or shrunken fetch |
| `d6234432ec` | View `marker_annotation_status_derived`; dump script keeps the not-in-release rows |
| `a219e81692` | The page, the reports and the snapshots read that view |
| `30d0e5c3e3` | Dropped `marker_annotation_status` |
| `f11fb0c449`, `4e229f9676` | The NCBI load's publication lists come from `load_publication`; the load stops on an unlisted publication |
| `245c6bf06a` | GRCv10 listed as a legacy assembly |
| `5364861c9a` | `load_file`, and `unload_production.sh` driven by it (via `dump_exclude_args.sh`) |
| `333469691f` | Load-Database Jenkins job: "Load the newest dump file" checkbox |
| `83ecf1d0e1` | Migration order fixed (found by a real restore plus Liquibase run); seeds the not-in-release table before the old status table is dropped |
| `ce368c2d50` | Report message wording |
| `43d9a53981`, `cbb1eacf8b` | Feature locations and the mapping detail page take their assembly order from the `assembly` table (`AssemblyOrder`) |

**Separate branch:** `jenkins-cli-pass-stdin` (`fafe87779b`, off `origin/main`) fixes `docker/base/jenkins-cli`, which swallowed standard input and made `jenkins-cli update-job … < file` fail. It was made without a worktree because the sandbox mounts `.git/worktrees` read-only; on a normal checkout run `git worktree add <dir> jenkins-cli-pass-stdin`. It takes effect when the base image is rebuilt.

**What was tested.** A full restore of the October 8 dump (`gradle loaddb`), `gradle liquibasePostBuild`, and a real `gradle ncbiLoadPort` run, 1 hour 16 minutes, exit 0. The load added 5 genes consistently and the annotation status matched the old stored values exactly. About 90 unit and database tests were added or changed and pass. **Not run:** the NCBI load integration tests (`-PncbiLoadTests`), because they empty tables. `MarkerMergeServiceTest` fails on the dev database for lack of fixture data, with or without these changes.

## 2. The decision: how a page picks its assembly

### The problem

Each place that draws a browser chooses its own assembly. Some are tied to the *name* `GRCz12tu`, some to GRCz11, and some look at the data. Details and screenshots are in `genome-browser-issues.md`. The part that matters for the decision:

- **The gene Sequences section is gated on the name `GRCz12tu`.** `GeneViewController` (and `PseudoGeneViewController`) show the browser only if `Marker.getLatestAssembly()` is named `GRCz12tu` and the gene has exactly one landmark location on it. `getLatestAssembly()` returns the lowest `a_order`. GRCz13ab has order 5 and GRCz12tu has 10, so **the first gene that gets a GRCz13ab row loses its Sequences browser.**
- **Other pages choose differently on purpose.** Transcripts (`TranscriptService`) and clones (`CloneViewController`) are fixed on GRCz11. The STR page prefers GRCz12tu and falls back to GRCz11 per STR. The feature page switches on the assembly name of its first location. The standalone `/action/jbrowse2` page is GRCz11 only. The navigation links to an external Amplify browser on GRCz12tu. The search "related data" redirect (`/action/jbrowse/byName`) opens the old JBrowse v1 on GRCz11, at the wrong location and with no tracks (a bug, separate from the decision).
- **Already fixed in this branch:** the mapping detail page now compares assemblies through `AssemblyOrder` (the `assembly` table's `a_order`) and takes the build from the location's own assembly.

### Three facts that shape the options

1. **Tracks differ by assembly.** `GenomeBrowserTrack.trackMap` (code) lists which tracks each assembly has:

   | Assembly | Tracks |
   |---|---|
   | GRCz12tu | Genes, RefSeq, Knockdown reagent |
   | GRCz11 | Genes, Transcripts, ZFIN Features, ZFIN Mutant, Complete clones |
   | GRCz10 | Genes |
   | Zv9 | Genes, ZFIN Mutant |

   This is why some pages stay on GRCz11: GRCz12tu has no Transcripts, ZFIN Features or Complete clones track. "Show the newest assembly" cannot be the rule on its own.
2. **Pages already declare the tracks they want** (`GenomeBrowserTrack.Page`): `GENE_SEQUENCE` (Genes, RefSeq), `GENE_TRANSCRIPTS` (Genes, Transcripts), `MAPPING_DETAIL_GENES` (Genes), `CLONES` (Complete clones, Genes, Transcripts), `GENE_STRS` (Genes, Knockdown reagent, Transcripts), `FEATURE` (Genes, ZFIN Features, Transcripts, ZFIN Mutant).
3. **The front end has its own list.** `frontend/javascript/react/constants/config-jbrowse2.json` holds four assemblies (GRCz12tu, GRCz11, GRCz10, Zv9) and 39 tracks, each naming the assemblies it belongs to. An unknown build silently falls back to the first assembly in the file. GRCz13ab is in none of the three places (the enum `GenomeBrowserBuild`, `trackMap`, the JSON).

### The options

**A. One table of what each assembly offers.**

```sql
create table genome_browser_track (
  gbt_a_pk_id  bigint not null references assembly (a_pk_id),
  gbt_track    text   not null,   -- logical track: GENES, REFSEQ, TRANSCRIPTS ... (GenomeBrowserTrack names)
  gbt_track_id text   not null,   -- id used in the browser URL, e.g. zfin-gene12
  primary key (gbt_a_pk_id, gbt_track)
);
```

Seeded from `trackMap`, 11 rows (GRCz12tu 3, GRCz11 5, GRCz10 1, Zv9 2). **Rule:** a page shows the newest assembly (by `a_order`) on which the object has a landmark and the tracks the page needs are registered. The page-to-tracks lists stay in the `Page` enum.

**B. One table of what each page may show.**

```sql
create table genome_browser_page_assembly (
  gpa_page_type text    not null,   -- GENE_SEQUENCE, GENE_TRANSCRIPTS, CLONES ...
  gpa_a_pk_id   bigint  not null references assembly (a_pk_id),
  gpa_rank      integer not null,   -- 1 is tried first
  gpa_enabled   boolean not null default true,
  primary key (gpa_page_type, gpa_a_pk_id)
);
```

About 15 to 20 rows, for example `GENE_SEQUENCE`: GRCz13ab rank 1 `enabled = false`, GRCz12tu rank 2; `GENE_TRANSCRIPTS` and `CLONES`: GRCz11; `GENE_STRS`: GRCz12tu then GRCz11; `FEATURE`: GRCz12tu, GRCz11, GRCz10, Zv9. **Rule:** try the page's assemblies in rank order and show the first enabled one on which the object has a landmark. `trackMap` is not replaced; it stays in code, or needs a second table.

**C. One flag per assembly.** `assembly.a_has_browser`, and the Sequences name check becomes "best assembly that has a browser". Fixes the Sequences gate only; the fixed-GRCz11 pages and `trackMap` stay as they are.

**A plus a switch (middle path).** Table A, plus one `enabled` column on `assembly`. Registering tracks says what is possible; the flag lets an admin turn an assembly on or off for every page at once. This gives B's rollout lever for one column.

### Comparison

| | A | B | C | A + switch |
|---|---|---|---|---|
| New rows to maintain | 11 | 15 to 20 | 1 column | 11 + 1 column |
| Replaces `trackMap` | Yes | No | No | Yes |
| Staging GRCz13ab | Nothing changes until its tracks are registered | Add rows disabled, then flip each | Flip one flag | Register tracks, then flip one flag |
| Can show a browser with no tracks | No | **Yes**, if enabled early | **Yes** | No |
| Fixed-GRCz11 pages (transcripts, clones) | Follow from the tracks | Written as rows | Unchanged code | Follow from the tracks |
| Fixes "first GRCz13ab row hides the browser" | Yes | Yes | Only for Sequences | Yes |

**Can an admin edit it?** Not through the site today, for any option. There are no admin screens for tables like these; the nearest thing is `DatabasePropertyController`, which edits key/value properties. The `assembly` table itself changes through migrations. So by default these would be edited by a developer through a migration or SQL.

- **A** records what the *deployed front end can draw*: the tracks must also be in `config-jbrowse2.json`, which ships with a release. An admin editing A's table without that deploy would break pages, so A belongs to the release process and an edit screen adds little.
- **B** is a presentation policy and is safe to flip when the tracks exist, so it is the one where an edit screen pays off, but it is also the one that can be set to a broken state.

### Questions to settle whichever way you go

1. **Required or wish-list tracks (A, and the middle path).** `GENE_SEQUENCE` asks for Genes and RefSeq. GRCz11 has Genes but not RefSeq. If all of a page's tracks are required, a gene that is only on GRCz11 gets no Sequences browser (as today; 2,062 dev genes). If only the first is required, it gets a GRCz11 one, a visible change. Starting with "all required" keeps today's behavior.
2. **Where the track ids live.** `trackMap` in Java and the front-end JSON describe the same facts twice. Moving `trackMap` into a table does not remove the JSON. Making the JSON the single source is a larger change.
3. **A toggle between assemblies on one page.** The front-end config already lists several assemblies, so offering GRCz12tu and GRCz11 on the same gene (18,971 dev genes have both landmarks) is possible. It is a front-end feature and does not depend on the choice above. Whether it replaces the separate Sequences and Transcripts images is open.
4. **The links outside the pages:** the navigation's Amplify link (GRCz12tu), the footer link and `/action/jbrowse2` (GRCz11), and the `byName` redirect. Recommended: keep the first three as manual links on a release checklist, and fix `byName` as its own bug.

### Recommendation

**A plus a switch.** It cannot produce a browser with no tracks, it replaces one of the three hard-coded lists, it makes the fixed-GRCz11 pages follow from data instead of code, and one column gives an admin the rollout lever. Start with all-required tracks. This is one person's recommendation from reading the code, not a decision; B is a reasonable choice if the team wants explicit per-page control and is willing to keep the track ids elsewhere.

### What each choice involves

For A (and the middle path), in order:

1. A migration creating `genome_browser_track` and seeding it from `trackMap`. Do not add GRCz13ab rows yet.
2. `GenomeBrowserTrack.getTrackId` reads the table, and a plain class `BrowserAssemblyChoice` that applies the rule, with unit tests (the same style as `MappingDetailControllerRulesTest`).
3. Replace the name check in `GeneViewController` and `PseudoGeneViewController` with that class.
4. Then one page per commit: transcripts, clones, STR, feature.
5. Last, remove the code constants (`GRCZ11` fixed in `TranscriptService`, `CloneViewController`, the switch in `FeatureService`).

For B: the migration and seed for the page table, `trackMap` stays, the same class applies the rank rule, and decide whether to build an edit screen at the same time.

## 3. Standing goals, and what loading GRCz13ab needs

None of this is built. The detailed steps are in section "Loading GRCz13ab" of `../assembly-blast-radius.md`. Two goals were set during this work and should stay on the list. Both are independent of D4, but D4 and GRCz13ab are easier once they are done.

### Goal 1: remove `marker_assembly`

`marker_assembly` is a stored copy of "which assembly is each gene on", rebuilt by `markerAssemblyUpdate.sql` during the NCBI load. The real source of truth is `sequence_feature_chromosome_location_generated`, and the copy drifts from it (it was off by one assembly for some genes). The goal is to delete the table and replace it with a view, a plain join over the location table, the same way "Current" annotation status became a view in this branch.

- **Before it can go:** D6 (the view must not count genetic positions as sequence placements; a plain join would add 3,993 GRCz11 gene rows, 3,827 of them from `other map location`) and the D8 follow-up (1,524 genes with only GRCv10 locations would show "GRCv10" unless the display ignores legacy assemblies).
- **What changes:** `markerAssemblyUpdate.sql` stops writing it, readers (`Marker.getLatestAssembly()` and the Genome Assembly row among them) read the view, and a migration drops the table. Use expand-then-contract: the view first, switch readers, drop the table in a later commit.

### Goal 2: move `gff3_ncbi*` to `external_resource`, without cached gene ids

`gff3_ncbi` and `gff3_ncbi_attribute` are a mirror of NCBI's GFF3 file, so they belong in the `external_resource` schema with the other mirrors (such as `ncbi_danio_rerio_gene_info`). The goal is for the table to be only the file's contents.

- **Drop the cached `gene_id` rows.** Today the load adds ZFIN gene ids into the mirror so it can be joined to markers. That mixes derived data into a copy of an upstream file. Replace it with a view that joins the mirror to `db_link` on the NCBI Gene id.
- **Add an assembly column** (or one table per assembly) so it can hold GRCz12tu and GRCz13ab together, and register the file in `load_file` with `lf_table_name` set, so the dump setting and comments work as for the other mirrors. Whether its rows are dumped follows the policy in `lf_exclude_from_dump`; the default is to dump.
- **Order:** create the view and switch readers to it, then stop writing the cached rows, then move the table. Table comments should give the upstream file's location, as agreed for the other mirrors.

- The GFF3 tables (`gff3_ncbi`, `gff3_ncbi_attribute`) hold one assembly and belong in `external_resource`; they need an assembly column.
- The NCBI load is tied to GRCz12tu in `markerAssemblyUpdate.sql`, `NCBIGff3Processor` and `NCBIGff3PostProcessor`. It should read the latest GFF3 for each current assembly from NCBI's `assembly_summary.txt`. NCBI now lists only GRCz12tu `.2` and GRCz13ab `.2` as latest (GRCz12ab was superseded), and the loader still fetches GRCz12tu `.1`.
- A dry run, including GRCz12tu `.2`, before writing.
- `marker_assembly` should become a plain join over the location table.
- The browser prerequisites: a JBrowse genome and tracks for GRCz13ab in the front-end config, and a landmark `ZFIN`-source location row written by the loader.

## 4. Other open items

| Item | Status |
|---|---|
| **D5** Which assembly the Alliance exports use | Open. `BasicVariantInfo` and `VariantLinkMLInfo` use `AssemblyEnum.GRCZ12TU`, and the Alliance job reads `zfin_genes.grcz12.gff3` by name. Moving them needs agreement outside ZFIN. |
| **D6** Where GRCz11 filtering lives | Open. The plain-join view of `marker_assembly` would add 3,993 GRCz11 gene rows, 3,827 of them from `other map location` (a genetic position, not a sequence placement). A table of location sources with a "physical placement" flag would put what `GenomeLocation.Source` knows into the database. |
| **D7** First run against GRCz12tu `.2` | Open. |
| **D8 follow-up** | GRCv10 is now a legacy assembly row. 1,524 genes have no other location; once `marker_assembly` is a plain join they would show "GRCv10" in the Genome Assembly row unless the display ignores legacy assemblies. A display rule is needed first. |
| `uniprot_release` | Has its own log; not folded into `load_file_log`. |
| Old columns on `load_file_log` | `lfl_load_name`, `lfl_source`, `lfl_table_name` are still written; drop once nothing reads them. |
| `GenPept` check in `NCBIDirectPort` | Leaves the supplement publication out of its "load publications" list, with a comment that it matches the original Perl. Looks like an omission; left unchanged. |
| NCBI query size | The "not in current release" query asks for at most 10,000 results and never checks NCBI's reported total. The list is about 4,900 today. |
| Pseudogene page | Builds its image before checking the assembly, and calls `getName()` with no null check. |

## 5. Practical notes

- **The dev database.** After this work it is the October 8 dump, plus the branch's migrations (applied by real Liquibase), plus one NCBI load. It has 3 rows in `load_publication`, not 4, because publication `ZDB-PUB-261014-17` was created after that dump and the seed skips publications that do not exist. To start over: `gradle loaddb`, then `gradle liquibasePostBuild` (about 7 and 8 minutes). The Jenkins job `Load-Database` does the same, and now has the "Load the newest dump file" checkbox. The running Jenkins keeps its own copy of the job files, so the repo change has to be applied to each instance separately.
- **Running the NCBI load directly:** `WORKING_DIR=<new empty dir> LOAD_NCBI_ONE_WAY_GENES=true gradle ncbiLoadPort`. The task is `ncbiLoadPort`.
- **Tests added:** `MarkerAnnotationStatusDerivedViewTest`, `NcbiNotInCurrentReleaseListTest`/`…DatabaseTest`, `NcbiLoadPublicationsTest`/`…DatabaseTest`, `LoadFileRegistryTest`, `AssemblyLegacyTest`, `FeatureLocationsByAssemblyTest`, `MappingDetailControllerRulesTest`, `AssemblyOrderTest`. Run one with `gradle test --tests 'org.zfin.mapping.AssemblyOrderTest'`.
- **Jenkins CLI:** `jenkins-cli login <user> <api-token>`. Until the wrapper fix ships, call `java -jar ~/.cache/zfin/jenkins-cli.jar -s http://jenkins:$JENKINS_PORT/jobs -http -auth @$JENKINS_HOME/secrets/jenkins-cli-auth update-job NAME < file` for anything that reads standard input.
- **Housekeeping still open:** push the branch; decide whether `workbench/` is committed (its images are 3.6 MB) or ignored; delete the untracked `.playwright-mcp/` directory.
- **Making the HTML version of these notes:** `pandoc FILE.md -f gfm -t html5 --standalone --embed-resources --css workbench/style.css --metadata pagetitle="TITLE" -o FILE.html`.

## 6. Decision record

Fill this in when the call is made.

- [ ] Option chosen: A / B / C / A plus a switch
- [ ] Page track lists: all required / first required
- [ ] Track ids stay in the front-end JSON and Java; no change / move toward a single source
- [ ] Toggle between assemblies on one page: yes, later / no
- [ ] Nav, footer, `/action/jbrowse2`: manual release checklist / generated
- [ ] Owner and date:

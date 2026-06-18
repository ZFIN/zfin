package org.zfin.datatransfer.go;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

/**
 * Parser for the GO Central unified "DANRE-mod" GPAD file
 * ({@code current.geneontology.org/annotations/gpad/DANRE-mod.gpad.gz}; staging copy at
 * {@code skyhook.geneontology.io/pipeline-from-goa/main/annotations/gpad/DANRE-mod.gpad.gz}).
 *
 * <p>This is the single MOD-ID-keyed product that GO's GOA-first pipeline will publish in
 * place of the separate {@code noctua_zfin.gpad.gz}, {@code zfin-prediction.gaf}, and EBI
 * {@code goa_zebrafish*.gaf} files (ZFIN-10025). The file is GPAD 2.0, so this class reuses
 * all of {@link GpadParser}'s column parsing, with/from prefix rewrites, Noctua model-id
 * extraction, and the ECO&rarr;evidence-code {@code postProcessing} unchanged.</p>
 *
 * <p>It differs from the Noctua GPAD load in one important respect: the file is a MERGED
 * superset of many sources, identified per-row by the {@code assigned_by} column, which
 * {@link GpadParser#getGafEntry} already maps into {@link GafEntry#getCreatedBy()}. Values
 * observed in staging (2026-06-02 build): {@code UniProt}, {@code GO_Central},
 * {@code InterPro}, {@code ZFIN} (our own Noctua curation round-tripping back), {@code GOC},
 * {@code RHEA}, {@code IntAct}, and ~15 lower-volume sources.</p>
 *
 * <p><b>Removal-scoping follow-up (ZFIN-10025, plan &sect;2):</b> because one file carries
 * many source organizations, the add/update/<b>remove</b> diff must eventually be
 * partitioned by source ({@code assigned_by} &rarr; {@link GafOrganization}) so each source
 * only removes the annotations it owns &mdash; otherwise a single umbrella organization would
 * mass-delete on cutover. That partitioning is Phase 1. In Phase 0 this class simply inherits
 * {@link GpadParser} behavior so the load can be exercised in <b>report-only</b> mode
 * ({@code GAF_LOAD_REPORT_ONLY=true}) for QC against the staging file with no DB writes.</p>
 */
@Component
public class DanreModGpadParser extends GpadParser {

    private Logger logger = LogManager.getLogger(DanreModGpadParser.class);

}

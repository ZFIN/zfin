package org.zfin.datatransfer.flankingsequence;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.query.NativeQuery;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.root.dto.FeatureTypeEnum;
import org.zfin.infrastructure.ant.AbstractValidateDataReportTask;
import org.zfin.mapping.GenomicLocationService;
import org.zfin.report.InlineDiff;
import org.zfin.report.Report;
import org.zfin.report.ReportNode;
import org.zfin.report.ReportTable;
import org.zfin.report.ReportWriter;
import org.zfin.sequence.gff.AssemblyEnum;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static org.zfin.mapping.GenomicLocationService.FLANKING_OFFSET;
import static org.zfin.mapping.GenomicLocationService.FLANKING_SEQUENCE_TYPES;

/**
 * Reports features whose stored flanking sequence/variation (variant_flanking_sequence.
 * vfseq_five_prime_flanking_sequence / vfseq_three_prime_flanking_sequence / vfseq_variation)
 * disagrees with what's computed live from the assembly FASTA and the feature's current
 * mutation detail, at the feature's location on whichever assembly ZFIN currently treats as
 * authoritative (ranked by the assembly table's a_order column -- the same ranking
 * FeatureRepository.getLocationByFeature() uses). Read-only -- no DB updates.
 *
 * Prompted by a curator report (ZDB-ALT-260408-13) where the displayed flanking sequence was
 * missing 2 bp. These three columns are exactly what's shown on a feature's ZFIN page under
 * Sequences -&gt; Flanking Sequence (home/WEB-INF/jsp/feature/feature-view-sequence.jsp:
 * vfsLeftEnd, vfsVariation in red, vfsRightEnd). variant_flanking_sequence has no assembly
 * column, and the daily batch job recomputed it from the GRCz11 location/FASTA regardless of
 * whether a feature had since gotten a GRCz12tu location, so it could silently overwrite a
 * correct GRCz12tu-derived value with a GRCz11-derived one in the same cache row (fixed in
 * ZFIN-10486).
 *
 * Produces three sections, because not everything that looks like drift should be fixed the
 * same way:
 * <ul>
 *   <li><b>Drift</b> -- computed rows that disagree with a recompute. Backfillable.</li>
 *   <li><b>Ambiguous location</b> -- features with more than one location row on their
 *       current assembly, with different coordinates (sometimes different chromosomes).
 *       There is no single right answer to recompute against, so these are listed rather
 *       than drift-checked; they need curation, not a backfill.</li>
 *   <li><b>Submitter-provided</b> -- rows attributed to the ZMP data-submission pub rather
 *       than to the computed-display pub. Never recomputed, by anything.</li>
 * </ul>
 *
 * Run via {@code ./gradlew :server_apps:DB_maintenance:checkFlankingSequenceDrift}.
 *
 * Nothing here is fixed automatically. To backfill the drift section, Load-Flank-Seq_w has
 * to be triggered by hand (it is manual-only as of ZFIN-10486 -- recomputing in place on an
 * unattended schedule is how the stale values got written in the first place) with
 * FORCE_FULL_UPDATE=true, plus INCLUDE_SANGER=true if "sa" features appear, then re-run this
 * report to confirm.
 */
public class CheckFlankingSequenceDriftTask extends AbstractValidateDataReportTask {

    private static final Logger LOG = LogManager.getLogger(CheckFlankingSequenceDriftTask.class);

    private static final String SCHEMA_DRIFT = "flankingSequenceDrift";
    private static final String SCHEMA_AMBIGUOUS = "ambiguousLocation";
    private static final String SCHEMA_SUBMITTED = "submitterProvided";

    /**
     * Sequences run to {@link GenomicLocationService#FLANKING_OFFSET} bases a side, and a
     * cross-assembly mismatch can differ along their whole length. Rendering that raw would
     * put hundreds of MB of near-fully-highlighted sequence into one HTML page, so cells show
     * a window around the first difference instead.
     */
    private static final int SEQUENCE_CONTEXT = 60;

    public record DriftRow(String featureZdbId, String featureAbbrev, FeatureTypeEnum featureType,
                            AssemblyEnum assembly, String assemblyName, String chromosome,
                            int startLoc, int endLoc,
                            String storedFivePrime, String storedThreePrime, String storedVariation,
                            String expectedFivePrime, String expectedThreePrime, String expectedVariation) {
    }

    public record AmbiguousLocationRow(String featureZdbId, String featureAbbrev, String featureType,
                                        String assemblyName, int locationCount, String locations) {
    }

    public record SubmittedRow(String featureZdbId, String featureAbbrev, String featureType,
                                String assemblyName, int storedOffset) {
    }

    /** Everything one scan found, plus the counters behind the summary line. */
    public record ScanResult(List<DriftRow> drift, List<AmbiguousLocationRow> ambiguous,
                              List<SubmittedRow> submitted, int examined, int caseOnlyDifferences,
                              int skippedNoCoords, int skippedUnsupportedAssembly) {
    }

    public CheckFlankingSequenceDriftTask(String jobName, String propertyFilePath, String dataDirectoryString) {
        super(jobName, propertyFilePath, dataDirectoryString);
    }

    /** Test-only: skips property/report-config init; just enough state to build a report. */
    CheckFlankingSequenceDriftTask(String jobName) {
        super();
        this.jobName = jobName;
    }

    public static void main(String[] args) {
        // args: propertiesPath, report_data dir, jobName (matches other DB_maintenance JavaExec tasks)
        CheckFlankingSequenceDriftTask task = new CheckFlankingSequenceDriftTask(args[2], args[0], args[1]);
        task.initDatabase(true);
        System.exit(task.execute());
    }

    @Override
    public int execute() {
        setLoggerFile();
        clearReportDirectory();

        try {
            ScanResult scan = scan();

            File html = new File(new File(dataDirectory, jobName), jobName + ".html");
            new ReportWriter().write(buildReport(scan), html);
            LOG.info("HTML report written to: " + html.getAbsolutePath());
            System.out.println("HTML report written to: " + html.getAbsolutePath());
            return 0;
        } catch (Exception e) {
            LOG.error("Flanking-sequence drift check failed", e);
            return 1;
        } finally {
            HibernateUtil.closeSession();
        }
    }

    // -------- report --------

    Report buildReport(ScanResult scan) {
        Report report = new Report()
            .meta(new Report.Meta()
                .title(jobName + " — flanking sequence drift")
                .createdAt(System.currentTimeMillis())
                .schemaVersion("1"))
            .definitions(defs());

        ReportNode root = new ReportNode()
            .id("_root")
            .title(jobName)
            .body(Report.Body.text(
                "Examined " + scan.examined() + " flanking-sequence rows (ZFIN-10486). "
                + scan.drift().size() + " drifted, " + scan.ambiguous().size() + " with an ambiguous "
                + "location, " + scan.submitted().size() + " submitter-provided (never recomputed). "
                + scan.skippedNoCoords() + " skipped for missing coordinates, "
                + scan.skippedUnsupportedAssembly() + " on an assembly with no FASTA. "
                + scan.caseOnlyDifferences() + " differ only in upper/lower case and are not counted "
                + "as drift.\n\n"
                + "Comparison is case-insensitive and uses a " + FLANKING_OFFSET + "bp offset (what the "
                + "production recalculation writes), not whatever offset the stored row happens to "
                + "carry. Sequence cells show a window around the first difference, with diff "
                + "highlighting (underlined + yellow)."));
        root.addChild(driftNode(scan.drift()));
        root.addChild(ambiguousNode(scan.ambiguous()));
        root.addChild(submittedNode(scan.submitted()));
        report.root(root);
        return report;
    }

    private ReportNode driftNode(List<DriftRow> drift) {
        ReportTable table = new ReportTable()
            .schemaRef(SCHEMA_DRIFT)
            .title(tableTitle("Drifted features", drift.size()));
        for (DriftRow row : drift) {
            int fiveAt = InlineDiff.firstDifference(row.storedFivePrime(), row.expectedFivePrime());
            int threeAt = InlineDiff.firstDifference(row.storedThreePrime(), row.expectedThreePrime());
            String storedFive = excerpt(row.storedFivePrime(), fiveAt);
            String expectedFive = excerpt(row.expectedFivePrime(), fiveAt);
            String storedThree = excerpt(row.storedThreePrime(), threeAt);
            String expectedThree = excerpt(row.expectedThreePrime(), threeAt);
            table.addRow(
                "zdbID", row.featureZdbId(),
                "featureAbbrev", row.featureAbbrev(),
                "featureType", row.featureType().name(),
                "assembly", row.assemblyName(),
                "location", row.chromosome() + ":" + row.startLoc() + "-" + row.endLoc(),
                "storedFivePrime", InlineDiff.highlightOld(storedFive, expectedFive),
                "expectedFivePrime", InlineDiff.highlightNew(storedFive, expectedFive),
                "storedVariation", InlineDiff.highlightOld(row.storedVariation(), row.expectedVariation()),
                "expectedVariation", InlineDiff.highlightNew(row.storedVariation(), row.expectedVariation()),
                "storedThreePrime", InlineDiff.highlightOld(storedThree, expectedThree),
                "expectedThreePrime", InlineDiff.highlightNew(storedThree, expectedThree));
        }
        return new ReportNode()
            .id("drift")
            .title("Drift")
            .count((long) drift.size())
            .body(Report.Body.text("Stored flanking sequence or variation disagrees with a recompute "
                + "against the feature's current-assembly location. These are fixable, but nothing "
                + "fixes them automatically: Load-Flank-Seq_w is manual-only and no longer runs "
                + "nightly, so someone has to trigger it by hand with FORCE_FULL_UPDATE=true -- plus "
                + "INCLUDE_SANGER=true if \"sa\" features appear here -- and then re-run this report "
                + "to confirm the count drops.\n\n"
                + "Rows whose Variation changed are listed first -- that is the ref/var notation "
                + "shown in red on the feature page, so a difference there is immediately visible "
                + "to anyone reading the record. Within each group, rows are ordered by feature "
                + "abbreviation."))
            .addTable(table);
    }

    private ReportNode ambiguousNode(List<AmbiguousLocationRow> ambiguous) {
        ReportTable table = new ReportTable()
            .schemaRef(SCHEMA_AMBIGUOUS)
            .title(tableTitle("Ambiguous locations", ambiguous.size()));
        for (AmbiguousLocationRow row : ambiguous) {
            table.addRow(
                "zdbID", row.featureZdbId(),
                "featureAbbrev", row.featureAbbrev(),
                "featureType", row.featureType(),
                "assembly", row.assemblyName(),
                "locationCount", row.locationCount(),
                "locations", row.locations());
        }
        return new ReportNode()
            .id("ambiguous")
            .title("Ambiguous location")
            .count((long) ambiguous.size())
            .body(Report.Body.text("These features have more than one location row on their current "
                + "assembly, with different coordinates -- sometimes on different chromosomes, which "
                + "suggests an unresolved multi-mapping from liftover. There is no single correct "
                + "position to compute a flanking sequence from, so they are NOT drift-checked here: "
                + "whichever row the code picks is arbitrary, and reporting that as drift would be "
                + "noise. Recalculating them will produce a stable but still arbitrary answer, so "
                + "they need a curator to resolve the duplicate, not a backfill."))
            .addTable(table);
    }

    private ReportNode submittedNode(List<SubmittedRow> submitted) {
        ReportTable table = new ReportTable()
            .schemaRef(SCHEMA_SUBMITTED)
            .title(tableTitle("Submitter-provided flanking sequence", submitted.size()));
        for (SubmittedRow row : submitted) {
            table.addRow(
                "zdbID", row.featureZdbId(),
                "featureAbbrev", row.featureAbbrev(),
                "featureType", row.featureType(),
                "assembly", row.assemblyName(),
                "storedOffset", row.storedOffset());
        }
        return new ReportNode()
            .id("submitted")
            .title("Submitter-provided")
            .count((long) submitted.size())
            .body(Report.Body.text("Flanking sequence attributed to "
                + GenomicLocationService.SUBMITTED_FLANK_SEQ_PUB + " (Sanger ZMP mutant data "
                + "submission) rather than to the computed-display pub "
                + GenomicLocationService.COMPUTED_FLANK_SEQ_PUB + ". It came from the submitter, not "
                + "from an assembly FASTA -- note the " + "offset, which this code never produces. "
                + "Recomputing would overwrite submitted data, so the batch job skips these "
                + "regardless of INCLUDE_SANGER, and they are not drift-checked. Listed only so the "
                + "exclusion is visible rather than silent.\n\n"
                + "The three sections are mutually exclusive and this one wins: a feature that is "
                + "both submitter-provided and ambiguously located appears only here, since nothing "
                + "would recompute it either way."))
            .addTable(table);
    }

    private String tableTitle(String label, int total) {
        return label + " (" + total + ")";
    }

    /**
     * A window of the sequence around {@code around}, so a cell stays readable (and the page
     * stays a sane size) even when the two sequences differ along their whole length.
     *
     * An elided start is marked "…[n]", where n is the 1-based position of the first base
     * shown -- without it you can see what changed but not where in the flank it sits.
     */
    static String excerpt(String sequence, int around) {
        if (sequence == null) {
            return "";
        }
        if (around < 0 || sequence.length() <= 2 * SEQUENCE_CONTEXT) {
            return sequence.length() <= 2 * SEQUENCE_CONTEXT
                ? sequence
                : sequence.substring(0, 2 * SEQUENCE_CONTEXT) + "…";
        }
        int from = Math.max(0, around - SEQUENCE_CONTEXT);
        int to = Math.min(sequence.length(), around + SEQUENCE_CONTEXT);
        return (from > 0 ? "…[" + (from + 1) + "]" : "")
            + sequence.substring(from, to)
            + (to < sequence.length() ? "…" : "");
    }

    private Report.Definitions defs() {
        return new Report.Definitions()
            .field("zdbID", Report.FieldDef.link("ZDB ID", "https://zfin.org/{value}"))
            .field("htmlCell", Report.FieldDef.html("Value"))
            .tableSchema(SCHEMA_DRIFT, new Report.TableSchema()
                .description("Stored vs. recomputed, windowed around the first difference; "
                        + "diff-highlighted cells use <u> (rendered yellow).")
                .addColumn(ReportTable.Column.of("zdbID", "Feature ID", "zdbID"))
                .addColumn(ReportTable.Column.of("featureAbbrev", "Feature Abbreviation"))
                .addColumn(ReportTable.Column.of("featureType", "Feature Type"))
                .addColumn(ReportTable.Column.of("assembly", "Assembly"))
                .addColumn(ReportTable.Column.of("location", "Location"))
                .addColumn(ReportTable.Column.of("storedFivePrime", "Stored 5' Flank", "htmlCell"))
                .addColumn(ReportTable.Column.of("expectedFivePrime", "Expected 5' Flank", "htmlCell"))
                .addColumn(ReportTable.Column.of("storedVariation", "Stored Variation", "htmlCell"))
                .addColumn(ReportTable.Column.of("expectedVariation", "Expected Variation", "htmlCell"))
                .addColumn(ReportTable.Column.of("storedThreePrime", "Stored 3' Flank", "htmlCell"))
                .addColumn(ReportTable.Column.of("expectedThreePrime", "Expected 3' Flank", "htmlCell")))
            .tableSchema(SCHEMA_AMBIGUOUS, new Report.TableSchema()
                .description("Features with multiple, disagreeing location rows on their current assembly.")
                .addColumn(ReportTable.Column.of("zdbID", "Feature ID", "zdbID"))
                .addColumn(ReportTable.Column.of("featureAbbrev", "Feature Abbreviation"))
                .addColumn(ReportTable.Column.of("featureType", "Feature Type"))
                .addColumn(ReportTable.Column.of("assembly", "Assembly"))
                .addColumn(ReportTable.Column.of("locationCount", "Location Rows"))
                .addColumn(ReportTable.Column.of("locations", "Conflicting Locations")))
            .tableSchema(SCHEMA_SUBMITTED, new Report.TableSchema()
                .description("Flanking sequence that came from the submitter; excluded from recalculation.")
                .addColumn(ReportTable.Column.of("zdbID", "Feature ID", "zdbID"))
                .addColumn(ReportTable.Column.of("featureAbbrev", "Feature Abbreviation"))
                .addColumn(ReportTable.Column.of("featureType", "Feature Type"))
                .addColumn(ReportTable.Column.of("assembly", "Assembly"))
                .addColumn(ReportTable.Column.of("storedOffset", "Stored Offset (bp)")));
    }

    // -------- data --------

    /**
     * One pass over every feature with a stored flanking sequence, sorting each into drift,
     * ambiguous-location, submitter-provided, or neither.
     */
    ScanResult scan() {
        List<DriftRow> drift = new ArrayList<>();
        List<AmbiguousLocationRow> ambiguous = new ArrayList<>();
        List<SubmittedRow> submitted = new ArrayList<>();
        int examined = 0;
        int caseOnlyDifferences = 0;
        int skippedNoCoords = 0;
        int skippedUnsupportedAssembly = 0;

        // variant_flanking_sequence has one row per feature (FeatureRepository.getFeatureVariant()
        // relies on that too), joined to the feature's location on whichever assembly ranks
        // highest by the assembly table's a_order -- the same source of truth
        // getLocationByFeature() uses, as a window function so this is one query rather than one
        // per feature. sfcl_zdb_id breaks a_order ties so the row picked here matches the one
        // getLocationByFeature()/getAllFeatureLocationsForAssembly() pick; rows_on_assembly
        // surfaces when that tie existed at all. The ranking subquery is restricted to features
        // we actually report on, so it doesn't window all ~162k location rows.
        String sql = """
                with candidate as (
                    select vfseq.vfseq_zdb_id,
                           vfseq.vfseq_data_zdb_id,
                           vfseq.vfseq_five_prime_flanking_sequence,
                           vfseq.vfseq_three_prime_flanking_sequence,
                           vfseq.vfseq_variation,
                           vfseq.vfseq_offset_start,
                           f.feature_abbrev,
                           f.feature_type,
                           fgmd.fgmd_sequence_of_reference,
                           fgmd.fgmd_sequence_of_variation
                      from variant_flanking_sequence vfseq
                      join feature f on f.feature_zdb_id = vfseq.vfseq_data_zdb_id
                      left join feature_genomic_mutation_detail fgmd
                             on fgmd.fgmd_feature_zdb_id = vfseq.vfseq_data_zdb_id
                     where f.feature_type in (:featureTypes)
                ),
                ranked_location as (
                    select sfcl.sfcl_feature_zdb_id,
                           sfcl.sfcl_assembly,
                           sfcl.sfcl_chromosome,
                           sfcl.sfcl_start_position,
                           sfcl.sfcl_end_position,
                           row_number() over (
                               partition by sfcl.sfcl_feature_zdb_id
                               order by a.a_order asc, sfcl.sfcl_zdb_id asc
                           ) as rn,
                           count(*) over (
                               partition by sfcl.sfcl_feature_zdb_id, sfcl.sfcl_assembly
                           ) as rows_on_assembly
                      from sequence_feature_chromosome_location sfcl
                      join assembly a on a.a_name = sfcl.sfcl_assembly
                      join candidate c on c.vfseq_data_zdb_id = sfcl.sfcl_feature_zdb_id
                )
                select c.vfseq_data_zdb_id,
                       c.feature_abbrev,
                       c.feature_type,
                       loc.sfcl_assembly,
                       loc.sfcl_chromosome,
                       loc.sfcl_start_position,
                       loc.sfcl_end_position,
                       c.vfseq_five_prime_flanking_sequence,
                       c.vfseq_three_prime_flanking_sequence,
                       c.vfseq_variation,
                       c.vfseq_offset_start,
                       c.fgmd_sequence_of_reference,
                       c.fgmd_sequence_of_variation,
                       coalesce(loc.rows_on_assembly, 1) as rows_on_assembly,
                       exists (
                           select 1 from record_attribution ra
                            where ra.recattrib_data_zdb_id = c.vfseq_zdb_id
                              and ra.recattrib_source_zdb_id = :submittedPub
                       ) as submitter_provided
                  from candidate c
                  left join ranked_location loc
                         on loc.sfcl_feature_zdb_id = c.vfseq_data_zdb_id and loc.rn = 1
                 order by c.vfseq_data_zdb_id
                """;

        NativeQuery<Object[]> query = HibernateUtil.currentSession().createNativeQuery(sql, Object[].class);
        query.setParameterList("featureTypes", FLANKING_SEQUENCE_TYPES.stream().map(Enum::name).toList());
        query.setParameter("submittedPub", GenomicLocationService.SUBMITTED_FLANK_SEQ_PUB);
        GenomicLocationService glService = new GenomicLocationService();

        for (Object[] row : query.list()) {
            examined++;
            String featureZdbId = (String) row[0];
            String featureAbbrev = (String) row[1];
            String featureTypeName = (String) row[2];
            String assemblyName = (String) row[3];
            String chromosome = (String) row[4];
            Integer startLoc = row[5] != null ? ((Number) row[5]).intValue() : null;
            Integer endLoc = row[6] != null ? ((Number) row[6]).intValue() : null;
            String storedFivePrime = (String) row[7];
            String storedThreePrime = (String) row[8];
            String storedVariation = (String) row[9];
            int storedOffset = row[10] != null ? ((Number) row[10]).intValue() : FLANKING_OFFSET;
            String fgmdSeqRef = (String) row[11];
            String fgmdSeqVar = (String) row[12];
            int rowsOnAssembly = ((Number) row[13]).intValue();
            boolean submitterProvided = (Boolean) row[14];

            if (submitterProvided) {
                submitted.add(new SubmittedRow(featureZdbId, nullToEmpty(featureAbbrev),
                        featureTypeName, nullToEmpty(assemblyName), storedOffset));
                continue;
            }

            if (assemblyName == null || chromosome == null || startLoc == null || endLoc == null) {
                skippedNoCoords++;
                continue;
            }

            if (rowsOnAssembly > 1) {
                ambiguous.add(new AmbiguousLocationRow(featureZdbId, nullToEmpty(featureAbbrev),
                        featureTypeName, assemblyName, rowsOnAssembly,
                        describeLocations(featureZdbId, assemblyName)));
                continue;
            }

            AssemblyEnum assembly = GenomicLocationService.supportedAssemblyFor(assemblyName);
            FeatureTypeEnum featureType = featureTypeFor(featureTypeName);
            if (assembly == null || featureType == null) {
                skippedUnsupportedAssembly++;
                continue;
            }

            String expectedFivePrime;
            String expectedThreePrime;
            try {
                // Always the production offset, not the stored one: a row written with a
                // different offset is itself something a recompute would change.
                GenomicLocationService.FlankingSequencePair expected = glService.computeFlankingSequences(
                        featureType, assembly, chromosome, startLoc, endLoc, FLANKING_OFFSET);
                expectedFivePrime = upper(expected.fivePrime());
                expectedThreePrime = upper(expected.threePrime());
            } catch (RuntimeException e) {
                skippedUnsupportedAssembly++;
                LOG.info("Skipping " + featureZdbId
                        + " (" + assemblyName + " " + chromosome + ":" + startLoc + "-" + endLoc
                        + "): " + e);
                continue;
            }

            String expectedVariation = expectedVariation(featureType, fgmdSeqRef, fgmdSeqVar);
            String storedFivePrimeUpper = upper(storedFivePrime);
            String storedThreePrimeUpper = upper(storedThreePrime);
            String storedVariationUpper = upper(storedVariation);
            String expectedVariationUpper = expectedVariation == null ? null : upper(expectedVariation);

            boolean flanksDiffer = !expectedFivePrime.equals(storedFivePrimeUpper)
                    || !expectedThreePrime.equals(storedThreePrimeUpper);
            // Null when the production code wouldn't rewrite vfseq_variation either, so there's
            // nothing to compare -- see expectedVariation()'s javadoc.
            boolean variationDiffers = expectedVariationUpper != null
                    && !expectedVariationUpper.equals(storedVariationUpper);

            if (!flanksDiffer && !variationDiffers) {
                // Equal once upper-cased but not byte-identical: the stored row is soft-masked
                // (lower-case) FASTA output. Curators have flagged the mixed casing separately;
                // count it so it's visible without drowning the real drift.
                if (!equalsExact(storedFivePrime, expectedFivePrime)
                        || !equalsExact(storedThreePrime, expectedThreePrime)) {
                    caseOnlyDifferences++;
                }
                continue;
            }

            drift.add(new DriftRow(featureZdbId, nullToEmpty(featureAbbrev), featureType, assembly,
                    assemblyName, chromosome, startLoc, endLoc,
                    storedFivePrimeUpper, storedThreePrimeUpper, storedVariationUpper,
                    expectedFivePrime, expectedThreePrime,
                    expectedVariationUpper != null ? expectedVariationUpper : storedVariationUpper));
        }

        drift.sort(DRIFT_ORDER);
        ambiguous.sort(Comparator.comparing(AmbiguousLocationRow::featureAbbrev, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(AmbiguousLocationRow::featureZdbId));
        submitted.sort(Comparator.comparing(SubmittedRow::featureAbbrev, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(SubmittedRow::featureZdbId));

        LOG.info("Examined " + examined + " variant_flanking_sequence rows; "
                + drift.size() + " drifted; "
                + ambiguous.size() + " ambiguous location; "
                + submitted.size() + " submitter-provided; "
                + caseOnlyDifferences + " case-only differences; "
                + skippedNoCoords + " skipped (no usable coords); "
                + skippedUnsupportedAssembly + " skipped (no FASTA for assembly / unreadable)");

        return new ScanResult(drift, ambiguous, submitted, examined, caseOnlyDifferences,
                skippedNoCoords, skippedUnsupportedAssembly);
    }

    /** The conflicting coordinates behind an ambiguous location, for the report cell. */
    private String describeLocations(String featureZdbId, String assemblyName) {
        List<Object[]> rows = HibernateUtil.currentSession().createNativeQuery("""
                select sfcl_chromosome, sfcl_start_position, sfcl_end_position
                  from sequence_feature_chromosome_location
                 where sfcl_feature_zdb_id = :featureId and sfcl_assembly = :assembly
                 order by sfcl_zdb_id
                """, Object[].class)
            .setParameter("featureId", featureZdbId)
            .setParameter("assembly", assemblyName)
            .list();
        List<String> described = new ArrayList<>(rows.size());
        for (Object[] row : rows) {
            described.add(row[0] + ":" + row[1] + "-" + row[2]);
        }
        return String.join("  |  ", described);
    }

    /**
     * The vfseq_variation string the production code would write for this feature -- the same
     * helper it uses, so the report can't disagree with it. Null means production wouldn't
     * write a variation at all (a mutation detail with no sequence in it), so there's nothing
     * to compare and no drift to report; those features are a separate problem, covered by
     * Check-Feature-Mutation-Detail-Missing-Sequences_w.
     */
    String expectedVariation(FeatureTypeEnum type, String fgmdSeqRef, String fgmdSeqVar) {
        return GenomicLocationService.variationNotation(type, fgmdSeqRef, fgmdSeqVar);
    }

    private FeatureTypeEnum featureTypeFor(String name) {
        try {
            return FeatureTypeEnum.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * A changed variation leads: that is the ref/var notation shown in red on the feature
     * page, so a difference there is what a reader notices first. Abbreviation orders the
     * rest, with the ZDB ID as a final tiebreak so the ordering is stable between runs.
     */
    static final Comparator<DriftRow> DRIFT_ORDER =
        Comparator.comparing((DriftRow row) -> variationChanged(row) ? 0 : 1)
            .thenComparing(DriftRow::featureAbbrev, String.CASE_INSENSITIVE_ORDER)
            .thenComparing(DriftRow::featureZdbId);

    /** Whether this row's variation is one of the fields that actually differs. */
    static boolean variationChanged(DriftRow row) {
        return !nullToEmpty(row.storedVariation()).equals(nullToEmpty(row.expectedVariation()));
    }

    private static boolean equalsExact(String a, String b) {
        return nullToEmpty(a).equals(nullToEmpty(b));
    }

    /** Locale.ROOT so an upper-casing locale (e.g. Turkish i) can't corrupt a comparison. */
    private static String upper(String value) {
        return nullToEmpty(value).toUpperCase(Locale.ROOT);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}

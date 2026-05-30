package org.zfin.zirc.service;

import org.junit.Test;
import org.zfin.marker.Marker;
import org.zfin.zirc.entity.Gene;
import org.zfin.zirc.entity.LineSubmission;
import org.zfin.zirc.entity.Mutation;
import org.zfin.zirc.service.LineSubmissionStatusComputer.FieldStatus;
import org.zfin.zirc.service.LineSubmissionStatusComputer.FieldStatusResult;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class LineSubmissionStatusComputerTest {

    // ─── worse() ────────────────────────────────────────────────────────────

    @Test
    public void worse_missingBeatsEverything() {
        assertEquals(FieldStatus.MISSING, FieldStatus.MISSING.worse(FieldStatus.IN_PROGRESS));
        assertEquals(FieldStatus.MISSING, FieldStatus.MISSING.worse(FieldStatus.COMPLETE));
        assertEquals(FieldStatus.MISSING, FieldStatus.COMPLETE.worse(FieldStatus.MISSING));
    }

    @Test
    public void worse_inProgressBeatsComplete() {
        assertEquals(FieldStatus.IN_PROGRESS,
                FieldStatus.IN_PROGRESS.worse(FieldStatus.COMPLETE));
        assertEquals(FieldStatus.IN_PROGRESS,
                FieldStatus.COMPLETE.worse(FieldStatus.IN_PROGRESS));
    }

    @Test
    public void worse_nullIsIgnored() {
        assertEquals(FieldStatus.COMPLETE, FieldStatus.COMPLETE.worse(null));
    }

    // ─── compute(): all-empty new submission ────────────────────────────────

    @Test
    public void allEmpty_requiredAreMissing_optionalAreComplete() {
        FieldStatusResult r = LineSubmissionStatusComputer.compute(new LineSubmission());

        // Required scalars / collections
        assertEquals(FieldStatus.MISSING, r.byField().get("name"));
        assertEquals(FieldStatus.MISSING, r.byField().get("maternalBackground"));
        assertEquals(FieldStatus.MISSING, r.byField().get("paternalBackground"));
        assertEquals(FieldStatus.MISSING, r.byField().get("backgroundChangeable"));
        assertEquals(FieldStatus.MISSING, r.byField().get("reasons"));
        assertEquals(FieldStatus.MISSING, r.byField().get("mutations"));

        // Concerns field is not applicable when backgroundChangeable is null
        assertFalse("concerns must be absent when not applicable",
                r.byField().containsKey("backgroundChangeConcerns"));

        // Optional fields are Complete whether empty or filled — empty is a valid terminal state
        assertEquals(FieldStatus.COMPLETE, r.byField().get("previousNames"));
        assertEquals(FieldStatus.COMPLETE, r.byField().get("abbreviation"));
        assertEquals(FieldStatus.COMPLETE, r.byField().get("singleAllelic"));
        assertEquals(FieldStatus.COMPLETE, r.byField().get("husbandryInfo"));
        assertEquals(FieldStatus.COMPLETE, r.byField().get("unreportedFeaturesDetails"));
        assertEquals(FieldStatus.COMPLETE, r.byField().get("additionalInfo"));
        assertEquals(FieldStatus.COMPLETE, r.byField().get("linkedFeatures"));

        // Section rollups
        assertEquals(FieldStatus.MISSING, r.bySection().get("Overview"));
        assertEquals(FieldStatus.MISSING, r.bySection().get("Mutations"));
        assertEquals(FieldStatus.COMPLETE, r.bySection().get("Linked Features"));
        assertEquals(FieldStatus.MISSING, r.bySection().get("Background"));
        assertEquals(FieldStatus.COMPLETE, r.bySection().get("Additional Info"));
        assertEquals(FieldStatus.MISSING, r.overall());
    }

    // ─── compute(): fully filled ────────────────────────────────────────────

    @Test
    public void fullyFilled_allComplete() {
        LineSubmission s = filledOut();
        FieldStatusResult r = LineSubmissionStatusComputer.compute(s);

        for (Map.Entry<String, FieldStatus> e : r.byField().entrySet()) {
            assertEquals("expected COMPLETE for " + e.getKey(),
                    FieldStatus.COMPLETE, e.getValue());
        }
        for (Map.Entry<String, FieldStatus> e : r.bySection().entrySet()) {
            assertEquals("expected COMPLETE for section " + e.getKey(),
                    FieldStatus.COMPLETE, e.getValue());
        }
        assertEquals(FieldStatus.COMPLETE, r.overall());
    }

    // ─── compute(): conditional concerns field ──────────────────────────────

    @Test
    public void concerns_appliesOnlyWhenBackgroundNotChangeable() {
        // changeable=true → concerns N/A, no entry
        LineSubmission s1 = new LineSubmission();
        s1.setBackgroundChangeable(Boolean.TRUE);
        s1.setBackgroundChangeConcerns(null);
        FieldStatusResult r1 = LineSubmissionStatusComputer.compute(s1);
        assertFalse(r1.byField().containsKey("backgroundChangeConcerns"));

        // changeable=false, concerns empty → COMPLETE (concerns is optional)
        LineSubmission s2 = new LineSubmission();
        s2.setBackgroundChangeable(Boolean.FALSE);
        s2.setBackgroundChangeConcerns(null);
        FieldStatusResult r2 = LineSubmissionStatusComputer.compute(s2);
        assertEquals(FieldStatus.COMPLETE, r2.byField().get("backgroundChangeConcerns"));

        // changeable=false, concerns filled → COMPLETE
        LineSubmission s3 = new LineSubmission();
        s3.setBackgroundChangeable(Boolean.FALSE);
        s3.setBackgroundChangeConcerns("some concern");
        FieldStatusResult r3 = LineSubmissionStatusComputer.compute(s3);
        assertEquals(FieldStatus.COMPLETE, r3.byField().get("backgroundChangeConcerns"));
    }

    // ─── compute(): mixed — required missing + optional empty → MISSING ────

    @Test
    public void mixed_sectionTakesWorstOfAllFields() {
        // Background section: maternal filled, paternal missing, changeable yes
        // → section should be MISSING (paternal drives it)
        LineSubmission s = new LineSubmission();
        s.setMaternalBackground("AB");
        s.setPaternalBackground(null);
        s.setBackgroundChangeable(Boolean.TRUE);
        FieldStatusResult r = LineSubmissionStatusComputer.compute(s);
        assertEquals(FieldStatus.COMPLETE, r.byField().get("maternalBackground"));
        assertEquals(FieldStatus.MISSING, r.byField().get("paternalBackground"));
        assertEquals(FieldStatus.COMPLETE, r.byField().get("backgroundChangeable"));
        assertEquals(FieldStatus.MISSING, r.bySection().get("Background"));
    }

    // ─── compute(): blank string is treated as empty ───────────────────────

    @Test
    public void blankString_treatedAsEmpty() {
        LineSubmission s = new LineSubmission();
        s.setName("   "); // blank, not null
        FieldStatusResult r = LineSubmissionStatusComputer.compute(s);
        assertEquals(FieldStatus.MISSING, r.byField().get("name"));
    }

    // ─── helpers ───────────────────────────────────────────────────────────

    private static LineSubmission filledOut() {
        LineSubmission s = new LineSubmission();
        s.setName("test line");
        s.setAbbreviation("tl");
        s.setPreviousNames(new String[]{"old name"});
        s.setMaternalBackground("AB");
        s.setPaternalBackground("TU");
        s.setBackgroundChangeable(Boolean.TRUE);
        s.setSingleAllelic(Boolean.TRUE);
        s.setHusbandryInfo("standard");
        s.setUnreportedFeaturesDetails("none");
        s.setAdditionalInfo("notes");
        s.setReasons(new String[]{"frequently_requested"});

        Set<Mutation> muts = new HashSet<>();
        Mutation mut = new Mutation();
        // Fill General-section required fields so MutationStatusComputer's
        // overall is COMPLETE; otherwise the LineSubmission's Mutations
        // section rolls up Missing per the cross-computer rule.
        mut.setAlleleDesignation("ab123");
        mut.setMutagenesisStage("F0");
        mut.setMutagenesisProtocol("CRISPR");
        mut.setMutationType("indel");
        // Genes is required on a Mutation; one Gene with a Marker set
        // satisfies the per-row required field too. The status check now
        // reads `mutatedGeneZdbID` via the entity's @Transient accessor,
        // so the Marker needs an actual ZDB-ID — a default-constructed
        // Marker returns null and would surface as MISSING.
        Gene g = new Gene();
        Marker marker = new Marker();
        marker.setZdbID("ZDB-GENE-TEST-1");
        g.setMutatedGene(marker);
        // genbankGenomicDna is now required per the gene schema.
        g.setGenbankGenomicDna("NC_999999");
        Set<Gene> genes = new HashSet<>();
        genes.add(g);
        mut.setGenes(genes);
        muts.add(mut);
        s.setMutations(muts);

        // linkedFeatures: optional, populate one so it's also Complete
        Set<org.zfin.zirc.entity.LinkedFeature> lfs = new HashSet<>();
        lfs.add(new org.zfin.zirc.entity.LinkedFeature());
        s.setLinkedFeatures(lfs);

        return s;
    }
}
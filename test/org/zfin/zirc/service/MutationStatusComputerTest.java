package org.zfin.zirc.service;

import org.junit.Test;
import org.zfin.marker.Marker;
import org.zfin.zirc.entity.Gene;
import org.zfin.zirc.entity.Mutation;
import org.zfin.zirc.service.LineSubmissionStatusComputer.FieldStatus;
import org.zfin.zirc.service.LineSubmissionStatusComputer.FieldStatusResult;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MutationStatusComputerTest {

    @Test
    public void emptyMutation_requiredAreMissing_optionalAreComplete_generalIsMissing() {
        Mutation m = new Mutation();
        // alleleInZfin defaults to FALSE per entity initializer, which is "complete" for an optional boolean.

        FieldStatusResult r = MutationStatusComputer.compute(m);

        assertEquals(FieldStatus.MISSING,  r.byField().get("alleleDesignation"));
        assertEquals(FieldStatus.MISSING,  r.byField().get("mutagenesisStage"));
        assertEquals(FieldStatus.MISSING,  r.byField().get("mutagenesisProtocol"));
        assertEquals(FieldStatus.MISSING,  r.byField().get("mutationType"));

        assertEquals(FieldStatus.COMPLETE, r.byField().get("alleleInZfin"));
        assertEquals(FieldStatus.COMPLETE, r.byField().get("molecularlyCharacterized"));
        assertEquals(FieldStatus.COMPLETE, r.byField().get("zfinRecordEstablished"));
        assertEquals(FieldStatus.COMPLETE, r.byField().get("mutationDiscoverer"));
        assertEquals(FieldStatus.COMPLETE, r.byField().get("mutationInstitution"));

        assertFalse("cellGenomicFeature must be absent when zfinRecordEstablished != true",
                r.byField().containsKey("cellGenomicFeature"));

        assertEquals(FieldStatus.MISSING, r.bySection().get("Overview"));
        assertEquals(FieldStatus.MISSING, r.bySection().get("Genes"));
        assertEquals(FieldStatus.MISSING, r.overall());
    }

    @Test
    public void allRequiredFilled_isComplete() {
        Mutation m = new Mutation();
        m.setAlleleDesignation("ab123");
        m.setMutagenesisStage("F0");
        m.setMutagenesisProtocol("CRISPR/Cas9");
        m.setMutationType("indel");
        m.setGenes(genesWithOne());

        FieldStatusResult r = MutationStatusComputer.compute(m);

        assertEquals(FieldStatus.COMPLETE, r.byField().get("alleleDesignation"));
        assertEquals(FieldStatus.COMPLETE, r.byField().get("mutagenesisStage"));
        assertEquals(FieldStatus.COMPLETE, r.byField().get("mutagenesisProtocol"));
        assertEquals(FieldStatus.COMPLETE, r.byField().get("mutationType"));
        assertEquals(FieldStatus.COMPLETE, r.bySection().get("Overview"));
        assertEquals(FieldStatus.COMPLETE, r.bySection().get("Genes"));
        assertEquals(FieldStatus.COMPLETE, r.overall());
    }

    @Test
    public void emptyGenes_makesGenesSectionMissing() {
        Mutation m = new Mutation();
        // Even with Overview complete, an empty Genes collection trips the rollup.
        m.setAlleleDesignation("ab123");
        m.setMutagenesisStage("F0");
        m.setMutagenesisProtocol("CRISPR/Cas9");
        m.setMutationType("indel");
        // no genes set
        FieldStatusResult r = MutationStatusComputer.compute(m);
        assertEquals(FieldStatus.COMPLETE, r.bySection().get("Overview"));
        assertEquals(FieldStatus.MISSING, r.bySection().get("Genes"));
        assertEquals(FieldStatus.MISSING, r.overall());
    }

    @Test
    public void genePresentWithoutMutatedGene_makesGenesSectionMissing() {
        Mutation m = new Mutation();
        m.setAlleleDesignation("ab123");
        m.setMutagenesisStage("F0");
        m.setMutagenesisProtocol("CRISPR/Cas9");
        m.setMutationType("indel");
        // a Gene row exists but its required mutatedGene is null
        Set<Gene> genes = new HashSet<>();
        genes.add(new Gene());
        m.setGenes(genes);
        FieldStatusResult r = MutationStatusComputer.compute(m);
        assertEquals(FieldStatus.MISSING, r.bySection().get("Genes"));
        assertEquals(FieldStatus.MISSING, r.overall());
    }

    /** Build a Set containing one valid Gene (mutatedGene set). */
    private static Set<Gene> genesWithOne() {
        Gene g = new Gene();
        g.setMutatedGene(new Marker());
        Set<Gene> genes = new HashSet<>();
        genes.add(g);
        return genes;
    }

    @Test
    public void cellGenomicFeature_appliesOnlyWhenZfinRecordEstablishedIsYes() {
        // null → not applicable
        Mutation m1 = new Mutation();
        FieldStatusResult r1 = MutationStatusComputer.compute(m1);
        assertFalse(r1.byField().containsKey("cellGenomicFeature"));

        // false → not applicable
        Mutation m2 = new Mutation();
        m2.setZfinRecordEstablished(Boolean.FALSE);
        FieldStatusResult r2 = MutationStatusComputer.compute(m2);
        assertFalse(r2.byField().containsKey("cellGenomicFeature"));

        // true + empty → still COMPLETE (field is optional even when applicable)
        Mutation m3 = new Mutation();
        m3.setZfinRecordEstablished(Boolean.TRUE);
        FieldStatusResult r3 = MutationStatusComputer.compute(m3);
        assertTrue(r3.byField().containsKey("cellGenomicFeature"));
        assertEquals(FieldStatus.COMPLETE, r3.byField().get("cellGenomicFeature"));

        // true + filled → COMPLETE
        Mutation m4 = new Mutation();
        m4.setZfinRecordEstablished(Boolean.TRUE);
        m4.setCellGenomicFeature("ZDB-GENO-123");
        FieldStatusResult r4 = MutationStatusComputer.compute(m4);
        assertEquals(FieldStatus.COMPLETE, r4.byField().get("cellGenomicFeature"));
    }

    @Test
    public void overallIsWorstOfGeneralFields() {
        Mutation m = new Mutation();
        m.setAlleleDesignation("ab123");
        m.setMutagenesisStage("F0");
        // mutagenesisProtocol + mutationType still missing → General = MISSING
        FieldStatusResult r = MutationStatusComputer.compute(m);
        assertEquals(FieldStatus.MISSING, r.bySection().get("Overview"));
        assertEquals(FieldStatus.MISSING, r.overall());
    }

    @Test
    public void blankString_treatedAsEmpty() {
        Mutation m = new Mutation();
        m.setAlleleDesignation("   ");
        m.setMutagenesisStage("F0");
        m.setMutagenesisProtocol("CRISPR");
        m.setMutationType("indel");
        FieldStatusResult r = MutationStatusComputer.compute(m);
        assertEquals(FieldStatus.MISSING, r.byField().get("alleleDesignation"));
    }
}

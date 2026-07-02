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
        assertEquals(FieldStatus.COMPLETE, r.byField().get("mutationDiscoverer"));
        assertEquals(FieldStatus.COMPLETE, r.byField().get("mutationInstitution"));

        assertEquals(FieldStatus.MISSING, r.bySection().get("General"));
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
        assertEquals(FieldStatus.COMPLETE, r.bySection().get("General"));
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
        assertEquals(FieldStatus.COMPLETE, r.bySection().get("General"));
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

    /**
     * Build a Set containing one valid Gene. The Marker needs an actual
     * ZDB-ID since GeneStatusComputer now reads {@code mutatedGeneZdbID}
     * (matching the schema's property name) — a default Marker would
     * return null and roll up as MISSING.
     */
    private static Set<Gene> genesWithOne() {
        Gene g = new Gene();
        Marker marker = new Marker();
        marker.setZdbID("ZDB-GENE-TEST-1");
        g.setMutatedGene(marker);
        // genbankGenomicDna is now required per the gene schema.
        g.setGenbankGenomicDna("NC_999999");
        Set<Gene> genes = new HashSet<>();
        genes.add(g);
        return genes;
    }

    @Test
    public void overallIsWorstOfGeneralFields() {
        Mutation m = new Mutation();
        m.setAlleleDesignation("ab123");
        m.setMutagenesisStage("F0");
        // mutagenesisProtocol + mutationType still missing → General = MISSING
        FieldStatusResult r = MutationStatusComputer.compute(m);
        assertEquals(FieldStatus.MISSING, r.bySection().get("General"));
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

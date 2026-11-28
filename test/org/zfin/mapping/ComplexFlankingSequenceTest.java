package org.zfin.mapping;

import org.junit.Before;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.feature.Feature;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.gff.AssemblyEnum;

import java.io.File;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

/**
 * ZFIN-10641: the single flanking-sequence section of a Complex feature, read from the GRCz12tu
 * FASTA. Runs only where that FASTA is available.
 */
public class ComplexFlankingSequenceTest extends AbstractDatabaseTest {

    @Before
    public void fastaAvailable() {
        assumeTrue("GRCz12tu FASTA not available",
            new File(GenomicLocationService.FASTA_BASE_DIR + GenomicLocationService.FASTA_GENOMIC_Z12_FILE).exists());
    }

    @Test
    public void threePointMutationsInGenomicOrder() {
        Feature feature = feature("gl12");
        try (GenomicLocationService service = new GenomicLocationService()) {
            ComplexFlankingSequence sequence = service.computeComplexFlankingSequence(feature);
            assertNotNull(sequence);
            assertEquals("GRCz12tu", sequence.getAssembly());
            List<ComplexFlankingSequence.Segment> segments = sequence.getSegments();
            // upstream, variant, between, variant, between, variant, downstream
            assertEquals(7, segments.size());
            assertSegments(segments, false, true, false, true, false, true, false);

            assertEquals(reference(service, "6", 31663681 - 500, 31663680), segments.get(0).sequence());
            assertEquals(reference(service, "6", 31663682, 31663691), segments.get(2).sequence());
            assertEquals(reference(service, "6", 31663693, 31663700), segments.get(4).sequence());
            assertEquals(reference(service, "6", 31663702, 31663701 + 500), segments.get(6).sequence());
            // a point mutation is reference base / variant base
            assertTrue(segments.get(1).sequence().matches("[ACGT]/[ACGT?]"));
        }
    }

    @Test
    public void deletionThenPointMutation() {
        Feature feature = feature("bns497");
        try (GenomicLocationService service = new GenomicLocationService()) {
            List<ComplexFlankingSequence.Segment> segments = service.computeComplexFlankingSequence(feature).getSegments();
            assertSegments(segments, false, true, false, true, false);
            // the deletion's reference is removed, so the flanks stop either side of it
            assertEquals(reference(service, "22", 16249186 - 500, 16249185), segments.get(0).sequence());
            assertTrue(segments.get(1).sequence().endsWith("/-"));
            assertEquals(reference(service, "22", 16249193, 16249208), segments.get(2).sequence());
            assertEquals(reference(service, "22", 16249210, 16249209 + 500), segments.get(4).sequence());
        }
    }

    @Test
    public void complexFeatureWithoutLocatedVariantsHasNone() {
        // a9: a Complex feature whose only variant has neither a type nor a location
        Feature feature = feature("a9");
        try (GenomicLocationService service = new GenomicLocationService()) {
            assertNull(service.computeComplexFlankingSequence(feature));
        }
    }

    private static Feature feature(String abbreviation) {
        Feature feature = RepositoryFactory.getFeatureRepository().getFeatureByAbbreviation(abbreviation);
        assertNotNull(abbreviation, feature);
        return feature;
    }

    private static String reference(GenomicLocationService service, String chromosome, int start, int end) {
        return new String(service.getReferenceSequence(AssemblyEnum.GRCZ12TU, chromosome, start, end).getBases()).toUpperCase();
    }

    private static void assertSegments(List<ComplexFlankingSequence.Segment> segments, boolean... variation) {
        assertEquals(variation.length, segments.size());
        for (int i = 0; i < variation.length; i++) {
            assertEquals("segment " + i, variation[i], segments.get(i).variation());
        }
    }
}

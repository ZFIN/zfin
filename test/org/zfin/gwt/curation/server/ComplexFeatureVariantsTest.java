package org.zfin.gwt.curation.server;

import org.hibernate.Session;
import org.junit.Assume;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.feature.Feature;
import org.zfin.feature.Variant;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.root.dto.FeatureDTO;
import org.zfin.gwt.root.dto.FeatureTypeEnum;
import org.zfin.gwt.root.dto.MutationDetailTranscriptChangeDTO;
import org.zfin.gwt.root.dto.VariantDTO;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.gwt.root.ui.ValidationException;
import org.zfin.mapping.FeatureLocation;
import org.zfin.mapping.GenomicLocationService;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * ZFIN-10623: the variants of a Complex feature as the feature curation form reads and saves them.
 * Every test is rolled back.
 * <p/>
 * bns497 (ZDB-ALT-230227-6) was curated with ZFIN-10622: Variant 1 a Small Deletion and Variant 2 a
 * Point Mutation, both on chr22 (GRCz12tu), with sequences of reference. Saving those checks the
 * sequences against the GRCz12tu FASTA, so those tests only run where the FASTA is available.
 * s334 (ZDB-ALT-080110-4) is a Complex feature from before variants existed: no variant, no location.
 * Its tests use GRCz10 locations, which are not checked against a FASTA.
 */
public class ComplexFeatureVariantsTest extends AbstractDatabaseTest {

    private static final String BNS497 = "ZDB-ALT-230227-6";
    private static final String S334 = "ZDB-ALT-080110-4";
    // the publication the form saves under (bns497's source)
    private static final String PUBLICATION = "ZDB-PUB-220901-7";

    private final FeatureRPCServiceImpl service = new FeatureRPCServiceImpl();

    @Test
    public void featureDTOCarriesVariants() {
        FeatureDTO dto = DTOConversionService.convertToFeatureDTO(loadFeature(BNS497));

        List<VariantDTO> variants = dto.getVariants();
        assertEquals(2, variants.size());
        assertEquals(FeatureTypeEnum.DELETION, variants.get(0).getVariantType());
        assertEquals(FeatureTypeEnum.POINT_MUTATION, variants.get(1).getVariantType());
        assertEquals("22", variants.get(0).getChromosome());
        assertEquals("GRCz12tu", variants.get(0).getAssembly());
        assertEquals(Integer.valueOf(16249186), variants.get(0).getStartLocation());
        assertEquals(Integer.valueOf(16249209), variants.get(1).getStartLocation());
        assertNotNull(variants.get(1).getEvidence());
        assertEquals("GTCTGGC", variants.get(0).getFgmdChangeDTO().getFgmdSeqRef());
        assertNotNull(variants.get(1).getDnaChangeDTO());
        assertNotNull(variants.get(1).getProteinChangeDTO());
        assertEquals(2, variants.get(0).getTranscriptChangeDTOSet().size());

        // the form edits location and details per variant, not on the feature
        assertNull(dto.getFeatureChromosome());
        assertNull(dto.getFgmdChangeDTO());
        assertNull(dto.getDnaChangeDTO());
    }

    @Test
    public void featureWithoutVariantsHasNone() {
        assertTrue(DTOConversionService.convertToFeatureDTO(loadFeature(S334)).getVariants().isEmpty());
    }

    @Test
    public void savingUnchangedVariantsKeepsThem() throws Exception {
        assumeGRCz12tuFasta();
        Feature feature = loadFeature(BNS497);
        List<String> idsBefore = variantIds(feature);

        service.saveComplexVariants(feature, formDTO(feature));
        Feature reloaded = reload(BNS497);

        assertEquals(idsBefore, variantIds(reloaded));
        FeatureDTO after = DTOConversionService.convertToFeatureDTO(reloaded);
        assertEquals(Integer.valueOf(16249192), after.getVariants().get(0).getEndLocation());
        assertEquals("C", after.getVariants().get(1).getFgmdChangeDTO().getFgmdSeqVar());
        assertEquals(2, after.getVariants().get(0).getTranscriptChangeDTOSet().size());
    }

    @Test
    public void addVariantsToFeatureWithoutVariants() throws Exception {
        Feature feature = loadFeature(S334);
        FeatureDTO dto = formDTO(feature);
        dto.getVariants().add(pointMutation(1000));
        dto.getVariants().add(pointMutationWithDetails(2000));

        service.saveComplexVariants(feature, dto);
        Feature saved = reload(S334);

        assertEquals(2, saved.getVariants().size());
        assertEquals(List.of(1, 2), variantNumbers(saved));
        Variant second = saved.getVariants().last();
        assertEquals(FeatureTypeEnum.POINT_MUTATION, second.getVariantType());
        FeatureLocation location = variantLocation(saved, second);
        assertEquals(Integer.valueOf(2000), location.getStartLocation());
        // a point mutation ends where it starts
        assertEquals(Integer.valueOf(2000), location.getEndLocation());
        assertNotNull(second.getFeatureDnaMutationDetail());
        assertNotNull(second.getFeatureProteinMutationDetail());
        assertFalse(second.getFeatureTranscriptMutationDetailSet().isEmpty());

        FeatureDTO reread = DTOConversionService.convertToFeatureDTO(saved);
        assertEquals("GRCz10", reread.getVariants().get(0).getAssembly());
        assertEquals(Integer.valueOf(1000), reread.getVariants().get(0).getStartLocation());
    }

    @Test
    public void addVariantThenCancelVariantTwo() throws Exception {
        Feature feature = loadFeature(S334);
        FeatureDTO dto = formDTO(feature);
        dto.getVariants().add(pointMutation(1000));
        dto.getVariants().add(pointMutationWithDetails(2000));
        dto.getVariants().add(pointMutation(3000));
        service.saveComplexVariants(feature, dto);
        Feature withThree = reload(S334);
        List<String> ids = variantIds(withThree);
        assertEquals(3, ids.size());
        Variant second = withThree.getVariants().stream().filter(v -> v.getVariantNumber() == 2).findFirst().orElseThrow();
        String secondLocationId = variantLocation(withThree, second).getZdbID();
        String secondDnaId = second.getFeatureDnaMutationDetail().getZdbID();

        // cancel Variant 2: Variant 3 becomes Variant 2
        FeatureDTO withoutTwo = formDTO(withThree);
        withoutTwo.getVariants().remove(1);
        service.saveComplexVariants(withThree, withoutTwo);
        Feature withTwo = reload(S334);

        assertEquals(List.of(ids.get(0), ids.get(2)), variantIds(withTwo));
        assertEquals(List.of(1, 2), variantNumbers(withTwo));
        assertEquals(Integer.valueOf(3000), variantLocation(withTwo, withTwo.getVariants().last()).getStartLocation());
        assertNull(currentSession().get(Variant.class, ids.get(1)));
        assertNull(currentSession().get(FeatureLocation.class, secondLocationId));
        assertNull(currentSession().get(org.zfin.feature.FeatureDnaMutationDetail.class, secondDnaId));
    }

    @Test
    public void removeDetailsAndLocationFromVariant() throws Exception {
        Feature feature = loadFeature(S334);
        FeatureDTO dto = formDTO(feature);
        dto.getVariants().add(pointMutation(1000));
        dto.getVariants().add(pointMutationWithDetails(2000));
        service.saveComplexVariants(feature, dto);
        Feature saved = reload(S334);

        FeatureDTO cleared = formDTO(saved);
        VariantDTO second = cleared.getVariants().get(1);
        second.setChromosome(null);
        second.setAssembly(null);
        second.setStartLocation(null);
        second.setEndLocation(null);
        second.setEvidence(null);
        second.setDnaChangeDTO(null);
        second.setProteinChangeDTO(null);
        second.setTranscriptChangeDTOSet(null);
        service.saveComplexVariants(saved, cleared);
        Feature after = reload(S334);

        Variant variant = after.getVariants().last();
        assertEquals(FeatureTypeEnum.POINT_MUTATION, variant.getVariantType());
        assertNull(variantLocation(after, variant));
        assertNull(variant.getFeatureDnaMutationDetail());
        assertNull(variant.getFeatureProteinMutationDetail());
        assertTrue(variant.getFeatureTranscriptMutationDetailSet().isEmpty());
    }

    @Test
    public void emptyVariantBlocksAreNotSaved() throws Exception {
        Feature feature = loadFeature(S334);
        FeatureDTO dto = formDTO(feature);
        dto.getVariants().add(new VariantDTO());
        dto.getVariants().add(new VariantDTO());

        service.saveComplexVariants(feature, dto);

        assertTrue(reload(S334).getVariants().isEmpty());
    }

    @Test(expected = ValidationException.class)
    public void variantWithoutTypeIsRejected() throws Exception {
        Feature feature = loadFeature(S334);
        FeatureDTO dto = formDTO(feature);
        VariantDTO untyped = new VariantDTO();
        untyped.setChromosome("22");
        dto.getVariants().add(untyped);

        service.saveComplexVariants(feature, dto);
    }

    @Test(expected = ValidationException.class)
    public void complexVariantCannotBeTransgenic() throws Exception {
        Feature feature = loadFeature(S334);
        FeatureDTO dto = formDTO(feature);
        VariantDTO transgenic = pointMutation(1000);
        transgenic.setVariantType(FeatureTypeEnum.TRANSGENIC_INSERTION);
        dto.getVariants().add(transgenic);

        service.saveComplexVariants(feature, dto);
    }

    /** The feature as the edit form holds it: the form sets the publication being curated. */
    private static FeatureDTO formDTO(Feature feature) {
        FeatureDTO dto = DTOConversionService.convertToFeatureDTO(feature);
        dto.setPublicationZdbID(PUBLICATION);
        return dto;
    }

    private static void assumeGRCz12tuFasta() {
        Assume.assumeTrue("GRCz12tu FASTA not available",
                new File(GenomicLocationService.FASTA_BASE_DIR + GenomicLocationService.FASTA_GENOMIC_Z12_FILE + ".fai").exists());
    }

    /** A point mutation on chr22 of GRCz10 (no FASTA check), without mutation details. */
    private static VariantDTO pointMutation(int position) {
        VariantDTO dto = new VariantDTO();
        dto.setVariantType(FeatureTypeEnum.POINT_MUTATION);
        dto.setChromosome("22");
        dto.setAssembly("GRCz10");
        dto.setStartLocation(position);
        dto.setEvidence("IC");
        return dto;
    }

    /** As pointMutation(), with the DNA, protein and transcript details of bns497's point mutation. */
    private static VariantDTO pointMutationWithDetails(int position) {
        VariantDTO bns497PointMutation = DTOConversionService.convertToFeatureDTO(loadFeature(BNS497)).getVariants().get(1);
        VariantDTO dto = pointMutation(position);
        dto.setDnaChangeDTO(bns497PointMutation.getDnaChangeDTO());
        dto.setProteinChangeDTO(bns497PointMutation.getProteinChangeDTO());
        for (MutationDetailTranscriptChangeDTO transcript : bns497PointMutation.getTranscriptChangeDTOSet()) {
            transcript.setZdbID(null);
            dto.getTranscriptChangeDTOSet().add(transcript);
        }
        return dto;
    }

    private static Session currentSession() {
        return HibernateUtil.currentSession();
    }

    private static Feature loadFeature(String zdbID) {
        return currentSession().get(Feature.class, zdbID);
    }

    private static Feature reload(String zdbID) {
        currentSession().flush();
        currentSession().clear();
        return loadFeature(zdbID);
    }

    private static List<String> variantIds(Feature feature) {
        return feature.getVariants().stream().map(Variant::getZdbID).collect(Collectors.toList());
    }

    private static List<Integer> variantNumbers(Feature feature) {
        return feature.getVariants().stream().map(Variant::getVariantNumber).collect(Collectors.toList());
    }

    private static FeatureLocation variantLocation(Feature feature, Variant variant) {
        return currentSession().createQuery(
                        "from FeatureLocation fl where fl.feature = :feature and fl.variant.zdbID = :variantID", FeatureLocation.class)
                .setParameter("feature", feature)
                .setParameter("variantID", variant.getZdbID())
                .uniqueResult();
    }
}

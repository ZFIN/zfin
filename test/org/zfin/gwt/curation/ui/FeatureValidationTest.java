package org.zfin.gwt.curation.ui;

import org.junit.*;
import org.zfin.gwt.root.dto.FeatureDTO;
import org.zfin.gwt.root.dto.FeatureGenomeMutationDetailChangeDTO;
import org.zfin.gwt.root.dto.FeatureTypeEnum;


/**
 */
public class FeatureValidationTest {

    private String testFeatureName = "featureName" ;
    private String testLabPrefix = "b" ;
    private String testLineNumber = "123" ;
    private String testSuffixType = "Et" ;
    private String unspecifiedSuffix = "_unspecified" ;
    private FeatureDTO featureDTO ;
//    private FeatureDTO featureDTO;

    @Before
    public void setup(){
        featureDTO = new FeatureDTO() ;
        featureDTO.setOptionalName(testFeatureName);
        featureDTO.setLabPrefix(testLabPrefix);
        featureDTO.setLineNumber(testLineNumber);
        featureDTO.setTransgenicSuffix(testSuffixType);
        featureDTO.setDominant(false);
        featureDTO.setKnownInsertionSite(false);
    }

    @After
    public void tearDown(){
        featureDTO = null ;
    }

    @Test
    public void pointMutationSmallDeletionUnknown(){
        featureDTO.setFeatureType(FeatureTypeEnum.POINT_MUTATION);
        Assert.assertEquals(testLabPrefix+testLineNumber, FeatureValidationService.generateFeatureDisplayName(featureDTO)) ;
        featureDTO.setName(FeatureValidationService.generateFeatureDisplayName(featureDTO)) ;
        Assert.assertEquals(null,FeatureValidationService.getNameFromFullName(featureDTO)) ;
        Assert.assertEquals(testLabPrefix+testLineNumber,FeatureValidationService.getAbbreviationFromName(featureDTO)) ;
        featureDTO.setDominant(true);
        Assert.assertEquals("d"+testLabPrefix+testLineNumber, FeatureValidationService.generateFeatureDisplayName(featureDTO)) ;
        featureDTO.setName(FeatureValidationService.generateFeatureDisplayName(featureDTO)) ;
        Assert.assertEquals(null,FeatureValidationService.getNameFromFullName(featureDTO)) ;
        Assert.assertEquals("d"+testLabPrefix+testLineNumber,FeatureValidationService.getAbbreviationFromName(featureDTO)) ;
    }

    /**
     * All of these are handled the same with similar interface behavior, so just one test needed.
     */
    @Test
    public void translocationInversionDeficiencyComplexNames(){
        featureDTO.setFeatureType(FeatureTypeEnum.INVERSION);
        Assert.assertEquals(testFeatureName +testLabPrefix+testLineNumber, FeatureValidationService.generateFeatureDisplayName(featureDTO)) ;
        Assert.assertEquals(testLabPrefix+testLineNumber,FeatureValidationService.getAbbreviationFromName(featureDTO)) ;
        featureDTO.setName(FeatureValidationService.generateFeatureDisplayName(featureDTO)) ;
        Assert.assertEquals(testFeatureName,FeatureValidationService.getNameFromFullName(featureDTO)) ;
        Assert.assertEquals(testLabPrefix+testLineNumber,FeatureValidationService.getAbbreviationFromName(featureDTO)) ;
        featureDTO.setDominant(true);
        Assert.assertEquals("d"+testFeatureName +testLabPrefix+testLineNumber, FeatureValidationService.generateFeatureDisplayName(featureDTO)) ;
        featureDTO.setName(FeatureValidationService.generateFeatureDisplayName(featureDTO)) ;
        Assert.assertEquals(testFeatureName,FeatureValidationService.getNameFromFullName(featureDTO)) ;
        Assert.assertEquals("d"+testLabPrefix+testLineNumber,FeatureValidationService.getAbbreviationFromName(featureDTO)) ;
    }

    // test against:  spl: checkFeatureAbbrev
//     define vFeatureLabPrefix like feature_prefix.fp_prefix;
//
//         let vFeatureLabPrefix =
//             (select fp_prefix
//                     from feature_prefix
//                     where vFeatureLabPrefixId = fp_pk_id);
//
//         if (vFeatureUnspecified ='t')
//         then
//            if (vFeatureAbbrev != vFeatureMrkrAbbrev||"_unspecified")
//            then raise exception -746,0,"FAIL!: unspecified allele must have abbrev like _unspecified. checkFeatureAbbrev.";
//            end if;
//         elif (vFeatureUnrecovered ='t')
//         then
//            if (vFeatureAbbrev != vFeatureMrkrAbbrev||"_unrecovered")
//            then raise exception -746,0,"FAIL!: unrecovered allele must have abbrev like _unrecovered. checkFeatureAbbrev.";
//            end if;
//         elif (vFeatureDominant = 't')
//         then
//              if (vFeatureAbbrev not like 'd%')
//              then raise exception -746,0,"FAIL!: dominant allele must have abbrev like d*. checkFeatureAbbrev.";
//              end if;
//         elif (vFeatureType = 'TRANSGENIC_INSERTION' and vFeatureKnownInsertionSite = 'f')
//         then
//              if (vFeatureAbbrev != vFeatureMrkrAbbrev||vFeatureLabPrefix||vFeatureLineNumber)
//              then raise exception -746,0,"FAIL!:tg insert not like construct||labPrefix||lineNumber. checkFeatureAbbrev.";
//              end if;
//         elif (vFeatureType = 'TRANSGENIC_INSERTION' and vFeatureKnownInsertionSite = 't')
//         then
//              if (vFeatureAbbrev != vFeatureLabPrefix||vFeatureLineNumber||vFeatureTgSuffix)
//              then raise exception -746,0,"FAIL!:tg known insert not like labPrefix||lineNumber||tgSuffix checkFeatureAbbrev.";
//              end if;
//         elif (vFeatureType in ('DEFICIENCY','COMPLEX','TRANSLOCATION'))
//         then
//              if (vFeatureAbbrev != vFeatureDfTranslocComplexPrefix||vFeatureLabPrefix||vFeatureLineNumber)
//              then raise exception -746,0,"FAIL!: complex, DF, T must have abbrev like tgprefix||labPrefix||lineNumber. checkFeatureAbbrev.";
//              end if;
//         else
//              if (vFeatureType != vFeatureLabPrefix||vFeatureLineNumber and vFeatureUnspecified = 'f' and vFeatureDominant = 'f' and vFeatureMrkrAbbrev is null and vFeatureKnownInsertionSite = 'f' and vFeatureDfTranslocComplexPrefix is null and vFeatureTgSuffix = 'f')
//              then raise exception -746,0,"FAIL!: feature_abbrev != fPrefix||fLineNumber. checkFeatureAbbrev.";
//              end if;
//         end if;

    /**
     * Half-entered genomic mutation detail. Each case below is the shape of a row that actually
     * reached production and is listed by
     * validatedata/Check-Feature-Mutation-Detail-Missing-Sequences_w.sql.
     */
    private FeatureGenomeMutationDetailChangeDTO mutationDetail(String reference, String variant) {
        FeatureGenomeMutationDetailChangeDTO fgmd = new FeatureGenomeMutationDetailChangeDTO();
        fgmd.setFgmdSeqRef(reference);
        fgmd.setFgmdSeqVar(variant);
        return fgmd;
    }

    /**
     * Supplying a variant sequence already requires a full location, and that check runs first, so
     * the cases below with a variant present have to satisfy it before the sequence-pair check is
     * reached. The affected features all do carry GRCz12tu coordinates in practice.
     */
    private void givenFullLocation() {
        featureDTO.setFeatureChromosome("20");
        featureDTO.setFeatureAssembly("GRCz12tu");
        featureDTO.setFeatureStartLoc(1000);
        featureDTO.setFeatureEndLoc(1010);
        featureDTO.setEvidence("ZDB-TERM-170419-250");
    }

    @Test
    public void pointMutationWithReferenceButNoVariantIsRejected() {
        // ZDB-ALT-220927-6 (zf3482): reference 'G', no variant. This crashed the Alliance export.
        featureDTO.setFeatureType(FeatureTypeEnum.POINT_MUTATION);
        featureDTO.setFgmdChangeDTO(mutationDetail("G", null));
        Assert.assertEquals("Sequence of Variant is required for a "
                        + FeatureTypeEnum.POINT_MUTATION.getDisplay()
                        + " when Sequence of Reference is specified",
                FeatureValidationService.isValidToSave(featureDTO));
    }

    @Test
    public void insertionWithReferenceButNoVariantIsRejected() {
        // ZDB-ALT-260309-2 (el1050): reference 'TC', no variant.
        featureDTO.setFeatureType(FeatureTypeEnum.INSERTION);
        featureDTO.setFgmdChangeDTO(mutationDetail("TC", null));
        Assert.assertEquals("Sequence of Variant is required for a "
                        + FeatureTypeEnum.INSERTION.getDisplay(),
                FeatureValidationService.isValidToSave(featureDTO));
    }

    @Test
    public void indelWithVariantButNoReferenceIsRejected() {
        // ZDB-ALT-250904-2 (cdz5) and six others: variant present, no reference.
        featureDTO.setFeatureType(FeatureTypeEnum.INDEL);
        givenFullLocation();
        featureDTO.setFgmdChangeDTO(mutationDetail("   ", "AGTA"));
        Assert.assertEquals("Sequence of Reference is required for a "
                        + FeatureTypeEnum.INDEL.getDisplay()
                        + " when Sequence of Variant is specified",
                FeatureValidationService.isValidToSave(featureDTO));
    }

    @Test
    public void deletionWithVariantButNoReferenceIsRejected() {
        // ZDB-ALT-220720-3 (w242): variant present, no reference.
        featureDTO.setFeatureType(FeatureTypeEnum.DELETION);
        givenFullLocation();
        featureDTO.setFgmdChangeDTO(mutationDetail(null, "GCAAGCCTATCCCA"));
        Assert.assertEquals("Sequence of Reference is required for a "
                        + FeatureTypeEnum.DELETION.getDisplay(),
                FeatureValidationService.isValidToSave(featureDTO));
    }

    /**
     * A feature that carries no genomic sequences at all is still saveable -- the check only fires
     * once the curator has supplied one of the pair, so it cannot force mutation detail onto
     * features that were never meant to have any.
     */
    @Test
    public void featureWithoutMutationDetailSequencesIsStillSaveable() {
        featureDTO.setFeatureType(FeatureTypeEnum.POINT_MUTATION);
        featureDTO.setFgmdChangeDTO(mutationDetail(null, null));
        Assert.assertNull(FeatureValidationService.isValidToSave(featureDTO));

        featureDTO.setFgmdChangeDTO(null);
        Assert.assertNull(FeatureValidationService.isValidToSave(featureDTO));
    }

}

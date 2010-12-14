package org.zfin.gwt.curation.ui;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.zfin.gwt.root.dto.FeatureDTO;
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

    @Test
    public void transgenicInsertion(){
        featureDTO.setFeatureType(FeatureTypeEnum.TRANSGENIC_INSERTION);
        Assert.assertEquals(testFeatureName + testLabPrefix+testLineNumber, FeatureValidationService.generateFeatureDisplayName(featureDTO)) ;
        Assert.assertEquals(testLabPrefix+testLineNumber,FeatureValidationService.getAbbreviationFromName(featureDTO)) ;
        featureDTO.setName(FeatureValidationService.generateFeatureDisplayName(featureDTO)) ;
        Assert.assertEquals(testFeatureName,FeatureValidationService.getNameFromFullName(featureDTO)) ;
        Assert.assertEquals(testLabPrefix+testLineNumber,FeatureValidationService.getAbbreviationFromName(featureDTO)) ;
        featureDTO.setDominant(true);
        Assert.assertEquals("d"+testFeatureName + testLabPrefix+testLineNumber, FeatureValidationService.generateFeatureDisplayName(featureDTO)) ;
        featureDTO.setName(FeatureValidationService.generateFeatureDisplayName(featureDTO)) ;
        Assert.assertEquals(testFeatureName,FeatureValidationService.getNameFromFullName(featureDTO)) ;
        Assert.assertEquals("d"+testLabPrefix+testLineNumber,FeatureValidationService.getAbbreviationFromName(featureDTO)) ;
        featureDTO.setKnownInsertionSite(true);
        Assert.assertEquals("d"+testLabPrefix+testLineNumber+testSuffixType, FeatureValidationService.generateFeatureDisplayName(featureDTO)) ;
        featureDTO.setName(FeatureValidationService.generateFeatureDisplayName(featureDTO)) ;
        Assert.assertEquals(null,FeatureValidationService.getNameFromFullName(featureDTO)) ;
        Assert.assertEquals("d"+testLabPrefix+testLineNumber+testSuffixType,FeatureValidationService.getAbbreviationFromName(featureDTO)) ;
        featureDTO.setKnownInsertionSite(false);
        featureDTO.setDominant(false);
        featureDTO.setKnownInsertionSite(false);
        featureDTO.setFeatureType(FeatureTypeEnum.UNSPECIFIED);
        Assert.assertEquals(testFeatureName+unspecifiedSuffix, FeatureValidationService.generateFeatureDisplayName(featureDTO)) ;
        Assert.assertEquals(testFeatureName+unspecifiedSuffix,FeatureValidationService.getAbbreviationFromName(featureDTO)) ;
        featureDTO.setName(FeatureValidationService.generateFeatureDisplayName(featureDTO)) ;
        Assert.assertEquals(testFeatureName,FeatureValidationService.getNameFromFullName(featureDTO)) ;
        Assert.assertEquals(testFeatureName+unspecifiedSuffix,FeatureValidationService.getAbbreviationFromName(featureDTO)) ;
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

    @Test
    public void harderTransgenicInsertionProblem(){
        featureDTO.setFeatureType(FeatureTypeEnum.TRANSGENIC_INSERTION);
        testFeatureName = "Tg(-1.0CaTuba1:GFP)ma1234" ;
        featureDTO.setOptionalName(testFeatureName);
        Assert.assertEquals(testFeatureName + testLabPrefix+testLineNumber, FeatureValidationService.generateFeatureDisplayName(featureDTO)) ;
        Assert.assertEquals(testLabPrefix+testLineNumber,FeatureValidationService.getAbbreviationFromName(featureDTO)) ;
        featureDTO.setName(FeatureValidationService.generateFeatureDisplayName(featureDTO)) ;
        Assert.assertEquals(testFeatureName,FeatureValidationService.getNameFromFullName(featureDTO)) ;
        Assert.assertEquals(testLabPrefix+testLineNumber,FeatureValidationService.getAbbreviationFromName(featureDTO)) ;
        featureDTO.setDominant(true);
        Assert.assertEquals("d"+testFeatureName + testLabPrefix+testLineNumber, FeatureValidationService.generateFeatureDisplayName(featureDTO)) ;
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


}

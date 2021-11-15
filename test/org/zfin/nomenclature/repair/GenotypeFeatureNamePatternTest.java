package org.zfin.nomenclature.repair;

import org.junit.Test;

import static org.junit.Assert.*;

public class GenotypeFeatureNamePatternTest {

    @Test
    public void testCanParseFeatureNames() {
        String feature = "tal1<sup>t21384/t21384</sup>";
        GenotypeFeatureName parsed = GenotypeFeatureNamePattern.parseFeatureName(feature);
        assertNotNull(parsed);
        assertEquals(parsed.geneName, "tal1");
        assertEquals(parsed.firstAllele, "t21384");
        assertEquals(parsed.secondAllele, "t21384"); //would this second allele ever be different from the first (except +)
    }

    @Test
    public void testTemplate2() {
        String feature = "gjd1a<sup>fh360/+</sup>";
        GenotypeFeatureName parsed = GenotypeFeatureNamePattern.parseFeatureName(feature);
        assertNotNull(parsed);
        assertEquals(parsed.geneName, "gjd1a");
        assertEquals(parsed.firstAllele, "fh360");
        assertEquals(parsed.secondAllele, null);
        assertEquals(parsed.alleleHeterozygous, true);
    }

    @Test
    public void testTemplate3() {
        String feature = "ufd1l<sup>hi3471Tg/+</sup>";
        GenotypeFeatureName parsed = GenotypeFeatureNamePattern.parseFeatureName(feature);
        assertNotNull(parsed);
        assertEquals(parsed.geneName, "ufd1l");
        assertEquals(parsed.firstAllele, "hi3471");
        assertEquals(parsed.firstAlleleTransGene, "Tg");
        assertEquals(parsed.secondAllele, null);
        assertEquals(parsed.alleleHeterozygous, true);
    }

    @Test
    public void testTemplate4() {
        String feature = "nns6Tg";
        GenotypeFeatureName parsed = GenotypeFeatureNamePattern.parseFeatureName(feature);
        assertNotNull(parsed);
        assertEquals(parsed.geneName, "nns6");
        assertEquals(parsed.transGene, "Tg");
        assertEquals(parsed.firstAllele, null);
        assertEquals(parsed.secondAllele, null);
        assertEquals(parsed.alleleHeterozygous, false);
    }

    @Test
    public void testTemplate5() {
        String feature = "jf5Tg/+";
        GenotypeFeatureName parsed = GenotypeFeatureNamePattern.parseFeatureName(feature);
        assertNotNull(parsed);
        assertEquals(parsed.geneName, "jf5");
        assertEquals(parsed.transGene, "Tg");
        assertEquals(parsed.firstAllele, null);
        assertEquals(parsed.secondAllele, null);
        assertEquals(parsed.geneHeterozygous, true);
    }

    @Test
    public void testTemplate6() {
        String feature = "xu072Tg/xu072Tg";
        GenotypeFeatureName parsed = GenotypeFeatureNamePattern.parseFeatureName(feature);
        assertNotNull(parsed);
        assertEquals(parsed.geneName, "xu072");
        assertEquals(parsed.transGene, "Tg");
        assertEquals(parsed.alleleHeterozygous, false);
    }

    @Test
    public void testTemplate7() {
        String feature = "s1101tEt";
        GenotypeFeatureName parsed = GenotypeFeatureNamePattern.parseFeatureName(feature);
        assertNotNull(parsed);
        assertEquals(parsed.geneName, "s1101t");
        assertEquals(parsed.transGene, "Et");
        assertEquals(parsed.alleleHeterozygous, false);
    }

    //TODO: make this pass
//    @Test
//    public void testWeirdCases() {
//        String[] featuresWithWeirdNames = new String[]{
//            "unm_zf634<sup>zf634/zf634</sup>",
//            "si:ch211-185a18.2<sup>i262/+</sup>",
//            "nksagp22a-12-9aGt",
//            "nkhspGFF62ATg",
//            "nkactc1btdTchrndTg",
//            "alkal2a<sup>t614A/t614A</sup>",
//            "alkal2b<sup>t615B/t615B</sup>",
//            "alkal1<sup>t613A/+</sup>",
//            "alkal2a<sup>t614A/t614A</sup>",
//            "nkhspGFFDMC90AEt",
//            "nkhspGFF62ATg",
//            "nkgsaizGFFD1105AGt",
//            "nkactc1btdTchrndTg",
//            "nkGCaMPHS4aTg",
//            "nkhspGFF62ATg",
//            "alkal1<sup>t613A/t613A</sup>",
//            "alkal2a<sup>t614A/t614A</sup>",
//            "alkal1<sup>t613A/t613A</sup>",
//            "aqp3a<sup>tVE1/tVE1</sup>",
//            "nkhspGFF15ATg",
//            "sqet33mi59BEt",
//            "nkhspGFFDMC28CEt",
//            "sqet33mi59BEt",
//            "nkgsaizGFFD886AGt",
//            "nkgsaizGFFD1105AGt"};
//
//        for(String feature : featuresWithWeirdNames) {
//            GenotypeFeatureName parsed = GenotypeFeatureNamePattern.parseFeatureName(feature);
//            assertNotNull(parsed);
//        }
//    }

}

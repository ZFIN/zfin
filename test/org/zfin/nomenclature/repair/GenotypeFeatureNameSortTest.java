package org.zfin.nomenclature.repair;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.*;

public class GenotypeFeatureNameSortTest {

    @Test
    public void testCanParseFeatureNames() {
        String s1 = "sh144Tg/sh144Tg";
        String s2 = "vhl<sup>hu2117/hu2117</sup>";
        GenotypeFeatureName pattern1 = GenotypeFeatureNamePattern.parseFeatureName(s1);
        GenotypeFeatureName pattern2 = GenotypeFeatureNamePattern.parseFeatureName(s2);
        List<GenotypeFeatureName> input = new ArrayList<GenotypeFeatureName>();
        input.add(pattern1);
        input.add(pattern2);
        input.sort(new GenotypeFeatureNameComparator());
        assertEquals(pattern2, input.get(0));
    }

    @Test
    public void testMultipleOrderings() {
        this.assertReordersTo("zf350Tg ; ubs3Tg",
					            "ubs3Tg; zf350Tg");

        this.assertReordersTo("zf279Et ; csf1ra<sup>j4e1/j4e1</sup> ; gl22Tg ; c264Tg ; csf1rb<sup>re01/+</sup>",
							    "csf1ra<sup>j4e1/j4e1</sup>; csf1rb<sup>re01/+</sup>; c264Tg; gl22Tg; zf279Et");

        this.assertReordersTo("um14Tg ; tet3<sup>mk18/mk18</sup> ; tet2<sup>mk17/mk17</sup>",
								"tet2<sup>mk17/mk17</sup>; tet3<sup>mk18/mk18</sup>; um14Tg");

        this.assertReordersTo("ubs18Tg ; ubs3Tg ; s916Tg ; esama<sup>ubs19/ubs19</sup>",
								"esama<sup>ubs19/ubs19</sup>; s916Tg; ubs18Tg; ubs3Tg");

        this.assertReordersTo("s896Tg ; klf6a<sup>ioz102/ioz102</sup> ; la2Tg",
								"klf6a<sup>ioz102/ioz102</sup>; la2Tg; s896Tg");

        this.assertReordersTo("uwm26Tg ; ubs42Tg",
								"ubs42Tg; uwm26Tg");

        this.assertReordersTo("s1999tTg ; i186Tg ; i149Tg ; gl24Tg",
								"gl24Tg; i149Tg; i186Tg; s1999tTg");

        this.assertReordersTo("fgf13b<sup>mn0094Gt/+</sup> ; gja5b<sup>dtq270</sup>",
								"gja5b<sup>dtq270</sup>; fgf13b<sup>mn0094Gt/+</sup>");

        this.assertReordersTo("s974Tg ; vangl2<sup>m209/m209</sup>",
								"vangl2<sup>m209/m209</sup>; s974Tg");

        this.assertReordersTo("kif5bb<sup>ae24/ae24</sup> ; kif5ba<sup>ae12/ae12</sup> ; ba2Tg",
								"kif5ba<sup>ae12/ae12</sup>; kif5bb<sup>ae24/ae24</sup>; ba2Tg");

        this.assertReordersTo("zf106Tg ; sk86Tg",
								"sk86Tg; zf106Tg");

        this.assertReordersTo("y1Tg ; gipc1<sup>skt1/skt1</sup>",
								"gipc1<sup>skt1/skt1</sup>; y1Tg");

        this.assertReordersTo("twu34Tg ; hey2<sup>hsc25/hsc25</sup>",
								"hey2<sup>hsc25/hsc25</sup>; twu34Tg");
    }

    public void assertReordersTo(String features1, String features2) {
        GenotypeFeatureNameComparator comparator = new GenotypeFeatureNameComparator();

        List<String> inputNames = List.of(features1.split(" ?; ?"));
        List<GenotypeFeatureName> inputFeatureNames = inputNames
                .stream()
                .map(GenotypeFeatureNamePattern::parseFeatureName)
                .collect(toList());

        List<String> outputNames = List.of(features2.split(" ?; ?"));
        List<GenotypeFeatureName> outputFeatureNames = outputNames
                .stream()
                .map(GenotypeFeatureNamePattern::parseFeatureName)
                .collect(toList());

        //sanity check that we're not already ordered
        assertFalse( GenotypeFeatureNameComparator.listsEqual(inputFeatureNames, outputFeatureNames));

        inputFeatureNames.sort(comparator);
        assertTrue(GenotypeFeatureNameComparator.listsEqual(inputFeatureNames, outputFeatureNames));
    }

}

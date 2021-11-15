package org.zfin.nomenclature.repair;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GenotypeNamingIssuesTest {

    @Test
    public void testCanFindTransposedNames() {

        GenotypeNamingIssues namingIssues = new GenotypeNamingIssues();

        assertTrue(namingIssues.canBeTransposedIntoEqualNames("slx4<sup>hg66/hg66</sup> ; ercc4<sup>hg68/+</sup>",
                                                              "ercc4<sup>hg68/+</sup>; slx4<sup>hg66/hg66</sup>"));

        assertTrue(namingIssues.canBeTransposedIntoEqualNames("um14Tg ; fgf3<sup>t26212/t26212</sup>",
                                                              "fgf3<sup>t26212/t26212</sup>; um14Tg"));

        assertFalse(namingIssues.canBeTransposedIntoEqualNames("um14Tg ; fgf3<sup>t26212/t26212</sup>",
                                                              "fgf3<sup>t26212/t26212</sup>; um14Tg; um15Tg"));

        assertTrue(namingIssues.canBeTransposedIntoEqualNames("a;b;c","a;b;c"));
        assertTrue(namingIssues.canBeTransposedIntoEqualNames("a;b;c","b;c;a"));
        assertTrue(namingIssues.canBeTransposedIntoEqualNames("a;b;c","c;b;a"));
        assertFalse(namingIssues.canBeTransposedIntoEqualNames("a;b;c;d","a;b;c"));
        assertFalse(namingIssues.canBeTransposedIntoEqualNames("a;b;c","a;b;c;d"));

    }
}

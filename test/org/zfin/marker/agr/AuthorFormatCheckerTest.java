package org.zfin.marker.agr;

import org.junit.Test;
import org.zfin.marker.agr.AuthorFormatChecker.Category;
import org.zfin.marker.agr.AuthorFormatChecker.Finding;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AuthorFormatCheckerTest {

    private static void assertFinding(String authors, Category category, String author, String suggestion) {
        List<Finding> findings = AuthorFormatChecker.check(authors);
        assertEquals(authors + " -> " + findings, 1, findings.size());
        assertEquals(category, findings.get(0).category());
        assertEquals(author, findings.get(0).author());
        assertEquals(suggestion, findings.get(0).suggestion());
    }

    private static void assertClean(String authors) {
        assertEquals(authors, List.of(), AuthorFormatChecker.check(authors));
    }

    @Test
    public void initialsBeforeSurname() {
        assertFinding("Collins, E.J. and L.S. Ross", Category.INITIALS_FIRST, "L.S. Ross", "Ross, L.S.");
        assertFinding("Pontier, P.J. and N.H. Hart", Category.INITIALS_FIRST, "N.H. Hart", "Hart, N.H.");
        assertFinding("Hsu, T. and F.-Y. Deng", Category.INITIALS_FIRST, "F.-Y. Deng", "Deng, F.-Y.");
    }

    @Test
    public void noCommaAfterSurname() {
        assertFinding("Palmer, F.B., Butler C.A., Timperley, M.H., and Evans, C.W.",
            Category.NO_COMMA, "Butler C.A.", "Butler, C.A.");
        assertFinding("Duan, C., Ding, J., Li, Y., Zhang J., and Royer, T.", Category.NO_COMMA, "Zhang J.", "Zhang, J.");
        assertFinding("Wullimann M.F.", Category.NO_COMMA, "Wullimann M.F.", "Wullimann, M.F.");
        assertFinding("Jowett, T., Amores, A., and Yan Y.-L.", Category.NO_COMMA, "Yan Y.-L.", "Yan, Y.-L.");
    }

    @Test
    public void noCommaNamesSeveralAuthorsInOneRow() {
        List<Finding> findings = AuthorFormatChecker.check("Trimble A.C., Frost, D., and Cooper M.S.");
        assertEquals(2, findings.size());
        assertEquals("Trimble, A.C.", findings.get(0).suggestion());
        assertEquals("Cooper, M.S.", findings.get(1).suggestion());
        assertEquals(Category.NO_COMMA, findings.get(1).category());
    }

    @Test
    public void multiWordSurnameWithoutComma() {
        assertFinding("Pugh, K., Mishoe Hernandez L., Shabdue, C.",
            Category.NO_COMMA, "Mishoe Hernandez L.", "Mishoe Hernandez, L.");
    }

    @Test
    public void periodInsteadOfComma() {
        assertFinding("Collas, P., Husebye, H., and Aleström. P.", Category.PERIOD_FOR_COMMA, "Aleström. P.", "Aleström, P.");
        assertFinding("Yu.S.F. and Hart, N.", Category.PERIOD_FOR_COMMA, "Yu.S.F.", "Yu, S.F.");
        assertFinding("Macherey. M.", Category.PERIOD_FOR_COMMA, "Macherey. M.", "Macherey, M.");
    }

    @Test
    public void junkTrailingPeriods() {
        assertFinding("Waterman.R.E..", Category.PERIOD_FOR_COMMA, "Waterman.R.E..", "Waterman, R.E.");
        assertFinding("Schmidt.H..", Category.PERIOD_FOR_COMMA, "Schmidt.H..", "Schmidt, H.");
    }

    @Test
    public void severalAuthorsInOneEntry() {
        assertFinding("Mabee, P.M., Haendel, M.A., Hilton, E.J, Lundberg, J.G, Mayden, R.L.",
            Category.SEVERAL_IN_ONE, "Hilton, E.J, Lundberg, J.G, Mayden, R.L.", "");
        assertFinding("Spence, R. Fatema, M.K.", Category.SEVERAL_IN_ONE, "Spence, R. Fatema, M.K.", "");
        assertFinding("Park,, D.", Category.SEVERAL_IN_ONE, "Park,, D.", "");
    }

    @Test
    public void unsplittableLists() {
        assertEquals(Category.UNSPLITTABLE, AuthorFormatChecker.check("Qing Ji, Honglu Guo, Anxin Shi").get(0).category());
        assertEquals(Category.UNSPLITTABLE, AuthorFormatChecker.check("Harrison,").get(0).category());
        assertTrue(AuthorFormatChecker.check("Harrison,").get(0).message().contains("ends with a comma"));
    }

    @Test
    public void notSplitBecauseOfParenthesesOrSemicolon() {
        assertFinding("Smith, A.D. (ed), Data, S.P. (ed)", Category.NOT_SPLIT, "Smith, A.D. (ed), Data, S.P. (ed)", "");
        assertEquals(Category.NOT_SPLIT, AuthorFormatChecker.check("Guille, M. (ed.)").get(0).category());
    }

    @Test
    public void validFormsAreNotFlagged() {
        assertClean("Westerfield, M. and Eisen, J.S.");
        assertClean("Krylov, V.V., Osipova, E.A., Pavlova, V.V., and Batrakova, A.A.");
        assertClean("Easter, Jr., S.S.");
        assertClean("Marcus, R.C., Burrill, J.D., and Easter, S.S. Jr.");
        assertClean("Campbell, W.A., IV");
        assertClean("Köster, R.W., von Trotha, J.W.");
        assertClean("Busch-Nentwich, E., Kettleborough, R., Dooley, C. M., Scahill, C.");
        assertClean("Detrich, H.W. III, Westerfield, M., and Zon, L.I., eds.");
        assertClean("Wang, Wanhe");
    }

    @Test
    public void organizationsAreNotFlagged() {
        assertClean("ZIRC and ZFIN Staff");
        assertClean("Anon.");
        assertClean("Sealy, I., ZFIN Staff");
        assertClean("Cold Spring Harbor Laboratory");
    }

    @Test
    public void unclearJunkIsLeftAlone() {
        assertClean("McCarty.");
        assertClean("Whitfield-T.");
    }

    @Test
    public void andIsNeverBlamed() {
        String[] samples = {"Collins, E.J. and L.S. Ross", "Palmer, F.B., Butler C.A., and Evans, C.W.",
            "Collas, P., Husebye, H., and Aleström. P.", "Yu.S.F. and Hart, N."};
        for (String sample : samples) {
            for (Finding finding : AuthorFormatChecker.check(sample)) {
                assertFalse(finding.message(), finding.message().toLowerCase().contains(" and "));
            }
        }
    }

    @Test
    public void blankAndPlaceholdersAreSeparate() {
        for (String s : new String[]{"", "  ", null}) {
            assertTrue(AuthorFormatChecker.isBlank(s));
            assertEquals(List.of(), AuthorFormatChecker.check(s));
        }
        for (String s : new String[]{"none", "[No authors listed]", "No authors listed.", "no author", "No authors",
            "Editor", "Editors", "editor", "N/A", "n/a", "a", "l"}) {
            assertTrue(s, AuthorFormatChecker.isPlaceholder(s));
            assertEquals(s, List.of(), AuthorFormatChecker.check(s));
        }
        assertFalse(AuthorFormatChecker.isPlaceholder("Brand, M."));
        assertFalse(AuthorFormatChecker.isPlaceholder("ZFIN Staff"));
        assertFalse(AuthorFormatChecker.isBlank("none"));
    }
}

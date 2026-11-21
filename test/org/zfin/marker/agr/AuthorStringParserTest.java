package org.zfin.marker.agr;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AuthorStringParserTest {

    private static void assertParsed(String input, String... expected) {
        assertEquals(List.of(expected), AuthorStringParser.parse(input));
    }

    @Test
    public void twoAuthorsWithAnd() {
        assertParsed("Westerfield, M. and Eisen, J.S.", "Westerfield, M.", "Eisen, J.S.");
    }

    @Test
    public void serialCommaAndAnd() {
        assertParsed("Krylov, V.V., Osipova, E.A., Pavlova, V.V., and Batrakova, A.A.",
            "Krylov, V.V.", "Osipova, E.A.", "Pavlova, V.V.", "Batrakova, A.A.");
    }

    @Test
    public void singleAuthor() {
        assertParsed("Yu, X.", "Yu, X.");
    }

    @Test
    public void threeAuthors() {
        assertParsed("Stuart, G.W., McMurray, J.V., and Westerfield, M.",
            "Stuart, G.W.", "McMurray, J.V.", "Westerfield, M.");
    }

    @Test
    public void apostrophe() {
        assertParsed("Khudoley, V.V., Gvil'dis, V.Y., and Pliss, G.B.",
            "Khudoley, V.V.", "Gvil'dis, V.Y.", "Pliss, G.B.");
    }

    @Test
    public void spacedInitialsAndHyphen() {
        assertParsed("Busch-Nentwich, E., Kettleborough, R., Dooley, C. M., Scahill, C.",
            "Busch-Nentwich, E.", "Kettleborough, R.", "Dooley, C. M.", "Scahill, C.");
    }

    @Test
    public void suffixAndEds() {
        assertParsed("Detrich, H.W. III, Westerfield, M., and Zon, L.I., eds.",
            "Detrich, H.W. III", "Westerfield, M.", "Zon, L.I.");
    }

    @Test
    public void trailingJr() {
        assertParsed("Marcus, R.C., Burrill, J.D., and Easter, S.S. Jr.",
            "Marcus, R.C.", "Burrill, J.D.", "Easter, S.S. Jr.");
    }

    @Test
    public void suffixWithTrailingComma() {
        assertParsed("Green, J.E.P. II,", "Green, J.E.P. II");
    }

    @Test
    public void etAlDropped() {
        assertParsed("Gong, Z.Y., et al.", "Gong, Z.Y.");
    }

    @Test
    public void organizationsStayWhole() {
        assertParsed("ZFIN and ZIRC Staff", "ZFIN and ZIRC Staff");
        assertParsed("ZFIN Staff", "ZFIN Staff");
        assertParsed("Zebrafish Nomenclature Committee", "Zebrafish Nomenclature Committee");
        assertParsed("Editor", "Editor");
    }

    @Test
    public void fullNamesStayWhole() {
        assertParsed("Wang, Wanhe", "Wang, Wanhe");
        assertParsed("Miranda, Laura Adriana Gutiérrez", "Miranda, Laura Adriana Gutiérrez");
        assertParsed("Müller, Heidi", "Müller, Heidi");
    }

    @Test
    public void placeholdersAndBlankKeepOriginal() {
        assertParsed("No authors listed.", "No authors listed.");
        assertParsed("[No authors listed]", "[No authors listed]");
        assertParsed("", "");
        assertParsed("  ", "  ");
        assertNull(AuthorStringParser.parse(null).get(0));
    }

    @Test
    public void semicolonOrParenthesesNotSplit() {
        String s = "RNAcentral (1). (1) European Bioinformatics Institute (EMBL-EBI), Hinxton; 2018";
        assertParsed(s, s);
        assertParsed("Smith, J.; Jones, K.", "Smith, J.; Jones, K.");
    }

    @Test
    public void strayPeriodAfterSurname() {
        assertParsed("Sanders, L.H. and Whitlock., K.E.", "Sanders, L.H.", "Whitlock, K.E.");
    }

    @Test
    public void whitespaceNormalized() {
        assertParsed("Westerfield,  M.,\n  Kimmel, C.B.,  and Walker, C.",
            "Westerfield, M.", "Kimmel, C.B.", "Walker, C.");
    }

    @Test
    public void noEmptyOrCommaTerminatedElements() {
        for (String in : List.of("A, B., and C, D.,", "Foo, A.B., , Bar, C.")) {
            for (String name : AuthorStringParser.parse(in)) {
                assertTrue(in + " -> '" + name + "'", !name.isEmpty() && !name.endsWith(","));
            }
        }
    }

    @Test
    public void suffixedSurnameBeforeInitialsIsOneAuthor() {
        assertParsed("Gomez Jr., G.A.", "Gomez Jr., G.A.");
        assertParsed("Gomez Jr., G.A., and Smith, B.", "Gomez Jr., G.A.", "Smith, B.");
    }

    @Test
    public void sloppyCommaSpacing() {
        assertParsed("Imagawa, T.,Kondo, S. and Hiraga, T.", "Imagawa, T.", "Kondo, S.", "Hiraga, T.");
        assertParsed("Hammond, C. L. , Huitema, L. F. A., Vanoevelen, J.",
            "Hammond, C. L.", "Huitema, L. F. A.", "Vanoevelen, J.");
    }

    @Test
    public void andEdsDropped() {
        assertParsed("Wullimann, M.F., Rupp, B., Reichert, H., and eds.",
            "Wullimann, M.F.", "Rupp, B.", "Reichert, H.");
    }

    @Test
    public void etAlWithExtraPeriod() {
        assertParsed("David, N.B., et. al.", "David, N.B.");
    }

    @Test
    public void suffixSeparatedByCommaStaysWithAuthor() {
        assertParsed("Carvan III, M.J., Incardona, J.P., and Rise, M.L.",
            "Carvan III, M.J.", "Incardona, J.P.", "Rise, M.L.");
        assertParsed("Easter, Jr., S.S.", "Easter, Jr., S.S.");
        assertParsed("Campbell, W.A., IV", "Campbell, W.A., IV");
        assertParsed("Rise, M.L. and Campbell, W.A., IV", "Rise, M.L.", "Campbell, W.A., IV");
    }

    // Known limitations: characterization tests. These are data problems curators should fix;
    // changing the parser's behaviour for them should be a deliberate decision.

    @Test
    public void knownLimitation_firstLastListWithoutPeriodsStaysWhole() {
        // Data that curators should fix: "First Last" names with no initials can't be split reliably.
        assertParsed("Qing Ji, Honglu Guo, Anxin Shi, Xingqiang Wang", "Qing Ji, Honglu Guo, Anxin Shi, Xingqiang Wang");
    }

    @Test
    public void knownLimitation_initialsFirstNameKeepsInitialsFirst() {
        // Data that curators should fix: "N.H. Hart" is not in "Last, Initials" order.
        assertParsed("Pontier, P.J. and N.H. Hart", "Pontier, P.J.", "N.H. Hart");
    }

    @Test
    public void knownLimitation_missingPeriodsAfterInitialsUnderSplit() {
        // Data that curators should fix: initials without a final period leave the tail unsplit.
        assertParsed("Mabee, P.M., Haendel, M.A., Arratia, G., Coburn, M.M., Hilton, E.J, Lundberg, J.G, Mayden, R.L.",
            "Mabee, P.M.", "Haendel, M.A.", "Arratia, G.", "Coburn, M.M.",
            "Hilton, E.J, Lundberg, J.G, Mayden, R.L.");
    }

    @Test
    public void knownLimitation_singleCharacterJunkStaysAsIs() {
        // Data that curators should fix: placeholder junk is passed through unchanged, as before.
        assertParsed("l", "l");
    }
}

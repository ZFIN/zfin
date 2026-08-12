package org.zfin.infrastructure.spam;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SpamDetectorTest {

    /**
     * A real submission that got through the previous filter, which only looked at first and last
     * name and needed 10+ characters with 3+ capitals to fire.
     */
    private static SpamAssessment realSpamExample() {
        return SpamDetector.examine()
                .name("firstName", "Uryaehw")
                .name("lastName", "Rrztyds")
                .text("address", "Fljqfedtoj")
                .name("lab", "zbYbqnZkPWsfthidngV")
                .url("url", "https://wmrvushwvduy.com")
                .orcid("orcid", "rPlgedlRSUqMgwEAx")
                .freeText("comments", "fwCkHqJmDxhvjWRxrkfaP")
                .assess();
    }

    @Test
    public void catchesRealSpamExample() {
        SpamAssessment assessment = realSpamExample();
        assertTrue("should be flagged: " + assessment.describe(), assessment.isSpam());
    }

    @Test
    public void explainsWhyItFlaggedSomething() {
        SpamAssessment assessment = realSpamExample();
        assertTrue("reasons should name the offending field: " + assessment.describe(),
                assessment.reasons().stream().anyMatch(reason -> reason.startsWith("lab:")));
    }

    /**
     * The gibberish name alone, with every other field left blank, is what the filter has to catch
     * once a bot stops randomizing the rest of the form.
     */
    @Test
    public void catchesGibberishNameOnItsOwn() {
        SpamAssessment assessment = SpamDetector.examine()
                .name("firstName", "WsEjiJCJYOXBXrWPWNLAwq")
                .name("lastName", "KyliyDPBwGnfnAiVOoKCl")
                .assess();
        assertTrue(assessment.describe(), assessment.isSpam());
    }

    /**
     * The historical case the original filter was written for: a single long mixed case word.
     */
    @Test
    public void catchesMixedCaseWordInAnyField() {
        SpamAssessment assessment = SpamDetector.examine()
                .freeText("comments", "zbYbqnZkPWsfthidngV")
                .assess();
        assertTrue(assessment.describe(), assessment.isSpam());
    }

    @Test
    public void catchesLinkMarkupInComments() {
        SpamAssessment assessment = SpamDetector.examine()
                .freeText("comments", "Great site! <a href=\"http://cheap-pills.example\">click here</a>")
                .assess();
        assertTrue(assessment.describe(), assessment.isSpam());
    }

    @Test
    public void catchesBbCodeLinkInComments() {
        SpamAssessment assessment = SpamDetector.examine()
                .freeText("comments", "[url=http://cheap-pills.example]click here[/url]")
                .assess();
        assertTrue(assessment.describe(), assessment.isSpam());
    }

    // --- submissions that must get through -------------------------------------------------

    @Test
    public void allowsOrdinarySubmission() {
        SpamAssessment assessment = SpamDetector.examine()
                .name("firstName", "Monte")
                .name("lastName", "Westerfield")
                .text("address", "1254 University of Oregon, Eugene, OR 97403")
                .name("lab", "Westerfield Lab")
                .url("url", "https://zfin.org")
                .orcid("orcid", "0000-0002-1825-0097")
                .freeText("comments", "Please add me to the lab roster. Thank you!")
                .assess();
        assertFalse(assessment.describe(), assessment.isSpam());
    }

    /**
     * Our submitters are scientists worldwide, so the heuristics have to leave real names alone.
     * These are the ones most at risk from consonant and vowel counting.
     */
    @Test
    public void allowsRealNames() {
        String[] names = {
                "Krzysztof", "Szczepanski", "Brzezinski", "Strzelecki", "Wojciech", "Zdravko",
                "Schmidt", "Schwartz", "Wright", "Brandt", "Schneider", "Christoph",
                "Nguyen", "Truong", "Xu", "Ng", "Zhang", "Qiang", "Kim", "Park", "Sharma",
                "Mgbeoji", "Nwachukwu", "Adeyemi", "Okonkwo",
                "MacDonald", "McDonald", "O'Brien", "Marie-Claire", "van der Berg", "de la Cruz",
                "Björk", "Müller", "Königsberg", "Skłodowska", "Þórdís", "Ægir",
                "Mary Jo Ann", "JOHN SMITH", "Ph.D.", "Jr.",
        };
        for (String name : names) {
            SpamAssessment assessment = SpamDetector.examine()
                    .name("lastName", name)
                    .assess();
            assertFalse(name + " should not be flagged: " + assessment.describe(), assessment.isSpam());
        }
    }

    @Test
    public void allowsRealInstitutionsAndLabs() {
        String[] institutions = {
                "HHMI", "UCSF", "CNRS", "NIH", "EMBL", "RIKEN", "CSIC", "INSERM",
                "Max Planck Institute for Developmental Biology",
                "Institut de Génomique Fonctionnelle de Lyon",
                "Universität Heidelberg", "Karolinska Institutet",
                "Westerfield Lab", "Nüsslein-Volhard Lab", "Zon Lab",
                "St. Jude Children's Research Hospital",
        };
        for (String institution : institutions) {
            SpamAssessment assessment = SpamDetector.examine()
                    .name("institution", institution)
                    .assess();
            assertFalse(institution + " should not be flagged: " + assessment.describe(), assessment.isSpam());
        }
    }

    /**
     * A single weak signal must never be enough on its own, or one unusual name in an otherwise
     * real submission would silently discard an account request.
     */
    @Test
    public void weakSignalAloneIsNotEnough() {
        SpamAssessment assessment = SpamDetector.examine()
                .name("lastName", "Rrztyds")
                .assess();
        assertTrue("expected a signal to fire", assessment.score() > 0);
        assertFalse(assessment.describe(), assessment.isSpam());
    }

    @Test
    public void missingOrcidIsNotSpamOnItsOwn() {
        SpamAssessment assessment = SpamDetector.examine()
                .name("firstName", "Monte")
                .name("lastName", "Westerfield")
                .orcid("orcid", "")
                .assess();
        assertFalse(assessment.describe(), assessment.isSpam());
        assertTrue("blank ORCID is a validation matter, not a spam signal", assessment.reasons().isEmpty());
    }

    @Test
    public void orcidPlaceholderIsNotASpamSignal() {
        for (String placeholder : new String[]{"N/A", "n/a", "none", "-", "?"}) {
            SpamAssessment assessment = SpamDetector.examine().orcid("orcid", placeholder).assess();
            assertTrue(placeholder + " should not raise a signal: " + assessment.describe(),
                    assessment.reasons().isEmpty());
        }
    }

    @Test
    public void acceptsOrcidInTheFormsPeopleSubmitIt() {
        for (String orcid : new String[]{
                "0000-0002-1825-0097",
                "0000000218250097",
                "https://orcid.org/0000-0002-1825-0097",
                "orcid.org/0000-0002-1825-0097",
                "  0000-0002-1825-0097  ",
                "0000-0002-1694-233X"}) {
            SpamAssessment assessment = SpamDetector.examine().orcid("orcid", orcid).assess();
            assertTrue(orcid + " should be accepted: " + assessment.describe(),
                    assessment.reasons().isEmpty());
        }
    }

    @Test
    public void flagsGibberishOrcid() {
        SpamAssessment assessment = SpamDetector.examine().orcid("orcid", "rPlgedlRSUqMgwEAx").assess();
        assertTrue("a random string in the ORCID field is conclusive: " + assessment.describe(),
                assessment.isSpam());
    }

    /**
     * Now that ORCID is required, a submitter who fumbles it must reach the validation error rather
     * than be silently discarded, so a mistyped ORCID cannot add up to spam by itself.
     */
    @Test
    public void mistypedOrcidIsNotSpam() {
        for (String mistyped : new String[]{
                "0000-0002-182-0097",
                "0000-0002-1825-009",
                "0000 0002 1825 00977",
                "0000-0002-1825-0097-",
                "my orcid is 0000-0002-1825-0097"}) {
            SpamAssessment assessment = SpamDetector.examine()
                    .name("firstName", "Monte")
                    .name("lastName", "Westerfield")
                    .orcid("orcid", mistyped)
                    .assess();
            assertFalse(mistyped + " should reach validation, not be discarded: " + assessment.describe(),
                    assessment.isSpam());
        }
    }

    /**
     * A pasted link in the comments is a weak signal, not a discard on its own: submitters do
     * sometimes point us at their lab page.
     */
    @Test
    public void allowsCommentsWithALabLink() {
        SpamAssessment assessment = SpamDetector.examine()
                .name("firstName", "Monte")
                .name("lastName", "Westerfield")
                .freeText("comments", "My lab page is https://zfin.org/lab - please link it. Thanks!")
                .assess();
        assertFalse(assessment.describe(), assessment.isSpam());
    }

    @Test
    public void allowsAnOrdinaryLabWebsite() {
        SpamAssessment assessment = SpamDetector.examine()
                .url("url", "https://smithlab.uoregon.edu/research/index.html")
                .assess();
        assertFalse(assessment.describe(), assessment.isSpam());
    }

    @Test
    public void handlesBlankAndNullFields() {
        SpamAssessment assessment = SpamDetector.examine()
                .name("firstName", null)
                .name("lastName", "")
                .text("address", "   ")
                .url("url", null)
                .orcid("orcid", null)
                .freeText("comments", null)
                .assess();
        assertFalse(assessment.describe(), assessment.isSpam());
        assertTrue(assessment.reasons().isEmpty());
    }
}

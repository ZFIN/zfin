package org.zfin.marker.presentation;

import org.apache.commons.lang3.ObjectUtils;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.DBLink;
import org.zfin.sequence.ReferenceDatabase;
import org.zfin.sequence.repository.SequenceRepository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class DbLinkDisplayComparatorTest extends AbstractDatabaseTest {

    private SequenceRepository sequenceRepository = RepositoryFactory.getSequenceRepository();

    @Test
    public void testIntegerComparison() {
        assertEquals(-1, new Integer(1).compareTo(2));
        assertEquals(1, new Integer(1).compareTo(0));
        assertEquals(0, new Integer(1).compareTo(1));
        assertEquals(1, ObjectUtils.compare(1, null));
        assertEquals(-1, ObjectUtils.compare(null, 1));
        assertEquals(0, ObjectUtils.compare(null, null));
    }

    @Test
    public void testOrderByAccessionNumber() {
        // these links are from the same foreign db and have the same length, so they
        // should be sorted by accession number.
        DBLink link1 = sequenceRepository.getDBLinkByID("ZDB-DBLINK-140923-179491"); // accession number O42278
        DBLink link2 = sequenceRepository.getDBLinkByID("ZDB-DBLINK-140923-179494"); // accession number Q5PRC3
        assertThat("DBLinks should be sorted alphabetically by accession number", link1, orderedBefore(link2));
    }

    @Test
    public void testOrderBySequenceLength() {
        // these links are from the same foreign db but have different lengths
        DBLink link1 = sequenceRepository.getDBLinkByID("ZDB-DBLINK-140923-179490");
        DBLink link2 = sequenceRepository.getDBLinkByID("ZDB-DBLINK-140923-179491");
        assertThat("DBLinks should be sorted by sequence length", link1, orderedAfter(link2));
    }

    @Test
    public void testSameDbLink() {
        // these links are from the same foreign db but have different lengths
        DBLink link = sequenceRepository.getDBLinkByID("ZDB-DBLINK-140923-179491");
        assertThat("Comparator result with DBLink should be 0", link, orderedSameAs(link));
    }

    @Test
    public void testNMBeforeXM() {
        DBLink link1 = sequenceRepository.getDBLink("ZDB-GENE-990415-200", "NM_131304", "RefSeq");
        DBLink link2 = sequenceRepository.getDBLink("ZDB-GENE-990415-200", "XM_009297881", "RefSeq");
        assertThat("NM DBLinks should be sorted before XM links", link1, orderedBefore(link2));

    }

    @Test
    public void testXMBeforeGenbank() {
        DBLink link1 = sequenceRepository.getDBLink("ZDB-GENE-001030-3", "AF072549", "GenBank");
        DBLink link2 = sequenceRepository.getDBLink("ZDB-GENE-001030-3", "XM_009299774", "RefSeq");
        assertThat("XM DBLinks should be sorted before Genbank links", link1, orderedAfter(link2));
    }

    @Test
    public void testRNABeforeGenomic() {
        DBLink link1 = sequenceRepository.getDBLink("ZDB-GENE-001030-3", "AF072549", "GenBank");
        DBLink link2 = sequenceRepository.getDBLink("ZDB-GENE-001030-3", "CABZ01094912", "GenBank");
        assertThat("RNA DBLinks should be sorted before Genomic links", link1, orderedBefore(link2));
    }



    private static Matcher<DBLink> orderedBefore(final DBLink other) {
        return new DBLinkMatcher(other, "before", "ordered after") {
            @Override
            protected boolean matchesSafely(DBLink link) {
                return comparator.compare(link, other) < 0;
            }
        };
    }

    private static Matcher<DBLink> orderedAfter(final DBLink other) {
        return new DBLinkMatcher(other, "after", "ordered before") {
            @Override
            protected boolean matchesSafely(DBLink link) {
                return comparator.compare(link, other) > 0;
            }
        };
    }

    private static Matcher<DBLink> orderedSameAs(final DBLink other) {
        return new DBLinkMatcher(other, "same as", "not ordered same as") {
            @Override
            protected boolean matchesSafely(DBLink link) {
                return comparator.compare(link, other) == 0;
            }
        };
    }

    private abstract static class DBLinkMatcher extends TypeSafeMatcher<DBLink> {

        private DBLink other;
        private String descriptionText;
        private String mismatchText;

        protected static DbLinkDisplayComparator comparator = new DbLinkDisplayComparator();

        public DBLinkMatcher(DBLink other, String descriptionText, String mismatchText) {
            this.other = other;
            this.descriptionText = descriptionText;
            this.mismatchText = mismatchText;
        }

        @Override
        protected void describeMismatchSafely(DBLink link, Description mismatchDescription) {
            mismatchDescription.appendText(formatDBLink(link))
                    .appendText(" was ")
                    .appendText(mismatchText)
                    .appendText(" ")
                    .appendText(formatDBLink(other));
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("A DBLink ordered ")
                    .appendText(descriptionText)
                    .appendText(" ")
                    .appendText(other.getZdbID());
        }

        protected static String formatDBLink(DBLink link) {
            ReferenceDatabase refDb = link.getReferenceDatabase();
            return String.format("%s [%s; %s; %s; %d]",
                    link.getZdbID(),
                    refDb.getForeignDBDataType().getDataType(),
                    refDb.getForeignDB().getDisplayName(),
                    link.getAccessionNumber(),
                    link.getLength());
        }

    }
}

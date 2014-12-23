package org.zfin.marker.presentation;

import org.apache.commons.lang3.ObjectUtils;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.BeforeClass;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.*;
import org.zfin.sequence.repository.SequenceRepository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class DbLinkDisplayComparatorTest {

    private static ReferenceDatabase uniprotDatabase;
    private static ReferenceDatabase refseqDatabase;
    private static ReferenceDatabase genbankRNADatabase;
    private static ReferenceDatabase genbankGenomicDatabase;
    private static DBLink dbLink1;
    private static DBLink dbLink2;

    @BeforeClass
    public static void setupClasses() {
        ForeignDB uniprotDB = new ForeignDB();
        uniprotDB.setDbID(40L);
        uniprotDB.setDbName(ForeignDB.AvailableName.UNIPROTKB);
        uniprotDB.setSignificance(0);
        uniprotDB.setDisplayName("MockUniProtKB");

        ForeignDB refseqDB = new ForeignDB();
        refseqDB.setDbID(32L);
        refseqDB.setDbName(ForeignDB.AvailableName.REFSEQ);
        refseqDB.setSignificance(1);
        refseqDB.setDisplayName("MockRefSeq");

        ForeignDB genbankDB = new ForeignDB();
        genbankDB.setDbID(15L);
        genbankDB.setDbName(ForeignDB.AvailableName.GENBANK);
        genbankDB.setSignificance(2);
        genbankDB.setDisplayName("MockGenBank");

        ForeignDBDataType polypeptideType = new ForeignDBDataType();
        polypeptideType.setSuperType(ForeignDBDataType.SuperType.SEQUENCE);
        polypeptideType.setDataType(ForeignDBDataType.DataType.POLYPEPTIDE);
        polypeptideType.setDisplayOrder(3);
        polypeptideType.setDataTypeID(2L);

        ForeignDBDataType rnaSequenceType = new ForeignDBDataType();
        rnaSequenceType.setSuperType(ForeignDBDataType.SuperType.SEQUENCE);
        rnaSequenceType.setDataType(ForeignDBDataType.DataType.RNA);
        rnaSequenceType.setDisplayOrder(1);
        rnaSequenceType.setDataTypeID(3L);

        ForeignDBDataType genomicType = new ForeignDBDataType();
        genomicType.setSuperType(ForeignDBDataType.SuperType.SEQUENCE);
        genomicType.setDataType(ForeignDBDataType.DataType.GENOMIC);
        genomicType.setDisplayOrder(2);
        genomicType.setDataTypeID(1L);

        uniprotDatabase = new ReferenceDatabase();
        uniprotDatabase.setZdbID("ZDB-FDBCONT-MOCK-1");
        uniprotDatabase.setForeignDB(uniprotDB);
        uniprotDatabase.setForeignDBDataType(polypeptideType);
        uniprotDatabase.setOrganism("Zebrafish");

        refseqDatabase = new ReferenceDatabase();
        // cannot use MOCK or else ReferenceDatabase.isRefSeq() will not work
        refseqDatabase.setZdbID("ZDB-FDBCONT-040412-38");
        refseqDatabase.setForeignDB(refseqDB);
        refseqDatabase.setForeignDBDataType(rnaSequenceType);
        refseqDatabase.setOrganism("Zebrafish");

        genbankRNADatabase = new ReferenceDatabase();
        genbankRNADatabase.setZdbID("ZDB-FDBCONT-MOCK-3");
        genbankRNADatabase.setForeignDB(genbankDB);
        genbankRNADatabase.setForeignDBDataType(rnaSequenceType);
        genbankRNADatabase.setOrganism("Zebrafish");

        genbankGenomicDatabase = new ReferenceDatabase();
        genbankGenomicDatabase.setZdbID("ZDB-FDBCONT-MOCK-4");
        genbankGenomicDatabase.setForeignDB(genbankDB);
        genbankGenomicDatabase.setForeignDBDataType(genomicType);
        genbankGenomicDatabase.setOrganism("Zebrafish");

        dbLink1 = new MarkerDBLink();
        dbLink1.setZdbID("ZDB-DBLINK-MOCK-1");
        dbLink1.setReferenceDatabase(uniprotDatabase);
        dbLink1.setLength(0);
        dbLink1.setAccessionNumber("");

        dbLink2 = new MarkerDBLink();
        dbLink2.setZdbID("ZDB-DBLINK-MOCK-2");
        dbLink2.setReferenceDatabase(uniprotDatabase);
        dbLink2.setLength(0);
        dbLink2.setAccessionNumber("");
    }

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
        dbLink1.setAccessionNumber("OQP423");
        dbLink1.setLength(200);
        dbLink1.setReferenceDatabase(uniprotDatabase);

        dbLink2.setAccessionNumber("ORP423");
        dbLink2.setLength(200);
        dbLink2.setReferenceDatabase(uniprotDatabase);

        assertThat("DBLinks should be sorted alphabetically by accession number", dbLink1, orderedBefore(dbLink2));
    }

    @Test
    public void testOrderBySequenceLength() {
        // these links are from the same foreign db but have different lengths
        dbLink1.setAccessionNumber("AAA");
        dbLink1.setLength(190);
        dbLink1.setReferenceDatabase(uniprotDatabase);

        dbLink2.setAccessionNumber("AAA");
        dbLink2.setLength(200);
        dbLink2.setReferenceDatabase(uniprotDatabase);

        assertThat("DBLinks should be sorted by sequence length", dbLink1, orderedAfter(dbLink2));
    }

    @Test
    public void testSameDbLink() {
        assertThat(dbLink1, orderedSameAs(dbLink1));
    }

    @Test
    public void testNMBeforeXM() {
        dbLink1.setAccessionNumber("NM_131304");
        dbLink1.setLength(2809);
        dbLink1.setReferenceDatabase(refseqDatabase);

        dbLink2.setAccessionNumber("XM_009297881");
        dbLink2.setLength(2937);
        dbLink2.setReferenceDatabase(refseqDatabase);

        assertThat("NM DBLinks should be sorted before XM links", dbLink1, orderedBefore(dbLink2));

    }

    @Test
    public void testXMBeforeGenbank() {
        dbLink1.setAccessionNumber("AF072549");
        dbLink1.setLength(1290);
        dbLink1.setReferenceDatabase(genbankRNADatabase);

        dbLink2.setAccessionNumber("XM_009299774");
        dbLink2.setLength(1287);
        dbLink2.setReferenceDatabase(refseqDatabase);

        assertThat("XM DBLinks should be sorted before Genbank links", dbLink1, orderedAfter(dbLink2));
    }

    @Test
    public void testRNABeforeGenomic() {
        dbLink1.setAccessionNumber("AF072549");
        dbLink1.setLength(1290);
        dbLink1.setReferenceDatabase(genbankRNADatabase);

        dbLink2.setAccessionNumber("CABZ01094912");
        dbLink2.setLength(5571);
        dbLink2.setReferenceDatabase(genbankGenomicDatabase);

        assertThat("RNA DBLinks should be sorted before Genomic links", dbLink1, orderedBefore(dbLink2));
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

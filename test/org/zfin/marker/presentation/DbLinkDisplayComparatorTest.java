package org.zfin.marker.presentation;

import org.apache.commons.lang3.ObjectUtils;
import org.junit.Test;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.DBLink;
import org.zfin.sequence.repository.SequenceRepository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;

public class DbLinkDisplayComparatorTest {

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
        DbLinkDisplayComparator comparator = new DbLinkDisplayComparator();
        // these links are from the same foreign db and have the same length, so they
        // should be sorted by accession number.
        DBLink link1 = sequenceRepository.getDBLinkByID("ZDB-DBLINK-140923-179491");
        DBLink link2 = sequenceRepository.getDBLinkByID("ZDB-DBLINK-140923-179494");
        assertThat("DBLinks should be sorted alphabetically by accession number",
                comparator.compare(link1, link2), lessThan(0));
    }

    @Test
    public void testOrderBySequenceLength() {
        DbLinkDisplayComparator comparator = new DbLinkDisplayComparator();
        // these links are from the same foreign db but have different lengths
        DBLink link1 = sequenceRepository.getDBLinkByID("ZDB-DBLINK-140923-179491");
        DBLink link2 = sequenceRepository.getDBLinkByID("ZDB-DBLINK-140923-179490");
        assertThat("DBLinks should be sorted by sequence length",
                comparator.compare(link1, link2), greaterThan(1));

        link1 = sequenceRepository.getDBLinkByID("ZDB-DBLINK-141114-113370");
        link2 = sequenceRepository.getDBLinkByID("ZDB-DBLINK-141114-113368");
        assertThat("DBLinks should be sorted by sequence length",
                comparator.compare(link1, link2), lessThan(1));
    }

    @Test
    public void testSameDbLink() {
        DbLinkDisplayComparator comparator = new DbLinkDisplayComparator();
        // these links are from the same foreign db but have different lengths
        DBLink link = sequenceRepository.getDBLinkByID("ZDB-DBLINK-140923-179491");
        assertThat("Comparator result with DBLink should be 0",
                comparator.compare(link, link), is(0));
    }

    @Test
    public void testNMBeforeXM() {
        DbLinkDisplayComparator comparator = new DbLinkDisplayComparator();
        DBLink link1 = sequenceRepository.getDBLinkByID("ZDB-DBLINK-141114-113248");
        DBLink link2 = sequenceRepository.getDBLinkByID("ZDB-DBLINK-141114-120150");
        assertThat("NM DBLinks should be sorted before XM links",
                comparator.compare(link1, link2), lessThan(0));
    }

    @Test
    public void testXMBeforeGenbank() {
        DbLinkDisplayComparator comparator = new DbLinkDisplayComparator();
        DBLink link1 = sequenceRepository.getDBLinkByID("ZDB-DBLINK-030708-16");
        DBLink link2 = sequenceRepository.getDBLinkByID("ZDB-DBLINK-141114-136242");
        assertThat("XM DBLinks should be sorted before Genbank links",
                comparator.compare(link1, link2), greaterThan(0));
    }

    @Test
    public void testRNABeforeGenomic() {
        DbLinkDisplayComparator comparator = new DbLinkDisplayComparator();
        DBLink link1 = sequenceRepository.getDBLinkByID("ZDB-DBLINK-141114-47180");
        DBLink link2 = sequenceRepository.getDBLinkByID("ZDB-DBLINK-141114-101531");
        assertThat("RNA DBLinks should be sorted before Genomic links",
                comparator.compare(link1, link2), lessThan(0));
    }
}

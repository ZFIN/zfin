package org.zfin.publication.presentation;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.zfin.publication.Publication;

import java.util.List;
import java.util.GregorianCalendar;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Test class fot eh publication sorting comparator.
 */
public class SortPublicationResultsTest extends TestCase {

    private List<Publication> publications;

    public static void main(String args[]) {
        TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(SortPublicationResultsTest.class);
    }

    protected void setUp() throws Exception {
        super.setUp();
        createPublications();
    }

    /**
     * Oldest publication first, most recent pub last.
     */
    public void testDateSortingAscending(){
        boolean ascending = true;
        Collections.sort(publications, new SortPublicationResults("date", ascending));
        Publication first = publications.get(0);
        Publication second = publications.get(1);
        Publication third = publications.get(2);
        Publication four = publications.get(3);
        assertEquals("First Publication", "ZDB-PUB-950513-3", first.getZdbID());
        assertEquals("Second Publication", "ZDB-PUB-000220-1", second.getZdbID());
        assertEquals("Third Publication", "ZDB-PUB-020930-34", third.getZdbID());
        assertEquals("Four Publication", "ZDB-PUB-051203-1", four.getZdbID());
    }

    /**
     * Most recent publication first oldest publication last.
     */
    public void testDateSortingDescending(){
        boolean ascending = false;
        Collections.sort(publications, new SortPublicationResults("date", ascending));
        Publication first = publications.get(0);
        Publication second = publications.get(1);
        Publication third = publications.get(2);
        Publication four = publications.get(3);
        assertEquals("First Publication", "ZDB-PUB-051203-1", first.getZdbID());
        assertEquals("Second Publication", "ZDB-PUB-020930-34", second.getZdbID());
        assertEquals("Third Publication", "ZDB-PUB-000220-1", third.getZdbID());
        assertEquals("Four Publication", "ZDB-PUB-950513-3", four.getZdbID());
    }

    private void createPublications(){
        Publication pubOne = new Publication();
        pubOne.setZdbID("ZDB-PUB-051203-1");
        pubOne.setTitle("Evolutionary Theory of Zebrafish and Human");
        pubOne.setPublicationDate(new GregorianCalendar(2005, 12, 3));

        Publication pubTwo = new Publication();
        pubTwo.setZdbID("ZDB-PUB-000220-1");
        pubTwo.setTitle("Phylogenetic Tree");
        pubTwo.setPublicationDate(new GregorianCalendar(2000, 2, 20));

        Publication pubThree = new Publication();
        pubThree.setZdbID("ZDB-PUB-950513-3");
        pubThree.setTitle("Phylogenetic Tree");
        pubThree.setPublicationDate(new GregorianCalendar(1995, 5, 13));

        Publication pubFour = new Publication();
        pubFour.setZdbID("ZDB-PUB-020930-34");
        pubFour.setTitle("Phylogenetic Tree");
        pubFour.setPublicationDate(new GregorianCalendar(2002, 9, 30));

        publications = new ArrayList<Publication>(5);
        publications.add(pubOne);
        publications.add(pubTwo);
        publications.add(pubThree);
        publications.add(pubFour);

    }

}

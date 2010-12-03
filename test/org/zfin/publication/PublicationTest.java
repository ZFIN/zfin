package org.zfin.publication;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

import static junit.framework.Assert.assertEquals;

public class PublicationTest {

    @Test
    public void comparePublicationByDate() {
        Publication two = new Publication();
        two.setPublicationDate(new GregorianCalendar(1999, 5, 1));
        String authorTwo = "Brand, M. and Granato, M.";
        two.setAuthors(authorTwo);

        Publication three = new Publication();
        three.setPublicationDate(new GregorianCalendar(2000, 1, 17));
        String authorThree = "Cameron, D.A. and Carney, L.H.";
        three.setAuthors(authorThree);

        Publication one = new Publication();
        one.setPublicationDate(new GregorianCalendar(1999, 11, 1));
        String authorOne = "Michel, W.C.";
        one.setAuthors(authorOne);

        Publication four = new Publication();
        four.setPublicationDate(new GregorianCalendar(1999, 1, 1));
        String authorFour = "Wall, S.B.";
        four.setAuthors(authorFour);

        List<Publication> pubs = new ArrayList<Publication>(4);
        pubs.add(one);
        pubs.add(two);
        pubs.add(three);
        pubs.add(four);

        Collections.sort(pubs);
        assertEquals(authorThree, pubs.get(0).getAuthors());
        assertEquals(authorTwo, pubs.get(1).getAuthors());
        assertEquals(authorOne, pubs.get(2).getAuthors());
        assertEquals(authorFour, pubs.get(3).getAuthors());
    }
}

package org.zfin.ontology;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * Testing the correct sorting of many terms.
 */
public class GenericTermTest {

    @Test
    public void sortCaseInsensitive() {
        Term termOne = createTermByName("cerebellum");
        Term termTwo = createTermByName("Purkinje Cell");

        List<Term> terms = new ArrayList<Term>(2);
        terms.add(termTwo);
        terms.add(termOne);
        Collections.sort(terms);
        assertEquals("cerebellum", terms.get(0).getTermName());
        assertEquals("Purkinje Cell", terms.get(1).getTermName());
    }

    @Test
    public void sortNumerically() {
        Term termOne = createTermByName("somite1");
        Term termTwo = createTermByName("somite20");
        Term termThree = createTermByName("somite10");
        Term termFour = createTermByName("somite2");

        List<Term> terms = new ArrayList<Term>(3);
        terms.add(termOne);
        terms.add(termTwo);
        terms.add(termThree);
        terms.add(termFour);
        Collections.sort(terms);
        assertEquals("somite1", terms.get(0).getTermName());
        assertEquals("somite2", terms.get(1).getTermName());
        assertEquals("somite10", terms.get(2).getTermName());
        assertEquals("somite20", terms.get(3).getTermName());
    }

    @Test
    public void sortNumericallyDouble() {
        Term termOne = createTermByName("somite1hh70");
        Term termTwo = createTermByName("somite20hh10");
        Term termThree = createTermByName("somite10hh9");
        Term termFour = createTermByName("somite2");
        Term termFive = createTermByName("somite10hh23");

        List<Term> terms = new ArrayList<Term>(3);
        terms.add(termOne);
        terms.add(termTwo);
        terms.add(termThree);
        terms.add(termFour);
        terms.add(termFive);
        Collections.sort(terms);
        assertEquals("somite1hh70", terms.get(0).getTermName());
        assertEquals("somite2", terms.get(1).getTermName());
        assertEquals("somite10hh9", terms.get(2).getTermName());
        assertEquals("somite10hh23", terms.get(3).getTermName());
        assertEquals("somite20hh10", terms.get(4).getTermName());
    }

    private Term createTermByName(String name) {
        Term termOne = new GenericTerm();
        termOne.setTermName(name);
        return termOne;
    }

}

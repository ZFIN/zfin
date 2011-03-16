package org.zfin.gwt.root.dto;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * Data Transfer Object for a composed term with expressed-in boolean.
 */
public class ExpressedTermDTOTest {

    @Test
    public void sortSingleItemList() {
        ExpressedTermDTO one = new ExpressedTermDTO();
        TermDTO dtoOne = new TermDTO();
        dtoOne.setName("secondary motor neuron");
        one.setSuperterm(dtoOne);

        List<ExpressedTermDTO> terms = new ArrayList<ExpressedTermDTO>(1);
        terms.add(one);

        assertEquals("secondary motor neuron", terms.get(0).getSuperterm().getName());
    }

    @Test
    public void sortWithoutSubtermsList() {
        ExpressedTermDTO two = new ExpressedTermDTO();
        TermDTO dtoTwo = new TermDTO();
        dtoTwo.setName("primary motor neuron");
        two.setSuperterm(dtoTwo);
        ExpressedTermDTO one = new ExpressedTermDTO();
        TermDTO dtoOne = new TermDTO();
        dtoOne.setName("secondary motor neuron");
        one.setSuperterm(dtoOne);

        List<ExpressedTermDTO> terms = new ArrayList<ExpressedTermDTO>(2);
        terms.add(one);
        terms.add(two);

        Collections.sort(terms);

        assertEquals("primary motor neuron", terms.get(0).getSuperterm().getName());
        assertEquals("secondary motor neuron", terms.get(1).getSuperterm().getName());
    }

    @Test
    public void sortWithOneSubterm() {
        ExpressedTermDTO two = new ExpressedTermDTO();
        TermDTO dtoTwo = new TermDTO();
        dtoTwo.setName("primary motor neuron");
        two.setSuperterm(dtoTwo);
        TermDTO dtoTwoSub = new TermDTO();
        dtoTwoSub.setName("cell some memberane");
        two.setSubterm(dtoTwoSub);
        ExpressedTermDTO one = new ExpressedTermDTO();
        TermDTO dtoOneSuper = new TermDTO();
        dtoOneSuper.setName("secondary motor neuron");
        one.setSuperterm(dtoOneSuper);

        List<ExpressedTermDTO> terms = new ArrayList<ExpressedTermDTO>(2);
        terms.add(one);
        terms.add(two);

        Collections.sort(terms);

        assertEquals("primary motor neuron", terms.get(0).getSuperterm().getName());
        assertEquals("secondary motor neuron", terms.get(1).getSuperterm().getName());
    }

    @Test
    public void sortWithOneSubtermSameSuperterms() {
        ExpressedTermDTO two = new ExpressedTermDTO();
        TermDTO dtoTwo = new TermDTO();
        dtoTwo.setName("primary motor neuron");
        two.setSuperterm(dtoTwo);
        TermDTO dtoTwoSub = new TermDTO();
        dtoTwoSub.setName("cell some memberane");
        two.setSubterm(dtoTwoSub);
        ExpressedTermDTO one = new ExpressedTermDTO();
        TermDTO dtoOneSuper = new TermDTO();
        dtoOneSuper.setName("primary motor neuron");
        one.setSuperterm(dtoOneSuper);

        List<ExpressedTermDTO> terms = new ArrayList<ExpressedTermDTO>(2);
        terms.add(one);
        terms.add(two);

        Collections.sort(terms);

        assertEquals("primary motor neuron", terms.get(0).getSuperterm().getName());
        assertEquals("cell some membrane", terms.get(1).getSubterm().getName());
        assertEquals("primary motor neuron", terms.get(1).getSuperterm().getName());
    }

    @Test
    public void sortWithTwoSubtermSameSuperterms() {
        ExpressedTermDTO two = new ExpressedTermDTO();
        TermDTO dtoTwo = new TermDTO();
        dtoTwo.setName("primary motor neuron");
        two.setSuperterm(dtoTwo);
        TermDTO dtoTwoSub = new TermDTO();
        dtoTwoSub.setName("cell some memberane");
        two.setSubterm(dtoTwoSub);
        ExpressedTermDTO one = new ExpressedTermDTO();
        TermDTO dtoOneSuper = new TermDTO();
        dtoOneSuper.setName("primary motor neuron");
        one.setSuperterm(dtoOneSuper);
        TermDTO dtoOneSub = new TermDTO();
        dtoOneSub.setName("axon");
        one.setSubterm(dtoOneSub);

        List<ExpressedTermDTO> terms = new ArrayList<ExpressedTermDTO>(2);
        terms.add(one);
        terms.add(two);

        Collections.sort(terms);

        assertEquals("primary motor neuron", terms.get(0).getSuperterm().getName());
        assertEquals("cell some membrane", terms.get(1).getSubterm().getName());
        assertEquals("axon", terms.get(0).getSubterm().getName());
        assertEquals("primary motor neuron", terms.get(1).getSuperterm().getName());
    }
}
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
        dtoOne.setTermName("secondary motor neuron");
        one.setSuperterm(dtoOne);

        List<ExpressedTermDTO> terms = new ArrayList<ExpressedTermDTO>(1);
        terms.add(one);

        assertEquals("secondary motor neuron", terms.get(0).getSuperterm().getTermName());
    }

    @Test
    public void sortWithoutSubtermsList() {
        ExpressedTermDTO two = new ExpressedTermDTO();
        TermDTO dtoTwo = new TermDTO();
        dtoTwo.setTermName("primary motor neuron");
        two.setSuperterm(dtoTwo);
        ExpressedTermDTO one = new ExpressedTermDTO();
        TermDTO dtoOne = new TermDTO();
        dtoOne.setTermName("secondary motor neuron");
        one.setSuperterm(dtoOne);

        List<ExpressedTermDTO> terms = new ArrayList<ExpressedTermDTO>(2);
        terms.add(one);
        terms.add(two);

        Collections.sort(terms);

        assertEquals("primary motor neuron", terms.get(0).getSuperterm().getTermName());
        assertEquals("secondary motor neuron", terms.get(1).getSuperterm().getTermName());
    }

    @Test
    public void sortWithOneSubterm() {
        ExpressedTermDTO two = new ExpressedTermDTO();
        TermDTO dtoTwo = new TermDTO();
        dtoTwo.setTermName("primary motor neuron");
        two.setSuperterm(dtoTwo);
        TermDTO dtoTwoSub = new TermDTO();
        dtoTwoSub.setTermName("cell some memberane");
        two.setSubterm(dtoTwoSub);
        ExpressedTermDTO one = new ExpressedTermDTO();
        TermDTO dtoOneSuper = new TermDTO();
        dtoOneSuper.setTermName("secondary motor neuron");
        one.setSuperterm(dtoOneSuper);

        List<ExpressedTermDTO> terms = new ArrayList<ExpressedTermDTO>(2);
        terms.add(one);
        terms.add(two);

        Collections.sort(terms);

        assertEquals("primary motor neuron", terms.get(0).getSuperterm().getTermName());
        assertEquals("secondary motor neuron", terms.get(1).getSuperterm().getTermName());
    }

    @Test
    public void sortWithOneSubtermSameSuperterms() {
        ExpressedTermDTO two = new ExpressedTermDTO();
        TermDTO dtoTwo = new TermDTO();
        dtoTwo.setTermName("primary motor neuron");
        two.setSuperterm(dtoTwo);
        TermDTO dtoTwoSub = new TermDTO();
        dtoTwoSub.setTermName("cell some memberane");
        two.setSubterm(dtoTwoSub);
        ExpressedTermDTO one = new ExpressedTermDTO();
        TermDTO dtoOneSuper = new TermDTO();
        dtoOneSuper.setTermName("primary motor neuron");
        one.setSuperterm(dtoOneSuper);

        List<ExpressedTermDTO> terms = new ArrayList<ExpressedTermDTO>(2);
        terms.add(one);
        terms.add(two);

        Collections.sort(terms);

        assertEquals("primary motor neuron", terms.get(0).getSuperterm().getTermName());
        assertEquals("cell some membrane", terms.get(1).getSubterm().getTermName());
        assertEquals("primary motor neuron", terms.get(1).getSuperterm().getTermName());
    }

    @Test
    public void sortWithTwoSubtermSameSuperterms() {
        ExpressedTermDTO two = new ExpressedTermDTO();
        TermDTO dtoTwo = new TermDTO();
        dtoTwo.setTermName("primary motor neuron");
        two.setSuperterm(dtoTwo);
        TermDTO dtoTwoSub = new TermDTO();
        dtoTwoSub.setTermName("cell some memberane");
        two.setSubterm(dtoTwoSub);
        ExpressedTermDTO one = new ExpressedTermDTO();
        TermDTO dtoOneSuper = new TermDTO();
        dtoOneSuper.setTermName("primary motor neuron");
        one.setSuperterm(dtoOneSuper);
        TermDTO dtoOneSub = new TermDTO();
        dtoOneSub.setTermName("axon");
        one.setSubterm(dtoOneSub);

        List<ExpressedTermDTO> terms = new ArrayList<ExpressedTermDTO>(2);
        terms.add(one);
        terms.add(two);

        Collections.sort(terms);

        assertEquals("primary motor neuron", terms.get(0).getSuperterm().getTermName());
        assertEquals("cell some membrane", terms.get(1).getSubterm().getTermName());
        assertEquals("axon", terms.get(0).getSubterm().getTermName());
        assertEquals("primary motor neuron", terms.get(1).getSuperterm().getTermName());
    }
}
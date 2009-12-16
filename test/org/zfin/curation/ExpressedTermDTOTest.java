package org.zfin.curation;

import org.junit.Test;
import org.zfin.framework.presentation.dto.ExpressedTermDTO;

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
        one.setSupertermName("secondary motor neuron");

        List<ExpressedTermDTO> terms = new ArrayList<ExpressedTermDTO>();
        terms.add(one);

        assertEquals("secondary motor neuron", terms.get(0).getSupertermName());
    }

    @Test
    public void sortWithoutSubtermsList() {
        ExpressedTermDTO two = new ExpressedTermDTO();
        two.setSupertermName("primary motor neuron");
        ExpressedTermDTO one = new ExpressedTermDTO();
        one.setSupertermName("secondary motor neuron");

        List<ExpressedTermDTO> terms = new ArrayList<ExpressedTermDTO>();
        terms.add(one);
        terms.add(two);

        Collections.sort(terms);

        assertEquals("primary motor neuron", terms.get(0).getSupertermName());
        assertEquals("secondary motor neuron", terms.get(1).getSupertermName());
    }

    @Test
    public void sortWithOneSubterm() {
        ExpressedTermDTO two = new ExpressedTermDTO();
        two.setSupertermName("primary motor neuron");
        two.setSubtermName("cell some memberane");
        ExpressedTermDTO one = new ExpressedTermDTO();
        one.setSupertermName("secondary motor neuron");

        List<ExpressedTermDTO> terms = new ArrayList<ExpressedTermDTO>();
        terms.add(one);
        terms.add(two);

        Collections.sort(terms);

        assertEquals("primary motor neuron", terms.get(0).getSupertermName());
        assertEquals("secondary motor neuron", terms.get(1).getSupertermName());
    }

    @Test
    public void sortWithOneSubtermSameSuperterms() {
        ExpressedTermDTO two = new ExpressedTermDTO();
        two.setSupertermName("primary motor neuron");
        two.setSubtermName("cell some membrane");
        ExpressedTermDTO one = new ExpressedTermDTO();
        one.setSupertermName("primary motor neuron");

        List<ExpressedTermDTO> terms = new ArrayList<ExpressedTermDTO>();
        terms.add(one);
        terms.add(two);

        Collections.sort(terms);

        assertEquals("primary motor neuron", terms.get(0).getSupertermName());
        assertEquals("cell some membrane", terms.get(1).getSubtermName());
        assertEquals("primary motor neuron", terms.get(1).getSupertermName());
    }

    @Test
    public void sortWithTwoSubtermSameSuperterms() {
        ExpressedTermDTO two = new ExpressedTermDTO();
        two.setSupertermName("primary motor neuron");
        two.setSubtermName("cell some membrane");
        ExpressedTermDTO one = new ExpressedTermDTO();
        one.setSupertermName("primary motor neuron");
        one.setSubtermName("axon");

        List<ExpressedTermDTO> terms = new ArrayList<ExpressedTermDTO>();
        terms.add(one);
        terms.add(two);

        Collections.sort(terms);

        assertEquals("primary motor neuron", terms.get(0).getSupertermName());
        assertEquals("cell some membrane", terms.get(1).getSubtermName());
        assertEquals("axon", terms.get(0).getSubtermName());
        assertEquals("primary motor neuron", terms.get(1).getSupertermName());
    }
}
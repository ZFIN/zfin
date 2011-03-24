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
        ExpressedTermDTO one = getExpressedTermDTO("secondary motor neuron", null);
        List<ExpressedTermDTO> terms = new ArrayList<ExpressedTermDTO>(1);
        terms.add(one);
        assertEquals("secondary motor neuron", terms.get(0).getEntity().getSuperTerm().getTermName());
    }

    @Test
    public void sortWithoutSubtermsList() {
        ExpressedTermDTO two = getExpressedTermDTO("primary motor neuron", null);
        ExpressedTermDTO one = getExpressedTermDTO("secondary motor neuron", null);

        List<ExpressedTermDTO> terms = new ArrayList<ExpressedTermDTO>(2);
        terms.add(one);
        terms.add(two);

        Collections.sort(terms);

        assertEquals("primary motor neuron", terms.get(0).getEntity().getSuperTerm().getTermName());
        assertEquals("secondary motor neuron", terms.get(1).getEntity().getSuperTerm().getTermName());
    }

    @Test
    public void sortWithOneSubterm() {
        ExpressedTermDTO two = getExpressedTermDTO("primary motor neuron", "cell some memberane");
        ExpressedTermDTO one = getExpressedTermDTO("secondary motor neuron", null);

        List<ExpressedTermDTO> terms = new ArrayList<ExpressedTermDTO>(2);
        terms.add(one);
        terms.add(two);

        Collections.sort(terms);

        assertEquals("primary motor neuron", terms.get(0).getEntity().getSuperTerm().getTermName());
        assertEquals("secondary motor neuron", terms.get(1).getEntity().getSuperTerm().getTermName());
    }

    @Test
    public void sortWithOneSubtermSameSuperterms() {
        ExpressedTermDTO two = getExpressedTermDTO("primary motor neuron", "cell some membrane");
        ExpressedTermDTO one = getExpressedTermDTO("primary motor neuron", null);

        List<ExpressedTermDTO> terms = new ArrayList<ExpressedTermDTO>(2);
        terms.add(one);
        terms.add(two);

        Collections.sort(terms);

        assertEquals("primary motor neuron", terms.get(0).getEntity().getSuperTerm().getTermName());
        assertEquals("cell some membrane", terms.get(1).getEntity().getSubTerm().getTermName());
        assertEquals("primary motor neuron", terms.get(1).getEntity().getSuperTerm().getTermName());
    }

    @Test
    public void sortWithTwoSubtermSameSuperterms() {
        ExpressedTermDTO two = getExpressedTermDTO("primary motor neuron", "cell some membrane");
        ExpressedTermDTO one = getExpressedTermDTO("primary motor neuron", "axon");

        List<ExpressedTermDTO> terms = new ArrayList<ExpressedTermDTO>(2);
        terms.add(one);
        terms.add(two);

        Collections.sort(terms);

        assertEquals("primary motor neuron", terms.get(0).getEntity().getSuperTerm().getTermName());
        assertEquals("cell some membrane", terms.get(1).getEntity().getSubTerm().getTermName());
        assertEquals("axon", terms.get(0).getEntity().getSubTerm().getTermName());
        assertEquals("primary motor neuron", terms.get(1).getEntity().getSuperTerm().getTermName());
    }

    private ExpressedTermDTO getExpressedTermDTO(String supertermName, String subtermName) {
        ExpressedTermDTO dto = new ExpressedTermDTO();
        TermDTO supertermDTO = new TermDTO();
        supertermDTO.setName(supertermName);
        EntityDTO entity = new EntityDTO();
        entity.setSuperTerm(supertermDTO);
        if(subtermName != null){
            TermDTO subtermDTO = new TermDTO();
            subtermDTO.setName(subtermName);
            entity.setSubTerm(subtermDTO);
        }
        dto.setEntity(entity);
        return dto;
    }

}

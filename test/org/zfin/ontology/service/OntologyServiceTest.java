package org.zfin.ontology.service;

import org.junit.Test;
import org.zfin.gwt.root.dto.OntologyDTO;
import org.zfin.gwt.root.dto.TermDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * This service provides a bridge between the OntologyRepository and business logic.
 */
public class OntologyServiceTest {

    @Test
    public void checkHistogram() {
        TermDTO termOne = new TermDTO();
        termOne.setOntology(OntologyDTO.ANATOMY);
        TermDTO termTwo = new TermDTO();
        termTwo.setOntology(OntologyDTO.GO_BP);
        TermDTO termThree = new TermDTO();
        termThree.setOntology(OntologyDTO.ANATOMY);
        TermDTO four = new TermDTO();
        four.setOntology(OntologyDTO.GO_CC);
        TermDTO five = new TermDTO();
        five.setOntology(OntologyDTO.GO_CC);
        List<TermDTO> list = new ArrayList<TermDTO>(7);
        list.add(termOne);
        list.add(termTwo);
        list.add(termThree);
        list.add(four);
        list.add(five);

        Map<OntologyDTO, Integer> map = OntologyService.getHistogramOfTerms(list);
        assertNotNull(map);
        assertEquals(3, map.size());
        assertEquals(new Integer(2), map.get(OntologyDTO.ANATOMY));
        assertEquals(new Integer(1), map.get(OntologyDTO.GO_BP));
    }
}

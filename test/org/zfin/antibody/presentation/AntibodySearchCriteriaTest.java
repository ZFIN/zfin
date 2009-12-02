package org.zfin.antibody.presentation;

import org.junit.Test;
import static org.junit.Assert.*;

public class AntibodySearchCriteriaTest  {


@Test
    public void noTerm(){
        String termIds = "";

        AntibodySearchCriteria searchCriteria = new AntibodySearchCriteria();
        searchCriteria.setAnatomyTermIDs(termIds);

        String[] terms = searchCriteria.getTermIDs();

        assertTrue(terms == null);
    }

    @Test
    public void oneTerm(){
        String termIds = "ZDB-ANAT-090811-01";

        AntibodySearchCriteria searchCriteria = new AntibodySearchCriteria();
        searchCriteria.setAnatomyTermIDs(termIds);

        String[] terms = searchCriteria.getTermIDs();

        assertTrue(terms != null);
        assertTrue(terms.length == 1);
        assertEquals(termIds, terms[0]);
    }


    @Test
    public void twoTermsNoSpace(){
        String termIds = "ZDB-ANAT-090811-01,ZDB-ANAT-090811-02";

        AntibodySearchCriteria searchCriteria = new AntibodySearchCriteria();
        searchCriteria.setAnatomyTermIDs(termIds);

        String[] terms = searchCriteria.getTermIDs();

        assertTrue(terms != null);
        assertTrue(terms.length == 2);
        assertEquals("ZDB-ANAT-090811-01", terms[0]);
        assertEquals("ZDB-ANAT-090811-02", terms[1]);
    }

    @Test
    public void twoTermsWithSpace(){
        String termIds = "ZDB-ANAT-090811-01, ZDB-ANAT-090811-02 ";

        AntibodySearchCriteria searchCriteria = new AntibodySearchCriteria();
        searchCriteria.setAnatomyTermIDs(termIds);

        String[] terms = searchCriteria.getTermIDs();

        assertTrue(terms != null);
        assertTrue(terms.length == 2);
        assertEquals("ZDB-ANAT-090811-01", terms[0]);
        assertEquals("ZDB-ANAT-090811-02", terms[1]);
    }

}
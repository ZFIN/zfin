package org.zfin.antibody.presentation;

import org.junit.Test;
import static org.junit.Assert.*;

public class AntibodySearchCriteriaTest  {

    @Test
    public void noTerm(){
        String textAreaString = "";

        AntibodySearchCriteria crit = new AntibodySearchCriteria();
        crit.setAnatomyTermsString(textAreaString);

        String[] terms = crit.getAnatomyTerms();

        assertTrue(terms == null);
    }

    @Test
    public void oneTerm(){
        String textAreaString = "retina";

        AntibodySearchCriteria crit = new AntibodySearchCriteria();
        crit.setAnatomyTermsString(textAreaString);

        String[] terms = crit.getAnatomyTerms();

        assertTrue(terms != null);
        assertTrue(terms.length == 1);
        assertEquals(textAreaString, terms[0]);
    }


    @Test
    public void twoTermsNoSpace(){
        String textAreaString = "retina,brain";

        AntibodySearchCriteria crit = new AntibodySearchCriteria();
        crit.setAnatomyTermsString(textAreaString);

        String[] terms = crit.getAnatomyTerms();

        assertTrue(terms != null);
        assertTrue(terms.length == 2);
        assertEquals("retina", terms[0]);
        assertEquals("brain", terms[1]);
    }

    @Test
    public void twoTermsWithSpace(){
        String textAreaString = "retina ,  brain ";

        AntibodySearchCriteria crit = new AntibodySearchCriteria();
        crit.setAnatomyTermsString(textAreaString);

        String[] terms = crit.getAnatomyTerms();

        assertTrue(terms != null);
        assertTrue(terms.length == 2);
        assertEquals("retina", terms[0]);
        assertEquals("brain", terms[1]);
    }

}
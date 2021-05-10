package org.zfin.antibody.presentation;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class AntibodySearchCriteriaTest  {

    private AntibodySearchCriteria searchCriteria = new AntibodySearchCriteria();

    @Test
    public void noTerm(){
        String termIds = "";

        searchCriteria.setAnatomyTermIDs(termIds);

        String[] terms = searchCriteria.getTermIDs();

        assertThat(terms, is(nullValue()));
    }

    @Test
    public void oneTerm(){
        String termIds = "ZDB-ANAT-090811-01";

        searchCriteria.setAnatomyTermIDs(termIds);

        String[] terms = searchCriteria.getTermIDs();

        assertThat(terms, is(notNullValue()));
        assertThat(terms, is(arrayWithSize(1)));
        assertThat(terms, is(arrayContaining(termIds)));
    }


    @Test
    public void twoTermsNoSpace(){
        String termIds = "ZDB-ANAT-090811-01,ZDB-ANAT-090811-02";

        searchCriteria.setAnatomyTermIDs(termIds);

        String[] terms = searchCriteria.getTermIDs();

        assertThat(terms, is(notNullValue()));
        assertThat(terms, is(arrayWithSize(2)));
        assertThat(terms, is(arrayContaining("ZDB-ANAT-090811-01", "ZDB-ANAT-090811-02")));
    }

    @Test
    public void twoTermsWithSpace(){
        String termIds = "ZDB-ANAT-090811-01, ZDB-ANAT-090811-02 ";

        searchCriteria.setAnatomyTermIDs(termIds);

        String[] terms = searchCriteria.getTermIDs();

        assertThat(terms, is(notNullValue()));
        assertThat(terms, is(arrayWithSize(2)));
        assertThat(terms, is(arrayContaining("ZDB-ANAT-090811-01", "ZDB-ANAT-090811-02")));
    }

}
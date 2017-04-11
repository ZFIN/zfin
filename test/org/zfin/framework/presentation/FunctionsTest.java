package org.zfin.framework.presentation;

import org.junit.Test;
import org.zfin.anatomy.presentation.AnatomySearchBean;
import org.zfin.ontology.presentation.OntologyBean;

import java.util.Calendar;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Class that is called from JSP through a function call.
 */
public class FunctionsTest {

    @Test
    public void removeQueryStringInLastPosition() {
        String queryString = "termOne=2213&termTwo=4463&termThree=harry";
        String returnString = ZfinJSPFunctions.removeQueryParameter(queryString, "termThree", "harry");
        assertEquals(returnString, "termOne=2213&termTwo=4463");
    }

    @Test
    public void removeQueryStringInFirstPosition() {
        String queryString = "term=11&termOne=2213&termTwo=4463&termThree=harry";
        String returnString = ZfinJSPFunctions.removeQueryParameter(queryString, "term", "11");
        assertEquals(returnString, "&termOne=2213&termTwo=4463&termThree=harry");
    }

    @Test
    public void removeQueryStringNotFound() {
        String queryString = "term=11&termOne=2213&termTwo=4463&termThree=harry";
        String returnString = ZfinJSPFunctions.removeQueryParameter(queryString, "term", "3325");
        assertEquals(returnString, queryString);
    }

    @Test
    public void sectionVisibilityDefaultFalse() {
        SectionVisibility vis = new SectionVisibility<OntologyBean.Section>(OntologyBean.Section.class);
        boolean isSectionVisible = ZfinJSPFunctions.isSectionVisible(OntologyBean.Section.EXPRESSION.toString(), vis);
        assertTrue(!isSectionVisible);
    }

    @Test
    public void sectionVisibilityDefaultTrue() {
        SectionVisibility vis = new SectionVisibility<OntologyBean.Section>(OntologyBean.Section.class, true);
        boolean visible = ZfinJSPFunctions.isSectionVisible(OntologyBean.Section.EXPRESSION.toString(), vis);
        assertTrue(visible);
        vis.setVisibility(OntologyBean.Section.EXPRESSION, false);
        visible = ZfinJSPFunctions.isSectionVisible(OntologyBean.Section.EXPRESSION.toString(), vis);
        assertTrue(!visible);
        visible = ZfinJSPFunctions.isSectionVisible(OntologyBean.Section.PHENOTYPE.toString(), vis);
        assertTrue(visible);
    }

    @Test
    public void removeAllParametersFromQueryString() {
        String queryString = "ID=ZDB-TERM-050915-94&sectionVisibility.showAll=true";
        String modifiedQueryString = ZfinJSPFunctions.removeAllVisibleQueryParameters(queryString, "sectionVisibility.");
        assertEquals("ID=ZDB-TERM-050915-94", modifiedQueryString);


        queryString = "ID=ZDB-TERM-050915-94&sectionVisibility.showAll=true&sectionVisibility.showAll=false&sectionVisibility.hideAll=true";
        modifiedQueryString = ZfinJSPFunctions.removeAllVisibleQueryParameters(queryString, "sectionVisibility.");
        assertEquals("ID=ZDB-TERM-050915-94", modifiedQueryString);


    }

    @Test
    public void removeAllParametersFromQueryStringEnumeration() {
        String queryString = "ID=ZDB-TERM-050915-94&showSection=EXPRESSION&showSection=PHENOTYPE";
        String modifiedQueryString = ZfinJSPFunctions.removeAllVisibilityQueryParameters(queryString, "", AnatomySearchBean.Section.getValues());
        assertEquals("ID=ZDB-TERM-050915-94", modifiedQueryString);


        queryString = "ID=ZDB-TERM-050915-94&showSection=EXPRESSION&showSection=PHENOTYPE&hideSection=EXPRESSION";
        modifiedQueryString = ZfinJSPFunctions.removeAllVisibilityQueryParameters(queryString, "", AnatomySearchBean.Section.getValues());
        assertEquals("ID=ZDB-TERM-050915-94", modifiedQueryString);


    }

    @Test
    public void isDateToday() {
        Date today = new Date();
        assertTrue(ZfinJSPFunctions.isToday(today));

        today.setMonth(today.getMonth() + 1);
        assertTrue(!ZfinJSPFunctions.isToday(today));

        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        assertTrue(!ZfinJSPFunctions.isToday(tomorrow.getTime()));
    }

    @Test
    public void isDateTomorrow() {
        Date today = new Date();
        today.setMonth(today.getMonth() + 1);
        assertTrue(!ZfinJSPFunctions.isTomorrow(today));

        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        assertTrue(ZfinJSPFunctions.isTomorrow(tomorrow.getTime()));
    }

    @Test
    public void isDateYesterday() {
        Date today = new Date();
        today.setMonth(today.getMonth() + 1);
        assertTrue(!ZfinJSPFunctions.isYesterday(today));

        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        assertTrue(!ZfinJSPFunctions.isYesterday(tomorrow.getTime()));

        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_MONTH, -1);
        assertTrue(ZfinJSPFunctions.isYesterday(yesterday.getTime()));
    }

    @Test
    public void makeDomIdentifierShouldRemoveIllegalCharacters() {
        assertThat("makeDomIdentifier should remove spaces from input",
                ZfinJSPFunctions.makeDomIdentifier("Affected Genomic Region"), is("AffectedGenomicRegion"));

        assertThat("makeDomIdentifier should remove slashes from input",
                ZfinJSPFunctions.makeDomIdentifier("Mutation / Tg"), is("MutationTg"));

        assertThat("makeDomIdentifier should remove parens from input",
                ZfinJSPFunctions.makeDomIdentifier("Sequence Targeting Reagent (STR)"), is("SequenceTargetingReagentSTR"));

        assertThat("makeDomIdentifier should remove ampersand from input",
                ZfinJSPFunctions.makeDomIdentifier("Cookies & Cream"), is("CookiesCream"));
    }
}


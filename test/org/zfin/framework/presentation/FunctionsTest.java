package org.zfin.framework.presentation;

import com.opensymphony.clickstream.Clickstream;
import com.opensymphony.clickstream.ClickstreamRequest;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.AnatomyRelationship;
import org.zfin.anatomy.presentation.AnatomySearchBean;
import org.zfin.ontology.TermRelationship;
import org.zfin.people.AccountInfo;
import org.zfin.people.Lab;
import org.zfin.people.Person;

import java.util.*;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

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
    public void removeQueryStringnotFound() {
        String queryString = "term=11&termOne=2213&termTwo=4463&termThree=harry";
        String returnString = ZfinJSPFunctions.removeQueryParameter(queryString, "term", "3325");
        assertEquals(returnString, queryString);
    }

    @Test
    public void sectionVisiblityDefaultFalse() {
        SectionVisibility vis = new SectionVisibility<AnatomySearchBean.Section>(AnatomySearchBean.Section.class);
        boolean isSectionVisible = ZfinJSPFunctions.isSectionVisible(AnatomySearchBean.Section.ANATOMY_EXPRESSION.toString(), vis);
        assertTrue(!isSectionVisible);
    }

    @Test
    public void sectionVisiblityDefaultTrue() {
        SectionVisibility vis = new SectionVisibility<AnatomySearchBean.Section>(AnatomySearchBean.Section.class, true);
        boolean visible = ZfinJSPFunctions.isSectionVisible(AnatomySearchBean.Section.ANATOMY_EXPRESSION.toString(), vis);
        assertTrue(visible);
        vis.setVisibility(AnatomySearchBean.Section.ANATOMY_EXPRESSION, false);
        visible = ZfinJSPFunctions.isSectionVisible(AnatomySearchBean.Section.ANATOMY_EXPRESSION.toString(), vis);
        assertTrue(!visible);
        visible = ZfinJSPFunctions.isSectionVisible(AnatomySearchBean.Section.ANATOMY_PHENOTYPE.toString(), vis);
        assertTrue(visible);
    }

    @Test
    public void removeAllParametersFromQueryString() {
        String queryString = "anatomyItem.zdbID=ZDB-ANAT-050915-94&sectionVisibility.showAll=true";
        String modifiedQueryString = ZfinJSPFunctions.removeAllVisibleQueryParameters(queryString, "sectionVisibility.");
        assertEquals("anatomyItem.zdbID=ZDB-ANAT-050915-94", modifiedQueryString);


        queryString = "anatomyItem.zdbID=ZDB-ANAT-050915-94&sectionVisibility.showAll=true&sectionVisibility.showAll=false&sectionVisibility.hideAll=true";
        modifiedQueryString = ZfinJSPFunctions.removeAllVisibleQueryParameters(queryString, "sectionVisibility.");
        assertEquals("anatomyItem.zdbID=ZDB-ANAT-050915-94", modifiedQueryString);


    }

    @Test
    public void removeAllParametersFromQueryStringEnumeration() {
        String queryString = "anatomyItem.zdbID=ZDB-ANAT-050915-94&showSection=ANATOMY_EXPRESSION&showSection=ANATOMY_PHENOTYPE";
        String modifiedQueryString = ZfinJSPFunctions.removeAllVisibilityQueryParameters(queryString, "", AnatomySearchBean.Section.getValues());
        assertEquals("anatomyItem.zdbID=ZDB-ANAT-050915-94", modifiedQueryString);


        queryString = "anatomyItem.zdbID=ZDB-ANAT-050915-94&showSection=ANATOMY_EXPRESSION&showSection=ANATOMY_PHENOTYPE&hideSection=ANATOMY_EXPRESSION";
        modifiedQueryString = ZfinJSPFunctions.removeAllVisibilityQueryParameters(queryString, "", AnatomySearchBean.Section.getValues());
        assertEquals("anatomyItem.zdbID=ZDB-ANAT-050915-94", modifiedQueryString);


    }

    @Test
    public void getTimeDurationToNextElement() {
/*
        ClickstreamRequest streamTwo = new ClickstreamRequest(new MockHttpServletRequest("GET", "action"), new Date());
        ClickstreamRequest streamOne = new ClickstreamRequest(new MockHttpServletRequest("GET", "action"), new Date());
*/
        Clickstream stream = new Clickstream();
        stream.addRequest(new MockHttpServletRequest("GET", "action"));
        stream.addRequest(new MockHttpServletRequest("GET", "action zwei"));
        String durationString = ZfinJSPFunctions.getTimeBetweenRequests(stream.getStream(), 0);
    }

}


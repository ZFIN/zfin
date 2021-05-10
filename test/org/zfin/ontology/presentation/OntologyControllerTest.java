package org.zfin.ontology.presentation;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;
import org.zfin.AbstractDatabaseTest;
import org.zfin.TestConfiguration;
import org.zfin.framework.presentation.LookupStrings;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "file:test/properties-test.xml")
public class OntologyControllerTest extends AbstractDatabaseTest {

    static {
        TestConfiguration.configure();
    }

    @Autowired
    private ApplicationContext applicationContext;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private HandlerAdapter handlerAdapter;
    private OntologyTermDetailController controller;


    @Before
    public void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        handlerAdapter = applicationContext.getBean(HandlerAdapter.class);
        controller = new OntologyTermDetailController();
    }

    @Test
    public void retrieveTermByOboID() throws Exception {
        request.setRequestURI("/ontology/term-detail/GO:0032502");
        ModelAndView mav = handlerAdapter.handle(request, response, controller);
        assertNotNull(mav);
        assertEquals("ontology/ontology-term", mav.getViewName());
    }

    @Test
    public void retrieveTermByTermID() throws Exception {
        // presumptive forebrain midbrain boundary
        request.setRequestURI("/ontology/term-detail/ZDB-TERM-100331-1323");
        ModelAndView mav = handlerAdapter.handle(request, response, controller);
        assertNotNull(mav);
        assertEquals("ontology/ontology-term", mav.getViewName());
        Map<String, Object> model = mav.getModel();
        assertNotNull(model);
        Object formBean = model.get(LookupStrings.FORM_BEAN);
        assertEquals(OntologyBean.class, formBean.getClass());
        OntologyBean ontologyBean = (OntologyBean) formBean;
        assertEquals("presumptive forebrain midbrain boundary", ontologyBean.getTerm().getTermName());
    }

    @Test
    public void retrieveTermByAnatomyID() throws Exception {
        // Rohon-Beard neuron
        request.setRequestURI("/ontology/term-detail/ZDB-ANAT-010921-407");
        ModelAndView mav = handlerAdapter.handle(request, response, controller);
        assertNotNull(mav);
        // redirect to Rohon-Beard neurons
        assertEquals("redirect:/action/ontology/term-detail/ZDB-TERM-100331-2208", mav.getViewName());
    }

    // Todo: This is broken only within the test environment (must be a bug in spring) as it is working
    // in the true servlet container.
    @Test
    @Ignore("broken only within the test environment")
    public void retrieveTermByAnatomyName() throws Exception {
        request.setRequestURI("/term-detail-by-name/term?name=liver&ontologyName=zebrafish_anatomy");
        ModelAndView mav = handlerAdapter.handle(request, response, controller);
        assertNotNull(mav);
        // redirect to 'liver'
        assertEquals("redirect:/action/ontology/term-detail/ZFA:0000123", mav.getViewName());
    }

    // Todo: This is broken only within the test environment (must be a bug in spring) as it is working
    // in the true servlet container.
    @Test
    @Ignore("broken only within the test environment")
    public void retrievePopupById() throws Exception {
        request.setRequestURI("/term-detail-popup?termID=GO:0043231");
        ModelAndView mav = handlerAdapter.handle(request, response, controller);
        assertNotNull(mav);
        // redirect to 'liver'
        assertEquals("redirect:/action/ontology/term-detail/ZFA:0000123", mav.getViewName());
    }

    @Test
    @Ignore("Requires to load AO ontology from serialized file.")
    public void retrieveTermListByWildtype() throws Exception {
        request.setRequestURI("/term-detail/emb*");
        ModelAndView mav = handlerAdapter.handle(request, response, controller);
        assertNotNull(mav);
        // redirect to 'liver'
        assertEquals("redirect:/action/ontology/term-detail/ZFA:0000123", mav.getViewName());
    }
}
package org.zfin.ontology.presentation;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.zfin.AbstractDatabaseTest;
import org.zfin.TestConfiguration;
import org.zfin.framework.presentation.LookupStrings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@RunWith(SpringJUnit4ClassRunner.class)
public class OntologyControllerTest extends AbstractDatabaseTest {

    static {
        TestConfiguration.configure();
    }

    private MockMvc mockMvc;
    private OntologyTermDetailController controller;

    @Before
    public void setUp() {
        controller = new OntologyTermDetailController();
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    public void retrieveTermByOboID() throws Exception {
        mockMvc.perform(get("/ontology/term/GO:0032502"))
                .andExpect(status().isOk())
                .andExpect(view().name("ontology/term-view"));
    }

    @Test
    public void retrieveTermByTermID() throws Exception {
        String termID = "ZDB-TERM-100331-1323";

//        MockMvcResultHandlers.print() // Optional: prints the request and response for debugging

        mockMvc.perform(get("/ontology/term/{termID}", termID))
                .andExpect(status().isOk())
                .andExpect(view().name("ontology/term-view"))
                .andExpect(model().attributeExists(LookupStrings.FORM_BEAN))
                .andDo(result -> {
                    Object formBean = result.getModelAndView().getModel().get(LookupStrings.FORM_BEAN);
                    assertNotNull(formBean);
                    assertEquals(OntologyBean.class, formBean.getClass());
                    OntologyBean ontologyBean = (OntologyBean) formBean;
                    assertEquals("presumptive forebrain midbrain boundary", ontologyBean.getTerm().getTermName());
                });
    }

    @Test
    public void retrieveTermByAnatomyID() throws Exception {
        String anatomyID = "ZDB-ANAT-010921-407";

        mockMvc.perform(get("/ontology/term/{anatomyID}", anatomyID))
                .andDo(result -> {
                    int status = result.getResponse().getStatus();

                    // Assert the redirect status
                    assertEquals(302, status);

                    // Assert the redirect location
                    String redirectUrl = result.getResponse().getHeader("Location");

                    assertNotNull(redirectUrl);
                    assertEquals("/action/ontology/term/ZDB-TERM-100331-2208", redirectUrl);
                });
    }
}
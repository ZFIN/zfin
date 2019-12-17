package org.zfin.framework.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.zfin.AbstractDatabaseTest;
import org.zfin.framework.ZfinConfiguration;
import org.zfin.marker.presentation.SequenceController;
import org.zfin.sequence.MarkerDBLink;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration("home")
@ContextConfiguration(classes = ZfinConfiguration.class)
public class SequenceControllerTest extends AbstractDatabaseTest {

    @Autowired
    private SequenceController controller;

    @Test
    public void getSequencesAlcama() {
        // alcama
        String zdbID = "ZDB-GENE-990415-30";

        JsonResultResponse<MarkerDBLink> links = controller.getSequenceView(zdbID, null, null, null, new Pagination());
        assertNotNull(links);
        assertThat(links.getTotal(), greaterThanOrEqualTo(34));

        links = controller.getSequenceView(zdbID, null, "1", null, new Pagination());
        assertNotNull(links);
        // filtered records for accession number '1'.
        assertThat(links.getTotal(), greaterThanOrEqualTo(13));
        assertThat(links.getTotal(), lessThanOrEqualTo(13));

        links = controller.getSequenceView(zdbID, "GEnomic", null, null, new Pagination());
        assertNotNull(links);
        // filtered records on type
        assertThat(links.getTotal(), greaterThanOrEqualTo(3));
        assertThat(links.getTotal(), lessThanOrEqualTo(3));
    }
}
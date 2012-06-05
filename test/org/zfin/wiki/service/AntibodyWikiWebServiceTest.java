package org.zfin.wiki.service;

import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.antibody.Antibody;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.zfin.repository.RepositoryFactory.getAntibodyRepository;

/**
 */
public class AntibodyWikiWebServiceTest extends AbstractDatabaseTest {

    /**
     * Test that the antibody page on the wiki (for zn-5) has the correct title
     * and the correct hyperlink to the recognized gene (alcama) at ZFIN.
     * @throws Exception
     */
    @Test
    public void checkGeneLinkOnAntibodyPage() throws Exception {

        // zn-5
        String zdbID = "ZDB-ATB-081002-19";
        Antibody ab = getAntibodyRepository().getAntibodyByID(zdbID);
        assertTrue(ab != null);

        AntibodyWikiWebService service = AntibodyWikiWebService.getInstance();
        String title = service.getWikiTitleFromAntibody(ab);
        assertEquals("zn-5", title);
        String contents = service.createWikiPageContentForAntibodyFromTemplate(ab);
        assertTrue("contains alcama gene", contents.contains("alcama"));
        assertTrue("contains hyperlink to alcama gene", contents.contains("/action/marker/view/ZDB-GENE-990415-30"));
    }

}

package org.zfin.gwt;

import java.util.Set;

/**
 * This class uses the more raw HtmlUnit protocols.
 */
public class GOEvidenceUnitTest extends AbstractJWebUnitTest {

    private String oldPubID;
    private Set<String> oldInferences;

    @Override
    public void setUp() {
        super.setUp();
        // set pub and number of inferences in back-end
    }


    @Override
    protected void tearDown() throws Exception {
        super.tearDown();    //To change body of overridden methods use File | Settings | File Templates.
        // revert pub and # of inferences
    }

    /**
     * For each evidence code (save old one), observe which inferences and
     * error strings are visible.
     */
    public void testInferenceForEvidenceCode() {


    }


}
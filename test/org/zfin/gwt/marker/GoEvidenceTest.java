package org.zfin.gwt.marker;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.mutant.presentation.MarkerGoEvidencePresentation;

import static org.junit.Assert.assertNotNull;

/**
 * DB tests for MarkerGoEvidence code.
 */
@FixMethodOrder
public class GoEvidenceTest extends AbstractDatabaseTest {

    @Test
    public void validateReferenceDatabases() {
        assertNotNull(MarkerGoEvidencePresentation.getGenbankReferenceDatabase());
        assertNotNull(MarkerGoEvidencePresentation.getEcReferenceDatabase());
        assertNotNull(MarkerGoEvidencePresentation.getGenpeptReferenceDatabase());
        assertNotNull(MarkerGoEvidencePresentation.getGoReferenceDatabase());
        assertNotNull(MarkerGoEvidencePresentation.getInterproReferenceDatabase());
        assertNotNull(MarkerGoEvidencePresentation.getRefseqReferenceDatabase());
        assertNotNull(MarkerGoEvidencePresentation.getSpkwReferenceDatabase());
    }

}

package org.zfin.zirc.service;

import org.junit.Test;
import org.zfin.zirc.entity.GenotypingAssayFile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Upload validation for the two attachment buckets (ZFIN-10415).
 *
 * <p>The asymmetry between the two is the thing worth pinning down: the
 * results bucket trusts the browser-reported content type, the protocol
 * bucket cannot, because browsers report Office formats inconsistently. A
 * regression that "tidied" the protocol bucket onto a content-type allow-list
 * would reject the .docx the ticket names, on some machines only.
 */
public class ZircSubmissionServiceTest {

    private static final String RESULT = GenotypingAssayFile.KIND_ASSAY_RESULT;
    private static final String PROTOCOL = GenotypingAssayFile.KIND_PROTOCOL_DOC;

    @Test
    public void protocolBucketAcceptsEveryDocumentTypeTheTicketNames() {
        for (String name : new String[] {
                "protocol.pdf", "protocol.docx", "protocol.doc",
                "protocol.txt", "protocol.rtf", "protocol.odt"}) {
            ZircSubmissionService.validateUploadType(PROTOCOL, name, "application/pdf");
        }
    }

    /**
     * The case the content-type check would get wrong: Windows and Linux
     * Chrome commonly post a .docx as application/octet-stream when Office
     * isn't registered for the extension.
     */
    @Test
    public void protocolBucketIgnoresAnUninformativeContentType() {
        ZircSubmissionService.validateUploadType(PROTOCOL, "protocol.docx", "application/octet-stream");
        ZircSubmissionService.validateUploadType(PROTOCOL, "protocol.pdf", null);
    }

    @Test
    public void protocolBucketMatchesTheExtensionCaseInsensitively() {
        ZircSubmissionService.validateUploadType(PROTOCOL, "PROTOCOL.PDF", null);
    }

    @Test
    public void protocolBucketRejectsAnImage() {
        String message = expectRejection(PROTOCOL, "gel.png", "image/png");
        assertTrue("the error should name the accepted types, got: " + message,
                message.contains(".docx"));
    }

    @Test
    public void protocolBucketRejectsAnExtensionlessFile() {
        expectRejection(PROTOCOL, "protocol", "application/pdf");
    }

    /**
     * ".pdf.exe" ends with neither ".pdf" nor any other accepted suffix, so
     * the endsWith check refuses it — worth pinning, since a "contains" test
     * would let it through.
     */
    @Test
    public void protocolBucketRejectsADoubleExtension() {
        expectRejection(PROTOCOL, "protocol.pdf.exe", "application/pdf");
    }

    @Test
    public void resultsBucketStillKeysOffContentType() {
        ZircSubmissionService.validateUploadType(RESULT, "gel.png", "image/png");
        // Same file the protocol bucket would take, rejected here because
        // its content type isn't in the results allow-list.
        expectRejection(RESULT, "protocol.docx", "application/octet-stream");
        expectRejection(RESULT, "gel.png", null);
    }

    private static String expectRejection(String kind, String filename, String contentType) {
        try {
            ZircSubmissionService.validateUploadType(kind, filename, contentType);
            fail("expected " + filename + " to be rejected from the " + kind + " bucket");
            return null;
        } catch (IllegalArgumentException expected) {
            return String.valueOf(expected.getMessage());
        }
    }

    @Test
    public void theTwoKindsAreTheOnesTheCheckConstraintAllows() {
        // Mirrors genotyping_assay_file_af_kind_check; the controller and
        // service reject anything outside this set before it reaches the DB.
        assertEquals(java.util.Set.of("assay_result", "protocol_doc"), GenotypingAssayFile.KINDS);
    }
}

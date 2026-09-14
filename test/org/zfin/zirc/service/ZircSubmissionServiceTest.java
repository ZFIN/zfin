package org.zfin.zirc.service;

import org.junit.Test;
import org.zfin.zirc.api.ZircAttachmentKind;
import org.zfin.zirc.entity.GenotypingAssayFile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Upload validation for the attachment buckets (ZFIN-10415, ZFIN-10417).
 *
 * <p>The thing worth pinning down is that validation is by <b>extension</b>,
 * for every bucket. A regression that "tidied" this onto a content-type
 * allow-list would reject the .docx ZFIN-10415 names and the .abi/.scf traces
 * ZFIN-10417 names — both of which browsers commonly post as
 * application/octet-stream — and would do it on some machines only, which is
 * the kind of bug that reaches production.
 */
public class ZircSubmissionServiceTest {

    private static final String RESULT = GenotypingAssayFile.KIND_ASSAY_RESULT;
    private static final String PROTOCOL = GenotypingAssayFile.KIND_PROTOCOL_DOC;

    @Test
    public void protocolBucketAcceptsEveryDocumentTypeTheTicketNames() {
        for (String name : new String[] {
                "protocol.pdf", "protocol.docx", "protocol.doc",
                "protocol.txt", "protocol.rtf", "protocol.odt"}) {
            ZircSubmissionService.validateUploadType(ZircAttachmentKind.PROTOCOL_DOC, name);
        }
    }

    @Test
    public void protocolBucketMatchesTheExtensionCaseInsensitively() {
        ZircSubmissionService.validateUploadType(ZircAttachmentKind.PROTOCOL_DOC, "PROTOCOL.PDF");
    }

    @Test
    public void protocolBucketRejectsAnImage() {
        String message = expectRejection(ZircAttachmentKind.PROTOCOL_DOC, "gel.png");
        assertTrue("the error should name the accepted types, got: " + message,
                message.contains(".docx"));
    }

    @Test
    public void protocolBucketRejectsAnExtensionlessFile() {
        expectRejection(ZircAttachmentKind.PROTOCOL_DOC, "protocol");
    }

    /**
     * ".pdf.exe" resolves to the "exe" extension, not "pdf" — worth pinning,
     * since a "contains" or "startsWith" test would let it through.
     */
    @Test
    public void protocolBucketRejectsADoubleExtension() {
        expectRejection(ZircAttachmentKind.PROTOCOL_DOC, "protocol.pdf.exe");
    }

    /**
     * The two buckets disagree about the same file, which is the whole point
     * of validating per bucket rather than globally.
     */
    @Test
    public void eachBucketJudgesTheSameFileByItsOwnList() {
        ZircSubmissionService.validateUploadType(ZircAttachmentKind.GEL_IMAGE, "gel.png");
        expectRejection(ZircAttachmentKind.GEL_IMAGE, "protocol.docx");

        ZircSubmissionService.validateUploadType(ZircAttachmentKind.PROTOCOL_DOC, "protocol.docx");
        expectRejection(ZircAttachmentKind.PROTOCOL_DOC, "gel.png");
    }

    /** The unrestricted bucket takes anything; the size cap still applies. */
    @Test
    public void anUnrestrictedBucketTakesEverything() {
        ZircSubmissionService.validateUploadType(ZircAttachmentKind.MELT_CURVE, "curve.weird");
        ZircSubmissionService.validateUploadType(ZircAttachmentKind.MELT_CURVE, "no-extension");
    }

    /**
     * A null bucket means "no rule to apply", not "reject": a results upload
     * against an assay whose type the curator has not picked yet still has to
     * go through.
     */
    @Test
    public void anUnresolvedBucketImposesNoRule() {
        assertNull("an unset assay type resolves to no results bucket",
                ZircAttachmentKind.forUpload(RESULT, null));
        ZircSubmissionService.validateUploadType(null, "anything.xyz");
    }

    /** Protocol documents are ungated: every assay type offers the bucket. */
    @Test
    public void theProtocolBucketResolvesRegardlessOfAssayType() {
        assertEquals(ZircAttachmentKind.PROTOCOL_DOC,
                ZircAttachmentKind.forUpload(PROTOCOL, "pcr_gel"));
        assertEquals(ZircAttachmentKind.PROTOCOL_DOC,
                ZircAttachmentKind.forUpload(PROTOCOL, null));
        assertTrue("PROTOCOL_DOC should carry no assay-type gate",
                ZircAttachmentKind.PROTOCOL_DOC.isUngated());
    }

    @Test
    public void anUnknownKindResolvesToNoBucket() {
        assertNull(ZircAttachmentKind.forUpload("not_a_kind", "pcr_gel"));
    }

    private static String expectRejection(ZircAttachmentKind bucket, String filename) {
        try {
            ZircSubmissionService.validateUploadType(bucket, filename);
            fail("expected " + filename + " to be rejected from " + bucket);
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

package org.zfin.zirc.api;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Guards the per-bucket accepted-file-type rules (ZFIN-10413, ZFIN-10417).
 *
 * <p>{@link ZircAttachmentKind} is read from two places that must agree —
 * the form schema (which publishes {@code accept} + helper text) and the
 * upload endpoint (which enforces it). These tests pin the mapping and the
 * extension matching so a change in one place cannot silently diverge.
 */
public class ZircAttachmentKindTest {

    /**
     * Every assay type the dropdown offers must resolve to exactly one
     * bucket. A type present in no bucket would render an assay form with
     * nowhere to attach files; a type in two would render two uploaders over
     * the same backing array.
     */
    @Test
    public void everyAssayTypeMapsToExactlyOneBucket() {
        List<String> unbucketed = new ArrayList<>();
        for (String assayType : ZircAssayFormSchema.ASSAY_TYPES) {
            List<ZircAttachmentKind> matches = new ArrayList<>();
            for (ZircAttachmentKind kind : ZircAttachmentKind.values()) {
                if (kind.getAssayTypes().contains(assayType)) {
                    matches.add(kind);
                }
            }
            if (matches.size() != 1) {
                unbucketed.add(assayType + " -> " + matches);
            }
        }
        assertEquals("assay types not in exactly one attachment bucket",
                List.of(), unbucketed);
    }

    /**
     * The reverse direction: a bucket must not claim an assay type the
     * dropdown no longer offers, which would leave a rule that can never fire.
     */
    @Test
    public void bucketsClaimOnlyKnownAssayTypes() {
        Set<String> known = new HashSet<>(ZircAssayFormSchema.ASSAY_TYPES);
        for (ZircAttachmentKind kind : ZircAttachmentKind.values()) {
            for (String assayType : kind.getAssayTypes()) {
                assertTrue(kind + " claims unknown assay type " + assayType,
                        known.contains(assayType));
            }
        }
    }

    @Test
    public void assayTypeLookupResolvesEachBucket() {
        assertSame(ZircAttachmentKind.GEL_IMAGE,
                ZircAttachmentKind.forAssayType("pcr_gel"));
        assertSame(ZircAttachmentKind.GEL_IMAGE,
                ZircAttachmentKind.forAssayType("sslp"));
        assertSame(ZircAttachmentKind.CHROMATOGRAM,
                ZircAttachmentKind.forAssayType("pcr_sequencing"));
        assertSame(ZircAttachmentKind.RESULT_IMAGE,
                ZircAttachmentKind.forAssayType("kasp"));
        assertSame(ZircAttachmentKind.MELT_CURVE,
                ZircAttachmentKind.forAssayType("hrma"));
    }

    /**
     * An unknown or absent assay type yields null, which the upload endpoint
     * reads as "no rule to apply" — never as "reject". A half-filled assay
     * whose type is not yet chosen must still accept an upload.
     */
    @Test
    public void unknownAssayTypeHasNoBucket() {
        assertNull(ZircAttachmentKind.forAssayType(null));
        assertNull(ZircAttachmentKind.forAssayType(""));
        assertNull(ZircAttachmentKind.forAssayType("not_an_assay_type"));
    }

    /** ZFIN-10417's list, plus the .ab1 spelling ABI sequencers emit. */
    @Test
    public void chromatogramBucketAcceptsTraceFormats() {
        ZircAttachmentKind kind = ZircAttachmentKind.CHROMATOGRAM;
        for (String name : List.of("trace.abi", "trace.ab1", "trace.scf",
                "reads.fa", "notes.txt", "summary.docx")) {
            assertTrue(name + " should be accepted", kind.accepts(name));
        }
    }

    /** Gel-image formats have no business in the chromatogram bucket. */
    @Test
    public void chromatogramBucketRejectsImages() {
        ZircAttachmentKind kind = ZircAttachmentKind.CHROMATOGRAM;
        for (String name : List.of("gel.png", "gel.tif", "gel.jpeg", "scan.pdf")) {
            assertFalse(name + " should be rejected", kind.accepts(name));
        }
    }

    /** ZFIN-10413: images plus PDF, which the old global list already allowed. */
    @Test
    public void gelImageBucketAcceptsImagesAndPdf() {
        ZircAttachmentKind kind = ZircAttachmentKind.GEL_IMAGE;
        for (String name : List.of("gel.tif", "gel.tiff", "gel.jpg", "gel.jpeg",
                "gel.png", "gel.gif", "gel.pdf")) {
            assertTrue(name + " should be accepted", kind.accepts(name));
        }
    }

    @Test
    public void gelImageBucketRejectsTraceFormats() {
        ZircAttachmentKind kind = ZircAttachmentKind.GEL_IMAGE;
        for (String name : List.of("trace.abi", "trace.ab1", "trace.scf",
                "sheet.xlsx", "archive.zip")) {
            assertFalse(name + " should be rejected", kind.accepts(name));
        }
    }

    /** Both image buckets take the same set — they must not drift apart. */
    @Test
    public void imageBucketsShareOneExtensionSet() {
        assertEquals(ZircAttachmentKind.GEL_IMAGE.getAcceptedExtensions(),
                ZircAttachmentKind.RESULT_IMAGE.getAcceptedExtensions());
    }

    /**
     * Melt curves stay unrestricted until curators enumerate the instrument
     * formats; the size cap is the only limit there.
     */
    @Test
    public void meltCurveBucketAcceptsAnything() {
        ZircAttachmentKind kind = ZircAttachmentKind.MELT_CURVE;
        assertTrue(kind.acceptsAnyExtension());
        assertTrue(kind.accepts("curve.weird"));
        assertTrue(kind.accepts("no_extension_at_all"));
    }

    /** A curator's file from Windows may well be uppercase. */
    @Test
    public void extensionMatchingIgnoresCase() {
        assertTrue(ZircAttachmentKind.GEL_IMAGE.accepts("GEL.PNG"));
        assertTrue(ZircAttachmentKind.GEL_IMAGE.accepts("Gel.TifF"));
        assertTrue(ZircAttachmentKind.CHROMATOGRAM.accepts("TRACE.AB1"));
    }

    /** The last dot wins, so a compound name still resolves. */
    @Test
    public void extensionReadsTheLastDot() {
        assertEquals("ab1", ZircAttachmentKind.extensionOf("trace.raw.ab1"));
        assertTrue(ZircAttachmentKind.CHROMATOGRAM.accepts("trace.raw.ab1"));
        // A dotted name whose tail is not an accepted type is still rejected.
        assertFalse(ZircAttachmentKind.CHROMATOGRAM.accepts("trace.ab1.exe"));
    }

    /**
     * No extension means we cannot show the file matches, so a restricted
     * bucket rejects it rather than letting it through unchecked.
     */
    @Test
    public void namesWithoutAnExtensionAreRejectedByRestrictedBuckets() {
        assertNull(ZircAttachmentKind.extensionOf("README"));
        assertNull(ZircAttachmentKind.extensionOf("trailing."));
        assertNull(ZircAttachmentKind.extensionOf(null));
        assertFalse(ZircAttachmentKind.GEL_IMAGE.accepts("README"));
        assertFalse(ZircAttachmentKind.GEL_IMAGE.accepts("trailing."));
        assertFalse(ZircAttachmentKind.GEL_IMAGE.accepts(null));
    }

    /** Helper text is generated from the list, so it cannot drift from it. */
    @Test
    public void acceptedExtensionsDisplayIsDotPrefixedAndOrdered() {
        assertEquals(".abi, .ab1, .scf, .fa, .txt, .docx",
                ZircAttachmentKind.CHROMATOGRAM.getAcceptedExtensionsDisplay());
        assertEquals(".tif, .tiff, .jpg, .jpeg, .png, .gif, .pdf",
                ZircAttachmentKind.GEL_IMAGE.getAcceptedExtensionsDisplay());
        assertEquals("", ZircAttachmentKind.MELT_CURVE.getAcceptedExtensionsDisplay());
    }

    /**
     * The served Content-Type comes from the extension, never from the
     * upload — the download endpoint serves attachments with an inline
     * Content-Disposition, so an uploader who could dictate the type could
     * turn an attachment into stored XSS.
     */
    @Test
    public void contentTypeIsDerivedFromTheExtension() {
        assertEquals("image/png", ZircAttachmentKind.contentTypeFor("gel.png"));
        assertEquals("image/tiff", ZircAttachmentKind.contentTypeFor("gel.tif"));
        assertEquals("image/tiff", ZircAttachmentKind.contentTypeFor("gel.tiff"));
        assertEquals("image/jpeg", ZircAttachmentKind.contentTypeFor("gel.jpg"));
        assertEquals("image/jpeg", ZircAttachmentKind.contentTypeFor("gel.jpeg"));
        assertEquals("image/gif", ZircAttachmentKind.contentTypeFor("gel.gif"));
        assertEquals("application/pdf", ZircAttachmentKind.contentTypeFor("scan.pdf"));
        assertEquals("text/plain", ZircAttachmentKind.contentTypeFor("notes.txt"));
        assertEquals("text/plain", ZircAttachmentKind.contentTypeFor("reads.fa"));
    }

    /** Uppercase names must resolve to the same type, not the fallback. */
    @Test
    public void contentTypeIgnoresCase() {
        assertEquals("image/png", ZircAttachmentKind.contentTypeFor("GEL.PNG"));
        assertEquals("application/pdf", ZircAttachmentKind.contentTypeFor("Scan.PDF"));
    }

    /**
     * Trace formats and anything unrecognized are served as a download, so
     * nothing tries to render them in the browser.
     */
    @Test
    public void unrenderableAndUnknownTypesFallBackToOctetStream() {
        for (String name : List.of("trace.abi", "trace.ab1", "trace.scf",
                "curve.weird", "no_extension", "trailing.")) {
            assertEquals(name + " should be served as a download",
                    ZircAttachmentKind.DEFAULT_CONTENT_TYPE,
                    ZircAttachmentKind.contentTypeFor(name));
        }
        assertEquals(ZircAttachmentKind.DEFAULT_CONTENT_TYPE,
                ZircAttachmentKind.contentTypeFor(null));
    }

    /**
     * The types that make an inline response dangerous must never be
     * derivable, whatever the file is called. An uploader controls the
     * filename, so this is the guard that matters.
     */
    @Test
    public void noFilenameYieldsAnInlineScriptableType() {
        for (String name : List.of("evil.html", "evil.htm", "evil.svg",
                "evil.xhtml", "evil.xml", "evil.js", "gel.png.html",
                "evil.HTML", "evil.SVG")) {
            String served = ZircAttachmentKind.contentTypeFor(name);
            assertFalse(name + " must not be served as a scriptable type: " + served,
                    served.contains("html") || served.contains("svg")
                            || served.contains("xml") || served.contains("javascript"));
        }
    }

    /** Every accepted extension must map to a type we chose deliberately. */
    @Test
    public void everyAcceptedExtensionHasADerivableContentType() {
        for (ZircAttachmentKind kind : ZircAttachmentKind.values()) {
            for (String ext : kind.getAcceptedExtensions()) {
                String served = ZircAttachmentKind.contentTypeFor("file." + ext);
                // The trace formats intentionally fall back to a download.
                boolean trace = List.of("abi", "ab1", "scf").contains(ext);
                if (trace) {
                    assertEquals(ZircAttachmentKind.DEFAULT_CONTENT_TYPE, served);
                } else {
                    assertFalse(ext + " should have an explicit content type",
                            ZircAttachmentKind.DEFAULT_CONTENT_TYPE.equals(served));
                }
            }
        }
    }

    /** Extensions are stored dot-less and lowercase; the display adds the dot. */
    @Test
    public void storedExtensionsAreNormalized() {
        for (ZircAttachmentKind kind : ZircAttachmentKind.values()) {
            for (String ext : kind.getAcceptedExtensions()) {
                assertFalse(kind + " extension should not carry a dot: " + ext,
                        ext.startsWith("."));
                assertEquals(kind + " extension should be lowercase",
                        ext.toLowerCase(java.util.Locale.ROOT), ext);
            }
        }
    }
}

package org.zfin.zirc.dto;

import org.junit.Test;
import org.zfin.zirc.entity.GenotypingAssay;
import org.zfin.zirc.entity.GenotypingAssayFile;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * The af_kind split (ZFIN-10415). {@code GenotypingAssay.files} is one
 * unordered Set holding both upload buckets; AssayDTO is what turns it back
 * into the two arrays the two Controls bind to. Getting the split wrong shows
 * up as a protocol document appearing under "Annotated gel images" (or
 * vanishing from the form entirely), which no compiler or schema check
 * catches — hence a test at this seam specifically.
 */
public class AssayDTOTest {

    @Test
    public void filesAreSplitIntoTheTwoBuckets() {
        GenotypingAssay assay = assayWith(
                file(1L, "gel.png", GenotypingAssayFile.KIND_ASSAY_RESULT, 1_000L),
                file(2L, "protocol.pdf", GenotypingAssayFile.KIND_PROTOCOL_DOC, 2_000L),
                file(3L, "gel2.png", GenotypingAssayFile.KIND_ASSAY_RESULT, 3_000L));

        AssayDTO dto = AssayDTO.of(assay);

        assertEquals(List.of("gel.png", "gel2.png"), filenames(dto.attachments()));
        assertEquals(List.of("protocol.pdf"), filenames(dto.protocolDocuments()));
    }

    @Test
    public void eachBucketIsSortedOldestFirst() {
        GenotypingAssay assay = assayWith(
                file(1L, "newer.pdf", GenotypingAssayFile.KIND_PROTOCOL_DOC, 5_000L),
                file(2L, "older.pdf", GenotypingAssayFile.KIND_PROTOCOL_DOC, 1_000L));

        AssayDTO dto = AssayDTO.of(assay);

        assertEquals(List.of("older.pdf", "newer.pdf"), filenames(dto.protocolDocuments()));
    }

    /**
     * af_kind is NOT NULL as of ZFIN-10415 and existing rows were backfilled,
     * so a null kind means a row written outside the service. It still has to
     * land in a bucket — a file the form cannot show is a file the curator
     * cannot delete either.
     */
    @Test
    public void nullKindFallsBackToTheResultsBucket() {
        GenotypingAssay assay = assayWith(file(1L, "legacy.png", null, 1_000L));

        AssayDTO dto = AssayDTO.of(assay);

        assertEquals(List.of("legacy.png"), filenames(dto.attachments()));
        assertEquals(List.of(), filenames(dto.protocolDocuments()));
    }

    @Test
    public void anAssayWithNoFilesGivesTwoEmptyBuckets() {
        AssayDTO dto = AssayDTO.of(assayWith());

        assertEquals(List.of(), filenames(dto.attachments()));
        assertEquals(List.of(), filenames(dto.protocolDocuments()));
    }

    // ─── fixtures ──────────────────────────────────────────────────────

    private static GenotypingAssay assayWith(GenotypingAssayFile... files) {
        GenotypingAssay assay = new GenotypingAssay();
        assay.setId(42L);
        assay.setSortOrder(1);
        assay.setAssayType("pcr_gel");
        assay.setFiles(new LinkedHashSet<>(Set.of(files)));
        return assay;
    }

    private static GenotypingAssayFile file(Long id, String name, String kind, long uploadedAtMillis) {
        GenotypingAssayFile f = new GenotypingAssayFile();
        f.setId(id);
        f.setOriginalFilename(name);
        f.setKind(kind);
        f.setStoredPath("/tmp/" + name);
        f.setUploadedAt(new Date(uploadedAtMillis));
        return f;
    }

    private static List<String> filenames(List<AssayFileDTO> files) {
        return files.stream().map(AssayFileDTO::originalFilename).toList();
    }
}

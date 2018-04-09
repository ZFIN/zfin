package org.zfin.datatransfer.go;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

/**
 * Test gaf parsing, etc.
 */
public class GafParserUnitTest {

    private FpInferenceGafParser gafParser = new FpInferenceGafParser();

    private final String GAF_GOA_DIRECTORY = "test/gaf/goa/";
    private final String FP_INFERENCE_DIRECTORY = "test/gaf/fp_inference/";
    private final String GPAD_DIRECTORY = "test/gaf/gpad/";
    private final String PAINT_DIRECTORY = "test/gaf/paint/";

    @Test
    public void parseGafLine() throws Exception {
        File testFile1 = new File(GAF_GOA_DIRECTORY + "gene_association.goa_zebrafish_parsetest");
        List<GafEntry> gafEntries = gafParser.parseGafFile(testFile1);
        assertEquals(1, gafEntries.size());
        GafEntry gafEntry = gafEntries.get(0);
        assertEquals("Q7ZVV0", gafEntry.getEntryId());
        assertEquals("", gafEntry.getQualifier());
        assertEquals("GO:0001947", gafEntry.getGoTermId());
        assertEquals("PMID:16399798", gafEntry.getPubmedId());
        assertEquals("IMP", gafEntry.getEvidenceCode());
        assertEquals("", gafEntry.getInferences());
        assertEquals("taxon:7955", gafEntry.getTaxonId());
        assertEquals("20080521", gafEntry.getCreatedDate());
        assertEquals("BHF-UCL", gafEntry.getCreatedBy());
    }

    @Test
    public void fpInferenceLine() throws Exception {
        File testFile1 = new File(FP_INFERENCE_DIRECTORY + "gene_association.zfin_test1");
        List<GafEntry> gafEntries = gafParser.parseGafFile(testFile1);
        assertEquals(23, gafEntries.size()); // a lot of ZFIN entries
        GafEntry gafEntry = gafEntries.get(0);
        assertEquals("ZDB-GENE-000125-12", gafEntry.getEntryId());
        assertEquals("", gafEntry.getQualifier());
        assertEquals("GO:0008285", gafEntry.getGoTermId());
        assertEquals("ZFIN:ZDB-PUB-000111-5|PMID:10611375", gafEntry.getPubmedId());
        assertEquals("IMP", gafEntry.getEvidenceCode());
        assertEquals("", gafEntry.getInferences());
        assertEquals("taxon:7955", gafEntry.getTaxonId());
        assertEquals("20090605", gafEntry.getCreatedDate());
        assertEquals("UniProtKB", gafEntry.getCreatedBy());
    }

    @Test
    public void gpadParser() throws Exception {
        File testFile1 = new File(GPAD_DIRECTORY + "zfin.gpad");
        GpadParser parser = new GpadParser();
        List<GafEntry> gafEntries = parser.parseGafFile(testFile1);
        parser.postProcessing(gafEntries);
        assertThat(gafEntries, hasSize(11));
        GafEntry gafEntry = gafEntries.get(0);
        assertThat("ZDB-GENE-040426-2330", equalTo(gafEntry.getEntryId()));
        assertThat("", equalTo(gafEntry.getQualifier()));
        assertEquals("GO:0002244", gafEntry.getGoTermId());
        assertEquals("ZFIN:ZDB-PUB-170214-264", gafEntry.getPubmedId());
        assertEquals("IMP", gafEntry.getEvidenceCode());
        assertNull(gafEntry.getInferences());
        assertNull(gafEntry.getTaxonId());
        assertEquals("20180402", gafEntry.getCreatedDate());
        assertEquals("ZFIN", gafEntry.getCreatedBy());
        assertEquals("gomodel:59dc728000000555", gafEntry.getModelID());

        // last line has an annotation extension
        gafEntry = gafEntries.get(10);
        assertEquals("occurs_in(UBERON:0002329),causally_upstream_of(GO:0002244)", gafEntry.getAnnotExtn());
        // second line has TAS as evidence code
        assertEquals("TAS", gafEntries.get(1).getEvidenceCode());

    }

    @Test
    public void gpadParserAnnotationExtension() throws Exception {
        String line = "occurs_in(UBERON:0002329),causally_upstream_of(GO:0002244)";

        FpInferenceGafParser parser = new FpInferenceGafParser();
        List<GafAnnotationGroup> groups = parser.parseAnnotationExtension(line);
        // 1 overall group
        assertThat(groups, hasSize(1));
        // 2 comma-separated components
        assertThat(groups.get(0).annotationExtensions, hasSize(2));

        assertThat(groups.get(0).annotationExtensions.get(0).getRelationshipName(), equalTo("occurs_in"));
        assertThat(groups.get(0).annotationExtensions.get(0).getAnnotationTermID(), equalTo("UBERON:0002329"));

        line = "regulates_expression_of(ENSEMBL:ENSDARG00000026784),positively_regulates(GO:0003262)|regulates_expression_of(ENSEMBL:ENSDARG00000014891)";
        groups = parser.parseAnnotationExtension(line);
        // 2 overall group
        assertThat(groups, hasSize(2));
        assertThat(groups.get(0).annotationExtensions, hasSize(2));
        assertThat(groups.get(1).annotationExtensions, hasSize(1));
        assertThat(groups.get(0).annotationExtensions.get(0).getRelationshipName(), equalTo("regulates_expression_of"));
        assertThat(groups.get(1).annotationExtensions.get(0).getAnnotationTermID(), equalTo("ENSEMBL:ENSDARG00000014891"));

    }

    @Test
    public void fpInferProteinID() throws Exception {
        File testFile1 = new File(FP_INFERENCE_DIRECTORY + "gene_association.zfin_test4");
        List<GafEntry> gafEntries = gafParser.parseGafFile(testFile1);
        assertEquals(3, gafEntries.size()); // a lot of ZFIN entries
        GafEntry gafEntry = gafEntries.get(0);
        assertEquals("ZDB-GENE-050628-1", gafEntry.getEntryId());
        assertEquals("", gafEntry.getQualifier());
        assertEquals("GO:0005515", gafEntry.getGoTermId());
        assertEquals("ZFIN:ZDB-PUB-070122-12|PMID:17196985", gafEntry.getPubmedId());
        assertEquals("IPI", gafEntry.getEvidenceCode());
        assertEquals("GenPept:NP_571030", gafEntry.getInferences());
        assertEquals("taxon:7955", gafEntry.getTaxonId());
        assertEquals("20070521", gafEntry.getCreatedDate());
        //assertEquals("GOC", gafEntry.getCreatedBy());
        assertEquals("GenPept:NP_036859|GenPept:NP_598289|GenPept:NP_604463", gafEntries.get(1).getInferences());
        assertEquals("GenPept:AAA58428", gafEntries.get(2).getInferences());
    }

    @Test
    public void fpInferEMBL() throws Exception {
        File testFile1 = new File(FP_INFERENCE_DIRECTORY + "gene_association.zfin_test5");
        List<GafEntry> gafEntries = gafParser.parseGafFile(testFile1);
        assertEquals(3, gafEntries.size()); // a lot of ZFIN entries
        GafEntry gafEntry = gafEntries.get(0);
        assertEquals("ZDB-GENE-020919-4", gafEntry.getEntryId());
        assertEquals("", gafEntry.getQualifier());
        assertEquals("GO:0008373", gafEntry.getGoTermId());
        assertEquals("ISS", gafEntry.getEvidenceCode());
        assertEquals("GenBank:AF004668", gafEntry.getInferences());
        assertEquals("GenBank:D26360", gafEntries.get(1).getInferences());
        assertEquals("GenBank:AY882944", gafEntries.get(2).getInferences());

    }

    @Test
    public void parseGafExcludeAllButOne() throws Exception {
        File file = new File(GAF_GOA_DIRECTORY + "gene_association.goa_zebrafish_parseonly");
        List<GafEntry> gafEntries = gafParser.parseGafFile(file);
        assertEquals(1, gafEntries.size());
    }

    @Test
    public void testInferencePipe() {
        assertEquals(1, Arrays.asList("UniprotKB:ABCD".split("\\|")).size());
        assertEquals(2, Arrays.asList("UniprotKB:ABCD|DOGS:CEEFG".split("\\|")).size());

    }

    @Test
    public void testIntersection() {
        Set<String> a = new HashSet<String>();
        Set<String> b = new HashSet<String>();
        assertEquals(CollectionUtils.intersection(a, b).size(), a.size());
        b.add("1");
        assertEquals(CollectionUtils.intersection(a, b).size(), a.size());
        a.add("1");
        assertEquals(CollectionUtils.intersection(a, b).size(), a.size());
        a.add("2");
        assertNotSame(CollectionUtils.intersection(a, b).size(), a.size());
    }


}

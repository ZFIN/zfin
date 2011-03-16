package org.zfin.datatransfer.go;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

/**
 * Test gaf parsing, etc.
 */
public class GafParserUnitTest {

    private GafParser gafParser = new GafParser();

    private final String GAF_DIRECTORY = "test/goa_go/";

    @Test
    public void parseGafLine() throws Exception {
        File testFile1 = new File(GAF_DIRECTORY + "gene_association.goa_zebrafish_parsetest");
        List<GafEntry> gafEntries = gafParser.parseGafFile(testFile1);
        assertEquals(1, gafEntries.size());
        GafEntry gafEntry = gafEntries.get(0);
        assertEquals("Q7ZVV0", gafEntry.getUniprotId());
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
    public void parseGafExcludeAllButOne() throws Exception {
        File file = new File(GAF_DIRECTORY + "gene_association.goa_zebrafish_parseonly");
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

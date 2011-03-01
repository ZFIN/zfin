package org.zfin.datatransfer.microarray;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.zfin.TestConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests parsing for microarrays
 */
public class MicroarrayParseTest {

    File file = new File("test/GPLTest_family.soft");

    @Before
    public void setup() {
        TestConfiguration.configure();
    }

    @Test
    public void parseLine() {
        DefaultGeoSoftParser parser = new DefaultGeoSoftParser();
        parser.setAccessionColumn(5);
        String accessionNumber = parser.parseLine("227825\tCGENXEB_456016237_0\t0\tfj38d02.x1 zebrafish adult brain Danio rerio cDNA 3', mRNA sequence.\tAW233581\t");
        assertEquals("AW233581", accessionNumber);
    }


    @Test
    public void parseFile() {
        DefaultGeoSoftParser parser = new DefaultGeoSoftParser();
        parser.setFileName("GPLTest");
        parser.setAccessionColumn(5);
        Set<String> uniqueNumbers = parser.parseUniqueNumbers(file);
        assertEquals(6, uniqueNumbers.size());
    }

    @Test
    public void parseFileIncludesDanioRerio() {
        DefaultGeoSoftParser parser = new DefaultGeoSoftParser();
        parser.setFileName("GPLTest");
        parser.setAccessionColumn(5);
        parser.setIncludePatterns(new String[]{"Danio rerio"});
        Set<String> uniqueNumbers = parser.parseUniqueNumbers(file);
        assertEquals(5, uniqueNumbers.size());
    }

    @Test
    public void parseFileIncludesDanioRerioExcludeControl() {
        DefaultGeoSoftParser parser = new DefaultGeoSoftParser();
        parser.setFileName("GPLTest");
        parser.setAccessionColumn(5);
        parser.setIncludePatterns(new String[]{"Danio rerio"});
        parser.setExcludePatterns(new String[]{"CONTROL"});
        Set<String> uniqueNumbers = parser.parseUniqueNumbers(file);
        assertEquals(4, uniqueNumbers.size());
    }

    @Test
    public void fixAccession1() {
        String accession = "ABC123.1";
        accession = new DefaultGeoSoftParser().fixAccession(accession);
        assertEquals("ABC123", accession);
    }

    @Test
    public void fixAccession2() {
        String accession = "ABC123_1";
        accession = new DefaultGeoSoftParser().fixAccession(accession);
        assertEquals("ABC123", accession);
        accession = "NM_ABC123_1";
        accession = new DefaultGeoSoftParser().fixAccession(accession);
        assertEquals("NM_ABC123", accession);
    }


    @Test
    public void intersectionFun() {
        List newList = new ArrayList();
        newList.add("a");
        newList.add("b");
        newList.add("c");

        List oldList = new ArrayList();
        oldList.add("a");
        oldList.add("c");
        oldList.add("d");
        oldList.add("e");

        Collection thingsToAdd = CollectionUtils.subtract(newList, oldList);
        assertEquals(1, thingsToAdd.size());
        assertEquals("b", thingsToAdd.iterator().next().toString());
        Collection thingsToRemove = CollectionUtils.subtract(oldList, newList);
        assertEquals(2, thingsToRemove.size());
        assertTrue(thingsToRemove.contains("d"));
        assertTrue(thingsToRemove.contains("e"));
        Set oldSet = new HashSet(oldList);
        Set newSet = new HashSet(newList);

        // this to add
        newSet.removeAll(oldList);
        assertEquals(1, newSet.size());
        assertEquals("b", newSet.iterator().next().toString());

        // this to remove
        oldSet.removeAll(newList);
        assertEquals(2, oldSet.size());
        assertTrue(oldSet.contains("d"));
        assertTrue(oldSet.contains("e"));

    }

    @Test
    public void microarrayBeanTest() {
        try {
            System.out.println(System.getProperty("java.io.tmpdir"));
            MicroarrayBean microarrayBean = new MicroarrayBean();
            File file = microarrayBean.getFile();
            microarrayBean.addMessage("asdfa");
            microarrayBean.addNotFound("hhhh");
            microarrayBean.addNotFound("gggg");
            microarrayBean.addMessage("bbbbb");
            String retrieve = microarrayBean.finishReadingAndRetrieve();
            String validString = "file[" + file.getAbsolutePath() + "]\n" +
                    "asdfa\n" +
                    "not found[hhhh]\n" +
                    "not found[gggg]\n" +
                    "bbbbb\n";
            assertEquals(validString, retrieve);
            String retrieve2 = microarrayBean.finishReadingAndRetrieve();
            assertEquals(validString, retrieve2);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }

    }
}

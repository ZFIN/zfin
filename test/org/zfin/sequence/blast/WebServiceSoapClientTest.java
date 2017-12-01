package org.zfin.sequence.blast;

import org.junit.Test;
import org.zfin.datatransfer.webservice.NCBIEfetch;
import org.zfin.sequence.Defline;
import org.zfin.sequence.Sequence;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class WebServiceSoapClientTest {

    @Test
    public void useEfetchForProtein() {
        doAccessionTest("P26630", NCBIEfetch.Type.POLYPEPTIDE);
    }

    @Test
    public void useEfetchForProtein2() {
        doAccessionTest("NP_571379", NCBIEfetch.Type.POLYPEPTIDE);
    }

    @Test
    public void useEfetchForNucleotide() {
        doAccessionTest("AY627769", NCBIEfetch.Type.NUCLEOTIDE);
    }

    @Test
    public void useEfetchForNewSequence() {
        doAccessionTest("JF828767", NCBIEfetch.Type.NUCLEOTIDE);
    }

    @Test
    public void useEfetchForBadSequence() {
        List<Sequence> sequences = NCBIEfetch.getSequenceForAccession("notasequencethatIknowof", NCBIEfetch.Type.POLYPEPTIDE);
        assertThat(sequences, is(empty()));
    }

    @Test
    public void useEfetchForNucleotide2() {
        doAccessionTest("FN428721", NCBIEfetch.Type.NUCLEOTIDE);
    }

    @Test
    public void useEfetchForNucleotideWithMultipleReturn() {
        doAccessionTest("X63183", NCBIEfetch.Type.NUCLEOTIDE);
    }

    @Test
    public void validateAccessions() {
        String[] goodAccessions = {"NP_571379", "XP_009296153"};
        for (String accession : goodAccessions) {
            assertThat(accession + " should validate", NCBIEfetch.validateAccession(accession), is(true));
        }

        String[] badAccessions = {"NOT_GOOD_VERY_VERY_BAD_ACCESSION"};
        for (String accession : badAccessions) {
            assertThat(accession + " should not validate", NCBIEfetch.validateAccession(accession), is(false));
        }
    }

    @Test
    public void hasMicroarray() {
        assertThat("rpf1 should have microarray data", NCBIEfetch.hasMicroarrayData(new HashSet<>(), "rpf1"), is(true));
        assertThat("abcdefg should not have microarray data", NCBIEfetch.hasMicroarrayData(new HashSet<>(), "abcdefg"), is(false));

        // for ZDB-EST-010111-34 a rare clone with GEO expression
        Set<String> strings = new HashSet<>();
        strings.add("AI584640");
        strings.add("AI545457");
        assertThat(NCBIEfetch.hasMicroarrayData(strings), is(true));
        Set<String> strings2 = new HashSet<>();
        strings2.add("BBBBBBBB");
        assertThat(NCBIEfetch.hasMicroarrayData(strings2), is(false));
    }

    @Test
    public void getLink() {
        List<String> accessions = new ArrayList<>();
        accessions.add("AB");
        accessions.add("CD");
        String link = NCBIEfetch.createMicroarrayQuery(accessions, "dogz");
        assertThat(link, is("txid7955[organism] AND (dogz[gene symbol] OR (AB OR AB.* OR CD OR CD.*))"));
    }

    @Test
    public void testNucleodiesNcbi() throws Exception {
        String accession = "KC818433";
        List<Sequence> sequences = NCBIEfetch.getSequenceForAccession(accession, NCBIEfetch.Type.NUCLEOTIDE);
        assertThat(sequences, not(empty()));
    }

    private void doAccessionTest(String accession, NCBIEfetch.Type type) {
        List<Sequence> sequences = NCBIEfetch.getSequenceForAccession(accession, type);
        assertThat("One sequence should be found for accession " + accession, sequences, hasSize(1));

        Sequence sequence = sequences.get(0);
        Defline defline = sequence.getDefLine();
        assertThat(defline.getAccession(), is(accession));
        assertThat("Formatted data for " + accession + " too short", sequence.getFormattedData().length(), greaterThan(100));
        assertThat("Formatted sequence for " + accession + " too short", sequence.getFormattedSequence().length(), greaterThan(100));
        assertThat("Defline for " + accession + " too short", defline.toString().length(), greaterThan(20));
    }

}

package org.zfin.datatransfer.webservice;

import gov.nih.nlm.ncbi.www.soap.eutils.EUtilsServiceStub;
import org.junit.Test;
import org.zfin.datatransfer.ServiceConnectionException;
import org.zfin.datatransfer.microarray.GeoMicorarrayEntriesBean;
import org.zfin.sequence.Sequence;

import java.util.List;

import static junit.framework.Assert.assertNotNull;


public class NcbiEfetchTest {


    @Test
    public void testGeoProfiles() throws Exception {
        EUtilsServiceStub service = new EUtilsServiceStub();
        EUtilsServiceStub.ESearchRequest request = new EUtilsServiceStub.ESearchRequest();
        request.setDb("geoprofiles");
        request.setTerm("txid7955[organism]");
        request.setRetStart("0");
        request.setRetMax("0");
        request.setUsehistory("y");
        EUtilsServiceStub.ESearchResult result = service.run_eSearch(request);
        String webEnvKey = result.getWebEnv();

        int totalGeoAccessions = Integer.parseInt(result.getCount());
        EUtilsServiceStub.ESummaryRequest summaryRequest = new EUtilsServiceStub.ESummaryRequest();
        summaryRequest.setWebEnv(webEnvKey);
        summaryRequest.setRetstart("1");
        summaryRequest.setRetmax("100");
        summaryRequest.setDb("geoprofiles");
        summaryRequest.setQuery_key("1");

        EUtilsServiceStub.ESummaryResult eSummaryResult = service.run_eSummary(summaryRequest);
         assertNotNull(eSummaryResult);
    }

    @Test
    public void testNucleodiesNcbi() throws Exception {
        String accession = "KC818433";
        List<Sequence> sequences = NCBIEfetch.getSequenceForAccession(accession, NCBIEfetch.Type.NUCLEOTIDE);
        assertNotNull(sequences);
    }
}
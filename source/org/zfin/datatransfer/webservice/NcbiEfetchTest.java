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
        request.setDb("pubmed");
        request.setTerm("21225340");
        EUtilsServiceStub.ESearchResult result = service.run_eSearch(request);
        String webEnvKey = result.getWebEnv();

        int totalGeoAccessions = Integer.parseInt(result.getCount());
        EUtilsServiceStub.ESummaryRequest summaryRequest = new EUtilsServiceStub.ESummaryRequest();
        summaryRequest.setWebEnv(webEnvKey);
        summaryRequest.setDb("pubmed");

        EUtilsServiceStub.ESummaryResult eSummaryResult = service.run_eSummary(summaryRequest);
         assertNotNull(eSummaryResult);
    }

    @Test
    public void testNucleodiesNcbi() throws Exception {
        String accession = "KC818433";
        List<Sequence> sequences = NCBIEfetch.getSequenceForAccession(accession, NCBIEfetch.Type.NUCLEOTIDE);
        assertNotNull(sequences);
    }

    public static void main(String[] args) throws Exception
    {
        // eInfo utility returns a list of available databases
        try
        {
            EUtilsServiceStub service = new EUtilsServiceStub();

            // call NCBI EInfo utility
            EUtilsServiceStub.EInfoRequest req = new EUtilsServiceStub.EInfoRequest();
            EUtilsServiceStub.EInfoResult res = service.run_eInfo(req);
            // results output
            for(int i=0; i<res.getDbList().getDbName().length; i++)
            {
                System.out.println(res.getDbList().getDbName()[i]);
            }
        }
        catch(Exception e) { System.out.println(e.toString()); }

        try
        {
            EUtilsServiceStub service = new EUtilsServiceStub();
            // call NCBI ESearch utility
            // NOTE: search term should be URL encoded
            EUtilsServiceStub.ESearchRequest req = new EUtilsServiceStub.ESearchRequest();
            req.setDb("pubmed");
            req.setTerm("21225340");
            req.setRetMax("15");
            EUtilsServiceStub.ESearchResult res = service.run_eSearch(req);
            // results output
            //System.out.println("Original query: stem cells AND free fulltext[filter]");
            System.out.println("Found ids: " + res.getCount());
            System.out.print("First " + res.getRetMax() + " ids: ");
            for (int i = 0; i < res.getIdList().getId().length; i++)
            {
                System.out.print(res.getIdList().getId()[i] + " ");
            }
            System.out.println();
        }
        catch (Exception e) { System.out.println(e.toString()); }

        try
        {
            EUtilsServiceStub service = new EUtilsServiceStub();
            // call NCBI ESummary utility
            EUtilsServiceStub.ESummaryRequest req = new EUtilsServiceStub.ESummaryRequest();
/*
            req.setDb("nucleotide");
            req.setId("28864546,28800981");
*/
            req.setDb("pubmed");
            req.setId("21225340");
            EUtilsServiceStub.ESummaryResult res = service.run_eSummary(req);
            // results output
            for(int i=0; i<res.getDocSum().length; i++)
            {
                System.out.println("ID: "+res.getDocSum()[i].getId());
                for (int k = 0; k < res.getDocSum()[i].getItem().length; k++)
                {
                    System.out.println("    " + res.getDocSum()[i].getItem()[k].getName() +
                            ": " + res.getDocSum()[i].getItem()[k].getItemContent());
                }
            }
            System.out.println("-----------------------\n");
        }
        catch(Exception e) { System.out.println(e.toString()); }
    }

}
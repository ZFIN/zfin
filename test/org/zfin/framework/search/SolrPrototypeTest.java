package org.zfin.framework.search;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.zfin.AbstractSolrTest;
import org.junit.Test;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.zfin.search.service.SolrService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;



public class SolrPrototypeTest extends AbstractSolrTest {

    private SolrService solrService = new SolrService();
    private static String CORE = "prototype";


    @Test
    public void getAResult() {
        SolrClient server = solrService.getSolrClient(CORE);

        String name = "fgf8a";

        SolrQuery query = new SolrQuery();

        query.setQuery(name);

        QueryResponse response = new QueryResponse();
        try {
            response = server.query(query);
        } catch (Exception e) {
            logger.error(e);
        }

        assertNotNull("Response from Solr query should not be null", response);

        SolrDocumentList documentList = response.getResults();
        assertTrue("Query should return at least one result", documentList.getNumFound() > 0);
        assertNotNull("First result shouldn't be null", documentList.get(0));

        SolrDocument document = documentList.get(0);

        assertNotNull("first document should have an id", document.getFieldValue("id"));
        assertNotNull("first document should have a name", document.getFieldValue("name"));
    }

    @Test
    public void getFacets() {
        SolrClient server = solrService.getSolrClient(CORE);

        String name = "fgf8a";

        SolrQuery query = new SolrQuery();
        query.setQuery(name);

        String facetFieldName = "author";

        query.setFacet(true);
        query.addFacetField(facetFieldName);

        QueryResponse response = new QueryResponse();
        try {
            response = server.query(query);
        } catch (Exception e) {
            logger.error(e);
        }


        FacetField facetField = response.getFacetField(facetFieldName);

        assertNotNull("FacetField should not be null", facetField);
        assertEquals("FacetField " + facetFieldName + " should be named " + facetFieldName, facetFieldName, facetField.getName());


        assertNotNull("First facet value is not null", facetField.getValues().get(0));
        assertNotNull("First facet value has a name", facetField.getValues().get(0).getName());
        assertNotNull("First facet value has a count", facetField.getValues().get(0).getCount());
    }

        /*
      filterQueries for successively higher ontology terms should bring back a greater than or equal number of results
     */
    @Test
    public void walkUpAnatomy() {
        SolrClient server = solrService.getSolrClient(CORE);

        List<String> terms = new ArrayList<String>();
        terms.add("primary olfactory fiber layer");
        terms.add("olfactory bulb");
        terms.add("telencephalon");
        terms.add("forebrain");
        terms.add("brain");
        terms.add("central nervous system");
        terms.add("nervous system");

        long lastNumFound = -1;
        for (String term : terms) {
            SolrQuery query = new SolrQuery();
            query.addFilterQuery("expression_anatomy_synonyms:" + ClientUtils.escapeQueryChars(term));
            query.addFilterQuery("category:Figure");
            logger.error(query);
            QueryResponse response = new QueryResponse();
            try {
                response = server.query(query);
            } catch (Exception e) {
                logger.error(e);
            }
            SolrDocumentList documentList = response.getResults();
            long thisNumFound = documentList.getNumFound();

            assertFalse("A parent ontology term should never have less results than it's child: " + term, thisNumFound < lastNumFound);

            assertNotNull(response);
            assertNotNull(documentList);
            logger.error("term: " + term);
            logger.error("lastNumFound: " + lastNumFound);
            logger.error("thisNumFound: " + thisNumFound);
            lastNumFound = thisNumFound;
        }

    }
}

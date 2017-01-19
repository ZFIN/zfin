package org.zfin.expression.service;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.Group;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.zfin.expression.presentation.ExpressionSearchCriteria;
import org.zfin.expression.presentation.FigureResult;
import org.zfin.expression.presentation.GeneResult;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by kschaper on 1/17/17.
 */
public class ExpressionSearchService {


    private static SolrClient solrClient;

    public static SolrClient getSolrClient() {
        //this method should be replaced with ZFIN SolrService call
        if (solrClient == null) {
            solrClient = new HttpSolrClient("http://localhost:9500/solr/prototype");
        }

        return solrClient;
    }

    public static SolrDocumentList getGeneResults(ExpressionSearchCriteria criteria) {

        SolrQuery solrQuery = new SolrQuery();
        //todo: this should match against names, symbols, aliases, etc
        //not sure if this should be a request handler, or maybe aliasing
        //a fake field name to match all...
        solrQuery.addFilterQuery("name:(" + criteria.getGeneField() + ")");
        if (criteria.getAnatomy() != null) {
            for (String term : criteria.getAnatomy()) {
                solrQuery.addFilterQuery("anatomy_tf:(" + term + ")");
            }
        }
        solrQuery.addFilterQuery("type:Gene");

        QueryResponse queryResponse = null;
        try {
            queryResponse = getSolrClient().query(solrQuery);
        } catch (SolrServerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return queryResponse.getResults();
    }

    public static List<SolrDocument> getFigureResults(ExpressionSearchCriteria criteria) {
        SolrQuery solrQuery = new SolrQuery();

        solrQuery.addFilterQuery("category:(" + "Expression" + ")");
        if (criteria.getAnatomy() != null) {
            String termQuery = criteria.getAnatomy().stream()
                    .collect(Collectors.joining(" OR "));
            solrQuery.addFilterQuery("expression_anatomy_tf:(" + termQuery + ")");
        }
        solrQuery.addFilterQuery("zebrafish_gene:(" + criteria.getExactGene() + ")");
        solrQuery.add("group", "true");
        solrQuery.add("group.ngroups", "true");
        solrQuery.add("group.field", "fig_zdb_id");

        QueryResponse queryResponse = null;
        try {
            queryResponse = getSolrClient().query(solrQuery);
        } catch (SolrServerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return queryResponse.getGroupResponse().getValues().get(0).getValues()
                .stream()
                .map((Group g) -> g.getResult().get(0))
                .collect(Collectors.toList());


    }

    public static List<FigureResult> buildFigureResults(List<SolrDocument> solrDocumentList) {
        return solrDocumentList.stream()
                .map(it -> buildFigureResult(it))
                .collect(Collectors.toList());
    }

    public static FigureResult buildFigureResult(SolrDocument document) {
        FigureResult figureResult = new FigureResult();
        figureResult.setFigure((String)document.get("name"));
        figureResult.setPublication((String)document.get("publication"));

        return figureResult;
    }

    public static List<GeneResult> buildGeneResults(SolrDocumentList solrDocumentList, ExpressionSearchCriteria criteria) {
        return solrDocumentList.stream()
                .map(d -> buildGeneResult(d, criteria))
                .collect(Collectors.toList());
    }

    public static GeneResult buildGeneResult(SolrDocument document, ExpressionSearchCriteria criteria) {
        GeneResult geneResult =  new GeneResult();
        geneResult.setId(document.get("id").toString());
        geneResult.setSymbol(document.get("name").toString());
        geneResult.setPublicationCount(getPublicationCount(geneResult.getSymbol(), criteria.getAnatomy()));
        geneResult.setFigureCount(getFigureCount(geneResult.getSymbol(), criteria.getAnatomy()));

        return geneResult;
    }

    public static Integer getPublicationCount(String symbol,List<String> anatomy) {
        return getCount(symbol, anatomy, "publication");
    }

    public static Integer getFigureCount(String symbol, List<String> anatomy) {
        return getCount(symbol, anatomy, "fig_zdb_id");
    }

    public static Integer getCount(String symbol, List<String> anatomy, String groupingField) {
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.addFilterQuery("category:(" + "Expression" + ")");
        if (anatomy != null) {
            String termQuery = anatomy.stream()
                    .collect(Collectors.joining(" OR "));
            solrQuery.addFilterQuery("expression_anatomy_tf:(" + termQuery + ")");
        }
        solrQuery.addFilterQuery("zebrafish_gene:(" + symbol + ")");
        solrQuery.add("group", "true");
        solrQuery.add("group.ngroups", "true");
        solrQuery.add("group.field", groupingField);
        solrQuery.setRows(0);

        QueryResponse queryResponse = null;
        try {
            queryResponse = getSolrClient().query(solrQuery);
        } catch (SolrServerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //todo: needs to be a little more null safe...
        return queryResponse.getGroupResponse().getValues().iterator().next().getNGroups();

    }



}

package org.zfin.expression.presentation;

import org.springframework.stereotype.Controller;
import groovy.json.JsonOutput;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zfin.expression.service.ExpressionSearchService;

import java.util.Arrays;
import java.util.List;

@Controller("/expression")
public class ExpressionSearchController {

    @ResponseBody
    @RequestMapping("/geneResults")
    public List<GeneResult> genes(ExpressionSearchCriteria criteria) {

        criteria.setGeneField("fgf");
        criteria.setAnatomy(Arrays.asList("brain", "eye"));

        SolrDocumentList documentList = ExpressionSearchService.getGeneResults(criteria);
        List<GeneResult> geneResults = ExpressionSearchService.buildGeneResults(documentList, criteria);

        return geneResults;

    }

    @ResponseBody
    @RequestMapping("/figureResults")
    public static List<FigureResult> figures(ExpressionSearchCriteria criteria) {


        criteria.setExactGene("fgf8a");
        criteria.setAnatomy(Arrays.asList("brain", "eye"));

        //todo: hrm, since this isn't a "SolrDocumentList" it won't have the number of results total..
        List<SolrDocument> documentList = ExpressionSearchService.getFigureResults(criteria);
        List<FigureResult> figureResults = ExpressionSearchService.buildFigureResults(documentList);

        return figureResults;


    }

}

package org.zfin.expression.presentation;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import groovy.json.JsonOutput;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zfin.expression.service.ExpressionSearchService;
import org.springframework.ui.Model;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

@Controller("/expression")
public class ExpressionSearchController {

    private static Logger logger = Logger.getLogger(ExpressionSearchController.class);


    @RequestMapping("/geneResults")
    public String genes(Model model, @ModelAttribute("criteria") ExpressionSearchCriteria criteria, HttpServletRequest request) {

        logger.error("expression search controller? please?");

        criteria.setGeneField("fgf");
        criteria.setAnatomy(Arrays.asList("brain", "eye"));

        SolrDocumentList documentList = ExpressionSearchService.getGeneResults(criteria);
        List<GeneResult> geneResults = ExpressionSearchService.buildGeneResults(documentList, criteria);

        return "expression/gene-results.page";

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

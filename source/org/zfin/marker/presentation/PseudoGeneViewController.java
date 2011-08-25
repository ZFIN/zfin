package org.zfin.marker.presentation;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.expression.service.ExpressionService;
import org.zfin.framework.presentation.Area;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.Marker;
import org.zfin.marker.service.MarkerService;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.service.TranscriptService;

/**
 */
@Controller
public class PseudoGeneViewController {

    private Logger logger = Logger.getLogger(PseudoGeneViewController.class);

    @Autowired
    private ExpressionService expressionService ;

    @RequestMapping(value ="/pseudogene/view")
    public String getGeneView(
            Model model
            ,@PathVariable("zdbID") String zdbID
    ) throws Exception {
        // set base bean
        GeneBean geneBean = new GeneBean();

        logger.info("zdbID: " + zdbID);
        Marker gene = RepositoryFactory.getMarkerRepository().getMarkerByID(zdbID);
        logger.info("gene: " + gene);
        geneBean.setMarker(gene);

        MarkerService.createDefaultViewForMarker(geneBean);

        // EXPRESSION SECTION
        geneBean.setMarkerExpression(expressionService.getExpressionForGene(gene));

        // (Transcripts)
        geneBean.setRelatedTranscriptDisplay(TranscriptService.getRelatedTranscriptsForGene(gene));

        // ORTHOLOGY
        geneBean.setOrthologyPresentationBean(MarkerService.getOrthologyEvidence(gene));

        // MAPPING INFO:
        geneBean.setMappedMarkerBean(MarkerService.getMappedMarkers(gene));

        model.addAttribute(LookupStrings.FORM_BEAN, geneBean);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, Area.PSEUDOGENE.getTitleString() + gene.getAbbreviation());

        return "marker/pseudogene-view.page";
    }
}
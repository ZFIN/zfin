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
import org.zfin.marker.MarkerHistory;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.DisplayGroup;
import org.zfin.sequence.service.TranscriptService;

import java.util.Collections;
import java.util.List;

/**
 */
@Controller
@RequestMapping("/marker")
public class PseudoGeneViewController {

    private Logger logger = Logger.getLogger(PseudoGeneViewController.class);

    private LinkDisplayOtherComparator linkDisplayOtherComparator = new LinkDisplayOtherComparator();

    @Autowired
    private ExpressionService expressionService;

    @Autowired
    private MarkerRepository markerRepository;

    @RequestMapping(value = "/pseudogene/view")
    public String getGeneView(Model model,
                              @PathVariable("zdbID") String zdbID) throws Exception {
        // set base bean
        GeneBean geneBean = new GeneBean();

        logger.info("zdbID: " + zdbID);
        Marker gene = markerRepository.getMarkerByID(zdbID);
        logger.info("gene: " + gene);
        geneBean.setMarker(gene);

        MarkerService.createDefaultViewForMarker(geneBean);

        // OTHER GENE / MARKER PAGES:
        // pull vega genes from transcript onto gene page
        // case 7586
        List<LinkDisplay> otherMarkerDBLinksLinks = geneBean.getOtherMarkerPages();
        otherMarkerDBLinksLinks.addAll(RepositoryFactory.getMarkerRepository()
                .getVegaGeneDBLinksTranscript(gene, DisplayGroup.GroupName.SUMMARY_PAGE));
        Collections.sort(otherMarkerDBLinksLinks, linkDisplayOtherComparator);
        geneBean.setOtherMarkerPages(otherMarkerDBLinksLinks);


        // EXPRESSION SECTION
        geneBean.setMarkerExpression(expressionService.getExpressionForGene(gene));
        // MUTANTS AND TARGETED KNOCKDOWNS
        geneBean.setMutantOnMarkerBeans(MarkerService.getMutantsOnGene(gene));

        // (Transcripts)
        geneBean.setRelatedTranscriptDisplay(TranscriptService.getRelatedTranscriptsForGene(gene));

        // ORTHOLOGY
        geneBean.setOrthologyPresentationBean(MarkerService.getOrthologyEvidence(gene));

        model.addAttribute(LookupStrings.FORM_BEAN, geneBean);
        model.addAttribute("markerHistoryReasonCodes", MarkerHistory.Reason.values());
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, Area.PSEUDOGENE.getTitleString() + gene.getAbbreviation());

        return "marker/pseudogene-view.page";
    }
}
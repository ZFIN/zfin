package org.zfin.marker.presentation;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.expression.service.ExpressionService;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerHistory;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;
import org.zfin.repository.RepositoryFactory;

/**
 */
@Controller
@RequestMapping("/marker")
public class NTRViewController {

    private Logger logger = Logger.getLogger(NTRViewController.class);

//    @Autowired
//    private ExpressionService expressionService ;
private LinkDisplayOtherComparator linkDisplayOtherComparator = new LinkDisplayOtherComparator();

    @Autowired
    private ExpressionService expressionService;

    @Autowired
    private MarkerRepository markerRepository ;
    
    @Autowired
    private EfgViewController efgViewController;

    @RequestMapping(value ="/nontranscribedregion/view/{zdbID}")
    public String getNontranscribedRegionView(
            Model model
            ,@PathVariable("zdbID") String zdbID
    ) throws Exception {
        // set base bean
        MarkerBean markerBean = new MarkerBean();

        logger.info("zdbID: " + zdbID);
        Marker region = markerRepository.getMarkerByID(zdbID);
        logger.info("region: " + region);
        markerBean.setMarker(region);

        // not used, too much stuff excluded
       MarkerService.createDefaultViewForMarker(markerBean);
//        MarkerService.pullClonesOntoGeneFromTranscript(markerBean);
        /*List<LinkDisplay> otherMarkerDBLinksLinks = markerBean.getOtherMarkerPages();
        otherMarkerDBLinksLinks.addAll(markerRepository.getVegaGeneDBLinksTranscript(
                region, DisplayGroup.GroupName.SUMMARY_PAGE));
        Collections.sort(otherMarkerDBLinksLinks, linkDisplayOtherComparator);
        markerBean.setOtherMarkerPages(otherMarkerDBLinksLinks);*/


//        markerBean.setHasChimericClone(markerRepository.isFromChimericClone(region.getZdbID()));

        // EXPRESSION SECTION
        markerBean.setMarkerExpression(expressionService.getExpressionForGene(region));

        // MUTANTS AND TARGETED KNOCKDOWNS
        markerBean.setMutantOnMarkerBeans(MarkerService.getMutantsOnGene(region));

        // PHENOTYPE
        markerBean.setPhenotypeOnMarkerBeans(MarkerService.getPhenotypeOnGene(region));

        // region Ontology
        markerBean.setGeneOntologyOnMarkerBeans(MarkerService.getGeneOntologyOnMarker(region));

        //region Transcripts
//        markerBean.setRelatedTranscriptDisplay(TranscriptService.getRelatedTranscriptsForGene(region));
        // (CONSTRUCTS)
        if (efgViewController != null) {
            efgViewController.populateConstructList(markerBean, region);
        }

        markerBean.setNumPubs(RepositoryFactory.getPublicationRepository().getNumberAssociatedPublicationsForZdbID(region.getZdbID()));
        markerBean.setSequenceInfo(MarkerService.getSequenceInfoSummary(region));
        model.addAttribute(LookupStrings.FORM_BEAN, markerBean);
        model.addAttribute("markerHistoryReasonCodes", MarkerHistory.Reason.values());
      //  model.addAttribute(LookupStrings.DYNAMIC_TITLE, Marker.Type.getType(markerBean.getMarkerTypeDisplay()) + region.getAbbreviation());

        return "marker/region-view.page";
    }
}
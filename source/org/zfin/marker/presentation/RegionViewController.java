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
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.DisplayGroup;
import org.zfin.sequence.service.SequenceService;
import org.zfin.sequence.service.TranscriptService;

import java.util.Collections;
import java.util.List;

/**
 */
@Controller
@RequestMapping("/marker")
public class RegionViewController {

    private Logger logger = Logger.getLogger(RegionViewController.class);

//    @Autowired
//    private ExpressionService expressionService ;

   
    @Autowired
    private ExpressionService expressionService;

    @Autowired
    private MarkerRepository markerRepository;


    @RequestMapping(value ="/eregion/view/{zdbID}")
    public String getView(
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

        markerBean.setMarkerTypeDisplay(MarkerService.getMarkerTypeString(region));
        markerBean.setPreviousNames(markerRepository.getPreviousNamesLight(region));
        markerBean.setHasMarkerHistory(markerRepository.getHasMarkerHistory(zdbID)) ;

        // EXPRESSION SECTION
        /*List<LinkDisplay> otherMarkerDBLinksLinks = markerBean.getOtherMarkerPages();
        otherMarkerDBLinksLinks.addAll(markerRepository.getVegaGeneDBLinksTranscript(
                region, DisplayGroup.GroupName.SUMMARY_PAGE));
        Collections.sort(otherMarkerDBLinksLinks, linkDisplayOtherComparator);
        markerBean.setOtherMarkerPages(otherMarkerDBLinksLinks);


        markerBean.setHasChimericClone(markerRepository.isFromChimericClone(region.getZdbID()));*/

        // EXPRESSION SECTION
        markerBean.setMarkerExpression(expressionService.getExpressionForGene(region));

        // MUTANTS AND TARGETED KNOCKDOWNS
        markerBean.setMutantOnMarkerBeans(MarkerService.getMutantsOnGene(region));

        // PHENOTYPE
        markerBean.setPhenotypeOnMarkerBeans(MarkerService.getPhenotypeOnGene(region));

        // region Ontology
        markerBean.setGeneOntologyOnMarkerBeans(MarkerService.getGeneOntologyOnMarker(region));

        // Protein Products (Protein Families, Domains, and Sites)
        markerBean.setProteinProductDBLinkDisplay(SequenceService.getProteinProducts(region));

        // (Transcripts)
        markerBean.setRelatedTranscriptDisplay(TranscriptService.getRelatedTranscriptsForGene(region));

        // region products
        markerBean.setGeneProductsBean(markerRepository.getGeneProducts(region.getZdbID()));

        // (CONSTRUCTS)




//      CITATIONS
        markerBean.setNumPubs(RepositoryFactory.getPublicationRepository().getNumberAssociatedPublicationsForZdbID(region.getZdbID()));
        markerBean.setSequenceInfo(MarkerService.getSequenceInfoSummary(region));
        model.addAttribute(LookupStrings.FORM_BEAN, markerBean);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, Area.EREGION.getTitleString() + region.getAbbreviation());

        return "marker/eregion-view.page";
    }
}
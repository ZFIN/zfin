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
import org.zfin.marker.service.MarkerService;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.DisplayGroup;
import org.zfin.sequence.service.SequenceService;
import org.zfin.sequence.service.TranscriptService;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 */
@Controller
public class GeneViewController {

    private Logger logger = Logger.getLogger(GeneViewController.class);
    private LinkDisplayOtherComparator linkDisplayOtherComparator = new LinkDisplayOtherComparator();

    @Autowired
    private ExpressionService expressionService;

    @RequestMapping(value = "/gene/view/{zdbID}")
    public String getGeneView(
            Model model
            , @PathVariable("zdbID") String zdbID
    ) throws Exception {
        // set base bean
        GeneBean geneBean = new GeneBean();

        logger.info("zdbID: " + zdbID);
        Marker gene = RepositoryFactory.getMarkerRepository().getMarkerByID(zdbID);
        logger.info("gene: " + gene);
        geneBean.setMarker(gene);

        MarkerService.createDefaultViewForMarker(geneBean);

        // OTHER GENE / MARKER PAGES:
        // pull vega genes from transcript onto gene page
        // case 7586
//        geneBean.setOtherMarkerPages(RepositoryFactory.getMarkerRepository().getMarkerDBLinksFast(gene, DisplayGroup.GroupName.SUMMARY_PAGE));
        List<LinkDisplay> otherMarkerDBLinksLinks = geneBean.getOtherMarkerPages();
        otherMarkerDBLinksLinks.addAll(RepositoryFactory.getMarkerRepository()
                .getVegaGeneDBLinksTranscript(gene, DisplayGroup.GroupName.SUMMARY_PAGE)) ;
        Collections.sort(otherMarkerDBLinksLinks, linkDisplayOtherComparator) ;
        geneBean.setOtherMarkerPages(otherMarkerDBLinksLinks);


        geneBean.setHasChimericClone(RepositoryFactory.getMarkerRepository().isFromChimericClone(gene.getZdbID()));

        // EXPRESSION SECTION
        geneBean.setMarkerExpression(expressionService.getExpressionForGene(gene));

        // MUTANTS AND TARGETED KNOCKDOWNS
        geneBean.setMutantOnMarkerBeans(MarkerService.getMutantsOnGene(gene));

        // PHENOTYPE
        geneBean.setPhenotypeOnMarkerBeans(MarkerService.getPhenotypeOnGene(gene));

        // Gene Ontology
        geneBean.setGeneOntologyOnMarkerBeans(MarkerService.getGeneOntologyOnMarker(gene));

        // Protein Products (Protein Families, Domains, and Sites)
        geneBean.setProteinProductDBLinkDisplay(SequenceService.getProteinProducts(gene));

        // (Transcripts)
        geneBean.setRelatedTranscriptDisplay(TranscriptService.getRelatedTranscriptsForGene(gene));

        // gene products
        geneBean.setGeneProductsBean(RepositoryFactory.getMarkerRepository().getGeneProducts(gene.getZdbID()));

        // (CONSTRUCTS)
        Set<MarkerRelationship.Type> types = new HashSet<MarkerRelationship.Type>();
        types.add(MarkerRelationship.Type.PROMOTER_OF);
        types.add(MarkerRelationship.Type.CODING_SEQUENCE_OF);
        types.add(MarkerRelationship.Type.CONTAINS_ENGINEERED_REGION);
        Set<Marker> constructs = MarkerService.getRelatedMarker(gene, types) ;
        constructs.addAll(RepositoryFactory.getMarkerRepository().getConstructsForGene(gene)) ;
        geneBean.setConstructs(constructs);

        // (Antibodies)
        geneBean.setRelatedAntibodies(RepositoryFactory.getMarkerRepository()
                .getRelatedMarkerDisplayForTypes(gene, true
                        , MarkerRelationship.Type.GENE_PRODUCT_RECOGNIZED_BY_ANTIBODY));

        // ORTHOLOGY
        geneBean.setOrthologyPresentationBean(MarkerService.getOrthologyEvidence(gene));

        // MAPPING INFO:
        geneBean.setMappedMarkerBean(MarkerService.getMappedMarkers(gene));

        model.addAttribute(LookupStrings.FORM_BEAN, geneBean);

        model.addAttribute(LookupStrings.DYNAMIC_TITLE, Area.GENE.getTitleString() + gene.getAbbreviation());

        return "marker/gene-view.page";
    }

    public void setExpressionService(ExpressionService expressionService) {
        this.expressionService = expressionService;
    }
}
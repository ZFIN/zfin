package org.zfin.marker.presentation;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.anatomy.presentation.AnatomySearchBean;
import org.zfin.expression.FigureService;
import org.zfin.expression.presentation.FigureSummaryDisplay;
import org.zfin.expression.service.ExpressionService;
import org.zfin.framework.presentation.Area;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;
import org.zfin.mutant.Fish;
import org.zfin.ontology.GenericTerm;
import org.zfin.orthology.Orthology;
import org.zfin.orthology.OrthologyEvidenceService;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.DisplayGroup;
import org.zfin.sequence.service.SequenceService;
import org.zfin.sequence.service.TranscriptService;

import java.util.Collections;
import java.util.List;

import static org.zfin.repository.RepositoryFactory.getMarkerRepository;
import static org.zfin.repository.RepositoryFactory.getPublicationRepository;

/**
 */
@Controller
@RequestMapping("/marker")
public class GeneViewController {

    private Logger logger = Logger.getLogger(GeneViewController.class);
    private LinkDisplayOtherComparator linkDisplayOtherComparator = new LinkDisplayOtherComparator();

    @Autowired
    private ExpressionService expressionService;

    @Autowired
    private MarkerRepository markerRepository;

    @Autowired
    private EfgViewController efgViewController;

    @RequestMapping(value = "/gene/view/{zdbID}")
    public String getGeneView(@PathVariable("zdbID") String zdbID,
                              Model model) throws Exception {
        // set base bean
        GeneBean geneBean = new GeneBean();

        logger.info("zdbID: " + zdbID);
        Marker gene = RepositoryFactory.getMarkerRepository().getMarkerByID(zdbID);
        logger.info("gene: " + gene);
        geneBean.setMarker(gene);

        MarkerService.createDefaultViewForMarker(geneBean);

        // if it is a gene, also add any clones if related via a transcript
        MarkerService.pullClonesOntoGeneFromTranscript(geneBean);


        // OTHER GENE / MARKER PAGES:
        // pull vega genes from transcript onto gene page
        // case 7586
//        geneBean.setOtherMarkerPages(RepositoryFactory.getMarkerRepository().getMarkerDBLinksFast(gene, DisplayGroup.GroupName.SUMMARY_PAGE));
        List<LinkDisplay> otherMarkerDBLinksLinks = geneBean.getOtherMarkerPages();
        otherMarkerDBLinksLinks.addAll(markerRepository.getVegaGeneDBLinksTranscript(
                gene, DisplayGroup.GroupName.SUMMARY_PAGE));
        Collections.sort(otherMarkerDBLinksLinks, linkDisplayOtherComparator);
        geneBean.setOtherMarkerPages(otherMarkerDBLinksLinks);


        geneBean.setHasChimericClone(markerRepository.isFromChimericClone(gene.getZdbID()));

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
        geneBean.setGeneProductsBean(markerRepository.getGeneProducts(gene.getZdbID()));

        // (CONSTRUCTS)
        if (efgViewController != null) {
            efgViewController.populateConstructList(geneBean, gene);
        }

        // (Antibodies)
        geneBean.setRelatedAntibodies(markerRepository.getRelatedMarkerDisplayForTypes(
                gene, true, MarkerRelationship.Type.GENE_PRODUCT_RECOGNIZED_BY_ANTIBODY));

        geneBean.setPlasmidDBLinks(
                markerRepository.getMarkerDBLinksFast(gene, DisplayGroup.GroupName.PLASMIDS));

        // ORTHOLOGY
        geneBean.setOrthologyPresentationBean(MarkerService.getOrthologyEvidence(gene));

        model.addAttribute(LookupStrings.FORM_BEAN, geneBean);

        model.addAttribute(LookupStrings.DYNAMIC_TITLE, Area.GENE.getTitleString() + gene.getAbbreviation());

        return "marker/gene-view.page";
    }

    public void setExpressionService(ExpressionService expressionService) {
        this.expressionService = expressionService;
    }

    public void setMarkerRepository(MarkerRepository markerRepository) {
        this.markerRepository = markerRepository;
    }

    @RequestMapping("/{zdbID}/orthology")
    public String showOrthology(@PathVariable("zdbID") String zdbID,
                                @RequestParam(value = "update", required = false, defaultValue = "0") String update,
                                @ModelAttribute("formBean") GeneBean geneBean,
                                Model model) {

        logger.info("zdbID: " + zdbID);
        Marker gene = RepositoryFactory.getMarkerRepository().getMarkerByID(zdbID);
        logger.info("gene: " + gene);
        geneBean.setMarker(gene);
        geneBean.setOrthologyPresentationBean(MarkerService.getOrthologyEvidence(gene));

        model.addAttribute("update", update.equals("1"));

        return "marker-orthology.simple-page";
    }

    @RequestMapping("/{markerID}/orthology-detail")
    public String showOrthologyDetail(@PathVariable String markerID,
                                      @ModelAttribute("formBean") GeneBean geneBean,
                                      Model model) {
        logger.info("zdbID: " + markerID);
        Marker gene = RepositoryFactory.getMarkerRepository().getMarkerByID(markerID);
        geneBean.setOrthologyPresentationBean(MarkerService.getOrthologyEvidence(gene));
        List<Orthology> list = getPublicationRepository().getOrthologyPublications(gene);
        List<Orthology> evidenceList = OrthologyEvidenceService.getEvidenceCenteredList(list);
        geneBean.setMarker(gene);
        model.addAttribute("publicationOrthologyList", list);
        model.addAttribute("evidenceOrthologyList", evidenceList);

        return "marker/marker-orthology-detail.page";
    }


    @RequestMapping(value = "/{geneID}/phenotype-summary")
    public String genotypeSummary(Model model
            , @PathVariable("geneID") String geneID) throws Exception {

        Marker marker = getMarkerRepository().getMarkerByID(geneID);
        if (marker == null) {
            model.addAttribute(LookupStrings.ZDB_ID, geneID);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }
        List<FigureSummaryDisplay> figureSummaryDisplayList = FigureService.createPhenotypeFigureSummary(marker);
        model.addAttribute("figureSummaryDisplayList", figureSummaryDisplayList);

        //retrieveMutantData(term, form, true);
        //model.addAttribute(LookupStrings.FORM_BEAN, form);
        model.addAttribute("marker", marker);
        return "marker/phenotype-summary.page";
    }


}
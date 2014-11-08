package org.zfin.marker.presentation;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.expression.service.ExpressionService;
import org.zfin.framework.presentation.Area;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;
import org.zfin.orthology.Orthology;
import org.zfin.orthology.OrthologyEvidenceService;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.DisplayGroup;
import org.zfin.sequence.service.SequenceService;
import org.zfin.sequence.service.TranscriptService;

import java.util.*;

import static org.zfin.repository.RepositoryFactory.getPublicationRepository;

/**
 */
@Controller
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
                                @ModelAttribute("formBean") GeneBean geneBean,
                                Model model) {

        logger.info("zdbID: " + zdbID);
        Marker gene = RepositoryFactory.getMarkerRepository().getMarkerByID(zdbID);
        logger.info("gene: " + gene);
        geneBean.setMarker(gene);
        geneBean.setOrthologyPresentationBean(MarkerService.getOrthologyEvidence(gene));

        return "marker-orthology.simple-page";
    }

    @RequestMapping("/publication/{pubID}/orthology-list")
    public String showOrthologyList(@PathVariable String pubID,
                                    @ModelAttribute("formBean") GeneBean geneBean,
                                    Model model) {
        logger.info("zdbID: " + pubID);

        if (StringUtils.equals(pubID, "ZDB-PUB-030905-1")) {
            return "redirect:/" + pubID;
        }

        List<Marker> list = getPublicationRepository().getOrthologyGeneList(pubID);
        Publication publication = getPublicationRepository().getPublication(pubID);
        List<GeneBean> beanList = new ArrayList<>(list.size());
        for (Marker marker : list) {
            GeneBean orthologyBean = new GeneBean();
            orthologyBean.setMarker(marker);
            orthologyBean.setOrthologyPresentationBean(MarkerService.getOrthologyEvidence(marker, publication));
            beanList.add(orthologyBean);
        }
        model.addAttribute("orthologyBeanList", beanList);
        model.addAttribute("publication", publication);
        return "marker/marker-orthology-list.page";
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

}
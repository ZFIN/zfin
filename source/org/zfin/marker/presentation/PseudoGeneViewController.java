package org.zfin.marker.presentation;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.expression.service.ExpressionSearchService;
import org.zfin.expression.service.ExpressionService;
import org.zfin.framework.presentation.Area;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.genomebrowser.GenomeBrowserTrack;
import org.zfin.jbrowse.presentation.GenomeBrowserImageBuilder;
import org.zfin.infrastructure.seo.CanonicalLinkConfig;
import org.zfin.mapping.GenomeLocation;
import org.zfin.mapping.MappingService;
import org.zfin.mapping.MarkerGenomeLocation;
import org.zfin.mapping.presentation.BrowserLink;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerHistory;
import org.zfin.marker.MarkerNotFoundException;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.DisplayGroup;
import org.zfin.sequence.gff.Assembly;
import org.zfin.sequence.service.TranscriptService;

import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import static org.zfin.mapping.GenomeLocation.GRCZ12TU;
import static org.zfin.repository.RepositoryFactory.getLinkageRepository;

/**
 */
@Controller
@RequestMapping("/marker")
public class PseudoGeneViewController {

    private Logger logger = LogManager.getLogger(PseudoGeneViewController.class);

    private LinkDisplayOtherComparator linkDisplayOtherComparator = new LinkDisplayOtherComparator();

    @Autowired
    private ExpressionService expressionService;

    @Autowired
    private ExpressionSearchService expressionSearchService;

    @Autowired
    private MarkerRepository markerRepository;

    @Autowired
    private MarkerService markerService;


    @RequestMapping(value = "/pseudogene/view/{zdbID}")
    public String getGeneView(Model model, @PathVariable("zdbID") String zdbID) throws Exception {
        CanonicalLinkConfig.addCanonicalIfFound(model);

        // set base bean
        GeneBean geneBean = new GeneBean();

        String activeZdbID = markerService.getActiveMarkerID(zdbID);
        if (!zdbID.equals(activeZdbID)) {
            return "redirect:/" + activeZdbID;
        }

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
        geneBean.setRelatedInteractions(markerRepository.getRelatedMarkerDisplayForTypes(
                gene, true, MarkerRelationship.Type.RNAGENE_INTERACTS_WITH_GENEP));


        // sequence section: if not empty
        List<MarkerGenomeLocation> genomeLocation = getLinkageRepository().getGenomeLocation(gene, GenomeLocation.Source.ZFIN_NCBI, GenomeLocation.Source.NCBI_LOADER);
        if (genomeLocation.size() > 0) {
            MarkerGenomeLocation landmark = genomeLocation.get(0);
            int startPadding = (landmark.getEnd() - landmark.getStart()) / 10;
            int endPadding = (landmark.getEnd() - landmark.getStart()) / 20;

            GenomeBrowserImageBuilder refseqBuilder = new GenomeBrowserImageBuilder()
                .setLandmarkByGenomeLocation(landmark)
                // add 10% left padding and 5% right padding
                .withPadding(startPadding, endPadding)
                .tracks(GenomeBrowserTrack.getGenomeBrowserTracks(GenomeBrowserTrack.Page.GENE_SEQUENCE));
            geneBean.setRefSeqLocations(refseqBuilder.build());
            // if GRCz12 then show jBrowse image
            Assembly latestAssembly = genomeLocation.get(0).getMarker().getLatestAssembly();
            GenomeBrowserImage genomeBrowserImageSequence = refseqBuilder.build();
            if (latestAssembly.getName().equals(GRCZ12TU)) {
                geneBean.setRefSeqLocations(genomeBrowserImageSequence);
                TreeSet<BrowserLink> locations = MappingService.getJBrowserBrowserLinks(genomeLocation, genomeBrowserImageSequence, latestAssembly);
                geneBean.setLocations(locations);
            }
        }

        // ORTHOLOGY
        geneBean.setOrthologyPresentationBean(MarkerService.getOrthologyEvidence(gene));

        model.addAttribute(LookupStrings.FORM_BEAN, geneBean);
        model.addAttribute("markerHistoryReasonCodes", MarkerHistory.Reason.values());
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, Area.PSEUDOGENE.getTitleString() + gene.getAbbreviation());

        return "marker/pseudogene/pseudogene-view";
    }

    @RequestMapping(value = "/pseudogene/view/{zdbID}/expression")
    public String getPseudogeneExpressionView(@PathVariable("zdbID") String zdbID) throws MarkerNotFoundException {
        return expressionSearchService.forwardToExpressionSearchForMarker(zdbID);
    }
}
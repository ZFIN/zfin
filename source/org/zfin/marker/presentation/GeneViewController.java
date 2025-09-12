package org.zfin.marker.presentation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zfin.expression.FigureService;
import org.zfin.expression.presentation.FigureSummaryDisplay;
import org.zfin.expression.presentation.MarkerExpression;
import org.zfin.expression.service.ExpressionSearchService;
import org.zfin.expression.service.ExpressionService;
import org.zfin.feature.repository.FeatureRepository;
import org.zfin.framework.presentation.Area;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.genomebrowser.GenomeBrowserTrack;
import org.zfin.genomebrowser.presentation.GenomeBrowserFactory;
import org.zfin.genomebrowser.presentation.GenomeBrowserImageBuilder;
import org.zfin.infrastructure.ControlledVocab;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.infrastructure.seo.CanonicalLinkConfig;
import org.zfin.mapping.GenomeLocation;
import org.zfin.mapping.MarkerGenomeLocation;
import org.zfin.mapping.presentation.BrowserLink;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerHistory;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.agr.*;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;
import org.zfin.orthology.Ortholog;
import org.zfin.orthology.OrthologExternalReference;
import org.zfin.orthology.presentation.OrthologEvidencePresentation;
import org.zfin.orthology.presentation.OrthologyPresentationRow;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;
import org.zfin.search.presentation.SearchPrototypeController;
import org.zfin.sequence.DisplayGroup;
import org.zfin.sequence.repository.SequenceRepository;
import org.zfin.sequence.service.TranscriptService;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.*;

@Controller
@RequestMapping("/marker")
public class GeneViewController {

    private Logger logger = LogManager.getLogger(GeneViewController.class);
    private LinkDisplayOtherComparator linkDisplayOtherComparator = new LinkDisplayOtherComparator();

    @Autowired
    private ExpressionService expressionService;

    @Autowired
    private MarkerService markerService;

    @Autowired
    private MarkerRepository markerRepository;

    @Autowired
    private FeatureRepository featureRepository;

    @Autowired
    private SequenceRepository sequenceRepository;

    @Autowired
    private InfrastructureRepository infrastructureRepository;

    @Autowired
    private EfgViewController efgViewController;

    @Autowired
    SearchPrototypeController searchController;

    private void prepareGeneView(Model model, String zdbID) {
        // set base bean
        GeneBean geneBean = new GeneBean();

        Marker gene = RepositoryFactory.getMarkerRepository().getMarkerByID(zdbID);
        logger.info("gene: " + gene);
        geneBean.setMarker(gene);

        MarkerService.createDefaultViewForMarker(geneBean);

        // if it is a gene, also add any clones if related via a transcript
        MarkerService.pullClonesOntoGeneFromTranscript(geneBean);


        // OTHER GENE / MARKER PAGES:
        // pull vega genes from transcript onto gene page (case 7586)
        List<LinkDisplay> otherMarkerDBLinksLinks = geneBean.getOtherMarkerPages();
        List<LinkDisplay> moreLinks = markerRepository.getVegaGeneDBLinksTranscript(gene, DisplayGroup.GroupName.SUMMARY_PAGE);
        otherMarkerDBLinksLinks.addAll(moreLinks);
        otherMarkerDBLinksLinks.sort(linkDisplayOtherComparator);
        geneBean.setOtherMarkerPages(otherMarkerDBLinksLinks);


        geneBean.setHasChimericClone(markerRepository.isFromChimericClone(gene.getZdbID()));

        // EXPRESSION SECTION
        MarkerExpression markerExpression = expressionService.getExpressionForGene(gene);
        markerExpression.setEnsdargGenes(geneBean.getEnsdargAccessions());
        geneBean.setMarkerExpression(markerExpression);

        // MUTANTS AND TARGETED KNOCKDOWNS
        geneBean.setMutantOnMarkerBeans(MarkerService.getMutantsOnGene(gene));

        // PHENOTYPE
        geneBean.setPhenotypeOnMarkerBeans(MarkerService.getPhenotypeOnGene(gene));

        // Gene Ontology
        geneBean.setGeneOntologyOnMarkerBeans(MarkerService.getGeneOntologyOnMarker(gene));

        // Protein Products (Protein Families, Domains, and Sites)

        geneBean.setIpProtein(markerRepository.getInterProForMarker(gene));
        geneBean.setProteinType(markerRepository.getProteinType(gene));
        geneBean.setProteinDetailDomainBean(markerService.getProteinDomainDetailBean(gene));

        // sequence section: if not empty
        List<MarkerGenomeLocation> genomeLocation = getLinkageRepository().getGenomeLocation(gene, GenomeLocation.Source.ZFIN_NCBI_Z12);
        if (genomeLocation.size() > 0) {
            GenomeBrowserImageBuilder refseqBuilder = GenomeBrowserFactory.getStaticImageBuilder()
                .setLandmarkByGenomeLocation(genomeLocation.get(0))
                // add 10% left padding
                .withPadding((genomeLocation.get(0).getEnd() - genomeLocation.get(0).getStart()) / 10, 0)
                .tracks(new GenomeBrowserTrack[]{GenomeBrowserTrack.GENES, GenomeBrowserTrack.REFSEQ});
            geneBean.setRefSeqLocations(refseqBuilder.build());
        }

        // Transcripts
        geneBean.setRelatedTranscriptDisplay(TranscriptService.getRelatedTranscriptsForGene(gene));
        List<MarkerGenomeLocation> genomeMarkerLocationList = getLinkageRepository().getGenomeLocation(gene);
        TreeSet locations = new TreeSet<>();
        for (MarkerGenomeLocation genomeMarkerLocation : genomeMarkerLocationList) {
            BrowserLink location = new BrowserLink();
            if (genomeMarkerLocation.getSource().equals(GenomeLocation.Source.ZFIN_NCBI_Z12)) {
                location.setUrl(genomeMarkerLocation.getUrl());
                location.setName("ZFIN");
                location.setOrder(0);
            } else if (genomeMarkerLocation.getSource().equals(GenomeLocation.Source.ENSEMBL)) {
                location.setUrl(genomeMarkerLocation.getUrl());
                location.setName(genomeMarkerLocation.getSource().getDisplayName());
                location.setOrder(1);
            } else if (genomeMarkerLocation.getSource().equals(GenomeLocation.Source.NCBI_LOADER_Z12)) {
                location.setUrl(genomeMarkerLocation.getUrl());
                location.setName("NCBI");
                location.setOrder(2);
            } else if (genomeMarkerLocation.getSource().equals(GenomeLocation.Source.UCSC)) {
                location.setUrl(genomeMarkerLocation.getUrl());
                location.setName(genomeMarkerLocation.getSource().getDisplayName());
                location.setOrder(3);
            } else {
                continue;
            }
            locations.add(location);
        }
        geneBean.setLocations(locations);

        // gene products
        geneBean.setGeneProductsBean(markerRepository.getGeneProducts(gene.getZdbID()));

        // (CONSTRUCTS)
        if (efgViewController != null) {
            ControlledVocab zebrafish = new ControlledVocab();
            zebrafish.setCvTermName("Dre.");
            zebrafish.setCvForeignSpecies("zebrafish");
            zebrafish.setCvNameDefinition("Danio rerio");
            Set<MarkerRelationship.Type> types = new HashSet<>();
            types.add(MarkerRelationship.Type.PROMOTER_OF);
            types.add(MarkerRelationship.Type.CODING_SEQUENCE_OF);
            types.add(MarkerRelationship.Type.CONTAINS_REGION);
            Set<Marker> relatedMarkers = new TreeSet<>();
            relatedMarkers = MarkerService.getRelatedMarker(gene, types);
            geneBean.setNumberOfConstructs(relatedMarkers.size());
            List<ConstructBean> constructBeans = new ArrayList<>();
            for (Marker mrkr : relatedMarkers) {
                ConstructBean constructBean = new ConstructBean();
                constructBean.setMarker(mrkr);
                List<MarkerRelationshipPresentation> mrkrRels = new ArrayList<>();
                mrkrRels.addAll(markerRepository.getRelatedMarkerOrderDisplayForTypes(
                    mrkr, true
                    , MarkerRelationship.Type.PROMOTER_OF
                    , MarkerRelationship.Type.CODING_SEQUENCE_OF
                    , MarkerRelationship.Type.CONTAINS_REGION
                ));

                List<Marker> regulatoryRegions = new ArrayList<>();
                List<MarkerRelationshipPresentation> regulatoryRegionPresentations = new ArrayList<>();
                List<MarkerRelationshipPresentation> codingSequencePresentations = new ArrayList<>();
                List<Marker> codingSequences = new ArrayList<>();
                for (MarkerRelationshipPresentation markerRelationshipPresentation : mrkrRels) {
                    if (markerRelationshipPresentation.getRelationshipType().equals("Has Promoter")) {
                        regulatoryRegions.add(markerRepository.getMarkerByID(markerRelationshipPresentation.getZdbId()));
                        regulatoryRegionPresentations.add(markerRelationshipPresentation);
                    } else if (markerRelationshipPresentation.getRelationshipType().equals("Has Coding Sequence")) {
                        codingSequences.add(markerRepository.getMarkerByID(markerRelationshipPresentation.getZdbId()));
                        codingSequencePresentations.add(markerRelationshipPresentation);
                    }
                }
                constructBean.setRegulatoryRegions(regulatoryRegions);
                constructBean.setCodingSequences(codingSequences);
                constructBean.setNumPubs(RepositoryFactory.getPublicationRepository().getNumberAssociatedPublicationsForZdbID(mrkr.getZdbID()));
                constructBean.setNumberOfTransgeniclines(featureRepository.getNumberOfFeaturesForConstruct(mrkr));
                List<ControlledVocab> species = infrastructureRepository.getControlledVocabsForSpeciesByConstruct(mrkr);
                species.add(zebrafish);
                List sortedSpecies = species.stream().sorted(Comparator.comparing(ControlledVocab::getCvNameDefinition)).collect(Collectors.toList());

                constructBean.setSpecies(sortedSpecies);
                constructBeans.add(constructBean);
            }

            geneBean.setConstructBeans(constructBeans);
        }

        // (Antibodies)
        List<MarkerRelationshipPresentation> antibodyRelationships = markerRepository.getRelatedMarkerDisplayForTypes(
            gene, true, MarkerRelationship.Type.GENE_PRODUCT_RECOGNIZED_BY_ANTIBODY);

        if (CollectionUtils.isNotEmpty(antibodyRelationships)) {
            Set<String> antibodyIds = antibodyRelationships.stream()
                .map(MarkerRelationshipPresentation::getZdbId)
                .collect(Collectors.toSet());
            geneBean.setAntibodies(markerRepository.getAntibodies(antibodyIds));
            List<AntibodyMarkerBean> beans = markerRepository.getAntibodies(antibodyIds).stream()
                .map(antibody -> {
                    AntibodyMarkerBean antibodyBean = new AntibodyMarkerBean();
                    antibodyBean.setAntibody(antibody);
                    antibodyBean.setNumPubs(RepositoryFactory.getPublicationRepository().getNumberDirectPublications(antibody.getZdbID()));
                    antibodyBean.setAntigenGenes(markerRepository.getRelatedMarkerDisplayForTypes(
                        antibody, false, MarkerRelationship.Type.GENE_PRODUCT_RECOGNIZED_BY_ANTIBODY));
                    return antibodyBean;
                })
                .collect(Collectors.toList());
            geneBean.setAntibodyBeans(beans);
        }

        if (gene.getType() == Marker.Type.GENE) {
            geneBean.setRelatedInteractions(markerRepository.getRelatedMarkerDisplayForTypes(
                gene, false, MarkerRelationship.Type.RNAGENE_INTERACTS_WITH_GENE, MarkerRelationship.Type.NTR_INTERACTS_WITH_GENE));
        }


        geneBean.setPlasmidDBLinks(
            markerRepository.getMarkerDBLinksFast(gene, DisplayGroup.GroupName.PLASMIDS));
        geneBean.setPathwayDBLinks(
            markerRepository.getMarkerDBLinksFast(gene, DisplayGroup.GroupName.PATHWAYS));

        // orthology
        List<Ortholog> orthologList = getOrthologyRepository().getOrthologs(gene);
        List<String> bGeeIds = markerService.getBeeGeeStrings(otherMarkerDBLinksLinks, orthologList);
        if (CollectionUtils.isNotEmpty(bGeeIds)) {
            model.addAttribute("bGeeIds", String.join(",", bGeeIds));
            //model.addAttribute("geneTree", otherMarkerDBLinksLinks.stream().filter(linkDisplay -> linkDisplay.getAccession().startsWith("ENSDARG")).findFirst().get().getAccession());
            model.addAttribute("geneTree", markerService.getGeneTreeEnsdarg(gene));
        }

        model.addAttribute("hasOrthology", CollectionUtils.isNotEmpty(orthologList));
        if (gene.getOrthologyNote() != null)
            model.addAttribute("orthologyNote", gene.getOrthologyNote().getNote());

        model.addAttribute(LookupStrings.FORM_BEAN, geneBean);
        model.addAttribute("markerHistoryReasonCodes", MarkerHistory.Reason.values());
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, Area.GENE.getTitleString() + gene.getAbbreviation());
    }

    @RequestMapping(value = "/gene/view/{zdbID}")
    public String getGeneView(Model model, @PathVariable("zdbID") String zdbID) throws Exception {
        CanonicalLinkConfig.addCanonicalIfFound(model);

        String activeMarkerID = markerService.getActiveMarkerID(zdbID);
        if (!markerService.isOfTypeGene(activeMarkerID)) {
            return "redirect:/" + activeMarkerID;
        }
        if (!activeMarkerID.equals(zdbID)) {
            return "redirect:/" + activeMarkerID;
        }
        prepareGeneView(model, activeMarkerID);

        return "marker/gene/gene-view";
    }

    public void setExpressionService(ExpressionService expressionService) {
        this.expressionService = expressionService;
    }

    public void setMarkerRepository(MarkerRepository markerRepository) {
        this.markerRepository = markerRepository;
    }


    @RequestMapping(value = "/{geneID}/phenotype-summary")
    public String phenotypeSummary(Model model
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
        model.addAttribute("title", "ZFIN Phenotype Figure Summary for Marker " + marker.getAbbreviation());
        return "marker/phenotype-summary";
    }

    @RequestMapping(value = {"/{geneID}/expression", "/gene/view/{geneID}/expression"})
    public String getExpression(Model model, @PathVariable String geneID) {
        Marker marker = getMarkerRepository().getMarkerByID(geneID);
        if (marker == null) {
            model.addAttribute(LookupStrings.ZDB_ID, geneID);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        String searchLink = new ExpressionSearchService.LinkBuilder()
            .gene(marker)
            .title("Expression for Marker " + marker.getAbbreviation())
            .build();
        return "forward:" + searchLink;
    }

    @RequestMapping(value = {"/{geneID}/wt-expression", "/gene/view/{geneID}/wt-expression"})
    public String getWildtypeExpression(Model model, @PathVariable String geneID) {
        Marker marker = getMarkerRepository().getMarkerByID(geneID);
        if (marker == null) {
            model.addAttribute(LookupStrings.ZDB_ID, geneID);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        String searchLink = new ExpressionSearchService.LinkBuilder()
            .wildtypeOnly(true)
            .gene(marker)
            .title("Wildtype Expression for Marker " + marker.getAbbreviation())
            .build();
        return "forward:" + searchLink;
    }

    @RequestMapping(value = {"/{geneID}/wt-expression/images", "/gene/view/{geneID}/wt-expression/images"})
    public String getWildtypeExpressionImages(@PathVariable String geneID, Model model, HttpServletRequest request) {
        return searchController.viewWtExpressionGalleryResults(geneID, model, request);
    }


    @RequestMapping(value = "/{geneID}/download/orthology")
    public void getOrthologyCSV(@PathVariable String geneID, HttpServletResponse response) {
        Marker marker = getMarkerRepository().getMarkerByID(geneID);
        if (marker == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setContentType("data:text/csv;charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + marker.getAbbreviation() + "-" + marker.getZdbID() + "-orthology.csv\"");


        try {
            OutputStream resOs = response.getOutputStream();
            OutputStream buffOs = new BufferedOutputStream(resOs);
            OutputStreamWriter outputwriter = new OutputStreamWriter(buffOs);
            CSVFormat csvFormat = CSVFormat.DEFAULT.withRecordSeparator("\n");
            CSVPrinter csvPrinter = new CSVPrinter(outputwriter, csvFormat);

            OrthologyPresentationBean orthologyBean = MarkerService.getOrthologyEvidence(marker);

            //print column headers
            csvPrinter.printRecord("species", "symbol", "location", "accession", "pub_id", "evidence");

            for (OrthologyPresentationRow row : orthologyBean.getOrthologs()) {
                for (OrthologExternalReference orthologExternalReference : row.getAccessions()) {
                    for (OrthologEvidencePresentation orthologEvidencePresentation : row.getEvidence()) {
                        for (Publication publication : orthologEvidencePresentation.getPublications()) {
                            csvPrinter.printRecord(
                                row.getSpecies(),
                                row.getAbbreviation(),
                                row.getChromosome(),
                                orthologExternalReference.getReferenceDatabase().getForeignDB().getDbName() + ":" + orthologExternalReference.getAccessionNumber(),
                                publication.getZdbID(),
                                orthologEvidencePresentation.getCode().getName()
                            );
                        }

                    }
                }


            }

            outputwriter.flush();
            outputwriter.close();

        } catch (IOException e) {
            logger.error(e);
        }
    }

    @ResponseBody
    @RequestMapping(value = "/genes")
    public AllGeneDTO getAllGenes() throws Exception {
        return getFirstGenes(0);
    }

    @ResponseBody
    @RequestMapping(value = "/diseases")
    public AllDiseaseDTO getAllDiseases() throws Exception {
        return getFirstDiseases(0);
    }

    @ResponseBody
    @RequestMapping(value = "/expression")
    public AllExpressionDTO getAllExpression() throws Exception {
        return getFirstExpression(0);
    }

    @ResponseBody
    @RequestMapping(value = "/all-genes/{number}")
    public AllGeneDTO getFirstGenes(@PathVariable("number") int number) throws Exception {
        BasicGeneInfo info = new BasicGeneInfo(number);
        return info.getAllGeneInfo();
    }

    @ResponseBody
    @RequestMapping(value = "/diseases/{number}")
    public AllDiseaseDTO getFirstDiseases(@PathVariable("number") int number) throws Exception {
        DiseaseInfo info = new DiseaseInfo(number);
        return info.getDiseaseInfo(number);
    }

    @ResponseBody
    @RequestMapping(value = "/expression/{number}")
    public AllExpressionDTO getFirstExpression(@PathVariable("number") int number) throws Exception {
        BasicExpressionInfo info = new BasicExpressionInfo(number);
        return info.getBasicExpressionInfo(number);
    }

}

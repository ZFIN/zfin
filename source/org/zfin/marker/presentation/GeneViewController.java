package org.zfin.marker.presentation;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zfin.expression.FigureService;
import org.zfin.expression.presentation.FigureSummaryDisplay;
import org.zfin.expression.service.ExpressionService;
import org.zfin.framework.presentation.Area;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerHistory;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.agr.AllDiseaseDTO;
import org.zfin.marker.agr.AllGeneDTO;
import org.zfin.marker.agr.BasicGeneInfo;
import org.zfin.marker.agr.DiseaseInfo;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;
import org.zfin.orthology.OrthologExternalReference;
import org.zfin.orthology.presentation.OrthologEvidencePresentation;
import org.zfin.orthology.presentation.OrthologyPresentationRow;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.DisplayGroup;
import org.zfin.sequence.service.SequenceService;
import org.zfin.sequence.service.TranscriptService;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.List;

import static org.zfin.repository.RepositoryFactory.getMarkerRepository;

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
        geneBean.setPathwayDBLinks(
                markerRepository.getMarkerDBLinksFast(gene, DisplayGroup.GroupName.PATHWAYS));

        model.addAttribute(LookupStrings.FORM_BEAN, geneBean);
        model.addAttribute("markerHistoryReasonCodes", MarkerHistory.Reason.values());
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, Area.GENE.getTitleString() + gene.getAbbreviation());

        return "marker/gene-view.page";
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
        return "marker/phenotype-summary.page";
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

}

package org.zfin.marker.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.zfin.database.InformixUtil;
import org.zfin.expression.ExpressionResult;
import org.zfin.expression.presentation.ExpressionDisplay;
import org.zfin.expression.repository.ExpressionRepository;
import org.zfin.expression.service.ExpressionService;
import org.zfin.feature.Feature;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.gbrowse.GBrowseService;
import org.zfin.gbrowse.presentation.GBrowseImage;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.mapping.GenomeLocation;
import org.zfin.mapping.MarkerGenomeLocation;
import org.zfin.mapping.repository.LinkageRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;
import org.zfin.mutant.GenotypeFigure;
import org.zfin.mutant.PhenotypeService;
import org.zfin.mutant.PhenotypeStatementWarehouse;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.publication.presentation.PublicationPresentation;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.ForeignDB;
import org.zfin.sequence.ForeignDBDataType;
import org.zfin.sequence.ReferenceDatabase;
import org.zfin.sequence.blast.Database;

import java.util.*;

@Controller
@RequestMapping("/marker")
public class SequenceTargetingReagentViewController {

    private Logger logger = Logger.getLogger(SequenceTargetingReagentViewController.class);

    @Autowired
    private MarkerRepository markerRepository;

    @Autowired
    private MarkerService markerService;

    @Autowired
    private LinkageRepository linkageRepository;

    //    private String ncbiBlastUrl ;
    private List<Database> databases;

    public SequenceTargetingReagentViewController() {
        ReferenceDatabase referenceDatabase = RepositoryFactory.getSequenceRepository()
                .getZebrafishSequenceReferenceDatabase(ForeignDB.AvailableName.PUBRNA, ForeignDBDataType.DataType.RNA);
        databases = referenceDatabase.getOrderedRelatedBlastDB();
        HibernateUtil.closeSession();
    }

    @RequestMapping(value = "/str/view/{zdbID}")
    public String getView(Model model, @PathVariable("zdbID") String zdbID) throws Exception {
        // set base bean
        SequenceTargetingReagentBean sequenceTargetingReagentBean = new SequenceTargetingReagentBean();

        zdbID = markerService.getActiveMarkerID(zdbID);
        logger.info("zdbID: " + zdbID);
        SequenceTargetingReagent sequenceTargetingReagent = markerRepository.getSequenceTargetingReagent(zdbID);
        logger.info("sequenceTargetingReagent: " + sequenceTargetingReagent);

        sequenceTargetingReagentBean.setMarker(sequenceTargetingReagent);
        model.addAttribute("sequenceTargetingReagent", sequenceTargetingReagent);

        MarkerService.createDefaultViewForMarker(sequenceTargetingReagentBean);

        // set targetGenes
        addKnockdownRelationships(sequenceTargetingReagent, sequenceTargetingReagentBean);

        populateConstructList(sequenceTargetingReagentBean, sequenceTargetingReagent);

        // (Antibodies)


        // Expression data
        ExpressionRepository expressionRepository = RepositoryFactory.getExpressionRepository();
        List<ExpressionResult> strExpressionResults = expressionRepository.getExpressionResultsBySequenceTargetingReagent(sequenceTargetingReagent);
        List<String> expressionFigureIDs = expressionRepository.getExpressionFigureIDsBySequenceTargetingReagent(sequenceTargetingReagent);
        List<String> expressionPublicationIDs = expressionRepository.getExpressionPublicationIDsBySequenceTargetingReagent(sequenceTargetingReagent);
        List<ExpressionDisplay> strExpressionDisplays = ExpressionService.createExpressionDisplays(sequenceTargetingReagent.getZdbID(), strExpressionResults, expressionFigureIDs, expressionPublicationIDs, false);
        sequenceTargetingReagentBean.setExpressionDisplays(strExpressionDisplays);

        // PHENOTYPE
        boolean phenoMartRegening = RepositoryFactory.getPhenotypeRepository().getPhenoMartStatus().isSystemUpdateDisabled();
        sequenceTargetingReagentBean.setPhenoMartBeingRegened(phenoMartRegening);

        if (!phenoMartRegening) {
            List<GenotypeFigure> genotypeFigures = MarkerService.getPhenotypeDataForSTR(sequenceTargetingReagent);

            if (genotypeFigures == null || genotypeFigures.size() == 0) {
                sequenceTargetingReagentBean.setPhenotypeDisplays(null);
            } else {
                List<PhenotypeStatementWarehouse> phenotypeStatements = new ArrayList<>();
                for (GenotypeFigure genotypeFigure : genotypeFigures) {
                    PhenotypeStatementWarehouse phenotypeStatement = genotypeFigure.getPhenotypeStatement();
                    if (phenotypeStatement != null) {
                        phenotypeStatements.add(phenotypeStatement);
                    }
                }
                sequenceTargetingReagentBean.setPhenotypeDisplays(PhenotypeService.getPhenotypeDisplays(phenotypeStatements, "str", "phenotypeStatement"));
            }

            List<PhenotypeStatementWarehouse> allPhenotypeStatements = RepositoryFactory.getPhenotypeRepository().getAllPhenotypeStatementsForSTR(sequenceTargetingReagent);

            if (allPhenotypeStatements == null || allPhenotypeStatements.size() == 0) {
                sequenceTargetingReagentBean.setAllPhenotypeDisplays(null);
            } else {
                sequenceTargetingReagentBean.setAllPhenotypeDisplays(PhenotypeService.getPhenotypeDisplays(allPhenotypeStatements, "condition", "fish"));
            }
        }

        // Genomic Features created by STR (CRISPR and TALEN only at this time)
        if (sequenceTargetingReagentBean.isTALEN() || sequenceTargetingReagentBean.isCRISPR()) {
            List<Feature> features = markerRepository.getFeaturesBySTR(sequenceTargetingReagent);
            sequenceTargetingReagentBean.setGenomicFeatures(features);
        }

        // get sequence attribution
        if (sequenceTargetingReagent.getSequence() != null) {
            List<RecordAttribution> attributions = RepositoryFactory.getInfrastructureRepository()
                    .getRecordAttributionsForType(sequenceTargetingReagent.getZdbID(), RecordAttribution.SourceType.SEQUENCE);
            // for this particular set, we only ever want the first one
            if (attributions.size() >= 1) {
                sequenceTargetingReagentBean.setSequenceAttribution(PublicationPresentation.getLink(attributions.iterator().next().getSourceZdbID(), "1"));
            }
        } else {
            logger.warn("No sequence available for the sequence targeting reagent: " + sequenceTargetingReagentBean.getZdbID());
        }

        sequenceTargetingReagentBean.setDatabases(databases);
        String strType = sequenceTargetingReagent.getType().toString();

        // Todo: there should be a better place to store the display name for the different STR entities
        if (strType.equals("MRPHLNO")) {
            strType = "Morpholino";
        }

        // set source
        sequenceTargetingReagentBean.setSuppliers(markerRepository.getSuppliersForMarker(sequenceTargetingReagent.getZdbID()));
        model.addAttribute(LookupStrings.FORM_BEAN, sequenceTargetingReagentBean);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, strType + ": " + sequenceTargetingReagent.getAbbreviation());

        return "marker/sequence-targeting-reagent-view.page";
    }

    @RequestMapping(value = "/view/{zdbID}/str-targeted-genes")
    public String showTargetedGenes(Model model, @PathVariable("zdbID") String zdbID) {
        SequenceTargetingReagentBean sequenceTargetingReagentBean = new SequenceTargetingReagentBean();
        SequenceTargetingReagent sequenceTargetingReagent = markerRepository.getSequenceTargetingReagent(zdbID);
        sequenceTargetingReagentBean.setMarker(sequenceTargetingReagent);

        addKnockdownRelationships(sequenceTargetingReagent, sequenceTargetingReagentBean);

        model.addAttribute(LookupStrings.FORM_BEAN, sequenceTargetingReagentBean);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, sequenceTargetingReagent.getAbbreviation() + " Target Locations");

        return "marker/sequence-targeting-reagent-target-view.page";
    }


    @RequestMapping(value = "/popup/{zdbID}")
    public String getPopup(Model model, @PathVariable("zdbID") String zdbID) throws Exception {
        //getView(model, zdbID);

        SequenceTargetingReagentBean sequenceTargetingReagentBean = new SequenceTargetingReagentBean();

        logger.info("zdbID: " + zdbID);
        SequenceTargetingReagent sequenceTargetingReagent = markerRepository.getSequenceTargetingReagent(zdbID);
        logger.info("sequenceTargetingReagent: " + sequenceTargetingReagent);

        sequenceTargetingReagentBean.setMarker(sequenceTargetingReagent);
        model.addAttribute("sequenceTargetingReagent", sequenceTargetingReagent);

        MarkerService.createDefaultViewForMarker(sequenceTargetingReagentBean);

        // set targetGenes
        addKnockdownRelationships(sequenceTargetingReagent, sequenceTargetingReagentBean);


        model.addAttribute(LookupStrings.FORM_BEAN, sequenceTargetingReagentBean);

        return "marker/sequence-targeting-reagent-popup.popup";
    }


    @RequestMapping(value = "/call-regen-genox", method = RequestMethod.GET)
    public
    @ResponseBody
    int runRegenGenoxForAllTargets(@RequestParam("sequenceTargetingReagentZdbId") String sequenceTargetingReagentZdbId) {
        SequenceTargetingReagent sequenceTargetingReagent = markerRepository.getSequenceTargetingReagent(sequenceTargetingReagentZdbId);
        List<MarkerRelationshipPresentation> knockdownRelationships = new ArrayList<>();
        knockdownRelationships.addAll(markerRepository.getRelatedMarkerOrderDisplayForTypes(
                sequenceTargetingReagent, true
                , MarkerRelationship.Type.KNOCKDOWN_REAGENT_TARGETS_GENE
        ));
        for (MarkerRelationshipPresentation targetGene : knockdownRelationships) {
            InformixUtil.runInformixProcedure("regen_genox_marker", targetGene.getZdbId());
        }
        return knockdownRelationships.size();
    }

    protected void populateConstructList(SequenceTargetingReagentBean sequenceTargetingReagentBean, SequenceTargetingReagent sequenceTargetingReagent) {
        Set<MarkerRelationship.Type> types = new HashSet<>();
        types.add(MarkerRelationship.Type.PROMOTER_OF);
        types.add(MarkerRelationship.Type.CODING_SEQUENCE_OF);
        types.add(MarkerRelationship.Type.CONTAINS_REGION);
        Set<Marker> markerSet = new TreeSet<>();
        PaginationResult<Marker> relatedMarker = MarkerService.getRelatedMarker(sequenceTargetingReagent, types, 7);
        markerSet.addAll(relatedMarker.getPopulatedResults());
        sequenceTargetingReagentBean.setConstructs(markerSet);
        sequenceTargetingReagentBean.setNumberOfConstructs(relatedMarker.getTotalCount());
    }

    @RequestMapping(value = "/str/constructs/{zdbID}")
    public String getAllConstructs(Model model,
                                   @PathVariable("zdbID") String zdbID
    ) throws Exception {
        // set base bean
        SequenceTargetingReagentBean strBean = new SequenceTargetingReagentBean();
        Marker str = markerRepository.getMarkerByID(zdbID);
        logger.info("gene: " + str);
        strBean.setMarker(str);
        // (CONSTRUCTS)
        Set<MarkerRelationship.Type> types = new HashSet<>();
        types.add(MarkerRelationship.Type.PROMOTER_OF);
        types.add(MarkerRelationship.Type.CODING_SEQUENCE_OF);
        types.add(MarkerRelationship.Type.CONTAINS_REGION);
        Set<Marker> markerSet = new TreeSet<>();
        // get all constructs
        PaginationResult<Marker> relatedMarker = MarkerService.getRelatedMarker(str, types, -1);
        markerSet.addAll(relatedMarker.getPopulatedResults());
        strBean.setConstructs(markerSet);
        strBean.setNumberOfConstructs(relatedMarker.getTotalCount());
        model.addAttribute(LookupStrings.FORM_BEAN, strBean);
        return "marker/str-all-constructs.ajax";
    }


    private void addKnockdownRelationships(SequenceTargetingReagent sequenceTargetingReagent,
                                           SequenceTargetingReagentBean sequenceTargetingReagentBean) {
        List<MarkerRelationshipPresentation> knockdownRelationships = new ArrayList<>();
        knockdownRelationships.addAll(markerRepository.getRelatedMarkerOrderDisplayForTypes(
                sequenceTargetingReagent, true
                , MarkerRelationship.Type.KNOCKDOWN_REAGENT_TARGETS_GENE,MarkerRelationship.Type.CRISPR_TARGETS_REGION,MarkerRelationship.Type.TALEN_TARGETS_REGION
        ));
        sequenceTargetingReagentBean.setMarkerRelationshipPresentationList(knockdownRelationships);

        // get gbrowse genomic location of each targeted gene
        List<MarkerGenomeLocation> targetedGeneLocations = new ArrayList<>();
        for (MarkerRelationshipPresentation knockdownRelationship : knockdownRelationships) {
            Marker targetedGene = markerRepository.getGeneByID(knockdownRelationship.getZdbId());
            List<MarkerGenomeLocation> locations = linkageRepository.getGenomeLocation(targetedGene, GenomeLocation.Source.ZFIN);
            if (CollectionUtils.isNotEmpty(locations)) {
                // if we have location(s) for targeted gene, just take the first one... i guess
                targetedGeneLocations.add(locations.get(0));
            }
        }

        // get gbrowse genomic locations of str itself
        List<MarkerGenomeLocation> strLocations = linkageRepository.getGenomeLocation(sequenceTargetingReagent, GenomeLocation.Source.ZFIN);

        // for talens there should be two location that need to be combined into one. it might be better to do this upstream
        // in the GFF3 track generation (e.g. with parent-child relationships), but this works for now.
        if (CollectionUtils.isNotEmpty(strLocations) && sequenceTargetingReagent.getMarkerType().getType() == Marker.Type.TALEN) {
            MarkerGenomeLocation combinedLocation = null;
            for (MarkerGenomeLocation location : strLocations) {
                if (combinedLocation == null) {
                    combinedLocation = location;
                } else {
                    combinedLocation.setStart(Math.min(combinedLocation.getStart(), location.getStart()));
                    combinedLocation.setEnd(Math.max(combinedLocation.getEnd(), location.getEnd()));
                }
            }
            strLocations = Arrays.asList(combinedLocation);
        }

        if (targetedGeneLocations.size() == 1 && strLocations.size() == 1 &&
                targetedGeneLocations.get(0).getChromosome().equals(strLocations.get(0).getChromosome())) {
            // one STR location + one targeted location on the same chromosome: show the STR in the context of the gene

            // str is not necessarily contained in the gene
            MarkerGenomeLocation mergedLocation = targetedGeneLocations.get(0);
            mergedLocation.setStart(Math.min(mergedLocation.getStart(), strLocations.get(0).getStart()));
            mergedLocation.setEnd(Math.max(mergedLocation.getEnd(), strLocations.get(0).getEnd()));

            sequenceTargetingReagentBean.addGBrowseImage(GBrowseImage.builder()
                    .landmark(mergedLocation)
                    .withPadding(0.1)
                    .tracks(GBrowseService.getGBrowseTracks(sequenceTargetingReagent))
                    .highlight(sequenceTargetingReagent)
                    .build()
            );
        } else {
            // otherwise: just show each STR location with 10kbp padding
            for (MarkerGenomeLocation location : strLocations) {
                sequenceTargetingReagentBean.addGBrowseImage(GBrowseImage.builder()
                        .landmark(location)
                        .withPadding(10000)
                        .tracks(GBrowseService.getGBrowseTracks(sequenceTargetingReagent))
                        .highlight(sequenceTargetingReagent)
                        .build()
                );
            }
        }

    }
}



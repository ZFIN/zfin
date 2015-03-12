package org.zfin.marker.presentation;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zfin.database.InformixUtil;
import org.zfin.expression.ExpressionResult;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;
import org.zfin.mutant.*;
import org.zfin.mutant.presentation.GenotypeInformation;
import org.zfin.publication.presentation.PublicationPresentation;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.ForeignDB;
import org.zfin.sequence.ForeignDBDataType;
import org.zfin.sequence.ReferenceDatabase;
import org.zfin.sequence.blast.Database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 */
@Controller
@RequestMapping("/marker")
public class SequenceTargetingReagentViewController {

    private Logger logger = Logger.getLogger(SequenceTargetingReagentViewController.class);

    @Autowired
    private MarkerRepository markerRepository;

    //    private String ncbiBlastUrl ;
    private List<Database> databases;

    public SequenceTargetingReagentViewController() {
        ReferenceDatabase referenceDatabase = RepositoryFactory.getSequenceRepository()
                .getZebrafishSequenceReferenceDatabase(ForeignDB.AvailableName.PUBRNA, ForeignDBDataType.DataType.RNA);
        databases = referenceDatabase.getOrderedRelatedBlastDB();
    }

    @RequestMapping(value = "/marker/view/{zdbID}")
    public String getView(
            Model model
            , @RequestParam("zdbID") String zdbID
    ) throws Exception {
        // set base bean
        SequenceTargetingReagentBean sequenceTargetingReagentBean = new SequenceTargetingReagentBean();

        logger.info("zdbID: " + zdbID);
        SequenceTargetingReagent sequenceTargetingReagent = markerRepository.getSequenceTargetingReagent(zdbID);
        logger.info("sequenceTargetingReagent: " + sequenceTargetingReagent);

        sequenceTargetingReagentBean.setMarker(sequenceTargetingReagent);
        model.addAttribute("sequenceTargetingReagent", sequenceTargetingReagent);

        MarkerService.createDefaultViewForMarker(sequenceTargetingReagentBean);

        // set targetGenes
//        Set<Marker> targetGenes = MarkerService.getRelatedMarker(morpholino, MarkerRelationship.Type.KNOCKDOWN_REAGENT_TARGETS_GENE);
//        markerBean.setTargetGenes(targetGenes);
        List<MarkerRelationshipPresentation> knockdownRelationships = new ArrayList<>();
        knockdownRelationships.addAll(markerRepository.getRelatedMarkerOrderDisplayForTypes(
                sequenceTargetingReagent, true
                , MarkerRelationship.Type.KNOCKDOWN_REAGENT_TARGETS_GENE
        ));
        sequenceTargetingReagentBean.setMarkerRelationshipPresentationList(knockdownRelationships);

        // PHENOTYPE
        List<GenotypeFigure> genotypeFigures = MarkerService.getPhenotypeDataForSTR(sequenceTargetingReagent);
        if (genotypeFigures == null || genotypeFigures.size() == 0)  {
            sequenceTargetingReagentBean.setPhenotypeDisplays(null);
        } else {
            List<PhenotypeStatement> phenoStatements = new ArrayList<>();
            for (GenotypeFigure genoFig : genotypeFigures) {
                if (genoFig.getPhenotypeExperiment() != null) {
                    if (genoFig.getPhenotypeExperiment().getPhenotypeStatements() != null)  {
                        phenoStatements.addAll(genoFig.getPhenotypeExperiment().getPhenotypeStatements());
                    }
                }
            }
            sequenceTargetingReagentBean.setPhenotypeDisplays(PhenotypeService.getPhenotypeDisplays(phenoStatements,"fish"));
        }

        // GENOTYPE (for CRISPR and TALEN only at this time)
        if (sequenceTargetingReagentBean.isTALEN() || sequenceTargetingReagentBean.isCRISPR()) {
            List<Genotype> genotypes = markerRepository.getTALENorCRISPRcreatedGenotypes(zdbID);
            sequenceTargetingReagentBean.setGenotypes(genotypes);
            List<GenotypeInformation> genoData = new ArrayList<>();
            if (genotypes == null) {
                sequenceTargetingReagentBean.setGenotypeData(null);
            } else {
                for (Genotype geno : genotypes) {
                    GenotypeInformation genoInfo = new GenotypeInformation(geno);
                    genoData.add(genoInfo);
                }
                Collections.sort(genoData);
                sequenceTargetingReagentBean.setGenotypeData(genoData);
            }
        }

        // Expression data
        List<ExpressionResult> strExpressionResults = RepositoryFactory.getExpressionRepository().getExpressionResultsBySequenceTargetingReagent(sequenceTargetingReagent);
        sequenceTargetingReagentBean.setExpressionResults(strExpressionResults);
        List<String> expressionFigureIDs = RepositoryFactory.getExpressionRepository().getExpressionFigureIDsBySequenceTargetingReagent(sequenceTargetingReagent);
        sequenceTargetingReagentBean.setExpressionFigureIDs(expressionFigureIDs);
        List<String> expressionPublicationIDs = RepositoryFactory.getExpressionRepository().getExpressionPublicationIDsBySequenceTargetingReagent(sequenceTargetingReagent);
        sequenceTargetingReagentBean.setExpressionPublicationIDs(expressionPublicationIDs);

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
        for (MarkerRelationshipPresentation targetGene: knockdownRelationships) {
            InformixUtil.runInformixProcedure("regen_genox_marker", targetGene.getZdbId());
        }
        return knockdownRelationships.size();
    }
}



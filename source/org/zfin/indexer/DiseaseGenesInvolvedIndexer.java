package org.zfin.indexer;

import lombok.extern.log4j.Log4j2;
import org.hibernate.SessionFactory;
import org.zfin.expression.ExperimentCondition;
import org.zfin.expression.Figure;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.api.Pagination;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.marker.Marker;
import org.zfin.mutant.Fish;
import org.zfin.mutant.PhenotypeStatementWarehouse;
import org.zfin.mutant.presentation.ChebiFishModelDisplay;
import org.zfin.mutant.presentation.FishModelDisplay;
import org.zfin.mutant.presentation.FishStatistics;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.OmimPhenotypeDisplay;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.service.OntologyService;
import org.zfin.properties.ZfinProperties;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;
import static org.zfin.repository.RepositoryFactory.getOntologyRepository;

@Log4j2
public class DiseaseGenesInvolvedIndexer {

    public static void main(String[] args) {
        DiseaseGenesInvolvedIndexer indexer = new DiseaseGenesInvolvedIndexer();
        indexer.init();
        indexer.runFishModels();
        //indexer.runGenesInvolved();
        //indexer.runTermPhenotype();
        System.out.println("Finished Indexing");
    }

    private void runFishModels() {
        HibernateUtil.createTransaction();
        List<FishModelDisplay> diseaseModelsWithFishModel = OntologyService.getDiseaseModelsWithFishModelsGrouped(null, false, new Pagination());
        diseaseModelsWithFishModel.forEach(fishModelDisplay -> {
            fishModelDisplay.setEvidenceSearch(fishModelDisplay.getFishModel().getDiseaseAnnotationModels().stream()
                .map(model -> DTOConversionService.evidenceCodeIdToAbbreviation(model.getDiseaseAnnotation().getEvidenceCode().getZdbID()))
                .distinct()
                .collect(Collectors.joining(",")));
            fishModelDisplay.setEvidenceCode(fishModelDisplay.getFishModel().getDiseaseAnnotationModels().stream().map(model -> model.getDiseaseAnnotation().getEvidenceCode()).collect(Collectors.toList()));
            fishModelDisplay.setFishSearch(fishModelDisplay.getFish().getName().replaceAll("<[^>]*>", ""));
            fishModelDisplay.setConditionSearch(fishModelDisplay.getFishModel().getExperiment().getDisplayAllConditions());
            HibernateUtil.currentSession().save(fishModelDisplay);
            Set<GenericTerm> chebiTerms = fishModelDisplay.getFishModel().getDiseaseAnnotationModels().stream()
                .map(diseaseAnnotationModel -> diseaseAnnotationModel.getFishExperiment().getExperiment().getExperimentConditions().stream()
                    .map(ExperimentCondition::getChebiTerm).toList()).toList()
                .stream().flatMap(Collection::stream).collect(toSet());

            chebiTerms.stream().filter(Objects::nonNull)
                .forEach(chebiTerm -> {
                    ChebiFishModelDisplay display = new ChebiFishModelDisplay();
                    display.setFishModelDisplay(fishModelDisplay);
                    display.setChebi(chebiTerm);
                    HibernateUtil.currentSession().save(display);
                });

        });
        HibernateUtil.flushAndCommitCurrentSession();
    }

    private void runGenesInvolved() {
        HibernateUtil.createTransaction();
        //GenericTerm term = getOntologyRepository().getTermByOboID("DOID:0112009");
        List<GenericTerm> diseaseList = getOntologyRepository().getAllTermsFromOntology(Ontology.DISEASE_ONTOLOGY);
        //GenericTerm term = getOntologyRepository().getTermByOboID("DOID:9952");
        diseaseList.forEach(term -> {
            List<OmimPhenotypeDisplay> displayListSingle = OntologyService.getOmimPhenotype(term, new Pagination(), false);
            OntologyService.fixupSearchColumns(displayListSingle);
            displayListSingle.forEach(omimDisplay -> HibernateUtil.currentSession().save(omimDisplay));
        });
        HibernateUtil.flushAndCommitCurrentSession();
        //log.info("Number of Records: "+displayListSingle.size());
    }

    private void runTermPhenotype() {
        HibernateUtil.createTransaction();
        Map<Fish, Map<GenericTerm, List<PhenotypeStatementWarehouse>>> figureMap = RepositoryFactory.getPublicationRepository().getAllFiguresForPhenotype();

        figureMap.forEach((fish, termMap) -> termMap.forEach((term, phenotypeStatementWarehouses) -> {
            FishStatistics stat = new FishStatistics(fish, term);
            stat.setAffectedGenes(fish.getAffectedGenes());
            stat.setPhenotypeStatements(phenotypeStatementWarehouses);
            Set<Figure> figs = phenotypeStatementWarehouses.stream().map(warehouse -> warehouse.getPhenotypeWarehouse().getFigure()).collect(Collectors.toSet());
            stat.setNumberOfFigs(figs.size());
            if (figs.size() == 1) {
                stat.setFigure(figs.iterator().next());
            }
            Set<Publication> pubs = phenotypeStatementWarehouses.stream().map(warehouse -> warehouse.getPhenotypeWarehouse().getFigure().getPublication()).collect(Collectors.toSet());
            stat.setNumberOfPubs(pubs.size());
            if (pubs.size() == 1) {
                stat.setPublication(pubs.iterator().next());
            }
            stat.setFishSearch(fish.getName().replaceAll("<[^>]*>", ""));
            stat.setPhenotypeStatementSearch(phenotypeStatementWarehouses.stream().map(PhenotypeStatementWarehouse::getDisplayName).collect(Collectors.joining("|")));
            stat.setGeneSymbolSearch(fish.getAffectedGenes().stream().map(Marker::getAbbreviation).sorted().collect(Collectors.joining("|")));
            HibernateUtil.currentSession().save(stat);
        }));
        HibernateUtil.flushAndCommitCurrentSession();
        //log.info("Number of Records: "+displayListSingle.size());
    }

    public void init() {
        ZfinProperties.init();
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        if (sessionFactory == null) {
            new HibernateSessionCreator(false);
        }
    }

}

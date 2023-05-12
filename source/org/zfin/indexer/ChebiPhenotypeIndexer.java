package org.zfin.indexer;

import lombok.extern.log4j.Log4j2;
import org.zfin.expression.Experiment;
import org.zfin.expression.ExperimentCondition;
import org.zfin.expression.Figure;
import org.zfin.framework.HibernateUtil;
import org.zfin.marker.Marker;
import org.zfin.mutant.Fish;
import org.zfin.mutant.PhenotypeStatement;
import org.zfin.mutant.PhenotypeStatementWarehouse;
import org.zfin.mutant.presentation.ChebiPhenotypeDisplay;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.OmimPhenotypeDisplay;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

import javax.persistence.JoinTable;
import javax.persistence.Table;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

@Log4j2
public class ChebiPhenotypeIndexer extends UiIndexer<ChebiPhenotypeDisplay> {

    public ChebiPhenotypeIndexer(UiIndexerConfig config) {
        super(config);
    }

    @Override
    protected List<ChebiPhenotypeDisplay> inputOutput() {
        Map<Fish, Map<Experiment, Map<GenericTerm, Set<PhenotypeStatementWarehouse>>>> figureMap = RepositoryFactory.getPublicationRepository().getAllChebiPhenotype();
        List<ChebiPhenotypeDisplay> resultList = new ArrayList<>();
        figureMap.forEach((fish, termMap) -> termMap.forEach((experiment, experimentMao) -> {
            experimentMao.forEach((term, phenotypeStatementWarehouses) -> {
                ChebiPhenotypeDisplay display = new ChebiPhenotypeDisplay(fish, term);
                List<PhenotypeStatementWarehouse> phenotypeStatements = (new ArrayList<>(phenotypeStatementWarehouses).stream().sorted()).toList();
                display.setPhenotypeStatements(phenotypeStatements);
                display.setExperiment(experiment);
                if (experiment.getExperimentConditions().stream().filter(experimentCondition -> experimentCondition.getChebiTerm() != null).collect(toSet()).size() > 1) {
                    display.setMultiChebiCondition(true);
                }
                Set<String> phenotypeTags = phenotypeStatements.stream()
                    .filter(warehouse -> (warehouse.getTag().equals(PhenotypeStatement.Tag.AMELIORATED.toString()) ||
                                          warehouse.getTag().equals(PhenotypeStatement.Tag.EXACERBATED.toString())))
                    .map(PhenotypeStatementWarehouse::getTag)
                    .collect(toSet());
                if (phenotypeTags.size() > 0) {
                    display.setAmelioratedExacerbatedPhenoSearch(String.join(", ", phenotypeTags));
                }
                Set<Figure> figs = phenotypeStatementWarehouses.stream().map(warehouse -> warehouse.getPhenotypeWarehouse().getFigure()).collect(Collectors.toSet());
                display.setNumberOfFigs(figs.size());
                if (figs.size() == 1) {
                    display.setFigure(figs.iterator().next());
                }
                Set<Publication> pubs = phenotypeStatementWarehouses.stream().map(warehouse -> warehouse.getPhenotypeWarehouse().getFigure().getPublication()).collect(Collectors.toSet());
                display.setNumberOfPubs(pubs.size());
                if (pubs.size() == 1) {
                    display.setPublication(pubs.iterator().next());
                }
                display.setFishSearch(fish.getName().replaceAll("<[^>]*>", ""));
                display.setPhenotypeStatementSearch(phenotypeStatementWarehouses.stream().map(PhenotypeStatementWarehouse::getDisplayName).collect(Collectors.joining("|")));
                display.setGeneSymbolSearch(fish.getAffectedGenes().stream().map(Marker::getAbbreviation).sorted().collect(Collectors.joining("|")));
                display.setConditionSearch(experiment.getDisplayAllConditions());
                display.setSingleMisfortune(experiment.isSingleMisfortune());
                display.setExpConditionChebiSearch(experiment.getExperimentConditions().stream()
                    .filter(experimentCondition -> experimentCondition.getChebiTerm() != null)
                    .map(ExperimentCondition::getChebiTerm)
                    .map(GenericTerm::getTermName)
                    .collect(Collectors.joining("|")));
                resultList.add(display);
            });
        }));
        return resultList;
    }

    @Override
    protected void cleanUiTables() {
        try {
            String associationTable = ChebiPhenotypeDisplay.class.getDeclaredField("phenotypeStatements").getAnnotation(JoinTable.class).name();
            String chebiPhenotypeTable = ChebiPhenotypeDisplay.class.getAnnotation(Table.class).name();
            cleanoutTable(associationTable, chebiPhenotypeTable);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

}

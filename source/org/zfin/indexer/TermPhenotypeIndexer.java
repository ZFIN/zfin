package org.zfin.indexer;

import jakarta.persistence.JoinTable;
import jakarta.persistence.Table;
import lombok.extern.log4j.Log4j2;
import org.zfin.expression.Figure;
import org.zfin.marker.Marker;
import org.zfin.mutant.Fish;
import org.zfin.mutant.PhenotypeStatementWarehouse;
import org.zfin.mutant.presentation.FishStatistics;
import org.zfin.ontology.GenericTerm;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

import java.util.*;
import java.util.stream.Collectors;

import static org.zfin.util.ZfinCollectionUtils.firstInEachGrouping;

@Log4j2
public class TermPhenotypeIndexer extends UiIndexer<FishStatistics> {

    public TermPhenotypeIndexer(UiIndexerConfig config) {
        super(config);
    }

    @Override
    protected List<FishStatistics> inputOutput() {
        Map<Fish, Map<GenericTerm, List<PhenotypeStatementWarehouse>>> figureMap = RepositoryFactory.getPublicationRepository().getAllFiguresForPhenotype();
        List<FishStatistics> resultList = new ArrayList<>();
        figureMap.forEach((fish, termMap) -> termMap.forEach((term, phenotypeStatementWarehouses) -> {
            FishStatistics stat = new FishStatistics(fish, term);
            stat.setAffectedGenes(new HashSet<>(fish.getAffectedGenes()));
            // remove phenotype statements with the same display name
            SortedSet<PhenotypeStatementWarehouse> set1 = new TreeSet<>(firstInEachGrouping(phenotypeStatementWarehouses, PhenotypeStatementWarehouse::getShortName));
            stat.setPhenotypeStatements(set1);
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
            stat.setPhenotypeStatementSearch(phenotypeStatementWarehouses.stream().map(PhenotypeStatementWarehouse::getShortName).collect(Collectors.joining("|")));
            stat.setGeneSymbolSearch(fish.getAffectedGenes().stream().map(Marker::getAbbreviation).sorted().collect(Collectors.joining("|")));
            resultList.add(stat);
        }));
        return resultList;
    }

    @Override
    protected void cleanUiTables() {
        try {
            String schema = FishStatistics.class.getDeclaredField("affectedGenes").getAnnotation(JoinTable.class).schema();
            String associationTable = FishStatistics.class.getDeclaredField("affectedGenes").getAnnotation(JoinTable.class).name();
            String associationTable1 = FishStatistics.class.getDeclaredField("phenotypeStatements").getAnnotation(JoinTable.class).name();
            String fishStatsTable = FishStatistics.class.getAnnotation(Table.class).name();
            cleanoutTable(schema, associationTable, associationTable1, fishStatsTable);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }


}

package org.zfin.indexer;

import lombok.extern.log4j.Log4j2;
import org.zfin.expression.Figure;
import org.zfin.framework.HibernateUtil;
import org.zfin.marker.Marker;
import org.zfin.mutant.Fish;
import org.zfin.mutant.PhenotypeStatementWarehouse;
import org.zfin.mutant.presentation.ChebiPhenotypeDisplay;
import org.zfin.mutant.presentation.FishStatistics;
import org.zfin.ontology.GenericTerm;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

import javax.persistence.JoinTable;
import javax.persistence.Table;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.getDiseasePageRepository;

@Log4j2
public class TermPhenotypeIndexer extends UiIndexer<GenericTerm> {

    public TermPhenotypeIndexer(UiIndexerConfig config) {
        super(config);
    }

    @Override
    protected void index() {
        Map<Fish, Map<GenericTerm, List<PhenotypeStatementWarehouse>>> figureMap = retrieveRecordMap();
        cleanUiTables();
        saveRecordMap(figureMap);
    }

    @Override
    protected List<GenericTerm> retrieveRecords() {
        return null;
    }

    protected Map<Fish, Map<GenericTerm, List<PhenotypeStatementWarehouse>>> retrieveRecordMap() {
        indexerHelper = new IndexerHelper();
        startTransaction("Start retrieving term phenotype...");
        Map<Fish, Map<GenericTerm, List<PhenotypeStatementWarehouse>>> figureMap = RepositoryFactory.getPublicationRepository().getAllFiguresForPhenotype();
        commitTransaction("Finished retrieving term phenotype: ", figureMap.size());
        return figureMap;
    }

    @Override
    protected void cleanUiTables() {
        try {
            String associationTable = FishStatistics.class.getDeclaredField("affectedGenes").getAnnotation(JoinTable.class).name();
            String associationTable1 = FishStatistics.class.getDeclaredField("phenotypeStatements").getAnnotation(JoinTable.class).name();
            String fishStatsTable = FishStatistics.class.getAnnotation(Table.class).name();
            cleanoutTable(associationTable, associationTable1, fishStatsTable);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void saveRecords(List<GenericTerm> diseaseList) {
    }

    protected void saveRecordMap(Map<Fish, Map<GenericTerm, List<PhenotypeStatementWarehouse>>> figureMap) {
        indexerHelper = new IndexerHelper();
        startTransaction("Start saving term phenotype...");
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
        commitTransaction("Finished saving term phenotype: ", figureMap.size());
    }


}

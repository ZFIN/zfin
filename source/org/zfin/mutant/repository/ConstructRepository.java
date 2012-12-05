package org.zfin.mutant.repository;

import org.zfin.expression.Experiment;
import org.zfin.expression.ExpressionResult;
import org.zfin.expression.Figure;
import org.zfin.mutant.ConstructSearchCriteria;
import org.zfin.mutant.ConstructSearchResult;
import org.zfin.fish.WarehouseSummary;
import org.zfin.fish.presentation.Fish;
import org.zfin.infrastructure.ZdbFlag;
import org.zfin.infrastructure.ZfinFigureEntity;
import org.zfin.mutant.ConstructSearchCriteria;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.presentation.Construct;

import java.util.List;
import java.util.Set;

/**
 * Basic repository to handle fish search requests.
 */
public interface ConstructRepository {

    public ConstructSearchResult getConstructs(ConstructSearchCriteria criteria);
    public Construct getConstruct(String constructID);
    public List<ExpressionResult> getExpressionForConstructs(String constructID, List<String> termIDs);
     Set<ZfinFigureEntity> getFiguresByConstructAndTerms(String constructID, List<String> termIDs);
    List<Genotype> getFigureGenotype(Figure figure,String constructID);
    Set<ZfinFigureEntity> getAllFigures(String constructID);
    WarehouseSummary getWarehouseSummary(WarehouseSummary.Mart mart);

    /**
     * Retrieve the status of the fish mart:
     * true: fish mart ready for usage
     * false: fish mart is being rebuilt.
     *
     * @return status
     */
    ZdbFlag getConstructMartStatus();


}

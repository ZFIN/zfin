package org.zfin.ui.repository;

import org.zfin.framework.api.Pagination;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.mutant.presentation.ChebiFishModelDisplay;
import org.zfin.mutant.presentation.ChebiPhenotypeDisplay;
import org.zfin.mutant.presentation.FishModelDisplay;
import org.zfin.mutant.presentation.FishStatistics;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.OmimPhenotypeDisplay;

import java.util.List;

public interface DiseasePageRepository {

    PaginationResult<OmimPhenotypeDisplay> getGenesInvolved(GenericTerm term, Pagination pagination, boolean includeChildren);

    PaginationResult<FishStatistics> getPhenotype(GenericTerm term, Pagination pagination, Boolean includeChildren, Boolean isIncludeNormalPhenotype);

    PaginationResult<FishModelDisplay> getFishDiseaseModels(GenericTerm term, Pagination pagination, boolean includeChildren);

    List<ChebiFishModelDisplay> getFishDiseaseChebiModels(GenericTerm term, boolean includeChildren);
    List<FishModelDisplay> getAllFishDiseaseModels();

    PaginationResult<ChebiPhenotypeDisplay> getPhenotypeChebi(GenericTerm term, Pagination pagination, String filterPhenotype, boolean includeChildren);

    int deleteUiTables(String... tableName);
}

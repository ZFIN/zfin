package org.zfin.ontology;

import org.zfin.gwt.root.dto.TermDTO;
import org.zfin.gwt.root.dto.TermNotFoundException;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.repository.RepositoryFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * This class manages the expression and phenotype data associated with a given ontology term.
 */
public class OntologyDataManager {

    private static OntologyDataManager manager;
    private Set<TermDTO> expressionSet = new HashSet<TermDTO>(2000);
    private Set<TermDTO> phenotypeSet = new HashSet<TermDTO>(2000);

    public static OntologyDataManager getInstance() {

        if (manager == null) {
            init();
        }
        return manager;
    }

    private static void init() {
        manager = new OntologyDataManager();
        Set<String> termIDs = RepositoryFactory.getExpressionRepository().getAllDistinctExpressionTermIDs();
        for (String termID : termIDs) {
            TermDTO term = OntologyManager.getInstance().getTermByID(termID);
            manager.expressionSet.add(term);
        }
        Set<String> phenoTermIDs = RepositoryFactory.getExpressionRepository().getAllDistinctPhenotypeTermIDs();
        for (String termID : phenoTermIDs) {
            TermDTO term = OntologyManager.getInstance().getTermByID(termID);
            manager.phenotypeSet.add(term);
        }
    }

    public boolean hasExpressionData(TermDTO term) {
        return expressionSet.contains(term);
    }

    public boolean hasPhenotypeData(TermDTO term) {
        return phenotypeSet.contains(term);
    }

    public boolean hasExpressionOrPhenotypeData(TermDTO term) {
        return hasExpressionData(term) || hasPhenotypeData(term);
    }
}

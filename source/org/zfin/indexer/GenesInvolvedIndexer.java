package org.zfin.indexer;

import lombok.extern.log4j.Log4j2;
import org.hibernate.Session;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.api.Pagination;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.OmimPhenotypeDisplay;
import org.zfin.ontology.service.OntologyService;

import javax.persistence.JoinTable;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.getDiseasePageRepository;
import static org.zfin.repository.RepositoryFactory.getOntologyRepository;

@Log4j2
public class GenesInvolvedIndexer extends UiIndexer<OmimPhenotypeDisplay> {

    public GenesInvolvedIndexer(UiIndexerConfig config) {
        super(config);
    }

    @Override
    protected List<OmimPhenotypeDisplay> inputOutput() {
        Set<GenericTerm> diseaseList = getOntologyRepository().getDiseaseTermsOmimPhenotype();
        List<OmimPhenotypeDisplay> resultList = new ArrayList<>(1000);
        for (GenericTerm term : diseaseList) {
            List<OmimPhenotypeDisplay> displayListSingle = OntologyService.getOmimPhenotype(term, new Pagination(), false);
            OntologyService.fixupSearchColumns(displayListSingle);
            resultList.addAll(displayListSingle);
        }
        return resultList;
    }

    @Override
    protected void cleanUiTables() {
        try {
            String associationTable = OmimPhenotypeDisplay.class.getDeclaredField("zfinGene").getAnnotation(JoinTable.class).name();
            String omimTable = OmimPhenotypeDisplay.class.getAnnotation(Table.class).name();
            cleanoutTable(associationTable, omimTable);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }


}

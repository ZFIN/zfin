package org.zfin.indexer;

import lombok.extern.log4j.Log4j2;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.api.Pagination;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.OmimPhenotypeDisplay;
import org.zfin.ontology.service.OntologyService;

import javax.persistence.JoinTable;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.zfin.repository.RepositoryFactory.getOntologyRepository;

@Log4j2
public class GenesInvolvedIndexer extends UiIndexer<GenericTerm> {

    public GenesInvolvedIndexer(UiIndexerConfig config) {
        super(config);
    }

    @Override
    protected void index() {
        List<GenericTerm> records = retrieveRecords();
        cleanUiTables();
        saveRecords(records);
    }

    @Override
    protected List<GenericTerm> retrieveRecords() {
        indexerHelper = new IndexerHelper();
        startTransaction("Start retrieving genes involved...");
        Set<GenericTerm> diseaseList = getOntologyRepository().getDiseaseTermsOmimPhenotype();
        commitTransaction("Finished retrieving genes involved: ", diseaseList.size());
        return new ArrayList<>(diseaseList);
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

    @Override
    protected void saveRecords(List<GenericTerm> diseaseList) {
        indexerHelper = new IndexerHelper();
        startTransaction("Start saving genes involved...");
        int index = 0;
        for (GenericTerm term : diseaseList) {
            List<OmimPhenotypeDisplay> displayListSingle = OntologyService.getOmimPhenotype(term, new Pagination(), false);
            OntologyService.fixupSearchColumns(displayListSingle);
            displayListSingle.forEach(omimDisplay -> HibernateUtil.currentSession().save(omimDisplay));
            if (index % 200 == 0)
                System.out.println(index);
            index++;
        }
        commitTransaction("Finished saving genes involved: ", diseaseList.size());
    }


}

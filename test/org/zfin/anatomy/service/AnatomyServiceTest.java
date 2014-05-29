package org.zfin.anatomy.service;

import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.mutant.presentation.AntibodyStatistics;
import org.zfin.ontology.GenericTerm;
import org.zfin.repository.RepositoryFactory;

import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AnatomyServiceTest extends AbstractDatabaseTest {


    @Test
    public void testAntibodiesPerAnatomy() {

        PaginationBean pagination = new PaginationBean();
        // forebrain
        GenericTerm aoTerm = RepositoryFactory.getOntologyRepository().getTermByOboID("ZFA:0000109");
        List<AntibodyStatistics> list = RepositoryFactory.getAntibodyRepository().getAntibodyStatistics(aoTerm, pagination, false);
        assertNotNull(list);
        assertTrue(list.size() > 5);

    }


}

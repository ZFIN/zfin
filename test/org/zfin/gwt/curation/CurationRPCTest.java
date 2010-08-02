package org.zfin.gwt.curation;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.zfin.TestConfiguration;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.curation.server.CurationExperimentRPCImpl;
import org.zfin.gwt.curation.ui.CurationExperimentRPC;
import org.zfin.gwt.root.dto.ExperimentDTO;

import java.util.List;

import static junit.framework.Assert.assertNotNull;


public class CurationRPCTest {

    private static CurationExperimentRPC curationRpc = new CurationExperimentRPCImpl();

    static {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        if (sessionFactory == null) {
            new HibernateSessionCreator();
        }
    }

    @Before
    public void setUp() {
        TestConfiguration.configure();
        TestConfiguration.initApplicationProperties();
    }

    @Test
    public void getExperimentsByFilter() {
        // dazed gene is necessary for late cell type development ...
        String pubID = "ZDB-PUB-050422-7";
        ExperimentDTO experimentFilter = new ExperimentDTO();
        experimentFilter.setPublicationID(pubID);
        List<ExperimentDTO> experiments = curationRpc.getExperimentsByFilter(experimentFilter);
        assertNotNull(experiments);
    }



}
package org.zfin.gwt.curation;

import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.gwt.curation.server.CurationExperimentRPCImpl;
import org.zfin.gwt.curation.ui.CurationExperimentRPC;
import org.zfin.gwt.root.dto.ExpressionExperimentDTO;

import java.util.List;

import static org.junit.Assert.assertNotNull;


public class CurationRPCTest extends AbstractDatabaseTest{

    private static CurationExperimentRPC curationRpc = new CurationExperimentRPCImpl();

    @Test
    public void getExperimentsByFilter() {
        // dazed gene is necessary for late cell type development ...
        String pubID = "ZDB-PUB-050422-7";
        ExpressionExperimentDTO experimentFilter = new ExpressionExperimentDTO();
        experimentFilter.setPublicationID(pubID);
        List<ExpressionExperimentDTO> experiments = curationRpc.getExperimentsByFilter(experimentFilter);
        assertNotNull(experiments);
    }



}

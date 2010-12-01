package org.zfin.util;

import org.junit.Before;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.TestConfiguration;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.mutant.MutantFigureStage;

import static junit.framework.Assert.assertNotNull;

/**
 * Test class for service class.
 */
public class BODtoConversionServiceTest extends AbstractDatabaseTest{


    @Before
    public void setUp() {
        TestConfiguration.setAuthenticatedUser();
    }

    @Test
    public void getPhenotypeFigureStageFromDto() {
        String startID = "ZDB-STAGE-010723-35";
        String endID = "ZDB-STAGE-010723-49";
        String genotypeID = "ZDB-GENO-030530-1";
        String figureID = "ZDB-FIG-041108-3";
        // Standard
        String envID = "ZDB-EXP-041102-1";
        String pubID = "ZDB-PUB-090731-2";

        StageDTO start = new StageDTO();
        start.setZdbID(startID);
        StageDTO end = new StageDTO();
        end.setZdbID(endID);
        GenotypeDTO geno = new GenotypeDTO();
        geno.setZdbID(genotypeID);
        FigureDTO fig = new FigureDTO();
        fig.setZdbID(figureID);
        PhenotypeFigureStageDTO pfs = new PhenotypeFigureStageDTO();
        pfs.setGenotype(geno);
        pfs.setStart(start);
        pfs.setEnd(end);
        pfs.setFigure(fig);
        EnvironmentDTO envDto = new EnvironmentDTO();
        envDto.setZdbID(envID);
        pfs.setEnvironment(envDto);
        pfs.setPublicationID(pubID);
        MutantFigureStage mfs = DTOConversionService.convertToMutantFigureStageFromDTO(pfs);
        assertNotNull(mfs);
        assertNotNull(mfs.getEnd());
        assertNotNull(mfs.getStart());
        assertNotNull(mfs.getFigure());
        assertNotNull(mfs.getGenotypeExperiment());
    }

}

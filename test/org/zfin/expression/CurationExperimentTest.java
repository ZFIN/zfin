package org.zfin.expression;

import org.junit.Test;
import org.zfin.gwt.curation.server.CurationExperimentRPCImpl;
import org.zfin.gwt.root.dto.ExpressionFigureStageDTO;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class CurationExperimentTest {

    @Test
    public void testFigureAnnotationSessionSetNewAddition() {
        CurationExperimentRPCImpl service = new CurationExperimentRPCImpl();
        boolean checked = true;
        long unique = 100;
        Set<Long> set = new HashSet<>();
        service.updateFigureAnnotationSessionSet(unique, set, checked);
        assertEquals(1, set.size());
    }

    @Test
    public void testFigureAnnotationSessionSetNewAdditionExist() {
        CurationExperimentRPCImpl service = new CurationExperimentRPCImpl();
        boolean checked = true;
        long unique = 100;
        Set<Long> set = new HashSet<>();
        service.updateFigureAnnotationSessionSet(unique, set, checked);
        assertEquals(1, set.size());
        // try to add it again
        service.updateFigureAnnotationSessionSet(unique, set, checked);
        assertEquals(1, set.size());
    }

    @Test
    public void testFigureAnnotationSessionSetRemove() {
        CurationExperimentRPCImpl service = new CurationExperimentRPCImpl();
        boolean checked = true;
        long unique = 100;
        Set<Long> set = new HashSet<>();
        service.updateFigureAnnotationSessionSet(unique, set, checked);
        assertEquals(1, set.size());
        // remove
        checked = false;
        service.updateFigureAnnotationSessionSet(unique, set, checked);
        assertEquals(0, set.size());
        // remove again
        service.updateFigureAnnotationSessionSet(unique, set, checked);
        assertEquals(0, set.size());
    }

    @Test
    public void testUniqueID() {
        String uniqueID = "ZDB-EXP-091009-1:ZDB-FIG-071009-1:ZDB-STAGE-091009-25:ZDB-STAGE-091009-443";
        ExpressionFigureStageDTO dto = new ExpressionFigureStageDTO();
        dto.setUniqueID(uniqueID);
        assertTrue(dto.getExperiment() != null);
        assertEquals("ZDB-EXP-091009-1", dto.getExperiment().getExperimentZdbID());
        assertEquals("ZDB-FIG-071009-1", dto.getFigure().getZdbID());
        assertEquals("ZDB-STAGE-091009-25", dto.getStart().getZdbID());
        assertEquals("ZDB-STAGE-091009-443", dto.getEnd().getZdbID());
    }

}
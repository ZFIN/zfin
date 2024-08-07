package org.zfin.expression.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.expression.presentation.MarkerExpression;
import org.zfin.framework.HibernateUtil;
import org.zfin.marker.Clone;
import org.zfin.marker.Marker;
import org.zfin.marker.presentation.LinkDisplay;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.ForeignDB;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.*;

/**
 *
 */
public class ExpressionServiceTest extends AbstractDatabaseTest {

    private final ExpressionService expressionService = new ExpressionService();


    @Test
    public void getEfgExpression() {
        // should have direct expression and withdrawn stuff
        // cb280
        Marker marker = RepositoryFactory.getMarkerRepository().getMarkerByID("ZDB-EFG-070117-3");
        MarkerExpression markerExpression = expressionService.getExpressionForEfg(marker);
        assertNotNull(markerExpression);
        assertThat(markerExpression.getTotalCountForStuff(), greaterThan(35));
        assertThat(markerExpression.getAllMarkerExpressionInstance().getPublicationCount(), greaterThan(20));
        assertNull(markerExpression.getGeoLink());
        assertThat(markerExpression.getAllMarkerExpressionInstance().getFigureCount(), greaterThan(35));
        assertNotNull(markerExpression.getDirectlySubmittedExpression());
        assertNull(markerExpression.getWildTypeStageExpression());
    }

    @Test
    public void getFishMiRnaExpressionLink() {
        Marker marker = RepositoryFactory.getMarkerRepository().getMarkerByID("ZDB-MIRNAG-091023-14");
        LinkDisplay markerExpression = expressionService.getFishMiRna(marker, ForeignDB.AvailableName.FISHMIRNA);
        assertNotNull(markerExpression);
    }

    @Test
    public void getEfgExpressionWithDirect() {
        // should have direct expression and withdrawn stuff
        // cb280
        Marker marker = RepositoryFactory.getMarkerRepository().getMarkerByID("ZDB-EFG-080131-2");
        MarkerExpression markerExpression = expressionService.getExpressionForEfg(marker);
        assertNotNull(markerExpression);
        assertThat(markerExpression.getTotalCountForStuff(), greaterThan(160));
        assertNotNull(markerExpression.getDirectlySubmittedExpression());
        assertThat(markerExpression.getDirectlySubmittedExpression().getFigureCount(), greaterThan(140));
        assertThat(markerExpression.getDirectlySubmittedExpression().getImageCount(), greaterThan(1000));
        assertThat(markerExpression.getDirectlySubmittedExpression().getFigureCount(), lessThan(2000));
        assertThat(markerExpression.getAllMarkerExpressionInstance().getPublicationCount(), greaterThan(10));
        assertThat(markerExpression.getAllMarkerExpressionInstance().getFigureCount(), greaterThan(160));
        assertNull(markerExpression.getWildTypeStageExpression());
    }


    public void testMicroarrayWebserviceJob() {

        MicroarrayWebServiceBean microarrayWebServiceBean = new MicroarrayWebServiceBean();
        Set<String> ids = new HashSet<>(3);
        ids.add("ZDB-BAC-041007-88");
        ids.add("ZDB-BAC-041007-119");
        microarrayWebServiceBean.setAddZdbIds(ids);
        HibernateUtil.createTransaction();
        try {
            expressionService.writeMicroarrayWebServiceBean(microarrayWebServiceBean);
        } catch (Exception e) {
            fail(e.toString());
        } finally {
            HibernateUtil.rollbackTransaction();
        }
        assertNotNull(microarrayWebServiceBean);
    }


    @Test
    public void isThisseProbe() {
        Clone c = RepositoryFactory.getMarkerRepository().getCloneById("ZDB-CDNA-080114-24");
        assertTrue(expressionService.isThisseProbe(c));
    }
}

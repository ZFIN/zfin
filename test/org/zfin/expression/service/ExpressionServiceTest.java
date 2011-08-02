package org.zfin.expression.service;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.expression.presentation.MarkerExpression;
import org.zfin.framework.HibernateUtil;
import org.zfin.marker.Clone;
import org.zfin.marker.Marker;
import org.zfin.repository.RepositoryFactory;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.*;

/**
 */
public class ExpressionServiceTest extends AbstractDatabaseTest {

    private Logger logger = Logger.getLogger(ExpressionServiceTest.class);

    private ExpressionService expressionService = new ExpressionService();


    @Test
    public void getEfgExpression() {
        // should have direct expression and withdrawn stuff
        // cb280
        Marker marker = RepositoryFactory.getMarkerRepository().getMarkerByID("ZDB-EFG-070117-3");
        MarkerExpression markerExpression = expressionService.getExpressionForEfg(marker);
        assertNotNull(markerExpression);
        assertThat(markerExpression.getTotalCountForStuff(), greaterThan(35));
        assertThat(markerExpression.getTotalCountForStuff(), lessThan(100));
        assertThat(markerExpression.getAllExpressionData().getPublicationCount(), greaterThan(20));
        assertThat(markerExpression.getAllExpressionData().getPublicationCount(), lessThan(50));
        assertNull(markerExpression.getGeoLink());
        assertThat(markerExpression.getAllExpressionData().getFigureCount(), greaterThan(35));
        assertThat(markerExpression.getAllExpressionData().getFigureCount(), lessThan(100));
        assertNotNull(markerExpression.getDirectlySubmittedExpression());
        assertNull(markerExpression.getWildTypeStageExpression());
    }

    @Test
    public void getEfgExpressionWithDirect() {
        // should have direct expression and withdrawn stuff
        // cb280
        Marker marker = RepositoryFactory.getMarkerRepository().getMarkerByID("ZDB-EFG-080131-2");
        MarkerExpression markerExpression = expressionService.getExpressionForEfg(marker);
        assertNotNull(markerExpression);
        assertThat(markerExpression.getTotalCountForStuff(), greaterThan(160));
        assertThat(markerExpression.getTotalCountForStuff(), lessThan(190));
        assertNotNull(markerExpression.getDirectlySubmittedExpression());
        assertThat(markerExpression.getDirectlySubmittedExpression().getFigureCount(), greaterThan(140));
        assertThat(markerExpression.getDirectlySubmittedExpression().getFigureCount(), lessThan(200));
        assertThat(markerExpression.getDirectlySubmittedExpression().getImageCount(), greaterThan(1000));
        assertThat(markerExpression.getDirectlySubmittedExpression().getFigureCount(), lessThan(2000));
        assertThat(markerExpression.getAllExpressionData().getPublicationCount(), greaterThan(10));
        assertThat(markerExpression.getAllExpressionData().getPublicationCount(), lessThan(30));
        assertThat(markerExpression.getAllExpressionData().getFigureCount(), greaterThan(160));
        assertThat(markerExpression.getAllExpressionData().getFigureCount(), lessThan(200));
        assertNull(markerExpression.getWildTypeStageExpression());
    }


    @Test
    public void testMicroarrayWebserviceJob() {

//        MicroarrayWebServiceBean microarrayWebServiceBean = expressionService.processMicroarrayRecordAttributionsForType(Marker.Type.GENEP,5);
        int numberMarkers = RepositoryFactory.getMarkerRepository().getMarkerZdbIdsForType(Marker.Type.GENE).size();
        long startTime = System.currentTimeMillis();
        MicroarrayWebServiceBean microarrayWebServiceBean = expressionService.processMicroarrayRecordAttributionsForType(Marker.Type.GENE, 20);
        HibernateUtil.createTransaction();
        try {
            expressionService.writeMicroarrayWebServiceBean(microarrayWebServiceBean);
        } catch (Exception e) {
            fail(e.toString());
        } finally {
            HibernateUtil.rollbackTransaction();
        }
        long endTime = System.currentTimeMillis();
        logger.info("total time: " + (endTime - startTime) + " in seconds " + ((endTime - startTime) / 1000f));
        logger.info("total records: " + 200);
        logger.info("time should be : " + (endTime - startTime) / (1000f * 200) + " s");
        assertNotNull(microarrayWebServiceBean);
    }

    // TODO: re-enable after the GEO job has been run once
    @Test
    public void getGeoLinkForMarker() {
        Marker m = RepositoryFactory.getMarkerRepository().getMarkerByID("ZDB-GENE-041008-244");
        assertNotNull(m);
        String linkString = expressionService.getGeoLinkForMarker(m);
        assertNotNull(linkString);
        m = RepositoryFactory.getMarkerRepository().getMarkerByID("ZDB-SSLP-000426-106");
        assertNotNull(m);
        assertNull(expressionService.getGeoLinkForMarker(m));

    }

    @Test
    public void findGeoLinkForNCBI(){
        Marker m ;
        m = RepositoryFactory.getMarkerRepository().getMarkerByID("ZDB-EST-010427-5"); // af086761
        assertNotNull(m);
        assertThat(expressionService.updateGeoLinkForMarker(m),greaterThan(-1));
        HibernateUtil.currentSession().flush();
        assertNotNull(expressionService.getGeoLinkForMarker(m));

        m = RepositoryFactory.getMarkerRepository().getMarkerByID("ZDB-GENE-010502-3"); // bing3y
        assertNotNull(m);
        assertThat(expressionService.updateGeoLinkForMarker(m),lessThan(1));
        HibernateUtil.currentSession().flush();
        assertNull(expressionService.getGeoLinkForMarker(m));

        m = RepositoryFactory.getMarkerRepository().getMarkerByID("ZDB-GENE-110207-1"); // agbl1
        assertNotNull(m);
        assertThat(expressionService.updateGeoLinkForMarker(m),greaterThan(-1));
        HibernateUtil.currentSession().flush();
        assertNotNull(expressionService.getGeoLinkForMarker(m));
    }


    @Test
    public void isThisseProbe(){
        Clone c = RepositoryFactory.getMarkerRepository().getCloneById("ZDB-CDNA-080114-24");
        assertTrue(expressionService.isThisseProbe(c));
    }
}

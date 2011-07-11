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
public class ExpressionServiceTest extends AbstractDatabaseTest{

    private Logger logger = Logger.getLogger(ExpressionServiceTest.class);

    private ExpressionService expressionService = new ExpressionService();


    @Test
    public void getEfgExpression(){
        // should have direct expression and withdrawn stuff
        // cb280
        Marker marker = RepositoryFactory.getMarkerRepository().getMarkerByID("ZDB-EFG-070117-3");
        MarkerExpression markerExpression = expressionService.getExpressionForEfg(marker);
        assertNotNull(markerExpression);
        assertThat(markerExpression.getTotalCountForStuff(),greaterThan(35));
        assertThat(markerExpression.getTotalCountForStuff(),lessThan(100));
        assertThat(markerExpression.getAllExpressionData().getPublicationCount(),greaterThan(20));
        assertThat(markerExpression.getAllExpressionData().getPublicationCount(),lessThan(50));
        assertNull(markerExpression.getGeoLink());
        assertThat(markerExpression.getAllExpressionData().getFigureCount(), greaterThan(35));
        assertThat(markerExpression.getAllExpressionData().getFigureCount(), lessThan(100));
        assertNull(markerExpression.getDirectlySubmittedExpression());
        assertNull(markerExpression.getWildTypeStageExpression());

    }

    @Test
    public void testMicroarrayWebserviceJob(){

//        MicroarrayWebServiceBean microarrayWebServiceBean = expressionService.processMicroarrayRecordAttributionsForType(Marker.Type.GENEP,5);
        int numberMarkers = RepositoryFactory.getMarkerRepository().getMarkerZdbIdsForType(Marker.Type.GENE).size();
        long startTime = System.currentTimeMillis();
        MicroarrayWebServiceBean microarrayWebServiceBean = expressionService.processMicroarrayRecordAttributionsForType(Marker.Type.GENE, 200);
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
        logger.info("total records: " + 200) ;
        logger.info("time should be : " + (endTime - startTime) / (1000f * 200) + " s");
        assertNotNull(microarrayWebServiceBean);
    }

    // TODO: re-enable after the GEO job has been run once
    @Test
    public void getGeoLinkForMarker(){
        Marker m = RepositoryFactory.getMarkerRepository().getMarkerByID("ZDB-GENE-041008-244");
        assertNotNull(m);
        String linkString = expressionService.getGeoLinkForMarker(m);
        assertNotNull(linkString);
        m = RepositoryFactory.getMarkerRepository().getMarkerByID("ZDB-SSLP-000426-106");
        assertNotNull(m);
        assertNull(expressionService.getGeoLinkForMarker(m));
    }
}

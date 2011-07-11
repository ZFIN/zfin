package org.zfin.datatransfer.microarray;

import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.expression.service.ExpressionService;
import org.zfin.framework.HibernateUtil;
import org.zfin.marker.Marker;
import org.zfin.repository.RepositoryFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 */
public class MicroarrayServiceTest extends AbstractDatabaseTest{

    private ExpressionService expressionService = new ExpressionService();
    // this is more for convenience and shouldn't be run as a regular test
//    @Test
//    public void testIndividualGPL() {
//        DefaultGeoSoftParser defaultSoftParser = new DefaultGeoSoftParser();
////        defaultSoftParser.setAlwaysUseExistingFile(true);
//        defaultSoftParser.parseUniqueNumbers("GPL1319", 2, new String[]{"Danio rerio"}, new String[]{"Control"});
//    }

    @Test
    public void updateGeoLink(){
        Marker m = RepositoryFactory.getMarkerRepository().getMarkerByID("ZDB-GENE-041008-244");
        HibernateUtil.createTransaction();
        try{
            assertEquals(0, expressionService.updateGeoLinkForMarker(m));
            RepositoryFactory.getInfrastructureRepository().removeRecordAttributionForData(m.getZdbID(), MicroarrayWebserviceJob.MICROARRAY_PUB);
            assertEquals(1, expressionService.updateGeoLinkForMarker(m));
//            m.setAbbreviation("elmerfudd");
//            assertEquals(-1, expressionService.updateGeoLinkForMarker(m));
        }
        catch(Exception e){
            fail(e.toString());
        }
        finally {
            HibernateUtil.rollbackTransaction();
        }
    }
}

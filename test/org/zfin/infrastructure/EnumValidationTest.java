package org.zfin.infrastructure ;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.framework.presentation.QuartzJobsBean;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**  Tests MarkerEnum
 *
 */
public class EnumValidationTest extends AbstractDatabaseTest {


    private static Logger logger = Logger.getLogger(EnumValidationTest.class);

    private EnumValidationService enumValidationService = new EnumValidationService(); 

    @Test
    public void validateAllServices(){
        try{
            enumValidationService.checkAllEnums();
        }
        catch(EnumValidationException eve){
            logger.fatal(eve);
            fail(eve.toString()) ;
        }
        String report = enumValidationService.getReport();
        assertEquals(null, report);
    }



    @Test
    public void getCollectionDifference(){
        List<String> namesOne = new ArrayList<String>();
        namesOne.add("Walter");
        namesOne.add("Heinrich");

        List<String> namesTwo = new ArrayList<String>();
        namesTwo.add("Walter");
        namesTwo.add("Heinrich");

        String errorReport = EnumValidationService.getCollectionDifferenceReport(namesOne, namesTwo, String.class);
        assertNull(errorReport );

        namesTwo.add("Ingrid");
        errorReport = EnumValidationService.getCollectionDifferenceReport(namesOne, namesTwo, String.class);
        assertNotNull(errorReport );

    }

    @Test
    public void correctBoolean(){
        assertTrue(QuartzJobsBean.Action.RESUME.isIndividualAction()) ;
        assertTrue(QuartzJobsBean.Action.PAUSE.isIndividualAction()) ;
        assertTrue(QuartzJobsBean.Action.RUN.isIndividualAction()) ;
        assertFalse(QuartzJobsBean.Action.PAUSE_ALL.isIndividualAction()) ;
        assertFalse(QuartzJobsBean.Action.RESUME_ALL.isIndividualAction()) ;

        assertFalse(QuartzJobsBean.Action.RESUME.isPauseAction()) ;
        assertFalse(QuartzJobsBean.Action.RUN.isPauseAction()) ;
        assertTrue(QuartzJobsBean.Action.PAUSE.isPauseAction()) ;
        assertTrue(QuartzJobsBean.Action.PAUSE_ALL.isPauseAction()) ;
        assertFalse(QuartzJobsBean.Action.RESUME_ALL.isPauseAction()) ; 
    }

}

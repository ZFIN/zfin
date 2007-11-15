package org.zfin;

import org.apache.log4j.Logger;
import org.apache.commons.collections.CollectionUtils;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.TestConfiguration;
import org.zfin.infrastructure.EnumValidationService;
import org.zfin.infrastructure.EnumValidationException;
import org.hibernate.SessionFactory;
import org.junit.*;
import static org.junit.Assert.* ;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

/**  Tests MarkerEnum
 *
 */
public class EnumValidationTest {
    private static Logger logger = Logger.getLogger(EnumValidationTest.class);

    private EnumValidationService enumValidationService = new EnumValidationService(); 

    static{
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory() ;
        if(sessionFactory == null){
            new HibernateSessionCreator(false, TestConfiguration.getHibernateConfiguration());
        }
    }

    @Before
    public void setUp() {
        TestConfiguration.configure();
    }

    @After
    public void closeSession() {
        HibernateUtil.closeSession();
    }

    @Test
    public void validateAllServices(){
        try{
            enumValidationService.validateMarkerTypes();
        }
        catch(EnumValidationException eve){
            logger.fatal(eve);
            fail(eve.toString()) ;
        }
    }



    @Test
    public void getCollectionDifference(){
        List<String> namesOne = new ArrayList<String>();
        namesOne.add("Walter");
        namesOne.add("Heinrich");

        List<String> namesTwo = new ArrayList<String>();
        namesTwo.add("Walter");
        namesTwo.add("Heinrich");

        String errorReport = EnumValidationService.getCollectionDifferenceReport(namesOne, namesTwo);
        assertNull(errorReport );

        namesTwo.add("Ingrid");
        errorReport = EnumValidationService.getCollectionDifferenceReport(namesOne, namesTwo);
        assertNotNull(errorReport );
        assertTrue( errorReport.startsWith("Target collection has unmatched String: Ingrid"));



    }

}

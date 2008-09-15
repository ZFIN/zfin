package org.zfin.infrastructure ;

import org.apache.log4j.Logger;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.TestConfiguration;
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

}

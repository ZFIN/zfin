package org.zfin.framework.presentation;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.zfin.TestConfiguration;
import org.zfin.properties.ZfinPropertiesEnum;

import static org.junit.Assert.assertEquals;

/**
 */
public class EntityPresentationTest {

    private Logger logger = Logger.getLogger(EntityPresentationTest.class) ;

    @Before
    public void setup(){
        TestConfiguration.configure();
    }

    @Test
    public void testJumpToLink(){
        String zdbId = "ZDB-GENE-040718-186" ;
        String assertionString = "http://"+ ZfinPropertiesEnum.DOMAIN_NAME+"/"+
                ZfinPropertiesEnum.WEBDRIVER_LOC.value() +
                "/" + "ZFIN_jump?record=" + zdbId ;
        logger.debug(assertionString);

        assertEquals(assertionString,EntityPresentation.getJumpToLink(zdbId)) ;
    }

}

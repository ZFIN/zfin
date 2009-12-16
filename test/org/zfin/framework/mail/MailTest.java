package org.zfin.framework.mail;

import junit.framework.JUnit4TestAdapter;
import org.junit.Test;
import org.zfin.TestConfiguration;
import org.zfin.properties.ApplicationProperties;
import org.zfin.properties.Path;
import org.zfin.properties.ZfinProperties;
import org.zfin.properties.impl.ApplicationPropertiesImpl;
import org.zfin.properties.impl.PathImpl;

import static org.junit.Assert.assertTrue;

/**
 * Tests the MailSender classes.
 */
public class MailTest {

//    public static final String APP_SETUP_REPOSITORY_DIRECTORY = "home/WEB-INF";
//    public static final String APP_SETUP_MASTER_FILE = "zfin-properties.xml";

    public static final String APP_SETUP_REPOSITORY_DIRECTORY = "test";
    public static final String APP_SETUP_MASTER_FILE = "zfin-properties-test.xml";


//    @Before
//    public void setUp() throws Exception {
//        TestConfiguration.configure();
//        TestConfiguration.initApplicationProperties();
//    }

    public void initRealApplicationProperties(){
        ZfinProperties.init(APP_SETUP_REPOSITORY_DIRECTORY, APP_SETUP_MASTER_FILE);
        ApplicationProperties properties = new ApplicationPropertiesImpl();
        Path path = new PathImpl();
        path.setWebdriver("cgi-bin/webdriver");
        properties.setPath(path);
        ZfinProperties.init(properties);
    }

    @Test
    public void sending(){
        TestConfiguration.configure();
        initRealApplicationProperties() ;
        TestConfiguration.initApplicationProperties();
        IntegratedJavaMailSender mailSender = new IntegratedJavaMailSender() ;
        assertTrue(mailSender.sendMail("test subject","test message",new String[]{"ndunn@uoregon.edu"})) ;                
    }

    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(MailTest.class);
    }
}

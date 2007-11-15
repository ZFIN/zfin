package org.zfin;

import org.apache.log4j.xml.DOMConfigurator;
import org.zfin.properties.ZfinProperties;
import org.zfin.properties.ApplicationProperties;
import org.zfin.properties.Path;
import org.zfin.properties.impl.ApplicationPropertiesImpl;
import org.zfin.properties.impl.PathImpl;
import org.zfin.people.Person;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextImpl;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.AuthenticationManager;
import org.acegisecurity.MockAuthenticationManager;
import org.acegisecurity.Authentication;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;

import java.io.File;

/**
 * This class sets up a test environment:
 * - Log4j
 */
public class TestConfiguration {

    public static final String APP_SETUP_REPOSITORY_DIRECTORY = "test";
    public static final String APP_SETUP_MASTER_FILE = "zfin-properties-test.xml";

    public static void configure() {
        File file = new File("test", "log4j.xml");
        DOMConfigurator.configure(file.getAbsolutePath());
    }

    public static String[] getHibernateConfiguration() {
        return new String[]{
                "reno.hbm.xml",
                "anatomy.hbm.xml",
                "people.hbm.xml",
                "blast.hbm.xml",
                "marker.hbm.xml",
                "expression.hbm.xml",
                "sequence.hbm.xml",
                "publication.hbm.xml",
                "orthology.hbm.xml",
                "mutant.hbm.xml",
                "infrastructure.hbm.xml",
                "mapping.hbm.xml"
        };
    }

    /**
     * Creates a default ApplicationProperties object.
     */
    public static void initApplicationProperties() {
//        ZfinProperties.init(APP_SETUP_REPOSITORY_DIRECTORY, APP_SETUP_MASTER_FILE);
        ApplicationProperties properties = new ApplicationPropertiesImpl();
        Path path = new PathImpl();
        path.setWebdriver("cgi-bin/webdriver");
        properties.setPath(path);
        ZfinProperties.init(properties);

    }

    public static void setAuthenticatedUser() {
        SecurityContext security = new SecurityContextImpl();
        AuthenticationManager manager = new MockAuthenticationManager(true);
        Person person = createNonSecurityPerson();
        Authentication authentication = new UsernamePasswordAuthenticationToken(person, null);
        manager.authenticate(authentication);
        security.setAuthentication(authentication);
        SecurityContextHolder.setContext(security);
    }

    private static Person createNonSecurityPerson() {
        Person user = new Person();
        user.setZdbID("ZDB-PERS-990101-01");
        user.setName("Authenticated User");
        return user;
    }


    public static void unsetAuthenticatedUser() {
        if (SecurityContextHolder.getContext() != null)
            SecurityContextHolder.setContext(null);
    }
}

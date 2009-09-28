package org.zfin;

import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationManager;
import org.acegisecurity.MockAuthenticationManager;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.context.SecurityContextImpl;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.apache.log4j.xml.DOMConfigurator;
import org.zfin.people.Person;
import org.zfin.people.repository.ProfileRepository;
import org.zfin.properties.ZfinProperties;
import org.zfin.repository.RepositoryFactory;

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
                "filters.hbm.xml",
                "antibody.hbm.xml",
                "anatomy.hbm.xml",
                "blast.hbm.xml",
                "expression.hbm.xml",
                "general.hbm.xml",
                "infrastructure.hbm.xml",
                "mapping.hbm.xml",
                "marker.hbm.xml",
                "mutant.hbm.xml",
                "orthology.hbm.xml",
                "people.hbm.xml",
                "publication.hbm.xml",
                "reno.hbm.xml",
                "sequence.hbm.xml",
        };
    }

    /**
     * Creates a default ApplicationProperties object.
     */
    public static void initApplicationProperties() {
        ZfinProperties.init(APP_SETUP_REPOSITORY_DIRECTORY, APP_SETUP_MASTER_FILE);
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
        user.setZdbID("ZDB-PERS-060413-1");
        user.setName("Authenticated User");
        return user;
    }

    public static Person getPerson(){
        ProfileRepository pr = RepositoryFactory.getProfileRepository();
        return pr.getPerson("ZDB-PERS-000103-2");
    }


    public static void unsetAuthenticatedUser() {
        if (SecurityContextHolder.getContext() != null)
            SecurityContextHolder.setContext(null);
    }
}

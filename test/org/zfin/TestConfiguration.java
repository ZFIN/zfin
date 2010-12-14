package org.zfin;

import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.zfin.people.Person;
import org.zfin.people.repository.ProfileRepository;
import org.zfin.properties.ZfinProperties;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.repository.RepositoryFactory;
import org.zfin.security.MockAuthenticationManager;

import java.io.File;

/**
 * This class sets up a test environment:
 * - Log4j
 */
public class TestConfiguration {

    public static void configure() {
        // setup log file
        File file = new File("test", "log4j.xml");
        DOMConfigurator.configure(file.getAbsolutePath());

        // set tomcat temp directory
        ZfinProperties.init();
        System.setProperty("java.io.tmpdir", ZfinPropertiesEnum.CATALINA_BASE.value()+"/temp") ;
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

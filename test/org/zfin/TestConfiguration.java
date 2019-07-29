package org.zfin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.zfin.profile.AccountInfo;
import org.zfin.profile.Person;
import org.zfin.profile.repository.ProfileRepository;
import org.zfin.properties.ZfinProperties;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.repository.RepositoryFactory;
import org.zfin.security.MockAuthenticationManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * This class sets up a test environment:
 * - Log4j
 */
public class TestConfiguration {

    public static void configure() {
        // setup log file
        File file = new File("test", "log4j2.xml");
        LoggerContext context = (org.apache.logging.log4j.core.LoggerContext) LogManager.getContext(false);
        context.setConfigLocation(file.toURI());

        // set tomcat temp directory
        ZfinProperties.init();
        File tempDir = new File("/tmp/");
        if (tempDir.canWrite()) {
            tempDir = new File(tempDir, ZfinPropertiesEnum.MUTANT_NAME.value());
            if (!tempDir.exists()) {
                tempDir.mkdir();
            }
            tempDir = new File(tempDir, "unit-test");
            if (!tempDir.exists()) {
                tempDir.mkdir();
            }
        }
        if (tempDir.canWrite()) {
            System.setProperty("java.io.tmpdir", tempDir.getAbsolutePath());
        } else {
            System.setProperty("java.io.tmpdir", ZfinPropertiesEnum.CATALINA_BASE.value() + "/temp");
        }
    }

    public static void setAuthenticatedUser(Person person) {
        SecurityContext security = new SecurityContextImpl();
        AuthenticationManager manager = new MockAuthenticationManager(true);
        Authentication authentication = new UsernamePasswordAuthenticationToken(person, null);
        manager.authenticate(authentication);
        security.setAuthentication(authentication);
        SecurityContextHolder.setContext(security);
    }

    public static void setAuthenticatedUser() {
        setAuthenticatedUser(createNonSecurityPerson());
    }

    public static void setAuthenticatedRootUser() {
        SecurityContext security = new SecurityContextImpl();
        AuthenticationManager manager = new MockAuthenticationManager(true);
        Person person = createSecurityPerson();
        Authentication authentication = new UsernamePasswordAuthenticationToken(person, null);
        manager.authenticate(authentication);
        security.setAuthentication(authentication);
        SecurityContextHolder.setContext(security);
    }

    private static Person createNonSecurityPerson() {
        Person user = new Person();
        user.setZdbID("ZDB-PERS-060413-1");
        user.setShortName("Authenticated User");
        return user;
    }

    private static Person createSecurityPerson() {
        Person user = new Person();
        user.setZdbID("ZDB-PERS-060413-1");
        user.setShortName("Authenticated Root User");
        AccountInfo info = new AccountInfo();
        info.setRole(AccountInfo.Role.ROOT.toString());
        user.setAccountInfo(info);
        return user;
    }

    public static Person getPerson() {
        ProfileRepository pr = RepositoryFactory.getProfileRepository();
        return pr.getPerson("ZDB-PERS-000103-2");
    }


    public static void unsetAuthenticatedUser() {
        if (SecurityContextHolder.getContext() != null) {
            SecurityContextHolder.setContext(null);
        }
    }
}

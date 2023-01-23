package org.zfin;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;

@Configuration
@ComponentScan(basePackages = {"org.zfin"})
@ImportResource("classpath:WEB-INF/spring/security.xml") //see: https://stackoverflow.com/a/27979838
public class AppConfig {

    @Bean
    public SessionRegistry getSessionRegistry(){
        return new SessionRegistryImpl();
    }
}

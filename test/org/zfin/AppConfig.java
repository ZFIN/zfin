package org.zfin;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;

@Configuration
@ComponentScan(basePackages = {"org.zfin"})
public class AppConfig {

    @Bean
    public SessionRegistry getSessionRegistry(){
        return new SessionRegistryImpl();
    }
}

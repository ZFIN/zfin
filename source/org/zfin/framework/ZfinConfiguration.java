package org.zfin.framework;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

@Configuration
@ComponentScan({"org.zfin"})
//@ContextConfiguration()
// currently only used for unit tests
public class ZfinConfiguration {

    public ZfinConfiguration() {
        String name ="";
    }
}

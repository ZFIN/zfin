package org.zfin.framework;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Map;

/**
 */
@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = {"classpath:**/applicationContext.xml"})
//@ContextConfiguration(locations = {"/test-applicationContext.xml"})
@ContextConfiguration(locations = {"classpath:**/security.xml"})
public class SpringTest extends AbstractJUnit4SpringContextTests {


//    @Autowired
    private SchedulerFactoryBean scheduler;

    @Before
    public void setupContext(){
//        scheduler = (SchedulerFactoryBean) applicationContext.getBean("scheduler") ;

    }

    @Test
    public void seeIfItWorks(){
        System.out.println("INSTANCE: " + System.getenv("INSTANCE")) ;
        Map<String,SchedulerFactoryBean> beans = applicationContext.getBeansOfType(SchedulerFactoryBean.class) ;
        System.out.println("# of beans: "+beans.size()) ;
        for(String name: beans.keySet()){
            System.out.println(name ) ;
        }
//        assertEquals(scheduler.isAutoStartup(), Boolean.valueOf(ZfinPropertiesEnum.RUN_QUARTZ_JOBS.value()));
    }
    
}

package org.zfin.properties.presentation;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.zfin.framework.TomcatStartupException;
import org.zfin.properties.ZfinProperties;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.properties.ZfinPropertiesLoadListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 */
public class ZfinPropertiesPlaceholderConfigurer extends PropertyPlaceholderConfigurer {

    private Logger logger = Logger.getLogger(ZfinPropertiesPlaceholderConfigurer.class) ;
    private String webRoot ;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        webRoot = ZfinPropertiesLoadListener.getWebRoot() ;
        try {
            initProperties();
        } catch (TomcatStartupException e) {
            throw new RuntimeException("TomcatStartupException caught, Stopping server",e) ;
        }
        super.postProcessBeanFactory(beanFactory);   
    }

    /**
     * Initialize the Zfin Properties by reading the property file and
     * making the parameters available.
     * @throws TomcatStartupException Notifies tomcat that it should not startup.
     */
    public void initProperties() throws TomcatStartupException {
        String instance = System.getenv("INSTANCE") ;
        if(instance==null){
            throw new TomcatStartupException("INSTANCE not defined in environment") ;
        }

        String propertiesFileString = webRoot + "/WEB-INF/zfin.properties" ;
        File propertiesFile = new File(propertiesFileString) ;
        String absolutePath = propertiesFile.getAbsolutePath();
        logger.info(absolutePath);
        if(false==propertiesFile.exists()){
            throw new TomcatStartupException("Property file: " +propertiesFile.getAbsolutePath() +
                    " not found for INSTANCE: "+ instance) ;
        }
        ZfinProperties.init(propertiesFileString);
        ZfinProperties.validateProperties() ;
    }
}

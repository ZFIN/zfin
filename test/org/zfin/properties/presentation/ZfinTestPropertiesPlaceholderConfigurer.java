package org.zfin.properties.presentation;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.zfin.framework.TomcatStartupException;
import org.zfin.properties.ZfinProperties;
import org.zfin.properties.ZfinPropertiesEnum;

import java.io.File;
import java.util.Properties;

/**
 */
public class ZfinTestPropertiesPlaceholderConfigurer extends PropertyPlaceholderConfigurer {

    private Logger logger = LogManager.getLogger(ZfinTestPropertiesPlaceholderConfigurer.class);

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        try {
            initProperties();

            // we do it this way instead of using system properties (though we could do that, as well)
            // this directly assess the spring properties
            Properties properties = new Properties();
            for (ZfinPropertiesEnum zfinPropertiesEnum : ZfinPropertiesEnum.values()) {
                properties.put(zfinPropertiesEnum.name(), zfinPropertiesEnum.value());
            }
            processProperties(beanFactory, properties);
        } catch (Exception e) {
            throw new RuntimeException("Exception caught", e);
        }
    }

    /**
     * Initialize the Zfin Properties by reading the property file and
     * making the parameters available.
     */
    public void initProperties() {
        String instance = ZfinPropertiesEnum.INSTANCE.value();
        if (instance == null) {
            throw new RuntimeException("INSTANCE not defined in environment");
        }

        String propertiesFileString = "home/WEB-INF/zfin.properties";
        File propertiesFile = new File(propertiesFileString);
        String absolutePath = propertiesFile.getAbsolutePath();
        logger.info(absolutePath);
        if (!propertiesFile.exists()) {
            throw new RuntimeException("Property file: " + propertiesFile.getAbsolutePath() +
                    " not found for INSTANCE: " + instance);
        }
        ZfinProperties.init(propertiesFileString);
        ZfinProperties.validateProperties();
    }
}

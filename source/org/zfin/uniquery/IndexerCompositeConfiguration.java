package org.zfin.uniquery;

import org.apache.commons.configuration.CompositeConfiguration;
import org.springframework.stereotype.Service;

@Service
public class IndexerCompositeConfiguration extends CompositeConfiguration {

    private String propertyFile;

    public void setPropertyFile(String propertyFile) {
        this.propertyFile = propertyFile;
    }

    /*
    public IndexerCompositeConfiguration(String propertyFile){
        try {
            addConfiguration(new PropertiesConfiguration(propertyFile));
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
    }
*/
}

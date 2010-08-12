package org.zfin.properties;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.zfin.TestConfiguration;

import static org.junit.Assert.*;

/**
 * Test if the property file can be read.
 */
public abstract class AbstractZfinPropertiesTest {

    public static final String TEST_PROPERTIES_FILE = "./externals/properties/test-properties.properties";
    private String currentPropertyFile ;

    /**
     * We init to get the mutant, but we re-init in order to evaluate our "test" case.
     */
    @Before
    public void setUp() throws Exception {
        currentPropertyFile = ZfinProperties.getCurrentPropertyFile() ;
        TestConfiguration.configure();
        ZfinProperties.init(TEST_PROPERTIES_FILE) ;
    }

    /**
     * This cleans up the property file to the old one.
     */
    @After
    public void cleanup(){
        ZfinProperties.init(currentPropertyFile);
    }

}

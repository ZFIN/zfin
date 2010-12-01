package org.zfin.util;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.util.Iterator;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * Test class for Configuration class.
 */
public class ZfinConfigurationTest {

    private static Configuration defaultConfiguration = null;
    private static Configuration implConfiguration = null;
    private static final String ENV = "env.";
    private FileWriter environmentFileWriter;

    @Before
    public void setup() throws Exception {
        defaultConfiguration = new PropertiesConfiguration("test-default.properties");
        implConfiguration = new PropertiesConfiguration("test.properties");
        openEnvironmentFile();
    }

    @After
    public void tearDown() throws Exception{
       closeEnvironmentFile();
    }

    /**
     * Find regular configuration variable.
     */
    @Test
    public void getConfigurationVariables() {
        String domain = defaultConfiguration.getString("DOMAIN_NAME");
        assertNotNull(domain);
        assertEquals("frost.zfin.org", domain);
    }

    /**
     * Find regular configuration variable.
     */
    @Test
    public void getSystemVariable() {
        String domain = defaultConfiguration.getString("user-dir");
        assertNotNull(domain);
        String path = defaultConfiguration.getString("constantVar");
        assertNotNull(path);
        path = defaultConfiguration.getString("userPath");
        assertNotNull(path);
    }

    /**
     * Find override default values.
     */
    @Test
    public void getNonOverrideValue() {
        CompositeConfiguration cc = new CompositeConfiguration();
        cc.addConfiguration(defaultConfiguration);
        String domain = cc.getString("DOMAIN_NAME");
        assertNotNull(domain);
        cc.addConfiguration(implConfiguration);
        String defaultDomain = cc.getString("DOMAIN_NAME");
        assertNotNull(domain.equals(defaultDomain));
    }

    /**
     * Find override default values.
     */
    @Test
    public void getOverrideValue() {
        CompositeConfiguration cc = new CompositeConfiguration();
        cc.addConfiguration(implConfiguration);
        cc.addConfiguration(defaultConfiguration);
        String domain = cc.getString("DOMAIN_NAME");
        assertNotNull(domain);
    }

    /**
     * Find override default values.
     */
    @Test
    public void getComposedValue() {
        CompositeConfiguration cc = new CompositeConfiguration();
        cc.addConfiguration(implConfiguration);
        cc.addConfiguration(defaultConfiguration);
        String fullName = cc.getString("fullName");
        assertNotNull(fullName);
    }

    /**
     * Find override default values.
     */
    @Test
    public void getComposedEmailValue() {
        CompositeConfiguration cc = new CompositeConfiguration();
        cc.addConfiguration(implConfiguration);
        cc.addConfiguration(defaultConfiguration);
        String emailAddress = cc.getString("emailAddressPrefix");
        assertNotNull(emailAddress);
        String domainName = cc.getString("env.DOMAIN_NAME");
        assertNotNull(domainName);
    }

    @Test
    public void createEnvironmentFile() throws Exception{
        CompositeConfiguration cc = new CompositeConfiguration();
        cc.addConfiguration(implConfiguration);
        cc.addConfiguration(defaultConfiguration);
        final Iterator<String> iter = cc.getKeys();
        while (iter.hasNext()) {
            String key = iter.next();
            if (key.toLowerCase().startsWith(ENV.toLowerCase())) {
                String environmentVariable = key.substring(ENV.length());
                StringBuffer sb = new StringBuffer("setenv ");
                sb.append(environmentVariable);
                sb.append(" ");
                sb.append(cc.getString(key));
                writeToEnvironmentFile(sb.toString());
            }
        }
    }

    private void openEnvironmentFile() throws Exception {
        File file = new File("environment.sh");
        environmentFileWriter = new FileWriter(file);
    }

    private void closeEnvironmentFile()throws Exception {
        environmentFileWriter.close();
    }

    private void writeToEnvironmentFile(String environmentLine) throws Exception{
        environmentFileWriter.write(environmentLine);
        environmentFileWriter.write(System.getProperty("line.separator"));
    }
}

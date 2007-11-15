package org.zfin;

import org.hibernate.cfg.Configuration;
import org.zfin.framework.HibernateUtil;
import org.zfin.util.FileUtil;
import org.zfin.properties.ZfinProperties;
import org.zfin.anatomy.SetupAnatomyOntology;
import junit.framework.TestCase;

/**
 * This class sets up a test database (Hypersonic) for unit testing.
 */
public abstract class BaseDatabaseUnitTest extends TestCase {

    private static boolean firstTime = true;

    protected void setUp() throws Exception {

    }

    public BaseDatabaseUnitTest() {
        if (firstTime)
            init();
        firstTime = false;
    }

    public void init() {
        Configuration config = new Configuration();
        config.setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
        config.setProperty("hibernate.connection.driver_class", "org.hsqldb.jdbcDriver");
        config.setProperty("hibernate.connection.url", "jdbc:hsqldb:hsql://localhost/xdb");
        config.setProperty("hibernate.connection.username", "sa");
        config.setProperty("hibernate.connection.password", "");
        config.setProperty("hibernate.connection.pool_size", "1");
        config.setProperty("hibernate.connection.autocommit", "true");
        config.setProperty("hibernate.cache.provider_class", "org.hibernate.cache.HashtableCacheProvider");
        config.setProperty("hibernate.hbm2ddl.auto", "create-drop");
        config.setProperty("hibernate.show_sql", "true");
        String absolutePath = FileUtil.createAbsolutePath(ZfinProperties.CONFIGURATION_DIRECTORY, "anatomy.hbm.xml");
        config.addFile(absolutePath);
        HibernateUtil.init(config.buildSessionFactory());

        // read the ao obo file with test terms
        String fileName = "zfin-ao-test.obo";
        SetupAnatomyOntology.setupAO(fileName);

    }

}

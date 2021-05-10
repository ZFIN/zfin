package org.zfin.sequence.blast;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zfin.TestConfiguration;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.properties.ZfinPropertiesEnum;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.fail;

public class BlastFilesystemTests {
    private final Logger logger = LogManager.getLogger(BlastFilesystemTests.class);

    static {
        TestConfiguration.configure();
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

        if (sessionFactory == null) {
            new HibernateSessionCreator();
        }
    }


    @Before
    public void setUp() {
    }

    @After
    public void closeSession() {
        HibernateUtil.closeSession();
    }



}

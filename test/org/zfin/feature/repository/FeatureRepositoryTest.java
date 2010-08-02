package org.zfin.feature.repository;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.zfin.TestConfiguration;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.mutant.Feature;
import org.zfin.repository.RepositoryFactory;

import java.util.List;

import static junit.framework.Assert.assertNotNull;

public class FeatureRepositoryTest {

    private static FeatureRepository featureRepository = RepositoryFactory.getFeatureRepository();

    static {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        if (sessionFactory == null) {
            new HibernateSessionCreator();
        }
    }

    @Before
    public void setUp() {
        TestConfiguration.configure();
    }


    /**
     * Check that genotype anh^m149 has background AB.
     */
    @Test
    public void getFeatureForPublication() {
        //  publication: Abdelilah
        String pubID = "ZDB-PUB-970210-18";
        List<Feature> features = featureRepository.getFeaturesByPublication(pubID);

        assertNotNull("feature list exists", features);

    }


}
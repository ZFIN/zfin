package org.zfin.feature.repository;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.zfin.TestConfiguration;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.AnatomyPhenotype;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.anatomy.presentation.AnatomySearchBean;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.expression.ExpressionExperiment;
import org.zfin.expression.Figure;
import org.zfin.expression.repository.ExpressionRepository;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.mutant.*;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.ontology.GoTerm;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.fail;

public class FeatureRepositoryTest {

    private static FeatureRepository featureRepository = RepositoryFactory.getFeatureRepository();

    static {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        if (sessionFactory == null) {
            new HibernateSessionCreator(TestConfiguration.getHibernateConfiguration());
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
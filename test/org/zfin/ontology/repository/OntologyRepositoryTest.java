package org.zfin.ontology.repository;

import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.zfin.TestConfiguration;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.ontology.*;
import org.zfin.properties.ZfinProperties;
import org.zfin.util.FileUtil;

import static org.zfin.repository.RepositoryFactory.*;

import java.io.File;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * Repository for Ontology-related actions: mostly lookup.
 */
public class OntologyRepositoryTest {

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

    @Test
    public void getMatchingQualityTerms() {
        String query = "red";
        List<MatchingTerm> qualities = OntologyManager.getInstance().getMatchingTerms(Ontology.QUALITY, query);
        assertNotNull(qualities);
        assertEquals(23, qualities.size());
    }

    //@Test
    public void getMatchingAnatomyTerms() {
        String query = "mel";
        List<MatchingTerm> anatomyList = OntologyManager.getInstance().getMatchingTerms(Ontology.ANATOMY, query);
        assertNotNull(anatomyList);
        assertEquals(21, anatomyList.size());
    }

    @Test
    public void getMatchingAliasAnatomyTerms() {
        List<TermAlias> anatomyList = getOntologyRepository().getAllAliases(Ontology.ANATOMY);
        assertNotNull(anatomyList);
    }

    @Test
    public void getAnatomyRootTermInfo() {
        String anatomyRootID = "ZFA:0000037";
        Term term = getOntologyRepository().getTermByOboID(anatomyRootID);
        Assert.assertNotNull(term);
    }


}
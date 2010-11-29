package org.zfin.ontology.repository;

import org.hibernate.SessionFactory;
import org.junit.Test;
import org.zfin.TestConfiguration;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.mutant.Phenotype;
import org.zfin.ontology.*;

import java.util.List;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.zfin.repository.RepositoryFactory.getOntologyRepository;

/**
 * Repository for Ontology-related actions: mostly lookup.
 */
public class OntologyRepositoryTest extends AbstractOntologyTest{

    static {
        TestConfiguration.configure();
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        if (sessionFactory == null) {
            new HibernateSessionCreator();
        }
    }

    @Test
    public void getMatchingQualityTerms() {
        String query = "red";
        MatchingTermService matcher = new MatchingTermService();
        Set<MatchingTerm> qualities = matcher.getMatchingTerms(Ontology.QUALITY, query);
        assertNotNull(qualities);
        assertEquals(14, qualities.size());

        int count = 0 ;
        for(MatchingTerm matchingTerm : qualities){
            count += (matchingTerm.getTerm().isObsolete() ? 1 : 0) ;
        }
        assertEquals(4, count);

    }

    //@Test

    public void getMatchingAnatomyTerms() {
        String query = "mel";
        MatchingTermService matcher = new MatchingTermService();
        Set<MatchingTerm> anatomyList = matcher.getMatchingTerms(Ontology.ANATOMY, query);
        assertNotNull(anatomyList);
        assertEquals(21, anatomyList.size());
    }

    @Test
    public void getMatchingAliasAnatomyTerms() {
        List<TermAlias> anatomyList = getOntologyRepository().getAllAliases(Ontology.ANATOMY);
        assertNotNull(anatomyList);
    }

    @Test
    public void getTermByName() {
        String anatomyTermName = "forerunner cell group";
        Term term = getOntologyRepository().getTermByName(anatomyTermName, Ontology.ANATOMY);
        assertNotNull(term);
    }

    @Test
    public void getTransitiveClosure() {
        List<TransitiveClosure> tcs = getOntologyRepository().getTransitiveClosure();
        assertNotNull(tcs);
    }


    @Test
    public void getAnatomyRootTermInfo() {
        String anatomyRootID = "ZFA:0000037";
        Term term = getOntologyRepository().getTermByOboID(anatomyRootID);
        assertNotNull(term);
    }

    @Test
    public void loadAllTermsFromFiles() throws Exception{
        OntologyManager manager = OntologyManager.getInstanceFromFile(Ontology.ANATOMY);
        assertNotNull(manager);
    }

    @Test
    public void loadAllTermsOfOntology() throws Exception{
        List<Term> terms = getOntologyRepository().getAllTermsFromOntology(Ontology.QUALITY);
        assertNotNull(terms);
    }

    @Test
    public void loadOntologyMetatdataForAll() throws Exception{
        List<OntologyMetadata> metadata = getOntologyRepository().getAllOntologyMetadata();
        assertNotNull(metadata);
        assertEquals("ontology name", "sequence ontology", metadata.get(0).getName());
        assertEquals("ontology name", "zebrafish_anatomy", metadata.get(1).getName());
    }

    @Test
    public void loadOntologyMetatdataForQuality() throws Exception{
        OntologyMetadata metadata = getOntologyRepository().getOntologyMetadata(Ontology.QUALITY.getOntologyName());
        assertNotNull(metadata);
        assertEquals("Default name space", "quality", metadata.getDefaultNamespace());
    }

    @Test
    public void getPhenotypesWithSecondaryTerms() throws Exception{
        List<Phenotype> phenotypesWithSecondaryTerms= getOntologyRepository().getPhenotypesWithSecondaryTerms();
        assertNotNull(phenotypesWithSecondaryTerms);
    }

    @Override
    protected Ontology[] getOntologiesToLoad() {
        Ontology[] ontologies = new Ontology[2];
        ontologies[0] =  Ontology.ANATOMY;
        ontologies[1] =  Ontology.QUALITY;
        return ontologies ;
    }

}

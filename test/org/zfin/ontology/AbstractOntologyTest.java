package org.zfin.ontology;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.zfin.TestConfiguration;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;

import java.util.Iterator;
import java.util.Set;

/**
 */
public abstract class AbstractOntologyTest {

    protected final static Ontology ontology = Ontology.GO_CC;
    protected static OntologyManager ontologyManager ;
    private static final Logger logger = Logger.getLogger(AbstractOntologyTest.class);
    private static String oldTempDirectory ;
    private static String testTempDirectory = "test/ontologies";
    protected final static String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final static String ALPHABET_WITH_SPACES = "ABCDEFGH IJKLMN OPQ RSTUVWXYZ";

    protected void initHibernate(){
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        if (sessionFactory == null) {
            new HibernateSessionCreator(TestConfiguration.getHibernateConfiguration());
        }
    }

    protected void loadOntologyManager(){
        try {
                ontologyManager = OntologyManager.getInstanceFromFile() ;
//            ontologyManager = OntologyManager.getInstanceFromFile(ontology) ;
        } catch (Exception e) {
            initHibernate();
            ontologyManager = OntologyManager.getInstance(ontology) ;
            logger.error("failed to load: " + ontologyManager,e);
        }
    }

    @Before
    public void setTempDirectory(){
        if(oldTempDirectory==null || false==oldTempDirectory.equals(testTempDirectory)){
            oldTempDirectory = System.getProperty("java.io.tmpdir");
            System.setProperty("java.io.tmpdir",testTempDirectory) ;
        }

        if(ontologyManager==null){
            loadOntologyManager();
        }

    }

    @After
    public void restsetTempDirectory(){
        System.setProperty("java.io.tmpdir",oldTempDirectory) ;
    }


    protected String generateRandomWord() {
        int length = (int) (Math.random() * 20f) + 12;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int character = (int) (Math.random() * 26);
            sb.append(ALPHABET.substring(character, character + 1));
        }
        return sb.toString().toLowerCase();
    }


    protected String getRandomWordFromSet(Set<String> strings) {
        String testWord ;
        int randomLength = (int) (Math.random() * (strings.size()-1)) ;
        int counter = 0 ;
        for(Iterator<String> iterator = strings.iterator() ; iterator.hasNext() ; ){
            testWord = iterator.next() ;
            if(counter > randomLength){
                return testWord ;
            }
            ++counter ;
        }
        return null ;
    }

    protected String generateRandomWordWithSpaces() {
        int length = (int) (Math.random() * 20f) + 12;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int character = (int) (Math.random() * ALPHABET_WITH_SPACES.length());
            sb.append(ALPHABET_WITH_SPACES.substring(character, character + 1));
        }
        return sb.toString().toLowerCase();
    }
}

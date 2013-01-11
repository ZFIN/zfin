package org.zfin.ontology;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.root.dto.OntologyDTO;
import org.zfin.gwt.root.dto.RelationshipType;
import org.zfin.gwt.root.dto.TermDTO;
import org.zfin.infrastructure.PatriciaTrieMultiMap;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.repository.RepositoryFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.*;

/**
 * 1	pato.ontology
 * 46	zebrafish_stages (5 seconds)
 * 151	spatial (7 seconds)
 * 2282	quality (100 seconds)
 * 2688	zebrafish_anatomy
 * 2941	cellular_component
 * 9896	molecular_function
 * 21017	biological_process
 */
public class OntologySerializationTest extends AbstractDatabaseTest {

    private final static Logger logger = Logger.getLogger(OntologySerializationTest.class);

    private OntologyRepository ontologyRepository = RepositoryFactory.getOntologyRepository();

    private static String testTempDirectory = "test-delete";
    private static String oldTempDirectory;
    private static File testDirectory ;

    @Before
    public void setTempDirectory() {
	System.out.println("odl temp dir: "+ oldTempDirectory);	
	System.out.println("java.io.tmpdir: "+System.getProperty("java.io.tmpdir") );

        if (oldTempDirectory == null || false == oldTempDirectory.equals(testTempDirectory)) {
            oldTempDirectory = System.getProperty("java.io.tmpdir");
            System.setProperty("java.io.tmpdir", testTempDirectory);
            testDirectory = new File(testTempDirectory) ;
            //assertTrue(!testDirectory.exists() || testDirectory.delete());
            //assertTrue(testDirectory.mkdir());
	boolean success = testDirectory.mkdir();
        }
    }

    @After
    public void resetTempDirectory() throws IOException {
        System.setProperty("java.io.tmpdir", oldTempDirectory);
        try {
            FileUtils.deleteDirectory(testDirectory);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        //assertFalse(testDirectory.exists());
    }

    @Test
    public void serializeStage() throws Exception {
        List<DevelopmentStage> developmentStageList = HibernateUtil.currentSession().createCriteria(DevelopmentStage.class).list();
        OntologyManager ontologyManager = new OntologyManager();
        ontologyManager.initOntologyMapFastNoRelations(Ontology.STAGE);
        assertNotNull(ontologyManager.getTermByName("Adult", Ontology.STAGE));
        PatriciaTrieMultiMap<TermDTO> stageMap = ontologyManager.getOntologyMap().get(OntologyDTO.STAGE);

        // +1 because root of stages is "Stage" in the term tabled
        Set<TermDTO> termDTOs = stageMap.getAllValues();
        assertEquals(developmentStageList.size() + 1, termDTOs.size());

        OntologySerializationService service = new OntologySerializationService(ontologyManager);
        service.serializeOntology(Ontology.STAGE);

        stageMap = ontologyManager.getOntologyMap().get(OntologyDTO.STAGE);
        assertEquals(developmentStageList.size() + 1, stageMap.getAllValues().size());
        assertNotNull(ontologyManager.getTermByName("Adult", Ontology.STAGE));
    }

    @Test
    public void serializeSpatial() throws Exception {
        int count = ontologyRepository.getNumberTermsForOntology(Ontology.SPATIAL);
        OntologyManager ontologyManager = new OntologyManager();
//        ontologyManager.initSingleOntologyMap(Ontology.SPATIAL);
        ontologyManager.initOntologyMapFast(Ontology.SPATIAL);


        TermDTO termAnatomicalSide = ontologyManager.getTermByName("anatomical side", Ontology.SPATIAL);
        assertNotNull(termAnatomicalSide);
        assertTrue(termAnatomicalSide.getChildrenTerms().size() > 15);
        for (TermDTO childTerm : termAnatomicalSide.getChildrenTerms()) {
            assertNotNull(childTerm.getName());
        }

        TermDTO termToSerialize = ontologyManager.getTermByName("left/right axis", Ontology.SPATIAL);
        assertNotNull(termToSerialize);
        PatriciaTrieMultiMap<TermDTO> anatomyMap = ontologyManager.getOntologyMap().get(OntologyDTO.SPATIAL);

        assertEquals(count, anatomyMap.getAllValues().size());

        OntologySerializationService service = new OntologySerializationService(ontologyManager);
        service.serializeOntology(Ontology.SPATIAL);


        anatomyMap = ontologyManager.getOntologyMap().get(OntologyDTO.SPATIAL);
        assertEquals(count, anatomyMap.getAllValues().size());
        TermDTO termDeserialized = ontologyManager.getTermByName("left/right axis", Ontology.SPATIAL);
        assertNotNull(termDeserialized);
        assertEquals(termToSerialize, termDeserialized);


        Term termFromDatabase = ontologyRepository.getTermByName("left/right axis", Ontology.SPATIAL);
        assertNotNull(termFromDatabase);

        assertEquals(termFromDatabase.getTermName(), termDeserialized.getName());
        assertEquals(termFromDatabase.getAliases().size(), termDeserialized.getAliases().size());
        assertEquals(termFromDatabase.getParentTerms().size(), termDeserialized.getParentTerms().size());
        assertEquals(termFromDatabase.getChildTerms().size(), termDeserialized.getChildrenTerms().size());
        Map<String, Set<TermDTO>> allRelatedTerms = termDeserialized.getAllRelatedTerms();
        assertEquals(3, allRelatedTerms.keySet().size()); // starts axis, finishes axis, is_a
        assertEquals(1, allRelatedTerms.get("inverse starts_axis").size());
        assertEquals(1, allRelatedTerms.get("inverse finishes_axis").size());
        assertEquals(1, allRelatedTerms.get("is a type of").size());
        assertEquals(termFromDatabase.getComment(), termDeserialized.getComment());
        assertEquals(termFromDatabase.getDefinition(), termDeserialized.getDefinition());
        assertEquals(termFromDatabase.getOboID(), termDeserialized.getOboID());
        // TODO: Note that this won't work with quality . . . grr
        assertEquals(termFromDatabase.getOntology().getOntologyName(), termDeserialized.getOntology().getOntologyName());
        assertEquals(termFromDatabase.getZdbID(), termDeserialized.getZdbID());


    }

//        @Test
    public void serializeQuality() throws Exception {
        long startTime = System.currentTimeMillis();
        int termCount = ontologyRepository.getNumberTermsForOntology(Ontology.QUALITY);

//        logger.info("time A: " +  (System.currentTimeMillis()-startTime ) / 1000f  + "s");
        OntologyManager ontologyManager = new OntologyManager();
//        ontologyManager.initOntologyMap(Ontology.QUALITY);
        ontologyManager.initSingleOntologyMap(Ontology.QUALITY);
        TermDTO termToSerialize = ontologyManager.getTermByName("variant", Ontology.QUALITY);
        assertNotNull(termToSerialize);
        PatriciaTrieMultiMap<TermDTO> anatomyMap = ontologyManager.getOntologyMap().get(OntologyDTO.QUALITY);

        assertEquals(termCount, anatomyMap.getAllValues().size());

        OntologySerializationService service = new OntologySerializationService(ontologyManager);
        service.serializeOntology(Ontology.QUALITY);
        ontologyManager.deserializeOntology(Ontology.QUALITY);


        anatomyMap = ontologyManager.getOntologyMap().get(OntologyDTO.QUALITY);
        assertEquals(termCount, anatomyMap.getAllValues().size());
        TermDTO termDeserialized = ontologyManager.getTermByName("variant", Ontology.QUALITY);
        assertNotNull(termDeserialized);
        assertEquals(termToSerialize, termDeserialized);


        Term termFromDatabase = ontologyRepository.getTermByName("variant", Ontology.QUALITY);
        assertNotNull(termFromDatabase);

        assertEquals(termFromDatabase.getZdbID(), termDeserialized.getZdbID());
        assertEquals(termFromDatabase.getTermName(), termDeserialized.getName());
        assertEquals(termFromDatabase.getAliases().size(), termDeserialized.getAliases().size());
        assertEquals(termFromDatabase.getParentTerms().size(), termDeserialized.getParentTerms().size());
        assertEquals(termFromDatabase.getChildTerms().size(), termDeserialized.getChildrenTerms().size());
        Map<String, Set<TermDTO>> allRelatedTerms = termDeserialized.getAllRelatedTerms();
        assertEquals(1, allRelatedTerms.keySet().size()); // starts axis, finishes axis, is_a
        assertEquals(3, allRelatedTerms.get("is_a").size());
        assertEquals(termFromDatabase.getComment(), termDeserialized.getComment());
        assertEquals(termFromDatabase.getDefinition(), termDeserialized.getDefinition());
        assertEquals(termFromDatabase.getOboID(), termDeserialized.getOboID());

        logger.info("time End: " + (System.currentTimeMillis() - startTime) / 1000f + "s");
//        return ontologyManager;
        // TODO: Note that this won't work with quality . . . grr
//        assertEquals(termFromDatabase .getOntology().getOntologyName(),termDeserialized .getOntology().getOntologyName()) ;
    }

    @Test
    public void serializeQualityRoot() throws Exception {
        OntologyManager ontologyManager = new OntologyManager();
//        ontologyManager.deserializeOntology(OntologyDTO.QUALITY);
        ontologyManager.initOntologyMapFast(Ontology.QUALITY);
        ontologyManager.serializeOntology(Ontology.QUALITY);

//        assertEquals(termList.size(), anatomyMap.getAllValues().size());

        assertNotNull(ontologyManager.getTermByName("quality", Ontology.QUALITY));
        assertNotNull(ontologyManager.getTermByName("extra or missing processual parts", Ontology.QUALITY));

//        ontologyManager.initRootOntologyMap(Ontology.QUALITY_PROCESSES,Ontology.QUALITY, "PATO:0001236");
//        ontologyManager.initQualityProcessesRootOntology();
        ontologyManager.initRootOntologyFast(Ontology.QUALITY_PROCESSES, OntologyManager.QUALITY_PROCESSES_ROOT);
        assertThat(ontologyManager.getTermsForOntology(OntologyDTO.QUALITY_PROCESSES).getAllValues().size() , lessThan(300) );
        assertThat(ontologyManager.getTermsForOntology(OntologyDTO.QUALITY_PROCESSES).getAllValues().size() , greaterThan(94));

        ontologyManager.serializeOntology(Ontology.QUALITY_PROCESSES);

        ontologyManager.initRootOntologyFast(Ontology.QUALITY_QUALITIES, OntologyManager.QUALITY_QUALITIES_ROOT);
        assertThat(ontologyManager.getTermsForOntology(OntologyDTO.QUALITY_QUALITIES).getAllValues().size() , lessThan(2000));
        assertThat(ontologyManager.getTermsForOntology(OntologyDTO.QUALITY_QUALITIES).getAllValues().size() , greaterThan(1100));

        ontologyManager.serializeOntology(Ontology.QUALITY_QUALITIES);

        assertNotNull(ontologyManager.getTermByName("quality", Ontology.QUALITY));
        assertNull(ontologyManager.getTermByName("quality", Ontology.QUALITY_PROCESSES));
        assertNotNull(ontologyManager.getTermByName("extra or missing processual parts", Ontology.QUALITY_PROCESSES));


        assertNotNull(ontologyManager.getTermByName("quality", Ontology.QUALITY));
        assertNull(ontologyManager.getTermByName("quality", Ontology.QUALITY_PROCESSES));
        assertNotNull(ontologyManager.getTermByName("extra or missing processual parts", Ontology.QUALITY_PROCESSES));

        ontologyManager.deserializeOntology(Ontology.QUALITY_PROCESSES);

        assertNotNull(ontologyManager.getTermByName("quality", Ontology.QUALITY));
        assertNull(ontologyManager.getTermByName("quality", Ontology.QUALITY_PROCESSES));
        assertNotNull(ontologyManager.getTermByName("extra or missing processual parts", Ontology.QUALITY_PROCESSES));

    }


    // typically this is too long to run
//    @Test
    public void serializeAnatomy() {
        long startTime, endTime;
        OntologyManager ontologyManager = OntologyManager.getEmptyInstance();

        startTime = System.currentTimeMillis();
        ontologyManager.initOntologyMapFastNoRelations(Ontology.STAGE); // correct for this
        endTime = System.currentTimeMillis();
        logger.info("STAGE time End: " + (endTime - startTime) / 1000f + "s");


        startTime = System.currentTimeMillis();
        ontologyManager.initOntologyMapFast(Ontology.ANATOMY);
        endTime = System.currentTimeMillis();
        logger.info("ANATOMY time End: " + (endTime - startTime) / 1000f + "s");

        TermDTO heartTermDTO = ontologyManager.getTermByName("heart", Ontology.ANATOMY);
        assertNotNull(heartTermDTO);
        Term heartTerm = ontologyRepository.getTermByName("heart", Ontology.ANATOMY);
        assertNotNull(heartTerm);
        assertEquals(heartTerm.getChildTerms().size(), heartTermDTO.getChildrenTerms().size());
        assertEquals(heartTerm.getParentTerms().size(), heartTermDTO.getParentTerms().size());
        Map<String, Set<TermDTO>> allRelatedTerms = heartTermDTO.getAllRelatedTerms();
        assertEquals(6, allRelatedTerms.size());
        assertEquals(2, allRelatedTerms.get(RelationshipType.PART_OF2.getDisplay()).size());
        assertEquals(11, allRelatedTerms.get(RelationshipType.HAS_PARTS.getDisplay()).size());
        assertEquals(1, allRelatedTerms.get(RelationshipType.DEVELOPS_FROM2.getDisplay()).size());
        assertEquals(1, allRelatedTerms.get(RelationshipType.END_STAGE.getDisplay()).size());
        assertEquals(1, allRelatedTerms.get(RelationshipType.IS_A.getDisplay()).size());
        assertEquals(1, allRelatedTerms.get(RelationshipType.START_STAGE.getDisplay()).size());

        // happens to be obsolete
        TermDTO termDeserialized = ontologyManager.getTermByName("portion of tissue", Ontology.ANATOMY);
        assertNotNull(termDeserialized);
        Term termFromDatabase = ontologyRepository.getTermByName("portion of tissue", Ontology.ANATOMY);

        assertEquals(termFromDatabase.getZdbID(), termDeserialized.getZdbID());
        assertEquals(termFromDatabase.getTermName(), termDeserialized.getName());
//        assertNull(termDeserialized.getAliases()) ;
//        assertEquals(0,termFromDatabase.getChildTerms().size());
        assertEquals(termFromDatabase.getChildTerms().size(), termDeserialized.getChildrenTerms().size());
        assertEquals(termFromDatabase.getParentTerms().size(), termDeserialized.getParentTerms().size());

        assertNotNull(termDeserialized.getStartStage());
        assertNotNull(termDeserialized.getEndStage());

//        assertEquals(ontologyRepository.getNumberTermsForOntology(Ontology.ANATOMY)
//                , ontologyManager.getTermsForOntology(OntologyDTO.ANATOMY).getAllValues().size());

//        for(TermDTO anaTermDTO : ontologyManager.getTermsForOntology(OntologyDTO.ANATOMY).getAllValues()){
//            if(false==anaTermDTO.isObsolete()){
//                assertNotNull("Did not have a populated stage: " + anaTermDTO.getZdbID(),anaTermDTO.getStartStage());
//                assertNotNull(anaTermDTO.getEndStage());
//            }
//        }
    }
}

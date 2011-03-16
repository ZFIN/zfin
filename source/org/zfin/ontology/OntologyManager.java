package org.zfin.ontology;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.gwt.root.dto.OntologyDTO;
import org.zfin.gwt.root.dto.TermDTO;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.infrastructure.PatriciaTrieMultiMap;
import org.zfin.ontology.presentation.OntologyLoadingEntity;
import org.zfin.ontology.service.OntologyService;
import org.zfin.repository.RepositoryFactory;
import org.zfin.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.zfin.repository.RepositoryFactory.getOntologyRepository;

/**
 * This class manages all ontologies used in ZFIN for lookup.
 * It is initialized once and stores all ontologies in static maps.
 * For each ontology there will be two maps:<br/>
 * 1) a map with (term name, term)<br/>
 * 2) a map with (alias, term)<br/>
 * This allows to retrieve matches against a term name and an alias.
 *
 * NOTE: This class should ONLY used CachedTermDTO!!! never GenericTermDTO
 */
public class OntologyManager {

    protected final static String QUALITY_PROCESSES_ROOT = "PATO:0001236";
    protected final static String QUALITY_QUALITIES_ROOT = "PATO:0001241";

    public static final int NUMBER_OF_SERIALIZABLE_ONTOLOGIES = Ontology.getSerializableOntologies().length;
    private static Map<OntologyDTO, PatriciaTrieMultiMap<TermDTO>> ontologyTermDTOMap = new HashMap<OntologyDTO, PatriciaTrieMultiMap<TermDTO>>(NUMBER_OF_SERIALIZABLE_ONTOLOGIES);
    // holds all relationships between terms.
    // this object only exists during initialization of the ontologies.
    // The helper class does not reference the TermDTO object rather the PKs only to allow this
    // object to be serialized. Since every term has a relationship to all children and parents, through the
    // parents and children relationships each term is indirectly related to all terms in the Ontology
    // which would upon serialization lead to infinite loops and ultimately a StackOverflowError
//    private Map<String, List<TermDTORelationship>> allRelationships;
    // distinct relationship Types used in an ontology
//    private Map<OntologyDTO, Set<String>> distinctRelationshipTypes = new HashMap<OntologyDTO, Set<String>>(NUMBER_OF_SERIALIZABLE_ONTOLOGIES);
    private OntologyTokenizer tokenizer = new OntologyTokenizer(3);
    // name of serialized file.

    /**
     * A map of all terms as a key and a list of terms that are children of the key term given by the transitive closure.
     */
//    private Map<TermDTO, List<TransitiveClosure>> allRelatedChildrenMap;
    private Map<OntologyDTO, OntologyLoadingEntity> loadingData = new TreeMap<OntologyDTO, OntologyLoadingEntity>(new OntologyNameComparator());
    // sole singleton instance
    private static OntologyManager ontologyManager = null;
    transient private static final Logger logger = Logger.getLogger(OntologyManager.class);

    private static final double MILLISECONDS_PER_SECOND = 1000.0;

    private static Map<OntologyDTO, Double> loadingTimeMap = new HashMap<OntologyDTO, Double>(10);
    private static final Object LOADING_FLAG = new Object();
    public static final String SERIALIZED_LOOKUP_SUFFIX = "-lookup.ser";
    public static final String LOADING_STATS_SER = "loading-statistics.ser";

    private static Ontology singleOntology = null;

    private static int activeCount, aliasCount, obsoleteCount;
    private static long startTime, endTime;
    private static Date dateStarted;


    /**
     * Obtain the reference to the singleton instance of this manager.
     * It may return a manager which is not yet fully initialized, to allow for a quicker access to
     * the ontologies. May have to be stricter about the access.
     *
     * @return sole instance of this manager.
     */
    public static OntologyManager getInstance() {

        if (ontologyManager == null) {
            init();
        }
        return ontologyManager;
    }

    /**
     * Exists as a singleton only, except for tests.
     */
    protected OntologyManager() { }

    public static OntologyManager getEmptyInstance() {

        if (ontologyManager == null) {
            ontologyManager = new OntologyManager();
        }
        return ontologyManager;
    }

    public static OntologyManager getInstance(LoadingMode mode) throws Exception {
        switch (mode) {
            case DATABASE:
                return getInstance();
            case SERIALIZED_FILE:
                return getInstanceFromFile();
        }
        throw new RuntimeException("No valid loading mode provided");
    }

    public static OntologyManager getInstanceFromFile() throws Exception {
        ontologyManager = new OntologyManager();
        try {
            ontologyManager.deserializeOntologies();
        } catch (Exception e) {
            logger.warn("Problem loading serialized file. Try loading ontologies from database...", e);
            init();
        }
        return ontologyManager;
    }

    public static OntologyManager getInstanceFromFile(OntologyDTO ontology) throws Exception {
        if (ontologyManager == null) {
            ontologyManager = new OntologyManager();
        }
        ontologyManager.deserializeOntology(ontology);
        return ontologyManager;
    }


    public static OntologyManager getInstance(Ontology ontology) {
        singleOntology = ontology;
        if (ontologyManager == null) {
            synchronized (LOADING_FLAG) {
                init();
            }
        }
        return ontologyManager;
    }

    /**
     * Check if the Manager has loaded the ontologies yet.
     *
     * @return false if loading has not started yet, true otherwise.
     */
    public static boolean hasStartedLoadingOntologies() {
        return ontologyManager != null;
    }

    /**
     * Initialization: Load all ontologies.
     * This method is synchronized to avoid loading the same ontologies twice.
     */
    private static void init() {
        logger.info("Start loading ontologies from database");
        long startTime = System.currentTimeMillis();
        synchronized (LOADING_FLAG) {
            ontologyManager = new OntologyManager();
            ontologyManager.loadOntologiesFromDatabase();
        }
        long endTime = System.currentTimeMillis();
        double loadingTimeInSeconds = (double) (endTime - startTime) / MILLISECONDS_PER_SECOND;
        logger.info("Finished loading all ontologies: took " + loadingTimeInSeconds + " seconds.");
    }

    protected void loadOntologiesFromDatabase() {
        if (singleOntology != null) {
//            List<GenericTermRelationship> relationships = getOntologyRepository().getAllRelationships();
//            allRelationships = createRelationshipsMap(relationships);
            initSingleOntologyMap(singleOntology);
            serializeOntology(singleOntology);
//            if (singleOntology == Ontology.ANATOMY) {
//                loadTransitiveClosure();
//            }
        } else {
            loadDefaultOntologiesFromDatabase();
//            loadTransitiveClosure();
        }
    }

//    /**
//     * Load the transitive closure, the object that contains the children terms for each term in all ontologies.
//     * Currently, we only have the closure info for the Anatomy ontology.
//     */
//    private void loadTransitiveClosure() {
//        List<TransitiveClosure> closures = getOntologyRepository().getTransitiveClosureForAnatomy();
//        if (closures == null || closures.size() == 0)
//            return;
//
//        // for now we hard-code the size to the size of the anatomy ontology.
//        allRelatedChildrenMap = new HashMap<TermDTO, List<TransitiveClosure>>(getTermOntologyMap(Ontology.ANATOMY).size());
//        for (TransitiveClosure closure : closures) {
//            TermDTO root = closure.getRoot().createTermDTO();
//            TermDTO omRoot = getTermByID(root.getZdbID(), root.getOntology());
//            List<TransitiveClosure> children = allRelatedChildrenMap.get(omRoot);
//            if (children == null)
//                children = new ArrayList<TransitiveClosure>(10);
//            children.add(closure);
//            Collections.sort(children);
//            allRelatedChildrenMap.put(omRoot, children);
//        }
//        logger.info("Loading Transitive closure for  " + allRelatedChildrenMap.size() + " terms. The total number of connections is " + closures.size());
//    }

    private void loadDefaultOntologiesFromDatabase() {
        // load relationships
//        loadTermRelationshipsFromDatabase();
        // Spatial Modifier

//        initSingleOntologyMap(Ontology.SPATIAL);
        initOntologyMapFast(Ontology.SPATIAL);
        serializeOntology(Ontology.SPATIAL);

//        initSingleOntologyMap(Ontology.STAGE);
//        initSingleOntologyMap(Ontology.STAGE);
        initOntologyMapFastNoRelations(Ontology.STAGE);
        serializeOntology(Ontology.STAGE);

//        initSingleOntologyMap(Ontology.ANATOMY);
        initOntologyMapFast(Ontology.ANATOMY);
        serializeOntologyInThread(Ontology.ANATOMY);

//        populateStageInformationForAnatomy();

        // define the root ontology for Quality here
//        initSingleOntologyMap(Ontology.QUALITY);
        initOntologyMapFast(Ontology.QUALITY);
        serializeOntologyInThread(Ontology.QUALITY);

        // Quality  Processes and Objects
        // Root is "process quality"
        initRootOntologyFast(QUALITY_PROCESSES_ROOT, Ontology.QUALITY_PROCESSES);
//        initRootOntologyMap(Ontology.QUALITY_PROCESSES, Ontology.QUALITY, "PATO:0001236");
        serializeOntologyInThread(Ontology.QUALITY_PROCESSES);

        // Root is "physical object quality"
        initRootOntologyFast(QUALITY_QUALITIES_ROOT, Ontology.QUALITY_QUALITIES);
//        initRootOntologyMap(Ontology.QUALITY_QUALITIES, Ontology.QUALITY, "PATO:0001241");
        serializeOntologyInThread(Ontology.QUALITY_QUALITIES);

        // GO ontology
//        initSingleOntologyMap(Ontology.GO_CC);
        initOntologyMapFast(Ontology.GO_CC);
        serializeOntologyInThread(Ontology.GO_CC);
//        initSingleOntologyMap(Ontology.GO_MF);
        initOntologyMapFast(Ontology.GO_MF);
        serializeOntologyInThread(Ontology.GO_MF);
//        initSingleOntologyMap(Ontology.GO_BP);
        initOntologyMapFast(Ontology.GO_BP);
        serializeOntologyInThread(Ontology.GO_BP);
//        HibernateUtil.currentSession().clear();

        serializeObject(loadingData, LOADING_STATS_SER);
        // transitive closure
//        loadTransitiveClosure();
//        serializeObject(allRelatedChildrenMap, TRANSITIVE_CLOSURE_SER);
//        serializeObject(distinctRelationshipTypes, RELATIONSHIP_TYPES_SER);
    }

    private void serializeOntologyInThread(final Ontology ontology) {
        new Thread() {
            @Override
            public void run() {
                serializeOntology(ontology);
            }
        }.start();
    }

//    private void loadTermRelationshipsFromDatabase() {
//        resetCounter();
//        List<GenericTermRelationship> relationships = getOntologyRepository().getAllRelationships();
//        loadTermRelationships(relationships);
////        serializeTermRelationships();
//    }


//    private void serializeTermRelationships() {
//        startTime = System.currentTimeMillis();
//        File lookupFile = FileUtil.serializeObject(allRelationships,
//                FileUtil.createOntologySerializationFile(ALL_RELATIONSHIPS));
//        endTime = System.currentTimeMillis();
//        double time = (double) (endTime - startTime) / MILLISECONDS_PER_SECOND;
//
//        logger.info("Time to serialize relationships: " + time + " seconds.");
//        logger.info("Lookup file path[" + lookupFile.getAbsolutePath() + "] size: " + (lookupFile.length() / 1024) + "kB");
//    }

//    public void loadTermRelationships(List<GenericTermRelationship> relationships) {
//        allRelationships = createRelationshipsMap(relationships);
//        endTime = System.currentTimeMillis();
//        double time = (double) (endTime - startTime) / MILLISECONDS_PER_SECOND;
//        logger.info("Time to load relationships from database: " + time + " seconds.");
//    }

    /**
     * @deprecated
     * @param subOntology
     * @param rootOntology
     * @param rootOboIDs
     */
    protected void initRootOntologyMap(Ontology subOntology, Ontology rootOntology, String... rootOboIDs) {
        long startTime = System.currentTimeMillis();
        Date dateStarted = new Date();
        int averageMaximumNumOfChildren = 30;

        List<TermDTO> children = new ArrayList<TermDTO>(averageMaximumNumOfChildren);
        Set<TermDTO> childrenSet = new HashSet<TermDTO>(averageMaximumNumOfChildren);
        for (String rootOboID : rootOboIDs) {
            Term rootTerm = getOntologyRepository().getTermByOboID(rootOboID);
            for(Term childTerm : rootTerm.getChildTerms()){
                TermDTO childTermDTO = DTOConversionService.convertToTermDTOWithDirectRelationships(childTerm);
                children.add(childTermDTO);
                childrenSet.add(childTermDTO);
            }
        }
        PatriciaTrieMultiMap<TermDTO> termMap = new PatriciaTrieMultiMap<TermDTO>();
        int obsoleteCount = 0;
        int aliasCount = 0;
        int activeCount = 0;
        while (childrenSet.size() > 0) {
            TermDTO currentTermDTO = childrenSet.iterator().next();
            childrenSet.remove(currentTermDTO);
            Set<TermDTO> newChildren = currentTermDTO.getChildrenTerms();
            if (newChildren != null && !newChildren.isEmpty()) {
                for (TermDTO child : newChildren) {
                    if (children.contains(child)) {
                        logger.info("TermDTO already processed: " + child.getName() + " [" + child.getZdbID());
                    } else {
                        children.add(child);
                        childrenSet.add(child);
                    }
                }
            }
            TermDTO term = getTermByID(currentTermDTO.getZdbID(), DTOConversionService.convertToOntologyDTO(rootOntology));
            if (term == null)
                logger.error("Child TermDTO <" + currentTermDTO.getZdbID() + "> not found in Root ontology " + rootOntology.getOntologyName());
            else {
                tokenizer.tokenizeTerm(currentTermDTO, termMap);
                if (currentTermDTO.isObsolete()) {
                    ++obsoleteCount;
                } else {
                    ++activeCount;
                }
                if (term.getAliases() != null) {
                    aliasCount += term.getAliases().size();
                }
            }
        }
        OntologyDTO subOntologyDTO = DTOConversionService.convertToOntologyDTO(subOntology);
        ontologyTermDTOMap.put(subOntologyDTO, termMap);

        long endTime = System.currentTimeMillis();
        double loadingTimeInSeconds = (double) (endTime - startTime) / MILLISECONDS_PER_SECOND;
        logLoading(subOntology, startTime, endTime, dateStarted, activeCount, obsoleteCount, aliasCount,
                termMap.keySet().size(), termMap.getAllValues().size());
        loadingTimeMap.put(subOntologyDTO, loadingTimeInSeconds);
    }


    /**
     * Load a single ontology.
     *
     * @param ontology Ontology
     * @deprecated
     */
    public void initSingleOntologyMap(Ontology ontology) {
        resetCounter();
        List<GenericTerm> terms = getOntologyRepository().getAllTermsFromOntology(ontology);
        long nextTime = System.currentTimeMillis();
        logger.debug("time to load from DB: " + (nextTime - startTime));
        loadTermsIntoOntology(terms, ontology);
    }

    private void resetCounter() {
        startTime = System.currentTimeMillis();
        dateStarted = new Date();
        activeCount = 0;
        aliasCount = 0;
        obsoleteCount = 0;
    }


    /**
     *
     * @param ontology
     */
    public void initOntologyMapFastNoRelations(Ontology ontology) {
        resetCounter();
        Collection<TermDTO> termDTOs = getOntologyRepository().getTermDTOsFromOntologyNoRelation(ontology);
        createMapForTerms(termDTOs, ontology);
    }

    private PatriciaTrieMultiMap<TermDTO> createMapForTerms(Collection<TermDTO> termDTOs,Ontology ontology){
        long nextTime = System.currentTimeMillis();
        PatriciaTrieMultiMap<TermDTO> termMap = new PatriciaTrieMultiMap<TermDTO>();
        if (termDTOs == null) {
            logger.error("No terms for ontology <" + ontology.getOntologyName() + "> found.");
            return termMap;
        }

        for (TermDTO term : termDTOs) {
            tokenizer.tokenizeTerm(term, termMap);
            if (term.isObsolete()) {
                ++obsoleteCount;
            } else {
                ++activeCount;
            }

            if (term.getAliases() != null) {
                aliasCount += term.getAliases().size();
            }
        }
        ontologyTermDTOMap.put(DTOConversionService.convertToOntologyDTO(ontology), termMap);
        logger.debug("to have loaded ontologies : " + (System.currentTimeMillis() - nextTime) / MILLISECONDS_PER_SECOND + "s");

        endTime = System.currentTimeMillis();
        logLoading(ontology, startTime, endTime, dateStarted, activeCount, obsoleteCount, aliasCount,
                termMap.keySet().size(), termMap.getAllValues().size());
        return termMap;
    }

    public void initOntologyMapFast(Ontology ontology) {

        resetCounter();
        Map<String,TermDTO> termDTOMap = getOntologyRepository().getTermDTOsFromOntology(ontology);
        Collection<TermDTO> termDTOs = OntologyService.populateRelationships(termDTOMap, this);

        createMapForTerms(termDTOs,ontology);
    }

    /**
     * @deprecated
     * @param terms
     * @param ontology
     */
    public void loadTermsIntoOntology(List<GenericTerm> terms, Ontology ontology) {

        long nextTime = System.currentTimeMillis();

        PatriciaTrieMultiMap<TermDTO> termMap = new PatriciaTrieMultiMap<TermDTO>();
        if (terms == null) {
            logger.info("No terms for ontology <" + ontology.getOntologyName() + "> found.");
            return;
        }

        for (Term term : terms) {
            if(term.getOntology()==Ontology.STAGE){
                tokenizer.tokenizeTerm(DTOConversionService.convertToTermDTO(term), termMap);
            }
            else {
                tokenizer.tokenizeTerm(DTOConversionService.convertToTermDTOWithDirectRelationships(term), termMap);
            }
            if (term.isObsolete()) {
                ++obsoleteCount;
            } else {
                ++activeCount;
            }

            if (term.getAliases() != null) {
                aliasCount += term.getAliases().size();
            }

        }
        ontologyTermDTOMap.put(DTOConversionService.convertToOntologyDTO(ontology), termMap);
        logger.debug("to have loaded ontologies : " + (System.currentTimeMillis() - nextTime) / MILLISECONDS_PER_SECOND + "s");
//        nextTime = System.currentTimeMillis();

        endTime = System.currentTimeMillis();
        logLoading(ontology, startTime, endTime, dateStarted, activeCount, obsoleteCount, aliasCount,
                termMap.keySet().size(), termMap.getAllValues().size());
    }

    /**
     * Only for those ontologies that are not the stage ontology.
     *
     * @param terms    all terms of a given ontology
     * @param ontology given ontology
     */
//    public void populateRelationshipsFromDatabase(Iterable<TermDTO> terms, Ontology ontology) {
////        if (ontology == Ontology.STAGE)
////            return;
//
//        Map<String,List<TermRelationship>> relationshipsForOntology = RepositoryFactory.getOntologyRepository().getTermRelationshipsForOntology(ontology);
//        if(relationshipsForOntology.size()==0){
//            return ;
//        }
//
//        // populate the relationships onto each term
//        for (TermDTO term : terms) {
//            List<TermRelationship> termRelationships =  relationshipsForOntology.get(term.getZdbID());
//            term.setRelatedTerms(termRelationships);
//            if (termRelationships != null) {
//                List<TermRelationship> relatedTermDTOs = new ArrayList<TermRelationship>(relationshipsForOntology.size());
//                for (TermRelationship termRelationship : termRelationships) {
//                    // is this even necessary?
////                    termRelationship.setTermDTOOne(termRelationship.getTermOne());
////                    termRelationship.setTermDTOTwo(termRelationship.getTermTwo());
//                    relatedTermDTOs.add(termRelationship);
//                    // add relationship type to map
//                    addRelationshipType(ontology, termRelationship.getType());
//                }
//                term.setRelatedTerms(relatedTermDTOs);
//            }
//        }
//    }
//
//    private void addRelationshipType(Ontology ontology, String type) {
//        Set<String> relationshipTypes = distinctRelationshipTypes.get(ontology);
//        if (relationshipTypes == null) {
//            relationshipTypes = new HashSet<String>();
//        }
//        relationshipTypes.add(type);
//        distinctRelationshipTypes.put(ontology, relationshipTypes);
//    }


//    private Map<String, List<TermRelationship>> createRelationshipsMap(List<TermRelationship> relationships) {
//        Map<String, List<TermRelationship>> map = new HashMap<String, List<TermRelationship>>(relationships.size());
//        for (TermRelationship relationship : relationships) {
//            List<TermRelationship> relListOne;
//            relListOne = map.get(relationship.getTermOne().getZdbID());
//            if (relListOne == null) {
//                int averageNumOfRelationships = 5;
//                relListOne = new ArrayList<TermRelationship>(averageNumOfRelationships);
//            }
//            relListOne.add(relationship);
//            map.put(relationship.getTermOne().getZdbID(), relListOne);
//
//            List<TermRelationship> relList;
//            relList = map.get(relationship.getTermTwo().getZdbID());
//            if (relList == null) {
//                int averageNumOfRelationships = 5;
//                relList = new ArrayList<TermRelationship>(averageNumOfRelationships);
//            }
//            relList.add(relationship);
//            map.put(relationship.getTermTwo().getZdbID(), relList);
//        }
//        return map;
//    }

    /**
     * Check if the ontology is anatomy and then get the stage info and set it onto the term object.
     *
     */
//    private void populateStageInformationForAnatomy() {
//        // only anatomy terms have stages defined.
//
//        Map<String, DevelopmentStage> developmentStageMap = getAllStageMap();
//        List<GenericTerm> anatomyTerms = RepositoryFactory.getOntologyRepository().getAllTermsFromOntology(Ontology.ANATOMY);
//        for (GenericTerm term : anatomyTerms) {
//            TermDTO termDTO = getTermByID(term.getZdbID());
//            List<TermRelationship> relatedTerms = term.getAllDirectlyRelatedTerms();
//            if (relatedTerms != null){
//                for (TermRelationship relatedTermDTO : relatedTerms) {
//                    // ToDo: temporary until we know why this relationship name is changed back and forth.
//                    if (relatedTermDTO.getType().equals("start stage") || relatedTermDTO.getType().equals("start")) {
//                        String relatedTermDTOOboId = relatedTermDTO.getRelatedTerm(term).getOboID();
//                        DevelopmentStage start = developmentStageMap.get(relatedTermDTOOboId);
//                        termDTO.setStartStage(DTOConversionService.convertToStageDTO(start));
//                    } else if (relatedTermDTO.getType().equals("end stage") || relatedTermDTO.getType().equals("end")) {
//                        String relatedTermDTOOboId = relatedTermDTO.getRelatedTerm(term).getOboID();
//                        DevelopmentStage end = developmentStageMap.get(relatedTermDTOOboId);
//                        termDTO.setEndStage(DTOConversionService.convertToStageDTO(end));
//                    }
//                }
//            }
//        }
//    }

    /**
     * Create a map of all stages found in the stage table.
     * key: obo id
     * value DevelopmentStage object
     *
     * @return map of all stages keyed by oboID.
     */
    private Map<String, DevelopmentStage> getAllStageMap() {
        List<DevelopmentStage> allStages = RepositoryFactory.getAnatomyRepository().getAllStages();
        Map<String, DevelopmentStage> allStagesMap = new HashMap<String, DevelopmentStage>(allStages.size());
        for (DevelopmentStage stage : allStages) {
            allStagesMap.put(stage.getOboID(), stage);
        }
        return allStagesMap;
    }


    private void logLoading(Ontology ontology, long startTime, long endTime, Date dateOfStart,
                            int numOfTermDTOs, int numOfObsoleteTermDTOs, int numOfAliases, int numKeys, int numValues) {
        long loadingTime = endTime - startTime;
        double loadingTimeSeconds = (double) loadingTime / MILLISECONDS_PER_SECOND;
        logger.info("Loading <" + ontology.getOntologyName() + "> ontology took " + loadingTimeSeconds + " seconds.");
        OntologyLoadingEntity loadingEntity = loadingData.get(DTOConversionService.convertToOntologyDTO(ontology));
        if (loadingEntity == null) {
            loadingEntity = new OntologyLoadingEntity(ontology);
        }
        loadingEntity.addLoadingEvent(dateOfStart, loadingTime, numOfTermDTOs, numOfObsoleteTermDTOs, numOfAliases, numKeys, numValues);
        loadingData.put(DTOConversionService.convertToOntologyDTO(ontology), loadingEntity);
    }

    /**
     * Retrieve a copy of the internal map for a given ontology or set.
     *
     *
     * @param ontology Ontology
     * @return map
     */
    public PatriciaTrieMultiMap<TermDTO> getTermOntologyMapCopy(Ontology ontology) {
        PatriciaTrieMultiMap<TermDTO> map = new PatriciaTrieMultiMap<TermDTO>();

        if (ontology.isComposedOntologies()) {
            // now we construct a map
            for (Ontology subOntology : ontology.getIndividualOntologies()) {
                map.putAll(ontologyTermDTOMap.get(DTOConversionService.convertToOntologyDTO(subOntology)));
            }
        } else {
            map.putAll(ontologyTermDTOMap.get(DTOConversionService.convertToOntologyDTO(ontology)));
        }
        return map;
    }


    /**
     * Retrieve a term by ID (internal) or Obo ID from a given ontology.
     * Not just a zdbID so use "ID".
     *
     *
     * @param id       term ID
     * @param ontology ontology
     * @return term
     */
    public TermDTO getTermByID(String id, OntologyDTO ontology) {
        Set<TermDTO> terms = ontologyTermDTOMap.get(ontology).get(id);
        //if (CollectionUtils.isNotEmpty(terms)) {
        if (terms != null && terms.size() > 0) {
            if (terms.size() != 1) {
                logger.error("multiple terms [" + terms.size() + "] returned for termID: " + id);
            }
            return terms.iterator().next();
        }
        return null;
    }

    public boolean isOntologyLoaded(Ontology ontology) {
        return ontologyTermDTOMap != null && ontologyTermDTOMap.get(DTOConversionService.convertToOntologyDTO(ontology)) != null;

    }

    /**
     * Retrieve a term by ID. All ontologies will be searched through.
     * If no term is found it returns null.
     *
     * @param termID term ID
     * @return term
     */
    public TermDTO getTermByID(String termID) {
        if (StringUtils.isEmpty(termID)) {
            return null;
        }

        for (OntologyDTO ontology : ontologyTermDTOMap.keySet()) {
            TermDTO term = getTermByID(termID, ontology);
            if (term != null) {
                return term;
            }
        }
        return null;
    }

    /**
     * Determine which sub-ontology a term belongs to based on a term ID and the root ontology.
     * If no sub-ontology is found is returns the root ontology.
     *
     * @param rootOntology root Ontology
     * @param termID       term ID
     * @return sub ontology
     */
    public Ontology getSubOntology(Ontology rootOntology, String termID) {
        if (rootOntology == null)
            return null;

        Collection<Ontology> subOntologies = Ontology.getSubOntologies(rootOntology);
        for (Ontology subOntology : subOntologies) {
            if (getTermByID(termID, DTOConversionService.convertToOntologyDTO(subOntology)) != null)
                return subOntology;
        }
        return rootOntology;
    }

    /**
     * Retrieve a term by name from a given ontology.
     *
     * @param termName      term name
     * @param ontology      ontology
     * @param allowObsolete include obsolete term in the search.
     * @return term
     */
    public TermDTO getTermByName(String termName, Ontology ontology, boolean allowObsolete) {
//        PatriciaTrieMultiMap<TermDTO> termMap = getTermOntologyMapCopy(ontology);
        Set<TermDTO> terms = new HashSet<TermDTO>();
        String lookupValue = termName.trim().toLowerCase();

        if (ontology.isComposedOntologies()) {
            // now we construct a map
            for (Ontology subOntology : ontology.getIndividualOntologies()) {
                // will this be one or multiple
                PatriciaTrieMultiMap<TermDTO> termMap  = ontologyTermDTOMap.get(DTOConversionService.convertToOntologyDTO(subOntology));
                if(termMap!=null && termMap.get(lookupValue)!=null){
                    terms.addAll(termMap.get(lookupValue));
                }
            }
        } else {
            PatriciaTrieMultiMap<TermDTO> termMap  = ontologyTermDTOMap.get(DTOConversionService.convertToOntologyDTO(ontology));
            if(termMap!=null && termMap.get(lookupValue)!=null){
                terms.addAll(termMap.get(lookupValue));
            }
        }


        if (terms.size()==0) {
            logger.info("No terms for term: " + termName + " and ontology: " + ontology.getOntologyName());
            return null;
        } else {
            for (TermDTO term : terms) {
                if ((!term.isObsolete() || allowObsolete)
                        &&
                        term.getName().equalsIgnoreCase(termName)) {
                    return term;
                }
            }
            return null;
        }
    }

    /**
     * Retrieve a term by name from a given ontology.
     *
     *
     * @param termName term name
     * @param ontology ontology
     * @return term
     */
    public TermDTO getTermByName(String termName, Ontology ontology) {
        return getTermByName(termName, ontology, false);
    }

    /**
     * Check if the first term is a substructure of the second (root) term via
     * any type of relationship. It basically checks if the transitive closure has
     * an association between the two terms.
     *
     * @param child child term of the root term
     * @param root  parent term
     * @return true or false
     */
//    public boolean isSubstructureOf(Term child, Term root) {
//        List<TransitiveClosure> children = allRelatedChildrenMap.get(root);
//        // return if no children are found for the root term.
//        if (children == null) {
//            return false;
//        }
//        // ToDO: Need to loop over list rather than use .contains() method because the terms maybe be proxied
//        for (TransitiveClosure childTermDTO : children) {
//            if (childTermDTO.getChild().getZdbID().equals(child.getZdbID()))
//                return true;
//        }
//        return false;
//    }

    /**
     * Retrieve all children for a given parent term. This ignores the type or relationship by including
     * any relationship between the parent term and the child term.
     *
     * @param parent term
     * @return list of all children
     */
//    public List<TransitiveClosure> getAllChildren(TermDTO parent) {
//        return allRelatedChildrenMap.get(parent);
//    }

    /**
     * Returns an unmodifiable map of the loading times for each ontology being loaded
     *
     * @return map of loading times in seconds.
     */
//    public Map<OntologyDTO, Double> getOntologyLoadingTimes() {
//        return Collections.unmodifiableMap(loadingTimeMap);
//    }

    public Map<OntologyDTO, PatriciaTrieMultiMap<TermDTO>> getOntologyMap() {
        return ontologyTermDTOMap;
    }

    public Map<OntologyDTO, OntologyLoadingEntity> getLoadingData() {
        return Collections.unmodifiableMap(loadingData);
    }

    /**
     * This will re-load the ontologies into memory.
     * It replaces the old values in the hash map with the new ones.
     * <p/>
     * At the end a new serialized file is created to keep the file with the
     * memory instance in sync.
     */
    public synchronized void reLoadOntologies() {
        loadOntologiesFromDatabase();
        // currently done after each load
//        serializeOntologies();
    }

    final private static String NEWLINE = System.getProperty("line.separator");

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(100);
        builder.append("Ontology Manager:");
        builder.append(NEWLINE);
        builder.append("Number of Ontologies: ");
        builder.append(ontologyTermDTOMap.size());
        builder.append(NEWLINE);
        for (OntologyDTO ontology : ontologyTermDTOMap.keySet()) {
            builder.append(ontology);
            builder.append(" [");
            builder.append(ontologyTermDTOMap.get(ontology).size());
            builder.append("]");
            builder.append(NEWLINE);
        }
        return builder.toString();
    }

    public void serializeOntologies() {
//        serializeTermRelationships();
        for (Ontology ontology : Ontology.getSerializableOntologies()) {
            if (!ontology.isComposedOntologies()) {
                serializeOntology(ontology);
            }
        }
    }

    public void deserializeOntologies() throws Exception {
//        deserializeRelationships();
//        deserializeInfrastructureFiles();
        deserializeLoadingStatistic();
        for (Ontology ontology : Ontology.getSerializableOntologies()) {
            if (!ontology.isComposedOntologies()) {
                deserializeOntology(DTOConversionService.convertToOntologyDTO(ontology));
            }
        }
    }

//    public void deserializeInfrastructureFiles() throws Exception {
//        deserializeLoadingStatistic();
////        deserializeTransitiveClosure();
////        deserializeRelationshipTypes();
//    }

//    private void deserializeRelationshipTypes() throws Exception {
//        long start = System.currentTimeMillis();
//        File lookupFile;
//        try {
//            lookupFile = FileUtil.createOntologySerializationFile(RELATIONSHIP_TYPES_SER);
//            if (!lookupFile.exists() || !lookupFile.canRead()) {
//                throw new IOException("Lookup file does not exist or has bad permissions: " + lookupFile.getAbsolutePath());
//            }
//            logger.info("Lookup file: " + lookupFile + " size:" + lookupFile.length() + " last modified: " + new Date(lookupFile.lastModified()));
//
//            distinctRelationshipTypes = (Map<OntologyDTO, Set<String>>) FileUtil.deserializeOntologies(lookupFile);
//        } catch (Exception e) {
//            logger.error("Failed to deserialize the files", e);
//            throw e;
//        }
//        long end = System.currentTimeMillis();
//        double time = (double) (end - start) / MILLISECONDS_PER_SECOND;
//
//        logger.info("Time to deserialize ontology[" + ALL_RELATIONSHIPS + "]: " + time + " seconds.");
//    }

    /**
     * Retrieve the relationships between terms from a serialized file.
     *
     * @throws Exception throws exception when deserialization fails.
     */
//    public void deserializeRelationships() throws Exception {
//        long start = System.currentTimeMillis();
//        File lookupFile;
//        try {
//            lookupFile = FileUtil.createOntologySerializationFile(ALL_RELATIONSHIPS);
//            if (!lookupFile.exists() || !lookupFile.canRead()) {
//                throw new IOException("Lookup file does not exist or has bad permissions: " + lookupFile.getAbsolutePath());
//            }
//            logger.info("Lookup file: " + lookupFile + " size:" + lookupFile.length() + " last modified: " + new Date(lookupFile.lastModified()));
//
//            allRelationships = (Map<String, List<TermRelationship>>) FileUtil.deserializeOntologies(lookupFile);
//        } catch (Exception e) {
//            logger.error("Failed to deserialize the files", e);
//            throw e;
//        }
//        long end = System.currentTimeMillis();
//        double time = (double) (end - start) / MILLISECONDS_PER_SECOND;
//
//        logger.info("Time to deserialize ontology[" + ALL_RELATIONSHIPS + "]: " + time + " seconds.");
//    }


    /**
     * Here, we serialize 2 objects
     *
     * @param ontology Ontology.
     * @throws IOException Thrown if problem writing to file.
     */
    @SuppressWarnings("unchecked")
    public void deserializeOntology(OntologyDTO ontology) throws Exception {
        long start = System.currentTimeMillis();
        File lookupFile;
        try {
            lookupFile = FileUtil.createOntologySerializationFile(ontology.name() + SERIALIZED_LOOKUP_SUFFIX);
            if (!lookupFile.exists() || !lookupFile.canRead()) {
                throw new IOException("Lookup file does not exist or has bad permissions: " + lookupFile.getAbsolutePath());
            }
            logger.info("Lookup file: " + lookupFile + " size:" + lookupFile.length() + " last modified: " + new Date(lookupFile.lastModified()));

            PatriciaTrieMultiMap<TermDTO> lookupMap =
                    (PatriciaTrieMultiMap<TermDTO>) FileUtil.deserializeOntologies(lookupFile);
            lookupMap.rebuild();
            ontologyTermDTOMap.remove(ontology);
            ontologyTermDTOMap.put(ontology, lookupMap);
        } catch (Exception e) {
            logger.error("Failed to deserialize the files", e);
            throw e;
        }
        long end = System.currentTimeMillis();
        double time = (double) (end - start) / MILLISECONDS_PER_SECOND;

        logger.info("Time to deserialize ontology[" + ontology.name() + "]: " + time + " seconds.");
    }


    /**
     * Deserialize the OBO id - term id mapping.
     */
    public void deserializeLoadingStatistic() throws Exception {
        long start = System.currentTimeMillis();
        File idFile;
        try {
            // load id obo mapping
            idFile = FileUtil.createOntologySerializationFile(LOADING_STATS_SER);
            if (!idFile.exists() || !idFile.canRead()) {
                throw new IOException("file does not exist or has bad permissions: " + idFile.getAbsolutePath());
            }
            logger.info("file: " + idFile + " size:" + idFile.length() + " last modified: " + new Date(idFile.lastModified()));
            loadingData = (Map<OntologyDTO, OntologyLoadingEntity>) FileUtil.deserializeOntologies(idFile);
            logger.info(LOADING_STATS_SER + " file: " + idFile + " size:" + idFile.length() + " last modified: " + new Date(idFile.lastModified()));

        } catch (Exception e) {
            logger.error("Failed to deserialize the files", e);
            throw e;
        }
        long end = System.currentTimeMillis();
        double time = (double) (end - start) / MILLISECONDS_PER_SECOND;
        logger.info("Time to deserialize loading statistics: " + time + " seconds.");
    }

    /**
     * Deserialize the OBO id - term id mapping.
     */
//    public void deserializeTransitiveClosure() throws Exception {
//        long start = System.currentTimeMillis();
//        File idFile;
//        try {
//            // load id obo mapping
//            idFile = FileUtil.createOntologySerializationFile(TRANSITIVE_CLOSURE_SER);
//            if (!idFile.exists() || !idFile.canRead()) {
//                throw new IOException("idFile file does not exist or has bad permissions: " + idFile.getAbsolutePath());
//            }
//            logger.info("idFile file: " + idFile + " size:" + idFile.length() + " last modified: " + new Date(idFile.lastModified()));
//            allRelatedChildrenMap = (Map<TermDTO, List<TransitiveClosure>>) FileUtil.deserializeOntologies(idFile);
//            logger.info(TRANSITIVE_CLOSURE_SER + " file: " + idFile + " size:" + idFile.length() + " last modified: " + new Date(idFile.lastModified()));
//
//        } catch (Exception e) {
//            logger.error("Failed to deserialize the files", e);
//            throw e;
//        }
//        long end = System.currentTimeMillis();
//        double time = (double) (end - start) / MILLISECONDS_PER_SECOND;
//        logger.info("Time to deserialize obo id mapping: " + time + " seconds.");
//    }

    /**
     * Here, we serialize 2 objects
     *
     * @param ontology Ontology.
     */
    public void serializeOntology(Ontology ontology) {
        long start = System.currentTimeMillis();
        PatriciaTrieMultiMap<TermDTO> mapToSerialize = ontologyTermDTOMap.get(DTOConversionService.convertToOntologyDTO(ontology)) ;
        File lookupFile = FileUtil.serializeObject(mapToSerialize,
                FileUtil.createOntologySerializationFile(ontology.name() + SERIALIZED_LOOKUP_SUFFIX));
        long end = System.currentTimeMillis();
        double time = (double) (end - start) / MILLISECONDS_PER_SECOND;

        logger.info("Time to serialize ontology[" + ontology.name() + "]: " + time + " seconds.");
        logger.info("Lookup file path[" + lookupFile.getAbsolutePath() + "] size: " + (lookupFile.length() / 1024) + "kB");
    }

    public void serializeObject(Object object, String fileName) {
        if (object == null || fileName == null)
            return;

        if (ontologyManager == null)
            getInstance();
        long start = System.currentTimeMillis();
        File file = FileUtil.serializeObject(object,
                FileUtil.createOntologySerializationFile(fileName));
        long end = System.currentTimeMillis();
        double time = (double) (end - start) / MILLISECONDS_PER_SECOND;
        logger.info("Time to serialize " + fileName + " [" + object.getClass().getSimpleName() + "]: " + time + " seconds.");
        logger.info("Lookup file path [" + file.getAbsolutePath() + "] size: " + (file.length() / 1024) + "kB");
    }

    /**
     * Retrieve a term by term name and a list of ontologies.
     * The logic loops over all ontologies and returns the term from the ontology
     * in which it is found first. If no term is found a null is returned.
     *
     *
     * @param termName      term name
     * @param ontologies    collection of ontologies
     * @param allowObsolete True if an obsolete term may be returned.
     * @return term object
     */
    public TermDTO getTermByName(String termName, List<Ontology> ontologies, boolean allowObsolete) {
        if (ontologies == null)
            return null;

        for (Ontology ontology : ontologies) {
            TermDTO term = getTermByName(termName, ontology, allowObsolete);
            if (term != null)
                return term;
        }
        return null;
    }

    /**
     * Retrieve a term by term name and a list of ontologies.
     * The logic loops over all ontologies and returns the term from the ontology
     * in which it is found first. If no term is found a null is returned.
     *
     *
     * @param termName   term name
     * @param ontologies collection of ontologies
     * @return term object
     */
    public TermDTO getTermByName(String termName, List<Ontology> ontologies) {
        return getTermByName(termName, ontologies, false);
    }

    public Set<OntologyDTO> getOntologies(Ontology ontology) {
        Set<OntologyDTO> ontologyDTOs = new HashSet<OntologyDTO>() ;

        if (ontology.isComposedOntologies()) {
            for (Ontology subOntology : ontology.getIndividualOntologies()) {
                ontologyDTOs.add(DTOConversionService.convertToOntologyDTO(subOntology)) ;
            }
        }
        else {
            ontologyDTOs.add(DTOConversionService.convertToOntologyDTO(ontology)) ;
        }
        return ontologyDTOs ;
    }

    public PatriciaTrieMultiMap<TermDTO> getTermsForOntology(OntologyDTO ontologyDTO) {
        return ontologyTermDTOMap.get(ontologyDTO);
    }

    public void initRootOntologyFast(String rootZdbID,Ontology ontology){
        resetCounter();

        TermDTO termDTO = getTermByID(rootZdbID);
        TermDTO rootDTO = new TermDTO();

        rootDTO.shallowCopyFrom(termDTO);
        Set<TermDTO> termsToProcess = new HashSet<TermDTO>();
        termsToProcess.add(rootDTO);

        Set<String> childZdbIDs = RepositoryFactory.getOntologyRepository().getAllChildZdbIDs(termDTO.getZdbID());

        for(String childZdbID : childZdbIDs){
            termsToProcess.add(getTermByID(childZdbID));
        }

        createMapForTerms(termsToProcess,ontology);
    }

    public void initQualityProcessesRootOntology() {
//        ontologyManager.initRootOntologyMap(Ontology.QUALITY_PROCESSES,Ontology.QUALITY, "PATO:0001236");

        initRootOntologyFast(QUALITY_PROCESSES_ROOT, Ontology.QUALITY_PROCESSES);
    }

    public void initQualityQualitiesRootOntology() {
        //To change body of created methods use File | Settings | File Templates.
    }


    public enum LoadingMode {
        DATABASE, SERIALIZED_FILE
    }
}

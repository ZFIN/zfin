package org.zfin.ontology;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.gwt.root.dto.OntologyDTO;
import org.zfin.gwt.root.dto.RelationshipType;
import org.zfin.gwt.root.dto.StageDTO;
import org.zfin.gwt.root.dto.TermDTO;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.infrastructure.PatriciaTrieMultiMap;
import org.zfin.ontology.presentation.OntologyLoadingEntity;
import org.zfin.ontology.service.OntologyService;
import org.zfin.repository.RepositoryFactory;
import org.zfin.util.DateUtil;

import java.util.*;

import static org.zfin.repository.RepositoryFactory.getOntologyRepository;

/**
 * This class manages all ontologies used in ZFIN for lookup.
 * It is initialized once and stores all ontologies in static maps.
 * For each ontology there will be two maps:<br/>
 * 1) a map with (term name, term)<br/>
 * 2) a map with (alias, term)<br/>
 * This allows to retrieve matches against a term name and an alias.
 * <p/>
 * NOTE: This class should ONLY used CachedTermDTO!!! never GenericTermDTO
 */
public class OntologyManager {

    protected final static String QUALITY_PROCESSES_ROOT = "PATO:0001236";
    protected final static String QUALITY_QUALITIES_ROOT = "PATO:0001241";
    protected static final String QUALITATIVE_TERM = "PATO:0000068";
    protected static final String QUALITY_TERM_NORMAL = "PATO:0000461";

    public static final int NUMBER_OF_SERIALIZABLE_ONTOLOGIES = Ontology.getSerializableOntologies().length;
    private static Map<OntologyDTO, PatriciaTrieMultiMap<TermDTO>> ontologyTermDTOMap = new HashMap<OntologyDTO, PatriciaTrieMultiMap<TermDTO>>(NUMBER_OF_SERIALIZABLE_ONTOLOGIES);
    private OntologyTokenizer tokenizer = new OntologyTokenizer(3);

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

    private static Ontology singleOntology = null;
    private Map<Ontology, Set> excludedTerms = new HashMap<Ontology, Set>(5);
    private static OntologySerializationService ontologySerializationService;

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
    protected OntologyManager() {
    }

    public static OntologyManager getEmptyInstance() {

        if (ontologyManager == null) {
            ontologyManager = new OntologyManager();
            ontologySerializationService = new OntologySerializationService(ontologyManager);
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
        ontologySerializationService = new OntologySerializationService(ontologyManager);
        try {
            ontologyManager.deserializeOntologies();
        } catch (Exception e) {
            logger.warn("Problem loading serialized file. Try loading ontologies from database...", e);
            init();
        }
        return ontologyManager;
    }

    public static OntologyManager getInstanceFromFile(OntologyDTO ontologyDto) throws Exception {
        if (ontologyManager == null) {
            ontologyManager = new OntologyManager();
            ontologySerializationService = new OntologySerializationService(ontologyManager);
        }
        Ontology ontology = Ontology.getOntology(ontologyDto.getDBName());
        ontologySerializationService.deserializeOntology(ontology);
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
            if (ontologySerializationService == null)
                ontologySerializationService = new OntologySerializationService(ontologyManager);
            ontologyManager.loadOntologiesFromDatabase();
        }
        logger.info("Finished loading all ontologies: took " + DateUtil.getTimeDuration(startTime));
    }

    public static Collection<TermDTO> populateRelationships(Map<String, TermDTO> termDTOMap){

        // pass two fills in the rest of the child / parent type info
        for(TermDTO termDTO : termDTOMap.values()){

            // populate child
            if(termDTO.getChildrenTerms()!=null){
                for(TermDTO childTerm : termDTO.getChildrenTerms()){
                    TermDTO cachedTerm = termDTOMap.get(childTerm.getZdbID()) ;
                    if(cachedTerm == null){
                        cachedTerm = ontologyManager.getTermByID(childTerm.getZdbID());
                    }

                    if(cachedTerm==null){
                        logger.error("Term is not cached, will create bad cache: "+childTerm);
                    }
                    else{
                        childTerm.shallowCopyFrom(cachedTerm);
                    }
                }
            }


            // populate parent term
            if(termDTO.getParentTerms()!=null){
                for(TermDTO parentTerm : termDTO.getParentTerms()){
                    // for the purpose of working with anatomy, the stage parent is not always in the ontology
                    TermDTO cachedTerm = termDTOMap.get(parentTerm.getZdbID()) ;
                    if(cachedTerm == null){
                        cachedTerm = ontologyManager.getTermByID(parentTerm.getZdbID());
                    }

                    if(cachedTerm==null){
                        logger.error("Term is not cached, will create bad cache: "+parentTerm);
                    }
                    else{
                        parentTerm.shallowCopyFrom(cachedTerm);

                        // handle anatomy here
                        if(parentTerm.getRelationshipType().equals(RelationshipType.START_STAGE.getDbMappedName())){
                            StageDTO stageDTO = new StageDTO();
                            stageDTO.setZdbID(parentTerm.getZdbID());
                            stageDTO.setOboID(parentTerm.getOboID());
                            stageDTO.setName(parentTerm.getName());
                            termDTO.setStartStage(stageDTO);
                        }
                        else
                        if(parentTerm.getRelationshipType().equals(RelationshipType.END_STAGE.getDbMappedName())){
                            StageDTO stageDTO = new StageDTO();
                            stageDTO.setZdbID(parentTerm.getZdbID());
                            stageDTO.setOboID(parentTerm.getOboID());
                            stageDTO.setName(parentTerm.getName());
                            termDTO.setEndStage(stageDTO);
                        }
                    }
                }
            }
        }

        return termDTOMap.values();  //To change body of implemented methods use File | Settings | File Templates.
    }

    protected void loadOntologiesFromDatabase() {
        if (singleOntology != null) {
            initOntologyMapFast(singleOntology);
            ontologySerializationService.serializeOntology(singleOntology);
        } else {
            loadDefaultOntologiesFromDatabase();
        }
    }


    private void loadDefaultOntologiesFromDatabase() {
        initOntologyMapFast(Ontology.SPATIAL);
        serializeOntology(Ontology.SPATIAL);

        initOntologyMapFastNoRelations(Ontology.STAGE);
        serializeOntology(Ontology.STAGE);

        initOntologyMapFast(Ontology.ANATOMY);
        serializeOntology(Ontology.ANATOMY);

        initOntologyMapFast(Ontology.QUALITY);
        serializeOntology(Ontology.QUALITY);

        // Quality  Processes and Objects
        // Root is "process quality"
        // Quality Processes and Objects
        // exclude 'normal' term from process and quality ontology.
        Set<String> excludedTermsIds = new HashSet<String>(2);
        excludedTermsIds.add(QUALITY_TERM_NORMAL);
        excludedTerms.put(Ontology.QUALITY_PROCESSES, excludedTermsIds);
        excludedTerms.put(Ontology.QUALITY_QUALITIES, excludedTermsIds);
        initRootOntologyFast(Ontology.QUALITY_PROCESSES, QUALITY_PROCESSES_ROOT, QUALITATIVE_TERM);
        serializeOntology(Ontology.QUALITY_PROCESSES);
        initRootOntologyFast(Ontology.QUALITY_QUALITIES, QUALITY_QUALITIES_ROOT, QUALITATIVE_TERM);
        serializeOntology(Ontology.QUALITY_QUALITIES);

        // GO ontologies
        initOntologyMapFast(Ontology.GO_CC);
        serializeOntology(Ontology.GO_CC);
        initOntologyMapFast(Ontology.GO_MF);
        serializeOntology(Ontology.GO_MF);
        initOntologyMapFast(Ontology.GO_BP);
        serializeOntology(Ontology.GO_BP);

        ontologySerializationService.serializeLoadData(loadingData);
    }

    /**
     * @param subOntology
     * @param rootOntology
     * @param rootOboIDs
     * @deprecated
     */
    protected void initRootOntologyMap(Ontology subOntology, Ontology rootOntology, String... rootOboIDs) {
        long startTime = System.currentTimeMillis();
        Date dateStarted = new Date();
        int averageMaximumNumOfChildren = 30;

        List<TermDTO> children = new ArrayList<TermDTO>(averageMaximumNumOfChildren);
        Set<TermDTO> childrenSet = new HashSet<TermDTO>(averageMaximumNumOfChildren);
        for (String rootOboID : rootOboIDs) {
            Term rootTerm = getOntologyRepository().getTermByOboID(rootOboID);
            for (Term childTerm : rootTerm.getChildTerms()) {
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
            Set excludedTermIds = excludedTerms.get(subOntology);
            if (excludedTermIds != null) {
                // do not add excluded terms or any of its children.
                if (excludedTermIds.contains(currentTermDTO.getOboID()))
                    continue;
            }
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
        ontologySerializationService.serializeOntology(subOntology);
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
     * @param ontology
     */
    public void initOntologyMapFastNoRelations(Ontology ontology) {
        resetCounter();
        Collection<TermDTO> termDTOs = getOntologyRepository().getTermDTOsFromOntologyNoRelation(ontology);
        createMapForTerms(termDTOs, ontology);
    }

    private PatriciaTrieMultiMap<TermDTO> createMapForTerms(Collection<TermDTO> termDTOs, Ontology ontology) {
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
        Map<String, TermDTO> termDTOMap = getOntologyRepository().getTermDTOsFromOntology(ontology);
        Collection<TermDTO> termDTOs = populateRelationships(termDTOMap);
        createMapForTerms(termDTOs, ontology);
    }

    /**
     * @param terms
     * @param ontology
     * @deprecated
     */
    public void loadTermsIntoOntology(List<GenericTerm> terms, Ontology ontology) {

        long nextTime = System.currentTimeMillis();

        PatriciaTrieMultiMap<TermDTO> termMap = new PatriciaTrieMultiMap<TermDTO>();
        if (terms == null) {
            logger.info("No terms for ontology <" + ontology.getOntologyName() + "> found.");
            return;
        }

        for (Term term : terms) {
            if (term.getOntology() == Ontology.STAGE) {
                tokenizer.tokenizeTerm(DTOConversionService.convertToTermDTO(term), termMap);
            } else {
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

        endTime = System.currentTimeMillis();
        logLoading(ontology, startTime, endTime, dateStarted, activeCount, obsoleteCount, aliasCount,
                termMap.keySet().size(), termMap.getAllValues().size());
        ontologySerializationService.serializeOntology(ontology);
    }


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
     * @param id       term ID
     * @param ontology ontology
     * @return term
     */
    public TermDTO getTermByID(String id, OntologyDTO ontology) {
        Set<TermDTO> terms = ontologyTermDTOMap.get(ontology).get(id);
        if (terms != null && terms.size() > 0) {
            if (terms.size() != 1) {
                logger.warn("multiple terms [" + terms.size() + "] returned for termID: " + id);
            }
            // return if an exact match, otherwise, return the first one
            for (TermDTO t : terms) {
                // name is the only thing that will register multiple hits
                if (t.getName().equals(id)) {
                    return t;
                }
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
                PatriciaTrieMultiMap<TermDTO> termMap = ontologyTermDTOMap.get(DTOConversionService.convertToOntologyDTO(subOntology));
                if (termMap != null && termMap.get(lookupValue) != null) {
                    terms.addAll(termMap.get(lookupValue));
                }
            }
        } else {
            PatriciaTrieMultiMap<TermDTO> termMap = ontologyTermDTOMap.get(DTOConversionService.convertToOntologyDTO(ontology));
            if (termMap != null && termMap.get(lookupValue) != null) {
                terms.addAll(termMap.get(lookupValue));
            }
        }


        if (terms.size() == 0) {
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
     * @param termName term name
     * @param ontology ontology
     * @return term
     */
    public TermDTO getTermByName(String termName, Ontology ontology) {
        return getTermByName(termName, ontology, false);
    }


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
        for (Ontology ontology : Ontology.getSerializableOntologies()) {
            if (!ontology.isComposedOntologies()) {
                ontologySerializationService.serializeOntology(ontology);
            }
        }
    }

    public void deserializeOntologies() throws Exception {
        deserializeInfrastructureFiles();
        for (Ontology ontology : Ontology.getSerializableOntologies()) {
            if (!ontology.isComposedOntologies()) {
                ontologySerializationService.deserializeOntology(ontology);
            }
        }
    }

    public void deserializeInfrastructureFiles() throws Exception {
        ontologySerializationService.deserializeLoadingStatistic();
    }

    /**
     * Retrieve a term by term name and a list of ontologies.
     * The logic loops over all ontologies and returns the term from the ontology
     * in which it is found first. If no term is found a null is returned.
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
     * @param termName   term name
     * @param ontologies collection of ontologies
     * @return term object
     */
    public TermDTO getTermByName(String termName, List<Ontology> ontologies) {
        return getTermByName(termName, ontologies, false);
    }

    public Set<OntologyDTO> getOntologies(Ontology ontology) {
        Set<OntologyDTO> ontologyDTOs = new HashSet<OntologyDTO>();

        if (ontology.isComposedOntologies()) {
            for (Ontology subOntology : ontology.getIndividualOntologies()) {
                ontologyDTOs.add(DTOConversionService.convertToOntologyDTO(subOntology));
            }
        } else {
            ontologyDTOs.add(DTOConversionService.convertToOntologyDTO(ontology));
        }
        return ontologyDTOs;
    }

    public PatriciaTrieMultiMap<TermDTO> getTermsForOntology(OntologyDTO ontologyDTO) {
        return ontologyTermDTOMap.get(ontologyDTO);
    }

    /**
     * Create slim ontology from a given ontology and given root terms including all their children.
     *
     * @param ontology Ontology
     * @param rootIDs  root terms to be included in the slim.
     */
    public void initRootOntologyFast(Ontology ontology, String... rootIDs) {
        if (ontology == null || rootIDs == null)
            return;

        // get root ontology
        Ontology rootOntology = ontology.getRootOntology();
        OntologyDTO rootOntologyDTO = DTOConversionService.convertToOntologyDTO(rootOntology);
        resetCounter();
        Set<TermDTO> termsToProcess = new HashSet<TermDTO>();
        for (String rootID : rootIDs) {
            TermDTO termDTO = getTermByID(rootID, rootOntologyDTO);
            termsToProcess.add(termDTO);

            TermDTO rootDTO = new TermDTO();
            rootDTO.shallowCopyFrom(termDTO);
            termsToProcess.add(rootDTO);
            Set<String> childZdbIDs = RepositoryFactory.getOntologyRepository().getAllChildZdbIDs(termDTO.getZdbID());

            for (String childZdbID : childZdbIDs) {
                TermDTO childTerm = getTermByID(childZdbID, rootOntologyDTO);
                Set excludedTermIds = excludedTerms.get(ontology);
                if (excludedTermIds != null) {
                    // do not add excluded terms or any of its children.
                    if (excludedTermIds.contains(childTerm.getOboID())) {
                        logger.info("Excluded Term: " + childTerm);
                        continue;
                    }
                }
                termsToProcess.add(childTerm);
            }
        }
        createMapForTerms(termsToProcess, ontology);
    }

    public void initQualityProcessesRootOntology() {
        initRootOntologyFast(Ontology.QUALITY_PROCESSES, QUALITY_PROCESSES_ROOT);
    }

    public void initQualityQualitiesRootOntology() {
        //To change body of created methods use File | Settings | File Templates.
    }


    void setLoadingData(Map<OntologyDTO, OntologyLoadingEntity> loadingData) {
        this.loadingData = loadingData;
    }

    public Set<TermDTO> getTermsByNames(String termName) {
        Set<TermDTO> terms = new HashSet<TermDTO>(5);
        for (Ontology ontology : Ontology.values()) {
            TermDTO term = getTermByName(termName, ontology);
            if (term != null)
                terms.add(term);
        }
        return terms;
    }

    public void deserializeOntology(Ontology ontology) throws Exception {
        if (ontologySerializationService == null)
            ontologySerializationService = new OntologySerializationService(this);
        ontologySerializationService.deserializeOntology(ontology);
    }

    public void serializeOntology(Ontology ontology) {
        if (ontologySerializationService == null)
            ontologySerializationService = new OntologySerializationService(this);
        ontologySerializationService.serializeOntology(ontology);
    }

    /**
     * Reload a given ontology from the database and serialize it to disk.
     * If the ontology is PATO also re-load the slims.
     *
     * @param ontology Ontology
     */
    public void reloadOntology(Ontology ontology) {
        if (ontology.equals(Ontology.ANATOMY_FULL)) {
            reloadOntology(Ontology.ANATOMY);
            reloadOntology(Ontology.STAGE);
            return;
        }
        if (ontology.equals(Ontology.GO_ONTOLOGY)) {
            reloadOntology(Ontology.GO_CC);
            reloadOntology(Ontology.GO_MF);
            reloadOntology(Ontology.GO_BP);
            return;
        }
        initOntologyMapFast(ontology);
        serializeOntology(ontology);
        if (ontology.equals(Ontology.QUALITY)) {
            initRootOntologyFast(Ontology.QUALITY_PROCESSES, QUALITY_PROCESSES_ROOT, QUALITATIVE_TERM);
            serializeOntology(Ontology.QUALITY_PROCESSES);
            initRootOntologyFast(Ontology.QUALITY_QUALITIES, QUALITY_QUALITIES_ROOT, QUALITATIVE_TERM);
            serializeOntology(Ontology.QUALITY_QUALITIES);
        }
    }

    public enum LoadingMode {
        DATABASE, SERIALIZED_FILE
    }
}

package org.zfin.ontology;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.PatriciaTrieMultiMap;
import org.zfin.ontology.presentation.OntologyLoadingEntity;
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
 */
public class OntologyManager {

    public static final int NUMBER_OF_SERIALIZABLE_ONTOLOGIES = Ontology.getSerializableOntologies().length;
    private static Map<Ontology, PatriciaTrieMultiMap<Term>> ontologyTermMap = new HashMap<Ontology, PatriciaTrieMultiMap<Term>>(NUMBER_OF_SERIALIZABLE_ONTOLOGIES);
    // holds all relationships between terms.
    // this object only exists during initialization of the ontologies.
    // The helper class does not reference the GenericTerm object rather the PKs only to allow this
    // object to be serialized. Since every term has a relationship to all children and parents, through the
    // parents and children relationships each term is indirectly related to all terms in the Ontology
    // which would upon serialization lead to infinite loops and ultimately a StackOverflowError
    private Map<String, List<TermRelationshipHelper>> allRelationships;
    // distinct relationship Types used in an ontology
    private Map<Ontology, Set<String>> distinctRelationshipTypes = new HashMap<Ontology, Set<String>>(NUMBER_OF_SERIALIZABLE_ONTOLOGIES);
    private OntologyTokenizer tokenizer = new OntologyTokenizer(3);
    // name of serialized file.

    /**
     * A map of all terms as a key and a list of terms that are children of the key term given by the transitive closure.
     */
    private Map<Term, List<TransitiveClosure>> allRelatedChildrenMap;
    private Map<Ontology, OntologyLoadingEntity> loadingData = new TreeMap<Ontology, OntologyLoadingEntity>(new OntologyNameComparator());
    // sole singleton instance
    private static OntologyManager ontologyManager = null;
    transient private static final Logger logger = Logger.getLogger(OntologyManager.class);

    private static final double MILLISECONDS_PER_SECOND = 1000.0;

    private static Map<Ontology, Double> loadingTimeMap = new HashMap<Ontology, Double>(10);
    private static final Object LOADING_FLAG = new Object();
    public static final String SERIALIZED_LOOKUP_SUFFIX = "-lookup.ser";
    private final static String ALL_RELATIONSHIPS = "all_relationships.ser";
    public static final String LOADING_STATS_SER = "loading-statistics.ser";
    public static final String TRANSITIVE_CLOSURE_SER = "transitive-closure.ser";
    public static final String RELATIONSHIP_TYPES_SER = "relationship-types.ser";

    private static Ontology singleOntology = null;

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

    public static OntologyManager getInstanceFromFile(Ontology ontology) throws Exception {
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
            List<TermRelationshipHelper> relationships = getOntologyRepository().getAllRelationships();
            allRelationships = createRelationshipsMap(relationships);
            initSingleOntologyMap(singleOntology);
            serializeOntology(singleOntology);
            if (singleOntology == Ontology.ANATOMY) {
                loadTransitiveClosure();
            }
        } else {
            loadDefaultOntologies();
            loadTransitiveClosure();
        }
    }

    /**
     * Load the transitive closure, the object that contains the children terms for each term in all ontologies.
     * Currently, we only have the closure info for the Anatomy ontology.
     */
    private void loadTransitiveClosure() {
        List<TransitiveClosure> closures = getOntologyRepository().getTransitiveClosure();
        if (closures == null || closures.size() == 0)
            return;

        // for now we hard-code the size to the size of the anatomy ontology.
        allRelatedChildrenMap = new HashMap<Term, List<TransitiveClosure>>(getTermOntologyMap(Ontology.ANATOMY).size());
        for (TransitiveClosure closure : closures) {
            Term root = closure.getRoot();
            Term omRoot = getTermByID(root.getOntology(), root.getID());
            List<TransitiveClosure> children = allRelatedChildrenMap.get(omRoot);
            if (children == null)
                children = new ArrayList<TransitiveClosure>(10);
            children.add(closure);
            Collections.sort(children);
            allRelatedChildrenMap.put(omRoot, children);
        }
        logger.info("Loading Transitive closure for  " + allRelatedChildrenMap.size() + " terms. The total number of connections is " + closures.size());
    }

    private void loadDefaultOntologies() {
        // load relationships
        loadAndSerializeAllTermRelationships();
        // Spatial Modifier
        initSingleOntologyMap(Ontology.SPATIAL);
        serializeOntology(Ontology.SPATIAL);

        initSingleOntologyMap(Ontology.STAGE);
        serializeOntologyInThread(Ontology.STAGE);
        initSingleOntologyMap(Ontology.ANATOMY);
        serializeOntologyInThread(Ontology.ANATOMY);
        initSingleOntologyMap(Ontology.QUALITY);
        serializeOntologyInThread(Ontology.QUALITY);

        // Quality  Processes and Objects
        initRootOntologyMap(Ontology.QUALITY_PROCESSES, Ontology.QUALITY, "PATO:0001236");
        serializeOntologyInThread(Ontology.QUALITY_PROCESSES);
        initRootOntologyMap(Ontology.QUALITY_QUALITIES, Ontology.QUALITY, "PATO:0001241");
        serializeOntologyInThread(Ontology.QUALITY_QUALITIES);

        // GO ontology
        initSingleOntologyMap(Ontology.GO_CC);
        serializeOntologyInThread(Ontology.GO_CC);
        initSingleOntologyMap(Ontology.GO_MF);
        serializeOntologyInThread(Ontology.GO_MF);
        initSingleOntologyMap(Ontology.GO_BP);
        serializeOntologyInThread(Ontology.GO_BP);
        HibernateUtil.currentSession().clear();

        serializeObject(loadingData, LOADING_STATS_SER);
        // transitive closure
        loadTransitiveClosure();
        serializeObject(allRelatedChildrenMap, TRANSITIVE_CLOSURE_SER);
        serializeObject(distinctRelationshipTypes, RELATIONSHIP_TYPES_SER);
    }

    private void serializeOntologyInThread(final Ontology ontology) {
        new Thread() {
            @Override
            public void run() {
                serializeOntology(ontology);
            }
        }.start();
    }

    private void loadAndSerializeAllTermRelationships() {
        long start = System.currentTimeMillis();
        List<TermRelationshipHelper> relationships = getOntologyRepository().getAllRelationships();
        allRelationships = createRelationshipsMap(relationships);
        long end = System.currentTimeMillis();
        double time = (double) (end - start) / MILLISECONDS_PER_SECOND;
        logger.info("Time to load relationships from database: " + time + " seconds.");
        start = System.currentTimeMillis();
        File lookupFile = FileUtil.serializeObject(allRelationships,
                FileUtil.createOntologySerializationFile(ALL_RELATIONSHIPS));
        end = System.currentTimeMillis();
        time = (double) (end - start) / MILLISECONDS_PER_SECOND;

        logger.info("Time to serialize relationships: " + time + " seconds.");
        logger.info("Lookup file path[" + lookupFile.getAbsolutePath() + "] size: " + (lookupFile.length() / 1024) + "kB");

    }

    private void initRootOntologyMap(Ontology subOntology, Ontology rootOntology, String... rootOboIDs) {
        long startTime = System.currentTimeMillis();
        Date dateStarted = new Date();
        int averageMaximumNumOfChildren = 30;

        List<Term> children = new ArrayList<Term>(averageMaximumNumOfChildren);
        Set<Term> childrenSet = new HashSet<Term>(averageMaximumNumOfChildren);
        for (String rootOboID : rootOboIDs) {
            Term rootTerm = getOntologyRepository().getTermByOboID(rootOboID);
            children.addAll(rootTerm.getChildrenTerms());
            childrenSet.addAll(rootTerm.getChildrenTerms());
        }
        PatriciaTrieMultiMap<Term> termMap = new PatriciaTrieMultiMap<Term>();
        int obsoleteCount = 0;
        int aliasCount = 0;
        int activeCount = 0;
        while (childrenSet.size() > 0) {
            Term currentTerm = childrenSet.iterator().next();
            childrenSet.remove(currentTerm);
            List<Term> newChildren = currentTerm.getChildrenTerms();
            if (newChildren != null && !newChildren.isEmpty()) {
                for (Term child : newChildren) {
                    if (children.contains(child)){
                        logger.info("Term already processed: " + child.getTermName() + " [" + child.getID());
                    }else{
                        children.add(child);
                        childrenSet.add(child);
                    }
                }
            }
            Term term = getTermByID(currentTerm.getOntology(), currentTerm.getID());
            if (term == null)
                logger.error("Child Term <" + currentTerm.getID() + "> not found in Root ontology " + rootOntology.getOntologyName());
            else {
                tokenizer.tokenizeTerm(currentTerm, termMap);
                if (currentTerm.isObsolete()) {
                    ++obsoleteCount;
                } else {
                    ++activeCount;
                }
                if (term.getAliases() != null) {
                    aliasCount += term.getAliases().size();
                }
            }
        }
        ontologyTermMap.put(subOntology, termMap);

        long endTime = System.currentTimeMillis();
        double loadingTimeInSeconds = (double) (endTime - startTime) / MILLISECONDS_PER_SECOND;
        logLoading(subOntology, startTime, endTime, dateStarted, activeCount, obsoleteCount, aliasCount,
                termMap.keySet().size(), termMap.getAllValues().size());
        loadingTimeMap.put(subOntology, loadingTimeInSeconds);
    }


    /**
     * Load a single ontology.
     *
     * @param ontology Ontology
     */
    public void initSingleOntologyMap(Ontology ontology) {
        long startTime = System.currentTimeMillis();
        Date dateStarted = new Date();
        List<Term> terms = getOntologyRepository().getAllTermsFromOntology(ontology);
        long nextTime = System.currentTimeMillis();
        logger.debug("time to load from DB: " + (nextTime - startTime));

        PatriciaTrieMultiMap<Term> termMap = new PatriciaTrieMultiMap<Term>();
        if (terms == null) {
            logger.info("No terms for ontology <" + ontology.getOntologyName() + "> found.");
            return;
        }

        int activeCount = 0;
        int aliasCount = 0;
        int obsoleteCount = 0;
        for (Term term : terms) {
            tokenizer.tokenizeTerm(term, termMap);
            if (term.isObsolete()) {
                ++obsoleteCount;
            } else {
                ++activeCount;
            }
            if (term.getAliases() != null) {
                aliasCount += term.getAliases().size();
            }

            if (term.getAliases() != null) {
                aliasCount += term.getAliases().size();
            }

        }
        logger.debug("to put in hashmap: " + (System.currentTimeMillis() - nextTime) / MILLISECONDS_PER_SECOND + "s");
        nextTime = System.currentTimeMillis();

        ontologyTermMap.put(ontology, termMap);
        logger.debug("to have loaded ontologies : " + (System.currentTimeMillis() - nextTime) / MILLISECONDS_PER_SECOND + "s");
        nextTime = System.currentTimeMillis();

        populateRelationships(terms, ontology);
        logger.debug("Time to load relationships [" + ontology.getOntologyName() + "]: " + (System.currentTimeMillis() - nextTime) / MILLISECONDS_PER_SECOND + "s");
        nextTime = System.currentTimeMillis();

        logger.debug("calculate aliases: " + (System.currentTimeMillis() - nextTime) / MILLISECONDS_PER_SECOND + "s");
        nextTime = System.currentTimeMillis();
        populateStageInformation(ontology);

        logger.info("populate stage info: " + (System.currentTimeMillis() - nextTime) / MILLISECONDS_PER_SECOND + "s");

        long endTime = System.currentTimeMillis();
        logLoading(ontology, startTime, endTime, dateStarted, activeCount, obsoleteCount, aliasCount,
                termMap.keySet().size(), termMap.getAllValues().size());
    }

    /**
     * Only for those ontologies that are not the stage ontology.
     *
     * @param terms    all terms of a given ontology
     * @param ontology given ontology
     */
    private void populateRelationships(Iterable<Term> terms, Ontology ontology) {
        if (ontology == Ontology.STAGE)
            return;

        // populate the relationships onto each term
        for (Term term : terms) {
            List<TermRelationshipHelper> relationships = allRelationships.get(term.getID());
            if (relationships != null) {
                List<TermRelationship> relatedTerms = new ArrayList<TermRelationship>(relationships.size());
                for (TermRelationshipHelper helper : relationships) {
                    Term termOne = getTermByID(helper.getTermOneID());
                    Term termTwo = getTermByID(helper.getTermTwoID());
                    helper.setTermOne(termOne);
                    helper.setTermTwo(termTwo);
                    relatedTerms.add(helper);
                    // add relationship type to map
                    Set<String> relationshipTypes = distinctRelationshipTypes.get(ontology);
                    if (relationshipTypes == null)
                        relationshipTypes = new HashSet<String>(5);
                    relationshipTypes.add(helper.getType());
                    distinctRelationshipTypes.put(ontology, relationshipTypes);
                }
                term.setRelatedTerms(relatedTerms);
            }
        }
    }


    private Map<String, List<TermRelationshipHelper>> createRelationshipsMap(List<TermRelationshipHelper> relationships) {
        Map<String, List<TermRelationshipHelper>> map = new HashMap<String, List<TermRelationshipHelper>>(relationships.size());
        for (TermRelationshipHelper relationship : relationships) {
            List<TermRelationshipHelper> relListOne;
            relListOne = map.get(relationship.getTermOneID());
            if (relListOne == null) {
                int averageNumOfRelationships = 5;
                relListOne = new ArrayList<TermRelationshipHelper>(averageNumOfRelationships);
            }
            relListOne.add(relationship);
            map.put(relationship.getTermOneID(), relListOne);

            List<TermRelationshipHelper> relList;
            relList = map.get(relationship.getTermTwoID());
            if (relList == null) {
                int averageNumOfRelationships = 5;
                relList = new ArrayList<TermRelationshipHelper>(averageNumOfRelationships);
            }
            relList.add(relationship);
            map.put(relationship.getTermTwoID(), relList);
        }
        return map;
    }

    /**
     * Check if the ontology is anatomy and then get the stage info and set it onto the term object.
     *
     * @param ontology ontology
     */
    private void populateStageInformation(Ontology ontology) {
        // only anatomy terms have stages defined.
        if (!ontology.equals(Ontology.ANATOMY))
            return;

        Map<String, DevelopmentStage> developmentStageMap = getAllStageMap();
        for (Term term : getTermOntologyMap(ontology).getAllValues()) {
            List<TermRelationship> relatedTerms = term.getRelatedTerms();
            if (relatedTerms == null)
                continue;
            for (TermRelationship relatedTerm : relatedTerms) {
                // ToDo: temporary until we know why this relationship name is changed back and forth.
                if (relatedTerm.getType().equals("start stage") || relatedTerm.getType().equals("start")) {
                    String relatedTermOboId = relatedTerm.getRelatedTerm(term).getOboID();
                    DevelopmentStage start = developmentStageMap.get(relatedTermOboId);
                    term.setStart(start);
                } else if (relatedTerm.getType().equals("end stage") || relatedTerm.getType().equals("end")) {
                    String relatedTermOboId = relatedTerm.getRelatedTerm(term).getOboID();
                    DevelopmentStage end = developmentStageMap.get(relatedTermOboId);
                    term.setEnd(end);
                }
            }
        }
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
                            int numOfTerms, int numOfObsoleteTerms, int numOfAliases, int numKeys, int numValues) {
        long loadingTime = endTime - startTime;
        double loadingTimeSeconds = (double) loadingTime / MILLISECONDS_PER_SECOND;
        logger.info("Loading <" + ontology.getOntologyName() + "> ontology took " + loadingTimeSeconds + " seconds.");
        OntologyLoadingEntity loadingEntity = loadingData.get(ontology);
        if (loadingEntity == null) {
            loadingEntity = new OntologyLoadingEntity(ontology);
        }
        loadingEntity.addLoadingEvent(dateOfStart, loadingTime, numOfTerms, numOfObsoleteTerms, numOfAliases, numKeys, numValues);
        loadingData.put(ontology, loadingEntity);
    }

    /**
     * Retrieve the internal map for a given ontology
     *
     * @param ontology Ontology
     * @return map
     */
    public PatriciaTrieMultiMap<Term> getTermOntologyMap(Ontology ontology) {
        PatriciaTrieMultiMap<Term> map = null;
        if (ontology.isComposedOntologies()) {
            // now we construct a map
            map = new PatriciaTrieMultiMap<Term>();
            for (Ontology subOntology : ontology.getIndividualOntologies()) {
                map.putAll(ontologyTermMap.get(subOntology));
            }
        } else {
            map = ontologyTermMap.get(ontology);
        }
        return map;
    }

    private OntologyManager() {
    }

    /**
     * Retrieve a term by ID (internal) or Obo ID from a given ontology.
     *
     * @param ontology ontology
     * @param id       term ID
     * @return term
     */
    public Term getTermByID(Ontology ontology, String id) {
        Set<Term> terms = ontologyTermMap.get(ontology).get(id);
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
        return ontologyTermMap != null && ontologyTermMap.get(ontology) != null;

    }

    /**
     * Retrieve a term by ID. All ontologies will be searched through.
     * If no term is found it returns null.
     *
     * @param termID term ID
     * @return term
     */
    public Term getTermByID(String termID) {
        if (StringUtils.isEmpty(termID))
            return null;

        for (Ontology ontology : ontologyTermMap.keySet()) {
            Term term = getTermByID(ontology, termID);
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
            if (getTermByID(subOntology, termID) != null)
                return subOntology;
        }
        return rootOntology;
    }

    /**
     * Retrieve a term by name from a given ontology.
     *
     * @param ontology      ontology
     * @param termName      term name
     * @param allowObsolete include obsolete term in the search.
     * @return term
     */
    public Term getTermByName(Ontology ontology, String termName, boolean allowObsolete) {
        Collection<Term> terms = getTermOntologyMap(ontology).get(termName.trim().toLowerCase());
        if (terms == null) {
            logger.info("No terms for term: " + termName + " and ontology: " + ontology.getOntologyName());
            return null;
        } else {
            for (Term term : terms) {
                if ((!term.isObsolete() || allowObsolete)
                        &&
                        term.getTermName().equalsIgnoreCase(termName)) {
                    return term;
                }
            }
            return null;
        }
    }

    /**
     * Retrieve a term by name from a given ontology.
     *
     * @param ontology ontology
     * @param termName term name
     * @return term
     */
    public Term getTermByName(Ontology ontology, String termName) {
        return getTermByName(ontology, termName, false);
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
    public boolean isSubstructureOf(Term child, Term root) {
        List<TransitiveClosure> children = allRelatedChildrenMap.get(root);
        // ToDO: Need to loop over list rather than use .contains() method because the terms maybe be proxied
        for (TransitiveClosure childTerm : children) {
            if (childTerm.getChild().getID().equals(child.getID()))
                return true;
        }
        return false;
    }

    /**
     * Retrieve all children for a given parent term. This ignores the type or relationship by including
     * any relationship between the parent term and the child term.
     *
     * @param parent term
     * @return list of all children
     */
    public List<TransitiveClosure> getAllChildren(Term parent) {
        return allRelatedChildrenMap.get(parent);
    }

    /**
     * Returns an unmodifiable map of the loading times for each ontology being loaded
     *
     * @return map of loading times in seconds.
     */
    public Map<Ontology, Double> getOntologyLoadingTimes() {
        return Collections.unmodifiableMap(loadingTimeMap);
    }

    public Map<Ontology, PatriciaTrieMultiMap<Term>> getOntologyMap() {
        return ontologyTermMap;
    }

    public Map<Ontology, OntologyLoadingEntity> getLoadingData() {
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
        builder.append(ontologyTermMap.size());
        builder.append(NEWLINE);
        for (Ontology ontology : ontologyTermMap.keySet()) {
            builder.append(ontology);
            builder.append(" [");
            builder.append(ontologyTermMap.get(ontology).size());
            builder.append("]");
            builder.append(NEWLINE);
        }
        return builder.toString();
    }

    public void serializeOntologies() {
        for (Ontology ontology : Ontology.getSerializableOntologies()) {
            if (!ontology.isComposedOntologies()) {
                serializeOntology(ontology);
            }
        }
    }

    public void deserializeOntologies() throws Exception {
        deserializeRelationships();
        deserializeInfrastructureFiles();
        for (Ontology ontology : Ontology.getSerializableOntologies()) {
            if (!ontology.isComposedOntologies()) {
                deserializeOntology(ontology);
            }
        }
    }

    public void deserializeInfrastructureFiles() throws Exception {
        deserializeLoadingStatistic();
        deserializeTransitiveClosure();
        deserializeRelationshipTypes();
    }

    private void deserializeRelationshipTypes() throws Exception {
        long start = System.currentTimeMillis();
        File lookupFile;
        try {
            lookupFile = FileUtil.createOntologySerializationFile(RELATIONSHIP_TYPES_SER);
            if (!lookupFile.exists() || !lookupFile.canRead()) {
                throw new IOException("Lookup file does not exist or has bad permissions: " + lookupFile.getAbsolutePath());
            }
            logger.info("Lookup file: " + lookupFile + " size:" + lookupFile.length() + " last modified: " + new Date(lookupFile.lastModified()));

            distinctRelationshipTypes = (Map<Ontology, Set<String>>) FileUtil.deserializeOntologies(lookupFile);
        } catch (Exception e) {
            logger.error("Failed to deserialize the files", e);
            throw e;
        }
        long end = System.currentTimeMillis();
        double time = (double) (end - start) / MILLISECONDS_PER_SECOND;

        logger.info("Time to deserialize ontology[" + ALL_RELATIONSHIPS + "]: " + time + " seconds.");
    }

    /**
     * Retrieve the relationships between terms from a serialized file.
     *
     * @throws Exception throws exception when deserialization fails.
     */
    public void deserializeRelationships() throws Exception {
        long start = System.currentTimeMillis();
        File lookupFile;
        try {
            lookupFile = FileUtil.createOntologySerializationFile(ALL_RELATIONSHIPS);
            if (!lookupFile.exists() || !lookupFile.canRead()) {
                throw new IOException("Lookup file does not exist or has bad permissions: " + lookupFile.getAbsolutePath());
            }
            logger.info("Lookup file: " + lookupFile + " size:" + lookupFile.length() + " last modified: " + new Date(lookupFile.lastModified()));

            allRelationships = (Map<String, List<TermRelationshipHelper>>) FileUtil.deserializeOntologies(lookupFile);
        } catch (Exception e) {
            logger.error("Failed to deserialize the files", e);
            throw e;
        }
        long end = System.currentTimeMillis();
        double time = (double) (end - start) / MILLISECONDS_PER_SECOND;

        logger.info("Time to deserialize ontology[" + ALL_RELATIONSHIPS + "]: " + time + " seconds.");
    }


    /**
     * Here, we serialize 2 objects
     *
     * @param ontology Ontology.
     * @throws IOException Thrown if problem writing to file.
     */
    @SuppressWarnings("unchecked")
    public void deserializeOntology(Ontology ontology) throws Exception {
        long start = System.currentTimeMillis();
        File lookupFile;
        try {
            lookupFile = FileUtil.createOntologySerializationFile(ontology.name() + SERIALIZED_LOOKUP_SUFFIX);
            if (!lookupFile.exists() || !lookupFile.canRead()) {
                throw new IOException("Lookup file does not exist or has bad permissions: " + lookupFile.getAbsolutePath());
            }
            logger.info("Lookup file: " + lookupFile + " size:" + lookupFile.length() + " last modified: " + new Date(lookupFile.lastModified()));

            PatriciaTrieMultiMap<Term> lookupMap =
                    (PatriciaTrieMultiMap<Term>) FileUtil.deserializeOntologies(lookupFile);
            lookupMap.rebuild();
            ontologyTermMap.remove(ontology);
            ontologyTermMap.put(ontology, lookupMap);
            populateRelationships(lookupMap.getAllValues(), ontology);
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
            loadingData = (Map<Ontology, OntologyLoadingEntity>) FileUtil.deserializeOntologies(idFile);
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
    public void deserializeTransitiveClosure() throws Exception {
        long start = System.currentTimeMillis();
        File idFile;
        try {
            // load id obo mapping
            idFile = FileUtil.createOntologySerializationFile(TRANSITIVE_CLOSURE_SER);
            if (!idFile.exists() || !idFile.canRead()) {
                throw new IOException("idFile file does not exist or has bad permissions: " + idFile.getAbsolutePath());
            }
            logger.info("idFile file: " + idFile + " size:" + idFile.length() + " last modified: " + new Date(idFile.lastModified()));
            allRelatedChildrenMap = (Map<Term, List<TransitiveClosure>>) FileUtil.deserializeOntologies(idFile);
            logger.info(TRANSITIVE_CLOSURE_SER + " file: " + idFile + " size:" + idFile.length() + " last modified: " + new Date(idFile.lastModified()));

        } catch (Exception e) {
            logger.error("Failed to deserialize the files", e);
            throw e;
        }
        long end = System.currentTimeMillis();
        double time = (double) (end - start) / MILLISECONDS_PER_SECOND;
        logger.info("Time to deserialize obo id mapping: " + time + " seconds.");
    }

    /**
     * Here, we serialize 2 objects
     *
     * @param ontology Ontology.
     */
    public void serializeOntology(Ontology ontology) {
        long start = System.currentTimeMillis();
        File lookupFile = FileUtil.serializeObject(ontologyTermMap.get(ontology),
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
     * @param ontologies    collection of ontologies
     * @param termName      term name
     * @param allowObsolete True if an obsolete term may be returned.
     * @return term object
     */
    public Term getTermByName(List<Ontology> ontologies, String termName, boolean allowObsolete) {
        if (ontologies == null)
            return null;

        for (Ontology ontology : ontologies) {
            Term term = getTermByName(ontology, termName, allowObsolete);
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
     * @param ontologies collection of ontologies
     * @param termName   term name
     * @return term object
     */
    public Term getTermByName(List<Ontology> ontologies, String termName) {
        return getTermByName(ontologies, termName, false);
    }

    /**
     * Retrieve all terms for a given ontology.
     *
     * @param ontology Ontology
     * @return set of terms
     */
    public Set<Term> getAllTerms(Ontology ontology) {
        return getTermOntologyMap(ontology).getAllValues();
    }

    public Set<String> getDistinctRelationshipTypes(Ontology ontology) {
        if (distinctRelationshipTypes == null)
            throw new NullPointerException("No map for relationship types available.");
        return distinctRelationshipTypes.get(ontology);
    }

    public enum LoadingMode {
        DATABASE, SERIALIZED_FILE
    }
}

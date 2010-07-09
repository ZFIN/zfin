package org.zfin.ontology;

import org.apache.commons.collections.CollectionUtils;
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

    private OntologyTokenizer tokenizer  = new OntologyTokenizer() ;

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
    public static final String SERIALIZED_LOOKUP_SUFFIX = "-lookup.ser" ;

    private static Ontology singleOntology = null;
//    public static final String SERIALIZED_ALL_FILE_NAME = "all"+ SERIALIZED_LOOKUP_SUFFIX;

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
        ontologyManager.deserializeOntologies();
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
        logger.info("Start loading ontologies");
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
            initSingleOntologyMap(singleOntology);
            serializeOntology(singleOntology);
            if(singleOntology==Ontology.ANATOMY){
                loadTransitiveClosure();
            }
        } else{
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

    }


    private void serializeOntologyInThread(final Ontology ontology) {
        new Thread(){
            @Override
            public void run() {
                serializeOntology(ontology);
            }
        }.start();
    }

    private void initRootOntologyMap(Ontology subOntology, Ontology rootOntology, String... rootOboIDs) {
        long startTime = System.currentTimeMillis();
        Date dateStarted = new Date();

        int averageMaximumNumOfChildren = 30;
        List<TermRelationship> relationships = getOntologyRepository().getAllRelationships(rootOntology);
        // termID, collection of children terms
        Map<String, List<Term>> childrenMap = new TreeMap<String, List<Term>>();
        for (TermRelationship relationship : relationships) {
            String termID = relationship.getTermOne().getID();
            List<Term> children = childrenMap.get(termID);
            if (children == null)
                children = new ArrayList<Term>(10);
            children.add(relationship.getTermTwo());
            childrenMap.put(termID, children);
        }

        List<Term> children = new ArrayList<Term>(averageMaximumNumOfChildren);
        for (String rootOboID : rootOboIDs) {
            Term rootTerm = getOntologyRepository().getTermByOboID(rootOboID);
            children.addAll(childrenMap.get(rootTerm.getID()));
        }
        PatriciaTrieMultiMap<Term> termMap = new PatriciaTrieMultiMap<Term>();
        int obsoleteCount = 0 ;
        int aliasCount = 0 ;
        int activeCount = 0 ;
        while (!children.isEmpty()) {
            Term currentTerm = children.get(0);
            children.remove(0);
            List<Term> newChildren = childrenMap.get(currentTerm.getID());
            if (newChildren != null && !newChildren.isEmpty()) {
                children.addAll(newChildren);
            }
            Term term  = getTermByID(currentTerm.getOntology(),currentTerm.getID()) ;
            if (term == null)
                logger.error("Child Term <" + currentTerm.getID() + "> not found in Root ontology " + rootOntology.getOntologyName());
            else {
                tokenizer.tokenizeTerm(currentTerm,termMap) ;
                if(currentTerm.isObsolete()) {
                    ++obsoleteCount ;
                }
                else{
                    ++activeCount ;
                }
                if(term.getAliases()!=null){
                    aliasCount += term.getAliases().size() ;
                }
            }
        }
        ontologyTermMap.put(subOntology, termMap);

        long endTime = System.currentTimeMillis();
        double loadingTimeInSeconds = (double) (endTime - startTime) / MILLISECONDS_PER_SECOND;
        logLoading(subOntology, startTime, endTime, dateStarted, activeCount, obsoleteCount, aliasCount,
                termMap.keySet().size(),termMap.getAllValues().size());
        loadingTimeMap.put(subOntology, loadingTimeInSeconds);
    }


    /**
     * Load a single ontology.
     *
     * @param ontology Ontology
     */
    private void initSingleOntologyMap(Ontology ontology) {
        long startTime = System.currentTimeMillis();
        Date dateStarted = new Date();
        List<Term> terms = getOntologyRepository().getAllTermsFromOntology(ontology);
        long nextTime = System.currentTimeMillis() ;
        logger.debug("time to load from DB: "+ (nextTime - startTime) );

        PatriciaTrieMultiMap<Term> termMap = new PatriciaTrieMultiMap<Term>();
        if (terms == null) {
            logger.info("No terms for ontology <" + ontology.getOntologyName() + "> found.");
            return;
        }

        int activeCount= 0 ;
        int aliasCount = 0 ;
        int obsoleteCount = 0 ;
        for (Term term : terms) {
            tokenizer.tokenizeTerm(term,termMap) ;
            if(term.isObsolete()){
                ++obsoleteCount ;
            }
            else{
                ++activeCount ;
            }

            if(term.getAliases()!=null){
                aliasCount += term.getAliases().size() ;
            }

        }
        logger.debug("to put in hashmap: "+ (System.currentTimeMillis() - nextTime) );
        nextTime = System.currentTimeMillis() ;

        ontologyTermMap.put(ontology, termMap);

        logger.debug("to have loaded ontologies : "+ (System.currentTimeMillis() - nextTime) );
        nextTime = System.currentTimeMillis() ;

        logger.debug("calculate aliases: "+ (System.currentTimeMillis() - nextTime) );
        nextTime = System.currentTimeMillis() ;
        populateStageInformation(ontology);

        logger.info("populate stage info: "+ (System.currentTimeMillis() - nextTime) );

        long endTime = System.currentTimeMillis();
        logLoading(ontology, startTime, endTime, dateStarted, activeCount, obsoleteCount, aliasCount,
                termMap.keySet().size(),termMap.getAllValues().size());
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

        for (Term term : getTermOntologyMap(ontology).getAllValues()) {
            DevelopmentStage start = RepositoryFactory.getAnatomyRepository().getStartStage(term.getOboID());
            DevelopmentStage end = RepositoryFactory.getAnatomyRepository().getEndStage(term.getOboID());
            term.setStart(start);
            term.setEnd(end);
        }
    }


    private void logLoading(Ontology ontology, long startTime, long endTime, Date dateOfStart,
                            int numOfTerms, int numOfObsoleteTerms, int numOfAliases,int numKeys,int numValues) {
        long loadingTime = endTime - startTime;
        double loadingTimeSeconds = (double) loadingTime / MILLISECONDS_PER_SECOND;
        logger.info("Loading <" + ontology.getOntologyName() + "> ontology took " + loadingTimeSeconds + " seconds.");
        OntologyLoadingEntity loadingEntity = loadingData.get(ontology);
        if (loadingEntity == null) {
            loadingEntity = new OntologyLoadingEntity(ontology);
        }
        loadingEntity.addLoadingEvent(dateOfStart, loadingTime, numOfTerms, numOfObsoleteTerms, numOfAliases,numKeys,numValues);
        loadingData.put(ontology, loadingEntity);
    }

    /**
     * Retrieve the internal map for a given ontology
     *
     * @param ontology Ontology
     * @return map
     */
    public PatriciaTrieMultiMap<Term> getTermOntologyMap(Ontology ontology) {
        PatriciaTrieMultiMap<Term> map ;
        if(ontology.isComposedOntologies()){
            // now we construct a map
            map = new PatriciaTrieMultiMap<Term>() ;
            for(Ontology subOntology: ontology.getIndividualOntologies()){
                map.putAll(ontologyTermMap.get(subOntology));
            }
        }
        else{
            map = ontologyTermMap.get(ontology);
        }
        return map;
    }

    private OntologyManager() { }

    /**
     * Retrieve a term by ID from a given ontology.
     *
     * @param ontology ontology
     * @param termID   term ID
     * @return term
     */
    public Term getTermByID(Ontology ontology, String termID) {
        Set<Term> terms = ontologyTermMap.get(ontology).get(termID) ;
        if(CollectionUtils.isNotEmpty(terms)){
            if(terms.size()!=1){
                logger.error("multiple terms ["+terms.size()+"] returned for termID: "+termID);
            }
            return terms.iterator().next();
        }
        return null ;
    }

    /**
     * Retrieve a term by ID. All ontologies will be searched through.
     * If no term is found it return null.
     *
     * @param termID term ID
     * @return term
     */
    public Term getTermByID(String termID) {
        for (Ontology ontology : ontologyTermMap.keySet()) {
            Term term = getTermByID(ontology,termID);
            if (term!= null){
                return term ;
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
     * @param ontology ontology
     * @param termName term name
     * @param allowObsolete Indicates if obsoletes will be returned.
     * @return term
     */
    public Term getTermByName(Ontology ontology, String termName,boolean allowObsolete) {
        Collection<Term> terms =   getTermOntologyMap(ontology).get(termName.trim().toLowerCase());
        if(terms==null){
            logger.warn("No terms for term: "+ termName + " and ontology: "+ ontology.getOntologyName());
            return null ;
        }
        else{
            for(Term term: terms){
                if( (!term.isObsolete() || allowObsolete)
                        &&
                        term.getTermName().equalsIgnoreCase(termName)){
                    return term ;
                }
            }
            return null ;
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
        return getTermByName(ontology,termName,false) ;
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

    public void serializeOntologies(){
        for(Ontology ontology : Ontology.getSerializableOntologies()){
            serializeOntology(ontology);
        }
    }

    public void deserializeOntologies() throws Exception{
        for(Ontology ontology : Ontology.getSerializableOntologies()){
            deserializeOntology(ontology);
        }
    }


    /**
     * Here, we serialize 2 objects
     * @param ontology Ontology.
     * @throws IOException Thrown if problem writing to file.
     */
    @SuppressWarnings("unchecked")
    public void deserializeOntology(Ontology ontology) throws Exception{
        long start = System.currentTimeMillis();
        File lookupFile ;
        try {
            lookupFile = FileUtil.createOntologySerializationFile(ontology.name()+SERIALIZED_LOOKUP_SUFFIX);
            if(!lookupFile.exists() || !lookupFile.canRead()){
                throw new IOException("Lookup file does not exist or has bad permissions: " + lookupFile.getAbsolutePath()) ;
            }
            logger.info("Lookup file: "+lookupFile + " size:"+lookupFile.length() + " last modified: " + new Date(lookupFile.lastModified()));

            PatriciaTrieMultiMap<Term> lookupMap  =
                    (PatriciaTrieMultiMap<Term>) FileUtil.deserializeOntologies(lookupFile );
            lookupMap.rebuild();
            ontologyTermMap.remove(ontology);
            ontologyTermMap.put(ontology,lookupMap) ;

        } catch (IOException e) {
            logger.error("Failed to deserialize the files",e);
            throw e;
        }
        long end = System.currentTimeMillis();
        double time = (double) (end - start) / MILLISECONDS_PER_SECOND;

        logger.info("Time to deserialize ontology[" + ontology.name()+ "]: "+time+" seconds.");
    }

    /**
     * Here, we serialize 2 objects
     * @param ontology Ontology.
     */
    public void serializeOntology(Ontology ontology){
        long start = System.currentTimeMillis();
        File lookupFile = FileUtil.serializeObject(ontologyTermMap.get(ontology),
                FileUtil.createOntologySerializationFile(ontology.name()+SERIALIZED_LOOKUP_SUFFIX));
        long end = System.currentTimeMillis();
        double time = (double) (end - start) / MILLISECONDS_PER_SECOND;

        logger.info("Time to serialize ontology[" + ontology.name()+ "]: "+time+" seconds.");
        logger.info("Lookup file path[" + lookupFile.getAbsolutePath()+ "] size: "+lookupFile.length());
    }

    public void serializeOntology(File serializeFile) {
        if (ontologyManager == null)
            getInstance();
        long start = System.currentTimeMillis();
        FileUtil.serializeObject(ontologyTermMap, serializeFile);
        long end = System.currentTimeMillis();
        double time = (double) (end - start) / MILLISECONDS_PER_SECOND;

        logger.info("Time to serialize ontologies: " + time + " seconds");
    }

    /**
     * Retrieve a term by term name and a list of ontologies.
     * The logic loops over all ontologies and returns the term from the ontology
     * in which it is found first. If no term is found a null is returned.
     *
     * @param ontologies collection of ontologies
     * @param termName   term name
     * @param allowObsolete True if an obsolete term may be returned.
     * @return term object
     */
    public Term getTermByName(List<Ontology> ontologies, String termName,boolean allowObsolete) {
        if (ontologies == null)
            return null;

        for (Ontology ontology : ontologies) {
            Term term = getTermByName(ontology, termName,allowObsolete);
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
        return getTermByName(ontologies,termName,false) ;
    }

    public enum LoadingMode {
        DATABASE, SERIALIZED_FILE
    }
}

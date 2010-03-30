package org.zfin.ontology;

import org.apache.log4j.Logger;
import org.zfin.ontology.presentation.OntologyLoadingEntity;
import org.zfin.util.FileUtil;

import java.io.Serializable;
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
public class OntologyManager implements Serializable {

    private static final int NUMBER_OF_ONTOLOGIES = 6;
    // This holds all ontologies with the ontology as a key
    // (Ontology, (termID, term))
    private Map<Ontology, Map<String, Term>> ontologyTermMap = new HashMap<Ontology, Map<String, Term>>(NUMBER_OF_ONTOLOGIES);
    // This holds all ontologies with the ontology as a key
    // and obsoleted terms (Ontology, (termID, term))
    private Map<Ontology, Map<String, Term>> ontologyObsoleteTermMap = new HashMap<Ontology, Map<String, Term>>(NUMBER_OF_ONTOLOGIES);
    // This holds the collection of terms that are known under aliases
    // (ontology, (termID, term alias))
    private Map<Ontology, Map<String, TermAlias>> ontologyAliasTermMap = new HashMap<Ontology, Map<String, TermAlias>>(NUMBER_OF_ONTOLOGIES);

    // This holds all ontologies with the ontology as a key
    // (Ontology, (term ID, term name))
    private Map<Ontology, Map<String, String>> ontologyTermNameIDMapping = new HashMap<Ontology, Map<String, String>>(NUMBER_OF_ONTOLOGIES);

    private Map<Ontology, OntologyLoadingEntity> loadingData = new HashMap<Ontology, OntologyLoadingEntity>(NUMBER_OF_ONTOLOGIES);
    // sole singleton instance
    private static OntologyManager ontologyManager = null;
    private static final Logger LOG = Logger.getLogger(OntologyManager.class);

    private static final int MAXIMUM_NUMBER_OF_MATCHES = 25;
    private static final double MILLISECONDS = 1000.0;

    private static Map<Ontology, Double> loadingTimeMap = new HashMap<Ontology, Double>(10);
    private static final Object LOADING_FLAG = new Object();

    public static final String SERIALIZED_FILE_NAME = "serialized-ontologies.ser";

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

    public static OntologyManager getInstance(LoadingMode mode) throws Exception {
        switch (mode) {
            case DATABASE:
                return getInstance();
            case SERIALIZED_FILE:
                return getInstanceFromFile();
        }
        throw new RuntimeException("No valid loading mode provided");
    }

    private static OntologyManager getInstanceFromFile() throws Exception {
        if (FileUtil.isOntologyFileExist(SERIALIZED_FILE_NAME)) {
            loadOntologiesFromFile();
        }
        return ontologyManager;
    }

    private static void loadOntologiesFromFile() throws Exception {
        long start = System.currentTimeMillis();
        ontologyManager = (OntologyManager) FileUtil.deserializeOntologies(SERIALIZED_FILE_NAME);
        long end = System.currentTimeMillis();
        double time = (double) (end - start) / 1000.0;

        LOG.info("Time to load ontologies from serialized File: " + time + " seconds");
        LOG.info(ontologyManager);
    }

    private static Ontology singleOntology = null;

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
        LOG.info("Start loading ontologies");
        long startTime = System.currentTimeMillis();
        synchronized (LOADING_FLAG) {
            ontologyManager = new OntologyManager();
            ontologyManager.loadOntologiesFromDatabase();
        }
        long endTime = System.currentTimeMillis();
        double loadingTimeInSeconds = (double) (endTime - startTime) / MILLISECONDS;
        LOG.info("Finished loading all ontologies: took " + loadingTimeInSeconds + " seconds.");
    }

    private void loadOntologiesFromDatabase() {
        if (singleOntology != null) {
            initSingleOntologyMap(singleOntology);
            initSingleOntologyMap(Ontology.ANATOMY);
            initRootOntologyMap(Ontology.QUALITY_PROCESSES, Ontology.QUALITY, "PATO:0001236");
            initRootOntologyMap(Ontology.QUALITY_QUALITIES, Ontology.QUALITY, "PATO:0001241");
/*
            initRootOntologyMap(Ontology.QUALITY_QUALITATIVE, Ontology.QUALITY, "PATO:0000068");
            initRootOntologyMap(Ontology.QUALITY_OBJECT_RELATIONAL, Ontology.QUALITY, "PATO:0001238");
            initRootOntologyMap(Ontology.QUALITY_PROCESSES_RELATIONAL, Ontology.QUALITY, "PATO:0001240");
*/
        } else
            loadDefaultOntologies();
    }

    private void loadDefaultOntologies() {
        initSingleOntologyMap(Ontology.ANATOMY);
        initSingleOntologyMap(Ontology.QUALITY);
        // Quality  Processes and Objects
        initRootOntologyMap(Ontology.QUALITY_PROCESSES, Ontology.QUALITY, "PATO:0001236");
        initRootOntologyMap(Ontology.QUALITY_QUALITIES, Ontology.QUALITY, "PATO:0001241");
/*
        initRootOntologyMap(Ontology.QUALITY_QUALITATIVE, Ontology.QUALITY, "PATO:0000068");
        initRootOntologyMap(Ontology.QUALITY_OBJECT_RELATIONAL, Ontology.QUALITY, "PATO:0001238");
        initRootOntologyMap(Ontology.QUALITY_PROCESSES_RELATIONAL, Ontology.QUALITY, "PATO:0001240");
*/
        // GO ontology
        initSingleOntologyMap(Ontology.GO_MF);
        initSingleOntologyMap(Ontology.GO_CC);
        initSingleOntologyMap(Ontology.GO_BP);
        initComposedOntologyMap(Ontology.GO_BP_MF);
        initComposedOntologyMap(Ontology.GO);
    }

    private void initRootOntologyMapWithExclusion(Ontology qualityProcesses, Ontology rootOntology, String rootOboIDs, String excludeOboIDs) {

    }

    private void initRootOntologyMap(Ontology qualityProcesses, Ontology rootOntology, String... rootOboIDs) {
        long startTime = System.currentTimeMillis();
        Date dateStarted = new Date();

        int averageMaximumNumOfChildren = 30;
        List<Term> children = new ArrayList<Term>(averageMaximumNumOfChildren);
        for (String rootOboID : rootOboIDs) {
            Term rootTerm = getOntologyRepository().getTermByOboID(rootOboID);
            children.addAll(rootTerm.getChildrenTerms());
        }
        Map<String, Term> qualityTermMap = ontologyTermMap.get(rootOntology);
        Map<String, TermAlias> qualityTermAliasMap = ontologyAliasTermMap.get(rootOntology);
        Map<String, String> qualityIdNameMap = ontologyTermNameIDMapping.get(rootOntology);
        Map<String, Term> termMap = new TreeMap<String, Term>(new OntologyComparator());
        Map<String, TermAlias> termAliasMap = new TreeMap<String, TermAlias>(new OntologyComparator());
        Map<String, Term> obsoleteNameMap = new TreeMap<String, Term>(new OntologyComparator());
        while (!children.isEmpty()) {
            Term currentTerm = children.get(0);
            children.remove(0);
            List<Term> newChildren = currentTerm.getChildrenTerms();
            if (newChildren != null && !newChildren.isEmpty()) {
                children.addAll(newChildren);
            }
            String termName = qualityIdNameMap.get(currentTerm.getID());
            if (termName == null)
                LOG.error("Child Term <" + currentTerm.getID() + "> not found in Root ontology " + rootOntology.getOntologyName());
            else {
                if (currentTerm.isObsolete())
                    obsoleteNameMap.put(termName, currentTerm);
                else
                    termMap.put(termName, currentTerm);
            }
            //termAliasMap.put()
        }
        ontologyTermMap.put(qualityProcesses, termMap);
        ontologyObsoleteTermMap.put(qualityProcesses, obsoleteNameMap);
        ontologyTermNameIDMapping.put(qualityProcesses, qualityIdNameMap);
        ontologyAliasTermMap.put(qualityProcesses, termAliasMap);
        long endTime = System.currentTimeMillis();
        double loadingTimeInSeconds = (double) (endTime - startTime) / MILLISECONDS;
        logLoading(qualityProcesses, startTime, endTime, dateStarted, termMap.size());
        loadingTimeMap.put(qualityProcesses, loadingTimeInSeconds);
    }

    private void initComposedOntologyMap(Ontology ontology) {
        long startTime = System.currentTimeMillis();
        Date dateStarted = new Date();
        Collection<Ontology> ontologies = ontology.getIndividualOntologies();
        Map<String, Term> termMap = new TreeMap<String, Term>(new OntologyComparator());

        Iterator<Ontology> ontologyIterator = ontologies.iterator();
        while (ontologyIterator.hasNext()) {
            termMap.putAll(ontologyTermMap.get(ontologyIterator.next()));
        }

        ontologyTermMap.put(ontology, termMap);
        Map<String, TermAlias> aliasMap = new TreeMap<String, TermAlias>(new OntologyComparator());
        ontologyIterator = ontologies.iterator();
        while (ontologyIterator.hasNext())
            aliasMap.putAll(ontologyAliasTermMap.get(ontologyIterator.next()));

        ontologyAliasTermMap.put(ontology, aliasMap);
        long endTime = System.currentTimeMillis();
        double loadingTimeInSeconds = (double) (endTime - startTime) / MILLISECONDS;
        logLoading(ontology, startTime, endTime, dateStarted, termMap.size());
        loadingTimeMap.put(ontology, loadingTimeInSeconds);
    }

    /**
     * Load a single ontology.
     * First, it loads all terms and puts them into a hash map
     * Second, it loads all aliases and puts them into another hash map.
     *
     * @param ontology Ontology
     */
    private void initSingleOntologyMap(Ontology ontology) {
        long startTime = System.currentTimeMillis();
        Date dateStarted = new Date();
        List<Term> terms = getOntologyRepository().getAllTermsFromOntology(ontology);
        Map<String, Term> termMap = new TreeMap<String, Term>(new OntologyComparator());
        Map<String, String> termNameIDMap = new TreeMap<String, String>(new OntologyComparator());
        Map<String, Term> obsoleteNameMap = new TreeMap<String, Term>(new OntologyComparator());
        if (terms == null) {
            LOG.info("No terms for ontology <" + ontology.getOntologyName() + "> found.");
            return;
        }

        for (Term term : terms) {
            if (term.isObsolete())
                obsoleteNameMap.put(term.getTermName().toLowerCase(), term);
            else
                termMap.put(term.getTermName().toLowerCase(), term);
            termNameIDMap.put(term.getID(), term.getTermName());
        }
        ontologyTermMap.put(ontology, termMap);
        ontologyObsoleteTermMap.put(ontology, obsoleteNameMap);
        ontologyTermNameIDMapping.put(ontology, termNameIDMap);

        // load aliases
        List<TermAlias> anatomyTermsAlias = getOntologyRepository().getAllAliases(ontology);
        Map<String, TermAlias> aliases = new TreeMap<String, TermAlias>(new OntologyComparator());
        if (anatomyTermsAlias == null)
            return;

        for (TermAlias term : anatomyTermsAlias) {
            aliases.put(term.getAliasLowerCase(), term);
        }
        ontologyAliasTermMap.put(ontology, aliases);
        long endTime = System.currentTimeMillis();
        logLoading(ontology, startTime, endTime, dateStarted, termMap.size());
    }

    private void logLoading(Ontology ontology, long startTime, long endTime, Date dateOfStart, int numOfTerms) {
        long loadingTime = endTime - startTime;
        double loadingTimeSeconds = (double) loadingTime / MILLISECONDS;
        LOG.info("Loading <" + ontology.getOntologyName() + "> ontology took " + loadingTimeSeconds + " seconds.");
        OntologyLoadingEntity loadingEntity = loadingData.get(ontology);
        if (loadingEntity == null) {
            loadingEntity = new OntologyLoadingEntity(ontology);
        }
        loadingEntity.addLoadingEvent(dateOfStart, loadingTime, numOfTerms);
        loadingData.put(ontology, loadingEntity);
    }

    /**
     * Retrieve the internal map for a given ontology
     *
     * @param ontology Ontology
     * @return map
     */
    private Map<String, Term> getTermOntologyMap(Ontology ontology) {
        if (ontology == null)
            throw new NullPointerException("No ontology provided for lookup");
        Map<String, Term> map = ontologyTermMap.get(ontology);
        if (map == null && ontology.isComposedOntologies()) {
            initComposedOntologyMap(ontology);
            map = ontologyTermMap.get(ontology);
        }
        if (map == null || map.isEmpty())
            throw new RuntimeException("Cannot find ontology with name: " + ontology.getOntologyName());
        return map;
    }

    /**
     * Retrieve the internal map for a given ontology
     *
     * @param ontology Ontology
     * @return map
     */
    private Map<String, String> getTermOntologyIDMapping(Ontology ontology) {
        if (ontology == null)
            throw new NullPointerException("No ontology provided for lookup");
        Map<String, String> map = ontologyTermNameIDMapping.get(ontology);
        if (map == null)
            throw new RuntimeException("Cannot find ontology with name: " + ontology.getOntologyName());
        return map;
    }

    /**
     * Retrieve the internal map for a given ontology
     *
     * @param ontology Ontology
     * @return map
     */
    private Map<String, TermAlias> getAliasOntologyMap(Ontology ontology) {
        if (ontology == null)
            throw new NullPointerException("No ontology provided for lookup");
        Map<String, TermAlias> map = ontologyAliasTermMap.get(ontology);
        if (map == null)
            throw new RuntimeException("Cannot find ontology with name: " + ontology.getOntologyName());
        return ontologyAliasTermMap.get(ontology);
    }

    /**
     * Retrieve the internal obsolete map for a given ontology
     *
     * @param ontology Ontology
     * @return map
     */
    private Map<String, Term> getObsoleteTermMap(Ontology ontology) {
        if (ontology == null)
            throw new NullPointerException("No ontology provided for lookup");
        Map<String, Term> map = ontologyObsoleteTermMap.get(ontology);
        if (map == null)
            throw new RuntimeException("Cannot find ontology with name: " + ontology.getOntologyName());
        return ontologyObsoleteTermMap.get(ontology);
    }

    private OntologyManager() {
    }

    /**
     * Retrieve a list of terms that match a given query in a given ontology.<br/>
     * If the ontology is composed of more than one individual ontologies
     * the matching is performed on the combined, sorted ontology.
     * <p/>
     * First all Start_With matches are retrieved, then Contains matches as that is the default order of the return
     * collection.
     * There is an internal maximum number of returned terms which will truncate the search when this number is exceeded.
     * This may avoid looping over the Contains matches (including the alias matches) which are much slower.
     * <p/>
     * The list is sorted by: <br/>
     * 1) Starts with         <br/>
     * 2) Contains
     *
     * @param ontology Ontology
     * @param query    query string
     * @return list of terms
     */
    public List<MatchingTerm> getMatchingTerms(Ontology ontology, String query) {
        if (query == null)
            return null;

        boolean isMaxNumOfTermsFound = false;
        MatchingTermService service = new MatchingTermService(query, MAXIMUM_NUMBER_OF_MATCHES);
        // check all terms for matches
        for (String termName : getTermOntologyMap(ontology).keySet()) {
            Term term = getTermOntologyMap(ontology).get(termName);
            service.compareWithTerm(term);
            isMaxNumOfTermsFound = service.hasMaximumNumberOfTerms();
        }
        // if max number is reached by starts_with matches stop here and return the list
        // as the list always puts the starts_with matches before the contains matches. 
        if (isMaxNumOfTermsFound)
            return service.getTermsMatchingStart();

        // search alias map if it exists for matches
        Map<String, TermAlias> aliasMap = getAliasOntologyMap(ontology);
        if (aliasMap != null) {
            for (String termName : aliasMap.keySet()) {
                TermAlias termAlias = getAliasOntologyMap(ontology).get(termName);
                service.compareWithAlias(termAlias);
            }
        }
        // search obsolete map
        Map<String, Term> obsoleteMap = getObsoleteTermMap(ontology);
        if (obsoleteMap != null) {
            for (String termName : obsoleteMap.keySet()) {
                Term obosleteTerm = getObsoleteTermMap(ontology).get(termName);
                service.compareWithObsoleteTerm(obosleteTerm);
            }
        }
        return service.getCompleteMatchingList();
    }

    /**
     * Retrieve a term by ID from a given ontology.
     *
     * @param ontology ontology
     * @param termID   term ID
     * @return term
     */
    public Term getTermByID(Ontology ontology, String termID) {
        String termName = getTermOntologyIDMapping(ontology).get(termID);
        return getTermByName(ontology, termName);
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
     * @return term
     */
    public Term getTermByName(Ontology ontology, String termName) {
        return getTermOntologyMap(ontology).get(termName);

    }

    /**
     * Returns an unmodifiable map of the loading times for each ontology being loaded
     *
     * @return map of loading times in seconds.
     */
    public Map<Ontology, Double> getOntologyLoadingTimes() {
        return Collections.unmodifiableMap(loadingTimeMap);
    }

    public Map<Ontology, Map<String, Term>> getOntologyMap() {
        return Collections.unmodifiableMap(ontologyTermMap);
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
        serializeOntology();
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

    public static void serializeOntology() {
        if (ontologyManager == null)
            getInstance();
        long start = System.currentTimeMillis();
        FileUtil.serializeObject(ontologyManager, FileUtil.createOntologySerializationFile(SERIALIZED_FILE_NAME));
        long end = System.currentTimeMillis();
        double time = (double) (end - start) / 1000.0;

        LOG.info("Time to serialize ontologies: " + time + " seconds");
    }

    public enum LoadingMode {
        DATABASE, SERIALIZED_FILE
    }
}

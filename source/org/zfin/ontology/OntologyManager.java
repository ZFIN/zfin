package org.zfin.ontology;

import org.apache.log4j.Logger;
import org.zfin.ontology.presentation.OntologyLoadingEntity;
import org.zfin.util.FileUtil;

import java.io.File;
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
    private Map<Ontology, Map<String, List<TermAlias>>> ontologyAliasTermMap = new HashMap<Ontology, Map<String, List<TermAlias>>>(NUMBER_OF_ONTOLOGIES);

    // This holds all ontologies with the ontology as a key
    // (Ontology, (term ID, term name))
    private Map<Ontology, Map<String, String>> ontologyTermNameIDMapping = new HashMap<Ontology, Map<String, String>>(NUMBER_OF_ONTOLOGIES);

    private Map<Ontology, OntologyLoadingEntity> loadingData = new TreeMap<Ontology, OntologyLoadingEntity>(new OntologyNameComparator());
    // sole singleton instance
    private static OntologyManager ontologyManager = null;
    transient private static final Logger LOG = Logger.getLogger(OntologyManager.class);

    private static final double MILLI_SECONDS_PER_SECOND = 1000.0;

    private static Map<Ontology, Double> loadingTimeMap = new HashMap<Ontology, Double>(10);
    private static final Object LOADING_FLAG = new Object();
    public static final long serialVersionUID = 382860401967900179L;
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
            loadOntologiesFromFile(null);
        }
        return ontologyManager;
    }

    public static OntologyManager getInstanceFromFile(File file) throws Exception {
        if (file.exists()) {
            loadOntologiesFromFile(file);
        }
        return ontologyManager;
    }

    private static void loadOntologiesFromFile(File file) throws Exception {
        long start = System.currentTimeMillis();
        if (file == null)
            ontologyManager = (OntologyManager) FileUtil.deserializeOntologies(SERIALIZED_FILE_NAME);
        else
            ontologyManager = (OntologyManager) FileUtil.deserializeOntologies(file);
        long end = System.currentTimeMillis();
        double time = (double) (end - start) / MILLI_SECONDS_PER_SECOND;

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
        double loadingTimeInSeconds = (double) (endTime - startTime) / MILLI_SECONDS_PER_SECOND;
        LOG.info("Finished loading all ontologies: took " + loadingTimeInSeconds + " seconds.");
    }

    private void loadOntologiesFromDatabase() {
        if (singleOntology != null) {
            initSingleOntologyMap(singleOntology);
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
        Map<String, String> qualityIdNameMap = ontologyTermNameIDMapping.get(rootOntology);
        Map<String, Term> termMap = new TreeMap<String, Term>(new OntologyComparator());
        Map<String, Term> obsoleteNameMap = new TreeMap<String, Term>(new OntologyComparator());
        Set<Term> allTerms = new HashSet<Term>(500);
        allTerms.addAll(children);
        while (!children.isEmpty()) {
            Term currentTerm = children.get(0);
            children.remove(0);
            List<Term> newChildren = childrenMap.get(currentTerm.getID());
            if (newChildren != null && !newChildren.isEmpty()) {
                children.addAll(newChildren);
                allTerms.addAll(newChildren);
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
        }
        ontologyTermMap.put(subOntology, termMap);
        ontologyObsoleteTermMap.put(subOntology, obsoleteNameMap);
        ontologyTermNameIDMapping.put(subOntology, qualityIdNameMap);

        // create alias map
        List<TermAlias> anatomyTermsAlias = getOntologyRepository().getAllAliases(rootOntology);
        Map<String, List<TermAlias>> aliases = new HashMap<String, List<TermAlias>>(1);
        if (anatomyTermsAlias != null) {
            aliases = createAliasMap(anatomyTermsAlias, allTerms);
            ontologyAliasTermMap.put(subOntology, aliases);
        }
        long endTime = System.currentTimeMillis();
        double loadingTimeInSeconds = (double) (endTime - startTime) / MILLI_SECONDS_PER_SECOND;
        logLoading(subOntology, startTime, endTime, dateStarted, termMap.size(), obsoleteNameMap.size(), aliases.size());
        loadingTimeMap.put(subOntology, loadingTimeInSeconds);
    }

    /**
     * Creates a map of aliases as a key and a collection of terms that each alias is synonymous.
     * The provided term map contains all terms in a sub ontology of a given root ontology.
     * It loops over all terms of the sub ontology and checks which aliases are associated to it.
     * It's a bit of a hack for cases where we do not have a separate term_ontology identifier for
     * some of the slims, e.g. quality.objects  and quality.process.
     *
     * @param termMap  term map
     * @param ontology ontology
     * @return term map
     */
    private Map<String, List<TermAlias>> createAliasMapFromTermMap(Map<String, Term> termMap, Ontology ontology) {
        Map<String, List<TermAlias>> termAliasMap = new TreeMap<String, List<TermAlias>>(new OntologyComparator());
        Map<String, List<TermAlias>> aliasMapRootOntology = getAliasOntologyMap(ontology);
        for (String termName : termMap.keySet()) {
            Term term = termMap.get(termName);

        }
        return null;
    }

    private void initComposedOntologyMap(Ontology ontology) {
        long startTime = System.currentTimeMillis();
        Date dateStarted = new Date();
        Collection<Ontology> ontologies = ontology.getIndividualOntologies();
        Map<String, Term> termMap = new TreeMap<String, Term>(new OntologyComparator());
        Map<String, Term> obsoleteNameMap = new TreeMap<String, Term>(new OntologyComparator());

        Iterator<Ontology> ontologyIterator = ontologies.iterator();
        while (ontologyIterator.hasNext()) {
            final Ontology ontologyKey = ontologyIterator.next();
            termMap.putAll(ontologyTermMap.get(ontologyKey));
            obsoleteNameMap.putAll(ontologyObsoleteTermMap.get(ontologyKey));
        }

        ontologyTermMap.put(ontology, termMap);
        ontologyObsoleteTermMap.put(ontology, obsoleteNameMap);

        // create alias map
        Map<String, List<TermAlias>> aliasMap = new TreeMap<String, List<TermAlias>>(new OntologyComparator());
        ontologyIterator = ontologies.iterator();
        while (ontologyIterator.hasNext())
            aliasMap.putAll(ontologyAliasTermMap.get(ontologyIterator.next()));

        ontologyAliasTermMap.put(ontology, aliasMap);
        long endTime = System.currentTimeMillis();
        double loadingTimeInSeconds = (double) (endTime - startTime) / MILLI_SECONDS_PER_SECOND;
        logLoading(ontology, startTime, endTime, dateStarted, termMap.size(), obsoleteNameMap.size(), aliasMap.size());
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

        // load all aliases
        List<TermAlias> anatomyTermsAlias = getOntologyRepository().getAllAliases(ontology);
        Map<String, List<TermAlias>> aliases = null;
        if (anatomyTermsAlias != null) {
            aliases = createAliasMap(anatomyTermsAlias);
            ontologyAliasTermMap.put(ontology, aliases);
        }
        long endTime = System.currentTimeMillis();
        logLoading(ontology, startTime, endTime, dateStarted, termMap.size(), obsoleteNameMap.size(), aliases.size());
    }

    private Map<String, List<TermAlias>> createAliasMap(Iterable<TermAlias> termAliasList) {
        return createAliasMap(termAliasList, null);
    }

    /**
     * Iterates through the list of TermAlias objects and creates a map with unique alias names (key)
     * that have a collection of TermAliases associated (value). The alias name is made lower case for
     * case insensitive searching.
     * If an ontology id provided this method checks if the Term and TermAlias is part of the ontology.
     * If not then the alias is discarded.
     *
     * @param termAliasList map of TermAliases
     * @param allTerms      ontology: not required. the full ontology
     * @return map of alias names with list of TermAlias objects.
     */
    private Map<String, List<TermAlias>> createAliasMap(Iterable<TermAlias> termAliasList, Collection<Term> allTerms) {
        Map<String, List<TermAlias>> aliasMap = new TreeMap<String, List<TermAlias>>(new OntologyComparator());
        for (TermAlias termAlias : termAliasList) {
            List<TermAlias> existingAliases = aliasMap.get(termAlias.getAliasLowerCase());
            if (existingAliases == null)
                existingAliases = new ArrayList<TermAlias>(3);
            if (allTerms == null || allTerms.contains(termAlias.getTerm())) {
                existingAliases.add(termAlias);
                aliasMap.put(termAlias.getAliasLowerCase(), existingAliases);
            }
        }
        return aliasMap;
    }

    private void logLoading(Ontology ontology, long startTime, long endTime, Date dateOfStart, int numOfTerms, int numOfObsoleteTerms, int numOfAliases) {
        long loadingTime = endTime - startTime;
        double loadingTimeSeconds = (double) loadingTime / MILLI_SECONDS_PER_SECOND;
        LOG.info("Loading <" + ontology.getOntologyName() + "> ontology took " + loadingTimeSeconds + " seconds.");
        OntologyLoadingEntity loadingEntity = loadingData.get(ontology);
        if (loadingEntity == null) {
            loadingEntity = new OntologyLoadingEntity(ontology);
        }
        loadingEntity.addLoadingEvent(dateOfStart, loadingTime, numOfTerms, numOfObsoleteTerms, numOfAliases);
        loadingData.put(ontology, loadingEntity);
    }

    /**
     * Retrieve the internal map for a given ontology
     *
     * @param ontology Ontology
     * @return map
     */
    public Map<String, Term> getTermOntologyMap(Ontology ontology) {
        if (ontology == null)
            throw new NullPointerException("No ontology provided for lookup");
        Map<String, Term> map = ontologyTermMap.get(ontology);
        if (map == null && ontology.isComposedOntologies()) {
            initComposedOntologyMap(ontology);
            map = ontologyTermMap.get(ontology);
        }
        if (map == null || map.isEmpty())
            throw new RuntimeException("Cannot find term map for ontology with name: " + ontology.getOntologyName());
        return Collections.unmodifiableMap(map);
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
            throw new RuntimeException("Cannot find term ID map for ontology with name: " + ontology.getOntologyName());
        return map;
    }

    /**
     * Retrieve the internal map for a given ontology
     *
     * @param ontology Ontology
     * @return map
     */
    public Map<String, List<TermAlias>> getAliasOntologyMap(Ontology ontology) {
        if (ontology == null)
            throw new NullPointerException("No ontology provided for lookup");
        Map<String, List<TermAlias>> map = ontologyAliasTermMap.get(ontology);
        if (map == null)
            throw new RuntimeException("Cannot find alias map for ontology with name: " + ontology.getOntologyName());
        return Collections.unmodifiableMap(ontologyAliasTermMap.get(ontology));
    }

    /**
     * Retrieve the internal obsolete map for a given ontology
     *
     * @param ontology Ontology
     * @return map
     */
    public Map<String, Term> getObsoleteTermMap(Ontology ontology) {
        if (ontology == null)
            throw new NullPointerException("No ontology provided for lookup");
        Map<String, Term> map = ontologyObsoleteTermMap.get(ontology);
        if (map == null)
            throw new RuntimeException("Cannot find ontology with name: " + ontology.getOntologyName());
        return Collections.unmodifiableMap(ontologyObsoleteTermMap.get(ontology));
    }

    private OntologyManager() {
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

    public void serializeOntology() {
        long start = System.currentTimeMillis();
        FileUtil.serializeObject(ontologyManager, FileUtil.createOntologySerializationFile(SERIALIZED_FILE_NAME));
        long end = System.currentTimeMillis();
        double time = (double) (end - start) / MILLI_SECONDS_PER_SECOND;

        LOG.info("Time to serialize ontologies: " + time + " seconds");
    }

    public void serializeOntology(File serializeFile) {
        if (ontologyManager == null)
            getInstance();
        long start = System.currentTimeMillis();
        FileUtil.serializeObject(ontologyManager, serializeFile);
        long end = System.currentTimeMillis();
        double time = (double) (end - start) / MILLI_SECONDS_PER_SECOND;

        LOG.info("Time to serialize ontologies: " + time + " seconds");
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
        if (ontologies == null)
            return null;

        for (Ontology ontology : ontologies) {
            Term term = getTermByName(ontology, termName);
            if (term != null)
                return term;
        }
        return null;
    }

    public enum LoadingMode {
        DATABASE, SERIALIZED_FILE
    }
}

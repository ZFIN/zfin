package org.zfin.ontology;

import org.apache.log4j.Logger;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.ontology.presentation.OntologyLoadingEntity;
import org.zfin.repository.RepositoryFactory;
import org.zfin.util.FileUtil;
import org.zfin.util.NumberAwareStringComparator;

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

    private static final int NUMBER_OF_ONTOLOGIES = 8;
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

    /**
     * A map of all terms as a key and a list of terms that are children of the key term given by the transitive closure.
     */
    private Map<Term, List<TransitiveClosure>> allRelatedChildrenMap;
    private Map<Ontology, OntologyLoadingEntity> loadingData = new TreeMap<Ontology, OntologyLoadingEntity>(new OntologyNameComparator());
    // sole singleton instance
    private static OntologyManager ontologyManager = null;
    transient private static final Logger LOG = Logger.getLogger(OntologyManager.class);

    private static final double MILLISECONDS_PER_SECOND = 1000.0;

    private static Map<Ontology, Double> loadingTimeMap = new HashMap<Ontology, Double>(10);
    private static final Object LOADING_FLAG = new Object();
    public static final long serialVersionUID = -456814627961727234L;
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
        double time = (double) (end - start) / MILLISECONDS_PER_SECOND;

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
        double loadingTimeInSeconds = (double) (endTime - startTime) / MILLISECONDS_PER_SECOND;
        LOG.info("Finished loading all ontologies: took " + loadingTimeInSeconds + " seconds.");
    }

    private void loadOntologiesFromDatabase() {
        if (singleOntology != null) {
            initSingleOntologyMap(singleOntology);
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
        LOG.info("Loading Transitive closure for  " + allRelatedChildrenMap.size() + " terms. The total number of connections is " + closures.size());
    }

    private void loadDefaultOntologies() {
        initSingleOntologyMap(Ontology.STAGE);
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
        Map<String, String> rootIdNameMap = ontologyTermNameIDMapping.get(rootOntology);
        Map<String, String> subOntologyIdNameMap = new HashMap<String, String>(100);
        Map<String, Term> termMap = new TreeMap<String, Term>(new NumberAwareStringComparator());
        Map<String, Term> obsoleteNameMap = new TreeMap<String, Term>(new NumberAwareStringComparator());
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
            String termName = rootIdNameMap.get(currentTerm.getID());
            if (termName == null)
                LOG.error("Child Term <" + currentTerm.getID() + "> not found in Root ontology " + rootOntology.getOntologyName());
            else {
                if (currentTerm.isObsolete())
                    obsoleteNameMap.put(termName, currentTerm);
                else
                    termMap.put(termName, currentTerm);
                subOntologyIdNameMap.put(currentTerm.getID(), termName);
            }
        }
        ontologyTermMap.put(subOntology, termMap);
        ontologyObsoleteTermMap.put(subOntology, obsoleteNameMap);
        ontologyTermNameIDMapping.put(subOntology, subOntologyIdNameMap);

        // create alias map
        List<TermAlias> anatomyTermsAlias = getOntologyRepository().getAllAliases(rootOntology);
        Map<String, List<TermAlias>> aliases = new HashMap<String, List<TermAlias>>(1);
        if (anatomyTermsAlias != null) {
            aliases = createAliasMap(anatomyTermsAlias, allTerms);
            ontologyAliasTermMap.put(subOntology, aliases);
        }
        long endTime = System.currentTimeMillis();
        double loadingTimeInSeconds = (double) (endTime - startTime) / MILLISECONDS_PER_SECOND;
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
        Map<String, List<TermAlias>> termAliasMap = new TreeMap<String, List<TermAlias>>(new NumberAwareStringComparator());
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
        Map<String, Term> termMap = new TreeMap<String, Term>(new NumberAwareStringComparator());
        Map<String, Term> obsoleteNameMap = new TreeMap<String, Term>(new NumberAwareStringComparator());

        Iterator<Ontology> ontologyIterator = ontologies.iterator();
        while (ontologyIterator.hasNext()) {
            final Ontology ontologyKey = ontologyIterator.next();
            termMap.putAll(ontologyTermMap.get(ontologyKey));
            obsoleteNameMap.putAll(ontologyObsoleteTermMap.get(ontologyKey));
        }

        ontologyTermMap.put(ontology, termMap);
        ontologyObsoleteTermMap.put(ontology, obsoleteNameMap);

        // create alias map
        Map<String, List<TermAlias>> aliasMap = new TreeMap<String, List<TermAlias>>(new NumberAwareStringComparator());
        ontologyIterator = ontologies.iterator();
        while (ontologyIterator.hasNext())
            aliasMap.putAll(ontologyAliasTermMap.get(ontologyIterator.next()));

        ontologyAliasTermMap.put(ontology, aliasMap);
        long endTime = System.currentTimeMillis();
        double loadingTimeInSeconds = (double) (endTime - startTime) / MILLISECONDS_PER_SECOND;
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
        long nextTime = System.currentTimeMillis() ;
        LOG.debug("time to load from DB: "+ (nextTime - startTime) );

        Map<String, Term> termMap = new TreeMap<String, Term>(new NumberAwareStringComparator());
        Map<String, String> termNameIDMap = new TreeMap<String, String>(new NumberAwareStringComparator());
        Map<String, Term> obsoleteNameMap = new TreeMap<String, Term>(new NumberAwareStringComparator());
        if (terms == null) {
            LOG.info("No terms for ontology <" + ontology.getOntologyName() + "> found.");
            return;
        }

        Map<String, List<TermAlias>> aliasMap = new TreeMap<String, List<TermAlias>>(new NumberAwareStringComparator());
        for (Term term : terms) {
            if (term.isObsolete())
                obsoleteNameMap.put(term.getTermName().toLowerCase(), term);
            else {
                termMap.put(term.getTermName().toLowerCase(), term);
                for (TermAlias alias : term.getAliases()) {
                    insertPureTermAlias(aliasMap, alias);
                }
            }
            termNameIDMap.put(term.getID(), term.getTermName());
        }
        LOG.debug("to put in hashmap: "+ (System.currentTimeMillis() - nextTime) );
        nextTime = System.currentTimeMillis() ;

        ontologyTermMap.put(ontology, termMap);
        ontologyObsoleteTermMap.put(ontology, obsoleteNameMap);
        ontologyTermNameIDMapping.put(ontology, termNameIDMap);
        ontologyAliasTermMap.put(ontology, aliasMap);

        LOG.debug("to have loaded ontologies : "+ (System.currentTimeMillis() - nextTime) );
        nextTime = System.currentTimeMillis() ;

        int numberOfAliases = getNumberOfAliases(aliasMap);

        LOG.debug("calculate aliases: "+ (System.currentTimeMillis() - nextTime) );
        nextTime = System.currentTimeMillis() ;
        // load all aliases
/*
        List<TermAlias> anatomyTermsAlias = getOntologyRepository().getAllAliases(ontology);
        Map<String, List<TermAlias>> aliases = null;
        if (anatomyTermsAlias != null) {
            aliases = createAliasMap(anatomyTermsAlias);
            ontologyAliasTermMap.put(ontology, aliases);
        }
*/
        populateStageInformation(ontology);

        LOG.info("populate stage info: "+ (System.currentTimeMillis() - nextTime) );
        nextTime = System.currentTimeMillis() ;

        long endTime = System.currentTimeMillis();
        logLoading(ontology, startTime, endTime, dateStarted, termMap.size(), obsoleteNameMap.size(), numberOfAliases);
    }

    private int getNumberOfAliases(Map<String, List<TermAlias>> aliasMap) {
        if (aliasMap == null)
            return 0;
        int count = 0;
        for (String alias : aliasMap.keySet()) {
            List<TermAlias> termAliases = aliasMap.get(alias);
            if (termAliases != null)
                count += termAliases.size();
        }
        return count;
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

        for (Term term : getAllTerms(ontology)) {
            DevelopmentStage start = RepositoryFactory.getAnatomyRepository().getStartStage(term.getOboID());
            DevelopmentStage end = RepositoryFactory.getAnatomyRepository().getEndStage(term.getOboID());
            term.setStart(start);
            term.setEnd(end);
        }
    }

    /**
     * Retrieve the complete list of terms for a given ontology.
     *
     * @param ontology Ontology
     * @return a complete list of terms in the ontology
     */
    public List<Term> getAllTerms(Ontology ontology) {
        Map<String, Term> termMap = getTermOntologyMap(ontology);
        if (termMap == null)
            return null;

        List<Term> terms = new ArrayList<Term>(termMap.size());
        for (Term term : termMap.values()) {
            terms.add(term);
        }
        return terms;
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
        Map<String, List<TermAlias>> aliasMap = new TreeMap<String, List<TermAlias>>(new NumberAwareStringComparator());
        for (TermAlias termAlias : termAliasList) {
            insertTermAlias(allTerms, aliasMap, termAlias);
        }
        return aliasMap;
    }

    private void insertTermAlias(Collection<Term> allTerms, Map<String, List<TermAlias>> aliasMap, TermAlias termAlias) {
        List<TermAlias> existingAliases = aliasMap.get(termAlias.getAliasLowerCase());
        if (existingAliases == null)
            existingAliases = new ArrayList<TermAlias>(3);
        if (allTerms == null || allTerms.contains(termAlias.getTerm())) {
            existingAliases.add(termAlias);
            aliasMap.put(termAlias.getAliasLowerCase(), existingAliases);
        }
    }

    private void insertPureTermAlias(Map<String, List<TermAlias>> aliasMap, TermAlias termAlias) {
        List<TermAlias> existingAliases = aliasMap.get(termAlias.getAliasLowerCase());
        if (existingAliases == null)
            existingAliases = new ArrayList<TermAlias>(3);
        if (!existingAliases.contains(termAlias))
            existingAliases.add(termAlias);
        aliasMap.put(termAlias.getAliasLowerCase(), existingAliases);
    }

    private void logLoading(Ontology ontology, long startTime, long endTime, Date dateOfStart, int numOfTerms, int numOfObsoleteTerms, int numOfAliases) {
        long loadingTime = endTime - startTime;
        double loadingTimeSeconds = (double) loadingTime / MILLISECONDS_PER_SECOND;
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
     * Retrieve a term by ID. All ontologies will be searched through.
     * If no term is found it return null.
     *
     * @param termID term ID
     * @return term
     */
    public Term getTermByID(String termID) {
        for (Ontology ontology : ontologyTermNameIDMapping.keySet()) {
            String termName = getTermOntologyIDMapping(ontology).get(termID);
            if (termName != null)
                return getTermByName(ontology, termName);
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
     * @return term
     */
    public Term getTermByName(Ontology ontology, String termName) {
        Term term = getTermOntologyMap(ontology).get(termName);
        // try the obsolete map of terms
        if (term == null) {
            term = getObsoleteTermMap(ontology).get(termName);
        }
        return term;

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
        double time = (double) (end - start) / MILLISECONDS_PER_SECOND;

        LOG.info("Time to serialize ontologies: " + time + " seconds");
    }

    public void serializeOntology(File serializeFile) {
        if (ontologyManager == null)
            getInstance();
        long start = System.currentTimeMillis();
        FileUtil.serializeObject(ontologyManager, serializeFile);
        long end = System.currentTimeMillis();
        double time = (double) (end - start) / MILLISECONDS_PER_SECOND;

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

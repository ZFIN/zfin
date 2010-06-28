package org.zfin.ontology.presentation;

import org.apache.commons.lang.StringUtils;
import org.zfin.infrastructure.TrieMultiMap;
import org.zfin.ontology.OntologyManager;
import org.zfin.ontology.Term;
import org.zfin.ontology.TransitiveClosure;

import java.util.*;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class OntologyBean {

    private String action;
    private boolean ontologiesLoaded = true;
    private OntologyManager ontologyManager;
    private String ontologyName;
    private Set<Term> terms;
    private String termID;
    private Term term;
    private Map<Term, List<String>> valueMap ;
    private TreeMap<String,Set<Term>> keys ;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getOntologyName() {
        return ontologyName;
    }

    public void setOntologyName(String ontologyName) {
        this.ontologyName = ontologyName;
    }

    public boolean isSerializeOntologies() {
        return action != null && ActionType.SERIALIZE_ONTOLOGIES.toString().equals(action);
    }

    public boolean isLoadOntologiesFromFile() {
        return action != null && ActionType.LOAD_FROM_SERIALIZED_FILE.toString().equals(action);
    }

    public boolean isLoadOntologiesFromDatabase() {
        return action != null && ActionType.LOAD_FROM_DATABASE.toString().equals(action);
    }

    public String getTermID() {
        return termID;
    }

    public void setTermID(String termID) {
        this.termID = termID;
    }

    public Term getTerm() {
        return term;
    }

    public void setTerm(Term term) {
        this.term = term;
    }

    public ActionType getActionType() {
        if (StringUtils.isEmpty(action))
            return null;
        return ActionType.getActionType(action);
    }

    public boolean isOntologiesLoaded() {
        return ontologiesLoaded;
    }

    public void setOntologiesLoaded(boolean ontologiesLoaded) {
        this.ontologiesLoaded = ontologiesLoaded;
    }

    public OntologyManager getOntologyManager() {
        return ontologyManager;
    }

    public void setOntologyManager(OntologyManager ontologyManager) {
        this.ontologyManager = ontologyManager;
    }

    public Set<Term> getTerms() {
        return terms;
    }

    public void setTerms(Set<Term> terms) {
        this.terms = terms;
    }

    public TreeMap<String, Set<Term>> getKeys() {
        return keys;
    }

    public void setKeys(TreeMap<String, Set<Term>> keys) {
        this.keys = keys;
    }

    public List<TransitiveClosure> getAllChildren(){
        return OntologyManager.getInstance().getAllChildren(term);
    }

    public Map<Term, List<String>> getValueMap() {
        return valueMap;
    }

    public void setValueMap(Map<Term, List<String>> valueMap) {
        this.valueMap = valueMap ;
    }

    public static enum ActionType {
        SERIALIZE_ONTOLOGIES,
        LOAD_FROM_DATABASE,
        LOAD_FROM_SERIALIZED_FILE,
        SHOW_ALIASES,
        SHOW_EXACT,
        SHOW_OBSOLETE_TERMS,
        SHOW_ALL_TERMS,
        SHOW_TERM, SHOW_KEYS, SHOW_VALUES;

        public static ActionType getActionType(String type) {
            for (ActionType t : values()) {
                if (t.toString().equals(type))
                    return t;
            }
            throw new RuntimeException("No action type of string " + type + " found.");
        }

        public String getName(){
            return name();
        }
    }
}

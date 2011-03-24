package org.zfin.ontology.presentation;

import org.apache.commons.lang.StringUtils;
import org.zfin.gwt.root.dto.TermDTO;
import org.zfin.ontology.*;

import java.util.*;

/**
 *
 * Bean used to view cached ontologies.
 */
public class OntologyBean {

    private String action;
    private boolean ontologiesLoaded = true;
    private String ontologyName;
    private Ontology ontology;
    private Set<TermDTO> terms;
    private String termID;
    private GenericTerm term;
    private Map<TermDTO, List<String>> valueMap ;
    private TreeMap<String,Set<TermDTO>> keys ;
    private List<TransitiveClosure> childrenTransitiveClosureSet ;
    private OntologyManager ontologyManager ;
    private List<RelationshipPresentation> termRelationships;
    private List<OntologyMetadata> metadataList;

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

    public GenericTerm getTerm() {
        return term;
    }

    public void setTerm(GenericTerm term) {
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

    public List<TermDTO> getOrderedTerms(){
        List<TermDTO> termList = new ArrayList<TermDTO>(terms);
        Collections.sort(termList);
        return termList;
    }

    public Set<TermDTO> getTerms() {
        return terms;
    }

    public void setTerms(Set<TermDTO> terms) {
        this.terms = terms;
    }

    public TreeMap<String, Set<TermDTO>> getKeys() {
        return keys;
    }

    public void setKeys(TreeMap<String, Set<TermDTO>> keys) {
        this.keys = keys;
    }

    public List<TransitiveClosure> getAllChildren(){
        return childrenTransitiveClosureSet ;
    }

    public void setAllChildren(List<TransitiveClosure> transitiveClosures){
        this.childrenTransitiveClosureSet = transitiveClosures;
    }

    public Map<TermDTO, List<String>> getValueMap() {
        return valueMap;
    }

    public void setValueMap(Map<TermDTO, List<String>> valueMap) {
        this.valueMap = valueMap;
    }

    public Ontology getOntology() {
        return ontology;
    }

    public void setOntology(Ontology ontology) {
        this.ontology = ontology;
    }

    public OntologyManager getOntologyManager() {
        return ontologyManager;
    }

    public void setOntologyManager(OntologyManager ontologyManager) {
        this.ontologyManager = ontologyManager;
    }

    public List<OntologyMetadata> getMetadataList() {
        return metadataList;
    }

    public void setMetadataList(List<OntologyMetadata> metadataList) {
        this.metadataList = metadataList;
    }

    public List<RelationshipPresentation> getTermRelationships() {
        return termRelationships;
    }

    public void setTermRelationships(List<RelationshipPresentation> termRelationships) {
        this.termRelationships = termRelationships;
    }

    public static enum ActionType {
        SERIALIZE_ONTOLOGIES,
        LOAD_FROM_DATABASE,
        LOAD_FROM_SERIALIZED_FILE,
        SHOW_ALIASES,
        SHOW_EXACT,
        SHOW_OBSOLETE_TERMS,
        SHOW_ALL_TERMS,
        SHOW_TERM,
        SHOW_KEYS,
        SHOW_VALUES,
        SHOW_RELATIONSHIP_TYPES;

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

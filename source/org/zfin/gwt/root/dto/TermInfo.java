package org.zfin.gwt.root.dto;

import com.google.gwt.user.client.rpc.IsSerializable;
import org.zfin.anatomy.presentation.RelationshipSorting;
import org.zfin.gwt.root.util.NumberAwareStringComparatorDTO;
import org.zfin.gwt.root.util.RelationshipComparatorDTO;

import java.util.*;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class TermInfo implements IsSerializable, Comparable<TermInfo> {

    private String ID;
    private String oboID;
    private String name;
    private List<String> synonyms;
    private String definition;
    private String startStage;
    private String endStage;
    private OntologyDTO ontology;
    private String comment;
    private boolean obsolete;
    private Map<String, List<TermInfo>> relatedTermInfos = new TreeMap<String, List<TermInfo>>(new RelationshipComparatorDTO());

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getOboID() {
        return oboID;
    }

    public void setOboID(String oboID) {
        this.oboID = oboID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(List<String> synonyms) {
        this.synonyms = synonyms;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getStartStage() {
        return startStage;
    }

    public void setStartStage(String startStage) {
        this.startStage = startStage;
    }

    public String getEndStage() {
        return endStage;
    }

    public void setEndStage(String endStage) {
        this.endStage = endStage;
    }

    public Map<String, List<TermInfo>> getRelatedTermInfos() {
        return relatedTermInfos;
    }

    public void setRelatedTermInfos(Map<String, List<TermInfo>> relatedTermInfos) {
        this.relatedTermInfos = relatedTermInfos;
    }

    public void addRelatedTermInfo(String relationship, TermInfo termInfo) {
        List<TermInfo> terms = relatedTermInfos.get(relationship);
        if (terms == null) {
            terms = new ArrayList<TermInfo>();
            relatedTermInfos.put(relationship, terms);
        }
        terms.add(termInfo);
    }

    public OntologyDTO getOntology() {
        return ontology;
    }

    public void setOntology(OntologyDTO ontology) {
        this.ontology = ontology;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isObsolete() {
        return obsolete;
    }

    public void setObsolete(boolean obsolete) {
        this.obsolete = obsolete;
    }

    @Override
    public int compareTo(TermInfo o) {
        NumberAwareStringComparatorDTO comparator = new NumberAwareStringComparatorDTO();
        return comparator.compare(getName(), o.getName());
    }
}

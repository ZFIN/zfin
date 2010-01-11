package org.zfin.gwt.root.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class TermInfo implements IsSerializable {

    private String ID;
    private String name;
    private String synonyms;
    private String definition;
    private String startStage;
    private String endStage;
    private Ontology ontology;
    private String comment;
    private Map<String, List<TermInfo>> relatedTermInfos = new HashMap<String, List<TermInfo>>();

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(String synonyms) {
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

    public Ontology getOntology() {
        return ontology;
    }

    public void setOntology(Ontology ontology) {
        this.ontology = ontology;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}

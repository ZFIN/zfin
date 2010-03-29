package org.zfin.ontology.presentation;

import org.apache.commons.lang.StringUtils;
import org.zfin.ontology.OntologyManager;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class OntologyBean {

    private String action;
    private boolean ontologiesLoaded = true;
    private OntologyManager ontologyManager;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
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

    public enum ActionType {
        SERIALIZE_ONTOLOGIES,
        LOAD_FROM_DATABASE, LOAD_FROM_SERIALIZED_FILE;

        public static ActionType getActionType(String type) {
            for (ActionType t : values()) {
                if (t.toString().equals(type))
                    return t;
            }
            throw new RuntimeException("No action type of string " + type + " found.");
        }

    }
}

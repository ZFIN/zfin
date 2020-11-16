package org.zfin.ontology.presentation;

public enum ActionType {
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
            if (t.toString().equals(type)) {
                return t;
            }
        }
        throw new RuntimeException("No action type of string " + type + " found.");
    }

    public String getName() {
        return name();
    }
}

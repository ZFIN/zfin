package org.zfin.ontology.presentation;

public enum OntologySection {
    EXPRESSION,
    PHENOTYPE;

    public static String[] getValues() {
        String[] values = new String[values().length];
        int index = 0;
        for (OntologySection section : values()) {
            values[index++] = section.toString();
        }
        return values;
    }
}

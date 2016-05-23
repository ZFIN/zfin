package org.zfin.infrastructure.presentation;

/**
 */
public class ControlledVocabularyAddBean {

    private String termName;
    private String foreignSpecies;
    private String nameDefinition;

    public String getTermName() {
        return termName;
    }

    public void setTermName(String termName) {
        this.termName = termName;
    }

    public String getForeignSpecies() {
        return foreignSpecies;
    }

    public void setForeignSpecies(String foreignSpecies) {
        this.foreignSpecies = foreignSpecies;
    }

    public String getNameDefinition() {
        return nameDefinition;
    }

    public void setNameDefinition(String nameDefinition) {
        this.nameDefinition = nameDefinition;
    }
}


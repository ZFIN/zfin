package org.zfin.framework.services;

public enum VocabularyEnum {
    TRANSCRIPT_ANNOTATION_METHOD("transcript annotation method");

    private String name;

    VocabularyEnum(String name) {
        this.name = name;
    }

    public String getName(){
        return name;
    }
}

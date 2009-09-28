package org.zfin.sequence.blast;

public class Origination {

    private Long id ;
    private Type type ;
    private String definition;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public static enum Type{
        CURATED,
        GENERATED,
        LOADED,
        EXTERNAL,
        MARKERSEQUENCE,
        ;
    }
}

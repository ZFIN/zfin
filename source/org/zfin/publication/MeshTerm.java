package org.zfin.publication;

public class MeshTerm implements Comparable<MeshTerm> {

    private String id;
    private String name;
    private Type type;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public int compareTo(MeshTerm o) {
        return this.getName().compareTo(o.getName());
    }

    public enum Type {
        DESCRIPTOR, QUALIFIER, SUPPLEMENTARY
    }
}

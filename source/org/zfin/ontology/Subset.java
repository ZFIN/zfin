package org.zfin.ontology;

import java.io.Serializable;

/**
 * Subset object. Each term can have zero, one or more subset definitions, i.e.
 * the terms belongs to a certain subset of the complete ontology.
 * E.g. relational_slim for 'fused with'
 */
public class Subset implements Serializable {

    public static final String RELATIONAL_SLIM = "relational_slim";
    
    private long id;
    private String internalName;
    // this is also called definition in the database
    private String name;
    private OntologyMetadata metaData;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getInternalName() {
        return internalName;
    }

    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public OntologyMetadata getMetaData() {
        return metaData;
    }

    public void setMetaData(OntologyMetadata metaData) {
        this.metaData = metaData;
    }
}

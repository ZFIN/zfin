package org.zfin.gwt.root.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * DTO of Subset in ontology module
 */
public enum SubsetDTO implements IsSerializable {

    RELATIONAL_SLIM("relational_slim");
    private String name;

    SubsetDTO(String value) {
        this.name = value;
    }

    public String toString(){
        return name;
    }
}

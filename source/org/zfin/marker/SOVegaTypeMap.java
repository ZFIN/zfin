package org.zfin.marker;

import java.io.Serializable;

public class SOVegaTypeMap implements Serializable {
    private Long id ;


    private String vegaType;
    private String tscriptType;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVegaType() {
        return vegaType;
    }

    public void setVegaType(String vegaType) {
        this.vegaType = vegaType;
    }

    public String getTscriptType() {
        return tscriptType;
    }

    public void setTscriptType(String tscriptType) {
        this.tscriptType = tscriptType;
    }




}

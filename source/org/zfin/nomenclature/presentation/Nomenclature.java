package org.zfin.nomenclature.presentation;

import java.util.HashMap;
import java.util.Map;

public class Nomenclature {

    private String comments;
    private String reason;
    private String name;
    private String abbreviation;
    private String newAlias;
    private String attribution;
    private Map<String, Object> meta = new HashMap<>();

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isGeneAbbreviationChange() {
        return abbreviation != null;
    }

    public boolean isGeneNameChange() {
        return name != null;
    }

    public String getAttribution() {
        return attribution;
    }

    public void setAttribution(String attribution) {
        this.attribution = attribution;
    }

    public String getNewAlias() {
        return newAlias;
    }

    public void setNewAlias(String newAlias) {
        this.newAlias = newAlias;
    }

    public void putMeta(String key, Object value) {
        meta.put(key, value);
    }

    public Map<String, Object> getMeta() {
        return meta;
    }

}

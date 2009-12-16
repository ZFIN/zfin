package org.zfin.sequence;

import java.util.Set;


public class Entrez {

    private String entrezAccNum;
    private String abbreviation;
    private String name;
    private Set<EntrezOMIM> relatedOMIMAccessions;

    public String getEntrezAccNum() {
        return entrezAccNum;
    }

    public void setEntrezAccNum(String entrezAccNum) {
        this.entrezAccNum = entrezAccNum;
    }

    public Set<EntrezProtRelation> getRelatedProteinAccessions() {
        return relatedProteinAccessions;
    }

    public void setRelatedProteinAccessions(Set<EntrezProtRelation> relatedProteinAccessions) {
        this.relatedProteinAccessions = relatedProteinAccessions;
    }

    private Set<EntrezMGI> relatedMGIAccessions;
    private Set<EntrezProtRelation> relatedProteinAccessions;

    public Set<EntrezOMIM> getRelatedOMIMAccessions() {
        return relatedOMIMAccessions;
    }

    public void setRelatedOMIMAccessions(Set<EntrezOMIM> relatedOMIMAccessions) {
        this.relatedOMIMAccessions = relatedOMIMAccessions;
    }

    public Set<EntrezMGI> getRelatedMGIAccessions() {
        return relatedMGIAccessions;
    }

    public void setRelatedMGIAccessions(Set<EntrezMGI> relatedMGIAccessions) {
        this.relatedMGIAccessions = relatedMGIAccessions;
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
}

package org.zfin.framework.presentation;

import java.util.ArrayList;
import java.util.List;

/**
 * This class facilitates the search result page in displaying matching entities.
 */
public class MatchingText {

    private String descriptor;
    private List<String> matchingTerms;
    private Type type;
    private List<String> matchedStrings = new ArrayList<String>();
    private String appendix;

    public MatchingText(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public String getDescriptor() {
        if (descriptor == null)
            return type.getName();

        String fullDescription = descriptor + " " + type.getName();
        if (appendix != null)
            fullDescription += " " + appendix;
        return fullDescription;
    }

    public void setDescriptor(String descriptor) {
        this.descriptor = descriptor;
    }

    public List<String> getMatchingTerms() {
        return matchingTerms;
    }

    public String getMatchingString() {
        if (matchingTerms == null)
            return null;
        StringBuilder sb = new StringBuilder();
        for (String entry : matchingTerms) {
            sb.append(entry);
            sb.append(", ");
        }
        return sb.deleteCharAt(sb.length() - 2).toString();
    }

    public void setMatchingTerms(List<String> matchingTerms) {
        this.matchingTerms = matchingTerms;
    }

    public List<String> getMatchedStrings() {
        return matchedStrings;
    }

    public void addMatchedString(String matchedString) {
        matchedStrings.add(matchedString);
    }

    public void addMatchingTerm(String term) {
        if (matchingTerms == null)
            matchingTerms = new ArrayList<String>();
        matchingTerms.add(term);

    }

    public String getAppendix() {
        return appendix;
    }

    public void setAppendix(String appendix) {
        this.appendix = appendix;
    }

    public enum Type {
        GENE_NAME("Gene Name"),
        GENE_ALIAS("Gene Prev. Name"),
        ANTIBODY_NAME("Name"),
        ANTIBODY_ALIAS("Alias"),
        AO_TERM("Anatomy"),
        PART_OF("part of");

        private String name;

        private Type(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}

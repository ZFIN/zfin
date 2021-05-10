package org.zfin.framework.presentation;

import org.zfin.util.MatchType;

/**
 * This class facilitates the search result page in displaying matching entities.
 */
public class MatchingText {

    private String descriptor;
    private String queryString;
    private MatchingTextType type;
    private String matchedString;
    private String appendix;
    private String relatedEntity;
    private MatchType matchingQuality = MatchType.STARTS_WITH;

    public MatchingText(MatchingTextType type) {
        this.type = type;
    }

    public MatchingTextType getType() {
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

    public String getQueryString() {
        return queryString;
    }

    public String getMatchedString() {
        return matchedString;
    }

    /**
     * Add a matching pair.
     *
     * @param matchedString string
     * @param queryString     string
     */
    public void addMatchingTermPair(String matchedString, String queryString) {
        this.matchedString = matchedString;
        this.queryString = queryString;
    }

/*
    public boolean addMatchingTermPairs(String matchedString, String queryTerm) {
        if (matchingTerms == null)
            matchingTerms = new ArrayList<String>();
        matchingTerms.add(queryTerm);
        matchedStrings.add(matchedString);
        ZfinSimpleTokenizer tokenizer = new ZfinSimpleTokenizer(queryTerm);
        while (tokenizer.hasNext()) {
            String tokenStr = tokenizer.next();
            if (matchedString.toLowerCase().equals(tokenStr.toLowerCase())) {
                matchingQuality = MatchType.EXACT;
                return true;
            }
        }
        return false;
    }

*/
    public MatchType getMatchingQuality() {
        return matchingQuality;
    }

    public void setMatchingQuality(MatchType matchingQuality) {
        this.matchingQuality = matchingQuality;
    }

    public String getAppendix() {
        return appendix;
    }

    public void setAppendix(String appendix) {
        this.appendix = appendix;
    }

    public void setRelatedEntity(String relatedEntity) {
        this.relatedEntity = relatedEntity;
    }

    public String getRelatedEntityDisplay() {
        if (relatedEntity == null)
            return "";
        return "[" + relatedEntity + "]";
    }

    public static boolean matchedTextContainsQuery(String matchedOnString, String query) {
        if (matchedOnString == null)
            return false;
        if (query == null)
            return false;
        matchedOnString = matchedOnString.toLowerCase();
        query = query.toLowerCase();
        return matchedOnString.contains(query);
    }

    @Override
    public String toString() {
        return "MatchingText{" +
                "descriptor='" + descriptor + '\'' +
                ", queryString='" + queryString + '\'' +
                ", type=" + type +
                ", matchedString='" + matchedString + '\'' +
                ", appendix='" + appendix + '\'' +
                ", relatedEntity='" + relatedEntity + '\'' +
                ", matchingQuality=" + matchingQuality +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MatchingText that = (MatchingText) o;

        if (appendix != null ? !appendix.equals(that.appendix) : that.appendix != null) return false;
        if (descriptor != null ? !descriptor.equals(that.descriptor) : that.descriptor != null) return false;
        if (matchedString != null ? !matchedString.equals(that.matchedString) : that.matchedString != null)
            return false;
        if (matchingQuality != that.matchingQuality) return false;
        if (queryString != null ? !queryString.equals(that.queryString) : that.queryString != null) return false;
        if (relatedEntity != null ? !relatedEntity.equals(that.relatedEntity) : that.relatedEntity != null)
            return false;
        if (type != that.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = descriptor != null ? descriptor.hashCode() : 0;
        result = 31 * result + (queryString != null ? queryString.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (matchedString != null ? matchedString.hashCode() : 0);
        result = 31 * result + (appendix != null ? appendix.hashCode() : 0);
        result = 31 * result + (relatedEntity != null ? relatedEntity.hashCode() : 0);
        result = 31 * result + (matchingQuality != null ? matchingQuality.hashCode() : 0);
        return result;
    }
}

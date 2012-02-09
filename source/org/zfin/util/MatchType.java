package org.zfin.util;

import org.zfin.framework.ZfinSimpleTokenizer;

public enum MatchType {

    EXACT("Exact match") {
        @Override
        public boolean isMatch(String matchString, String query) {
            if (matchString == null || query == null)
                return false;
            return matchString.equalsIgnoreCase(query);
        }
    },
    EXACT_WORD("Exact word match") {
        @Override
        public boolean isMatch(String matchString, String query) {
            if (matchString == null || query == null)
                return false;
            ZfinSimpleTokenizer queryTokenizer = new ZfinSimpleTokenizer(query);
            while (queryTokenizer.hasNext()) {
                String queryWord = queryTokenizer.next();
                ZfinSimpleTokenizer matchStringTokenizer = new ZfinSimpleTokenizer(matchString);
                boolean foundMatch = false;
                while (matchStringTokenizer.hasNext()) {
                    String matchWord = matchStringTokenizer.next();
                    // found a match, continue with next query word
                    if (EXACT.isMatch(queryWord, matchWord)) {
                        foundMatch = true;
                        break;
                    }
                }
                if (!foundMatch)
                    return false;
            }
            //found matches for all query words.
            return true;
        }
    },

    STARTS_WITH_WORDS("Starts with word match") {
        @Override
        public boolean isMatch(String matchString, String query) {
            if (matchString == null || query == null)
                return false;
            ZfinSimpleTokenizer queryTokenizer = new ZfinSimpleTokenizer(query);
            while (queryTokenizer.hasNext()) {
                String queryWord = queryTokenizer.next();
                ZfinSimpleTokenizer matchStringTokenizer = new ZfinSimpleTokenizer(matchString);
                boolean foundMatch = false;
                while (matchStringTokenizer.hasNext()) {
                    String matchWord = matchStringTokenizer.next();
                    // found a match, continue with next query word
                    if (STARTS_WITH.isMatch(matchWord, queryWord)) {
                        foundMatch = true;
                        break;
                    }
                }
                if (!foundMatch)
                    return false;
            }
            //found matches for all query words.
            return true;
        }
    },

    EXACT_MATCH_ON_SOME_WORDS("Exact match on some words") {
        @Override
        public boolean isMatch(String matchString, String query) {
            if (matchString == null || query == null)
                return false;
            ZfinSimpleTokenizer queryTokenizer = new ZfinSimpleTokenizer(query);
            boolean foundMatch = false;
            while (queryTokenizer.hasNext()) {
                String queryWord = queryTokenizer.next();
                ZfinSimpleTokenizer matchStringTokenizer = new ZfinSimpleTokenizer(matchString);
                while (matchStringTokenizer.hasNext()) {
                    String matchWord = matchStringTokenizer.next();
                    // found a match, continue with next query word
                    if (STARTS_WITH.isMatch(matchWord, queryWord)) {
                        foundMatch = true;
                        break;
                    }
                }
            }
            //found matches for all query words.
            return foundMatch;
        }
    },

    STARTS_WITH("Starts with") {
        @Override
        public boolean isMatch(String matchString, String query) {
            if (matchString == null || query == null)
                return false;
            return matchString.toLowerCase().startsWith(query.toLowerCase());
        }
    },
    CONTAINS("Contains") {
        @Override
        public boolean isMatch(String matchString, String query) {
            if (matchString == null || query == null)
                return false;
            return matchString.toLowerCase().contains(query.toLowerCase());
        }
    },
    SUBSTRUCTURE("Substructure") {
        @Override
        public boolean isMatch(String matchString, String query) {
            if (matchString == null || query == null)
                return false;
            return matchString.equalsIgnoreCase(query);
        }
    },
    MATCH_ON_FILTER("Matches on Filter") {
        @Override
        public boolean isMatch(String matchString, String query) {
            return true;
        }
    },
    NO_MATCH("No Match") {
        @Override
        public boolean isMatch(String matchString, String query) {
            return false;
        }
    };

    private String name;

    private MatchType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract boolean isMatch(String matchString, String query);
}
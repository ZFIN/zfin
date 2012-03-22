package org.zfin.util;

import org.zfin.framework.presentation.MatchingText;
import org.zfin.framework.presentation.MatchingTextType;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Term;

import java.util.*;

import static org.zfin.repository.RepositoryFactory.getOntologyRepository;

/**
 * This class facilitates matching of query strings with given text.
 */
public class MatchingService {

    private List<MatchingText> matchingTextList = new ArrayList<MatchingText>();
    private List<MatchType> matchTypeList;

    public MatchingService() {
        matchTypeList = new ArrayList<MatchType>(3);
        matchTypeList.add(MatchType.EXACT);
        matchTypeList.add(MatchType.STARTS_WITH);
        matchTypeList.add(MatchType.CONTAINS);
    }

    public MatchingService(MatchType... matchTypes) {
        if (matchTypes != null) {
            matchTypeList = new ArrayList<MatchType>(matchTypes.length);
            Collections.addAll(matchTypeList, matchTypes);
        }
    }

    public void addMatchingText(MatchingText matchingText) {
        matchingTextList.add(matchingText);
    }

    public Set<MatchingText> getMatchingTextList() {
        Set<MatchingText> nonDuplicates = new HashSet<MatchingText>(matchingTextList.size());
        nonDuplicates.addAll(matchingTextList);
        return nonDuplicates;
    }

    public MatchType addMatchingText(String query, String matchedOnString, MatchingTextType type) {
        return addMatchingText(query, matchedOnString, type, null);
    }

    public MatchType addMatchingText(String query, String matchedOnString, MatchingTextType type, String relatedEntityName) {
        MatchType matchType = checkMatch(query, matchedOnString);
        if (matchType.equals(MatchType.NO_MATCH))
            return MatchType.NO_MATCH;

        MatchingText match = new MatchingText(type);
        match.addMatchingTermPair(matchedOnString, query);
        match.setMatchingQuality(matchType);
        if (relatedEntityName != null)
            match.setRelatedEntity(relatedEntityName);
        matchingTextList.add(match);
        return matchType;
    }

    public void addMatchingOnFilter(MatchingTextType type, boolean isFound) {
        addMatchingOnFilter(type, isFound, "");
    }

    public void addMatchingOnFilter(MatchingTextType type, boolean isFound, String text) {
        MatchingText match = new MatchingText(type);
        match.setMatchingQuality(MatchType.MATCH_ON_FILTER);
        match.addMatchingTermPair(text, "");
        matchingTextList.add(match);
    }

    /**
     * Returns true if the match is exact on a whole word.
     *
     * @param query           query that is compared to an entity
     * @param matchedOnString the entity the query is compared to
     * @return MatchType
     */
    public MatchType checkMatch(String query, String matchedOnString) {
        // go through matching types one at a time in the order
        // given
        for (MatchType matchType : matchTypeList) {
            if (matchType.isMatch(matchedOnString, query))
                return matchType;
        }
        return MatchType.NO_MATCH;
    }

    public void addMatchingOntologyTerm(String termName, MatchingTextType type) {
        MatchingText match = new MatchingText(type);
        match.addMatchingTermPair(termName, termName);
        match.setMatchingQuality(MatchType.EXACT);
        matchingTextList.add(match);
    }

    public void addMatchingSubstructureOntologyTerm(String termName, String parentTermName, MatchingTextType type) {
        MatchingText match = new MatchingText(type);
        match.addMatchingTermPair(termName, parentTermName);
        match.setMatchingQuality(MatchType.SUBSTRUCTURE);
        match.setRelatedEntity(parentTermName);
        matchingTextList.add(match);

    }

    /**
     * Checks if a query term matches a given set of terms. If so
     * it returns true, otherwise false.
     *
     * @param termID query term id
     * @param terms  set of terms to check
     * @return true or false
     */
    public boolean checkExactTermMatches(String termID, Set<Term> terms) {
        if (terms != null) {
            for (Term term : terms) {
                if (term.getZdbID().equals(termID)) {
                    addMatchingOntologyTerm(term.getTermName(), MatchingTextType.AO_TERM);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a query term matches a given set of terms or its substructures.
     * If so returns true otherwise false.
     *
     * @param queryTermID query term id
     * @param terms       set of terms to check
     * @return true or false
     */
    public boolean checkSubstructureTermMatches(String queryTermID, Set<Term> terms) {
        if (terms != null) {
            GenericTerm queryTerm = getOntologyRepository().getTermByZdbID(queryTermID);
            for (Term term : terms) {
                if (getOntologyRepository().isParentChildRelationshipExist(queryTerm, term)) {
                    addMatchingSubstructureOntologyTerm(term.getTermName(), queryTerm.getTermName(), MatchingTextType.AO_TERM);
                    return true;
                }
            }
        }
        return false;
    }

    public void addAppendixToLastMatch(String appendix) {
        matchingTextList.get(matchingTextList.size() - 1).setAppendix(appendix);

    }

    public void addMatchingType(MatchType type) {
        if (!matchTypeList.contains(type))
            matchTypeList.add(type);
    }

    public void removeMatchingType(MatchType type) {
        if (matchTypeList.contains(type))
            matchTypeList.remove(type);

    }
}

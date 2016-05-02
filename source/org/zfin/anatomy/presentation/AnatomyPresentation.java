package org.zfin.anatomy.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.zfin.ontology.*;
import org.zfin.util.ListFormatter;

import java.util.*;

/**
 * A helper class that provides methods that format anatomy items or
 * developmental stage objects according to certain presentation rules.
 */
public final class AnatomyPresentation {


    /**
     * Group all relationships by type
     *
     * @param relationshipTypes relationship types
     * @param term              term
     * @return list of Presentation objects
     */
    public static List<RelationshipPresentation> createRelationshipPresentation(List<String> relationshipTypes,
                                                                                GenericTerm term) {
        if (relationshipTypes == null)
            return null;
        if (term == null)
            return null;

        List<RelationshipPresentation> relList = new ArrayList<RelationshipPresentation>();
        for (String type : relationshipTypes) {
            RelationshipPresentation rel = new RelationshipPresentation();
            List<GenericTerm> items = new ArrayList<>();
            rel.setType(type);
            for (GenericTermRelationship relatedItem : term.getAllDirectlyRelatedTerms()) {
                if (relatedItem.getType().equals(type)) {
                    items.add(relatedItem.getTermTwo());
                }
            }
            if (!items.isEmpty()) {
                rel.setTerms(items);
                relList.add(rel);
            }
        }
        return relList;
    }

    /**
     * Generate a comma (including a white space) delimited list of synonyms  of an anatomy item.
     * If no item found return empty string.
     * It sorts the names alphabetically.
     *
     * @param term anatomy term
     * @return string
     */
    public static String createFormattedSynonymList(Term term) {
        Set<TermAlias> synonyms = sortSynonyms(term);
        if (synonyms == null) {
            return "";
        }
        ListFormatter list = new ListFormatter();
        for (TermAlias synonym : synonyms) {
            list.addItem(synonym.getAlias());
        }
        return list.getFormattedString();
    }

    /**
     * Create a formatted string list, comma-delimited (no spaces) and each entry
     * escaped with single quotes.
     *
     * @param items string list
     * @return string
     */
    public static String createFormattedList(Collection<String> items) {
        ListFormatter formatter = new ListFormatter(",", '\"');
        formatter.addStringList(items);
        return formatter.getFormattedString();
    }

    /**
     * @param term anatomy term
     * @return set of synonyms
     */
    private static Set<TermAlias> sortSynonyms(Term term) {
        Set<TermAlias> unsortedSynonyms = term.getAliases();
        if (CollectionUtils.isEmpty(unsortedSynonyms)) {
            return null;
        }
        Set<TermAlias> synonyms = new TreeSet<TermAlias>(new AnatomySynonymSorting());
        for (TermAlias synonym : unsortedSynonyms) {
            synonyms.add(synonym);
        }
        return synonyms;
    }

    /**
     * Create a list of terms that match a given query string.
     * The match can either be on the term name or any of the synonyms of the term.
     * <p/>
     * If the query string is null it is interpreted as an empty string
     *
     * @param terms       list of ao terms
     * @param queryString string
     * @return list of auto-complete terms
     *         empty collection if
     *         1) terms == null
     *         `             2) terms.size() = 0
     */
    public static List<AnatomyAutoCompleteTerm> getAnatomyTermList(List<Term> terms, String queryString) {
        List<AnatomyAutoCompleteTerm> list = new ArrayList<AnatomyAutoCompleteTerm>();

        if (terms == null || terms.size() == 0)
            return list;

        if (queryString == null)
            queryString = "";

        // to make the matching case insensitive
        queryString = queryString.toLowerCase().trim();

        for (Term anatomyItem : terms) {
            String term = anatomyItem.getTermName();
            AnatomyAutoCompleteTerm autoCompleteTerm = new AnatomyAutoCompleteTerm(term);
            autoCompleteTerm.setID(anatomyItem.getZdbID());
            // if a match is found on the term name skip the synonym matching logic.
            if (term.toLowerCase().contains(queryString)) {
                list.add(autoCompleteTerm);
                continue;
            }

            // try to fins a match on a synonym
            // stop on the first match found
            Set<TermAlias> synonyms = sortSynonyms(anatomyItem);

            if (synonyms != null) {
                for (TermAlias synonym : synonyms) {
                    String synonymName = synonym.getAlias();
                    if (synonymName.toLowerCase().contains(queryString)) {
                        autoCompleteTerm.setSynonymName(synonymName);
                        list.add(autoCompleteTerm);
                        break;
                    }
                }
            }
        }
        return list;
    }

    /**
     * Inner class: Comparator that compares the alias names of the
     * and orders them alphabetically.
     */
    public static class AnatomySynonymSorting implements Comparator<TermAlias> {

        public int compare(TermAlias synOne, TermAlias synTwo) {

            int aliassig1 = synOne.getAliasGroup().getSignificance();

            int aliassig2 = synTwo.getAliasGroup().getSignificance();
            String alias = synOne.getAlias();
            String alias1 = synTwo.getAlias();

            if (aliassig1 < aliassig2)
                return -1;
            else if (aliassig1 > aliassig2)
                return 1;
            else if (aliassig1 == aliassig2)
                return alias.compareToIgnoreCase(alias1);
            else
                return 0;
        }
    }
}

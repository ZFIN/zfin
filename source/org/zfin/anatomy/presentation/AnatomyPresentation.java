package org.zfin.anatomy.presentation;

import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.AnatomyRelationship;
import org.zfin.anatomy.AnatomySynonym;
import org.zfin.util.ListFormatter;

import java.util.*;

/**
 * A helper class that provides methods that format anatomy items or
 * developmental stage objects according to certain presentation rules.
 */
public final class AnatomyPresentation {


    /**
     * Group all relationships that
     *
     * @param relationshipTypes relationship types
     * @param anatomyItem       anatomy term
     * @return list of Presentation objects
     */
    public static List<RelationshipPresentation> createRelationshipPresentation(List<String> relationshipTypes,
                                                                                AnatomyItem anatomyItem) {
        if (relationshipTypes == null)
            return null;
        if (anatomyItem == null)
            return null;

        List<RelationshipPresentation> relList = new ArrayList<RelationshipPresentation>();
        for (String type : relationshipTypes) {
            RelationshipPresentation rel = new RelationshipPresentation();
            List<AnatomyItem> items = new ArrayList<AnatomyItem>();
            rel.setType(type);
            for (AnatomyRelationship relatedItem : anatomyItem.getRelatedItems()) {
                if (relatedItem.getRelationship().equals(type)) {
                    items.add(relatedItem.getAnatomyItem());
                }
            }
            if (items.size() > 0) {
                rel.setItems(items);
                relList.add(rel);
            }
        }
        return relList;
    }

    /**
     * Generate a comma (including a white space) delimited list of synomys of an anatomy item.
     * If no item found return empty string.
     * It sorts the names alphabetically.
     *
     * @param anatomyItem anatomy term
     * @return string
     */
    public static String createFormattedSynonymList(AnatomyItem anatomyItem) {
        Set<AnatomySynonym> synonyms = sortSynonyms(anatomyItem);
        if (synonyms == null) {
            return "";
        }
        ListFormatter list = new ListFormatter();
        for (AnatomySynonym synonym : synonyms) {
            list.addItem(synonym.getName());
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
    public static String createFormattedList(List<String> items) {
        ListFormatter formatter = new ListFormatter(",", '\"');
        formatter.addStringList(items);
        return formatter.getFormattedString();
    }

    /**
     * @param anatomyItem anatomy term
     * @return set of synonyms
     */
    private static Set<AnatomySynonym> sortSynonyms(AnatomyItem anatomyItem) {
        Set syns = anatomyItem.getSynonyms();
        if (syns == null)
            return null;
        Set<AnatomySynonym> synonyms = new TreeSet<AnatomySynonym>(new SynonymSorting());
        for (Object syn : syns) {
            AnatomySynonym synonym = (AnatomySynonym) syn;
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
     * @return list of autocomplete terms
     *         empty collection if
     *         1) terms == null
     *         `             2) terms.size() = 0
     */
    public static List<AnatomyAutoCompleteTerm> getAnatomyTermList(List<AnatomyItem> terms, String queryString) {
        List<AnatomyAutoCompleteTerm> list = new ArrayList<AnatomyAutoCompleteTerm>();

        if (terms == null || terms.size() == 0)
            return list;

        if (queryString == null)
            queryString = "";

        // to make the matching case insensitive
        queryString = queryString.toLowerCase().trim();

        for (AnatomyItem anatomyItem : terms) {
            String term = anatomyItem.getName();
            AnatomyAutoCompleteTerm autoCompleteTerm = new AnatomyAutoCompleteTerm(term);
            autoCompleteTerm.setID(anatomyItem.getZdbID());
            // if a match is found on the term name skip the synonym matching logic.
            if (term.toLowerCase().contains(queryString)) {
                list.add(autoCompleteTerm);
                continue;
            }

            // try to fins a match on a synonym
            // stop on the first match found
            Set<AnatomySynonym> synonyms = sortSynonyms(anatomyItem);

            if (synonyms != null) {
                for (AnatomySynonym synonym : synonyms) {
                    String synonymName = synonym.getName();
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
     * Inner class: Comparator that compares the alias names of the AnatomySynonym
     * and orders them alphabetically.
     */
    public static class SynonymSorting implements Comparator<AnatomySynonym> {

        public int compare(AnatomySynonym synOne, AnatomySynonym synTwo) {

            int aliassig1 = synOne.getAliasGroup().getSignificance();

            int aliassig2 = synTwo.getAliasGroup().getSignificance();
            String alias = synOne.getName();
            String alias1 = synTwo.getName();

            if(aliassig1 < aliassig2)
                return -1;
            else if(aliassig1 > aliassig2)
                return 1;
            else if(aliassig1 == aliassig2)
               return alias.compareToIgnoreCase(alias1);
            else
            return 0;
        }
    }
}
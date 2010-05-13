package org.zfin.ontology;

import org.zfin.anatomy.presentation.RelationshipSorting;
import org.zfin.gwt.root.dto.OntologyDTO;
import org.zfin.ontology.presentation.OntologyAutoCompleteTerm;
import org.zfin.util.ListFormatter;

import java.util.*;

/**
 * A helper class that provides methods that format anatomy items or
 * developmental stage objects according to certain presentation rules.
 */
public final class OntologyService {

    /**
     * Generate a comma (including a white space) delimited list of synomys of an anatomy item.
     * If no item found return empty string.
     * It sorts the names alphabetically.
     *
     * @param anatomyItem anatomy term
     * @return string
     */
    public static String createFormattedSynonymList(Term anatomyItem) {
        Set<TermAlias> synonyms = sortSynonyms(anatomyItem);
        if (synonyms == null) {
            return "";
        }
        ListFormatter list = new ListFormatter();
        for (TermAlias synonym : synonyms) {
            list.addItem(synonym.getAlias());
        }
        return list.getFormattedString();
    }

    public static List<String> createSortedSynonymsFromTerm(Term term) {
        Set<TermAlias> synonyms = sortSynonyms(term);
        if (synonyms == null) {
            return null;
        }
        List<String> list = new ArrayList<String>();
        for (TermAlias synonym : synonyms) {
            list.add(synonym.getAlias());
        }
        return list;
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
    private static Set<TermAlias> sortSynonyms(Term anatomyItem) {
        if (anatomyItem.getAliases() == null)
            return null;
        Set aliases = anatomyItem.getAliases();
        Set<TermAlias> synonyms = new TreeSet<TermAlias>(new SynonymSorting());
        for (Object syn : aliases) {
            TermAlias synonym = (TermAlias) syn;
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
    public static List<OntologyAutoCompleteTerm> getTermList(List<GenericTerm> terms, String queryString) {
        List<OntologyAutoCompleteTerm> list = new ArrayList<OntologyAutoCompleteTerm>();

        if (terms == null || terms.size() == 0)
            return list;

        if (queryString == null)
            queryString = "";

        // to make the matching case insensitive
        queryString = queryString.toLowerCase().trim();

        for (GenericTerm genericTerm : terms) {
            String term = genericTerm.getTermName();
            OntologyAutoCompleteTerm autoCompleteTerm = new OntologyAutoCompleteTerm(term);
            autoCompleteTerm.setID(genericTerm.getID());
            // if a match is found on the term name skip the synonym matching logic.
            if (term.toLowerCase().contains(queryString)) {
                list.add(autoCompleteTerm);
                continue;
            }

            // try to find a match on a synonym
            // stop on the first match found
            Set<TermAlias> synonyms = sortSynonyms(genericTerm);

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

    public static List<RelationshipPresentation> getRelatedTerms(Term term) {
        Set<String> types = new HashSet<String>(5);
        List<TermRelationship> relatedItems = term.getRelatedTerms();
        List<String> uniqueTypes = new ArrayList<String>(5);
        if (relatedItems != null) {
            for (TermRelationship rel : relatedItems) {
                types.add(rel.getRelationshipType().getTypeName());
            }
        }
        Collections.sort(uniqueTypes, new RelationshipSorting());
        return createRelationshipPresentation(types, term);
    }

    /**
     * Group all relationships that
     *
     * @param relationshipTypes relationship types
     * @param term              anatomy term
     * @return list of Presentation objects
     */
    public static List<RelationshipPresentation> createRelationshipPresentation(Set<String> relationshipTypes,
                                                                                Term term) {
        if (relationshipTypes == null)
            return null;
        if (term == null)
            return null;

        List<RelationshipPresentation> relList = new ArrayList<RelationshipPresentation>(10);
        for (String type : relationshipTypes) {
            RelationshipPresentation rel = new RelationshipPresentation();
            List<Term> items = new ArrayList<Term>(10);
            rel.setType(type);
            for (TermRelationship relatedItem : term.getRelatedTerms()) {
                if (relatedItem.getRelationshipType().getTypeName().equals(type)) {
                    items.add(relatedItem.getRelatedTerm(term));
                }
            }
            if (!items.isEmpty()) {
                rel.setItems(items);
                relList.add(rel);
            }
        }
        return relList;
    }

    public static Ontology convertOntology(OntologyDTO ontology) {
        if (ontology == null)
            return null;

        switch (ontology) {
            case ANATOMY:
                return Ontology.ANATOMY;
            case GO_CC:
                return Ontology.GO_CC;
            case GO:
                return org.zfin.ontology.Ontology.GO;
        }
        return null;
    }


    /**
     * Inner class: Comparator that compares the alias names of the AnatomySynonym
     * and orders them alphabetically.
     */
    public static class SynonymSorting implements Comparator<TermAlias> {

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

package org.zfin.anatomy.presentation;

import org.junit.Test;
import org.zfin.infrastructure.DataAliasGroup;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.GenericTermRelationship;
import org.zfin.ontology.Term;
import org.zfin.ontology.TermAlias;

import java.util.*;

import static org.junit.Assert.*;

@SuppressWarnings({"FeatureEnvy"})
public class AnatomyPresentationTest {

    @Test
    public void displayRelationshipsOnDetailPage() {
        List<String> types = new ArrayList<>(10);
        String typeOne = "develops into";
        String typeTwo = "develops from";
        String typeThree = "has instance";
        String typeFour = "is a";
        String typeFive = "has part";
        String typeSix = "part of";
        types.add(typeOne);
        types.add(typeTwo);
        types.add(typeThree);
        types.add(typeFour);
        types.add(typeFive);
        types.add(typeSix);

        GenericTerm item = new GenericTerm();
        String zdbID = "ZDB-TERM-051116-41";
        item.setZdbID(zdbID);
        item.setTermName("amacrine cell");

        GenericTerm relatedItemOne = new GenericTerm();
        relatedItemOne.setZdbID("ZDB-TERM-010921-544");
        relatedItemOne.setTermName("inner nuclear layer");

        GenericTermRelationship arOne = new GenericTermRelationship();
        arOne.setType("is a");
        arOne.setTermOne(item);
        arOne.setTermTwo(relatedItemOne);
        // add inverse relationship
        GenericTermRelationship arOneInverse = getInverseRelationship(arOne, typeThree);
        Set<GenericTermRelationship> arOneInverseRels = getSetFromItem(arOneInverse);
        relatedItemOne.setParentTermRelationships(arOneInverseRels);

        GenericTerm relatedItemTwo = new GenericTerm();
        relatedItemTwo.setZdbID("ZDB-TERM-010921-8897");
        relatedItemTwo.setTermName("yolk");

        GenericTermRelationship arTwo = new GenericTermRelationship();
        arTwo.setType("is a");
        arTwo.setTermOne(item);
        arTwo.setTermTwo(relatedItemTwo);


        GenericTerm relatedItemThree = new GenericTerm();
        relatedItemThree.setZdbID("ZDB-Term-010921-8897");
        relatedItemThree.setTermName("yolk");

        GenericTermRelationship arThree = new GenericTermRelationship();
        arThree.setType("part of");
        arThree.setTermOne(item);
        arThree.setTermTwo(relatedItemThree);

        Set<GenericTermRelationship> relatedItems = new TreeSet<>();
        relatedItems.add(arOne);
        relatedItems.add(arTwo);
        relatedItems.add(arThree);
        item.setChildTermRelationships(relatedItems);


        List<RelationshipPresentation> presentations = AnatomyPresentation.createRelationshipPresentation(types, item);
        assertEquals("Total Number of Relationships", 2, presentations.size());

        RelationshipPresentation presentation = presentations.get(0);
        assertEquals("Relation Ship", "is a", presentation.getType());

        List<GenericTerm> relItems = presentation.getTerms();
        assertEquals("Number of Items for 'is a'", 2, relItems.size());

        Term relItem = relItems.get(0);
        assertEquals("Name of First Anatomy Item", "inner nuclear layer", relItem.getTermName());

        Term relItemTwo = relItems.get(1);
        assertEquals("Name of Second Anatomy Item", "yolk", relItemTwo.getTermName());

        RelationshipPresentation presentationTwo = presentations.get(1);
        assertEquals("Relation Ship", "part of", presentationTwo.getType());

    }

    private Set<GenericTermRelationship> getSetFromItem(GenericTermRelationship relationship) {
        Set<GenericTermRelationship> relationshipSet = new HashSet<>(1);
        relationshipSet.add(relationship);
        return relationshipSet;
    }

    private GenericTermRelationship getInverseRelationship(GenericTermRelationship arOne, String type) {
        GenericTermRelationship relationship = new GenericTermRelationship();
        relationship.setTermOne(arOne.getTermTwo());
        relationship.setTermTwo(arOne.getTermOne());
        relationship.setType(type);
        return relationship;
    }

    /**
     * Create a list of four synonyms. The list should be a comma delimited
     * String with white spaces added after the comma.
     * The list uses the synonym comparator to order the items (alphabetically).
     */
    @Test
    public void multipleSynonymList() {
        Term item = new GenericTerm();
        DataAliasGroup dag1 = new DataAliasGroup();
        dag1.setName("exact alias");
        dag1.setSignificance(1);
        TermAlias syn1 = new TermAlias();
        syn1.setAlias("first");
        syn1.setDataZdbID("ZDB-TERM-080911-1");

        syn1.setAliasGroup(dag1);
        TermAlias syn2 = new TermAlias();
        DataAliasGroup dag2 = new DataAliasGroup();
        dag2.setName("related alias");
        dag2.setSignificance(3);
        syn2.setAlias("second");
        syn2.setDataZdbID("ZDB-TERM-080911-1");
        syn2.setAliasGroup(dag2);
        TermAlias syn3 = new TermAlias();
        DataAliasGroup dag3 = new DataAliasGroup();
        dag3.setName("exact plural");
        dag3.setSignificance(2);
        syn3.setAlias("third");
        syn3.setAliasGroup(dag3);
        syn3.setDataZdbID("ZDB-TERM-080911-1");
        TermAlias syn4 = new TermAlias();
        DataAliasGroup dag4 = new DataAliasGroup();
        dag4.setName("related plural");
        dag4.setSignificance(4);
        syn4.setAlias("fourth");
        syn4.setAliasGroup(dag4);
        syn4.setDataZdbID("ZDB-TERM-080911-13");
        Set<TermAlias> synonyms = new HashSet<>(4);
        synonyms.add(syn1);
        synonyms.add(syn2);
        synonyms.add(syn3);
        synonyms.add(syn4);
        item.setAliases(synonyms);

        String list = AnatomyPresentation.createFormattedSynonymList(item);
        assertEquals("Four elements, three commas", "first, third, second, fourth", list);
    }

    /**
     * Create a list of synonyms with a single item. No comma and no white space
     * should be added.
     */
    @Test
    public void singleSynonymList() {
        Term item = new GenericTerm();
        TermAlias syn1 = new TermAlias();
        syn1.setAlias("first");
        syn1.setDataZdbID("ZDB-TERM-080911-1");
        DataAliasGroup group = new DataAliasGroup();
        group.setSignificance(1);
        syn1.setAliasGroup(group);
        Set<TermAlias> synonyms = new HashSet<>(1);
        synonyms.add(syn1);
        item.setAliases(synonyms);

        String list = AnatomyPresentation.createFormattedSynonymList(item);
        assertEquals("One element, no commas", "first", list);
    }

    /**
     * Create a list of synonyms with no items. It should create an empty string.
     */
    @Test
    public void emptySynonymList() {
        Term item = new GenericTerm();

        String list = AnatomyPresentation.createFormattedSynonymList(item);
        assertEquals("No elements, no commas", "", list);
    }

    @Test
    public void emptyAoAutoCompleteTermList() {

        List<AnatomyAutoCompleteTerm> list = AnatomyPresentation.getAnatomyTermList(null, null);
        assertEquals(0, list.size());

        list = AnatomyPresentation.getAnatomyTermList(null, "");
        assertEquals(0, list.size());

        list = AnatomyPresentation.getAnatomyTermList(null, "harry");
        assertEquals(0, list.size());
    }

    /**
     * one and two terms, no synonyms, no query
     * should return the complete list.
     */
    @Test
    public void aoAutoCompleteTermListNoQuery() {

        List<Term> terms = new ArrayList<>(1);
        Term term = new GenericTerm();
        term.setTermName("neural rod");
        terms.add(term);

        List<AnatomyAutoCompleteTerm> list = AnatomyPresentation.getAnatomyTermList(terms, null);
        assertEquals(terms.size(), list.size());
        for (AnatomyAutoCompleteTerm autoTerm : list)
            assertEquals(true, autoTerm.isMatchOnTermName());

        GenericTerm termTwo = new GenericTerm();
        termTwo.setTermName("retina");
        terms.add(termTwo);

        list = AnatomyPresentation.getAnatomyTermList(terms, null);
        assertEquals(terms.size(), list.size());
        for (AnatomyAutoCompleteTerm autoTerm : list)
            assertEquals(true, autoTerm.isMatchOnTermName());

    }

    /**
     * one and two terms, no synonyms, no query
     * should return the complete list.
     */
    @Test
    public void aoAutoCompleteTermListWithQuery() {

        String query = "neur";
        List<Term> terms = new ArrayList<>(2);
        Term term = new GenericTerm();
        String firstTermName = "neural rod";
        term.setTermName(firstTermName);
        Term termTwo = new GenericTerm();
        termTwo.setTermName("retina");
        terms.add(term);
        terms.add(termTwo);

        // only first term matches
        List<AnatomyAutoCompleteTerm> list = AnatomyPresentation.getAnatomyTermList(terms, query);
        assertEquals(1, list.size());
        for (AnatomyAutoCompleteTerm autoTerm : list)
            assertEquals(true, autoTerm.isMatchOnTermName());

        TermAlias syn = new TermAlias();
        syn.setAlias("neuron");
        syn.setDataZdbID("ZDB-TERM-080911-1");
        DataAliasGroup group = new DataAliasGroup();
        group.setSignificance(1);
        syn.setAliasGroup(group);
        Set<TermAlias> syns = new HashSet<>(1);
        syns.add(syn);
        termTwo.setAliases(syns);

        // match on first term and second terms synonym
        list = AnatomyPresentation.getAnatomyTermList(terms, query);
        assertEquals(2, list.size());
        for (AnatomyAutoCompleteTerm autoTerm : list) {
            if (autoTerm.getTermName().equals(firstTermName))
                assertEquals(true, autoTerm.isMatchOnTermName());
            else
                assertEquals(false, autoTerm.isMatchOnTermName());

        }
    }

    /**
     * two terms, one with synonym one without
     * should return the complete list.
     * Check that matches are case-insensitive
     */
    @Test
    public void aoAutoCompleteWithQueryCaseInsensitive() {

        String query = "Neur";
        List<Term> terms = new ArrayList<>(2);
        Term term = new GenericTerm();
        String firstTermName = "neural rod";
        term.setTermName(firstTermName);
        Term termTwo = new GenericTerm();
        termTwo.setTermName("retina");
        terms.add(term);
        terms.add(termTwo);

        // only first term matches
        List<AnatomyAutoCompleteTerm> list = AnatomyPresentation.getAnatomyTermList(terms, query);
        assertEquals(1, list.size());
        for (AnatomyAutoCompleteTerm autoTerm : list)
            assertEquals(true, autoTerm.isMatchOnTermName());

        TermAlias syn = new TermAlias();
        syn.setAlias("neuron");
        syn.setDataZdbID("ZDB-TERM-080911-1");
        DataAliasGroup group = new DataAliasGroup();
        group.setSignificance(1);
        syn.setAliasGroup(group);
        Set<TermAlias> synonyms = new HashSet<>(1);
        synonyms.add(syn);
        termTwo.setAliases(synonyms);

        // match on first term and second terms synonym
        list = AnatomyPresentation.getAnatomyTermList(terms, query);
        assertEquals(2, list.size());
        for (AnatomyAutoCompleteTerm autoTerm : list) {
            if (autoTerm.getTermName().equals(firstTermName))
                assertEquals(true, autoTerm.isMatchOnTermName());
            else
                assertEquals(false, autoTerm.isMatchOnTermName());

        }

        syn = new TermAlias();
        syn.setAlias("Neuron");
        syn.setDataZdbID("ZDB-TERM-080911-1");
        syn.setAliasGroup(group);
        synonyms.clear();
        synonyms.add(syn);
        termTwo.setAliases(synonyms);

        // match on first term and second terms synonym
        list = AnatomyPresentation.getAnatomyTermList(terms, query);
        assertEquals(2, list.size());
        for (AnatomyAutoCompleteTerm autoTerm : list) {
            if (autoTerm.getTermName().equals(firstTermName))
                assertEquals(true, autoTerm.isMatchOnTermName());
            else
                assertEquals(false, autoTerm.isMatchOnTermName());

        }
    }

    /**
     * one term, one with synonym one without
     * should return the complete list.
     * Check that matches ignore white space at the end
     */
    @Test
    public void aoAutoCompleteWithQueryWhiteSpaceIgnore() {

        String query = "Neur  ";
        List<Term> terms = new ArrayList<>(2);
        Term term = new GenericTerm();
        String firstTermName = "neural rod";
        term.setTermName(firstTermName);
        Term termTwo = new GenericTerm();
        termTwo.setTermName("retina");
        terms.add(term);
        terms.add(termTwo);

        // only first term matches
        List<AnatomyAutoCompleteTerm> list = AnatomyPresentation.getAnatomyTermList(terms, query);
        assertEquals(1, list.size());
        for (AnatomyAutoCompleteTerm autoTerm : list)
            assertEquals(true, autoTerm.isMatchOnTermName());

        TermAlias syn = new TermAlias();
        syn.setAlias("neuron  ");
        syn.setDataZdbID("ZDB-TERM-080911-1");
        DataAliasGroup group = new DataAliasGroup();
        group.setSignificance(1);
        syn.setAliasGroup(group);
        Set<TermAlias> syns = new HashSet<>(1);
        syns.add(syn);
        termTwo.setAliases(syns);

        // match on first term and second terms synonym
        list = AnatomyPresentation.getAnatomyTermList(terms, query);
        assertEquals(2, list.size());
        for (AnatomyAutoCompleteTerm autoTerm : list) {
            if (autoTerm.getTermName().equals(firstTermName))
                assertEquals(true, autoTerm.isMatchOnTermName());
            else
                assertEquals(false, autoTerm.isMatchOnTermName());

        }

        syn = new TermAlias();
        syn.setAlias("Neuron");
        syn.setAliasGroup(group);
        syn.setDataZdbID("ZDB-TERM-080911-1");
        syns.clear();
        syns.add(syn);
        termTwo.setAliases(syns);

        // match on first term and second terms synonym
        list = AnatomyPresentation.getAnatomyTermList(terms, query);
        assertEquals(2, list.size());
        for (AnatomyAutoCompleteTerm autoTerm : list) {
            if (autoTerm.getTermName().equals(firstTermName))
                assertEquals(true, autoTerm.isMatchOnTermName());
            else
                assertEquals(false, autoTerm.isMatchOnTermName());

        }
    }

    @Test
    public void wildCard() {
        AnatomySearchBean anatomySearchBean = new AnatomySearchBean();
        anatomySearchBean.setSearchTerm("bob");
        assertEquals("bob", anatomySearchBean.getSearchTerm());
        assertFalse(anatomySearchBean.isWildCard());
        anatomySearchBean.setSearchTerm("bob*");
        assertTrue(anatomySearchBean.isWildCard());
        assertEquals("bob", anatomySearchBean.getSearchTerm());
    }
}

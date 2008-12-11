package org.zfin.anatomy.presentation;

import junit.framework.JUnit4TestAdapter;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.AnatomyRelationship;
import org.zfin.anatomy.AnatomySynonym;
import org.zfin.infrastructure.DataAliasGroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This is the test class that tests funcionality of the AnatomyPresentation class,
 * a helper class to provide convenience methods for presentation.
 */
public class AnatomyPresentationTest {

    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(AnatomyPresentationTest.class);
    }

    @Test
    public void DisplayRelationshipsOnDetailPage() {
        List<String> types = new ArrayList<String>();
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

        AnatomyItem item = new AnatomyItem();
        String zdbID = "ZDB-ANAT-051116-41";
        item.setZdbID(zdbID);
        item.setName("amacrine cell");
        AnatomyRelationship arOne = new AnatomyRelationship();
        arOne.setRelationship("is a");

        AnatomyItem relatedItemOne = new AnatomyItem();
        relatedItemOne.setZdbID("ZDB-ANAT-010921-544");
        relatedItemOne.setName("inner nuclear layer");
        arOne.setAnatomyItem(relatedItemOne);

        AnatomyRelationship arTwo = new AnatomyRelationship();
        arTwo.setRelationship("is a");

        AnatomyItem relatedItemTwo = new AnatomyItem();
        relatedItemTwo.setZdbID("ZDB-ANAT-010921-8897");
        relatedItemTwo.setName("yolk");
        arTwo.setAnatomyItem(relatedItemTwo);

        AnatomyRelationship arThree = new AnatomyRelationship();
        arThree.setRelationship("part of");

        AnatomyItem relatedItemThree = new AnatomyItem();
        relatedItemThree.setZdbID("ZDB-ANAT-010921-8897");
        relatedItemThree.setName("yolk");
        arThree.setAnatomyItem(relatedItemThree);

        List<AnatomyRelationship> relatedItems = new ArrayList<AnatomyRelationship>();
        relatedItems.add(arOne);
        relatedItems.add(arTwo);
        relatedItems.add(arThree);
        item.setRelatedItems(relatedItems);


        List<RelationshipPresentation> presentations = AnatomyPresentation.createRelationshipPresentation(types, item);
        assertEquals("Total Number of Relationships", 2, presentations.size());

        RelationshipPresentation presentation = presentations.get(0);
        assertEquals("Relation Ship", "is a", presentation.getType());

        List<AnatomyItem> relItems = presentation.getItems();
        assertEquals("Number of Items for 'is a'", 2, relItems.size());

        AnatomyItem relItem = relItems.get(0);
        assertEquals("Name of First Anatomy Item", "inner nuclear layer", relItem.getName());

        AnatomyItem relItemTwo = relItems.get(1);
        assertEquals("Name of Second Anatomy Item", "yolk", relItemTwo.getName());

        RelationshipPresentation presentationTwo = presentations.get(1);
        assertEquals("Relation Ship", "part of", presentationTwo.getType());

    }

    /**
     * Create a list of four synonyms. The list should be a comma delimited
     * String with white spaces added after the comma.
     * The list uses the sysnonym comparator to order the items (alpabetically).
     */
 @Test
    public void MulipleSynonymList() {
        AnatomyItem item = new AnatomyItem();
         DataAliasGroup dag1=new DataAliasGroup();
        dag1.setName("exact alias");
        dag1.setSignificance(1);
        AnatomySynonym syn1 = new AnatomySynonym();
        syn1.setName("first");

        syn1.setDAliasGroup(dag1);                                                                                                    
        AnatomySynonym syn2 = new AnatomySynonym();
        DataAliasGroup dag2=new DataAliasGroup();
        dag2.setName("related alias");
        dag2.setSignificance(3);
        syn2.setName("second");
        syn2.setDAliasGroup(dag2);
        AnatomySynonym syn3 = new AnatomySynonym();
        DataAliasGroup dag3=new DataAliasGroup();
        dag3.setName("exact plural");
        dag3.setSignificance(2);
        syn3.setName("third");
        syn3.setDAliasGroup(dag3);
        AnatomySynonym syn4 = new AnatomySynonym();
        DataAliasGroup dag4=new DataAliasGroup();
        dag4.setName("related plural");
        dag4.setSignificance(4);
        syn4.setName("fourth");
        syn4.setDAliasGroup(dag4);
        Set<AnatomySynonym> synonyms = new HashSet<AnatomySynonym>();
        synonyms.add(syn1);
        synonyms.add(syn2);
        synonyms.add(syn3);
        synonyms.add(syn4);
        item.setSynonyms(synonyms);

        String list = AnatomyPresentation.createFormattedSynonymList(item);
        assertEquals("Four elements, three commas", "first, third, second, fourth", list);
    }

    /**
     * Create a list of synonyms with a single item. No comma and no white space
     * should be added.
     */
    @Test
    public void SingleSynonymList() {
        AnatomyItem item = new AnatomyItem();
        AnatomySynonym syn1 = new AnatomySynonym();
        syn1.setName("first");
        Set<AnatomySynonym> synonyms = new HashSet<AnatomySynonym>();
        synonyms.add(syn1);
        item.setSynonyms(synonyms);

        String list = AnatomyPresentation.createFormattedSynonymList(item);
        assertEquals("One element, no commas", "first", list);
    }

    /**
     * Create a list of synonyms with no items. It should create an empty string.
     */
    @Test
    public void emptySynonymList() {
        AnatomyItem item = new AnatomyItem();

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

        List<AnatomyItem> terms = null;
    }

    /**
     * one and two terms, no synonyms, no query
     * should return the complete list.
     */
    @Test
    public void aoAutoCompleteTermListNoQuery() {

        List<AnatomyItem> terms = new ArrayList<AnatomyItem>();
        AnatomyItem term = new AnatomyItem();
        term.setName("neural rod");
        terms.add(term);

        List<AnatomyAutoCompleteTerm> list = AnatomyPresentation.getAnatomyTermList(terms, null);
        assertEquals(terms.size(), list.size());
        for (AnatomyAutoCompleteTerm autoTerm : list)
            assertEquals(true, autoTerm.isMatchOnTermName());

        AnatomyItem termTwo = new AnatomyItem();
        termTwo.setName("retina");
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
        List<AnatomyItem> terms = new ArrayList<AnatomyItem>();
        AnatomyItem term = new AnatomyItem();
        String firstTermName = "neural rod";
        term.setName(firstTermName);
        AnatomyItem termTwo = new AnatomyItem();
        termTwo.setName("retina");
        terms.add(term);
        terms.add(termTwo);

        // only first term matches
        List<AnatomyAutoCompleteTerm> list = AnatomyPresentation.getAnatomyTermList(terms, query);
        assertEquals(1, list.size());
        for (AnatomyAutoCompleteTerm autoTerm : list)
            assertEquals(true, autoTerm.isMatchOnTermName());

        AnatomySynonym syn = new AnatomySynonym();
        syn.setName("neuron");
        Set<AnatomySynonym> syns = new HashSet<AnatomySynonym>();
        syns.add(syn);
        termTwo.setSynonyms(syns);

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
    public void aoAutoCompleteTermListWithQueryCaseInsensitive() {

        String query = "Neur";
        List<AnatomyItem> terms = new ArrayList<AnatomyItem>();
        AnatomyItem term = new AnatomyItem();
        String firstTermName = "neural rod";
        term.setName(firstTermName);
        AnatomyItem termTwo = new AnatomyItem();
        termTwo.setName("retina");
        terms.add(term);
        terms.add(termTwo);

        // only first term matches
        List<AnatomyAutoCompleteTerm> list = AnatomyPresentation.getAnatomyTermList(terms, query);
        assertEquals(1, list.size());
        for (AnatomyAutoCompleteTerm autoTerm : list)
            assertEquals(true, autoTerm.isMatchOnTermName());

        AnatomySynonym syn = new AnatomySynonym();
        syn.setName("neuron");
        Set<AnatomySynonym> syns = new HashSet<AnatomySynonym>();
        syns.add(syn);
        termTwo.setSynonyms(syns);

        // match on first term and second terms synonym
        list = AnatomyPresentation.getAnatomyTermList(terms, query);
        assertEquals(2, list.size());
        for (AnatomyAutoCompleteTerm autoTerm : list) {
            if (autoTerm.getTermName().equals(firstTermName))
                assertEquals(true, autoTerm.isMatchOnTermName());
            else
                assertEquals(false, autoTerm.isMatchOnTermName());

        }

        syn = new AnatomySynonym();
        syn.setName("Neuron");
        syns.clear();
        syns.add(syn);
        termTwo.setSynonyms(syns);

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
    public void aoAutoCompleteTermListWithQueryWhiteSpaceIgnorance() {

        String query = "Neur  ";
        List<AnatomyItem> terms = new ArrayList<AnatomyItem>();
        AnatomyItem term = new AnatomyItem();
        String firstTermName = "neural rod";
        term.setName(firstTermName);
        AnatomyItem termTwo = new AnatomyItem();
        termTwo.setName("retina");
        terms.add(term);
        terms.add(termTwo);

        // only first term matches
        List<AnatomyAutoCompleteTerm> list = AnatomyPresentation.getAnatomyTermList(terms, query);
        assertEquals(1, list.size());
        for (AnatomyAutoCompleteTerm autoTerm : list)
            assertEquals(true, autoTerm.isMatchOnTermName());

        AnatomySynonym syn = new AnatomySynonym();
        syn.setName("neuron  ");
        Set<AnatomySynonym> syns = new HashSet<AnatomySynonym>();
        syns.add(syn);
        termTwo.setSynonyms(syns);

        // match on first term and second terms synonym
        list = AnatomyPresentation.getAnatomyTermList(terms, query);
        assertEquals(2, list.size());
        for (AnatomyAutoCompleteTerm autoTerm : list) {
            if (autoTerm.getTermName().equals(firstTermName))
                assertEquals(true, autoTerm.isMatchOnTermName());
            else
                assertEquals(false, autoTerm.isMatchOnTermName());

        }

        syn = new AnatomySynonym();
        syn.setName("Neuron");
        syns.clear();
        syns.add(syn);
        termTwo.setSynonyms(syns);

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
}

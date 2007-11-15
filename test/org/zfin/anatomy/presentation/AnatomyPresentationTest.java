package org.zfin.anatomy.presentation;

import junit.framework.JUnit4TestAdapter;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.AnatomyRelationship;
import org.zfin.anatomy.AnatomySynonym;

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
        AnatomySynonym syn1 = new AnatomySynonym();
        syn1.setName("first");
        AnatomySynonym syn2 = new AnatomySynonym();
        syn2.setName("second");
        AnatomySynonym syn3 = new AnatomySynonym();
        syn3.setName("third");
        AnatomySynonym syn4 = new AnatomySynonym();
        syn4.setName("fourth");

        Set<AnatomySynonym> synonyms = new HashSet<AnatomySynonym>();
        synonyms.add(syn1);
        synonyms.add(syn2);
        synonyms.add(syn3);
        synonyms.add(syn4);
        item.setSynonyms(synonyms);

        String list = AnatomyPresentation.createFormattedSynonymList(item);
        assertEquals("Four elements, three commas", "first, fourth, second, third", list);
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
    public void EmptySynonymList() {
        AnatomyItem item = new AnatomyItem();

        String list = AnatomyPresentation.createFormattedSynonymList(item);
        assertEquals("No elements, no commas", "", list);
    }

}

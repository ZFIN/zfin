package org.zfin.anatomy.presentation;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Test class for sorting relationship types:
 * See {@link RelationshipSorting} class.
 *
 */
public class RelationshipTypeSortingTest {

    @Test
    public void developsFromIntoRelationshipTypes() {
        String statFive = RelationshipSorting.HAS_SUBTYPE;
        String statSix = RelationshipSorting.IS_A_TYPE_OF;
        String statThree = RelationshipSorting.HAS_PARTS;
        String statFour = RelationshipSorting.IS_PART_OF;
        String statOne = RelationshipSorting.DEVELOPS_INTO;
        String statTwo = RelationshipSorting.DEVELOPS_FROM;

        List<String> list = new ArrayList<String>();
        list.add(statFive);
        list.add(statSix);
        list.add(statThree);
        list.add(statFour);
        list.add(statOne);
        list.add(statTwo);

        Collections.sort(list, new RelationshipSorting());

        String stOne = list.get(0);
        String stTwo = list.get(1);
        String stThree = list.get(2);
        String stFour = list.get(3);
        String stFive = list.get(4);
        String stSix = list.get(5);

        assertEquals("Develops From", RelationshipSorting.DEVELOPS_FROM, stOne);
        assertEquals("Develops into", RelationshipSorting.DEVELOPS_INTO, stTwo);
        assertEquals("Is part of", RelationshipSorting.IS_PART_OF, stThree);
        assertEquals("Has Parts", RelationshipSorting.HAS_PARTS, stFour);
        assertEquals("Is a type of", RelationshipSorting.IS_A_TYPE_OF, stFive);
        assertEquals("Has subtype", RelationshipSorting.HAS_SUBTYPE, stSix);
    }

}

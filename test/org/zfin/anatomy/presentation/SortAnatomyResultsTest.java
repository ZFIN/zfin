package org.zfin.anatomy.presentation;

import org.junit.Test;
import org.zfin.anatomy.AnatomyStatistics;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Term;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Test class for anatomy search results.
 */
public class SortAnatomyResultsTest {

    @Test
    public void oneObsoleteBothSearchTerm() {
        AnatomyStatistics statOne = new AnatomyStatistics();
        GenericTerm itemOne = new GenericTerm();
        itemOne.setTermName("Flycatcher");
        itemOne.setObsolete(false);
        statOne.setTerm(itemOne);

        AnatomyStatistics statTwo = new AnatomyStatistics();
        GenericTerm itemTwo = new GenericTerm();
        itemTwo.setTermName("Flyaborigines");
        itemTwo.setObsolete(true);
        statTwo.setTerm(itemTwo);

        List<AnatomyStatistics> list = new ArrayList<>();
        list.add(statOne);
        list.add(statTwo);

        Collections.sort(list, new SortAnatomyResults("Fly"));

        AnatomyStatistics stOne = list.get(0);
        AnatomyStatistics stTwo = list.get(1);

        String nameOne = stOne.getTerm().getTermName();
        String nameTwo = stTwo.getTerm().getTermName();

        assertEquals("", "Flycatcher", nameOne);
        assertEquals("", "Flyaborigines", nameTwo);
    }

    @Test
    public void oneObsoleteBothSearchTermAnatomy() {
        Term itemOne = new GenericTerm();
        itemOne.setTermName("Flycatcher");
        itemOne.setObsolete(false);

        Term itemTwo = new GenericTerm();
        itemTwo.setTermName("Flyaborigines");
        itemTwo.setObsolete(true);

        List<Term> list = new ArrayList<>();
        list.add(itemOne);
        list.add(itemTwo);

        Collections.sort(list, new SortAnatomySearchTerm("Fly"));

        Term stOne = list.get(0);
        Term stTwo = list.get(1);

        String nameOne = stOne.getTermName();
        String nameTwo = stTwo.getTermName();

        assertEquals("", "Flycatcher", nameOne);
        assertEquals("", "Flyaborigines", nameTwo);
    }

    @Test
    public void oneObsoleteOneSearchTerm() {
        AnatomyStatistics statOne = new AnatomyStatistics();
        GenericTerm itemOne = new GenericTerm();
        itemOne.setTermName("MyFlycatcher");
        itemOne.setObsolete(false);
        statOne.setTerm(itemOne);

        AnatomyStatistics statTwo = new AnatomyStatistics();
        GenericTerm itemTwo = new GenericTerm();
        itemTwo.setTermName("Flyaborigines");
        itemTwo.setObsolete(true);
        statTwo.setTerm(itemTwo);

        List<AnatomyStatistics> list = new ArrayList<>();
        list.add(statOne);
        list.add(statTwo);

        Collections.sort(list, new SortAnatomyResults("Fly"));

        AnatomyStatistics stOne = list.get(0);
        AnatomyStatistics stTwo = list.get(1);

        String nameOne = stOne.getTerm().getTermName();
        String nameTwo = stTwo.getTerm().getTermName();

        assertEquals("", "MyFlycatcher", nameOne);
        assertEquals("", "Flyaborigines", nameTwo);
    }

    @Test
    public void oneObsoleteOneSearchTermOther() {
        AnatomyStatistics statOne = new AnatomyStatistics();
        GenericTerm itemOne = new GenericTerm();
        itemOne.setTermName("Flycatcher");
        itemOne.setObsolete(false);
        statOne.setTerm(itemOne);

        AnatomyStatistics statTwo = new AnatomyStatistics();
        GenericTerm itemTwo = new GenericTerm();
        itemTwo.setTermName("MyFlyaborigines");
        itemTwo.setObsolete(true);
        statTwo.setTerm(itemTwo);

        List<AnatomyStatistics> list = new ArrayList<>();
        list.add(statOne);
        list.add(statTwo);

        Collections.sort(list, new SortAnatomyResults("Fly"));

        AnatomyStatistics stOne = list.get(0);
        AnatomyStatistics stTwo = list.get(1);

        String nameOne = stOne.getTerm().getTermName();
        String nameTwo = stTwo.getTerm().getTermName();

        assertEquals("", "Flycatcher", nameOne);
        assertEquals("", "MyFlyaborigines", nameTwo);
    }

    @Test
    public void twoObsolete() {
        AnatomyStatistics statOne = new AnatomyStatistics();
        GenericTerm itemOne = new GenericTerm();
        itemOne.setTermName("Flycatcher");
        itemOne.setObsolete(true);
        statOne.setTerm(itemOne);

        AnatomyStatistics statTwo = new AnatomyStatistics();
        GenericTerm itemTwo = new GenericTerm();
        itemTwo.setTermName("Flyaborigines");
        itemTwo.setObsolete(true);
        statTwo.setTerm(itemTwo);

        List<AnatomyStatistics> list = new ArrayList<>();
        list.add(statOne);
        list.add(statTwo);

        Collections.sort(list, new SortAnatomyResults("Fly"));

        AnatomyStatistics stOne = list.get(0);
        AnatomyStatistics stTwo = list.get(1);

        String nameOne = stOne.getTerm().getTermName();
        String nameTwo = stTwo.getTerm().getTermName();

        assertEquals("", "Flyaborigines", nameOne);
        assertEquals("", "Flycatcher", nameTwo);
    }
}

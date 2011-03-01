package org.zfin.anatomy.presentation;

import org.junit.Test;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.AnatomyStatistics;

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
        AnatomyItem itemOne = new AnatomyItem();
        itemOne.setTermName("Flycatcher");
        itemOne.setObsolete(false);
        statOne.setAnatomyItem(itemOne);

        AnatomyStatistics statTwo = new AnatomyStatistics();
        AnatomyItem itemTwo = new AnatomyItem();
        itemTwo.setTermName("Flyaborigines");
        itemTwo.setObsolete(true);
        statTwo.setAnatomyItem(itemTwo);

        List<AnatomyStatistics> list = new ArrayList<AnatomyStatistics>();
        list.add(statOne);
        list.add(statTwo);

        Collections.sort(list, new SortAnatomyResults("Fly"));

        AnatomyStatistics stOne = list.get(0);
        AnatomyStatistics stTwo = list.get(1);

        String nameOne = stOne.getAnatomyItem().getTermName();
        String nameTwo = stTwo.getAnatomyItem().getTermName();

        assertEquals("", "Flycatcher", nameOne);
        assertEquals("", "Flyaborigines", nameTwo);
    }

    @Test
    public void oneObsoleteBothSearchTermAnatomy() {
        AnatomyItem itemOne = new AnatomyItem();
        itemOne.setTermName("Flycatcher");
        itemOne.setObsolete(false);

        AnatomyItem itemTwo = new AnatomyItem();
        itemTwo.setTermName("Flyaborigines");
        itemTwo.setObsolete(true);

        List<AnatomyItem> list = new ArrayList<AnatomyItem>();
        list.add(itemOne);
        list.add(itemTwo);

        Collections.sort(list, new SortAnatomySearchTerm("Fly"));

        AnatomyItem stOne = list.get(0);
        AnatomyItem stTwo = list.get(1);

        String nameOne = stOne.getTermName();
        String nameTwo = stTwo.getTermName();

        assertEquals("", "Flycatcher", nameOne);
        assertEquals("", "Flyaborigines", nameTwo);
    }

    @Test
    public void oneObsoleteOneSearchTerm() {
        AnatomyStatistics statOne = new AnatomyStatistics();
        AnatomyItem itemOne = new AnatomyItem();
        itemOne.setTermName("MyFlycatcher");
        itemOne.setObsolete(false);
        statOne.setAnatomyItem(itemOne);

        AnatomyStatistics statTwo = new AnatomyStatistics();
        AnatomyItem itemTwo = new AnatomyItem();
        itemTwo.setTermName("Flyaborigines");
        itemTwo.setObsolete(true);
        statTwo.setAnatomyItem(itemTwo);

        List<AnatomyStatistics> list = new ArrayList<AnatomyStatistics>();
        list.add(statOne);
        list.add(statTwo);

        Collections.sort(list, new SortAnatomyResults("Fly"));

        AnatomyStatistics stOne = list.get(0);
        AnatomyStatistics stTwo = list.get(1);

        String nameOne = stOne.getAnatomyItem().getTermName();
        String nameTwo = stTwo.getAnatomyItem().getTermName();

        assertEquals("", "MyFlycatcher", nameOne);
        assertEquals("", "Flyaborigines", nameTwo);
    }

    @Test
    public void oneObsoleteOneSearchTermOther() {
        AnatomyStatistics statOne = new AnatomyStatistics();
        AnatomyItem itemOne = new AnatomyItem();
        itemOne.setTermName("Flycatcher");
        itemOne.setObsolete(false);
        statOne.setAnatomyItem(itemOne);

        AnatomyStatistics statTwo = new AnatomyStatistics();
        AnatomyItem itemTwo = new AnatomyItem();
        itemTwo.setTermName("MyFlyaborigines");
        itemTwo.setObsolete(true);
        statTwo.setAnatomyItem(itemTwo);

        List<AnatomyStatistics> list = new ArrayList<AnatomyStatistics>();
        list.add(statOne);
        list.add(statTwo);

        Collections.sort(list, new SortAnatomyResults("Fly"));

        AnatomyStatistics stOne = list.get(0);
        AnatomyStatistics stTwo = list.get(1);

        String nameOne = stOne.getAnatomyItem().getTermName();
        String nameTwo = stTwo.getAnatomyItem().getTermName();

        assertEquals("", "Flycatcher", nameOne);
        assertEquals("", "MyFlyaborigines", nameTwo);
    }

    @Test
    public void twoObsolete() {
        AnatomyStatistics statOne = new AnatomyStatistics();
        AnatomyItem itemOne = new AnatomyItem();
        itemOne.setTermName("Flycatcher");
        itemOne.setObsolete(true);
        statOne.setAnatomyItem(itemOne);

        AnatomyStatistics statTwo = new AnatomyStatistics();
        AnatomyItem itemTwo = new AnatomyItem();
        itemTwo.setTermName("Flyaborigines");
        itemTwo.setObsolete(true);
        statTwo.setAnatomyItem(itemTwo);

        List<AnatomyStatistics> list = new ArrayList<AnatomyStatistics>();
        list.add(statOne);
        list.add(statTwo);

        Collections.sort(list, new SortAnatomyResults("Fly"));

        AnatomyStatistics stOne = list.get(0);
        AnatomyStatistics stTwo = list.get(1);

        String nameOne = stOne.getAnatomyItem().getTermName();
        String nameTwo = stTwo.getAnatomyItem().getTermName();

        assertEquals("", "Flyaborigines", nameOne);
        assertEquals("", "Flycatcher", nameTwo);
    }
}

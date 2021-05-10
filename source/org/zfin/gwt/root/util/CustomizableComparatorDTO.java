package org.zfin.gwt.root.util;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Customizable comparator: Allows to provide a list of top strings that should be 
 * sorted to the top while other strings should be sorted to the bottom of a list.
 */
public class CustomizableComparatorDTO implements Comparator<String>, IsSerializable {

    private Collection<String> topValues = new ArrayList<String>(3);
    private Collection<String> bottomValues = new ArrayList<String>(3);
    private Collection<String> middleValues = new ArrayList<String>(3);
    private List<String> fullList;

    private static final int NOT_FOUND_INDEX = -1;

    public void addToTop(String value) {
        if (topValues == null)
            topValues = new ArrayList<String>();
        topValues.add(value);
    }

    public void addToBottom(String value) {
        if (bottomValues == null)
            bottomValues = new ArrayList<String>();
        bottomValues.add(value);
    }

    public void addPriority(String value) {
        if (bottomValues == null)
            bottomValues = new ArrayList<String>();
        bottomValues.add(value);
    }

    public int compare(String valueOne, String valueTwo) {
        if (valueOne == null)
            return -1;

        if (valueTwo == null)
            return 1;

        // both values are not null
        if (fullList == null) {
            fullList = new ArrayList<String>(topValues.size() + bottomValues.size() + middleValues.size());
            fullList.addAll(topValues);
            fullList.addAll(middleValues);
            fullList.addAll(bottomValues);
        }
        int indexOfValueOne = evaluateIndex(valueOne);
        int indexOfValueTwo = evaluateIndex(valueTwo);
        // if no match with given string
        if (indexOfValueOne == NOT_FOUND_INDEX && indexOfValueTwo == NOT_FOUND_INDEX)
            return valueOne.compareToIgnoreCase(valueTwo);

        if (indexOfValueOne == indexOfValueTwo)
            return 0;
        if (indexOfValueOne < indexOfValueTwo)
            return -1;
        else
            return +1;
    }

    private int evaluateIndex(String valueOne) {
        if (fullList != null) {
            int index = 0;
            for (String name : fullList) {
                if (name.toLowerCase().equals(valueOne.toLowerCase()))
                    return index;
                index++;
            }
        }
        return NOT_FOUND_INDEX;
    }

    @Override
    public String toString() {
        return "CustomizableComparatorDTO{" +
                "topValues=" + topValues +
                ", bottomValues=" + bottomValues +
                ", middleValues=" + middleValues +
                '}';
    }
}

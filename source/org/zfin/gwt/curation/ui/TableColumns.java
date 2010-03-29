package org.zfin.gwt.curation.ui;

import java.util.HashMap;
import java.util.Map;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class TableColumns {

    public static String NOTHING = "nothing";

    private int currentIndex = 1;
    private String name;

    Map<Integer, String> columns = new HashMap<Integer, String>(10);

    public void addColumn(String columnName){
        columns.put(currentIndex, name);
        currentIndex++;
    }

    
}

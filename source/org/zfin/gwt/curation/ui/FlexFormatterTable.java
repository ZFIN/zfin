package org.zfin.gwt.curation.ui;

import com.google.gwt.user.client.ui.FlexTable;

/**
*/
public class FlexFormatterTable extends FlexTable {
    private FlexCellFormatter formatter = new FlexCellFormatter();

    public FlexFormatterTable(){
        setCellFormatter(new FlexCellFormatter());
    }

    public FlexCellFormatter getFormatter() {
        return formatter;
    }
}

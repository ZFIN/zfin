package org.zfin.gwt.curation.ui;

import com.google.gwt.user.client.ui.ListBox;

/**
 * Adds a few convenience methods to the ListBox.
 */
public class ZfinListBox extends ListBox {


    public ZfinListBox() {
    }

    public ZfinListBox(boolean multiselect) {
        super(multiselect);
    }

    /**
     * Remove experiment from list.
     *
     * @param id id string
     */
    protected void removeEntryFromGuiList(String id) {
        int numOfRows = getItemCount();
        for (int row = 0; row < numOfRows; row++) {
            String listId = getValue(row);
            if (id.equals(listId))
                removeItem(row);
        }
    }

    public void selectEntryByDisplayName(String name) {
        if (name == null)
            return;
        
        int numOfRows = getItemCount();
        for (int row = 0; row < numOfRows; row++) {
            String itemDisplayText = getItemText(row);
            if (name.equals(itemDisplayText))
                setSelectedIndex(row);
        }
    }


}

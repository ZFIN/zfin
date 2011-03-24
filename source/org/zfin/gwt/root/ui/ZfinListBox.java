package org.zfin.gwt.root.ui;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.ListBox;

import java.util.ArrayList;
import java.util.List;

/**
 * Adds a few convenience methods to the ListBox.
 */
public class ZfinListBox extends ListBox {

    private List<ChangeHandler> changeHandlers = new ArrayList<ChangeHandler>(2);

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
            if (name.equals(itemDisplayText)) {
                setSelectedIndex(row);
                break;
            }
        }
    }

    public HandlerRegistration addChangeHandler(ChangeHandler changeHandler) {
        changeHandlers.add(changeHandler);
        return super.addChangeHandler(changeHandler);
    }

    public void fireChangeHandlers(){

        for(ChangeHandler changeHandler:changeHandlers)
            changeHandler.onChange(null);
    }

}

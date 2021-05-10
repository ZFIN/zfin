package org.zfin.gwt.root.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import org.zfin.gwt.root.dto.RelatedEntityDTO;
import org.zfin.gwt.root.event.RelatedEntityChangeListener;
import org.zfin.gwt.root.event.RelatedEntityEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic stack box.
 */
public abstract class AbstractStackComposite<T extends RelatedEntityDTO> extends AbstractComposite<T> implements Revertible {

    // GUI suppliers panel
    protected final HorizontalPanel addPanel = new HorizontalPanel();
    protected final Button addButton = new Button("Add");
    protected final FlexTable stackTable = new FlexTable();// contains the supplier names

    // listeners
    protected List<RelatedEntityChangeListener<T>> relatedEntityChangeListeners = new ArrayList<>();


    public abstract void resetInput();

    public abstract void sendUpdates();

    protected abstract void addToGUI(String name);

    protected abstract T createDTOFromGUI();

    @Override
    protected void addInternalListeners(HandlesError handlesError) {
        addButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                sendUpdates();
            }
        });
    }


    protected boolean containsName(String name) {
        return findRowForName(name) >= 0;
    }

    protected int findRowForName(String name) {
        int rowCount = stackTable.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            String widgetName = ((StackComposite) stackTable.getWidget(i, 0)).getDTO().getName();
            if (widgetName.equals(name)) {
                return i;
            }
        }
        return -1;
    }

    protected void removeFromGUI(String name) {
        int index = findRowForName(name);
        if (index >= 0) {
            stackTable.removeRow(index);
            fireDataChanged(new RelatedEntityEvent<>(createDTOFromGUI()));
            return;
        }
        setError("no inference to remove: " + name);
    }

    public void addRelatedEnityChangeListener(RelatedEntityChangeListener<T> relatedEntityChangeListener) {
        relatedEntityChangeListeners.add(relatedEntityChangeListener);
    }

    protected void fireDataChanged(RelatedEntityEvent<T> relatedEntityDTO) {
        for (RelatedEntityChangeListener<T> relatedEntityChangeListener : relatedEntityChangeListeners) {
            relatedEntityChangeListener.dataChanged(relatedEntityDTO);
        }
    }


    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public boolean handleDirty() {
        return false;
    }

    public FlexTable getStackTable() {
        return stackTable;
    }

    public String validateNewRelatedEntity(String name) {
        if (getRelatedEntityIndex(name) >= 0) {
            return "Related entity [" + name + "] exists.";
        }
        return null;
    }

    protected int getRelatedEntityIndex(String name) {
        List relatedEntityNames = getRelatedEntityNames();
        return (relatedEntityNames.indexOf(name));
    }

    protected List<String> getRelatedEntityNames() {
        int rowCount = stackTable.getRowCount();
        List<String> relatedEntityList = new ArrayList<>();
        for (int i = 0; i < rowCount; ++i) {
            if (stackTable.getWidget(i, 0) == null) {
                GWT.log("Problem at row, contact dev: " + i);
                return null;
            } else {
                relatedEntityList.add(((RelatedEntityLabel<T>) stackTable.getWidget(i, 0)).getName());
            }
        }
        return relatedEntityList;
    }
}
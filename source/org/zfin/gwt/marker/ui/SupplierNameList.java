package org.zfin.gwt.marker.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.marker.event.SupplierChangeListener;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.ui.StringListBox;

import java.util.ArrayList;
import java.util.List;

/**
 * A list of supplier names.
 */
public class SupplierNameList extends Composite implements CanRemoveSupplier {

    // GUI elements
    private final VerticalPanel panel = new VerticalPanel();

    // GUI suppliers panel
    private final HorizontalPanel supplierPanel = new HorizontalPanel();
    private final StringListBox supplierListBox = new StringListBox();
    private final Button supplierAddButton = new Button("Add Supplier");
    private final FlexTable supplierTable = new FlexTable();// contains the supplier names

    // listeners
    private final List<SupplierChangeListener> supplierChangeListeners = new ArrayList<SupplierChangeListener>();

    // internal data
    private MarkerDTO markerDTO;

    public SupplierNameList() {
        super();
        initGUI();
        initWidget(panel);
        initInternalListeners();
        RootPanel.get(StandardMarkerDivNames.supplierDiv).add(this);
    }

    public void setDomain(MarkerDTO markerDTO) {
        this.markerDTO = markerDTO;
        refreshGUI();
    }

    void refreshGUI() {
        supplierTable.clear();

        for (String supplier : markerDTO.getSuppliers()) {
            addSupplierToGUI(supplier);
        }
    }

    private void initInternalListeners() {
        addSupplierChangeListener(new SupplierChangeListener() {
            public void addSupplier(final String supplierName) {
                MarkerRPCService.App.getInstance().addMarkerSupplier(supplierName, getZdbID(),
                        new MarkerEditCallBack<Void>("failed to add supplier to marker: ") {
                            public void onSuccess(Void o) {
                                addSupplierToGUI(supplierName);
                            }
                        });
            }

            public void removeSupplier(final String supplierName) {
                MarkerRPCService.App.getInstance().removeMarkerSupplier(supplierName, getZdbID(),
                        new MarkerEditCallBack<Void>("failed to remove supplier to marker: ") {
                            public void onSuccess(Void o) {
                                removeSupplierFromGUI(supplierName);
                            }
                        });
            }
        });
    }

    private void initGUI() {
        supplierPanel.add(supplierListBox);
        supplierPanel.add(supplierAddButton);
        panel.add(supplierTable);
        panel.add(supplierPanel);
        panel.add(new HTML("<br>")); // spacer
        panel.setStyleName("gwt-editbox");


        MarkerRPCService.App.getInstance().getAllSupplierNames(new AsyncCallback<List<String>>() {
            public void onFailure(Throwable throwable) {
                Window.alert("Failed to load available marker suppliers: " + throwable);
            }

            public void onSuccess(List<String> list) {
                supplierListBox.addNullAndItems(list);
            }
        });

        supplierAddButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                fireSupplierAdded(supplierListBox.getSelected());
            }
        });


    }

    void fireSupplierAdded(String supplierName) {
        for (SupplierChangeListener supplierChangeListener : supplierChangeListeners) {
            supplierChangeListener.addSupplier(supplierName);
        }
    }

    public void fireSupplierRemoved(String supplierName) {
        for (SupplierChangeListener supplierChangeListener : supplierChangeListeners) {
            supplierChangeListener.removeSupplier(supplierName);
        }
    }

    void addSupplierToGUI(String supplierName) {
        supplierTable.setWidget(supplierTable.getRowCount(), 0, new SupplierComposite(this, supplierName));
        supplierListBox.setSelectedIndex(0);
    }

    void removeSupplierFromGUI(String supplierName) {
        int rowCount = supplierTable.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            String widgetName = ((SupplierComposite) supplierTable.getWidget(i, 0)).getName();
            if (widgetName.equals(supplierName)) {
                supplierTable.removeRow(i);
                return;
            }
        }
        Window.alert("supplier not found to remove: " + supplierName);
    }


    void addSupplierChangeListener(SupplierChangeListener supplierChangeListener) {
        supplierChangeListeners.add(supplierChangeListener);
    }


    String getZdbID() {
        return markerDTO.getZdbID();
    }

}

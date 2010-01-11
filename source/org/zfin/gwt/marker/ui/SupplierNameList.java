package org.zfin.gwt.marker.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.marker.event.SupplierChangeEvent;
import org.zfin.gwt.marker.event.SupplierChangeListener;
import org.zfin.gwt.root.dto.MarkerDTO;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class SupplierNameList extends Composite implements CanRemoveSupplier {

    // GUI elements
    private VerticalPanel panel = new VerticalPanel();

    // GUI suppliers panel
    private HorizontalPanel supplierPanel = new HorizontalPanel();
    private EasyListBox supplierListBox = new EasyListBox();
    private Button supplierAddButton = new Button("Add Supplier");
    private FlexTable supplierTable = new FlexTable();// contains the supplier names

    // listeners
    private List<SupplierChangeListener> supplierChangeListeners = new ArrayList<SupplierChangeListener>();

    // internal data
    private MarkerDTO markerDTO;

    public SupplierNameList(String div) {
        super();
        initGui();
        initWidget(panel);
        initInternalListeners();
        RootPanel.get(div).add(this);
    }

    public void setDomain(MarkerDTO markerDTO) {
        this.markerDTO = markerDTO;
        refreshGUI();
    }

    public void refreshGUI() {
        supplierTable.clear();

        for (String supplier : markerDTO.getSuppliers()) {
            addSupplierToGUI(supplier);
        }
    }

    private void initInternalListeners() {
        addSupplierChangeListener(new SupplierChangeListener() {
            public void addSupplier(final SupplierChangeEvent supplierChangeEvent) {
                MarkerRPCService.App.getInstance().addMarkerSupplier(supplierChangeEvent.getSupplierName(), getZdbID(),
                        new MarkerEditCallBack<Void>("failed to add supplier to marker: ") {
                            public void onSuccess(Void o) {
                                addSupplierToGUI(supplierChangeEvent.getSupplierName());
                            }
                        });
            }

            public void removeSupplier(final SupplierChangeEvent supplierChangeEvent) {
                MarkerRPCService.App.getInstance().removeMarkerSupplier(supplierChangeEvent.getSupplierName(), getZdbID(),
                        new MarkerEditCallBack<Void>("failed to remove supplier to marker: ") {
                            public void onSuccess(Void o) {
                                removeSupplierFromGUI(supplierChangeEvent.getSupplierName());
                            }
                        });
            }
        });
    }

    private void initGui() {
        supplierPanel.add(supplierListBox);
        supplierPanel.add(supplierAddButton);
        panel.add(supplierTable);
        panel.add(supplierPanel);
        panel.add(new HTML("<br>")); // spacer


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
                fireSupplierAdded(new SupplierChangeEvent(supplierListBox.getSelectedString()));
            }
        });


    }

    protected void fireSupplierAdded(SupplierChangeEvent supplierChangedEvent) {
        for (SupplierChangeListener supplierChangeListener : supplierChangeListeners) {
            supplierChangeListener.addSupplier(supplierChangedEvent);
        }
    }

    public void fireSupplierRemoved(SupplierChangeEvent supplierChangedEvent) {
        for (SupplierChangeListener supplierChangeListener : supplierChangeListeners) {
            supplierChangeListener.removeSupplier(supplierChangedEvent);
        }
    }

    public void addSupplierToGUI(String supplierName) {
        supplierTable.setWidget(supplierTable.getRowCount(), 0, new SupplierComposite(this, supplierName));
        supplierListBox.setSelectedIndex(0);
    }

    public void removeSupplierFromGUI(String supplierName) {
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


    public void addSupplierChangeListener(SupplierChangeListener supplierChangeListener) {
        supplierChangeListeners.add(supplierChangeListener);
    }


    public String getZdbID() {
        return markerDTO.getZdbID();
    }

}

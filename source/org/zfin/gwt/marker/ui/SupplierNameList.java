package org.zfin.gwt.marker.ui;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import org.zfin.gwt.marker.event.RelatedEntityAdapter;
import org.zfin.gwt.marker.event.RelatedEntityEvent;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.ui.StringListBox;

import java.util.List;

/**
 * A list of supplier names.
 */
public class SupplierNameList extends AbstractStackComposite<MarkerDTO>{


    // GUI suppliers panel
    private final StringListBox supplierListBox = new StringListBox();


    public SupplierNameList() {
        super();
        initGUI();
        addInternalListeners(this);
        initWidget(panel);
        RootPanel.get(StandardDivNames.supplierDiv).add(this);
    }

    @Override
    public void setDTO(MarkerDTO dto) {
        super.setDTO(dto);
        setValues();
    }

    @Override
    protected void initGUI() {
        addPanel.add(supplierListBox);
        addPanel.add(addButton);
        panel.add(stackTable);
        panel.add(addPanel);
        panel.add(errorLabel);
        errorLabel.setStyleName("error");
        panel.add(new HTML("<br>")); // spacer
        panel.setStyleName("gwt-editbox");

    }


    @Override
    public void sendUpdates() {
        final String valueToAdd = supplierListBox.getSelected();
        if(containsName(valueToAdd)){
            setError("Supplied already added: "+valueToAdd);
            notWorking();
            return;
        }
        working();
        MarkerRPCService.App.getInstance().addMarkerSupplier(valueToAdd, dto.getZdbID(),
                new MarkerEditCallBack<Void>("failed to add supplier to marker: ") {
                    public void onFailure(Throwable t) {
                        setError("Failed to remove supplier.");
                        notWorking();
                    }


                    public void onSuccess(Void o) {
                        addToGUI(valueToAdd);
                        notWorking();
                        fireEventSuccess();
                    }
                });
    }

    @Override
    public void working() {
        addButton.setEnabled(false);
        supplierListBox.setEnabled(false);
    }

    @Override
    public void notWorking() {
        addButton.setEnabled(true);
        supplierListBox.setEnabled(true);
    }

    @Override
    protected void setValues() {
        MarkerRPCService.App.getInstance().getAllSupplierNames(new AsyncCallback<List<String>>() {
            public void onFailure(Throwable throwable) {
                Window.alert("Failed to load available marker suppliers: " + throwable);
            }

            public void onSuccess(List<String> list) {
                supplierListBox.addNullAndItems(list);
            }
        });
    }

    @Override
    public void revertGUI() {
        stackTable.clear();
        for (String supplier : dto.getSuppliers()) {
            addToGUI(supplier);
        }
    }

    public MarkerDTO createDTOFromGUI() {
        // since all we handle is the inferreds, we will assume that we have the correct DTO.
        if(dto==null) return null ;
        MarkerDTO relatedEntityDTO = new MarkerDTO();
        if(supplierListBox.getItemCount()>0){
            relatedEntityDTO.setName(supplierListBox.getSelected());
        }
        relatedEntityDTO.setZdbID(dto.getZdbID());
        relatedEntityDTO.setDataZdbID(dto.getDataZdbID());
        return relatedEntityDTO;
    }

    protected void addToGUI(String name) {
        MarkerDTO relatedEntityDTO = createDTOFromGUI();
        relatedEntityDTO.setName(name);
        StackComposite<MarkerDTO> stackComposite = new StackComposite<MarkerDTO>(relatedEntityDTO) ;
        stackComposite.addRelatedEntityListener(new RelatedEntityAdapter<MarkerDTO>(){
            @Override
            public void removeRelatedEntity(final RelatedEntityEvent<MarkerDTO> event) {

                final String value = event.getDTO().getName();
                MarkerRPCService.App.getInstance().removeMarkerSupplier(value, event.getDTO().getZdbID(),
                        new MarkerEditCallBack<Void>("failed to remove supplier to marker: ") {
                            public void onSuccess(Void o) {
                                removeFromGUI(value);
                            }
                        });
            }
        });
        stackTable.setWidget(stackTable.getRowCount(), 0, stackComposite);
        resetInput();
    }



    @Override
    public void resetInput() {
        supplierListBox.setSelectedIndex(0);
    }


}

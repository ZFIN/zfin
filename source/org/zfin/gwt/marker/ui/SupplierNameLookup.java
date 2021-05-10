package org.zfin.gwt.marker.ui;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.event.RelatedEntityAdapter;
import org.zfin.gwt.root.event.RelatedEntityEvent;
import org.zfin.gwt.root.ui.*;
import org.zfin.gwt.root.util.LookupRPCService;

/**
 * A list of supplier names.
 */
public class SupplierNameLookup extends AbstractStackComposite<MarkerDTO> {


    // GUI suppliers panel
    private SupplierOracle oracle = new SupplierOracle();
    private TextBox supplierTextBox = new TextBox();
    private SuggestBox supplierSuggestBox;


    public SupplierNameLookup() {
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
        addPanel.add(addButton);
        addPanel.add(supplierTextBox);
        supplierSuggestBox = new SuggestBox(oracle, supplierTextBox);
        addPanel.add(supplierSuggestBox);
        panel.add(stackTable);
        panel.add(addPanel);
        panel.add(errorLabel);
        errorLabel.setStyleName("error");
        panel.add(new HTML("<br>")); // spacer
        panel.setStyleName("gwt-editbox");

    }


    @Override
    public void sendUpdates() {
        final String valueToAdd = supplierTextBox.getText().trim();
        if (containsName(valueToAdd)) {
            setError("Supplied already added: " + valueToAdd);
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
        supplierTextBox.setEnabled(false);
    }

    @Override
    public void notWorking() {
        addButton.setEnabled(true);
        supplierTextBox.setEnabled(true);
    }

    @Override
    protected void setValues() {
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
        if (dto == null) return null;
        MarkerDTO relatedEntityDTO = new MarkerDTO();
        String selectedSupplier = supplierTextBox.getText().trim();
        if (false == selectedSupplier.isEmpty()) {
            relatedEntityDTO.setName(selectedSupplier);
        }
        relatedEntityDTO.setZdbID(dto.getZdbID());
        relatedEntityDTO.setDataZdbID(dto.getDataZdbID());
        return relatedEntityDTO;
    }

    protected void addToGUI(String name) {
        MarkerDTO relatedEntityDTO = createDTOFromGUI();
        relatedEntityDTO.setName(name);
        StackComposite<MarkerDTO> stackComposite = new StackComposite<MarkerDTO>(relatedEntityDTO);
        stackComposite.addRelatedEntityListener(new RelatedEntityAdapter<MarkerDTO>() {
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
        supplierTextBox.setText("");
    }


    private class SupplierOracle extends LookupOracle {
        @Override
        public void doLookup(Request request, Callback callback) {
            LookupRPCService.App.getInstance().getSupplierSuggestions(request,
                    new LookupCallback(request, callback));
        }
    }
}
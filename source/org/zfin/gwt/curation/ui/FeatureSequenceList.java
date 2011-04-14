package org.zfin.gwt.curation.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTML;
import org.zfin.gwt.root.dto.FeatureDTO;
import org.zfin.gwt.root.event.RelatedEntityAdapter;
import org.zfin.gwt.root.event.RelatedEntityEvent;
import org.zfin.gwt.root.ui.*;

/**
 * A list of supplier names.
 */
public class FeatureSequenceList extends AbstractStackComposite<FeatureDTO> {


    // GUI suppliers panel
    private final StringTextBox sequenceTextBox = new StringTextBox();


    public FeatureSequenceList() {
        super();
        initGUI();
        addInternalListeners(this);
        initWidget(panel);

    }

    @Override
    public void setDTO(FeatureDTO dto) {
        super.setDTO(dto);
        setValues();
    }

    @Override
    protected void initGUI() {
        addPanel.add(sequenceTextBox);
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

        addSequence(sequenceTextBox.getText());
    }

    protected void addSequence(final String valueToAdd) {

        working();

        FeatureRPCService.App.getInstance().addFeatureSequence(valueToAdd, dto.getZdbID(),dto.getPublicationZdbID(),
                new FeatureEditCallBack<Void>("Failed to add sequence ["+valueToAdd+"] to feature: ",this) {
                    public void onFailure(Throwable t) {
                        super.onFailure(t);
                        notWorking();
                    }


                    public void onSuccess(Void o) {
                        addToGUI(valueToAdd);
                        notWorking();
                        fireEventSuccess();
                        clearError();
                    }
                });
    }

    @Override
    public void working() {
        addButton.setEnabled(false);
        sequenceTextBox.setEnabled(false);
    }

    @Override
    public void notWorking() {
        addButton.setEnabled(true);
        sequenceTextBox.setEnabled(true);
    }

    @Override
    protected void setValues() { }

    @Override
    public void revertGUI() {
        while(stackTable.getRowCount()>0){
            stackTable.removeRow(0);
        }
        if(dto.getFeatureSequences()!=null){
            for (String sequence : dto.getFeatureSequences()) {
                addToGUI(sequence);
            }
        }
    }

    public FeatureDTO createDTOFromGUI() {
        //
        if (dto == null) return null;
        FeatureDTO featureDTO = new FeatureDTO();
        //if (supplierListBox.getItemCount() > 0) {
        featureDTO.setFeatureSequence(sequenceTextBox.getText());

        featureDTO.setZdbID(dto.getZdbID());
        featureDTO.setDataZdbID(dto.getDataZdbID());
        return featureDTO;
    }

    protected void addToGUI(String name) {
        FeatureDTO featureDTO = createDTOFromGUI();
        featureDTO.setName(name);
        FeatureSequenceStackComposite stackComposite = new FeatureSequenceStackComposite(featureDTO,"Remove sequence from feature");
        stackComposite.addRelatedEntityListener(new RelatedEntityAdapter<FeatureDTO>() {
            @Override
            public void removeRelatedEntity(final RelatedEntityEvent<FeatureDTO> event) {

                final String value = event.getDTO().getName();
                FeatureRPCService.App.getInstance().removeFeatureSequence(value, event.getDTO().getZdbID(),
                        new FeatureEditCallBack<Void>("failed to remove supplier to marker: ") {
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
        sequenceTextBox.setText("");
    }

    /**
     * For use with the InferenceListBox.
     */
    public static class FeatureSequenceStackComposite extends StackComposite<FeatureDTO> {

        private String title ;

        public FeatureSequenceStackComposite(FeatureDTO dto,String title) {
            this.title = title ;
            initGUI();
            setDTO(dto);
            addInternalListeners(this);
            initWidget(panel);
        }

        @Override
        protected void addInternalListeners(HandlesError handlesError) {

            removeAttributionButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    working();
                    fireRelatedEntityRemoved(new RelatedEntityEvent<FeatureDTO>(dto));
                }
            });
        }

        public void initGUI() {
            removeAttributionButton.setStyleName("clickable");
            removeAttributionButton.setTitle(title);
            panel.add(nameLabel);
            panel.add(new HTML("&nbsp;&nbsp;"));
            panel.add(removeAttributionButton);
        }


    }
}
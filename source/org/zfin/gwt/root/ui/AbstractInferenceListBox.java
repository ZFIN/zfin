package org.zfin.gwt.root.ui;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.RootPanel;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.event.RelatedEntityAdapter;
import org.zfin.gwt.root.event.RelatedEntityChangeListener;
import org.zfin.gwt.root.event.RelatedEntityEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * A list of inferences.
 * In the first box, can be a set of inferences and the "entry" box is open text field
 * In the second box, will be the entry field and only visible if not ZFIN.
 * In the third box, will be a drop-down and only visible IF ZFIN.
 * <p/>
 * Everything available will be related to that pub.
 * However, if free-text can also be entered and it will be associated with whatever is selected in the drop-down.
 */
public abstract class AbstractInferenceListBox extends AbstractStackComposite<GoEvidenceDTO> implements IsDirty<GoEvidenceDTO> {


    // GUI suppliers panel
    protected final StringListBox inferenceCategoryList = new StringListBox();
    protected final StringTextBox lookupBox = new StringTextBox();
    protected final StringListBox availableList = new StringListBox();

    // listeners
    protected List<RelatedEntityChangeListener<GoEvidenceDTO>> goTermChangeListeners = new ArrayList<RelatedEntityChangeListener<GoEvidenceDTO>>();

    // internal GUI data
    protected String valueToSend;
    protected String prefixToSend;
    protected int startTabIndex ;
    protected int endTabIndex ;

    public AbstractInferenceListBox(String div) {
        super();
        initGUI();
        addInternalListeners(this);
        initWidget(panel);
        if (div != null) {
            RootPanel.get(div).add(this);
        }
    }

    public AbstractInferenceListBox() {
        this(StandardDivNames.directAttributionDiv);
    }


    protected void initGUI() {
        addPanel.add(inferenceCategoryList);
        addPanel.add(lookupBox);
        addPanel.add(availableList);
        addPanel.add(addButton);
        panel.add(stackTable);
        panel.add(addPanel);
        panel.add(errorLabel);
        errorLabel.setStyleName("error");
        panel.setStyleName("gwt-editbox");
    }


    protected void setAvailableValues() {
        if (inferenceCategoryList.getItemCount() == 0) {
            availableList.setVisible(false);
            lookupBox.setVisible(false);
            addButton.setVisible(false);
            return;
        } else {
            addButton.setVisible(true);
        }


        if (inferenceCategoryList.getSelected().equals(InferenceCategory.ZFIN_GENE.name())) {
            availableList.setVisible(true);
            lookupBox.setVisible(false);
            working();
            MarkerGoEvidenceRPCService.App.getInstance().getGenesForGOAttributions(dto,
                    new MarkerEditCallBack<List<MarkerDTO>>("Failed to get ZFIN Attributions") {

                        @Override
                        public void onSuccess(List<MarkerDTO> results) {
                            notWorking();
                            availableList.clear();
                            if (results.size() > 0) {
                                availableList.addItem("Genes:");
                                for (MarkerDTO markerDTO : results) {
                                    if (markerDTO.getZdbID() == null) {
                                        availableList.addItem(markerDTO.getName(), "null");
                                    } else {
                                        availableList.addItem(markerDTO.getName(), markerDTO.getZdbID());
                                    }
                                }
                            } else {
                                availableList.addItem("No ZFIN Attributions Available.");
                            }
                        }
                    });
        } else if (inferenceCategoryList.getSelected().equals(InferenceCategory.ZFIN_MRPH_GENO.name())) {
            availableList.setVisible(true);
            lookupBox.setVisible(false);
            working();
            if (dto.getPublicationZdbID() == null || dto.getPublicationZdbID().isEmpty() || dto.getPublicationZdbID().equals(IsDirty.NULL_STRING)) {
                notWorking();
//                setError("Please select a valid pub to display available options.");
                return;
            }
            MarkerGoEvidenceRPCService.App.getInstance().getGenotypesAndMorpholinosForGOAttributions(dto,
                    new MarkerEditCallBack<List<RelatedEntityDTO>>("Failed to get ZFIN Attributions") {
                        @Override
                        public void onSuccess(List<RelatedEntityDTO> results) {
                            notWorking();
                            availableList.clear();
                            if (results.size() > 0) {
                                for (RelatedEntityDTO relatedEntityDTO : results) {
                                    if (relatedEntityDTO.getZdbID() == null) {
                                        availableList.addItem(relatedEntityDTO.getName(), "null");
                                    } else {
                                        availableList.addItem(relatedEntityDTO.getName(), relatedEntityDTO.getZdbID());
                                    }
                                }
                            } else {
                                availableList.addItem("No ZFIN Attributions Available.");
                            }
                        }
                    });
        } else if (inferenceCategoryList.getSelected().equals(InferenceCategory.GO.name())) {
            availableList.setVisible(true);
            lookupBox.setVisible(false);
            working();
            MarkerGoEvidenceRPCService.App.getInstance().getGOTermsForPubAndMarker(dto,
                    new MarkerEditCallBack<List<GoEvidenceDTO>>("Failed to get GO Attributions") {
                        @Override
                        public void onSuccess(List<GoEvidenceDTO> results) {
                            notWorking();
                            availableList.clear();
                            if (results.size() > 0) {
                                for (GoEvidenceDTO relatedEntityDTO : results) {
                                    if (relatedEntityDTO.getDataZdbID() == null) {
                                        availableList.addItem(relatedEntityDTO.getDataZdbID(), "null");
                                    } else {
                                        availableList.addItem(relatedEntityDTO.getName(), 
                                                relatedEntityDTO.getGoTerm().getTermOboID().substring(InferenceCategory.GO.prefix().length()));
                                    }
                                }
                                // return the first 1
                                fireGoTermChanged(new RelatedEntityEvent(results.get(0)));
                            } else {
                                availableList.addItem("No GO Terms Available.");
                            }
                        }
                    });
        } else if (inferenceCategoryList.getSelected().equals(InferenceCategory.SP_KW.name())
                ||
                inferenceCategoryList.getSelected().equals(InferenceCategory.INTERPRO.name())
                ||
                inferenceCategoryList.getSelected().equals(InferenceCategory.EC.name())
                ) {
            availableList.setVisible(true);
            lookupBox.setVisible(true);
            working();
            final InferenceCategory inferenceCategory = InferenceCategory.getInferenceCategoryByName(inferenceCategoryList.getSelected());
            MarkerGoEvidenceRPCService.App.getInstance().getInferencesByMarkerAndType(dto, inferenceCategory.prefix(),
                    new MarkerEditCallBack<Set<String>>("Failed to get Inferred values for: " + inferenceCategoryList.getSelected()) {
                        @Override
                        public void onSuccess(Set<String> results) {
                            notWorking();
                            availableList.clear();
                            if (results.size() > 0) {
                                availableList.addItem("Available " + inferenceCategoryList.getSelected() + ":", NULL_STRING);
                                for (String inference : results) {
                                    availableList.addItem(inference.substring(inference.indexOf(":") + 1));
                                }
                            } else {
                                availableList.addItem("No inferences available for " + inferenceCategoryList.getSelected() + ".", NULL_STRING);
                            }
                        }
                    });
        } else {
            availableList.setVisible(false);
            lookupBox.setVisible(true);
        }
    }

    protected void setValues() {
        inferenceCategoryList.clear();
        if (dto.getEvidenceCode() != null) {
            for (InferenceCategory inferenceCategory : dto.getEvidenceCode().getInferenceCategories(dto.getPublicationZdbID())) {
                inferenceCategoryList.addItem(inferenceCategory.toString(), inferenceCategory.name());
            }
        }
        if (inferenceCategoryList.getItemCount() > 0) {
            inferenceCategoryList.setSelectedIndex(0);
        }
        setAvailableValues();
    }

    @Override
    public void setDTO(GoEvidenceDTO dto) {
        super.setDTO(dto);    //To change body of overridden methods use File | Settings | File Templates.
        setValues();
    }

    @Override
    protected void revertGUI() {
        stackTable.clear();
        while (stackTable.getRowCount() > 0) {
            stackTable.removeRow(0);
        }


        Set<String> inferredFrom = dto.getInferredFrom();
        if (inferredFrom != null) {
            for (final String inference : inferredFrom) {
                addToGUI(inference);
            }
        }
    }

    @Override
    public void addInternalListeners(HandlesError handlesError) {
        super.addInternalListeners(this);

        addHandlesErrorListener(this);

        // make RPC calls here, based on fired events
        // or just handle in the button-click method (or the method it calls)
        inferenceCategoryList.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                setAvailableValues();
                GoEvidenceDTO relatedEntityDTO = new GoEvidenceDTO();
                if (inferenceCategoryList.getSelected().equals(InferenceCategory.GO.name())
                        &&
                        availableList.getSelected() != null
                        ) {
                    TermDTO goTermDTO = new TermDTO();
                    goTermDTO.setDataZdbID(availableList.getSelected());
                    relatedEntityDTO.setGoTerm(goTermDTO);
                } else {
                    relatedEntityDTO.setGoTerm(dto.getGoTerm());
                }
                fireGoTermChanged(new RelatedEntityEvent(relatedEntityDTO));
            }
        });

        availableList.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                GoEvidenceDTO relatedEntityDTO = new GoEvidenceDTO();
                if (inferenceCategoryList.getSelected().equals(InferenceCategory.GO.name())) {
                    String goTermString = availableList.getSelectedText();
                    TermDTO goTermDTO = new TermDTO();
                    goTermDTO.setName(goTermString);
                    goTermDTO.setDataZdbID(availableList.getSelected());
                    relatedEntityDTO.setGoTerm(goTermDTO);
                    fireGoTermChanged(new RelatedEntityEvent(relatedEntityDTO));
                }
            }
        });


    }


    @Override
    public void sendUpdates() {
        working();

        // if they are both visible, then take the one with the values, checking the drop-down first
        if (availableList.isVisible() && lookupBox.isVisible()) {
            if (false == availableList.isSelectedNull()) {
                valueToSend = availableList.getSelected();
            } else if (availableList.isSelectedNull() && false == lookupBox.isEmpty()) {
                valueToSend = lookupBox.getBoxValue().trim();
            } else {
//            if(availableList.isSelectedNull() && lookupBox.isEmpty()){
                setError("Value required");
                notWorking();
                return;
            }
        } else if (availableList.isVisible() && false == lookupBox.isVisible()) {
            if (availableList.isSelectedNull()) {
                setError("Need to choose a valid ID");
                notWorking();
                return;
            }

            // handle ZFIN_GENE weirdness:
            valueToSend = availableList.getSelected();
        } else if (false == availableList.isVisible() && lookupBox.isVisible()) {
            if (lookupBox.isEmpty()) {
                setError("Value required");
                notWorking();
                return;
            }

            valueToSend = lookupBox.getBoxValue().trim();
        } else {
            setError("Nothing to add!");
            notWorking();
            return;
        }

        InferenceCategory inferenceCategory = InferenceCategory.getInferenceCategoryByName(inferenceCategoryList.getSelected());
        final String fullAccession = inferenceCategory.prefix() + valueToSend;
        if (containsName(fullAccession)) {
            setError("Already an inference: " + valueToSend);
            notWorking();
            return;
        }
        prefixToSend = inferenceCategory.prefix();

        MarkerGoEvidenceRPCService.App.getInstance().validateAccession(valueToSend, inferenceCategoryList.getSelected(),
                new MarkerEditCallBack<Boolean>("Failed to validate accession [" + valueToSend + "] " +
                        "for inference [" + inferenceCategoryList.getSelected() + "]") {
                    @Override
                    public void onSuccess(Boolean result) {
                        if (result) {
                            addToGUI(fullAccession);
                            handleDirty();
                        } else {
                            setError("Invalid accession[" + valueToSend + "] for category[" + inferenceCategoryList.getSelected() + "]");
                        }
                        notWorking();
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        super.onFailure(throwable);
                        handleDirty();
                        notWorking();
                    }
                });
    }

    protected void addToGUI(String name) {
        GoEvidenceDTO goEvidenceDTO = new GoEvidenceDTO();
        goEvidenceDTO.setName(name);
        goEvidenceDTO.setZdbID(dto.getZdbID());
        goEvidenceDTO.setDataZdbID(dto.getDataZdbID());
        StackComposite<GoEvidenceDTO> stackComposite = new StackComposite<GoEvidenceDTO>(goEvidenceDTO);

        stackComposite.addRelatedEntityListener(new RelatedEntityAdapter<GoEvidenceDTO>() {
            @Override
            public void removeRelatedEntity(final RelatedEntityEvent<GoEvidenceDTO> event) {
                removeFromGUI(event.getDTO().getName());
                notWorking();
            }
        });


        int rowCount = stackTable.getRowCount();
        stackTable.setWidget(rowCount, 0, stackComposite);
        resetInput();
        notWorking();
        fireEventSuccess();
    }

    @Override
    public void working() {
        inferenceCategoryList.setEnabled(false);
        availableList.setEnabled(false);
        addButton.setEnabled(false);
        lookupBox.setEnabled(false);
    }

    @Override
    public void notWorking() {
        inferenceCategoryList.setEnabled(true);
        availableList.setEnabled(true);
        addButton.setEnabled(true);
        lookupBox.setEnabled(true);
    }


    @Override
    public void resetInput() {
        lookupBox.setText("");
        if (availableList.getItemCount() > 0) {
            availableList.setSelectedIndex(0);
        }
    }

    public void addGoTermChangeListeners(RelatedEntityChangeListener<GoEvidenceDTO> changeListener) {
        goTermChangeListeners.add(changeListener);
    }

    protected void fireGoTermChanged(RelatedEntityEvent<GoEvidenceDTO> relatedEntityDTO) {
        for (RelatedEntityChangeListener<GoEvidenceDTO> relatedEntityChangeListener : this.goTermChangeListeners) {
            relatedEntityChangeListener.dataChanged(relatedEntityDTO);
        }
    }

    @Override
    public boolean isDirty(GoEvidenceDTO externalDTO) {
        if (externalDTO == null) return false;
        Set<String> inferences = externalDTO.getInferredFrom();
        for (String internalInference : dto.getInferredFrom()) {
            if (false == inferences.contains(internalInference)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public boolean isDirty() {
        return isDirty(createDTOFromGUI());
    }

    @Override
    protected GoEvidenceDTO createDTOFromGUI() {
        // since all we handle is the inferreds, we will assume that we have the correct DTO.
        if (dto == null) return null;
        GoEvidenceDTO dtoCopy = dto.deepCopy();
        Set<String> inferences = new TreeSet<String>();
        int rowCount = stackTable.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            inferences.add(((StackComposite) stackTable.getWidget(i, 0)).getDTO().getName());
        }
        dtoCopy.setInferredFrom(inferences);
        return dtoCopy;  //To change body of created methods use File | Settings | File Templates.
    }

    @Override
    public boolean handleDirty() {
        boolean dirty = isDirty();
        if (dirty) {
            fireDataChanged(new RelatedEntityEvent<GoEvidenceDTO>(createDTOFromGUI()));
        } else {
            fireEventSuccess();
        }
        return dirty;
    }

    public boolean setStackToDirty(boolean dirty) {
        int rowCount = stackTable.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            ((StackComposite) stackTable.getWidget(i, 0)).setDirty(dirty);
        }
        availableList.setStyleName((dirty ? DIRTY_STYLE : CLEAN_STYLE));
        lookupBox.setStyleName((dirty ? DIRTY_STYLE : CLEAN_STYLE));
        addButton.setStyleName((dirty ? DIRTY_STYLE : CLEAN_STYLE));
        inferenceCategoryList.setStyleName((dirty ? DIRTY_STYLE : CLEAN_STYLE));
        return dirty;
    }


    public int setTabIndex(int tabIndex){
        this.startTabIndex = tabIndex ;
        int thisIndex = this.startTabIndex ;
        inferenceCategoryList.setTabIndex(thisIndex++);
        availableList.setTabIndex(thisIndex);
        lookupBox.setTabIndex(thisIndex);
        ++thisIndex ;
        addButton.setTabIndex(thisIndex);
        return this.endTabIndex;
    }

    public int getStartTabIndex() {
        return startTabIndex;
    }

    public int getEndTabIndex() {
        return endTabIndex;
    }
}
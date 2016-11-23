package org.zfin.gwt.marker.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.zfin.gwt.marker.event.SequenceAddEvent;
import org.zfin.gwt.marker.event.SequenceAddListener;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.event.RelatedEntityEvent;
import org.zfin.gwt.root.event.RelatedEntityListener;
import org.zfin.gwt.root.ui.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NucleotideSequenceArea<U extends SequenceDTO> extends AbstractRelatedEntityContainer<U> {

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiTemplate("NucleotideSequenceArea.ui.xml")
    interface MyUiBinder extends UiBinder<VerticalPanel, NucleotideSequenceArea> {
    }

    @UiField
    ShowHideToggle showHideToggle;
    @UiField
    VerticalPanel fullSequencePanel;
    @UiField
    Button addSequenceButton;
    @UiField
    Button cancelButton;
    @UiField
    Label listBoxLabel;
    @UiField
    StringListBox databaseListBoxWrapper;
    @UiField(provided = true)
    SequenceBox newSequenceBox = new SequenceBox(SequenceBox.NUCLEOTIDE_SEQUENCE);

    // internal data
    private MarkerDTO markerDTO;

    // sequence panel
    final SequenceList sequenceList = new SequenceList();

    // listeners
    private final List<SequenceAddListener> sequenceAddListeners = new ArrayList<>();

    public NucleotideSequenceArea() {
        initWidget(uiBinder.createAndBindUi(this));
        addInternalListeners(this);
    }

    @UiHandler("showHideToggle")
    void onClickShowHide(@SuppressWarnings("unused") ClickEvent event) {
        showHideToggle.toggleVisibility();
        if(showHideToggle.isVisible())
            fireSequenceAddStartListeners(new SequenceAddEvent());
        else
            fireSequenceAddCancelListeners(new SequenceAddEvent());
    }

    @UiHandler("cancelButton")
    void onCancelReset(@SuppressWarnings("unused") ClickEvent event) {
        newSequenceBox.clearSequence();
        closeBox();
        fireSequenceAddCancelListeners(new SequenceAddEvent());
    }

    @UiHandler("addSequenceButton")
    void onAddSequence(@SuppressWarnings("unused") ClickEvent event) {
        if (false == attributionIsValid()) return;
        if (databaseListBoxWrapper.getSelected() == null
                ||
                AbstractListBox.NULL_STRING.equals(databaseListBoxWrapper.getSelected())) {
            setError("Please select a blast database.");
            return;
        }
        String validationError = newSequenceBox.checkSequence();
        if (validationError != null) {
            setError(validationError);
            return;
        }
        final SequenceDTO outgoingSequenceDTO = new SequenceDTO();
        outgoingSequenceDTO.setSequence(newSequenceBox.getSequenceAsString());
        outgoingSequenceDTO.setPublicationZdbID(getPublication());
        ReferenceDatabaseDTO referenceDatabaseDTO = new ReferenceDatabaseDTO();
        referenceDatabaseDTO.setZdbID(databaseListBoxWrapper.getSelected());
        fireSequenceAdded(new SequenceAddEvent(markerDTO, outgoingSequenceDTO, referenceDatabaseDTO));
    }


    public void inactivate() {
        addSequenceButton.setEnabled(false);
        cancelButton.setEnabled(false);
        databaseListBoxWrapper.setEnabled(false);
        newSequenceBox.inactivate();
    }

    public void activate() {
        addSequenceButton.setEnabled(true);
        cancelButton.setEnabled(true);
        databaseListBoxWrapper.setEnabled(true);
        newSequenceBox.activate();

        if (databaseListBoxWrapper.getItemCount() == 2) {
            databaseListBoxWrapper.setSelectedIndex(1);
        } else {
            databaseListBoxWrapper.setSelectedIndex(0);
        }
    }

    void addInternalListeners(final NucleotideSequenceArea nucleotideSequenceArea) {

        addRelatedEntityCompositeListener(new RelatedEntityListener<U>() {
            public void addRelatedEntity(RelatedEntityEvent<U> relatedEntityEvent) {
                // this is handled by the AddSequencelistener only
            }

            public void addAttribution(RelatedEntityEvent<U> relatedEntityEvent) {
                final SequenceDTO dto = relatedEntityEvent.getDTO();

                MarkerRPCService.App.getInstance().addSequenceAttribution(dto,
                        new MarkerEditCallBack<SequenceDTO>("failed to add attribution: ", nucleotideSequenceArea) {
                            public void onSuccess(SequenceDTO sequenceDTO) {
                                DeletableSequenceEntry deletableSequenceEntry = nucleotideSequenceArea.getDeletableSequenceEntryForSequenceDTO(sequenceDTO);
                                deletableSequenceEntry.addAttributionToGUI(sequenceDTO);
                            }

                            @Override
                            public void onFailure(Throwable throwable) {
                                GWT.log("failed: " + throwable);
                            }
                        });
            }

            public void removeRelatedEntity(RelatedEntityEvent<U> relatedEntityEvent) {
                // todo: take implementation for the RemoveSequenceListener
                final SequenceDTO sequenceDTO = relatedEntityEvent.getDTO();
                boolean confirm = Window.confirm("Remove sequence: " + sequenceDTO.getName() + "?");
                if (confirm == false) {
                    return;
                }
                MarkerRPCService.App.getInstance().removeDBLink(sequenceDTO,
                        new MarkerEditCallBack<DBLinkDTO>("failed to remove nucleotide sequence: ", nucleotideSequenceArea) {
                            public void onSuccess(DBLinkDTO dbLinkDTO) {
                                //nucleotideSequenceArea.removeRelatedEntityFromGUI(dbLinkDTO);
                            }
                        });
            }

            public void removeAttribution(RelatedEntityEvent<U> relatedEntityEvent) {
                final SequenceDTO sequenceDTO = relatedEntityEvent.getDTO();
                sequenceDTO.setDataZdbID(getZdbID());
                MarkerRPCService.App.getInstance().removeDBLinkAttribution(sequenceDTO,
                        new MarkerEditCallBack<DBLinkDTO>("failed to remove dblink reference: ") {
                            public void onSuccess(DBLinkDTO o) {
                                DeletableSequenceEntry deletableSequenceEntry = nucleotideSequenceArea.getDeletableSequenceEntryForSequenceDTO(sequenceDTO);
                                deletableSequenceEntry.removeAttributionFromGUI(sequenceDTO);
                            }
                        });
            }
        });
    }

    public void setMarkerDTO(MarkerDTO markerDTO) {
        this.markerDTO = markerDTO;
        sequenceList.clear();
        addSequences(markerDTO.getRnaSequences());
        newSequenceBox.clearSequence();
    }

    void openBox() {
        showHideToggle.setVisibilityToShow();
        fireSequenceAddStartListeners(new SequenceAddEvent());
    }

    public void closeBox() {
        showHideToggle.setVisibilityToHide();
    }

    void handleAddSequenceView() {
        showAddSequence();
    }

    void hideAddSequence() {
        showHideToggle.setVisibilityToHide();
    }

    void showAddSequence() {
        showHideToggle.setVisibilityToShow();
    }

    public void resetAndHide() {
        fireEventSuccess();
        newSequenceBox.clearSequence();
        closeBox();
    }

    protected String validateNewAttribution(RelatedEntityDTO dto) {
        String name = dto.getName();
        String pub = dto.getPublicationZdbID();

        if (pub == null || pub.length() == 0) {
            return "Publication must be selected to add new reference.";
        }
        List relatedEntityAttributions = getRelatedEntityAttributionsForName(name);
        if (getRelatedEntityIndex(name) < 0) {
            return "Entity name does not exist [" + name + "]";
        }
        if (relatedEntityAttributions.contains(pub)) {
            return "Publication [" + pub + "] exists for [" + name + "]";
        }

        return null;
    }


    public void removeAttribution(RelatedEntityDTO relatedEntityDTO) {
        SequenceDTO sequenceDTO = (SequenceDTO) relatedEntityDTO;
        fireAttributionRemoved(new RelatedEntityEvent(sequenceDTO));
    }


    protected List<String> getRelatedEntityNames() {
        List<String> relatedEntityList = new ArrayList<String>();
        for (DeletableSequenceEntry deletableSequenceEntry : sequenceList.getSequences()) {
            relatedEntityList.add(deletableSequenceEntry.getSequenceDTO().getName());
        }
        return relatedEntityList;
    }

    /**
     * @param relatedEntityName Related entity name.
     * @return A list of publications with this name
     */
    protected List<String> getRelatedEntityAttributionsForName(String relatedEntityName) {
        List<String> attributionList = new ArrayList<>();
        for (DeletableSequenceEntry deletableSequenceEntry : sequenceList.getSequences()) {
            if (deletableSequenceEntry.getSequenceDTO().getName().equals(relatedEntityName)) {
                for (PublicationAttributionLabel attributionLabel : deletableSequenceEntry.getAttributions()) {
                    attributionList.add(attributionLabel.getPublication());
                }
            }
        }
        return attributionList;
    }


    public void addRelatedEntityToGUI(U sequenceDTO) {
        // if the name already exists, just add the attributions
        if (getRelatedEntityIndex(sequenceDTO.getName()) >= 0) {
            getDeletableSequenceEntryForSequenceDTO(sequenceDTO).addAttributionToGUI(sequenceDTO);
            return;
        }

        sequenceDTO.setEditable(isEditable(sequenceDTO));
        final DeletableSequenceEntry internalSequenceEntry = new DeletableSequenceEntry(SequenceBox.NUCLEOTIDE_SEQUENCE, sequenceDTO, this);
        internalSequenceEntry.setSequence(sequenceDTO);

        sequenceList.addSequence(internalSequenceEntry);
    }


    void addSequences(List<SequenceDTO> sequences) {
        if (sequences == null || sequences.size() == 0) {
            return;
        }
        Collections.sort(sequences, new Comparator<SequenceDTO>() {
            public int compare(SequenceDTO o1, SequenceDTO o2) {
                if (o1.getAttributionType() == null && o2.getAttributionType() == null) {
                    return 0;
                } else if (o1.getAttributionType() == null && o2.getAttributionType() != null) {
                    return 1;
                } else if (o1.getAttributionType() != null && o2.getAttributionType() == null) {
                    return -1;
                }
                return o1.getAttributionType().compareTo(o2.getAttributionType());
            }
        });
        for (SequenceDTO sequenceDTO : sequences) {
            this.addRelatedEntityToGUI((U) sequenceDTO);
        }
        handleAddSequenceView();
    }


    public void removeRelatedEntityFromGUI(U relatedEntityDTO) {
        DeletableSequenceEntry deletableSequenceEntry = getDeletableSequenceEntryForSequenceDTO(relatedEntityDTO);
        if (deletableSequenceEntry != null) {
            sequenceList.remove(deletableSequenceEntry);
        } else {
            setError("Could not find sequence with defline: " + relatedEntityDTO.getDefLine());
        }
        handleAddSequenceView();
    }


    public int getNumberOfSequences() {
        return sequenceList.getSequences().size();
    }

    public int getNumberOfAttributions() {
        int numberOfAttributions = 0;
        for (DeletableSequenceEntry deletableSequenceEntry : sequenceList.getSequences()) {
            numberOfAttributions += deletableSequenceEntry.getAttributions().size();
        }
        return numberOfAttributions;
    }

    private DeletableSequenceEntry getDeletableSequenceEntryForSequenceDTO(SequenceDTO sequenceDTO) {
        for (DeletableSequenceEntry deletableSequenceEntry : sequenceList.getSequences()) {
            if (deletableSequenceEntry.getSequenceDTO().getDefLine().equals(sequenceDTO.getDefLine())) {
                return deletableSequenceEntry;
            }
        }
        return null;
    }

    void fireSequenceAdded(SequenceAddEvent sequenceAddedEvent) {
        fireEventSuccess();
        for (SequenceAddListener sequenceAddListener : sequenceAddListeners) {
            sequenceAddListener.add(sequenceAddedEvent);
        }
    }

    public void addSequenceAddListener(SequenceAddListener sequenceAddListener) {
        sequenceAddListeners.add(sequenceAddListener);
    }

    public AbstractListBox getDatabaseListBoxWrapper() {
        return databaseListBoxWrapper;
    }

    void fireSequenceAddStartListeners(SequenceAddEvent sequenceAddEvent) {
        for (SequenceAddListener sequenceAddListener : sequenceAddListeners) {
            sequenceAddListener.start(sequenceAddEvent);
        }
    }

    void fireSequenceAddCancelListeners(SequenceAddEvent sequenceAddEvent) {
        for (SequenceAddListener sequenceAddListener : sequenceAddListeners) {
            sequenceAddListener.cancel(sequenceAddEvent);
        }
    }

    public boolean isEditable(RelatedEntityDTO relatedEntityDTO) {
        return relatedEntityDTO.getName().startsWith("ZFINNUCL");
    }

    protected List<U> getRelatedEntityDTOs() {
        List<U> entities = new ArrayList<>();

        for (DeletableSequenceEntry deletableSequenceEntry : sequenceList.getSequences()) {
            entities.add((U) deletableSequenceEntry.getSequenceDTO());
        }

        return entities;
    }


    @Override
    public boolean isDirty() {
        return newSequenceBox.isDirty();
    }


    public class SequenceList extends VerticalPanel {
        public void addSequence(DeletableSequenceEntry deletableSequenceEntry) {
            add(deletableSequenceEntry);
        }

        public DeletableSequenceEntry getSequence(int index) {
            return (DeletableSequenceEntry) getWidget(index);
        }

        public List<DeletableSequenceEntry> getSequences() {
            List<DeletableSequenceEntry> sequences = new ArrayList<>();
            for (int i = 0; i < getNumSequences(); i++) {
                sequences.add(getSequence(i));
            }
            return sequences;
        }

        public int getNumSequences() {
            return getWidgetCount();
        }
    }


}

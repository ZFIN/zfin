package org.zfin.gwt.marker.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.marker.event.SequenceAddEvent;
import org.zfin.gwt.marker.event.SequenceAddListener;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.event.RelatedEntityEvent;
import org.zfin.gwt.root.event.RelatedEntityListener;
import org.zfin.gwt.root.ui.AbstractListBox;
import org.zfin.gwt.root.ui.MarkerEditCallBack;
import org.zfin.gwt.root.ui.MarkerRPCService;
import org.zfin.gwt.root.ui.StringListBox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NucleotideSequenceArea<U extends SequenceDTO> extends AbstractRelatedEntityContainer<U> {

    // internal data
    private MarkerDTO markerDTO;

    // gui elements
    private final VerticalPanel panel = new VerticalPanel();

    // gui data
    private String rightArrowHTMLString = "<a href=#sequence><img align=\"top\" src=\"/images/right.gif\" >Add Nucleotide Sequence</a>";
    private String downArrowHTMLString = "<a href=#sequence><img align=\"top\" src=\"/images/down.gif\" >Add Nucleotide Sequence</a>";
    private String historyToken = "sequence";

    // link
    private final HTML link = new HTML();
    private final VerticalPanel sequenceBoxPanel = new VerticalPanel();
    private final HorizontalPanel blastDatabasePanel = new HorizontalPanel();
    private final Label listBoxLabel = new Label("Blast Database:");
    private StringListBox databaseListBoxWrapper = new StringListBox();
    private final SequenceBox newSequenceBox = new SequenceBox(SequenceBox.NUCLEOTIDE_SEQUENCE);
    private final HorizontalPanel buttonPanel = new HorizontalPanel();
    private final Button addSequenceButton = new Button("Add Sequence");
    private final Button cancelButton = new Button("Cancel");

    // sequence panel
    final SequenceList sequenceList = new SequenceList();

    // listeners
    private final List<SequenceAddListener> sequenceAddListeners = new ArrayList<SequenceAddListener>();

    public NucleotideSequenceArea() {
        initGUI();
        addInternalListeners(this);
        initWidget(panel);
    }

    void initGUI() {

        link.setHTML(rightArrowHTMLString);
//        link.setTargetHistoryToken(historyToken);

        errorLabel.setStyleName("error");

        panel.add(errorLabel);
        panel.add(publicationLabel);

        publicationLabel.setStyleName("relatedEntityDefaultPub");
        panel.add(link);

        sequenceBoxPanel.setVisible(false);
        blastDatabasePanel.add(listBoxLabel);
        blastDatabasePanel.add(databaseListBoxWrapper);
        sequenceBoxPanel.add(blastDatabasePanel);
        sequenceBoxPanel.add(newSequenceBox);
        buttonPanel.add(addSequenceButton);
        buttonPanel.add(cancelButton);
        sequenceBoxPanel.add(buttonPanel);
        panel.add(sequenceBoxPanel);
        panel.add(sequenceList);

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

        link.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                if (sequenceBoxPanel.isVisible() == false) {
                    openBox();
                } else {
                    closeBox();
                    fireSequenceAddCancelListeners(new SequenceAddEvent());
                }
            }
        });

        cancelButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                newSequenceBox.clearSequence();
                closeBox();
            }
        });

        addSequenceButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
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
        });

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
                                Window.alert("failed: " + throwable);
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
//                TranscriptRPCService.App.getInstance().removeNucleotideSequenceFromTranscript(sequenceDTO,
//                        new MarkerEditCallBack<Void>("failed to remove nucleotide sequence: ", handlesError) {
//                            public void onSuccess(Void result) {
//                                removeRelatedEntity((RelatedEntityDTO) relatedEntityEvent.getRelatedEntityDTO());
//                            }
//                        });
                MarkerRPCService.App.getInstance().removeDBLink(sequenceDTO,
                        new MarkerEditCallBack<DBLinkDTO>("failed to remove nucleotide sequence: ", nucleotideSequenceArea) {
                            public void onSuccess(DBLinkDTO dbLinkDTO) {
                                nucleotideSequenceArea.removeRelatedEntityFromGUI(dbLinkDTO);
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
        sequenceBoxPanel.setVisible(false);
    }

    void openBox() {
        link.setHTML(downArrowHTMLString);
        sequenceBoxPanel.setVisible(true);
        fireSequenceAddStartListeners(new SequenceAddEvent());
    }

    public void closeBox() {
        link.setHTML(rightArrowHTMLString);
        sequenceBoxPanel.setVisible(false);
    }

    void handleAddSequenceView() {
        showAddSequence();
    }

    void hideAddSequence() {
        link.setVisible(false);
        sequenceBoxPanel.setVisible(false);
    }

    void showAddSequence() {
        link.setVisible(true);
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
        List<String> attributionList = new ArrayList<String>();
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

    public void setRightArrowHTMLString(String rightArrowHTMLString) {
        this.rightArrowHTMLString = rightArrowHTMLString;
    }

    public void setDownArrowHTMLString(String downArrowHTMLString) {
        this.downArrowHTMLString = downArrowHTMLString;
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
        List<U> entities = new ArrayList<U>();

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
            List<DeletableSequenceEntry> sequences = new ArrayList<DeletableSequenceEntry>();
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

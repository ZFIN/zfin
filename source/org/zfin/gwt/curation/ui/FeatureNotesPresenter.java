package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextArea;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.ui.FeatureEditCallBack;
import org.zfin.gwt.root.ui.IsDirtyWidget;
import org.zfin.gwt.root.ui.ZfinAsyncCallback;
import org.zfin.gwt.root.util.DeleteImage;

import java.util.ArrayList;

public class FeatureNotesPresenter {

    private FeatureNotesView view;
    private String publicationID;
    private FeatureDTO featureDTO;
    private PersonDTO curator;

    public FeatureNotesPresenter(String publicationID, FeatureNotesView view) {
        this.publicationID = publicationID;
        this.view = view;
    }

    public void go() {
        FeatureRPCService.App.getInstance().getCuratorInfo(new ZfinAsyncCallback<PersonDTO>("Failed to read Curator info", view.errorLabel) {
            @Override
            public void onSuccess(PersonDTO person) {
                curator = person;
            }
        });
    }

    private void populateDataTable() {

        view.dataTable.resizeRows(0);
        if (featureDTO == null) {
            view.endTableUpdate();
            return;
        }

        int elementIndex = 0;
        if (featureDTO.getPublicNoteList() != null) {
            for (NoteDTO noteDTO : featureDTO.getPublicNoteList()) {
                view.addNoteReferenceCell(noteDTO, elementIndex);
                addCommonNoteInfo(elementIndex, noteDTO);
                elementIndex++;
            }
        }
        if (featureDTO.getCuratorNotes() != null) {
            for (CuratorNoteDTO noteDTO : featureDTO.getCuratorNotes()) {
                view.addNoteCuratorReferenceCell(noteDTO, elementIndex);
                addCommonNoteInfo(elementIndex, noteDTO);
                elementIndex++;
            }
        }
        view.endTableUpdate();

    }

    private void addCommonNoteInfo(int elementIndex, NoteDTO noteDTO) {
        TextArea noteText = new TextArea();
        noteText.setText(noteDTO.getNoteData());
        view.addNoteTextAreaCell(noteText, elementIndex);
        if (noteDTO.getNoteEditMode().equals(NoteEditMode.PRIVATE) && !isMyCuratorNote(noteDTO)) {
            noteText.setEnabled(false);
        }
        DeleteImage deleteImage = new DeleteImage("Delete Note " + noteDTO.getZdbID());
        deleteImage.addClickHandler(new DeleteExternalFeatureNote(noteDTO));
        Button saveButton = new Button("Save");
        Button revertButton = new Button("Revert");
        // only show controlls for public notes and curator notes you do own.
        if (noteDTO.getNoteEditMode().equals(NoteEditMode.PUBLIC) || isMyCuratorNote(noteDTO))
            view.addControlCell(saveButton, revertButton, deleteImage, elementIndex);
        // wire-up the field's dependencies....
        new SingleFeatureNoteControlls(noteDTO, noteText, saveButton, revertButton);
    }

    private boolean isMyCuratorNote(NoteDTO noteDTO) {
        return noteDTO.getNoteEditMode().equals(NoteEditMode.PRIVATE)
                && curator.getZdbID().equals(((CuratorNoteDTO) noteDTO).getCurator().getZdbID());
    }

    public void addNote() {
        if (isClean()) {
            view.setError("Nothing to add.");
            return;
        }

        if (view.typeListBox.getSelected() == null) {
            view.setError("Must select type of note to add.");
            return;
        }

        NoteEditMode noteEditMode = NoteEditMode.valueOf(view.typeListBox.getSelected().toUpperCase());

        if (noteEditMode == NoteEditMode.PUBLIC) {
            final NoteDTO noteDTO = new PublicNoteDTO();
            noteDTO.setDataZdbID(featureDTO.getZdbID());
            noteDTO.setNoteData(view.newNoteTextArea.getText());
            noteDTO.setPublicationZdbID(publicationID);
            noteDTO.setNoteEditMode(noteEditMode);
            FeatureRPCService.App.getInstance().editPublicNote(noteDTO, new FeatureEditCallBack<FeatureDTO>("Failed to update public note") {
                @Override
                public void onSuccess(FeatureDTO featureDTOReturn) {
                    featureDTO = featureDTOReturn;
                    populateDataTable();
                    Scheduler.get().scheduleDeferred(new Command() {
                        @Override
                        public void execute() {
                            view.clearGUI();
                        }
                    });
                }
            });
        } else if (noteEditMode == NoteEditMode.PRIVATE) {
            final CuratorNoteDTO noteDTO = new CuratorNoteDTO();
            noteDTO.setDataZdbID(featureDTO.getZdbID());
            noteDTO.setNoteData(view.newNoteTextArea.getText());
            noteDTO.setPublicationZdbID(publicationID);
            noteDTO.setNoteEditMode(noteEditMode);
            FeatureRPCService.App.getInstance().addCuratorNote(noteDTO, new FeatureEditCallBack<CuratorNoteDTO>("Failed to update curator note") {
                @Override
                public void onSuccess(CuratorNoteDTO returnNoteDTO) {
                    if (featureDTO.getCuratorNotes() == null) {
                        featureDTO.setCuratorNotes(new ArrayList<CuratorNoteDTO>());
                    }
                    featureDTO.getCuratorNotes().add(returnNoteDTO);
                    populateDataTable();
                    Scheduler.get().scheduleDeferred(new Command() {
                        @Override
                        public void execute() {
                            view.clearGUI();
                        }
                    });
                }
            });
        }
    }

    public boolean isClean() {
        return view.newNoteTextArea.getText().equals("");
    }

    public FeatureDTO getFeatureDTO() {
        return featureDTO;
    }

    public void setFeatureDTO(FeatureDTO featureDTO) {
        this.featureDTO = featureDTO;
    }

    public void rebuildGUI() {
        populateDataTable();
    }

    ////// Handlers and Listeners....

    private class DeleteExternalFeatureNote implements ClickHandler {

        private NoteDTO noteDTO;


        public DeleteExternalFeatureNote(NoteDTO noteDTO) {
            this.noteDTO = noteDTO;
        }

        @Override
        public void onClick(ClickEvent clickEvent) {
            String message = "Are you sure you want to delete this note?";
            if (!Window.confirm(message))
                return;

            if (noteDTO.getNoteEditMode().equals(NoteEditMode.PUBLIC)) {
                FeatureRPCService.App.getInstance().removePublicNote(noteDTO, new FeatureEditCallBack<Void>("Failed to remove public note: ") {
                    @Override
                    public void onSuccess(Void result) {
                        noteDTO.setNoteData("");
                        featureDTO.getPublicNoteList().remove(noteDTO);
                        populateDataTable();
                    }
                });
            }
            if (noteDTO.getNoteEditMode().equals(NoteEditMode.PRIVATE)) {
                FeatureRPCService.App.getInstance().removeCuratorNote(noteDTO, new FeatureEditCallBack<Void>("Failed to remove curator note: ") {
                    @Override
                    public void onSuccess(Void result) {
                        noteDTO.setNoteData("");
                        featureDTO.getCuratorNotes().remove((CuratorNoteDTO) noteDTO);
                        populateDataTable();
                    }
                });
            }
        }
    }

    private class SingleFeatureNoteControlls {
        private TextArea noteTextArea;
        private Button saveButton;
        private Button revertButton;
        private NoteDTO noteDTO;


        public SingleFeatureNoteControlls(NoteDTO noteDTO, TextArea noteTextArea, Button saveButton, Button revertButton) {
            this.noteDTO = noteDTO;
            this.noteTextArea = noteTextArea;
            revertButton.setEnabled(false);
            saveButton.setEnabled(false);
            this.revertButton = revertButton;
            this.saveButton = saveButton;
            addListeners();
        }

        private void addListeners() {
            noteTextArea.addChangeHandler(new ChangeHandler() {
                @Override
                public void onChange(ChangeEvent event) {
                    handleDirty();
                }
            });

            noteTextArea.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    handleDirty();
                }
            });

            noteTextArea.addKeyPressHandler(new KeyPressHandler() {
                @Override
                public void onKeyPress(KeyPressEvent event) {
                    boolean dirty = isDirty();
                    saveButton.setEnabled(dirty);
                    revertButton.setEnabled(dirty);
                    noteTextArea.setFocus(true);
                    // changing the style messes stuff up
                }
            });

            revertButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    revertGUI();
                }
            });

            saveButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent clickEvent) {
                    noteDTO.setNoteData(noteTextArea.getText());
                    if (noteDTO.getNoteEditMode().equals(NoteEditMode.PUBLIC)) {
                        FeatureRPCService.App.getInstance().editPublicNote(noteDTO, new FeatureEditCallBack<FeatureDTO>("Failed to update public note") {
                            @Override
                            public void onSuccess(FeatureDTO dto) {
                                revertGUI();
                            }
                        });
                    } else if (noteDTO.getNoteEditMode().equals(NoteEditMode.PRIVATE)) {
                        FeatureRPCService.App.getInstance().editCuratorNote(noteDTO, new FeatureEditCallBack<Void>("Failed to update note: ") {
                            @Override
                            public void onSuccess(Void result) {
                                revertGUI();
                            }
                        });
                    }
                }
            });
        }

        public String getNoteText() {
            return noteTextArea.getText();
        }

        protected void revertGUI() {
            noteTextArea.setText(noteDTO.getNoteData());
            handleDirty();
        }

        public boolean handleDirty() {
            boolean dirty = isDirty();
            saveButton.setEnabled(dirty);
            revertButton.setEnabled(dirty);
            if (dirty) {
                noteTextArea.setStyleName(IsDirtyWidget.DIRTY_STYLE);
            } else {
                noteTextArea.setStyleName(IsDirtyWidget.CLEAN_STYLE);
            }
            return dirty;
        }

        public boolean isDirty() {
            return (!noteTextArea.getText().equals(noteDTO.getNoteData()));
        }

    }
}

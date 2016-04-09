package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextArea;
import org.zfin.gwt.root.dto.FeatureDTO;
import org.zfin.gwt.root.dto.NoteDTO;
import org.zfin.gwt.root.dto.NoteEditMode;
import org.zfin.gwt.root.dto.PublicNoteDTO;
import org.zfin.gwt.root.ui.FeatureEditCallBack;
import org.zfin.gwt.root.ui.IsDirty;
import org.zfin.gwt.root.util.DeleteImage;

import java.util.ArrayList;

public class FeatureNotesPresenter {

    private FeatureNotesView view;
    private String publicationID;
    private FeatureDTO featureDTO;

    public FeatureNotesPresenter(String publicationID, FeatureNotesView view) {
        this.publicationID = publicationID;
        this.view = view;
    }

    public void go() {

    }

    private void populateDataTable() {
        int elementIndex = 0;

        if (featureDTO == null || featureDTO.getPublicNoteList() == null) {
            return;
        }
        for (NoteDTO noteDTO : featureDTO.getPublicNoteList()) {
            view.addNoteTypeCell(noteDTO, elementIndex);
            TextArea noteText = new TextArea();
            noteText.setText(noteDTO.getNoteData());
            view.addNoteTextAreaCell(noteText, elementIndex);
            DeleteImage deleteImage = new DeleteImage("Delete Note " + noteDTO.getZdbID());
            deleteImage.addClickHandler(new DeleteExternalFeatureNote(noteDTO));
            view.addDeleteNoteImageCell(deleteImage, elementIndex);
            Button saveButton = new Button("Save");
            view.addSaveButtonCell(saveButton, elementIndex);
            Button revertButton = new Button("Revert");
            view.addRevertButtonCell(revertButton, elementIndex);
            // wire-up the field's dependencies....
            new SingleFeatureNoteControlls(noteDTO, noteText, saveButton, revertButton);
            elementIndex++;
        }
        view.endTableUpdate();

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
        final NoteDTO noteDTO = new PublicNoteDTO();
        noteDTO.setDataZdbID(featureDTO.getZdbID());
        noteDTO.setNoteData(view.newNoteTextArea.getText());
        noteDTO.setPublicationZdbID(publicationID);
        noteDTO.setNoteEditMode(noteEditMode);

        if (noteEditMode == NoteEditMode.PUBLIC) {
            FeatureRPCService.App.getInstance().editPublicNote(noteDTO, new FeatureEditCallBack<FeatureDTO>("Failed to update public note") {
                @Override
                public void onSuccess(FeatureDTO featureDTOReturn) {
                    featureDTO = featureDTOReturn;
                    populateDataTable();
                    Scheduler.get().scheduleDeferred(new Command() {
                        @Override
                        public void execute() {
                            view.resetGUI();
                        }
                    });
                }
            });
        } else if (noteEditMode == NoteEditMode.PRIVATE) {
            FeatureRPCService.App.getInstance().addCuratorNote(noteDTO, new FeatureEditCallBack<NoteDTO>("Failed to update curator note") {
                @Override
                public void onSuccess(NoteDTO returnNoteDTO) {
                    if (featureDTO.getCuratorNotes() == null) {
                        featureDTO.setCuratorNotes(new ArrayList<NoteDTO>());
                    }
                    featureDTO.getCuratorNotes().add(returnNoteDTO);
/*
                    addNoteToGUI(returnNoteDTO);
                    resetAddNote();
*/
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
            FeatureRPCService.App.getInstance().removePublicNote(noteDTO, new FeatureEditCallBack<Void>("Failed to remove public note: ") {
                @Override
                public void onSuccess(Void result) {
                    noteDTO.setNoteData("");
                    featureDTO.getPublicNoteList().remove(noteDTO);
                    populateDataTable();
                }
            });
        }
    }

    private class SaveExternalFeatureNote implements ClickHandler {

        private NoteDTO noteDTO;


        public SaveExternalFeatureNote(NoteDTO noteDTO) {
            this.noteDTO = noteDTO;
        }

        @Override
        public void onClick(ClickEvent clickEvent) {
            FeatureRPCService.App.getInstance().editPublicNote(noteDTO, new FeatureEditCallBack<FeatureDTO>("Failed to update public note") {
                @Override
                public void onSuccess(FeatureDTO dto) {
                    featureDTO = dto;
                    populateDataTable();
/*
                    Scheduler.get().scheduleDeferred(new Command() {
                        @Override
                        public void execute() {
                            view.resetGUI();
                        }
                    });
*/
                }
            });
        }
    }

    private class SingleFeatureNoteControlls{
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
                    FeatureRPCService.App.getInstance().editPublicNote(noteDTO, new FeatureEditCallBack<FeatureDTO>("Failed to update public note") {
                        @Override
                        public void onSuccess(FeatureDTO dto) {
                            revertGUI();
                        }
                    });

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
                noteTextArea.setStyleName(IsDirty.DIRTY_STYLE);
            } else {
                noteTextArea.setStyleName(IsDirty.CLEAN_STYLE);
            }
            return dirty;
        }

        public boolean isDirty() {
            return (!noteTextArea.getText().equals(noteDTO.getNoteData()));
        }

    }
}

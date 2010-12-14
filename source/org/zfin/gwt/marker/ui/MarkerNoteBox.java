package org.zfin.gwt.marker.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import org.zfin.gwt.root.dto.AntibodyDTO;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.dto.NoteDTO;
import org.zfin.gwt.root.dto.NoteEditMode;
import org.zfin.gwt.root.event.RemovableNoteListener;
import org.zfin.gwt.root.ui.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Integrates curated/private, public, and external notes.
 * We need to configure this so that only a set number of things to add/edit.
 */
public class MarkerNoteBox<T extends MarkerDTO> extends AbstractNoteBox<T> implements RemovableNoteListener {


    public MarkerNoteBox() {
        for (NoteEditMode noteEditMode : NoteEditMode.values()) {
            addEditMode(noteEditMode);
        }

        initGUI();
        setValues();
        initWidget(panel);
        addInternalListeners(this);
        RootPanel.get(StandardDivNames.noteDiv).add(this);
    }

    public void setValues() {
        typeListBox.clear();
        List<String> items = new ArrayList<String>();
        for (NoteEditMode noteEditMode : noteEditModes) {
            if (noteEditMode == NoteEditMode.PUBLIC && false == containsPublicNote()) {
                items.add(noteEditMode.name());
            } else if (noteEditMode != NoteEditMode.PUBLIC) {
                items.add(noteEditMode.name());
            }
        }
        typeListBox.addNullAndItems(items);
        if (typeListBox.getItemCount() == 2) {
            typeListBox.setSelectedIndex(1);
        }
        if (defaultNoteEditMode != null) {
            typeListBox.setIndexForText(defaultNoteEditMode.name());
        }
    }


    protected void addInternalListeners(final HandlesError handlesError) {

        super.addInternalListeners(handlesError);

        addButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (false == isDirty()) {
                    setError("Nothing to add.");
                    return;
                }

                if (typeListBox.getSelected() == null) {
                    setError("Must select type of note to add.");
                    return;
                }

//                working();
                // rpc call to add type of note
                NoteEditMode noteEditMode = NoteEditMode.valueOf(typeListBox.getSelected());
                final NoteDTO noteDTO = new NoteDTO();
                noteDTO.setDataZdbID(dto.getZdbID());
                noteDTO.setNoteData(newNoteTextArea.getText());
                noteDTO.setPublicationZdbID(publicationZdbID);
                noteDTO.setNoteEditMode(noteEditMode);

                if (noteEditMode == NoteEditMode.PUBLIC) {
                    MarkerRPCService.App.getInstance().editPublicNote(noteDTO, new MarkerEditCallBack<Void>("Failed to update public note") {
                        @Override
                        public void onSuccess(Void v) {
                            dto.setPublicNote(noteDTO);
                            addNoteToGUI(noteDTO);
                            DeferredCommand.addCommand(new Command() {
                                @Override
                                public void execute() {
                                    resetAddNote();
                                }
                            });
                        }
                    });
                } else if (noteEditMode == NoteEditMode.PRIVATE) {
                    MarkerRPCService.App.getInstance().addCuratorNote(noteDTO, new MarkerEditCallBack<NoteDTO>("Failed to update public note") {
                        @Override
                        public void onSuccess(NoteDTO returnNoteDTO) {
                            dto.getCuratorNotes().add(returnNoteDTO);
                            addNoteToGUI(returnNoteDTO);
                            resetAddNote();
                        }
                    });
                } else if (noteEditMode == NoteEditMode.EXTERNAL) {
                    if (noteDTO.getPublicationZdbID() == null || noteDTO.getPublicationZdbID().length() < 16) {
                        setError("External notes require publication");
                        return;
                    }
                    MarkerRPCService.App.getInstance().addExternalNote(noteDTO, new MarkerEditCallBack<NoteDTO>("Failed to update public note") {
                        @Override
                        public void onSuccess(NoteDTO returnNoteDTO) {
                            ((AntibodyDTO) dto).getExternalNotes().add(returnNoteDTO);
                            addNoteToGUI(returnNoteDTO);
                            resetAddNote();
                        }
                    });
                }

            }
        });
    }


    public void addNoteToGUI(NoteDTO noteDTO) {
        int rowCount = table.getRowCount();
        NoteEditMode noteEditMode = noteDTO.getNoteEditMode();
        if (noteEditMode == NoteEditMode.EXTERNAL) {
            PublicationLabel publicationLabel = new PublicationLabel(noteDTO.getPublicationZdbID());
            table.setWidget(rowCount, 0, publicationLabel);
        } else {
            table.setHTML(rowCount, 0, noteEditMode.name());
        }
        AbstractNoteEntry noteEntry = IntegratedNoteEntryFactory.createIntegratedNoteEntry(noteDTO, this);
        noteEntry.addNoteListener(this);
        table.setWidget(rowCount, 1, noteEntry);

        if (noteEditMode == NoteEditMode.PUBLIC) {
            DeferredCommand.addCommand(new Command() {
                @Override
                public void execute() {
                    setValues();
                }
            });
        }
    }

    @Override
    public boolean isDirty() {
        if (false == newNoteTextArea.getText().equals("")) return true;
//        if (typeListBox.getSelected()!=null) return true ;

        return false;
    }

    @Override
    protected void revertGUI() {
        table.clear();
        if (false == dto.getPublicNote().isEmpty()) {
            addNoteToGUI(dto.getPublicNote());
        }
        for (NoteDTO noteDTO : dto.getCuratorNotes()) {
            addNoteToGUI(noteDTO);
        }
        if (dto instanceof AntibodyDTO) {
            for (NoteDTO noteDTO : ((AntibodyDTO) dto).getExternalNotes()) {
                addNoteToGUI(noteDTO);
            }
        }

        resetAddNote();
    }

    private class PublicationLabel extends HTML {
        public PublicationLabel(final String publicationZdbID) {
            setHTML(publicationZdbID);
            setStyleName("externalLink");
            addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    Window.open("http://zfin.org/cgi-bin/webdriver/?MIval=aa-pubview2.apg&OID=" + publicationZdbID, "", "");
                }
            });
        }
    }

    public boolean containsPublicNote() {
        return dto != null &&
                dto.getPublicNote() != null &&
                dto.getPublicNote().getNoteData() != null &&
                dto.getPublicNote().getNoteData().length() > 0
                ;
    }


}

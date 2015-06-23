package org.zfin.gwt.curation.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import org.zfin.gwt.root.dto.GenotypeDTO;
import org.zfin.gwt.root.ui.ErrorHandler;
import org.zfin.gwt.root.ui.ZfinAsyncCallback;

import java.util.List;

/**
 * Table of associated genotypes
 */
public class GenotypePresenter implements Presenter {

    private CurationDiseaseRPCAsync diseaseRpcService = CurationDiseaseRPC.App.getInstance();
    private final HandlerManager eventBus;
    private GenotypeView view;
    private String publicationID;

    public GenotypePresenter(HandlerManager eventBus, GenotypeView view, String publicationID) {
        this.eventBus = eventBus;
        this.view = view;
        this.publicationID = publicationID;
        view.setPublicationID(publicationID);
    }

    public void bind() {
        // show / hide genotype list
        view.getShowHideGenoList().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                view.getGenotypeListToggle().toggleVisibility();
            }
        });
        addDeleteClickHandlerToPublicNotes();
    }

    private void addDeleteClickHandlerToPublicNotes() {
        List<GenotypeView.PublicNoteWidgets> list = view.getPublicNoteWidgetsList();
        if (list.size() == 0)
            return;
        for (final GenotypeView.PublicNoteWidgets widget : list) {
            // add Delete-Note click handler
            if (widget.hasDeleteLink()) {
                widget.getDeleteImage().addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent clickEvent) {
                        diseaseRpcService.deletePublicNote(publicationID, widget.getNote(), new RetrieveGenotypeListCallBack("delete note", view.getErrorElement()));
                    }
                });
            }
        }
        for (final GenotypeView.PublicNoteWidgets widget : list) {
            // add Save-Note click handler
            final NotePopup notePopup = widget.getNotePopup();
            if (notePopup.isPublicNewNote()) {
                notePopup.getSave().addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent clickEvent) {
                        Window.alert("Save Button now");
                        diseaseRpcService.createPublicNote(publicationID, notePopup.getGenotypeDTO(), notePopup.getTextArea().getText(), new RetrieveGenotypeListCallBack("Genotype List", view.getErrorElement()));
                        notePopup.hide();
                    }
                });
            }
        }
/*
        for (final GenotypeView.PublicNoteWidgets widget : list) {
            // add Save-Note click handler
            final NotePopup notePopup = widget.getNotePopup();
            if (notePopup.isPublicNewNote()) {
                Window.alert("HI dfdfg");
                notePopup.getSave().addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent clickEvent) {
                        diseaseRpcService.createPublicNote(publicationID, notePopup.getGenotypeDTO(), notePopup.getTextArea().getText(), new RetrieveGenotypeListCallBack("Genotype List", view.getErrorElement()));
                        notePopup.hide();
                    }
                });
            } else if (notePopup.isPrivateNewNote()) {
                notePopup.getSave().addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent clickEvent) {
                        diseaseRpcService.createCuratorNote(publicationID, notePopup.getGenotypeDTO(), notePopup.getTextArea().getText(), new RetrieveGenotypeListCallBack("Genotype List", view.getErrorElement()));
                        notePopup.hide();
                    }
                });
            } else if (notePopup.isPublicExistingNote()) {
                notePopup.getSave().addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent clickEvent) {
                        NoteDTO noteDTO = notePopup.getNoteDTO();
                        noteDTO.setNoteData(GenotypeView.getNoteStub(notePopup.getTextArea().getText()));
                        diseaseRpcService.savePublicNote(publicationID, (ExternalNoteDTO) noteDTO, new ZfinAsyncCallback<ExternalNoteDTO>("public note ", view.getErrorElement()) {
                            @Override
                            public void onSuccess(ExternalNoteDTO noteDTO) {
                                view.getPublicNoteAnchor().get(noteDTO.getZdbID()).setText(noteDTO.getNoteData());
                            }
                        });
                        notePopup.hide();
                    }
                });
            } else if (notePopup.isPrivateExistingNote()) {
                notePopup.getSave().addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent clickEvent) {
                        NoteDTO noteDTO = notePopup.getNoteDTO();
                        noteDTO.setNoteData(GenotypeView.getNoteStub(notePopup.getTextArea().getText()));
                        diseaseRpcService.saveCuratorNote(publicationID, (CuratorNoteDTO) noteDTO, new ZfinAsyncCallback<CuratorNoteDTO>("curator note ", view.getErrorElement()) {
                            @Override
                            public void onSuccess(CuratorNoteDTO noteDTO) {
                                view.getCuratorNoteAnchor().get(noteDTO.getZdbID()).setText(noteDTO.getNoteData());
                            }
                        });
                        notePopup.hide();
                    }
                });
            }
        }
*/
    }


    @Override
    public void go() {
        createGenotypeList();
    }

    private void createGenotypeList() {
        diseaseRpcService.getGenotypeList(publicationID, new RetrieveGenotypeListCallBack("Genotype List", null));
    }

    class RetrieveGenotypeListCallBack extends ZfinAsyncCallback<List<GenotypeDTO>> {

        public RetrieveGenotypeListCallBack(String errorMessage, ErrorHandler errorLabel) {
            super(errorMessage, errorLabel, (Widget) null);
        }

        @Override
        public void onSuccess(List<GenotypeDTO> list) {
            if (list != null && list.size() > 0)
                view.getNoneDefinedGenoLabel().setVisible(false);
            view.setData(list);
            bind();
        }
    }
}

package org.zfin.gwt.curation.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import org.zfin.gwt.root.dto.CuratorNoteDTO;
import org.zfin.gwt.root.dto.ExternalNoteDTO;
import org.zfin.gwt.root.dto.GenotypeDTO;
import org.zfin.gwt.root.ui.ErrorHandler;
import org.zfin.gwt.root.ui.ZfinAsyncCallback;

import java.util.Collections;
import java.util.List;

/**
 * Table of associated genotypes
 */
public class GenotypePresenter implements Presenter {

    private CurationDiseaseRPCAsync diseaseRpcService = CurationDiseaseRPC.App.getInstance();
    private GenotypeView view;
    private String publicationID;

    public GenotypePresenter(GenotypeView view, String publicationID) {
        this.view = view;
        this.publicationID = publicationID;
        this.view.setPresenter(this);
        view.setPublicationID(publicationID);
    }

    public void bind() {
        addDeleteClickHandlerToPublicNotes();
        addDeleteClickHandlerToCuratorNotes();
    }

    private void addDeleteClickHandlerToCuratorNotes() {
        List<GenotypeView.PublicNoteWidgets> list = view.getPrivateNoteWidgetsList();
        if (list.size() == 0)
            return;
        for (final GenotypeView.PublicNoteWidgets widget : list) {
            // add Delete-Note click handler
            if (widget.hasDeleteLink()) {
                widget.getDeleteImage().addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent clickEvent) {
                        diseaseRpcService.deleteCuratorNote(publicationID, widget.getCuratorNote(), new RetrieveGenotypeListCallBack("delete note", view.getErrorElement()));
                    }
                });
            }
        }
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
    }


    @Override
    public void go() {
        createGenotypeList();
    }

    private void createGenotypeList() {
        diseaseRpcService.getGenotypeList(publicationID, new RetrieveGenotypeListCallBack("Genotype List", null));
    }

    public void addCreatePublicNoteButtonClickHandler(final Button saveButton, final TextArea textArea, final GenotypeDTO genotypeDTO) {
        saveButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                diseaseRpcService.createPublicNote(publicationID, genotypeDTO, textArea.getText(), new RetrieveGenotypeListCallBack("Genotype List", view.getErrorElement()));
            }
        });
    }

    public void addCreateCuratorNoteButtonClickHandler(final Button saveButton, final TextArea textArea, final GenotypeDTO genotypeDTO) {
        saveButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                diseaseRpcService.createCuratorNote(publicationID, genotypeDTO, textArea.getText(), new RetrieveGenotypeListCallBack("Genotype List", view.getErrorElement()));
            }
        });
    }

    public void addSavePublicNoteButtonClickHandler(final Button saveButton, final TextArea textArea, final ExternalNoteDTO noteDTO) {
        saveButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                noteDTO.setNoteData(textArea.getText());
                diseaseRpcService.savePublicNote(publicationID, noteDTO, new RetrieveGenotypeListCallBack("Genotype List", view.getErrorElement()));
            }
        });
    }

    public void addSaveCuratorNoteButtonClickHandler(final Button saveButton, final TextArea textArea, final CuratorNoteDTO noteDTO) {
        saveButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                noteDTO.setNoteData(textArea.getText());
                diseaseRpcService.saveCuratorNote(publicationID, noteDTO, new RetrieveGenotypeListCallBack("Genotype List", view.getErrorElement()));
            }
        });
    }

    class RetrieveGenotypeListCallBack extends ZfinAsyncCallback<List<GenotypeDTO>> {

        public RetrieveGenotypeListCallBack(String errorMessage, ErrorHandler errorLabel) {
            super(errorMessage, errorLabel, (Widget) null);
        }

        @Override
        public void onSuccess(List<GenotypeDTO> list) {
            if (list != null && list.size() > 0) {
                Collections.sort(list);
                view.getNoneDefinedGenoLabel().setVisible(false);
                view.setData(list);
            }
            bind();
        }
    }
}

package org.zfin.gwt.curation.ui.fish;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import org.zfin.gwt.curation.ui.CurationDiseaseRPC;
import org.zfin.gwt.curation.ui.CurationDiseaseRPCAsync;
import org.zfin.gwt.curation.ui.Presenter;
import org.zfin.gwt.root.dto.CuratorNoteDTO;
import org.zfin.gwt.root.dto.ExternalNoteDTO;
import org.zfin.gwt.root.dto.GenotypeDTO;
import org.zfin.gwt.root.event.AjaxCallEventType;
import org.zfin.gwt.root.ui.ErrorHandler;
import org.zfin.gwt.root.ui.ZfinAsyncCallback;
import org.zfin.gwt.root.ui.ZfinModule;
import org.zfin.gwt.root.util.AppUtils;

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
                        AppUtils.fireAjaxCall(FishModule.getModuleInfo(), AjaxCallEventType.DELETE_CURATOR_NOTE_START);
                        diseaseRpcService.deleteCuratorNote(publicationID, widget.getCuratorNote(),
                                new RetrieveGenotypeListCallBack("delete note", view.getErrorLabel(),
                                        FishModule.getModuleInfo(), AjaxCallEventType.DELETE_CURATOR_NOTE_STOP));
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
                        AppUtils.fireAjaxCall(FishModule.getModuleInfo(), AjaxCallEventType.DELETE_PUBLIC_NOTE_START);
                        diseaseRpcService.deletePublicNote(publicationID, widget.getNote(),
                                new RetrieveGenotypeListCallBack("delete note", view.getErrorLabel(),
                                        FishModule.getModuleInfo(), AjaxCallEventType.DELETE_PUBLIC_NOTE_STOP));
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
        AppUtils.fireAjaxCall(FishModule.getModuleInfo(), AjaxCallEventType.GET_GENOTYPE_LIST_START);
        diseaseRpcService.getGenotypeList(publicationID, new RetrieveGenotypeListCallBack("Genotype List", null,
                FishModule.getModuleInfo(), AjaxCallEventType.GET_GENOTYPE_LIST_STOP));
    }

    public void addCreatePublicNoteButtonClickHandler(final Button saveButton, final TextArea textArea, final GenotypeDTO genotypeDTO) {
        saveButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                AppUtils.fireAjaxCall(FishModule.getModuleInfo(), AjaxCallEventType.CREATE_PUBLIC_NOTE_START);
                diseaseRpcService.createPublicNote(publicationID, genotypeDTO, textArea.getText(),
                        new RetrieveGenotypeListCallBack("Genotype List", view.getErrorLabel(),
                                FishModule.getModuleInfo(), AjaxCallEventType.CREATE_PUBLIC_NOTE_STOP));
            }
        });
    }

    public void addCreateCuratorNoteButtonClickHandler(final Button saveButton, final TextArea textArea, final GenotypeDTO genotypeDTO) {
        saveButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                AppUtils.fireAjaxCall(FishModule.getModuleInfo(), AjaxCallEventType.CREATE_CURATOR_NOTE_START);
                diseaseRpcService.createCuratorNote(publicationID, genotypeDTO, textArea.getText(),
                        new RetrieveGenotypeListCallBack("Genotype List", view.getErrorLabel(),
                                FishModule.getModuleInfo(), AjaxCallEventType.CREATE_CURATOR_NOTE_STOP));
            }
        });
    }

    public void addSavePublicNoteButtonClickHandler(final Button saveButton, final TextArea textArea, final ExternalNoteDTO noteDTO) {
        saveButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                noteDTO.setNoteData(textArea.getText());
                AppUtils.fireAjaxCall(FishModule.getModuleInfo(), AjaxCallEventType.SAVE_PUBLIC_NOTE_START);
                diseaseRpcService.savePublicNote(publicationID, noteDTO,
                        new RetrieveGenotypeListCallBack("Genotype List", view.getErrorLabel(),
                                FishModule.getModuleInfo(), AjaxCallEventType.SAVE_PUBLIC_NOTE_STOP));
            }
        });
    }

    public void addSaveCuratorNoteButtonClickHandler(final Button saveButton, final TextArea textArea, final CuratorNoteDTO noteDTO) {
        saveButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                noteDTO.setNoteData(textArea.getText());
                AppUtils.fireAjaxCall(FishModule.getModuleInfo(), AjaxCallEventType.SAVE_CURATOR_NOTE_START);
                diseaseRpcService.saveCuratorNote(publicationID, noteDTO,
                        new RetrieveGenotypeListCallBack("Genotype List", view.getErrorLabel(),
                                FishModule.getModuleInfo(), AjaxCallEventType.SAVE_CURATOR_NOTE_STOP));
            }
        });
    }

    class RetrieveGenotypeListCallBack extends ZfinAsyncCallback<List<GenotypeDTO>> {

        public RetrieveGenotypeListCallBack(String errorMessage, ErrorHandler errorLabel, ZfinModule module, AjaxCallEventType eventType) {
            super(errorMessage, errorLabel, (Widget) null, module, eventType);
        }

        @Override
        public void onSuccess(List<GenotypeDTO> list) {
            super.onFinish();
            if (list != null && list.size() > 0) {
                Collections.sort(list);
                view.getNoneDefinedGenoLabel().setVisible(false);
                view.setData(list);
            }
            bind();
        }
    }
}

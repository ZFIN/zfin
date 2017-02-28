package org.zfin.gwt.curation.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import org.zfin.gwt.root.dto.GoEvidenceCodeEnum;
import org.zfin.gwt.root.dto.GoEvidenceDTO;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.dto.TermNotFoundException;
import org.zfin.gwt.root.event.AjaxCallEventType;
import org.zfin.gwt.root.event.RelatedEntityEvent;
import org.zfin.gwt.root.ui.*;
import org.zfin.gwt.root.util.AppUtils;

import java.util.List;

/**
 * This class creates a MarkerGoEntry instance, but then refers to the Edit instance for editing.
 * 1 - qualifier widget
 * 2 - lookup name box
 * 4 - pubs
 * 3 - evidence codes
 */
public class GoCurationAddBox extends GoInlineCurationAddBox {


    public GoCurationAddBox(AbstractGoViewTable goViewTable) {
        this.parent = goViewTable;
        tabIndex = 10;
        initGUI();
        addInternalListeners(this);
        initWidget(panel);
    }


    protected void initGUI() {
        super.initGUI();
        saveButton.setText("Create");
        revertButton.setText("Cancel");
        zdbIDHTML.setHTML("<font color=red>Not Saved</font>");
    }


    public void updateGenes() {
        if (dto.getPublicationZdbID() != null) {
            AppUtils.fireAjaxCall(GoCurationModule.getModuleInfo(), AjaxCallEventType.GET_GENE_LIST_START);
            MarkerGoEvidenceRPCService.App.getInstance().getGenesForPub(dto.getPublicationZdbID(),
                    new MarkerEditCallBack<List<MarkerDTO>>("Failed to find genes for pub: " + dto.getPublicationZdbID()) {
                        @Override
                        public void onSuccess(List<MarkerDTO> results) {
                            AppUtils.fireAjaxCall(GoCurationModule.getModuleInfo(), AjaxCallEventType.GET_GENE_LIST_STOP);
                            geneBox.clear();
                            for (MarkerDTO dto : results) {
                                if (geneBox.setIndexForValue(dto.getName()) < 0) {
                                    geneBox.addItem(dto.getName(), dto.getZdbID());
                                }
                            }
                            GoEvidenceDTO goEvidenceDTO = dto.deepCopy();
                            goEvidenceDTO.setEvidenceCode(GoEvidenceCodeEnum.IMP);
                            if (results.iterator().hasNext()) {
                                MarkerDTO markerDTO = results.iterator().next();
                                goEvidenceDTO.setMarkerDTO(markerDTO);
                            }
                            setDTO(goEvidenceDTO);
                            setValues();
                        }

                        @Override
                        public void onFailure(Throwable throwable) {
                            super.onFailure(throwable);
                            AppUtils.fireAjaxCall(GoCurationModule.getModuleInfo(), AjaxCallEventType.GET_GENE_LIST_STOP);
                        }
                    });
        }
    }

    @Override
    protected void addInternalListeners(HandlesError handlesError) {
        super.addInternalListeners(handlesError);


        revertButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                revertGUI();
                fireEventSuccess();
            }

        });
    }


    protected void sendUpdates() {
        if (isDirty()) {
            GoEvidenceDTO goEvidenceDTO = createDTOFromGUI();
            // have to add marker
            MarkerDTO markerDTO = new MarkerDTO();
            markerDTO.setZdbID(geneBox.getSelectedStringValue());
            goEvidenceDTO.setMarkerDTO(markerDTO);
            try {
                GoEvidenceValidator.validate(goEvidenceDTO);
            } catch (ValidationException e) {
                setError(e.getMessage());
                return;
            }
            working();
            MarkerGoEvidenceRPCService.App.getInstance().createMarkerGoTermEvidence(goEvidenceDTO,
                    new MarkerEditCallBack<GoEvidenceDTO>("Failed to update GO evidence code:", this) {
                        @Override
                        public void onFailure(Throwable throwable) {
                            if (throwable instanceof TermNotFoundException) {
                                displayMessage(throwable.getMessage());
                            } else {
                                super.onFailure(throwable);
                                revertGUI();
                            }
                            notWorking();
                        }

                        @Override
                        public void onSuccess(final GoEvidenceDTO result) {
                            fireChangeEvent(new RelatedEntityEvent<GoEvidenceDTO>(result));
                            notWorking();
                            revertGUI();
                            fireEventSuccess();
                        }
                    });
        }
    }

}
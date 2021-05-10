package org.zfin.gwt.curation.ui;

import org.zfin.gwt.root.dto.GoEvidenceDTO;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.event.AjaxCallEventType;
import org.zfin.gwt.root.ui.*;
import org.zfin.gwt.root.util.AppUtils;

import java.util.List;

/**
 */
public abstract class AbstractGoCurationBox extends AbstractGoBox {

    protected ListBoxWrapper geneBox = new ListBoxWrapper();

    @Override
    protected void initGUI() {
        super.initGUI();
        ((GoEditTable) table).setGeneBox(geneBox);
    }

    @Override
    protected void revertGUI() {
        super.revertGUI();
        geneBox.setSelectedIndex(0);
    }

    @Override
    protected void setValues() {
        if (dto == null) return;
        super.setValues();

        geneBox.setEnabled(false);

        AppUtils.fireAjaxCall(GoCurationModule.getModuleInfo(), AjaxCallEventType.GET_GENES_FOR_MARKER_GO_START);
        MarkerGoEvidenceRPCService.App.getInstance().getGenesForPub(dto.getPublicationZdbID(),
                new MarkerEditCallBack<List<MarkerDTO>>("Failed to find genes for pub: " + publicationZdbID) {

                    @Override
                    public void onFailure(Throwable throwable) {
                        AppUtils.fireAjaxCall(GoCurationModule.getModuleInfo(), AjaxCallEventType.GET_GENES_FOR_MARKER_GO_STOP);
                        super.onFailure(throwable);
                        geneBox.setEnabled(true);
                    }

                    @Override
                    public void onSuccess(List<MarkerDTO> results) {
                        AppUtils.fireAjaxCall(GoCurationModule.getModuleInfo(), AjaxCallEventType.GET_GENES_FOR_MARKER_GO_STOP);
                        geneBox.clear();
                        for (MarkerDTO dto : results) {
                            if (geneBox.setIndexForValue(dto.getName()) < 0) {
                                geneBox.addItem(dto.getName(), dto.getZdbID());
                            }
                        }
                        if (dto.getMarkerDTO() != null) {
                            geneBox.setIndexForValue(dto.getMarkerDTO().getName());
                            geneBox.setEnabled(true);
                        }
                    }
                });
    }

    @Override
    public GoEvidenceDTO createDTOFromGUI() {
        GoEvidenceDTO goEvidenceDTO = super.createDTOFromGUI();

        MarkerDTO markerDTO = new MarkerDTO();
        markerDTO.setZdbID(geneBox.getSelectedStringValue());
        goEvidenceDTO.setMarkerDTO(markerDTO);

        return goEvidenceDTO;
    }
}

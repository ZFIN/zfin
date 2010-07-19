package org.zfin.gwt.curation.ui;

import org.zfin.gwt.root.dto.GoEvidenceCodeEnum;
import org.zfin.gwt.root.dto.GoEvidenceDTO;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.ui.*;

import java.util.List;

/**
 */
public abstract class AbstractGoCurationBox extends AbstractGoBox{

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
        if(dto==null) return ;
        super.setValues();

        geneBox.setEnabled(false);
        MarkerGoEvidenceRPCService.App.getInstance().getGenesForPub(dto.getPublicationZdbID(),
                new MarkerEditCallBack<List<MarkerDTO>>("Failed to find genes for pub: " + publicationZdbID) {

                    @Override
                    public void onFailure(Throwable throwable) {
                        super.onFailure(throwable);
                        geneBox.setEnabled(true);
                    }

                    @Override
                    public void onSuccess(List<MarkerDTO> results) {
                        geneBox.clear();
                        for (MarkerDTO dto : results) {
                            if (geneBox.setIndexForValue(dto.getName()) < 0) {
                                geneBox.addItem(dto.getName(), dto.getZdbID());
                            }
                        }
                        geneBox.setIndexForValue(dto.getMarkerDTO().getName());
                        geneBox.setEnabled(true);
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

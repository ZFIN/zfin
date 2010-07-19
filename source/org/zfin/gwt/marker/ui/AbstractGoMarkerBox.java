package org.zfin.gwt.marker.ui;

import com.google.gwt.user.client.ui.HTML;
import org.zfin.gwt.root.dto.GoEvidenceDTO;
import org.zfin.gwt.root.event.PublicationChangeEvent;
import org.zfin.gwt.root.ui.AbstractGoBox;
import org.zfin.gwt.root.ui.GoEditTable;

/**
 */
public abstract class AbstractGoMarkerBox extends AbstractGoBox{

    protected HTML geneLabel = new HTML();

    @Override
    protected void initGUI() {
        super.initGUI();
        setPubVisible(true) ;
    }

    @Override
    protected void revertGUI() {
        super.revertGUI();
        if(dto.getMarkerDTO()!=null){
            geneLabel = new HTML("<a href=\"/cgi-bin/webdriver?MIval=aa-markerview.apg&OID="+dto.getMarkerDTO().getZdbID()+"\">"+dto.getMarkerDTO().getName()+"</a>");
            ((GoEditTable) table).setGeneLabel(geneLabel);
        }
    }

    @Override
    protected void setValues() {
        super.setValues();
        geneLabel = new HTML("<a href=\"/cgi-bin/webdriver?MIval=aa-markerview.apg&OID="+dto.getMarkerDTO().getZdbID()+"\">"+dto.getMarkerDTO().getName()+"</a>");
        ((GoEditTable) table).setGeneLabel(geneLabel);
        inferenceListBox.setDTO(createDTOFromGUI());
    }

    @Override
    public GoEvidenceDTO createDTOFromGUI() {
        GoEvidenceDTO goEvidenceDTO = super.createDTOFromGUI();
        goEvidenceDTO.setPublicationZdbID(dto.getPublicationZdbID());
        goEvidenceDTO.setMarkerDTO(dto.getMarkerDTO());
        return goEvidenceDTO;
    }

    @Override
    public void onPublicationChanged(PublicationChangeEvent event) {
        if (event.isNotEmpty()) {
            dto.setPublicationZdbID(event.getPublication());
            super.onPublicationChanged(event);
            setValues();
        }
    }
}

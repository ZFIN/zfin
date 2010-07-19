package org.zfin.gwt.marker.ui;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import org.zfin.gwt.root.dto.GoEvidenceDTO;
import org.zfin.gwt.root.event.PublicationChangeEvent;
import org.zfin.gwt.root.event.PublicationChangeListener;
import org.zfin.gwt.root.ui.*;

import java.util.List;

/**
 */
public class GoMarkerViewTable extends AbstractGoViewTable {

    private PublicationLookupBox publicationLookupBox ;

    public GoMarkerViewTable(PublicationLookupBox publicationLookupBox){
        this.publicationLookupBox = publicationLookupBox ;
    }


    public void setValues() {
        MarkerGoEvidenceRPCService.App.getInstance().getMarkerGoTermEvidencesForMarker(zdbID,
                new MarkerEditCallBack<List<GoEvidenceDTO>>("Failed to find pub: " + zdbID + " ") {
                    @Override
                    public void onSuccess(List<GoEvidenceDTO> result) {
                        goEvidences = result;
                        refreshGUI();
                    }
                });
    }

    @Override
    public void setHeaderRow() {
        for (HeaderName headerName : HeaderName.values()) {
            setWidget(0, headerName.getIndex(), new HTML("<b>" + headerName.getValue() + "</b>"));
        }
    }

    @Override
    public void refreshGUI() {
        clearTable();
        setHeaderRow();
        int rowNumber = 1;
        String lastOntology = "";
        if (goEvidences != null) {
            for (GoEvidenceDTO goEvidenceDTO : goEvidences) {
                int columnCount = 0;
                String ontologyName = goEvidenceDTO.getGoTerm().getOntology().getOntologyName().replace("_"," ") ;
                if(false== lastOntology.equals(ontologyName)){
                    lastOntology = ontologyName;
                    setWidget(rowNumber, columnCount, new Label(lastOntology));
                }
                ++columnCount;
                if (goEvidenceDTO.getFlag() != null) {
                    setWidget(rowNumber, columnCount, new Label(goEvidenceDTO.getFlag().name()));
                }
                ++columnCount;
                setWidget(rowNumber, columnCount++, new GoLink(goEvidenceDTO.getGoTerm()));
                setWidget(rowNumber, columnCount++, new EvidenceLink(goEvidenceDTO.getEvidenceCode()));
                setWidget(rowNumber, columnCount++, new InferredFromComposite(goEvidenceDTO.getInferredFromLinks()));
                setWidget(rowNumber, columnCount++, new ReferenceComposite(goEvidenceDTO.getPublicationZdbID()));
                setWidget(rowNumber, columnCount++, new NoteHTML(goEvidenceDTO.getNote()));
                setWidget(rowNumber, columnCount++, new GoActionComposite(this, goEvidenceDTO, rowNumber));
                setRowStyle(rowNumber, 0);
                ++rowNumber;
            }
        }
    }

    protected enum HeaderName {
        ONTOLOGY(0, "Ontology"),
        QUALIFIER(1, ""),
        GO_TERM(2, "GO Term"),
        EVIDENCE(3, "Evidence"),
        INFERRED_FROM(4, "Inferred From"),
        REFERENCES(5, "Reference"),
        NOTE(6, "Note"),
        ACTIONS(7, "Action"),
        ;

        private int index;
        private String value;

        private HeaderName(int index, String value) {
            this.index = index;
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public int getIndex() {
            return index;
        }

    }

    @Override
    protected Widget createGoInlineEditBox(AbstractGoViewTable goViewTable, GoEvidenceDTO goEvidenceDTO) {
        AbstractGoBox newGoFromMarkerWidget = new GoInlineMarkerEditBox(goViewTable, goEvidenceDTO);
        newGoFromMarkerWidget.setPubVisible(true) ;
        publicationLookupBox.addPublicationChangeListener(newGoFromMarkerWidget);
        return newGoFromMarkerWidget ;
    }

    @Override
    protected Widget createGoInlineCloneBox(AbstractGoViewTable goViewTable, GoEvidenceDTO goEvidenceDTO) {
        AbstractGoBox newGoFromMarkerWidget = new GoInlineMarkerCloneBox(goViewTable, goEvidenceDTO);
        newGoFromMarkerWidget.setPubVisible(true) ;
        publicationLookupBox.addPublicationChangeListener(newGoFromMarkerWidget);
        return newGoFromMarkerWidget ;
    }
}
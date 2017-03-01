package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.lookup.ui.LookupPopup;
import org.zfin.gwt.root.dto.GoEvidenceCodeEnum;
import org.zfin.gwt.root.dto.GoEvidenceDTO;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.event.AjaxCallEventType;
import org.zfin.gwt.root.ui.AbstractGoViewTable;
import org.zfin.gwt.root.ui.GoActionComposite;
import org.zfin.gwt.root.ui.MarkerEditCallBack;
import org.zfin.gwt.root.ui.MarkerGoEvidenceRPCService;
import org.zfin.gwt.root.util.AppUtils;

import java.util.List;

import static com.google.gwt.query.client.GQuery.$;

/**
 */
public class GoCurationViewTable extends AbstractGoViewTable {

    private String geneFilter = GENE_FILTER_ALL;

    public void setValues() {
        AppUtils.fireAjaxCall(GoCurationModule.getModuleInfo(), AjaxCallEventType.GET_MARKER_GO_LIST_START);
        MarkerGoEvidenceServiceGWT.getMarkerGoTermEvidencesForPub(zdbID,
                new MarkerEditCallBack<List<GoEvidenceDTO>>("Failed to find pub: " + zdbID + " ") {
                    @Override
                    public void onSuccess(List<GoEvidenceDTO> result) {
                        AppUtils.fireAjaxCall(GoCurationModule.getModuleInfo(), AjaxCallEventType.GET_MARKER_GO_LIST_STOP);
                        goEvidences = result;
                        refreshGUI();
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        super.onFailure(throwable);
                        AppUtils.fireAjaxCall(GoCurationModule.getModuleInfo(), AjaxCallEventType.GET_MARKER_GO_LIST_STOP);
                    }
                });
    }

    public void doGeneFilter(String selectedText) {
        if (false == selectedText.equals(geneFilter)) {
            geneFilter = selectedText;
            refreshGUI();
        }
    }

    @Override
    public void setHeaderRow() {
        for (HeaderName headerName : HeaderName.values()) {
            setWidget(0, headerName.getIndex(), new HTML("<b>" + headerName.getValue() + "</b>"));
        }
    }

    private LookupPopup lookupPopup = null;

    public void refreshGUI() {
        clearTable();
        setHeaderRow();
        int rowNumber = 1;
        String lastZdbID = "";
        if (goEvidences != null) {
            for (final GoEvidenceDTO goEvidenceDTO : goEvidences) {
                MarkerDTO markerDTO = goEvidenceDTO.getMarkerDTO();
                if (geneFilter.equals(GENE_FILTER_ALL) || markerDTO.getName().equals(geneFilter)) {
                    int columnCount = 0;
                    if (false == lastZdbID.equals(markerDTO.getZdbID())) {
                        lastZdbID = markerDTO.getZdbID();
                        setWidget(rowNumber, columnCount, new GeneComposite(goEvidenceDTO, rowNumber));
                    }
                    ++columnCount;
                    if (goEvidenceDTO.getFlag() != null) {
                        setWidget(rowNumber, columnCount, new Label(goEvidenceDTO.getFlag().name()));
                    }
                    ++columnCount;
                    setWidget(rowNumber, columnCount++, new GoLink(goEvidenceDTO.getGoTerm()));
                    setWidget(rowNumber, columnCount++, new EvidenceLink(goEvidenceDTO.getEvidenceCode()));
                    setWidget(rowNumber, columnCount++, new InferredFromComposite(goEvidenceDTO.getInferredFromLinks()));
                    setWidget(rowNumber, columnCount++, new NoteHTML(goEvidenceDTO.getNote()));
                    setWidget(rowNumber, columnCount++, new GoActionComposite(this, goEvidenceDTO, rowNumber));
                    setRowStyle(rowNumber, 0);
                    ++rowNumber;
                }
            }
            Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                @Override
                public void execute() {
                    processPopupLinks();
                }
            });
        }

    }

    public static native void processPopupLinks() /*-{
        $wnd.processPopupLinks('body');
    }-*/;

    protected enum HeaderName {
        GENE(0, "Gene"),
        QUALIFIER(1, ""),
        GO_TERM(2, "GO Term"),
        EVIDENCE(3, "Evidence"),
        INFERRED_FROM(4, "Inferred From"),
        NOTES(5, "Notes"),
        ANNOTATION_ACTIONS(6, "");

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

    private void createGoFromMarker(MarkerDTO markerDTO, int rowNumber) {
        // get row
        int row = rowNumber + 1;

        // if they are the same, then just toggle them shut
        if (row == newGoRow) {
            hideNewGoRow();
            return;
        }

        if (newGoRow > 0) {
            hideNewGoRow();
        }

        GoEvidenceDTO goEvidenceDTO = new GoEvidenceDTO();
        goEvidenceDTO.setPublicationZdbID(zdbID);
        goEvidenceDTO.setMarkerDTO(markerDTO);
        goEvidenceDTO.setEvidenceCode(GoEvidenceCodeEnum.IMP);

        // get row
        insertRow(row);
        formatter.setColSpan(row, 0, HeaderName.values().length);
        Widget newGoFromMarkerWidget = new GoInlineCurationAddBox(this, goEvidenceDTO);
        setWidget(row, 0, newGoFromMarkerWidget);
        newGoRow = row;

    }

    @Override
    protected Widget createGoInlineEditBox(AbstractGoViewTable goViewTable, GoEvidenceDTO goEvidenceDTO) {
        return new GoInlineCurationEditBox(goViewTable, goEvidenceDTO);
    }

    @Override
    protected Widget createGoInlineCloneBox(AbstractGoViewTable goViewTable, GoEvidenceDTO goEvidenceDTO) {
        return new GoInlineCurationCloneBox(goViewTable, goEvidenceDTO);
    }

    protected class GeneComposite extends Composite {

        private HorizontalPanel horizontalPanel = new HorizontalPanel();
        private GoEvidenceDTO goEvidenceDTO;
        private int rowNumber;

        public GeneComposite(GoEvidenceDTO goEvidenceDTO, int rowNumber) {
            this.goEvidenceDTO = goEvidenceDTO;
            this.rowNumber = rowNumber;
            initGUI();
            initWidget(horizontalPanel);
        }

        private void initGUI() {
            horizontalPanel.add(new GeneLink(goEvidenceDTO.getMarkerDTO()));
            HTML newGOHTML = new HTML();
            newGOHTML.setHTML("[new&nbsp;GO]");
            newGOHTML.setStyleName("relatedEntityPubLink");
            newGOHTML.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    createGoFromMarker(goEvidenceDTO.getMarkerDTO(), rowNumber);
                }
            });
            horizontalPanel.add(newGOHTML);
        }
    }
}

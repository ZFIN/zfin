package org.zfin.gwt.curation.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.root.dto.GoEvidenceCodeEnum;
import org.zfin.gwt.root.dto.GoEvidenceDTO;
import org.zfin.gwt.root.dto.GoTermDTO;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.ui.MarkerEditCallBack;
import org.zfin.gwt.root.ui.MarkerGoEvidenceRPCService;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 */
public class GoViewTable extends ZfinFlexTable {

    // data
    private String publicationZdbID;
    private List<GoEvidenceDTO> goEvidences;

    // gui
    private FlexCellFormatter formatter = new FlexCellFormatter();
    private int newGoRow = -1;

    public GoViewTable() {
        super(HeaderName.values().length, -1);
        initGUI();
        RootPanel.get(GoCurationModule.GO_EVIDENCE_DISPLAY).add(this);
    }


    private void initGUI() {
        createTableHeader();
        setCellFormatter(formatter);
    }

    @Override
    public void setHeaderRow() {
        for (HeaderName headerName : HeaderName.values()) {
            setWidget(0, headerName.getIndex(), new HTML("<b>" + headerName.getValue() + "</b>"));
        }
    }

    public void setValues() {
        MarkerGoEvidenceRPCService.App.getInstance().getMarkerGoTermEvidencesForPub(publicationZdbID,
                new MarkerEditCallBack<List<GoEvidenceDTO>>("Failed to find pub: " + publicationZdbID + " ") {
                    @Override
                    public void onSuccess(List<GoEvidenceDTO> result) {
                        goEvidences = result;
                        refreshGUI();
                    }
                });
    }

    public void refreshGUI() {

        clearTable();
        setHeaderRow();
        int rowNumber = 1;
        String lastZdbID = "";
        if (goEvidences != null) {
            for (GoEvidenceDTO goEvidenceDTO : goEvidences) {
                int columnCount = 0;
                if (false == lastZdbID.equals(goEvidenceDTO.getMarkerDTO().getZdbID())) {
                    lastZdbID = goEvidenceDTO.getMarkerDTO().getZdbID();
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

    }

    @Override
    public void onClick(ClickEvent event) {
        HTMLTable.Cell cell = getCellForEvent(event);
        if (cell == null) {
            return;
        }
        Widget widget = getWidget(cell.getRowIndex(), cell.getCellIndex());
        if (widget == null) {
            return;
        }
        if (widget instanceof NoteHTML) {
            NoteHTML noteHTML = (NoteHTML) widget;
            noteHTML.onClick(event);
        }
    }

    public void setPublicationZdbID(String publicationZdbID) {
        this.publicationZdbID = publicationZdbID;
        setValues();
    }

    private enum HeaderName {
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

    private class GoLink extends HTML {

        public GoLink(GoTermDTO goTerm) {
            String htmlString = "";
            htmlString += "<a href='";
            htmlString += "http://www.ebi.ac.uk/ego/QuickGO?mode=display&entry=GO:";
            htmlString += goTerm.getDataZdbID();
            htmlString += "'>";
            htmlString += goTerm.getName();
            htmlString += "</a>";
            setHTML(htmlString);
        }
    }

    private class EvidenceLink extends HTML {
        public EvidenceLink(GoEvidenceCodeEnum evidenceCode) {
            String htmlString = "";
            htmlString += "<a href='";
            htmlString += "http://www.geneontology.org/GO.evidence.shtml#";
            htmlString += evidenceCode.name().toLowerCase();
            htmlString += "'>";
            htmlString += evidenceCode.name();
            htmlString += "</a>";
            setHTML(htmlString);
        }
    }

    private class GeneLink extends HTML {
        public GeneLink(MarkerDTO markerDTO) {
            String htmlString = "";
            htmlString += "<a href='";
            htmlString += "/cgi-bin/ZFIN_jump?record=";
            htmlString += markerDTO.getZdbID();
            htmlString += "'>";
            htmlString += markerDTO.getName();
            htmlString += "</a>";
            setHTML(htmlString);
        }
    }

    private class GeneComposite extends Composite {

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

    void cloneGO(GoEvidenceDTO goEvidenceDTO, int rowNumber) {
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

        // get row
        insertRow(row);
        formatter.setColSpan(row, 0, getCellCount(0));
        Widget newGoFromMarkerWidget = new GoInlineCloneBox(this, goEvidenceDTO);
        setWidget(row, 0, newGoFromMarkerWidget);
        newGoRow = row;
    }

    void editGO(GoEvidenceDTO goEvidenceDTO, int rowNumber) {
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

        // get row
        insertRow(row);
        formatter.setColSpan(row, 0, getCellCount(0));
        Widget newGoFromMarkerWidget = new GoInlineEditBox(this, goEvidenceDTO);
        setWidget(row, 0, newGoFromMarkerWidget);
        newGoRow = row;
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
        goEvidenceDTO.setPublicationZdbID(publicationZdbID);
        goEvidenceDTO.setMarkerDTO(markerDTO);
        goEvidenceDTO.setEvidenceCode(GoEvidenceCodeEnum.IMP);

        // get row
        insertRow(row);
        formatter.setColSpan(row, 0, 6);
        Widget newGoFromMarkerWidget = new GoInlineAddBox(this, goEvidenceDTO);
        setWidget(row, 0, newGoFromMarkerWidget);
        newGoRow = row;

    }

    void hideNewGoRow() {
        if (newGoRow >= 0 && newGoRow < getRowCount()) {
            removeRow(newGoRow);
        }
        newGoRow = -1;
    }

    /**
     */
    private class InferredFromComposite extends HTML {
        public InferredFromComposite(Set<String> inferredFromLinks) {
            String html = "";
            for (Iterator<String> iter = inferredFromLinks.iterator(); iter.hasNext();) {
                html += iter.next();
                if (iter.hasNext()) {
                    html += "<br>";
                }
            }
            setHTML(html);
        }

    }

    private class NoteHTML extends HTML implements ClickHandler {
        private final String RIGHT_ARROW = "<img align=\"top\" src=\"/images/right.gif\" class=\"clickable\">";
        private final String DOWN_ARROW = "<img align=\"top\" src=\"/images/down.gif\"  class=\"clickable\">";
        private final String ELLIPSES = "...";
        private final int MAX_LENGTH = 20;

        // state
        private boolean open = false;

        // data
        private String note;


        public NoteHTML(String note) {
            this.note = note;
            if (note != null && false == note.isEmpty()) {
                initGUI();
            }
        }

        private void initGUI() {
            setStyleName("clickable");
            setOpen(false);
        }

        private void setOpen(boolean open) {
            if (open) {
                setHTML(DOWN_ARROW + note);
            } else {
                if (note.length() > MAX_LENGTH) {
                    setHTML(RIGHT_ARROW + note.substring(0, MAX_LENGTH) + ELLIPSES);
                } else {
                    setHTML(note);
                }
            }
            this.open = open;
        }

        @Override
        public void onClick(ClickEvent event) {
            setOpen(!open);
        }
    }


}

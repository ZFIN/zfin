package org.zfin.gwt.root.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import org.zfin.gwt.root.dto.GoEvidenceCodeEnum;
import org.zfin.gwt.root.dto.GoEvidenceDTO;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.dto.TermDTO;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 */
public abstract class AbstractGoViewTable extends ZfinFlexTable implements HandlesError{

    // data
    protected String zdbID;
    protected List<GoEvidenceDTO> goEvidences;

    public static final String GENE_FILTER_ALL = "ALL" ;
    public static final String GO_EVIDENCE_DISPLAY = "go-evidence-display";

    // gui
    protected FlexCellFormatter formatter = new FlexCellFormatter();
    protected int newGoRow = -1;

    // listeners
    private final List<HandlesError> handlesErrorListeners = new ArrayList<HandlesError>();


    public abstract void setValues() ;
    public abstract void refreshGUI()  ;
    public abstract void setHeaderRow() ;
    protected abstract Widget createGoInlineEditBox(AbstractGoViewTable goViewTable , GoEvidenceDTO goEvidenceDTO);
    protected abstract Widget createGoInlineCloneBox(AbstractGoViewTable goViewTable , GoEvidenceDTO goEvidenceDTO);


    public AbstractGoViewTable() {
        super(7, -1);
        initGUI();
        RootPanel.get(GO_EVIDENCE_DISPLAY).add(this);
    }


    private void initGUI() {
        createTableHeader();
        setCellFormatter(formatter);
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

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
        setValues();
    }


    protected class GoLink extends HTML {

        public GoLink(TermDTO goTerm) {
            String htmlString = "";
            htmlString += "<a href='";
            htmlString += "http://www.ebi.ac.uk/ego/QuickGO?mode=display&entry=";
            htmlString += goTerm.getTermOboID();
            htmlString += "'>";
            htmlString += goTerm.getName();
            htmlString += "</a>";
            setHTML(htmlString);
        }
    }

    protected class EvidenceLink extends HTML {
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

    protected class GeneLink extends HTML {
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
        setWidget(row, 0, createGoInlineCloneBox(this,goEvidenceDTO));
        newGoRow = row;
    }


    protected void editGO(GoEvidenceDTO goEvidenceDTO, int rowNumber) {
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
        setWidget(row, 0, createGoInlineEditBox(this,goEvidenceDTO));
        newGoRow = row;
    }



    protected void hideNewGoRow() {
        if (newGoRow >= 0 && newGoRow < getRowCount()) {
            removeRow(newGoRow);
        }
        newGoRow = -1;
    }

    /**
     */
    protected class InferredFromComposite extends HTML {
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

    protected class NoteHTML extends HTML implements ClickHandler {
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

    @Override
    public void setError(String message) {
        Window.alert(message);
    }

    @Override
    public void clearError() {
        hideNewGoRow();
        setValues();
        fireEventSuccess();
    }

    @Override
    public void fireEventSuccess() {
        for (HandlesError handlesError : handlesErrorListeners) {
            handlesError.clearError();
        }
    }

    @Override
    public void addHandlesErrorListener(HandlesError handlesError) {
        handlesErrorListeners.add(handlesError);
    }
}

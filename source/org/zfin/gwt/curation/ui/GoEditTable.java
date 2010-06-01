package org.zfin.gwt.curation.ui;

import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.root.ui.AbstractInferenceListBox;
import org.zfin.gwt.root.ui.LookupComposite;
import org.zfin.gwt.root.ui.RevertibleTextArea;
import org.zfin.gwt.root.ui.StringListBox;

/**
 */
public class GoEditTable extends FlexTable {

    private FlexCellFormatter formatter = new FlexCellFormatter();
    private int initTabIndex ;

    public GoEditTable(int initTabIndex) {
        setCellFormatter(formatter);
        this.initTabIndex = initTabIndex ;
    }

    public void setGeneBox(ListBoxWrapper geneBox) {
        geneBox.setTabIndex(initTabIndex + Rows.GENE.tabIndex);
        formatter.setHorizontalAlignment(Rows.GENE.row, Rows.GENE.columnLabel, HasHorizontalAlignment.ALIGN_RIGHT);
        setHTML(Rows.GENE.row, Rows.GENE.columnLabel, "<b>GENE:</b>");
        setWidget(Rows.GENE.row, Rows.GENE.columnData, geneBox);
    }

    public void setQualifiers(StringListBox evidenceFlagBox) {
        evidenceFlagBox.setTabIndex(initTabIndex + Rows.QUALIFIER.tabIndex);
        setHTML(Rows.QUALIFIER.row, Rows.QUALIFIER.columnLabel, "<b>Qualifiers:</b>");
        formatter.setHorizontalAlignment(Rows.QUALIFIER.row, Rows.QUALIFIER.columnLabel, HasHorizontalAlignment.ALIGN_RIGHT);
        setWidget(Rows.QUALIFIER.row, Rows.QUALIFIER.columnData, evidenceFlagBox);
        formatter.setHorizontalAlignment(Rows.QUALIFIER.row, Rows.QUALIFIER.columnData, HasHorizontalAlignment.ALIGN_LEFT);
    }

    public void setEvidence(StringListBox evidenceCodeBox) {
        evidenceCodeBox.setTabIndex(initTabIndex + Rows.EVIDENCE.tabIndex);
        setHTML(Rows.EVIDENCE.row, Rows.EVIDENCE.columnLabel, "<b>EvidenceCode:</b>");
        setWidget(Rows.EVIDENCE.row, Rows.EVIDENCE.columnData, evidenceCodeBox);
    }

    public void setNote(RevertibleTextArea noteBox) {
        noteBox.setTabIndex(initTabIndex + Rows.NOTE.tabIndex);
        formatter.setHorizontalAlignment(Rows.NOTE.row, Rows.NOTE.columnLabel, HasHorizontalAlignment.ALIGN_RIGHT);
        setHTML(Rows.NOTE.row, Rows.NOTE.columnLabel, "<b>Private Note: </b>");
        formatter.setHorizontalAlignment(Rows.NOTE.row, Rows.NOTE.columnData, HasHorizontalAlignment.ALIGN_LEFT);
        setWidget(Rows.NOTE.row, Rows.NOTE.columnData, noteBox);
    }

    public void setInference(AbstractInferenceListBox inferenceListBox) {
        inferenceListBox.setTabIndex(initTabIndex + Rows.INFERENCES.tabIndex) ;
        setHTML(Rows.INFERENCES.row, Rows.INFERENCES.columnLabel, "<b>Inferences: </b>");
        setWidget(Rows.INFERENCES.row, Rows.INFERENCES.columnData, inferenceListBox);
        formatter.setHorizontalAlignment(Rows.INFERENCES.row, Rows.INFERENCES.columnLabel, HasHorizontalAlignment.ALIGN_RIGHT);
        formatter.setColSpan(Rows.INFERENCES.row, Rows.INFERENCES.columnData, 3);
    }

    public void setGoLookup(LookupComposite goTermBox) {
        goTermBox.setTabIndex(initTabIndex + Rows.GO_LOOKUP.tabIndex) ;
        formatter.setHorizontalAlignment(Rows.GO_LOOKUP.row, Rows.GO_LOOKUP.columnLabel, HasHorizontalAlignment.ALIGN_RIGHT);
        setHTML(Rows.GO_LOOKUP.row, Rows.GO_LOOKUP.columnLabel, "<b>GO Term:</b>");
        formatter.setHorizontalAlignment(Rows.GO_LOOKUP.row, Rows.GO_LOOKUP.columnData, HasHorizontalAlignment.ALIGN_LEFT);
        setWidget(Rows.GO_LOOKUP.row, Rows.GO_LOOKUP.columnData, goTermBox);
        formatter.setColSpan(Rows.GO_LOOKUP.row, Rows.GO_LOOKUP.columnData, 3);
    }


    public void setButtonPanel(HorizontalPanel buttonPanel) {
        setWidget(Rows.BUTTONS.row, Rows.BUTTONS.columnLabel, buttonPanel);
    }

    public void setErrorLabel(Label errorLabel) {
        setWidget(Rows.ERROR.row, Rows.ERROR.columnLabel, errorLabel);
        formatter.setColSpan(Rows.ERROR.row, Rows.ERROR.columnLabel, 1);
    }

    public void setGoTermButton(Button goTermButton) {
        goTermButton.setTabIndex(initTabIndex + Rows.GO_TERM_BUTTON.tabIndex);
        setWidget(Rows.GO_TERM_BUTTON.row, Rows.GO_TERM_BUTTON.columnLabel, goTermButton);
    }

    private enum Rows {
        GENE(0, 0,1),
        QUALIFIER(0, 2,2),
        EVIDENCE(1, 0,3),
        NOTE(1, 2,4),
        INFERENCES(2, 0,5),
        GO_LOOKUP(3, 0,10),
        BUTTONS(4, 0),
        GO_TERM_BUTTON(4, 3,12),
        ERROR(4, 1);

        private final int row;
        private final int columnLabel;
        private final int columnData;
        private final int tabIndex  ;

        private Rows(int row, int columnLabel) {
            this.row = row;
            this.columnLabel = columnLabel;
            this.columnData = columnLabel + 1;
            tabIndex = -1 ;
        }

        private Rows(int row, int columnLabel,int tabIndex) {
            this.row = row;
            this.columnLabel = columnLabel;
            this.columnData = columnLabel + 1;
            this.tabIndex = tabIndex ;
        }

    }

}

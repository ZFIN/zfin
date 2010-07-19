package org.zfin.gwt.marker.ui;

import org.zfin.gwt.root.dto.GoEvidenceCodeEnum;
import org.zfin.gwt.root.dto.GoEvidenceDTO;
import org.zfin.gwt.root.ui.AbstractGoViewTable;
import org.zfin.gwt.root.ui.GoEvidenceValidator;
import org.zfin.gwt.root.ui.MarkerEditCallBack;
import org.zfin.gwt.root.ui.MarkerGoEvidenceRPCService;

/**
 * This class creates a MarkerGoEntry instance, but then refers to the Edit instance for editing.
 * This code header houses 4 things:
 * 1 - qualifier widget
 * 2 - lookup name box
 * 4 - pubs
 * 3 - evidence codes
 */
public class GoInlineMarkerCloneBox extends AbstractGoMarkerBox{

    protected GoInlineMarkerCloneBox(){}

    protected GoInlineMarkerCloneBox(AbstractGoViewTable goViewTable) {
        super();
        this.parent = goViewTable;
        tabIndex = 50 ;
        initGUI();
        addInternalListeners(this);
        initWidget(panel);
    }

    public GoInlineMarkerCloneBox(AbstractGoViewTable goViewTable, GoEvidenceDTO goEvidenceDTO) {
        this(goViewTable);
        setDTO(goEvidenceDTO);
        dto.setZdbID(null);
        setValues();
    }

    @Override
    protected void initGUI() {
        super.initGUI();
        saveButton.setText("Create");
        revertButton.setText("Cancel");
        zdbIDHTML.setHTML("<font color=red>Not Saved</font>");
    }

    @Override
    protected void revertGUI() {
        super.revertGUI();
        goTermBox.setText("");
        evidenceFlagBox.setSelectedIndex(0);
        evidenceCodeBox.clear();
        pubText.setText("");
        saveButton.setText("Create");
        revertButton.setText("Cancel");
        zdbIDHTML.setHTML("<font color=red>Not Saved</font>");
    }

    @Override
    public void notWorking() {
        super.notWorking();
        saveButton.setEnabled(true);
        revertButton.setEnabled(true);
        saveButton.setText("Create");
        revertButton.setText("Cancel");
        zdbIDHTML.setHTML("<font color=red>Not Saved</font>");
    }

    protected void sendUpdates() {
        if (isDirty()) {
            GoEvidenceDTO goEvidenceDTO = createDTOFromGUI();
            if (false == GoEvidenceValidator.validate(this, goEvidenceDTO)) {
                return;
            }
            working();
            MarkerGoEvidenceRPCService.App.getInstance().createMarkerGoTermEvidence(goEvidenceDTO,
                    new MarkerEditCallBack<GoEvidenceDTO>("Failed to update GO evidence code:",this) {
                        @Override
                        public void onFailure(Throwable throwable) {
                            super.onFailure(throwable);
                            notWorking();
                        }

                        @Override
                        public void onSuccess(final GoEvidenceDTO result) {
                            notWorking();
                            revertGUI();
                            parent.clearError();
                        }
                    });
        }
    }
}
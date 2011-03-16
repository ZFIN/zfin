package org.zfin.gwt.curation.ui;

import org.zfin.gwt.root.dto.GoEvidenceDTO;
import org.zfin.gwt.root.ui.*;

/**
 * This class creates a MarkerGoEntry instance, but then refers to the Edit instance for editing.
 * This code header houses 4 things:
 * 1 - qualifier widget
 * 2 - lookup name box
 * 4 - pubs
 * 3 - evidence codes
 */
public class GoInlineCurationCloneBox extends GoInlineCurationAddBox {

    public GoInlineCurationCloneBox(AbstractGoViewTable goViewTable) {
        super();
        this.parent = goViewTable;
        tabIndex = 50;
        initGUI();
        addInternalListeners(this);
        initWidget(panel);
    }

    public GoInlineCurationCloneBox(AbstractGoViewTable goViewTable, GoEvidenceDTO goEvidenceDTO) {
        this(goViewTable);
        setDTO(goEvidenceDTO);
        dto.setZdbID(null);
        dto.setOrganizationSource("ZFIN");
        setValues();
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
            try {
                GoEvidenceValidator.validate(goEvidenceDTO);
            } catch (ValidationException ve) {
                setError(ve.getMessage());
                return ;
            }
            working();
            MarkerGoEvidenceRPCService.App.getInstance().createMarkerGoTermEvidence(goEvidenceDTO,
                    new MarkerEditCallBack<GoEvidenceDTO>("Failed to update GO evidence code:", this) {
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
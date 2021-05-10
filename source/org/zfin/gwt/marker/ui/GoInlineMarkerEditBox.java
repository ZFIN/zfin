package org.zfin.gwt.marker.ui;

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
public class GoInlineMarkerEditBox extends AbstractGoMarkerBox {

    public GoInlineMarkerEditBox(AbstractGoViewTable goViewTable, GoEvidenceDTO goEvidenceDTO) {
        this.parent = goViewTable;
        tabIndex = 50;

        initGUI();
        addInternalListeners(this);
        initWidget(panel);
        setDTO(goEvidenceDTO);
        setValues();
    }

    protected void sendUpdates() {
        if (isDirty()) {
            GoEvidenceDTO goEvidenceDTO = createDTOFromGUI();
            try {
                GoEvidenceValidator.validate(goEvidenceDTO);
            } catch (ValidationException e) {
                setError(e.getMessage());
                return ;
            }
            working();
            MarkerGoEvidenceRPCService.App.getInstance().editMarkerGoTermEvidenceDTO(goEvidenceDTO,
                    new MarkerEditCallBack<GoEvidenceDTO>("Failed to update GO evidence code:", this) {
                        @Override
                        public void onFailure(Throwable throwable) {
                            super.onFailure(throwable);
                            notWorking();
                            handleDirty();
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
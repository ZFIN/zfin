package org.zfin.gwt.curation.ui;

import com.google.gwt.user.client.Window;
import org.zfin.gwt.root.dto.GoEvidenceCodeEnum;
import org.zfin.gwt.root.dto.GoEvidenceDTO;
import org.zfin.gwt.root.dto.TermNotFoundException;
import org.zfin.gwt.root.ui.*;

/**
 * This class creates a MarkerGoEntry instance, but then refers to the Edit instance for editing.
 * This code header houses 4 things:
 * 1 - qualifier widget
 * 2 - lookup name box
 * 4 - pubs
 * 3 - evidence codes
 */
public class GoInlineCurationAddBox extends AbstractGoCurationBox {

    public GoInlineCurationAddBox() {
    }

    public GoInlineCurationAddBox(AbstractGoViewTable goViewTable) {
        this.parent = goViewTable;
        tabIndex = 50;
        initGUI();
        addInternalListeners(this);
        initWidget(panel);
    }

    public GoInlineCurationAddBox(AbstractGoViewTable goViewTable, GoEvidenceDTO goEvidenceDTO) {
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
        geneBox.setSelectedIndex(0);
        evidenceFlagBox.setSelectedIndex(0);
        evidenceCodeBox.setIndexForText(GoEvidenceCodeEnum.IMP.name());
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
                return;
            }
            working();
            MarkerGoEvidenceRPCService.App.getInstance().createMarkerGoTermEvidence(goEvidenceDTO,
                    new MarkerEditCallBack<GoEvidenceDTO>("Failed to update GO evidence code:", this) {
                        @Override
                        public void onFailure(Throwable throwable) {
                            if (throwable instanceof TermNotFoundException) {
                                displayMessage(throwable.getMessage());
                            } else {
                                super.onFailure(throwable);
                                revertGUI();
                            }
                            notWorking();
                            saveButton.setEnabled(true);
                            revertButton.setEnabled(true);
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
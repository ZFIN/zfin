package org.zfin.gwt.curation.ui;

import org.zfin.gwt.root.dto.GoEvidenceDTO;
import org.zfin.gwt.root.ui.MarkerEditCallBack;
import org.zfin.gwt.root.ui.MarkerGoEvidenceRPCService;
import org.zfin.gwt.root.ui.StackComposite;

/**
 */
public class InferenceEntry<T> extends StackComposite<GoEvidenceDTO> {

    private String inference;

    public InferenceEntry(GoEvidenceDTO goEvidenceDTO, String inference) {
        this.inference = inference;
        initGUI();
        setDTO(goEvidenceDTO);
        addInternalListeners(this);
        initWidget(panel);
    }

    @Override
    protected void revertGUI() {
        MarkerGoEvidenceRPCService.App.getInstance().createInferenceLink(inference,
                new MarkerEditCallBack<String>("Failed to create inference from: " + inference) {
                    @Override
                    public void onSuccess(String result) {
                        nameLabel.setHTML(result);
                    }
                });
    }


    @Override
    public boolean isDirty() {
        return false;
    }


    public String getInference() {
        return inference;
    }
}

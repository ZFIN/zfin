package org.zfin.gwt.marker.ui;

import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.RootPanel;
import org.zfin.gwt.marker.event.RelatedEntityEvent;
import org.zfin.gwt.root.dto.GoEvidenceDTO;

/**
 * Edits an existing MarkerGoEntry instance, validating before saving.
 * This code header houses 4 things
 * 1 - qualifier widget
 * 2 - lookup name box
 * 4 - pubs
 * 3 - evidence codes
 */
public class GoEvidenceHeaderEdit extends AbstractGoEvidenceHeader{

    public GoEvidenceHeaderEdit() {
        this(StandardDivNames.headerDiv) ;
    }

    public GoEvidenceHeaderEdit(String div) {

        initGUI();
        setValues() ;
        addInternalListeners(this);
        initWidget(panel);
        if(div!=null){
            RootPanel.get(div).add(this);
        }
    }

    protected void sendUpdates() {
        if (isDirty()) {
            GoEvidenceDTO goEvidenceDTO = createDTOFromGUI() ;
            if(false==GoEvidenceValidator.validate(this,createDTOFromGUI())){
                return ;
            }
            working();
            MarkerGoEvidenceRPCService.App.getInstance().editMarkerGoTermEvidenceDTO(goEvidenceDTO,new MarkerEditCallBack<GoEvidenceDTO>("Failed to update GO evidence code:"){
                @Override
                public void onFailure(Throwable throwable) {
                    super.onFailure(throwable);
                    notWorking();
                    revertGUI();
                }

                @Override
                public void onSuccess(GoEvidenceDTO result) {
                    setDTO(result);
                    fireEventSuccess();
                    notWorking();
                    fireChangeEvent(new RelatedEntityEvent<GoEvidenceDTO>(result));
                    DeferredCommand.addCommand(new CompareCommand());
                }
            });
        }
    }


    @Override
    public void working() {
        super.working();    //To change body of overridden methods use File | Settings | File Templates.
        evidenceCodeBox.setEnabled(false);
        pubLabel.setEnabled(false);
        evidenceFlagBox.setEnabled(false);
        inferenceListBox.working();
    }

    @Override
    public void notWorking() {
        super.notWorking();    //To change body of overridden methods use File | Settings | File Templates.
        evidenceCodeBox.setEnabled(true);
        evidenceFlagBox.setEnabled(true);
        nameBox.setEnabled(false);
        inferenceListBox.notWorking();
    }

}
package org.zfin.gwt.marker.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RootPanel;
import org.zfin.gwt.root.dto.GoEvidenceCodeEnum;
import org.zfin.gwt.root.dto.GoEvidenceDTO;

/**
 * This code header houses 4 things
 * 1 - qualifier widget
 * 2 - lookup name box
 * 4 - pubs
 * 3 - evidence codes
 */
public class GoEvidenceHeaderAdd extends AbstractGoEvidenceHeader{

    public GoEvidenceHeaderAdd() {
        this(StandardDivNames.headerDiv) ;
    }

    public GoEvidenceHeaderAdd(String div) {
        initGUI();
        setValues() ;
        addInternalListeners(this);
        initWidget(panel);
        if(div!=null){
            RootPanel.get(div).add(this);
        }
    }

    protected void initGUI() {
        super.initGUI();
        saveButton.setText("Create");
        revertButton.setText("Cancel");
        zdbIDHTML.setHTML("<font color=red>Not Saved</font>");
    }

    @Override
    protected void setValues() {
        super.setValues();
        if(dto!=null){
            evidenceCodeBox.setIndexForValue(GoEvidenceCodeEnum.IMP.name());
            dto.setEvidenceCode(GoEvidenceCodeEnum.IMP);
            inferenceListBox.setDTO(dto);
        }
    }

    protected void sendUpdates() {
        if (isDirty()) {
            GoEvidenceDTO goEvidenceDTO = createDTOFromGUI() ;
            if(false==GoEvidenceValidator.validate(this,goEvidenceDTO)){
                return ;
            }
            working();
            TermRPCService.App.getInstance().createMarkerGoTermEvidenceDTO(goEvidenceDTO,new MarkerEditCallBack<GoEvidenceDTO>("Failed to update GO evidence code:"){
                @Override
                public void onFailure(Throwable throwable) {
                    super.onFailure(throwable);
                    notWorking();
                    revertGUI();
                }

                @Override
                public void onSuccess(final GoEvidenceDTO result) {
                    setDTO(result);
                    fireEventSuccess();
                    notWorking();
                    saveButton.setEnabled(false);
                    revertButton.setEnabled(false);
                    goTermBox.setVisible(false);
                    nameBox.setText(goTermBox.getCurrentText());
                    nameBox.setEnabled(false);
//                    boolean doEdit = Window.confirm("Do you want to Edit this record?") ;
//                    if(doEdit){
//                        DeferredCommand.addCommand(new Command(){
//                            @Override
//                            public void execute() {
//                                Window.open("/action/marker/go-edit?zdbID="+result.getZdbID(),"_self","");
//                            }
//                        });
//                    }
//                    else{
                        Window.alert("Please close this record when finished viewing.");
                        Button button = new Button("Edit", new ClickHandler(){
                            @Override
                            public void onClick(ClickEvent event) {
                                Window.open("/action/marker/go-edit?zdbID="+result.getZdbID(),"_self","");
                            }
                        });
                        panel.add(button);
//                    }
//                    fireChangeEvent(new RelatedEntityEvent<GoEvidenceDTO>(result));
//                    DeferredCommand.addCommand(new CompareCommand());
                }
            });
        }
    }

    public boolean isDirty() {
        return true ;
    }

}
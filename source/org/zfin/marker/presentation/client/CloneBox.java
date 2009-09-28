package org.zfin.marker.presentation.client;

import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.zfin.marker.presentation.dto.CloneDTO;
import org.zfin.marker.presentation.dto.CloneTypesDTO;
import org.zfin.marker.presentation.event.CloneDataChangedEvent;
import org.zfin.marker.presentation.event.CloneDataListener;

import java.util.List;
import java.util.ArrayList;

/**
 */
public class CloneBox extends Composite implements HandlesError{
    private VerticalPanel panel = new VerticalPanel() ;
    private Button updateButton = new Button("update") ;
    private Button revertButton= new Button("revert") ;
    private HorizontalPanel buttonPanel = new HorizontalPanel() ;

    // error label
    private Label errorLabel = new Label() ;

    // table
    private Grid table = new Grid(5,4 ) ;
    private EasyListBox libraryListBox = new EasyListBox() ;
    private EasyListBox cloneRatingListBox = new EasyListBox() ;
    private EasyListBox vectorListBox = new EasyListBox() ;
    private EasyListBox digestListBox = new EasyListBox() ;
    private EasyListBox polymeraseListBox = new EasyListBox() ;
    private TextBoxWrapper insertSizeTextBox = new TextBoxWrapper() ;
    private TextBoxWrapper cloneComments = new TextBoxWrapper() ;
    private TextBoxWrapper pcrAmplificationTextBox = new TextBoxWrapper() ;
    private EasyListBox problemTypeListBox = new EasyListBox() ;
    private EasyListBox cloningSiteListBox = new EasyListBox() ;


    // internal data
    private CloneDTO cloneDTO;

    // listeners
    private List<CloneDataListener> cloneDataListeners = new ArrayList<CloneDataListener>() ;
    private List<HandlesError> handlesErrorListeners = new ArrayList<HandlesError>() ;

    public CloneBox(){
        initGUI() ;
        initWidget(panel);
    }

    protected void initGUI(){
        table.setText(0,0,"Cloning Site:");
        table.setWidget(0,1, cloningSiteListBox);
        table.setText(1,0,"Library:");
        table.setWidget(1,1, libraryListBox);
        table.setText(2,0,"Vector:");
        table.setWidget(2,1, vectorListBox);
        table.setText(3,0,"Problem Type:");
        table.setWidget(3,1, problemTypeListBox);
        table.setText(4,0,"Digest:");
        table.setWidget(4,1, digestListBox);


        cloneRatingListBox.addItem(EasyListBox.EMPTY_CHOICE,null);
        cloneRatingListBox.addItem("0","0");
        cloneRatingListBox.addItem("1","1");
        cloneRatingListBox.addItem("2","2");
        cloneRatingListBox.addItem("3","3");
        cloneRatingListBox.addItem("4","4");

        table.setText(0,2,"Clone Rating:");
        cloneRatingListBox.setEnabled(false);
        table.setWidget(0,3, cloneRatingListBox);
        table.setText(1,2,"Polymerase:");
        table.setWidget(1,3, polymeraseListBox);
        table.setText(2,2,"Insert Size:");
        table.setWidget(2,3, insertSizeTextBox);
        table.setText(3,2,"Comments:");
        cloneComments.setEnabled(false);
        table.setWidget(3,3,cloneComments);
        table.setText(4,2,"PCR Ampl:");
        table.setWidget(4,3,pcrAmplificationTextBox);

        panel.add(table);

        CloneRPCService.App.getInstance().getCloneTypes(new AsyncCallback<CloneTypesDTO>(){
            public void onFailure(Throwable throwable) {
                Window.alert("failure to load problem types: " + throwable);
            }

            public void onSuccess(CloneTypesDTO cloneTypesDTO) {
                problemTypeListBox.addNullAndItems(cloneTypesDTO.getProblemTypes());
                vectorListBox.addNullAndItems(cloneTypesDTO.getVectorNames());
                libraryListBox.addNullAndItems(cloneTypesDTO.getProbeLibraries());
                cloningSiteListBox.addNullAndItems(cloneTypesDTO.getCloneSites());
                digestListBox.addNullAndItems(cloneTypesDTO.getDigests());
                polymeraseListBox.addNullAndItems(cloneTypesDTO.getPolymeraseNames());

                refreshGUI() ;
            }
        });



        buttonPanel.add(updateButton);
        buttonPanel.add(revertButton);
        panel.add(buttonPanel);

        errorLabel.setStyleName("error");
        panel.add(errorLabel);


        libraryListBox.addChangeListener(new ChangeListener(){ public void onChange(Widget w){ checkDirty() ;}});
        cloneRatingListBox.addChangeListener(new ChangeListener(){ public void onChange(Widget w){ checkDirty() ;}});
        vectorListBox.addChangeListener(new ChangeListener(){ public void onChange(Widget w){ checkDirty() ;}});
        digestListBox.addChangeListener(new ChangeListener(){ public void onChange(Widget w){ checkDirty() ;}});
        polymeraseListBox.addChangeListener(new ChangeListener(){ public void onChange(Widget w){ checkDirty() ;}});
        problemTypeListBox.addChangeListener(new ChangeListener(){ public void onChange(Widget w){ checkDirty() ;}});
        cloningSiteListBox.addChangeListener(new ChangeListener(){ public void onChange(Widget w){ checkDirty() ;}});

        insertSizeTextBox.addKeyboardListener(new KeyboardListenerAdapter(){
            public void onKeyUp(Widget widget, char c, int i) { checkDirty() ; } });
        cloneComments.addKeyboardListener(new KeyboardListenerAdapter(){
            public void onKeyUp(Widget widget, char c, int i) { checkDirty() ; } });
        pcrAmplificationTextBox.addKeyboardListener(new KeyboardListenerAdapter(){
            public void onKeyUp(Widget widget, char c, int i) { checkDirty() ; } });



        updateButton.addClickListener(new ClickListener(){
            public void onClick(Widget widget) {
                if(isDirty()==true){
                    fireCloneDataUpdated(new CloneDataChangedEvent(createCloneDTOFromGUI())) ;
                    checkDirty();
                }
                else{
                    Window.alert("No fields changed.  Not sending update.");
                }
            }
        });

        revertButton.addClickListener(new ClickListener(){
            public void onClick(Widget widget) {
                refreshGUI() ;
                checkDirty();
            }
        });

    }

    protected void checkDirty(){
        boolean dirty = isDirty() ;
        updateButton.setEnabled(true==dirty);
        revertButton.setEnabled(true==dirty);
        if(false==dirty){
            fireEventSuccess();
        }
    }

    protected boolean isDirty(){
        try{
        if(false==cloneRatingListBox.isFieldEqual( cloneDTO.getRating()))  return true ;
        if(false==problemTypeListBox.isFieldEqual(cloneDTO.getProblemType())) return true ;
        if(false== vectorListBox.isFieldEqual(cloneDTO.getVectorName())) return true ;
        if(false== digestListBox.isFieldEqual(cloneDTO.getDigest())) return true ;
        if(false== libraryListBox.isFieldEqual(cloneDTO.getProbeLibraryName())) return true ;
        if(false== polymeraseListBox.isFieldEqual(cloneDTO.getPolymerase())) return true ;
        if(false== insertSizeTextBox.isFieldEqual(cloneDTO.getInsertSize())) return true ;
        if(false==cloneComments.isFieldEqual(cloneDTO.getCloneComments())) return true ;
        if(false==pcrAmplificationTextBox.isFieldEqual(cloneDTO.getPcrAmplification())) return true ;
        if(false== cloningSiteListBox.isFieldEqual(cloneDTO.getCloningSite())) return true ;
        }
        catch(Exception e){
            return true ; 
        }

        return false ;
    }


    public void setDomain(CloneDTO cloneDTO){
        this.cloneDTO = cloneDTO;
        refreshGUI() ;
        checkDirty();
    }


    protected CloneDTO createCloneDTOFromGUI(){
        CloneDTO newCloneDTO = new CloneDTO() ;

        newCloneDTO.setRating( cloneRatingListBox.getSelectedInteger() );
        newCloneDTO.setProblemType(  problemTypeListBox.getSelectedString() );
        newCloneDTO.setVectorName( vectorListBox.getSelectedString() );
        newCloneDTO.setDigest( digestListBox.getSelectedString() );
        newCloneDTO.setProbeLibraryName( libraryListBox.getSelectedString() );
        newCloneDTO.setPolymerase( polymeraseListBox.getSelectedString() );
        newCloneDTO.setInsertSize( insertSizeTextBox.getInteger() );
        newCloneDTO.setCloneComments( cloneComments.getText() );
        newCloneDTO.setPcrAmplification( pcrAmplificationTextBox.getText() );
        newCloneDTO.setCloningSite( cloningSiteListBox.getSelectedString() );

        // do not assing
        return newCloneDTO;
    }

    // todo: implement
    public void refreshGUI(){
        if(cloneDTO!=null){
            cloneRatingListBox.setIndexForValue(cloneDTO.getRating());
            problemTypeListBox.setIndexForValue(cloneDTO.getProblemType());

            libraryListBox.setIndexForValue(cloneDTO.getProbeLibraryName());
            vectorListBox.setIndexForValue(cloneDTO.getVectorName());
            digestListBox.setIndexForValue(cloneDTO.getDigest());
            polymeraseListBox.setIndexForValue(cloneDTO.getPolymerase());
            insertSizeTextBox.setText( (cloneDTO.getInsertSize()==null ? "" : cloneDTO.getInsertSize().toString() )) ;
            cloneComments.setText(cloneDTO.getCloneComments());
            pcrAmplificationTextBox.setText(cloneDTO.getPcrAmplification());
            cloningSiteListBox.setIndexForValue(cloneDTO.getCloningSite());
        }
    }



    protected void fireCloneDataUpdated(CloneDataChangedEvent cloneDataChangedEvent){
        for(CloneDataListener cloneDataListener : cloneDataListeners){
            cloneDataListener.cloneDataChanged(cloneDataChangedEvent);
        }
    }

    public void addCloneDataListener(CloneDataListener cloneDataListener){
        cloneDataListeners.add(cloneDataListener);
    }

    public CloneDTO getCloneDTO() {
        return cloneDTO;
    }

    public void setError(String message){
        errorLabel.setText(message);
    }

    public void clearError() {
        errorLabel.setText("");
    }

    public void addHandlesErrorListener(HandlesError handlesError){
        handlesErrorListeners.add(handlesError) ;
    }

    public void fireEventSuccess(){
        clearError();
        for(HandlesError handlesError: handlesErrorListeners){
            handlesError.clearError();
        }
    }
}

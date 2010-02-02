package org.zfin.gwt.marker.ui;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.marker.event.NoteEvent;
import org.zfin.gwt.marker.event.RemovableNoteListener;
import org.zfin.gwt.root.dto.AntibodyDTO;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.dto.NoteDTO;
import org.zfin.gwt.root.ui.HandlesError;
import org.zfin.gwt.root.ui.IsDirty;
import org.zfin.gwt.root.ui.StringListBox;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Integrates curated/private, public, and external notes.
 * We need to configure this so that only a set number of things to add/edit.
 */
public class NoteBox<T extends MarkerDTO> extends AbstractRevertibleComposite<T> implements RemovableNoteListener {

    public enum EditMode implements IsSerializable{
        PUBLIC,
        PRIVATE,
        EXTERNAL,;
    }

    // gui components
    private VerticalPanel panel = new VerticalPanel();
    private FlexTable table = new FlexTable();

    // add stuff
    private HorizontalPanel southPanel = new HorizontalPanel();
    private Button addButton = new Button("Add Note");
    private Button cancelButton = new Button("Cancel");
    private StringListBox typeListBox = new StringListBox();
    private TextArea newNoteTextArea = new TextArea();

    // internal data
    Set<EditMode> editModes = new HashSet<EditMode>();
    private EditMode defaultEditMode = null ;

    public NoteBox(String div){
        for(EditMode editMode : EditMode.values()){
            addEditMode(editMode);
        }

        initGUI();
        setValues() ;
        initWidget(panel);
        addInternalListeners(this);
        RootPanel.get(div).add(this);
    }

    private void setValues() {
        typeListBox.clear();
        List<String> items = new ArrayList<String>() ;
        for(EditMode editMode: editModes){
            if(editMode==EditMode.PUBLIC && false==containsPublicNote()){
                items.add(editMode.name());
            }
            else
            if(editMode!=EditMode.PUBLIC)
            {
                items.add(editMode.name());
            }
        }
        typeListBox.addNullAndItems(items);
        if(typeListBox.getItemCount()==2){
            typeListBox.setSelectedIndex(1);
        }
        if(defaultEditMode!=null){
            typeListBox.setIndexForValue(defaultEditMode.name());
        }
    }

    protected void initGUI() {

        panel.add(table);
        panel.setStyleName("gwt-editbox");


        newNoteTextArea.setWidth("400");
        southPanel.add(typeListBox);
        southPanel.add(newNoteTextArea);
        southPanel.add(addButton);
        southPanel.add(cancelButton);

        panel.add(southPanel);
        errorLabel.setStyleName("error");
        panel.add(errorLabel);
    }


    protected void addInternalListeners(final HandlesError handlesError) {

        newNoteTextArea.addChangeHandler(new ChangeHandler(){
            @Override
            public void onChange(ChangeEvent event) {
                checkDirty();
            }
        });

        addButton.addClickHandler(new ClickHandler(){
            @Override
            public void onClick(ClickEvent event) {
                if(false==isDirty()){
                    setError("Nothing to add.");
                    return ;
                }

                if(typeListBox.getSelected()==null){
                    setError("Must select type of note to add.");
                    return ;
                }

//                working();
                // rpc call to add type of note
                EditMode editMode = EditMode.valueOf(typeListBox.getSelected());
                final NoteDTO noteDTO = new NoteDTO();
                noteDTO.setDataZdbID(dto.getZdbID());
                noteDTO.setNoteData(newNoteTextArea.getText());
                noteDTO.setPublicationZdbID(publicationZdbID);
                noteDTO.setEditMode(editMode.name()) ;

                if(editMode==EditMode.PUBLIC){
                    MarkerRPCService.App.getInstance().editPublicNote(noteDTO,new MarkerEditCallBack<Void>("Failed to update public note"){
                        @Override
                        public void onSuccess(Void v) {
                            dto.setPublicNote(noteDTO);
                            addNoteToGUI(noteDTO);
                            DeferredCommand.addCommand(new Command(){
                                @Override
                                public void execute() {
                                    resetAddNote();
                                }
                            });
                        }
                    });
                }
                else
                if(editMode==EditMode.PRIVATE){
                    MarkerRPCService.App.getInstance().addCuratorNote(noteDTO,new MarkerEditCallBack<NoteDTO>("Failed to update public note"){
                        @Override
                        public void onSuccess(NoteDTO returnNoteDTO) {
                            dto.getCuratorNotes().add(returnNoteDTO);
                            addNoteToGUI(returnNoteDTO);
                            resetAddNote();
                        }
                    });
                }
                else
                if(editMode==EditMode.EXTERNAL){
                    if(noteDTO.getPublicationZdbID()==null|| noteDTO.getPublicationZdbID().length()<16){
                        setError("External notes require publication");
                        return ;
                    }
                    MarkerRPCService.App.getInstance().addExternalNote(noteDTO,new MarkerEditCallBack<NoteDTO>("Failed to update public note"){
                        @Override
                        public void onSuccess(NoteDTO returnNoteDTO) {
                            ((AntibodyDTO) dto).getExternalNotes().add(returnNoteDTO);
                            addNoteToGUI(returnNoteDTO);
                            resetAddNote();
                        }
                    });
                }

            }
        });

        cancelButton.addClickHandler(new ClickHandler(){
            @Override
            public void onClick(ClickEvent event) {
                resetAddNote() ;
            }
        });
    }

    private void resetAddNote() {
        newNoteTextArea.setText("");
        setValues();
        checkDirty();
//        typeListBox.setSelectedIndex(0);
    }


    public void addNoteToGUI(NoteDTO noteDTO){
        int rowCount = table.getRowCount();
        EditMode editMode = EditMode.valueOf(noteDTO.getEditMode());
        if(editMode==EditMode.EXTERNAL){
            PublicationLabel publicationLabel = new PublicationLabel(noteDTO.getPublicationZdbID());
            table.setWidget(rowCount,0,publicationLabel);
        }
        else{
            table.setHTML(rowCount,0,editMode.name());
        }
        AbstractNoteEntry noteEntry = IntegratedNoteEntryFactory.createIntegratedNoteEntry(noteDTO,this);
        noteEntry.addNoteListener(this);
        table.setWidget(rowCount,1,noteEntry);

        if(editMode==EditMode.PUBLIC){
            DeferredCommand.addCommand(new Command(){
                @Override
                public void execute() {
                    setValues();
                }
            });
        }
    }

    @Override
    public boolean isDirty() {
        if (false == newNoteTextArea.getText().equals("")) return true ;
//        if (typeListBox.getSelected()!=null) return true ;

        return false ;
    }

    @Override
    protected void revertGUI() {
        table.clear();
        if(false==dto.getPublicNote().isEmpty()){
            addNoteToGUI(dto.getPublicNote());
        }
        for(NoteDTO noteDTO: dto.getCuratorNotes()){
            addNoteToGUI(noteDTO);
        }
        if(dto instanceof AntibodyDTO){
            for(NoteDTO noteDTO: ((AntibodyDTO) dto).getExternalNotes()){
                addNoteToGUI(noteDTO);
            }
        }

        resetAddNote();
    }

    private class PublicationLabel extends HTML{
        public PublicationLabel(final String publicationZdbID) {
            setHTML(publicationZdbID);
            setStyleName("externalLink");
            addClickHandler(new ClickHandler(){
                @Override
                public void onClick(ClickEvent event) {
                    Window.open("http://zfin.org/cgi-bin/webdriver/?MIval=aa-pubview2.apg&OID=" + publicationZdbID, "", "");
                }
            });
        }
    }

    public boolean containsPublicNote(){
        return dto!=null &&
                dto.getPublicNote()!=null &&
                dto.getPublicNote().getNoteData()!=null &&
                dto.getPublicNote().getNoteData().length()>0
                ;
    }

    public void addEditMode(EditMode editMode){
        editModes.add(editMode) ;
    }

    public boolean checkDirty() {
        boolean dirty = isDirty();
        saveButton.setEnabled(dirty);
        revertButton.setEnabled(dirty);
        if (dirty) {
            newNoteTextArea.setStyleName(IsDirty.DIRTY_STYLE);
        }
        else{
            newNoteTextArea.setStyleName(IsDirty.CLEAN_STYLE);
            fireEventSuccess();
        }
        return dirty ;
    }

    public boolean hasDirtyNotes() {
        int numRows = table.getRowCount();
        for(int i = 0 ; i < numRows ; i++){
            if( ((AbstractNoteEntry) table.getWidget(i,1)).isDirty() ) return true ;
        }
        return false;
    }

    @Override
    public void removeNote(NoteEvent noteEvent) {
        NoteDTO noteDTO = noteEvent.getNoteDTO();
        int row = getRowForNote(noteDTO);
        table.removeRow(row);
        setValues();
    }

    private int getRowForNote(NoteDTO noteDTO) {
        for(int i = 0 ; i < table.getRowCount() ; i++){
            if(((AbstractNoteEntry) table.getWidget(i,1)).getNoteDTO().equals(noteDTO)){
                return i ;
            }
        }
        return -1 ;
    }


    public void removeEditMode(EditMode editMode){
        if(editModes.contains(editMode)){
            editModes.remove(editMode);
        }
    }

    public void setDefaultEditMode(EditMode editMode){
        this.defaultEditMode = editMode;
    }

}

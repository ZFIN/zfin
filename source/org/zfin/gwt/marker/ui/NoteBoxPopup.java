package org.zfin.gwt.marker.ui;

import com.google.gwt.user.client.ui.*;

/**
 * A base notebox class.  Maybe should be interface, but is good for now.
 */
public abstract class NoteBoxPopup extends PopupPanel {

    protected VerticalPanel panel = new VerticalPanel() ;
    protected HTML title = new HTML("<b>Note</b>") ;
    protected TextArea noteArea  = new TextArea() ;
    protected String imageURL = "/images/";
    protected Image closeButton = new Image(imageURL+"x-symbol.png") ;
    protected Button saveButton = new Button("save & close") ;
    protected HorizontalPanel buttonPanel = new HorizontalPanel() ;
    protected DockPanel topPanel = new DockPanel() ;

    // internal data
    protected String note ;
    protected NoteListBox parent ;

    protected NoteBoxPopup(NoteListBox noteListBox){
        super(false,false) ;
        parent = noteListBox;
        initGUI() ;
        setWidget(panel);
        reset();
    }


    protected NoteBoxPopup(NoteListBox noteListBox,String note){
        super(false,false) ;
        this.parent = noteListBox;
        this.note = note ;
        this.noteArea.setText(note);
        initGUI() ;
        setWidget(panel); 
    }

    abstract protected void initGUI()  ;
    abstract public boolean isDirty()  ;
    abstract public void checkButtonStatus() ; 

    public void reset(){
        noteArea.setText("");
    }

    public void setNote(String note) {
        this.note = note;
    }
}

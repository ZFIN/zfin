package org.zfin.gwt.marker.ui;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.RootPanel;
import org.zfin.gwt.marker.event.PublicationChangeEvent;
import org.zfin.gwt.marker.event.RelatedEntityEvent;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.ui.HandlesError;
import org.zfin.gwt.root.ui.RevertibleTextArea;
import org.zfin.gwt.root.ui.StringListBox;
import org.zfin.gwt.root.ui.StringTextBox;

import java.util.Date;

/**
 * This code header houses 4 things
 * 1 - qualifier widget
 * 2 - lookup name box
 * 4 - pubs
 * 3 - evidence codes
 */
public class StandAloneGoEvdidenceHeaderEdit extends AbstractHeaderEdit<GoEvidenceDTO>{

    // GUI name/type elements
    private final HTMLTable table = new Grid(6, 2);
    private final HTML geneHTML = new HTML();
    private final StringListBox evidenceFlagBox = new StringListBox(false);
    private final StringTextBox pubLabel = new StringTextBox();
    private final StringListBox evidenceCodeBox = new StringListBox(false);
    private final RevertibleTextArea noteBox = new RevertibleTextArea();

    // internal display data
    private boolean showButtons = false ;

    public StandAloneGoEvdidenceHeaderEdit() {
        this(StandardDivNames.headerDiv) ;
    }

    public StandAloneGoEvdidenceHeaderEdit(String div) {

        if(div!=null){
            showButtons = true ;
        }
        initGUI();
        setValues() ;
        addInternalListeners(this);
        initWidget(panel);
        if(div!=null){
            RootPanel.get(div).add(this);
        }
    }

    @Override
    protected void setValues() {

        evidenceCodeBox.clear();
        for(GoEvidenceCodeEnum evidenceCodeEnum : GoEvidenceCodeEnum.values()){
            evidenceCodeBox.addItem(evidenceCodeEnum.name());
        }
    }

    protected void addInternalListeners(final HandlesError handlesError) {
        super.addInternalListeners(this);

        evidenceCodeBox.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                DeferredCommand.addCommand(new CompareCommand());
            }
        });

        evidenceFlagBox.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                DeferredCommand.addCommand(new CompareCommand());
            }
        });

        noteBox.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                DeferredCommand.addCommand(new CompareCommand());
            }
        });

        noteBox.addKeyPressHandler(new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {
                DeferredCommand.addCommand(new CompareCommand());
            }
        });

    }

    protected void initGUI() {

        int rowCount = 0 ;
        table.setHTML(rowCount, 0, "<b>GENE:</b>");
        table.setWidget(rowCount, 1, geneHTML);
        ++rowCount;

        table.setHTML(rowCount, 0, "<b>Qualifiers:</b>");
        table.setWidget(rowCount, 1, evidenceFlagBox);
        ++rowCount;

        table.setHTML(rowCount, 0, "<b>GO Term:</b>");
        table.setWidget(rowCount, 1, nameBox);
        nameBox.setEnabled(false);
        ++rowCount;

        table.setHTML(rowCount, 0, "<b>Reference:</b>");
        table.setWidget(rowCount, 1, pubLabel);
        ++rowCount;

        table.setHTML(rowCount, 0, "<b>EvidenceCode:</b>");
        table.setWidget(rowCount, 1, evidenceCodeBox);
        ++rowCount;

        table.setHTML(rowCount, 0, "<b>Private Note: </b>");
        table.setWidget(rowCount, 1, noteBox);

        pubLabel.setEnabled(false);
        panel.add(table);

        if(showButtons){
            buttonPanel.add(saveButton);
            buttonPanel.add(new HTML("&nbsp;"));
            buttonPanel.add(revertButton);
            panel.add(buttonPanel);
        }

        panel.setStyleName("gwt-editbox");

        panel.add(new HTML("<br>")); // spacer

        errorLabel.setStyleName("error");
        panel.add(errorLabel);


    }


    protected void revertGUI() {
        geneHTML.setHTML("<a class='external' href='/cgi-bin/webdriver?MIval=aa-markerview.apg&OID="+dto.getMarkerDTO().getZdbID()+"'>"+
                dto.getMarkerDTO().getName()+"</a>");
//        zdbIDHTML.setHTML("<div class=\"attributionDefaultPub\">" + dto.getZdbID() + "</font>");
        nameBox.setText(dto.getName());
        evidenceCodeBox.setIndexForText(dto.getEvidenceCode().name());


        evidenceFlagBox.clear();
        evidenceFlagBox.addItem("NONE","null");
        // contributes to only for molecular function
        if(dto.getGoTerm().getSubOntology().equals(GoTermDTO.MOLECULAR_FUNCTION)){
            evidenceFlagBox.addItem(GoFlagEnum.CONTRIBUTES_TO.toString());
        }
        evidenceFlagBox.addItem(GoFlagEnum.NOT.toString());

        if(dto.getFlag()==null){
            evidenceFlagBox.setItemSelected(0,true);
        }
        else{
            evidenceFlagBox.setIndexForText( dto.getFlag().toString());
        }
        pubLabel.setText(dto.getPublicationZdbID());

        noteBox.setText(dto.getNote());

        handleDirty();
    }

    protected void sendUpdates() {
        if (isDirty()) {
            working();
            TermRPCService.App.getInstance().editMarkerHeaderGoTermEvidenceDTO(createDTOFromGUI(),new MarkerEditCallBack<GoEvidenceDTO>("Failed to update GO evidence code:"){
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
                    DeferredCommand.addCommand(new CompareCommand());
                    notWorking();
                    fireChangeEvent(new RelatedEntityEvent<GoEvidenceDTO>(result));
                }
            });
        }
    }

    public GoEvidenceDTO createDTOFromGUI() {
        GoEvidenceDTO goEvidenceDTO = dto.deepCopy();
        goEvidenceDTO.setFlag(evidenceFlagBox.getSelected()==null ? null : GoFlagEnum.getType(evidenceFlagBox.getSelected()));
        goEvidenceDTO.setPublicationZdbID(pubLabel.getBoxValue());
        goEvidenceDTO.setEvidenceCode(GoEvidenceCodeEnum.valueOf(evidenceCodeBox.getSelected()));
        goEvidenceDTO.setNote(noteBox.getText());
        goEvidenceDTO.setModifiedDate(new Date());
        return goEvidenceDTO;
    }


    public boolean isDirty() {
        boolean isDirty = false;
        isDirty = nameBox.isDirty(dto.getName()) || isDirty ;
        isDirty = evidenceCodeBox.isDirty(dto.getEvidenceCode().name()) || isDirty ;
        isDirty = pubLabel.isDirty(dto.getPublicationZdbID()) || isDirty ;
        isDirty = evidenceFlagBox.isDirty( (dto.getFlag()==null ? null : dto.getFlag().toString())) || isDirty ;
        isDirty = noteBox.isDirty( (dto.getNote()==null ? null : dto.getNote())) || isDirty ;
        return isDirty;
    }




    @Override
    public void working() {
        super.working();    //To change body of overridden methods use File | Settings | File Templates.
        evidenceCodeBox.setEnabled(false);
        pubLabel.setEnabled(false);
        evidenceFlagBox.setEnabled(false);
    }

    @Override
    public void notWorking() {
        super.notWorking();    //To change body of overridden methods use File | Settings | File Templates.
        evidenceCodeBox.setEnabled(true);
        evidenceFlagBox.setEnabled(true);
        nameBox.setEnabled(false);
    }

    @Override
    public void onPublicationChanged(PublicationChangeEvent event) {
        if(event.isNotEmpty()){
            super.onPublicationChanged(event);
            pubLabel.setText(event.getPublication());

            if( GoCurationDefaultPublications.INTERPRO.equals(event.getPublication())
                    ||
                    GoCurationDefaultPublications.SPKW.equals(event.getPublication())
                    ||
                    GoCurationDefaultPublications.EC.equals(event.getPublication())
                    ){
                evidenceCodeBox.setIndexForText(GoEvidenceCodeEnum.IEA.name());
            }
            else
            if(GoCurationDefaultPublications.ROOT.equals(event.getPublication())){
                evidenceCodeBox.setIndexForText(GoEvidenceCodeEnum.ND.name());
            }

            handleDirty();
        }
    }
}
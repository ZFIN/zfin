package org.zfin.gwt.marker.ui;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLTable;
import org.zfin.gwt.marker.event.PublicationChangeEvent;
import org.zfin.gwt.marker.event.RelatedEntityChangeListener;
import org.zfin.gwt.marker.event.RelatedEntityEvent;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.ui.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This code header houses 4 things
 * 1 - qualifier widget
 * 2 - lookup name box
 * 4 - pubs
 * 3 - evidence codes
 */
public abstract class AbstractGoEvidenceHeader extends AbstractHeaderEdit<GoEvidenceDTO>{

    // GUI name/type elements
    protected final HTMLTable table = new Grid(8, 2);
    protected final HTML geneHTML = new HTML();
    protected final StringListBox evidenceFlagBox = new StringListBox(false);
    protected final StringTextBox pubLabel = new StringTextBox();
    protected final StringListBox evidenceCodeBox = new StringListBox(false);
    protected final RevertibleTextArea noteBox = new RevertibleTextArea();
    protected final InferenceListBoxComposite inferenceListBox = new InferenceListBoxComposite(null);
    protected LookupComposite goTermBox = new LookupComposite();
    protected GoTermDTO temporaryGoTermDTO = null ;

    // listeners
    protected List<RelatedEntityChangeListener<GoEvidenceDTO>> goTermChangeListeners = new ArrayList<RelatedEntityChangeListener<GoEvidenceDTO>>();

    @Override
    protected void setValues() {

        evidenceCodeBox.clear();
        for(GoEvidenceCodeEnum evidenceCodeEnum : GoEvidenceCodeEnum.values()){
            evidenceCodeBox.addItem(evidenceCodeEnum.name());
        }
    }

    protected void addInternalListeners(final HandlesError handlesError) {
        super.addInternalListeners(this);

        inferenceListBox.addGoTermChangeListeners(new RelatedEntityChangeListener<GoEvidenceDTO>(){
            public void dataChanged(RelatedEntityEvent<GoEvidenceDTO> dataChangedEvent) {
                fireGoTermChanged(dataChangedEvent);
            }
        });
        inferenceListBox.addHandlesErrorListener(this) ;
        inferenceListBox.addRelatedEnityChangeListener(new RelatedEntityChangeListener<GoEvidenceDTO>(){

            public void dataChanged(RelatedEntityEvent<GoEvidenceDTO> dataChangedEvent) {
                handleDirty();
            }
        });

        evidenceCodeBox.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                DeferredCommand.addCommand(new CompareCommand());
                inferenceListBox.setDTO(createDTOFromGUI());
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
            public void onKeyPress(KeyPressEvent event) {
                DeferredCommand.addCommand(new CompareCommand());
            }
        });

        goTermBox.setAction(new SubmitAction(){
            public void doSubmit(String value) {
                if(value.isEmpty()){
                    setError("Go term is invalid ["+value+"].  Please add a valid go term.");
                    return ;
                }
                TermRPCService.App.getInstance().getGOTermByName(value, new MarkerEditCallBack<GoTermDTO>("Failed to retrieve GO value"){
                    @Override
                    public void onSuccess(GoTermDTO result) {
                        temporaryGoTermDTO = result;
                        GoEvidenceDTO goEvidenceDTO = dto.deepCopy();
                        goEvidenceDTO.setGoTerm(temporaryGoTermDTO);
                        fireGoTermChanged(new RelatedEntityEvent<GoEvidenceDTO>(goEvidenceDTO));
                        handleDirty();
                    }
                });
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
        goTermBox.setType(LookupComposite.GDAG_TERM_LOOKUP);
        goTermBox.setOntology(OntologyDTO.GO);
        goTermBox.setWildCard(false);
        goTermBox.setSuggestBoxWidth(60);
        goTermBox.initGui();
        table.setHTML(rowCount, 0, "<b>GO Term:</b>");
        table.setWidget(rowCount, 1, goTermBox);

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

        ++rowCount;
        table.setHTML(rowCount, 0, "<b>Inferences: </b>");
        table.setWidget(rowCount, 1, inferenceListBox);

        ++rowCount;
        table.setHTML(rowCount, 0, "<b style=\"font-size: small;\">ZdbID: </b>");
        table.setWidget(rowCount, 1, zdbIDHTML);

        pubLabel.setEnabled(false);
        panel.add(table);

        buttonPanel.add(saveButton);
        buttonPanel.add(new HTML("&nbsp;"));
        buttonPanel.add(revertButton);
        panel.add(buttonPanel);

        panel.setStyleName("gwt-editbox");
        panel.add(new HTML("<br>")); // spacer

        errorLabel.setStyleName("error");
        panel.add(errorLabel);


    }


    protected void revertGUI() {
        geneHTML.setHTML("<a class='external' href='/cgi-bin/webdriver?MIval=aa-markerview.apg&OID="+dto.getMarkerDTO().getZdbID()+"'>"+
                dto.getMarkerDTO().getAbbreviation()+"</a>");
        if(dto.getZdbID()==null || dto.getZdbID().equals("null")){
            zdbIDHTML.setHTML("<font color=red>Not Created</font>");
        }else{
            zdbIDHTML.setHTML("<div style=\"font-size: small;\">" + dto.getZdbID() + "</font>");

        }
        if(dto.getGoTerm()!=null){
            nameBox.setText(dto.getGoTerm().getName());
        }
        if(dto.getEvidenceCode()!=null){
            evidenceCodeBox.setIndexForText(dto.getEvidenceCode().name());
        }


        evidenceFlagBox.clear();
        evidenceFlagBox.addItem("NONE","null");
        // contributes to only for molecular function
        if(dto.getGoTerm()!=null){
            if(dto.getGoTerm().getSubOntology().equals(GoTermDTO.MOLECULAR_FUNCTION)){
                evidenceFlagBox.addItem(GoFlagEnum.CONTRIBUTES_TO.toString());
            }
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
        inferenceListBox.setDTO(dto);
        if(dto.getGoTerm()!=null){
            goTermBox.setText(dto.getGoTerm().getName());
        }

        handleDirty();
    }

    public GoEvidenceDTO createDTOFromGUI() {
        GoEvidenceDTO goEvidenceDTO ;
        if(dto!=null){
            goEvidenceDTO = dto.deepCopy();
        }
        else{
            goEvidenceDTO = new GoEvidenceDTO();
        }
        goEvidenceDTO.setFlag(evidenceFlagBox.getItemCount()==0 || evidenceFlagBox.getSelected()==null  ? null : GoFlagEnum.getType(evidenceFlagBox.getSelected()));
        goEvidenceDTO.setPublicationZdbID(pubLabel.getBoxValue());
        goEvidenceDTO.setEvidenceCode(GoEvidenceCodeEnum.valueOf(evidenceCodeBox.getSelected()));
        goEvidenceDTO.setNote(noteBox.getText());
        goEvidenceDTO.setModifiedDate(new Date());
        goEvidenceDTO.setGoTerm(temporaryGoTermDTO);

        if(inferenceListBox.createDTOFromGUI()!=null){
            goEvidenceDTO.setInferredFrom(inferenceListBox.createDTOFromGUI().getInferredFrom());
        }

        return goEvidenceDTO;
    }


    public boolean isDirty() {
        if(dto==null) return false ;
        boolean isDirty = false;
        isDirty = nameBox.isDirty(dto.getName()) || isDirty ;
        isDirty = evidenceCodeBox.isDirty(dto.getEvidenceCode().name()) || isDirty ;
        isDirty = pubLabel.isDirty(dto.getPublicationZdbID()) || isDirty ;
        isDirty = evidenceFlagBox.isDirty( (dto.getFlag()==null ? null : dto.getFlag().toString())) || isDirty ;
        isDirty = noteBox.isDirty( (dto.getNote()==null ? null : dto.getNote().toString())) || isDirty ;
        isDirty = inferenceListBox.isDirty() || isDirty ;
        if(temporaryGoTermDTO!=null){
            if(false==dto.getGoTerm().getName().equals(temporaryGoTermDTO.getName())){
                goTermBox.setStyleName(IsDirty.DIRTY_STYLE);
                isDirty = true ;
            }
            else{
                goTermBox.setStyleName(IsDirty.CLEAN_STYLE);
                isDirty = false || isDirty ;
            }
        }
        return isDirty;
    }




    @Override
    public void working() {
        super.working();
        evidenceCodeBox.setEnabled(false);
        pubLabel.setEnabled(false);
        evidenceFlagBox.setEnabled(false);
        inferenceListBox.working();
    }

    @Override
    public void notWorking() {
        super.notWorking();
        evidenceCodeBox.setEnabled(true);
        evidenceFlagBox.setEnabled(true);
        nameBox.setEnabled(false);
        inferenceListBox.notWorking();
    }


    @Override
    public void onPublicationChanged(PublicationChangeEvent event) {
        if(event.isNotEmpty()){
            super.onPublicationChanged(event);
            pubLabel.setText(publicationZdbID);

            GoCurationDefaultPublications goPubEnum = GoCurationDefaultPublications.getPubForZdbID(publicationZdbID) ;
            if(goPubEnum!=null){
                switch(goPubEnum){
                    case INTERPRO:
                    case SPKW:
                    case EC:
                        evidenceCodeBox.setIndexForText(GoEvidenceCodeEnum.IEA.name());
                        break;
                    case ISS_REF_GENOME:
                    case ISS_MANUAL_CURATED:
                        evidenceCodeBox.setIndexForText(GoEvidenceCodeEnum.ISS.name());
                        break ;
                    case ROOT:
                        evidenceCodeBox.setIndexForText(GoEvidenceCodeEnum.ND.name());
                        break;
                    default:
                }
            }

            inferenceListBox.setDTO(createDTOFromGUI());
            handleDirty();
        }
    }

    @Override
    public void setDTO(GoEvidenceDTO dto) {
        super.setDTO(dto);
        inferenceListBox.setDTO(this.dto);
        if(this.dto.getGoTerm()!=null){
            goTermBox.setText(this.dto.getGoTerm().getName());
        }
        temporaryGoTermDTO = this.dto.getGoTerm();
    }


    public void addGoTermChangeListeners(RelatedEntityChangeListener<GoEvidenceDTO> changeListener) {
        goTermChangeListeners.add(changeListener) ;
    }

    protected void fireGoTermChanged(RelatedEntityEvent<GoEvidenceDTO> relatedEntityDTO) {
        for(RelatedEntityChangeListener<GoEvidenceDTO> relatedEntityChangeListener: this.goTermChangeListeners){
            relatedEntityChangeListener.dataChanged(relatedEntityDTO);
        }
    }

}
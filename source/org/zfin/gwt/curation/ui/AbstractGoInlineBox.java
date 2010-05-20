package org.zfin.gwt.curation.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.event.RelatedEntityChangeListener;
import org.zfin.gwt.root.event.RelatedEntityEvent;
import org.zfin.gwt.root.ui.*;
import org.zfin.gwt.root.util.LookupRPCService;

import java.util.List;

/**
 * This class creates a MarkerGoEntry instance, but then refers to the Edit instance for editing.
 * This code header houses 4 things:
 * 1 - qualifier widget
 * 2 - lookup name box
 * 4 - pubs
 * 3 - evidence codes
 */
public abstract class AbstractGoInlineBox extends AbstractGoEvidenceHeader {

    protected ListBoxWrapper geneBox = new ListBoxWrapper();
    // http://tntluoma.com/sidebars/codes/
    protected final TermInfoComposite termInfoComposite = new TermInfoComposite(false, "&nbsp;&bull;&nbsp;", false);
    protected HorizontalPanel buttonPanel = new HorizontalPanel();
    protected Button goTermButton = new Button("<<--Use&nbsp;GO&nbsp;Term");
    protected HorizontalPanel mainPanel = new HorizontalPanel();
    protected HorizontalPanel zdbIDPanel = new HorizontalPanel();
    protected VerticalPanel eastPanel = new VerticalPanel();

    // data
    protected GoViewTable parent;

    protected void initGUI() {

        inferenceListBox = null;
        inferenceListBox = new GoCurationInferenceListBox();

        table = new GoEditTable();
        ((GoEditTable) table).setGeneBox(geneBox);

        ((GoEditTable) table).setQualifiers(evidenceFlagBox);
        ((GoEditTable) table).setEvidence(evidenceCodeBox);
        ((GoEditTable) table).setNote(noteBox);
        ((GoEditTable) table).setInference(inferenceListBox);
        ((GoEditTable) table).setButtonPanel(buttonPanel);
        ((GoEditTable) table).setErrorLabel(errorLabel);
        ((GoEditTable) table).setGoTermButton(goTermButton);

        goTermBox.setType(LookupComposite.GDAG_TERM_LOOKUP);
        goTermBox.setOntology(OntologyDTO.GO);
        goTermBox.setWildCard(false);
        goTermBox.setSuggestBoxWidth(60);
        goTermBox.setShowTermDetail(false);
        goTermBox.initGui();

        ((GoEditTable) table).setGoLookup(goTermBox);

        pubLabel.setEnabled(false);

        zdbIDPanel.add(new HTML("<b style=\"font-size: small;\">ZdbID: </b>"));
        zdbIDPanel.add(zdbIDHTML);
        eastPanel.add(zdbIDPanel);
        ScrollPanel scrollPanel = new ScrollPanel(termInfoComposite);
        scrollPanel.setAlwaysShowScrollBars(false);
        scrollPanel.setSize("700px","300px");
        eastPanel.add(scrollPanel);
        eastPanel.setWidth("800px");

        mainPanel.add(table);
        mainPanel.add(eastPanel);
        panel.add(mainPanel);

        buttonPanel.add(saveButton);
        buttonPanel.add(revertButton);

        panel.setStyleName("gwt-editbox");
        panel.add(new HTML("<br>")); // spacer

        errorLabel.setStyleName("error");
        saveButton.setText("Save");
        revertButton.setText("Cancel");
        zdbIDHTML.setHTML("");
        termInfoComposite.setWidth("600px");
    }

    protected class GoTermInfoCallBack extends TermInfoCallBack {
        public GoTermInfoCallBack(TermInfoComposite termInfoComposite, String termID) {
            super(termInfoComposite, termID);
        }

        @Override
        public void onSuccess(TermInfo result) {
            super.onSuccess(result);
            updateQualifiers(result);
        }

        private void updateQualifiers(TermInfo result) {
            evidenceFlagBox.clear();
            evidenceFlagBox.addItem("NONE", "null");
            if (result.getOntology() == OntologyDTO.GO_BP) {
                evidenceFlagBox.addItem(GoEvidenceQualifier.CONTRIBUTES_TO.toString());
            }
            evidenceFlagBox.addItem(GoEvidenceQualifier.NOT.toString());
        }
    }


    @Override
    protected void addInternalListeners(HandlesError handlesError) {
        super.addInternalListeners(handlesError);


        goTermBox.setHighlightAction(new HighlightAction() {
            @Override
            public void onHighlight(String termName) {
                if(false==termName.startsWith(ItemSuggestCallback.END_ELLIPSE)){
                    LookupRPCService.App.getInstance().getTermInfoByName(OntologyDTO.GO, termName, new TermInfoCallBack(termInfoComposite, termName));
                }
            }
        });

        addGoTermChangeListeners(new RelatedEntityChangeListener<GoEvidenceDTO>() {
            @Override
            public void dataChanged(RelatedEntityEvent<GoEvidenceDTO> dataChangedEvent) {
                String termID = "GO:" + dataChangedEvent.getDTO().getGoTerm().getDataZdbID();
                LookupRPCService.App.getInstance().getTermInfo(OntologyDTO.GO, termID, new GoTermInfoCallBack(termInfoComposite, termID));
            }
        });


        // TODO: maybe this should go into revertGUI
        revertButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                parent.hideNewGoRow();
            }
        });

        goTermButton.addClickHandler(new ClickHandler(){
            @Override
            public void onClick(ClickEvent event) {
                TermInfo termInfo = termInfoComposite.getCurrentTermInfo() ;
                if(termInfo!=null){
                    MarkerGoEvidenceRPCService.App.getInstance().getGOTermByName(termInfo.getName(), new MarkerEditCallBack<GoTermDTO>("Failed to retrieve GO value") {
                        @Override
                        public void onSuccess(GoTermDTO result) {
                            temporaryGoTermDTO = result;
                            GoEvidenceDTO goEvidenceDTO = dto.deepCopy();
                            goEvidenceDTO.setGoTerm(temporaryGoTermDTO);
                            fireGoTermChanged(new RelatedEntityEvent<GoEvidenceDTO>(goEvidenceDTO));
                            goTermBox.setText(result.getName());
                            handleDirty();
                        }
                    });
                }
                else{
                   setError("Term details box must be empty."); 
                }
            }
        });
    }

    @Override
    protected void setValues() {
        if (dto != null) {
            evidenceCodeBox.clear();
            for (GoEvidenceCodeEnum evidenceCodeEnum : GoEvidenceCodeEnum.getCodeEnumForPub(dto.getPublicationZdbID())) {
                evidenceCodeBox.addItem(evidenceCodeEnum.name());
            }
            if(dto.getEvidenceCode()==GoEvidenceCodeEnum.NAS){
                evidenceCodeBox.addItem(dto.getEvidenceCode().name());
            }

            evidenceCodeBox.setIndexForText(dto.getEvidenceCode().name());
            inferenceListBox.setDTO(dto);

            evidenceFlagBox.clear();
            evidenceFlagBox.addItem("NONE", "null");
            if (dto.getGoTerm() != null && dto.getGoTerm().getSubOntology().equals(GoTermDTO.MOLECULAR_FUNCTION)) {
                evidenceFlagBox.addItem(GoEvidenceQualifier.CONTRIBUTES_TO.toString());
            }
            evidenceFlagBox.addItem(GoEvidenceQualifier.NOT.toString());
            if (dto.getFlag() != null) {
                evidenceFlagBox.setIndexForValue(dto.getFlag().toString());
            }


            if (dto.getGoTerm() != null) {
                LookupRPCService.App.getInstance().getTermInfoByName(OntologyDTO.GO, dto.getGoTerm().getName(), new TermInfoCallBack(termInfoComposite, dto.getGoTerm().getName()));
            }


            geneBox.setEnabled(false);
            MarkerGoEvidenceRPCService.App.getInstance().getGenesForPub(dto.getPublicationZdbID(),
                    new MarkerEditCallBack<List<MarkerDTO>>("Failed to find genes for pub: " + publicationZdbID) {

                        @Override
                        public void onFailure(Throwable throwable) {
                            super.onFailure(throwable);
                            geneBox.setEnabled(true);
                        }

                        @Override
                        public void onSuccess(List<MarkerDTO> results) {
                            geneBox.clear();
                            for (MarkerDTO dto : results) {
                                if (geneBox.setIndexForValue(dto.getName()) < 0) {
                                    geneBox.addItem(dto.getName(), dto.getZdbID());
                                }
                            }
                            geneBox.setIndexForValue(dto.getMarkerDTO().getName());
                            geneBox.setEnabled(true);
                        }
                    });
        }
    }

    @Override
    public GoEvidenceDTO createDTOFromGUI() {
        GoEvidenceDTO goEvidenceDTO = super.createDTOFromGUI();
        MarkerDTO markerDTO = new MarkerDTO();
        goEvidenceDTO.setPublicationZdbID(dto.getPublicationZdbID());
        markerDTO.setZdbID(geneBox.getSelectedStringValue());
        goEvidenceDTO.setMarkerDTO(markerDTO);
        return goEvidenceDTO;
    }


    @Override
    public boolean isDirty() {
        return true ;
    }
}
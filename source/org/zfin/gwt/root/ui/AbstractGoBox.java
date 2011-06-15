package org.zfin.gwt.root.ui;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.event.PublicationChangeEvent;
import org.zfin.gwt.root.event.RelatedEntityChangeListener;
import org.zfin.gwt.root.event.RelatedEntityEvent;
import org.zfin.gwt.root.util.LookupRPCService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class creates a MarkerGoEntry instance, but then refers to the Edit instance for editing.
 * This code header houses 4 things:
 * 1 - qualifier widget
 * 2 - lookup name box
 * 4 - pubs
 * 3 - evidence codes
 */
public abstract class AbstractGoBox extends AbstractHeaderEdit<GoEvidenceDTO> {

    // http://tntluoma.com/sidebars/codes/
    protected HorizontalPanel buttonPanel = new HorizontalPanel();
    protected Button goTermButton = new Button("<<--Use&nbsp;GO&nbsp;Term");
    protected HorizontalPanel mainPanel = new HorizontalPanel();
    protected HorizontalPanel zdbIDPanel = new HorizontalPanel();
    protected VerticalPanel eastPanel = new VerticalPanel();
    protected HorizontalPanel northEastPanel = new HorizontalPanel();

    // GUI name/type elements
    protected FlexTable table;
    protected final HTML geneHTML = new HTML();
    protected final StringListBox evidenceFlagBox = new StringListBox(false);
    protected final HorizontalPanel pubPanel = new HorizontalPanel();
    protected final Label pubLabel = new HTML("<b>Publication:</b>");
    protected final StringTextBox pubText = new StringTextBox();
    protected final HTML organizationHTML = new HTML();
    protected final StringListBox evidenceCodeBox = new StringListBox(false);
    protected final RevertibleTextArea noteBox = new RevertibleTextArea();
    protected AbstractInferenceListBox inferenceListBox = new InferenceListBox(null);
    protected LookupComposite goTermBox = new LookupComposite();
    protected TermDTO temporaryGoTermDTO = null;
    protected final TermInfoComposite termInfoComposite = new TermInfoComposite(false, "&nbsp;&bull;&nbsp;", false);

    // listeners
    protected List<RelatedEntityChangeListener<GoEvidenceDTO>> goTermChangeListeners = new ArrayList<RelatedEntityChangeListener<GoEvidenceDTO>>();

    // data
    protected AbstractGoViewTable parent;
    protected int tabIndex;

    protected void initGUI() {

        inferenceListBox = null;
        inferenceListBox = new GoCurationInferenceListBox();

        table = new GoEditTable(getTabIndex());

        ((GoEditTable) table).setQualifiers(evidenceFlagBox);
        ((GoEditTable) table).setEvidence(evidenceCodeBox);
        ((GoEditTable) table).setNote(noteBox);
        ((GoEditTable) table).setInference(inferenceListBox);
        ((GoEditTable) table).setButtonPanel(buttonPanel);
        ((GoEditTable) table).setErrorLabel(errorLabel);

        goTermBox.setType(LookupComposite.GDAG_TERM_LOOKUP);
        goTermBox.setOntology(OntologyDTO.GO);
        goTermBox.setWildCard(false);
        goTermBox.setSuggestBoxWidth(60);
        goTermBox.setTermInfoTable(termInfoComposite);
        goTermBox.setSubmitOnEnter(true);
        goTermBox.setUseIdAsValue(true);
        goTermBox.setLimit(30);
        goTermBox.initGui();

        ((GoEditTable) table).setGoLookup(goTermBox);

        pubText.setEnabled(false);

        northEastPanel.add(goTermButton);
        zdbIDPanel.add(new HTML("<b style=\"font-size: small;\">ZdbID: </b>"));
        zdbIDPanel.add(zdbIDHTML);
        northEastPanel.add(zdbIDPanel);
        eastPanel.add(northEastPanel);
        ScrollPanel scrollPanel = new ScrollPanel(termInfoComposite);
        scrollPanel.setAlwaysShowScrollBars(false);
        scrollPanel.setSize("500px", "300px");
        eastPanel.add(scrollPanel);
        eastPanel.setWidth("500px");

        mainPanel.add(table);
        mainPanel.add(eastPanel);
        panel.add(mainPanel);
        pubPanel.setVisible(false);
        pubPanel.add(pubLabel);
        pubPanel.add(pubText);
        pubPanel.add(new HTML("&nbsp;"));
        panel.add(pubPanel);
        panel.add(organizationHTML);

        buttonPanel.add(saveButton);
        buttonPanel.add(revertButton);

        panel.setStyleName("gwt-editbox");
        panel.add(new HTML("<br>")); // spacer

        errorLabel.setStyleName("error");
        saveButton.setText("Save");
        revertButton.setText("Cancel");
        zdbIDHTML.setHTML("");
        termInfoComposite.setWidth("400px");
    }

    @Override
    protected void revertGUI() {

        if (dto.getMarkerDTO() != null) {
            geneHTML.setHTML("<a class='external' href='/cgi-bin/webdriver?MIval=aa-markerview.apg&OID=" + dto.getMarkerDTO().getZdbID() + "'>" +
                    dto.getMarkerDTO().getName() + "</a>");
        }
        if (dto.getZdbID() == null || dto.getZdbID().equals("null")) {
            zdbIDHTML.setHTML("<font color=red>Not Created</font>");
        } else {
            zdbIDHTML.setHTML("<div style=\"font-size: small;\">" + dto.getZdbID() + "</font>");

        }
        if (dto.getGoTerm() != null) {
            nameBox.setText(dto.getGoTerm().getName());
        }
        if (dto.getEvidenceCode() != null) {
            evidenceCodeBox.setIndexForText(dto.getEvidenceCode().name());
        } else {
            evidenceCodeBox.setIndexForText(GoEvidenceCodeEnum.IMP.name());
        }


        evidenceFlagBox.clear();
        evidenceFlagBox.addItem("NONE", "null");
        // contributes to only for molecular function
        if (dto.getGoTerm() != null) {
            if (dto.getGoTerm().getOntology().equals(OntologyDTO.GO_MF)) {
                evidenceFlagBox.addItem(GoEvidenceQualifier.CONTRIBUTES_TO.toString());
            }
        }
        evidenceFlagBox.addItem(GoEvidenceQualifier.NOT.toString());

        if (dto.getFlag() == null) {
            evidenceFlagBox.setItemSelected(0, true);
        } else {
            evidenceFlagBox.setIndexForText(dto.getFlag().toString());
        }
        pubText.setText(dto.getPublicationZdbID());
        organizationHTML.setText(dto.getOrganizationSource());
        noteBox.setText(dto.getNote());
        inferenceListBox.setDTO(dto);
        if (dto.getGoTerm() != null) {
            goTermBox.setText(dto.getGoTerm().getName());
        }


        handleDirty();

        goTermBox.setText("");
        evidenceFlagBox.setSelectedIndex(0);
        termInfoComposite.clear();
    }

    @Override
    protected void addInternalListeners(final HandlesError handlesError) {
        super.addInternalListeners(handlesError);


        // TODO: maybe this should go into revertGUI
        revertButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                parent.hideNewGoRow();
            }
        });

        goTermButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                TermDTO termInfoDTO = termInfoComposite.getCurrentTermInfoDTO();
                if (termInfoDTO != null) {
                    LookupRPCService.App.getInstance().getTermByName(OntologyDTO.GO, termInfoDTO.getName(), new MarkerEditCallBack<TermDTO>("Failed to retrieve GO value", handlesError) {
                        @Override
                        public void onSuccess(TermDTO result) {
                            temporaryGoTermDTO = result;
                            GoEvidenceDTO goEvidenceDTO = dto.deepCopy();
                            goEvidenceDTO.setGoTerm(temporaryGoTermDTO);
                            fireGoTermChanged(new RelatedEntityEvent<GoEvidenceDTO>(goEvidenceDTO));
                            goTermBox.setText(result.getName());
                            handleDirty();
                        }
                    });
                } else {
                    setError("Term details box must be empty.");
                }
            }
        });


        goTermBox.setHighlightAction(new HighlightAction() {
            @Override
            public void onHighlight(String termID) {
                if (false == termID.startsWith(ItemSuggestCallback.END_ELLIPSIS)) {
                    LookupRPCService.App.getInstance().getTermInfo(OntologyDTO.GO, termID, new TermInfoCallBack(termInfoComposite, termID));
                }
            }
        });

        addGoTermChangeListeners(new RelatedEntityChangeListener<GoEvidenceDTO>() {
            @Override
            public void dataChanged(RelatedEntityEvent<GoEvidenceDTO> dataChangedEvent) {
                if (dataChangedEvent.getDTO().getGoTerm() != null) {
                    String termID = dataChangedEvent.getDTO().getGoTerm().getOboID();
                    LookupRPCService.App.getInstance().getTermInfo(OntologyDTO.GO, termID, new GoTermInfoCallBack(termInfoComposite, termID));
                }
            }
        });

        inferenceListBox.addGoTermChangeListeners(new RelatedEntityChangeListener<GoEvidenceDTO>() {
            public void dataChanged(RelatedEntityEvent<GoEvidenceDTO> dataChangedEvent) {
                fireGoTermChanged(dataChangedEvent);
            }
        });
        inferenceListBox.addHandlesErrorListener(this);
        inferenceListBox.addRelatedEnityChangeListener(new RelatedEntityChangeListener<GoEvidenceDTO>() {

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

        goTermBox.setAction(new SubmitAction() {
            private boolean isSubmitting = false;

            public void doSubmit(final String value) {
                if (isSubmitting) {
                    return;
                }
                isSubmitting = true;
                if (value.isEmpty()) {
                    setError("Go term is invalid [" + value + "].  Please add a valid go term.");
                    return;
                }
                goTermBox.setEnabled(false);
                goTermBox.setNoteString("Validating [" + value + "]...");

                LookupRPCService.App.getInstance().getTermByName(OntologyDTO.GO, value,
                        new MarkerEditCallBack<TermDTO>("Failed to retrieve GO value [" + value + "]", handlesError, false) {

                            @Override
                            public void onFailure(Throwable throwable) {
                                super.onFailure(throwable);
                                goTermBox.setEnabled(true);
                                goTermBox.clearNote();
                                isSubmitting = false;
                            }

                            @Override
                            public void onSuccess(TermDTO result) {
                                goTermBox.setEnabled(true);
                                goTermBox.clearNote();
                                if (result == null) {
                                    goTermBox.setErrorString("Unable to find term[" + value + "]");
                                    return;
                                }
                                temporaryGoTermDTO = result;
                                GoEvidenceDTO goEvidenceDTO = dto.deepCopy();
                                goEvidenceDTO.setGoTerm(temporaryGoTermDTO);
                                fireGoTermChanged(new RelatedEntityEvent<GoEvidenceDTO>(goEvidenceDTO));
                                handleDirty();
                                goTermBox.setText(temporaryGoTermDTO.getName());
                                clearError();
                                isSubmitting = false;
                            }
                        });
            }
        });
    }

    @Override
    protected void setValues() {
        if (dto == null) return;

        evidenceCodeBox.clear();
        if (dto.getPublicationZdbID() != null) {
            GoEvidenceCodeEnum[] goEvidenceCodeEnums = GoEvidenceCodeEnum.getCodeEnumForPub(dto.getPublicationZdbID());
            for (GoEvidenceCodeEnum evidenceCodeEnum : goEvidenceCodeEnums) {
                evidenceCodeBox.addItem(evidenceCodeEnum.name());
            }
        }
        if (dto.getEvidenceCode() == GoEvidenceCodeEnum.NAS) {
            evidenceCodeBox.addItem(dto.getEvidenceCode().name());
        }

        if (dto.getEvidenceCode() != null) {
            evidenceCodeBox.setIndexForText(dto.getEvidenceCode().name());
            inferenceListBox.setDTO(dto);
        }

        evidenceFlagBox.clear();
        evidenceFlagBox.addItem("NONE", "null");
        if (dto.getGoTerm() != null && dto.getGoTerm().getOntology().equals(OntologyDTO.GO_MF)) {
            evidenceFlagBox.addItem(GoEvidenceQualifier.CONTRIBUTES_TO.toString());
        }
        evidenceFlagBox.addItem(GoEvidenceQualifier.NOT.toString());
        if (dto.getFlag() != null) {
            evidenceFlagBox.setIndexForValue(dto.getFlag().toString());
        }


        if (dto.getGoTerm() != null) {
            LookupRPCService.App.getInstance().getTermInfo(OntologyDTO.GO, dto.getGoTerm().getZdbID(), new TermInfoCallBack(termInfoComposite, dto.getGoTerm().getName()));
        }
    }

    public GoEvidenceDTO createDTOFromGUI() {
        GoEvidenceDTO goEvidenceDTO;
        if (dto != null) {
            goEvidenceDTO = dto.deepCopy();
        } else {
            goEvidenceDTO = new GoEvidenceDTO();
        }
        goEvidenceDTO.setFlag(evidenceFlagBox.getItemCount() == 0 || evidenceFlagBox.getSelected() == null ? null : GoEvidenceQualifier.getType(evidenceFlagBox.getSelected()));
        goEvidenceDTO.setPublicationZdbID(pubText.getBoxValue());
        if (evidenceCodeBox.getItemCount() > 0) {
            goEvidenceDTO.setEvidenceCode(GoEvidenceCodeEnum.valueOf(evidenceCodeBox.getSelected()));
        }
        goEvidenceDTO.setNote(noteBox.getText());

        // these are only used on the client-side
        goEvidenceDTO.setModifiedDate(new Date());
        goEvidenceDTO.setCreatedDate(new Date());
        goEvidenceDTO.setGoTerm(temporaryGoTermDTO);

        if (inferenceListBox.createDTOFromGUI() != null) {
            goEvidenceDTO.setInferredFrom(inferenceListBox.createDTOFromGUI().getInferredFrom());
        }

        goEvidenceDTO.setPublicationZdbID(dto.getPublicationZdbID());
        return goEvidenceDTO;
    }


    @Override
    public boolean isDirty() {
        return true;
    }

    public int getTabIndex() {
        return tabIndex;
    }

    public void setPubVisible(boolean pubVisible) {
        pubPanel.setVisible(pubVisible);
    }


    @Override
    public void working() {
        super.working();
        evidenceCodeBox.setEnabled(false);
        pubText.setEnabled(false);
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
        if (event.isNotEmpty()) {
            super.onPublicationChanged(event);
            pubText.setText(publicationZdbID);
            dto.setPublicationZdbID(publicationZdbID);

            GoDefaultPublication goPubEnum = GoDefaultPublication.getPubForZdbID(publicationZdbID);
            if (goPubEnum != null) {
                switch (goPubEnum) {
                    case INTERPRO:
                    case SPKW:
                    case EC:
                        evidenceCodeBox.setIndexForText(GoEvidenceCodeEnum.IEA.name());
                        break;
                    case ISS_REF_GENOME:
                    case ISS_MANUAL_CURATED:
                        evidenceCodeBox.setIndexForText(GoEvidenceCodeEnum.ISS.name());
                        break;
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
        if (this.dto.getGoTerm() != null) {
            goTermBox.setText(this.dto.getGoTerm().getName());
        }
        temporaryGoTermDTO = this.dto.getGoTerm();
    }


    public void addGoTermChangeListeners(RelatedEntityChangeListener<GoEvidenceDTO> changeListener) {
        goTermChangeListeners.add(changeListener);
    }

    protected void fireGoTermChanged(RelatedEntityEvent<GoEvidenceDTO> relatedEntityDTO) {
        for (RelatedEntityChangeListener<GoEvidenceDTO> relatedEntityChangeListener : this.goTermChangeListeners) {
            relatedEntityChangeListener.dataChanged(relatedEntityDTO);
        }
    }

    protected class GoTermInfoCallBack extends TermInfoCallBack {
        public GoTermInfoCallBack(TermInfoComposite termInfoComposite, String termID) {
            super(termInfoComposite, termID);
        }

        @Override
        public void onSuccess(TermDTO result) {
            super.onSuccess(result);
            updateQualifiers(result);
        }

        private void updateQualifiers(TermDTO result) {
            if (result != null) {
                evidenceFlagBox.clear();
                evidenceFlagBox.addItem("NONE", "null");
                if (result.getOntology() == OntologyDTO.GO_MF) {
                    evidenceFlagBox.addItem(GoEvidenceQualifier.CONTRIBUTES_TO.toString());
                }
                // fogbugz 6292
                if (false == result.getOboID().equals(GoEvidenceValidator.PROTEIN_BINDING_OBO_ID)) {
                    evidenceFlagBox.addItem(GoEvidenceQualifier.NOT.toString());
                }
            }
        }
    }
}

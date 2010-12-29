package org.zfin.gwt.curation.ui;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.ui.*;
import org.zfin.gwt.root.util.LookupRPCService;

import java.util.List;

/**
 */
public abstract class AbstractFeatureBox extends AbstractComposite<FeatureDTO> implements Revertible{

    protected FlexFormatterTable table = new FlexFormatterTable() ;
    protected VerticalPanel panel = new VerticalPanel();
    protected HorizontalPanel namePanel = new HorizontalPanel();
    protected HorizontalPanel featureTypePanel = new HorizontalPanel();
    protected HorizontalPanel labDesignationTypePanel = new HorizontalPanel();
    protected HorizontalPanel protocolPanel = new HorizontalPanel();
    protected HorizontalPanel buttonPanel = new HorizontalPanel();
    protected final CheckBox dominantCheckBox = new CheckBox();
    protected final CheckBox knownInsertionCheckBox = new CheckBox();
    protected final StringTextBox featureDisplayName = new StringTextBox();
    protected final StringListBox featureTypeBox = new StringListBox(false);
    protected final StringTextBox featureNameBox = new StringTextBox();
    protected final StringTextBox featureAliasBox = new StringTextBox(); // used in add
    protected final FeatureAliasList featureAliasList = new FeatureAliasList(); // used only in edit
    protected final StringListBox labOfOriginBox = new StringListBox(false);
    protected final StringListBox labDesignationBox = new StringListBox(false);
    protected final StringTextBox lineNumberBox = new StringTextBox();
    protected final StringListBox mutageeBox = new StringListBox(false);
    protected final StringListBox featureSuffixBox = new StringListBox(false);
    protected final StringListBox mutagenBox = new StringListBox(false);

    // for add box only
//    protected final StringTextBox curatorNoteBox = new StringTextBox();
    protected final TextArea curatorNoteBox = new TextArea();
    //    protected final StringTextBox publicNoteBox = new StringTextBox();
    protected final TextArea publicNoteBox = new TextArea();

    // for edit box only
    protected final FeatureNoteBox featureNoteBox = new FeatureNoteBox();


    protected Widget suffixLabel = new Label("Type: ");


    protected final String TEXT_SAVE = "Create";
    protected Button saveButton = new Button(TEXT_SAVE);
    protected ConstructOracle oracle = new ConstructOracle();
    protected TextBox constructTextBox = new TextBox();
    protected SuggestBox constructSuggestBox;

    protected final String ZF_PREFIX = "zf" ;

    protected abstract void sendUpdates() ;

    protected void addInternalListeners(final HandlesError handlesError) {
        labOfOriginBox.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                updateLabsDesignations() ;
            }
        });


        labDesignationBox.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                handleDirty();
            }
        });
        mutageeBox.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                handleDirty();
            }
        });
        mutagenBox.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                handleDirty();
            }
        });
        featureSuffixBox.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                handleDirty();
            }

        });
        lineNumberBox.addKeyUpHandler(new KeyUpHandler() {
            public void onKeyUp(KeyUpEvent keyUpEvent) {
                handleDirty();
            }
        });
        featureNameBox.addKeyUpHandler(new KeyUpHandler() {
            public void onKeyUp(KeyUpEvent keyUpEvent) {
                handleDirty();
            }
        });
        featureNameBox.addChangeHandler(new ChangeHandler(){
            @Override
            public void onChange(ChangeEvent event) {
                handleDirty();
            }
        });
        constructSuggestBox.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {
            public void onSelection(SelectionEvent<SuggestOracle.Suggestion> selectionEvent) {
                handleDirty();
            }

        });

        dominantCheckBox.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent clickEvent) {
                handleDirty();
            }
        });


        knownInsertionCheckBox.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent clickEvent) {
                if (knownInsertionCheckBox.getValue()) {
                    featureNameBox.setVisible(true);
                    featureNameBox.setEnabled(false);
                    featureSuffixBox.setVisible(true);
                    suffixLabel.setVisible(true);
                    constructTextBox.setVisible(false);
                } else {
                    featureNameBox.setVisible(false);
                    constructTextBox.setVisible(true);
                    featureSuffixBox.setVisible(false);
                    suffixLabel.setVisible(false);
                }
                handleDirty();
            }
        });

        saveButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent clickEvent) {
                sendUpdates();
            }
        });

        errorLabel.addClickHandler(new ClickHandler(){
            @Override
            public void onClick(ClickEvent event) {
                clearError();
            }
        });
    }

    protected void updateLabsDesignations() {
        if(labOfOriginBox.isSelectedNull()) return ;

        final String labOfOriginSelected = labOfOriginBox.getSelectedText();
        FeatureRPCService.App.getInstance().getPrefix(labOfOriginSelected,
                new FeatureEditCallBack<List<FeaturePrefixDTO>>("Failed to load lab prefixes",this) {

                    @Override
                    public void onSuccess(List<FeaturePrefixDTO> labPrefixList) {
                        labDesignationBox.clear();
                        boolean hasZf = false ;
                        for(FeaturePrefixDTO featurePrefixDTO : labPrefixList){
                            if(hasZf || featurePrefixDTO.getPrefix().equals(ZF_PREFIX)){
                                hasZf = true ;
                            }
                            if(featurePrefixDTO.isActive()){
                                labDesignationBox.addItem(featurePrefixDTO.getPrefix() + " (current)",featurePrefixDTO.getPrefix());
                            }
                            else{
                                labDesignationBox.addItem(featurePrefixDTO.getPrefix());
                            }
                        }
                        // always has zf
                        if(!hasZf){
                            labDesignationBox.addItem(ZF_PREFIX);
                        }
                        labDesignationBox.setIndexForValue(dto.getLabPrefix());
                        handleDirty();
                        clearError();

                    }

                });

    }

    protected void updateForFeatureType() {
        final FeatureTypeEnum featureTypeSelected = FeatureTypeEnum.getTypeForDisplay(featureTypeBox.getSelectedText());
        if(featureTypeSelected==null){
            saveButton.setEnabled(false);
            return ;
        }

        publicNoteBox.setEnabled(true);
        curatorNoteBox.setEnabled(true);
        featureNoteBox.notWorking();
        lineNumberBox.setEnabled(true);
        knownInsertionCheckBox.setEnabled(false);
        dominantCheckBox.setEnabled(true);
        labOfOriginBox.setEnabled(true);
        labDesignationBox.setEnabled(true);
        mutageeBox.setEnabled(true);
        mutagenBox.setEnabled(true);
        suffixLabel.setVisible(false);
        featureSuffixBox.setVisible(false);
        featureAliasBox.setEnabled(true);

        featureNameBox.setVisible(true);
        featureNameBox.setEnabled(false);
        constructTextBox.setVisible(false);

        switch (featureTypeSelected){
            case TRANSGENIC_INSERTION:
                knownInsertionCheckBox.setEnabled(true);
                if (knownInsertionCheckBox.getValue()) {
                    featureSuffixBox.setVisible(true);
                    suffixLabel.setVisible(true);
                }
                else{
                    featureNameBox.setVisible(false);
                    constructTextBox.setVisible(true);
                }
                break ;
            case POINT_MUTATION:
            case DELETION:
            case SEQUENCE_VARIANT:
            case INSERTION:
                // just uses the defaults
                featureNameBox.setText("");
                break ;
            case INVERSION:
            case TRANSLOC:
            case DEFICIENCY:
            case COMPLEX_SUBSTITUTION:
                // enable feature names
                featureNameBox.setEnabled(true);
                break ;
            case UNSPECIFIED:
                dominantCheckBox.setEnabled(false);
                labDesignationBox.setEnabled(false);
                lineNumberBox.setEnabled(false);
                featureAliasBox.setEnabled(false);
                labOfOriginBox.setEnabled(false);
                mutageeBox.setEnabled(false);
                mutagenBox.setEnabled(false);
                constructTextBox.setVisible(false);
                lineNumberBox.setText("");
                featureNameBox.setVisible(true);
                featureNameBox.setEnabled(true);
                break ;
            case TRANSGENIC_UNSPECIFIED:
                featureNameBox.setVisible(false);
                constructTextBox.setVisible(true);
                dominantCheckBox.setEnabled(false);
                labDesignationBox.setEnabled(false);
                lineNumberBox.setEnabled(false);
                featureAliasBox.setEnabled(false);
                labOfOriginBox.setEnabled(false);
                mutageeBox.setEnabled(false);
                mutagenBox.setEnabled(false);
                lineNumberBox.setText("");
                break ;
        }
        handleDirty();

    }




    public FeatureDTO createDTOFromGUI() {

        FeatureDTO featureDTO = new FeatureDTO();

        featureDTO.setName(featureDisplayName.getText());

        if(featureNameBox.isVisible()){
            featureDTO.setOptionalName(featureNameBox.getText());
        }
        else
        if(constructTextBox.isVisible()){
            featureDTO.setOptionalName(constructSuggestBox.getText());
        }


        FeatureTypeEnum featureTypeEnum = FeatureTypeEnum.getTypeForName(featureTypeBox.getSelected()) ;
        if(featureTypeEnum!=null){
            featureDTO.setFeatureType(featureTypeEnum);
            if(!featureTypeEnum.isUnspecified()){
                featureDTO.setLineNumber(lineNumberBox.getText());
                featureDTO.setLabPrefix(labDesignationBox.getSelected());
                featureDTO.setLabOfOrigin(labOfOriginBox.getSelected());
            }
        }
        featureDTO.setMutagen(mutagenBox.getSelected());
        featureDTO.setMutagee(mutageeBox.getSelected());

        featureDTO.setDominant(dominantCheckBox.getValue());
        featureDTO.setKnownInsertionSite(knownInsertionCheckBox.getValue());

        featureDTO.setPublicationZdbID(dto.getPublicationZdbID());
        if (knownInsertionCheckBox.getValue()){
            featureDTO.setTransgenicSuffix(featureSuffixBox.getSelectedText());
        }

        // we set the name right at the end, once everything has been set
        // this is the wrong place to do this check
//        if(false==featureDTO.getName().equals(FeatureValidationService.generateFeatureDisplayName(featureDTO))){
//            throw new RuntimeException("Display ["+featureDTO.getName()+
//                    "] and generated names ["+FeatureValidationService.generateFeatureDisplayName(featureDTO)+"]are not equal.") ;
//        }
        featureDTO.setAbbreviation(FeatureValidationService.getAbbreviationFromName(featureDTO));

        return featureDTO;
    }

    public void initGUI() {

        featureTypeBox.clear();
        featureTypeBox.addNull();
        for(FeatureTypeEnum featureTypeEnum : FeatureTypeEnum.values()){
            featureTypeBox.addItem(featureTypeEnum.getDisplay(),featureTypeEnum.name());
        }
        table.setHTML(FeatureTableLayout.TYPE.row(), 0, "<b>Feature type:</b>");

        featureTypePanel.add(featureTypeBox);
        featureTypePanel.add(new HTML("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"));
        featureTypePanel.add(new HTML("<b>Known Insertion Site:</b>"));
        featureTypePanel.add(knownInsertionCheckBox);


        suffixLabel.setStyleName("bold");
        featureTypePanel.add(suffixLabel);
        featureTypePanel.add(featureSuffixBox);

        table.setWidget(FeatureTableLayout.TYPE.row(), 1, featureTypePanel);



        table.setHTML(FeatureTableLayout.LAB_OF_ORIGIN.row(), 0, "<b>Lab Of Origin:</b>");
        table.setWidget(FeatureTableLayout.LAB_OF_ORIGIN.row(), 1, labOfOriginBox);


        table.setHTML(FeatureTableLayout.LAB_DESIGNATION.row(), 0, "<b>Lab designation:</b>");
        table.setWidget(FeatureTableLayout.LAB_DESIGNATION.row(), 1, labDesignationTypePanel);

        labDesignationTypePanel.add(labDesignationBox);
        labDesignationTypePanel.add(new HTML("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"));
        labDesignationTypePanel.add(new HTML("<b>Line Number:</b>"));
        labDesignationTypePanel.add(lineNumberBox);
        labDesignationTypePanel.add(new HTML("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"));
        labDesignationTypePanel.add(new HTML("<b>Dominant:</b>"));
        labDesignationTypePanel.add(dominantCheckBox);

        table.setHTML(FeatureTableLayout.NAME.row(), 0, "<b>Feature Name:</b>");
        constructSuggestBox = new SuggestBox(oracle, constructTextBox);
        namePanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        namePanel.add(featureNameBox);
        namePanel.add(constructSuggestBox);
        table.setWidget(FeatureTableLayout.NAME.row(), 1, namePanel);

        table.setHTML(FeatureTableLayout.ALIAS.row(), 0, "<b>Alias:</b>");
        table.setWidget(FeatureTableLayout.ALIAS.row(), 1, featureAliasBox);

        table.setHTML(FeatureTableLayout.PROTOCOL.row(), 0, "<b>Mutagenesis Protocol: </b>");
        protocolPanel.add(mutageeBox);
        protocolPanel.add(new HTML("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"));
        protocolPanel.add(new HTML("treated with: "));
        protocolPanel.add(mutagenBox);
        table.setWidget(FeatureTableLayout.PROTOCOL.row(),1,protocolPanel);

        table.setHTML(FeatureTableLayout.NAME_DISPLAY.row(), 0, "<b>Feature Display Name:</b>");
        featureDisplayName.setWidth("500px");
        table.setWidget(FeatureTableLayout.NAME_DISPLAY.row(), 1, featureDisplayName);


        featureDisplayName.setEnabled(false);
        knownInsertionCheckBox.setEnabled(false);
        suffixLabel.setVisible(false);
        featureSuffixBox.setVisible(false);

        constructTextBox.setVisible(false);
        constructSuggestBox.setVisible(false);
        panel.add(table);
        saveButton.setEnabled(false);
        buttonPanel.add(saveButton);
        panel.add(buttonPanel);

        panel.setStyleName("gwt-editbox");
        errorLabel.setStyleName("error");
        panel.add(errorLabel) ;
    }


    protected void setValues() {

        mutagenBox.addEnumValues(Mutagen.values());
        mutageeBox.addEnumValues(Mutagee.values());
        featureSuffixBox.addEnumValues(TransgenicSuffix.values());

        FeatureRPCService.App.getInstance().getLabsOfOriginWithPrefix(new FeatureEditCallBack<List<LabDTO>>("Failed to load labs",this) {
            public void onSuccess(List<LabDTO> list) {
                labOfOriginBox.clear();
                labOfOriginBox.addNull();
                for(LabDTO labDTO : list){
                    labOfOriginBox.addItem(labDTO.getName(),labDTO.getZdbID());
                }
            }
        });

    }

    protected class ConstructOracle extends LookupOracle {
        @Override
        public void doLookup(Request request, Callback callback) {
            LookupRPCService.App.getInstance().getConstructSuggestions(request,dto.getPublicationZdbID(),
                    new LookupCallback(request, callback));
        }
    }

    protected enum FeatureTableLayout{

        TITLE(0) ,
        TYPE(1) ,
        LAB_OF_ORIGIN(2) ,
        LAB_DESIGNATION(3) ,
        NAME(4) ,
        ALIAS(5) ,
        PROTOCOL(6) ,
        NOTE(7) ,
        NAME_DISPLAY(8) ,
        ;


        private int row ;

        FeatureTableLayout(int r){
            row = r;
        }

        public int row() {
            return row;
        }
    }

    public void resetInterface(){
        labOfOriginBox.setEnabled(false);
        labOfOriginBox.setSelectedIndex(0);
        labDesignationBox.setEnabled(false);
        labDesignationBox.clear();
        labDesignationBox.setSelectedIndex(0);
        lineNumberBox.setEnabled(false);
        lineNumberBox.clear();
        dominantCheckBox.setEnabled(false);
        dominantCheckBox.setValue(false);
        featureNameBox.setEnabled(false);
        featureNameBox.clear() ;
        constructTextBox.setText("");
        featureAliasBox.setEnabled(false);
        featureAliasBox.clear();
        featureAliasList.working();
        featureAliasList.resetInput();
        mutageeBox.setEnabled(false);
        mutageeBox.setSelectedIndex(0);
        mutagenBox.setEnabled(false);
        mutagenBox.setSelectedIndex(0);
        publicNoteBox.setEnabled(false);
        publicNoteBox.setText("");
        curatorNoteBox.setEnabled(false);
        curatorNoteBox.setText("");
        featureNoteBox.revertGUI();
        knownInsertionCheckBox.setEnabled(false);
        knownInsertionCheckBox.setValue(false);
        featureDisplayName.clear();
    }

    @Override
    public void setError(String message) {
        errorLabel.setStyleName("clickable-error");
        super.setError(message + "[close]");
    }

    public void setNote(String message) {
        errorLabel.setStyleName("clickable");
        super.setError(message + "[close]");
    }

    public void setPublication(String publicationZdbId) {
        this.publicationZdbID = publicationZdbId;
        FeatureDTO featureDTO = new FeatureDTO();
        featureDTO.setPublicationZdbID(publicationZdbId);
        setDTO(featureDTO);
    }

    @Override
    public void working() {
        saveButton.setText(TEXT_WORKING);
        saveButton.setEnabled(false);
        featureTypeBox.setEnabled(false);
        labOfOriginBox.setEnabled(false);
        labDesignationBox.setEnabled(false);
        lineNumberBox.setEnabled(false);
        dominantCheckBox.setEnabled(false);
        featureNameBox.setEnabled(false);
        featureAliasBox.setEnabled(false);
        mutageeBox.setEnabled(false);
        mutagenBox.setEnabled(false);
        publicNoteBox.setEnabled(false);
        curatorNoteBox.setEnabled(false);
        knownInsertionCheckBox.setEnabled(false);
    }

    @Override
    public void notWorking() {
        saveButton.setText(TEXT_SAVE);
        saveButton.setEnabled(true);
        featureTypeBox.setEnabled(true);
        updateForFeatureType();
    }

}

package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.root.dto.FeatureDTO;
import org.zfin.gwt.root.dto.FishDTO;
import org.zfin.gwt.root.dto.GenotypeDTO;
import org.zfin.gwt.root.dto.RelatedEntityDTO;
import org.zfin.gwt.root.ui.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Entry point for FX curation module.
 */
public class FishModule implements HandlesError, EntryPoint {

    public static final String FISH_TAB = "fishTab";
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiTemplate("FishModule.ui.xml")
    interface MyUiBinder extends UiBinder<FlowPanel, FishModule> {
    }

    // data
    private String publicationID;

    private AttributionModule attributionModule;

    // listener
    private List<HandlesError> handlesErrorListeners = new ArrayList<>();

    ListBox genotypeSelectionBox = new ListBox();

    ListBox strSelectionBox = new ListBox();

    Button addStrButton = new Button("Add STR");
    ListBox featureListBox = new ListBox();
    ListBox backgroundListBox = new ListBox();
    Button searchExistingGenotypes = new Button("Search");

    @UiField
    Button createFishButton;

    @UiField
    SimpleErrorElement errorLabelSearch;

    @UiField
    SimpleErrorElement errorLabel;

    @UiField
    Image loadingImage;

    @UiField
    Label noneDefined;

    @UiField
    ZfinFlexTable constructionTable;

    @UiField
    ZfinFlexTable fishListTable;

    @UiField
    ZfinFlexTable genotypeListTable;

    @UiField
    ZfinFlexTable genotypeSearchResultTable;

    @UiField
    Anchor showHideExistingGeno;
    @UiField
    Anchor showHideFishConstruction;

    public FishModule(String publicationID) {
        this.publicationID = publicationID;
        onModuleLoad();
    }

    // only few STR will be added
    private List<RelatedEntityDTO> newStrList = new ArrayList<>(4);
    private GenotypeDTO newGenotype;
    private List<FishDTO> fishList = new ArrayList<>(10);
    private List<GenotypeDTO> genotypeList = new ArrayList<>(10);
    private List<GenotypeDTO> existingGenotypeList = new ArrayList<>(10);

    private boolean showExistingGenoBool = false;
    private boolean showFishConstruction = false;

    @Override
    public void onModuleLoad() {

        FlowPanel outer = uiBinder.createAndBindUi(this);
        RootPanel.get(FISH_TAB).add(outer);
        errorLabel.setStyleName("error");
        genotypeListCallBack = new RetrieveDTOListCallBack<>(genotypeSelectionBox, "Genotypes", null);
        strListCallBack = new RetrieveRelatedEntityListCallBack(strSelectionBox, "STRs", null);
        attributionModule = new AttributionModule();
        RelatedEntityDTO relatedEntityDTO = new RelatedEntityDTO();
        relatedEntityDTO.setPublicationZdbID(publicationID);
        attributionModule.setDTO(relatedEntityDTO);
        addHandlers();
        initConstructionTableHeader();
        initConstructionRow();
        initFishListTable();
        initConstructionGenotypeSearchResultRow(0);
        retrieveAllValues();
        genotypeSearchResultTable.setVisible(showExistingGenoBool);
        constructionTable.setVisible(showFishConstruction);
        createFishButton.setVisible(showFishConstruction);

        // Add the widgets to the root panel.
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                updateConstructionTable();
            }
        });

    }

    private class PublicNotePopup extends PopupPanel {

        private GenotypeDTO genotypeDTO;

        public PublicNotePopup(final GenotypeDTO genotypeDTO) {
            // set auto hide to true
            super(true);
            this.genotypeDTO = genotypeDTO;
            VerticalPanel vPanel = new VerticalPanel();
            final TextArea textArea = new TextArea();
            textArea.setHeight("100px");
            textArea.setWidth("250px");
            textArea.setText(genotypeDTO.getPublicNote());
            vPanel.add(textArea);
            Button save = new Button("Save");
            save.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent clickEvent) {
                    genotypeDTO.setPublicNote(textArea.getText());
                    diseaseCurationRPCAsync.savePublicNote(publicationID, genotypeDTO, new ZfinAsyncCallback<GenotypeDTO>("public note ", errorLabelSearch) {
                        @Override
                        public void onSuccess(GenotypeDTO genotypeDTO) {
                            publicNoteAnchor.get(genotypeDTO).setText(genotypeDTO.getPublicNote());
                        }
                    });
                    hide();
                }
            });
            vPanel.add(save);
            setWidget(vPanel);
        }
    }

    private class CuratorNotePopup extends PopupPanel {

        private GenotypeDTO genotypeDTO;

        public CuratorNotePopup(final GenotypeDTO genotypeDTO) {
            // set auto hide to true
            super(true);
            this.genotypeDTO = genotypeDTO;
            VerticalPanel vPanel = new VerticalPanel();
            final TextArea textArea = new TextArea();
            textArea.setHeight("100px");
            textArea.setWidth("250px");
            textArea.setText(genotypeDTO.getPrivateNote());
            vPanel.add(textArea);
            Button save = new Button("Save");
            save.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent clickEvent) {
                    genotypeDTO.setPublicNote(textArea.getText());
                    diseaseCurationRPCAsync.savePublicNote(publicationID, genotypeDTO, new ZfinAsyncCallback<GenotypeDTO>("public note ", errorLabelSearch) {
                        @Override
                        public void onSuccess(GenotypeDTO genotypeDTO) {
                            publicNoteAnchor.get(genotypeDTO).setText(genotypeDTO.getPublicNote());
                        }
                    });
                    hide();
                }
            });
            vPanel.add(save);
            setWidget(vPanel);
        }
    }

    private void addHandlers() {
        genotypeSelectionBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent changeEvent) {
                int index = genotypeSelectionBox.getSelectedIndex();
                //Window.alert(""+index);
                List<GenotypeDTO> dtoList = genotypeListCallBack.getDtoList();
                newGenotype = dtoList.get(index);
                updateConstructionTable();
                unsetErrorMessage();
            }
        });
        strSelectionBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent changeEvent) {
                unsetErrorMessage();
            }
        });
        addStrButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                int index = strSelectionBox.getSelectedIndex();
                List<RelatedEntityDTO> dtoList = strListCallBack.getDtoList();
                RelatedEntityDTO str = dtoList.get(index);
                if (!newStrList.contains(str)) {
                    newStrList.add(str);
                    updateConstructionTable();
                    unsetErrorMessage();
                } else {
                    errorLabel.setText("STR already added");
                }
            }
        });
        createFishButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                FishDTO newFish = getNewFish();
                if (validate(newFish)) {
                    diseaseCurationRPCAsync.createFish(publicationID, newFish, new RetrieveFishListCallBack("error", errorLabel));
                    showLoadingImage(true);
                } else {
                    errorLabel.setText("Fish already exists");
                }
            }
        });
        searchExistingGenotypes.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                searchForGenotypes();
            }
        });
        attributionModule.addHandlesErrorListener(new HandlesError() {
            @Override
            public void setError(String message) {
            }

            @Override
            public void clearError() {
                fireEventSuccess();
            }

            @Override
            public void fireEventSuccess() {
                diseaseCurationRPCAsync.getStrList(publicationID, strListCallBack);
                diseaseCurationRPCAsync.getFishList(publicationID, new RetrieveFishListCallBack("Fish List", errorLabel));
                diseaseCurationRPCAsync.getGenotypeList(publicationID, new RetrieveGenotypeListCallBack("Genotype List", errorLabelSearch));
            }

            @Override
            public void addHandlesErrorListener(HandlesError handlesError) {
            }
        });
        showHideExistingGeno.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                toggleVisibilityExistingGenoTable();
            }
        });
        showHideFishConstruction.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                toggleVisibilityFishConstruction();
            }
        });
        featureListBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent changeEvent) {
                searchForGenotypes();
            }
        });
    }

    private void searchForGenotypes() {
        String featureID = getSelectedFeatureID();
        String genotypeID = getSelectedGenotypeID();
        diseaseCurationRPCAsync.searchGenotypes(publicationID, featureID, genotypeID, new RetrieveExistingGenotypeListCallBack("error", errorLabelSearch));
    }

    private void toggleVisibilityFishConstruction() {
        if (showFishConstruction) {
            showFishConstruction = false;
            showHideFishConstruction.setText("Show");
        } else {
            showFishConstruction = true;
            showHideFishConstruction.setText("Hide");
        }
        constructionTable.setVisible(showFishConstruction);
        createFishButton.setVisible(showFishConstruction);
    }

    private void toggleVisibilityExistingGenoTable() {
        if (showExistingGenoBool) {
            showExistingGenoBool = false;
            showHideExistingGeno.setText("Show");
        } else {
            showExistingGenoBool = true;
            showHideExistingGeno.setText("Hide");
        }
        genotypeSearchResultTable.setVisible(showExistingGenoBool);
    }

    private String getSelectedFeatureID() {
        String id = featureListBox.getSelectedValue();
        return id;
    }

    private String getSelectedGenotypeID() {
        String id = backgroundListBox.getSelectedValue();
        return id;
    }

    private void showLoadingImage(boolean showLoadingImage) {
        loadingImage.setVisible(showLoadingImage);
    }

    private boolean validate(FishDTO newFish) {
        // check if the fish already exists
        if (fishList != null && fishList.size() > 0)
            return !fishList.contains(newFish);
        return true;
    }

    private FishDTO getNewFish() {
        FishDTO dto = new FishDTO();
        dto.setGenotypeDTO(newGenotype);
        if (newStrList != null)
            dto.setStrList(newStrList);
        return dto;
    }

    private void resetUI() {
        newGenotype = null;
        newStrList.clear();
        genotypeSelectionBox.setSelectedIndex(0);
        strSelectionBox.setSelectedIndex(0);
        removeConstructionRow();
        unsetErrorMessage();
    }


    private void unsetErrorMessage() {
        errorLabel.setText("");
    }

    private Widget getStrPanel() {
        HorizontalPanel panel = new HorizontalPanel();
        panel.setSpacing(5);
        if (newStrList.size() > 0) {
            int index = 0;
            for (final RelatedEntityDTO str : newStrList) {
                panel.add(new InlineHTML(str.getName()));
                Anchor removeLink = new Anchor(" (X)");
                removeLink.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent clickEvent) {
                        newStrList.remove(str);
                        updateConstructionTable();
                    }
                });
                panel.add(removeLink);
                if (index < newStrList.size() - 1)
                    panel.add(new InlineHTML(" + "));
                index++;
            }
        }
        return panel;
    }

    private CurationDiseaseRPCAsync diseaseCurationRPCAsync = CurationDiseaseRPC.App.getInstance();
    private CurationExperimentRPCAsync curationExperimentRPCAsync = CurationExperimentRPC.App.getInstance();
    private RetrieveDTOListCallBack<GenotypeDTO> genotypeListCallBack;
    private RetrieveRelatedEntityListCallBack strListCallBack;

    private void retrieveAllValues() {
        // get genotype list
        curationExperimentRPCAsync.getGenotypes(publicationID, genotypeListCallBack);

        // get wildtype background list
        RetrieveRelatedEntityListCallBack retrieveBackgroundCallback = new RetrieveRelatedEntityListCallBack(backgroundListBox, "Baclbground List", errorLabel);
        retrieveBackgroundCallback.setLeaveFirstEntryBlank(true);
        curationExperimentRPCAsync.getBackgroundGenotypes(publicationID, retrieveBackgroundCallback);

        // get STR list
        diseaseCurationRPCAsync.getStrList(publicationID, strListCallBack);

        // get Fish List
        diseaseCurationRPCAsync.getFishList(publicationID, new RetrieveFishListCallBack("Fish List", errorLabel));

        // get Fish List
        diseaseCurationRPCAsync.getGenotypeList(publicationID, new RetrieveGenotypeListCallBack("Genotype List", errorLabel));

        // get Feature List
        diseaseCurationRPCAsync.getFeatureList(publicationID, new RetrieveRelatedEntityListCallBack(featureListBox, "Feature List", errorLabel));

    }

    private void initConstructionTableHeader() {
        constructionTable.setText(0, 0, "Genotype");
        constructionTable.getCellFormatter().setStyleName(0, 0, "bold");
        constructionTable.setText(0, 1, "ST Reagent");
        constructionTable.getCellFormatter().setStyleName(0, 1, "bold");
        constructionTable.getRowFormatter().setStyleName(0, "table-header");
    }

    private void initConstructionGenotypeSearchResultRow(int row) {
        int col = 0;
        HorizontalPanel search = new HorizontalPanel();
        search.add(searchExistingGenotypes);
        genotypeSearchResultTable.setWidget(row, col, search);
        HorizontalPanel panel = new HorizontalPanel();
        InlineHTML featureHtml = new InlineHTML("Feature: ");
        featureHtml.setStyleName("bold");
        panel.add(featureHtml);
        panel.add(featureListBox);
        InlineHTML backgroundHtml = new InlineHTML("Background: ");
        backgroundHtml.setStyleName("bold");
        panel.add(backgroundHtml);
        panel.add(backgroundListBox);
        genotypeSearchResultTable.getCellFormatter().setStyleName(row, col++, "bold");
        genotypeSearchResultTable.setWidget(row, col, panel);
        genotypeSearchResultTable.getCellFormatter().setStyleName(row, col, "bold");
        genotypeSearchResultTable.getFlexCellFormatter().setColSpan(row, col, 2);
        genotypeSearchResultTable.getRowFormatter().setStyleName(row, "table-header");
    }

    private void updateConstructionTable() {
        if (newGenotype == null) {
            if (genotypeListCallBack != null) {
                if (genotypeListCallBack.getDtoList() != null)
                    newGenotype = genotypeListCallBack.getDtoList().get(0);
                else return;
            } else
                return;
        }
        HorizontalPanel genoPanel = new HorizontalPanel();
        genoPanel.add(new InlineHTML(newGenotype.getName()));
        //genoPanel.setSpacing(5);
        constructionTable.setWidget(1, 0, genoPanel);
        if (newStrList != null) {
            constructionTable.setWidget(1, 1, getStrPanel());
        }
        constructionTable.setRowStyle(1, null, newGenotype.getZdbID(), 0);
    }

    private void removeConstructionRow() {
        updateConstructionTable();
    }


    private void initConstructionRow() {
        HorizontalPanel genoPanel = new HorizontalPanel();
        genoPanel.add(genotypeSelectionBox);
        constructionTable.setWidget(2, 0, genoPanel);
        HorizontalPanel panelStr = new HorizontalPanel();
        panelStr.add(strSelectionBox);
        panelStr.add(addStrButton);
        constructionTable.setWidget(2, 1, panelStr);
        constructionTable.getRowFormatter().setStyleName(2, "table-header");
    }

    private void initFishListTable() {
        int col = 0;
        fishListTable.getCellFormatter().setStyleName(0, col, "bold");
        fishListTable.setText(0, col++, "Fish Name");
        fishListTable.getCellFormatter().setStyleName(0, col, "bold");
        fishListTable.setText(0, col++, "Display Handle");
        fishListTable.getCellFormatter().setStyleName(0, col, "bold");
        fishListTable.setText(0, col++, "Delete");
        fishListTable.getRowFormatter().setStyleName(0, "table-header");
    }

    private void initGenotypeSearchResultTable() {
        int col = 0;
        genotypeSearchResultTable.getCellFormatter().setStyleName(0, col, "bold");
        genotypeSearchResultTable.setText(0, col++, "Select");
        genotypeSearchResultTable.getCellFormatter().setStyleName(0, col, "bold");
        genotypeSearchResultTable.setText(0, col++, "Display Handle");
        genotypeSearchResultTable.getCellFormatter().setStyleName(0, col, "bold");
        genotypeSearchResultTable.setText(0, col++, "Nickname");
        genotypeSearchResultTable.getRowFormatter().setStyleName(0, "table-header");
    }

    private void initGenotypeListTable() {
        int col = 0;
        genotypeListTable.getCellFormatter().setStyleName(0, col, "bold");
        genotypeListTable.setText(0, col++, "Display Name");
        genotypeListTable.getCellFormatter().setStyleName(0, col, "bold");
        genotypeListTable.setText(0, col++, "Genotype Nickname");
        genotypeListTable.getCellFormatter().setStyleName(0, col, "bold");
        genotypeListTable.setText(0, col++, "Feature");
        genotypeListTable.getCellFormatter().setStyleName(0, col, "bold");
        genotypeListTable.setText(0, col++, "Public Note");
        genotypeListTable.getCellFormatter().setStyleName(0, col, "bold");
        genotypeListTable.setText(0, col++, "Curator Note");
        genotypeListTable.getCellFormatter().setStyleName(0, col, "bold");
        genotypeListTable.setText(0, col++, "Delete");
        genotypeListTable.getRowFormatter().setStyleName(0, "table-header");
    }

    public void updateFishListTableContent(List<FishDTO> fishList) {
        fishListTable.removeAllRows();
        if (fishList == null || fishList.size() == 0) {
            fishListTable.setVisible(false);
            return;
        }

        fishListTable.setVisible(true);
        initFishListTable();
        int index = 1;

        int groupIndex = 0;
        int rowIndex = 1;
        for (FishDTO fish : fishList) {
            int col = 0;
            Anchor html = new Anchor(SafeHtmlUtils.fromTrustedString(fish.getName()), "/" + fish.getZdbID());
            fishListTable.setWidget(index, col++, html);
            InlineHTML handle = new InlineHTML(fish.getHandle());
            handle.setTitle(fish.getZdbID());
            fishListTable.setWidget(index, col++, handle);
            Anchor anchor = new Anchor("X", "/action/infrastructure/deleteRecord/" + fish.getZdbID());
            fishListTable.getCellFormatter().setHorizontalAlignment(index, col, HasHorizontalAlignment.ALIGN_CENTER);
            fishListTable.setWidget(index++, col++, anchor);
            groupIndex = fishListTable.setRowStyle(rowIndex++, null, fish.getZdbID(), groupIndex);

        }
    }

    public void updateFishGenotypeListTableContent(List<GenotypeDTO> genotypeDTOList) {
        genotypeListTable.removeAllRows();
        if (genotypeDTOList == null || genotypeDTOList.size() == 0) {
            genotypeListTable.setVisible(false);
            return;
        }

        genotypeListTable.setVisible(true);
        initGenotypeListTable();
        int index = 1;

        int groupIndex = 0;
        int rowIndex = 1;
        for (final GenotypeDTO genotype : genotypeDTOList) {
            int col = 0;
            Anchor html = new Anchor(SafeHtmlUtils.fromTrustedString(genotype.getName()), "/" + genotype.getZdbID());
            genotypeListTable.setWidget(index, col++, html);
            InlineHTML handle = new InlineHTML(genotype.getHandle());
            handle.setTitle(genotype.getZdbID());
            genotypeListTable.setWidget(index, col++, handle);
            VerticalPanel featurePanel = new VerticalPanel();
            if (genotype.getFeatureList() != null && genotype.getFeatureList().size() > 0)
                for (FeatureDTO featureDTO : genotype.getFeatureList())
                    featurePanel.add(new InlineHTML(featureDTO.getAbbreviation()));
            genotypeListTable.setWidget(index, col++, featurePanel);

            Anchor publicNote = new Anchor("Add");
            if (genotype.getPublicNote() != null)
                publicNote = new Anchor(genotype.getPublicNote());
            publicNote.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent clickEvent) {
                    PublicNotePopup publicNotePopup = new PublicNotePopup(genotype);
                    publicNotePopup.show();
                    publicNotePopup.center();
                }
            });
            publicNoteAnchor.put(genotype, publicNote);
            genotypeListTable.setWidget(index, col++, publicNote);
            genotypeListTable.setWidget(index, col++, new InlineHTML());
            Anchor anchor = new Anchor("X", "/action/infrastructure/deleteRecord/" + genotype.getZdbID());
            genotypeListTable.getCellFormatter().setHorizontalAlignment(index, col, HasHorizontalAlignment.ALIGN_CENTER);
            genotypeListTable.setWidget(index++, col++, anchor);
            groupIndex = genotypeListTable.setRowStyle(rowIndex++, null, genotype.getZdbID(), groupIndex);

        }
    }

    private Map<GenotypeDTO, Anchor> publicNoteAnchor = new HashMap<>();

    public void updateExistingGenotypeListTableContent(List<GenotypeDTO> genotypeDTOList) {
        genotypeSearchResultTable.removeAllRows();
        if (genotypeDTOList == null || genotypeDTOList.size() == 0) {
            genotypeSearchResultTable.setVisible(false);
            return;
        }

        genotypeSearchResultTable.setVisible(true);
        initGenotypeSearchResultTable();

        int groupIndex = 0;
        int rowIndex = 1;
        for (final GenotypeDTO genotype : genotypeDTOList) {
            int col = 0;
            CheckBox checkBox = new CheckBox();
            checkBox.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent clickEvent) {
                    diseaseCurationRPCAsync.addGenotypeToPublication(publicationID, genotype.getZdbID(),
                            new RetrieveGenotypeListCallBack("error add existing genotype", errorLabelSearch, true));
                }
            });
            genotypeSearchResultTable.setWidget(rowIndex, col++, checkBox);
            InlineHTML genoName = new InlineHTML(genotype.getName());
            genotypeSearchResultTable.setWidget(rowIndex, col++, genoName);
            InlineHTML geno = new InlineHTML(genotype.getHandle());
            geno.setTitle(genotype.getZdbID());
            genotypeSearchResultTable.setWidget(rowIndex, col++, geno);
            groupIndex = genotypeSearchResultTable.setRowStyle(rowIndex++, null, genotype.getZdbID(), groupIndex);
        }
        initConstructionGenotypeSearchResultRow(rowIndex);
    }


    @Override
    public void setError(String message) {
    }

    @Override
    public void clearError() {
        attributionModule.clearError();
        revertGUI();
    }

    private void revertGUI() {
        attributionModule.revertGUI();
    }

    @Override
    public void fireEventSuccess() {
        for (HandlesError handlesError : handlesErrorListeners) {
            handlesError.clearError();
        }
    }

    @Override
    public void addHandlesErrorListener(HandlesError handlesError) {
        handlesErrorListeners.add(handlesError);
    }

    class RetrieveFishListCallBack extends ZfinAsyncCallback<List<FishDTO>> {


        public RetrieveFishListCallBack(String errorMessage, ErrorHandler errorLabel) {
            super(errorMessage, errorLabel, loadingImage);
        }

        @Override
        public void onSuccess(List<FishDTO> list) {
            fishList = list;
            resetUI();
            updateFishListTableContent(list);
            loadingImage.setVisible(false);
            if (fishList != null && fishList.size() > 0)
                noneDefined.setVisible(false);
            attributionModule.populateAttributeRemoval();
        }
    }

    class RetrieveGenotypeListCallBack extends ZfinAsyncCallback<List<GenotypeDTO>> {

        private boolean updateExistingGenotypeList;

        public RetrieveGenotypeListCallBack(String errorMessage, ErrorHandler errorLabel, boolean updateExistingGenotypeList) {
            super(errorMessage, errorLabel);
            this.updateExistingGenotypeList = updateExistingGenotypeList;
        }

        public RetrieveGenotypeListCallBack(String errorMessage, ErrorHandler errorLabel) {
            super(errorMessage, errorLabel, loadingImage);
        }

        @Override
        public void onSuccess(List<GenotypeDTO> list) {
            genotypeList = list;
            resetUI();
            updateFishGenotypeListTableContent(list);
            loadingImage.setVisible(false);
            if (fishList != null && fishList.size() > 0)
                noneDefined.setVisible(false);
            attributionModule.populateAttributeRemoval();
            if (updateExistingGenotypeList)
                diseaseCurationRPCAsync.searchGenotypes(publicationID, getSelectedFeatureID(), getSelectedGenotypeID(), new RetrieveExistingGenotypeListCallBack("error", errorLabelSearch));
        }


    }

    class RetrieveExistingGenotypeListCallBack extends ZfinAsyncCallback<List<GenotypeDTO>> {


        public RetrieveExistingGenotypeListCallBack(String errorMessage, ErrorHandler errorLabel) {
            super(errorMessage, errorLabel, loadingImage);
        }

        @Override
        public void onSuccess(List<GenotypeDTO> list) {
            existingGenotypeList = list;
            resetUI();
            updateExistingGenotypeListTableContent(list);
            loadingImage.setVisible(false);
        }
    }
}

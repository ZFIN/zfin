package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.ui.*;
import org.zfin.gwt.root.util.DeleteImage;
import org.zfin.gwt.root.util.ShowHideWidget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Entry point for FX curation module.
 */
public class FishModule implements HandlesError, EntryPoint {

    public static final String FISH_TAB = "fishTab";
    public static final String UNRECOVERED = "unrecovered";
    public static final String UNSPECIFIED = "unspecified";
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
    ListBox backgroundNewGenoListBox = new ListBox();
    Button searchExistingGenotypes = new Button("Search");

    @UiField
    Button createFishButton;

    @UiField
    SimpleErrorElement errorLabelSearch;
    @UiField
    SimpleErrorElement errorCreateGenotype;

    @UiField
    SimpleErrorElement errorLabel;

    @UiField
    Image loadingImage;
    @UiField
    Image loadingImageCreateGenotype;

    @UiField
    Image loadingImageGenoSearch;

    @UiField
    Label noneDefined;

    @UiField
    Label noneDefinedGenoLabel;

    @UiField
    ZfinFlexTable constructionTable;

    @UiField
    ZfinFlexTable genotypeConstructionTable;
    @UiField
    VerticalPanel genotypeConstructionPanel;
    @UiField
    VerticalPanel importGenotypePanel;
    @UiField
    VerticalPanel fishConstructionPanel;
    @UiField
    ZfinFlexTable fishListTable;

    @UiField
    ZfinFlexTable genotypeListTable;

    @UiField
    ZfinFlexTable genotypeSearchResultTable;
    @UiField
    ZfinFlexTable newGenotypeInfoTable;

    @UiField
    Hyperlink showHideGenoList;
    @UiField
    Hyperlink showHideExistingGeno;
    @UiField
    Button createGenotypeButton;
    @UiField
    Hyperlink showHideFishConstruction;

    private TextBox genotypeNickname = new TextBox();
    private InlineHTML genotypeDisplayName = new InlineHTML();
    private Label genotypeHandle = new Label();
    @UiField
    Hyperlink showHideGenotypeConstruction;

    private ShowHideWidget genotypeListToggle;
    private ShowHideWidget existingGenotypeListToggle;
    private ShowHideWidget genotypeConstructionListToggle;
    private ShowHideWidget fishConstructionListToggle;

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

    private Button buttonUUU = new Button("U,U,U");
    private Button button211 = new Button("2,1,1");
    private Button button2UU = new Button("2,U,U");

    @Override
    public void onModuleLoad() {

        FlowPanel outer = uiBinder.createAndBindUi(this);
        RootPanel.get(FISH_TAB).add(outer);
        errorLabel.setStyleName("error");
        genotypeListToggle = new ShowHideWidget(showHideGenoList, genotypeListTable, true);
        existingGenotypeListToggle = new ShowHideWidget(showHideExistingGeno, importGenotypePanel);
        genotypeConstructionListToggle = new ShowHideWidget(showHideGenotypeConstruction, genotypeConstructionPanel);
        fishConstructionListToggle = new ShowHideWidget(showHideFishConstruction, fishConstructionPanel);
        genotypeListCallBack = new RetrieveDTOListCallBack<>(genotypeSelectionBox, "Genotypes", null);
        strListCallBack = new RetrieveRelatedEntityListCallBack(strSelectionBox, "STRs", null);
        attributionModule = new AttributionModule();
        RelatedEntityDTO relatedEntityDTO = new RelatedEntityDTO();
        relatedEntityDTO.setPublicationZdbID(publicationID);
        attributionModule.setDTO(relatedEntityDTO);
        addHandlers();
        initConstructionTableHeader();
        initGenotypeConstructionTableHeader();
        initNewGenotypeInfo();
        initConstructionRow();
        initFishListTable();
        initConstructionGenotypeSearchResultRow(0);
        initGenotypeConstructionRow(1);
        retrieveAllValues();
        createGenotypeButton.setText("Create Genotype");

        // Add the widgets to the root panel.
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                updateConstructionTable();
            }
        });

    }

    private class PublicNotePopup extends PopupPanel {

        public PublicNotePopup(final GenotypeDTO genotypeDTO, boolean isPublic) {
            // set auto hide to true
            super(true);
            makeBackgroundDarker();
            VerticalPanel vPanel = new VerticalPanel();
            final TextArea textArea = new TextArea();
            textArea.setHeight("100px");
            textArea.setWidth("250px");
            vPanel.add(textArea);
            Button save = new Button("Save");
            vPanel.add(save);
            setWidget(vPanel);
            if (isPublic) {
                save.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent clickEvent) {
                        diseaseCurationRPCAsync.createPublicNote(publicationID, genotypeDTO, textArea.getText(), new RetrieveGenotypeListCallBack("Genotype List", errorLabel));
                        hide();
                    }
                });
            } else {
                save.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent clickEvent) {
                        diseaseCurationRPCAsync.createCuratorNote(publicationID, genotypeDTO, textArea.getText(), new RetrieveGenotypeListCallBack("Genotype List", errorLabel));
                        hide();
                    }
                });
            }
        }

        private void makeBackgroundDarker() {
            setGlassEnabled(true);
            Style glassStyle = getGlassElement().getStyle();
            glassStyle.setProperty("width", "100%");
            glassStyle.setProperty("height", "100%");
            glassStyle.setProperty("backgroundColor", "#000");
            glassStyle.setProperty("opacity", "0.45");
        }

        public PublicNotePopup(final NoteDTO externalNoteDTO, boolean isPublic) {
            // set auto hide to true
            super(true);
            makeBackgroundDarker();
            VerticalPanel vPanel = new VerticalPanel();
            final TextArea textArea = new TextArea();
            textArea.setHeight("100px");
            textArea.setWidth("250px");
            textArea.setText(externalNoteDTO.getNoteData());
            vPanel.add(textArea);
            Button save = new Button("Save");
            vPanel.add(save);
            setWidget(vPanel);
            if (isPublic) {
                save.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent clickEvent) {
                        externalNoteDTO.setNoteData(getNoteStub(textArea.getText()));
                        diseaseCurationRPCAsync.savePublicNote(publicationID, (ExternalNoteDTO) externalNoteDTO, new ZfinAsyncCallback<ExternalNoteDTO>("public note ", errorLabelSearch) {
                            @Override
                            public void onSuccess(ExternalNoteDTO noteDTO) {
                                publicNoteAnchor.get(noteDTO.getZdbID()).setText(noteDTO.getNoteData());
                            }
                        });
                        hide();
                    }
                });
            } else {
                save.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent clickEvent) {
                        externalNoteDTO.setNoteData(getNoteStub(textArea.getText()));
                        diseaseCurationRPCAsync.saveCuratorNote(publicationID, (CuratorNoteDTO) externalNoteDTO, new ZfinAsyncCallback<CuratorNoteDTO>("curator note ", errorLabelSearch) {
                            @Override
                            public void onSuccess(CuratorNoteDTO noteDTO) {
                                curatorNoteAnchor.get(noteDTO.getZdbID()).setText(noteDTO.getNoteData());
                            }
                        });
                        hide();
                    }
                });
            }
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
                    RetrieveFishListCallBack fishListCallBack = new RetrieveFishListCallBack("error", errorLabel);
                    fishListCallBack.setInitiatedFromNewFishCreation(true);
                    diseaseCurationRPCAsync.createFish(publicationID, newFish, fishListCallBack);
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
                // update feature list on geno search
                diseaseCurationRPCAsync.getFeatureList(publicationID, new RetrieveRelatedEntityDTOListCallBack(featureListBox, "Feature List", errorLabel));
            }

            @Override
            public void addHandlesErrorListener(HandlesError handlesError) {
            }
        });
        showHideExistingGeno.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                existingGenotypeListToggle.toggleVisibility();
            }
        });
        showHideGenoList.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                genotypeListToggle.toggleVisibility();
            }
        });
        showHideFishConstruction.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                fishConstructionListToggle.toggleVisibility();
            }
        });
        showHideGenotypeConstruction.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                genotypeConstructionListToggle.toggleVisibility();
            }
        });
        featureListBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent changeEvent) {
                searchForGenotypes();
            }
        });
        backgroundListBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent changeEvent) {
                searchForGenotypes();
            }
        });

        buttonUUU.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                zygosityListBox.setSelectedIndex(3);
                zygosityMaternalListBox.setSelectedIndex(3);
                zygosityPaternalListBox.setSelectedIndex(3);
                resetGenoConstructionZoneError();
            }
        });
        button2UU.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                zygosityListBox.setSelectedIndex(0);
                zygosityMaternalListBox.setSelectedIndex(3);
                zygosityPaternalListBox.setSelectedIndex(3);
                resetGenoConstructionZoneError();
            }
        });
        button211.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                zygosityListBox.setSelectedIndex(0);
                zygosityMaternalListBox.setSelectedIndex(1);
                zygosityPaternalListBox.setSelectedIndex(1);
                resetGenoConstructionZoneError();
            }
        });

        addGenotypeFeature.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                resetGenoConstructionZoneError();
                int selectedFeature = featureForGenotypeListBox.getSelectedIndex();
                GenotypeFeatureDTO dto = new GenotypeFeatureDTO();
                FeatureDTO feature = featureGenotypeListCallBack.getDtoList().get(selectedFeature);
                if (featureAlreadyInUse(feature)) {
                    errorCreateGenotype.setError("Feature already used.");
                    return;
                }
                dto.setFeatureDTO(feature);
                dto.setZygosity(zygosityList.get(zygosityListBox.getSelectedIndex()));
                dto.setMaternalZygosity(zygosityList.get(zygosityMaternalListBox.getSelectedIndex()));
                dto.setPaternalZygosity(zygosityList.get(zygosityPaternalListBox.getSelectedIndex()));
                genotypeFeatureDTOList.add(dto);
                updateGenotypeFeatureList();
            }
        });
        backgroundNewGenoListBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent changeEvent) {
                getSelectedGenotypeBackground();
                setGenotypeInfo();
                resetGenoConstructionZoneError();
            }
        });
        createGenotypeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                resetGenoConstructionZoneError();
                if(genotypeFeatureDTOList.size() == 0){
                    errorCreateGenotype.setError("No Feature selected");
                    return;
                }
                String nicknameString = genotypeNickname.getText();
                if (nicknameString.equals(genotypeHandle.getText()))
                    nicknameString = null;
                diseaseCurationRPCAsync.createGenotypeFeature(publicationID,
                        genotypeFeatureDTOList,
                        getSelectedGenotypeBackground(),
                        nicknameString,
                        new CreateGenotypeCallBack("Create new Genotype", errorCreateGenotype, loadingImageCreateGenotype));
                loadingImageCreateGenotype.setVisible(true);
            }
        });
        featureForGenotypeListBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent changeEvent) {
                resetGenoConstructionZoneError();
            }
        });
    }

    private void resetGenoConstructionZoneError() {
        errorCreateGenotype.setError("");
    }

    private boolean featureAlreadyInUse(FeatureDTO feature) {
        if (genotypeFeatureDTOList == null)
            return false;
        for (GenotypeFeatureDTO dto : genotypeFeatureDTOList)
            if (dto.getFeatureDTO().equals(feature))
                return true;
        return false;
    }

    private GenotypeDTO getSelectedGenotypeBackground() {
        int index = backgroundNewGenoListBox.getSelectedIndex();
        if (index == 0)
            backgroundNewGeno = null;
        else {
            // offset empty first entry
            backgroundNewGeno = retrieveBackgroundNewGenoCallback.getDtoList().get(index - 1);
        }
        return backgroundNewGeno;
    }

    private GenotypeDTO backgroundNewGeno;

    private List<GenotypeFeatureDTO> genotypeFeatureDTOList = new ArrayList<>(4);

    private void searchForGenotypes() {
        String featureID = getSelectedFeatureID();
        String genotypeID = getSelectedGenotypeID();
        if (featureID == null && genotypeID == null)
            return;
        diseaseCurationRPCAsync.searchGenotypes(publicationID, featureID, genotypeID, new RetrieveExistingGenotypeListCallBack("error", errorLabelSearch));
        loadingImageGenoSearch.setVisible(true);
    }

    private String getSelectedFeatureID() {
        String id = featureListBox.getSelectedValue();
        if (id.trim().equals(""))
            return null;
        return id;
    }

    private String getSelectedGenotypeID() {
        String id = backgroundListBox.getSelectedValue();
        if (id.trim().equals(""))
            return null;
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
    private RetrieveRelatedEntityDTOListCallBack<GenotypeDTO> retrieveBackgroundNewGenoCallback;

    private void retrieveAllValues() {
        // get genotype list
        curationExperimentRPCAsync.getGenotypes(publicationID, genotypeListCallBack);

        // get wildtype background list
        RetrieveRelatedEntityDTOListCallBack<GenotypeDTO> retrieveBackgroundCallback = new RetrieveRelatedEntityDTOListCallBack(backgroundListBox, "Background List", errorLabel);
        retrieveBackgroundCallback.setLeaveFirstEntryBlank(true);
        curationExperimentRPCAsync.getBackgroundGenotypes(publicationID, retrieveBackgroundCallback);

        // set wildtype background list for new Geno generation
        retrieveBackgroundNewGenoCallback = new RetrieveRelatedEntityDTOListCallBack(backgroundNewGenoListBox, "Background List", errorLabel);
        retrieveBackgroundNewGenoCallback.setLeaveFirstEntryBlank(true);
        curationExperimentRPCAsync.getBackgroundGenotypes(publicationID, retrieveBackgroundNewGenoCallback);

        // get STR list
        diseaseCurationRPCAsync.getStrList(publicationID, strListCallBack);

        // get Fish List
        diseaseCurationRPCAsync.getFishList(publicationID, new RetrieveFishListCallBack("Fish List", errorLabel));

        // get genotype List
        diseaseCurationRPCAsync.getGenotypeList(publicationID, new RetrieveGenotypeListCallBack("Genotype List", errorLabel));

        // get Feature List
        RetrieveRelatedEntityDTOListCallBack<FeatureDTO> featureListCallBack = new RetrieveRelatedEntityDTOListCallBack(featureListBox, "Feature List", errorLabel);
        featureListCallBack.setLeaveFirstEntryBlank(true);
        diseaseCurationRPCAsync.getFeatureList(publicationID, featureListCallBack);

        // get Feature List  from new Genotypes
        featureGenotypeListCallBack = new RetrieveRelatedEntityDTOListCallBack<>(featureForGenotypeListBox, "Feature Geno List", errorLabel);
        diseaseCurationRPCAsync.getFeatureList(publicationID, featureGenotypeListCallBack);

        diseaseCurationRPCAsync.getZygosityLists(new RetrieveZygosityListCallBack("Zygosity List", errorLabel));

    }

    private RetrieveRelatedEntityDTOListCallBack<FeatureDTO> featureGenotypeListCallBack;

    private void initNewGenotypeInfo() {
        int column = 0;
        int row = 0;
        newGenotypeInfoTable.setText(row, column, "Display Name");
        newGenotypeInfoTable.getCellFormatter().setStyleName(row, column++, "table-header bold");
        newGenotypeInfoTable.setWidget(row, column, genotypeDisplayName);
        newGenotypeInfoTable.getCellFormatter().setStyleName(row, column, "bold");
        row++;
        column = 0;
        newGenotypeInfoTable.setText(row, column, "Handle");
        newGenotypeInfoTable.getCellFormatter().setStyleName(row, column++, "table-header bold");
        newGenotypeInfoTable.setWidget(row, column, genotypeHandle);
        newGenotypeInfoTable.getCellFormatter().setStyleName(row, column, "bold");
        column = 0;
        row++;
        newGenotypeInfoTable.setText(row, column, "Nickname");
        newGenotypeInfoTable.getCellFormatter().setStyleName(row, column++, "table-header bold");
        newGenotypeInfoTable.setWidget(row, column, genotypeNickname);
        newGenotypeInfoTable.getCellFormatter().setStyleName(row, column, "bold");
    }


    private void initConstructionTableHeader() {
        constructionTable.setText(0, 0, "Genotype");
        constructionTable.getCellFormatter().setStyleName(0, 0, "bold");
        constructionTable.setText(0, 1, "ST Reagent");
        constructionTable.getCellFormatter().setStyleName(0, 1, "bold");
        constructionTable.getRowFormatter().setStyleName(0, "table-header");
    }

    private void initGenotypeConstructionTableHeader() {
        int column = 0;
        genotypeConstructionTable.setText(0, column, "Feature");
        genotypeConstructionTable.getCellFormatter().setStyleName(0, column++, "bold");
        genotypeConstructionTable.setText(0, column, "Zygosity");
        genotypeConstructionTable.getCellFormatter().setStyleName(0, column++, "bold");
        genotypeConstructionTable.setText(0, column, "Maternal Zygosity");
        genotypeConstructionTable.getCellFormatter().setStyleName(0, column++, "bold");
        genotypeConstructionTable.setText(0, column, "Paternal Zygosity");
        genotypeConstructionTable.getCellFormatter().setStyleName(0, column++, "bold");
        genotypeConstructionTable.setText(0, column, "Delete");
        genotypeConstructionTable.getCellFormatter().setStyleName(0, column++, "bold");
        genotypeConstructionTable.getRowFormatter().setStyleName(0, "table-header");
    }

    private ListBox featureForGenotypeListBox = new ListBox();
    private ListBox zygosityListBox = new ListBox();
    private ListBox zygosityMaternalListBox = new ListBox();
    private ListBox zygosityPaternalListBox = new ListBox();
    private Button addGenotypeFeature = new Button("Add");

    private void initGenotypeConstructionRow(int row) {
        int col = 0;
        genotypeConstructionTable.setWidget(row, col, featureForGenotypeListBox);
        genotypeConstructionTable.getCellFormatter().setStyleName(row, col++, "bold");
        genotypeConstructionTable.setWidget(row, col, zygosityListBox);
        genotypeConstructionTable.getCellFormatter().setStyleName(row, col++, "bold");
        genotypeConstructionTable.setWidget(row, col, zygosityMaternalListBox);
        genotypeConstructionTable.getCellFormatter().setStyleName(row, col++, "bold");
        genotypeConstructionTable.setWidget(row, col, zygosityPaternalListBox);
        genotypeConstructionTable.getCellFormatter().setStyleName(row, col++, "bold");
        genotypeConstructionTable.setWidget(row, col, addGenotypeFeature);
        genotypeConstructionTable.getCellFormatter().setStyleName(row, col++, "bold");
        genotypeConstructionTable.getRowFormatter().setStyleName(row, "table-header");
        col = 0;
        row++;
        genotypeConstructionTable.setText(row, col, "Set Zygosities");
        HorizontalPanel panel = new HorizontalPanel();
        panel.add(buttonUUU);
        panel.add(button2UU);
        panel.add(button211);
        genotypeConstructionTable.getCellFormatter().setStyleName(row, col++, "bold");
        genotypeConstructionTable.setWidget(row, col, panel);
        genotypeConstructionTable.getFlexCellFormatter().setColSpan(row, col, 4);
        col = 0;
        row++;
        genotypeConstructionTable.getCellFormatter().setStyleName(row, col, "bold");
        genotypeConstructionTable.setText(row, col++, "Background");
        genotypeConstructionTable.setWidget(row, col, backgroundNewGenoListBox);
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

    private void updateGenotypeFeatureList() {
        genotypeConstructionTable.removeAllRows();
        if (genotypeFeatureDTOList == null || genotypeFeatureDTOList.size() == 0) {
            initGenotypeConstructionTableHeader();
            initGenotypeConstructionRow(1);
            return;
        }
        initGenotypeConstructionTableHeader();
        int groupIndex = 0;
        int rowIndex = 1;
        for (GenotypeFeatureDTO genotypeFeature : genotypeFeatureDTOList) {
            int col = 0;
            genotypeConstructionTable.setWidget(rowIndex, col++, getHtml(genotypeFeature.getFeatureDTO()));
            genotypeConstructionTable.setWidget(rowIndex, col++, getHtml(genotypeFeature.getZygosity()));
            genotypeConstructionTable.setWidget(rowIndex, col++, getHtml(genotypeFeature.getMaternalZygosity()));
            genotypeConstructionTable.setWidget(rowIndex, col++, getHtml(genotypeFeature.getPaternalZygosity()));
            DeleteImage delete = new DeleteImage("Remove Note");
            delete.addClickHandler(new RemoveGenotypeFeature(genotypeFeature));
            genotypeConstructionTable.setWidget(rowIndex, col++, delete);
            groupIndex = genotypeConstructionTable.setRowStyle(rowIndex++, null, genotypeFeature.getZdbID(), groupIndex);
        }
        setGenotypeInfo();
        initGenotypeConstructionRow(rowIndex);
    }

    private void setGenotypeInfo() {
        String genotypeHandleName = "";
        String genotypeDisplayNameString = "";
        for (GenotypeFeatureDTO genotypeFeature : genotypeFeatureDTOList) {
            genotypeHandleName += genotypeFeature.getFeatureDTO().getName();
            genotypeHandleName += genotypeFeature.getZygosityInfo();
            genotypeDisplayNameString += "<i>";
            if (genotypeFeature.getFeatureDTO().getDisplayNameForGenotypeBase() != null) {
                genotypeDisplayNameString += genotypeFeature.getFeatureDTO().getDisplayNameForGenotypeBase();
                genotypeDisplayNameString += "<sup>";
                genotypeDisplayNameString += genotypeFeature.getZygosity().getMutantZygosityDisplay(getDisplayFeatureName(genotypeFeature.getFeatureDTO().getName()));
                genotypeDisplayNameString += "</sup>";
            } else {
                genotypeDisplayNameString += genotypeFeature.getZygosity().getMutantZygosityDisplay(getDisplayFeatureName(genotypeFeature.getFeatureDTO().getName()));
            }
            genotypeDisplayNameString += "</i>";
            genotypeDisplayNameString += " ; ";
        }
        if (backgroundNewGeno != null) {
            genotypeHandleName += backgroundNewGeno.getName();
        }
        genotypeDisplayNameString = genotypeDisplayNameString.substring(0, genotypeDisplayNameString.length() - 3);
        genotypeHandle.setText(genotypeHandleName);
        genotypeDisplayName.setHTML(SafeHtmlUtils.fromTrustedString(genotypeDisplayNameString));
        genotypeDisplayName.setHTML(SafeHtmlUtils.fromTrustedString(genotypeDisplayNameString));
        genotypeNickname.setText(genotypeHandleName);
    }

    private String getDisplayFeatureName(String name) {
        if (name.endsWith("_" + UNRECOVERED))
            return UNRECOVERED;
        if (name.endsWith("_" + UNSPECIFIED))
            return UNSPECIFIED;
        return name;
    }

    private InlineHTML getHtml(RelatedEntityDTO dto) {
        InlineHTML html = new InlineHTML(dto.getName());
        html.setTitle(dto.getZdbID());
        return html;
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
        genotypeSearchResultTable.setText(0, col++, "Add");
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
            DeleteImage deleteFish = new DeleteImage("/action/infrastructure/deleteRecord/" + fish.getZdbID(), "Delete Fish");
            fishListTable.getCellFormatter().setHorizontalAlignment(index, col, HasHorizontalAlignment.ALIGN_CENTER);
            fishListTable.setWidget(index++, col++, deleteFish);
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
            InlineHTML handle = new InlineHTML(genotype.getNickName());
            handle.setTitle(genotype.getZdbID());
            genotypeListTable.setWidget(index, col++, handle);
            VerticalPanel featurePanel = new VerticalPanel();
            if (genotype.getFeatureList() != null && genotype.getFeatureList().size() > 0)
                for (FeatureDTO featureDTO : genotype.getFeatureList())
                    featurePanel.add(new InlineHTML(featureDTO.getAbbreviation()));
            genotypeListTable.setWidget(index, col++, featurePanel);

            VerticalPanel publicNotePanel = addPublicNotes(genotype);
            VerticalPanel curatorNotePanel = addCuratorNotes(genotype);
            genotypeListTable.setWidget(index, col++, publicNotePanel);
            genotypeListTable.setWidget(index, col++, curatorNotePanel);
            DeleteImage deleteImage = new DeleteImage("/action/infrastructure/deleteRecord/" + genotype.getZdbID(), "Delete Genotype");
            genotypeListTable.getCellFormatter().setHorizontalAlignment(index, col, HasHorizontalAlignment.ALIGN_CENTER);
            genotypeListTable.setWidget(index++, col++, deleteImage);
            groupIndex = genotypeListTable.setRowStyle(rowIndex++, null, genotype.getZdbID(), groupIndex);

        }
    }

    private VerticalPanel addPublicNotes(GenotypeDTO genotype) {
        VerticalPanel publicNotePanel = new VerticalPanel();
        if (genotype.getPublicNotes(publicationID) != null) {
            for (final ExternalNoteDTO note : genotype.getPublicNotes(publicationID)) {
                Anchor publicNote = new Anchor(getNoteStub(note.getNoteData()));
                HorizontalPanel panel = new HorizontalPanel();
                panel.add(publicNote);
                DeleteImage remove = new DeleteImage("Remove Note");
                remove.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent clickEvent) {
                        diseaseCurationRPCAsync.deletePublicNote(publicationID, note, new RetrieveGenotypeListCallBack("delete note", errorLabelSearch));
                    }
                });
                panel.add(remove);
                publicNoteAnchor.put(note.getZdbID(), publicNote);
                publicNotePanel.add(panel);
                addClickHandler(note, publicNote, true);
            }
        }
        Anchor publicNote = new Anchor("Add");
        addClickHandler(genotype, publicNote, true);
        publicNoteAnchor.put(genotype.getDataZdbID(), publicNote);
        publicNotePanel.add(publicNote);
        return publicNotePanel;
    }

    private VerticalPanel addCuratorNotes(GenotypeDTO genotype) {
        VerticalPanel curatorNotesPanel = new VerticalPanel();
        if (genotype.getPrivateNotes() != null) {
            for (final CuratorNoteDTO note : genotype.getPrivateNotes()) {
                Anchor curatorNote = new Anchor(getNoteStub(note.getNoteData()));
                HorizontalPanel panel = new HorizontalPanel();
                panel.add(curatorNote);
                DeleteImage remove = new DeleteImage("Remove Note");
                remove.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent clickEvent) {
                        diseaseCurationRPCAsync.deleteCuratorNote(publicationID, note, new RetrieveGenotypeListCallBack("delete note", errorLabelSearch));
                    }
                });
                panel.add(remove);
                curatorNoteAnchor.put(note.getZdbID(), curatorNote);
                curatorNotesPanel.add(panel);
                addClickHandler(note, curatorNote, false);
            }
        }
        Anchor curatorNote = new Anchor("Add");
        addClickHandler(genotype, curatorNote, false);
        curatorNoteAnchor.put(genotype.getDataZdbID(), curatorNote);
        curatorNotesPanel.add(curatorNote);

        return curatorNotesPanel;
    }

    private String getNoteStub(String note) {
        if (note.length() < 15)
            return note;
        else
            return note.substring(0, 15) + "...";
    }

    private void addClickHandler(final GenotypeDTO noteDTO, Anchor publicNote, final boolean isPublic) {
        publicNote.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                PublicNotePopup publicNotePopup = new PublicNotePopup(noteDTO, isPublic);
                publicNotePopup.show();
                publicNotePopup.center();
            }
        });
    }

    private void addClickHandler(final NoteDTO noteDTO, Anchor publicNote, final boolean isPublic) {
        publicNote.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                PublicNotePopup publicNotePopup = new PublicNotePopup(noteDTO, isPublic);
                publicNotePopup.show();
                publicNotePopup.center();
            }
        });
    }

    private Map<String, Anchor> publicNoteAnchor = new HashMap<>();
    private Map<String, Anchor> curatorNoteAnchor = new HashMap<>();

    public void updateExistingGenotypeListTableContent(List<GenotypeDTO> genotypeDTOList) {
        genotypeSearchResultTable.removeAllRows();
        if (genotypeDTOList == null || genotypeDTOList.size() == 0) {
            initGenotypeSearchResultTable();
            initConstructionGenotypeSearchResultRow(1);
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

        private boolean initiatedFromNewFishCreation;

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
            if (initiatedFromNewFishCreation)
                attributionModule.populateAttributeRemoval();
        }

        public void setInitiatedFromNewFishCreation(boolean initiatedFromNewFishCreation) {
            this.initiatedFromNewFishCreation = initiatedFromNewFishCreation;
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
            if (list != null && list.size() > 0)
                noneDefinedGenoLabel.setVisible(false);
            updateFishGenotypeListTableContent(list);
            loadingImage.setVisible(false);
            if (fishList != null && fishList.size() > 0)
                noneDefined.setVisible(false);
            if (updateExistingGenotypeList)
                diseaseCurationRPCAsync.searchGenotypes(publicationID, getSelectedFeatureID(), getSelectedGenotypeID(), new RetrieveExistingGenotypeListCallBack("error", errorLabelSearch));
        }
    }

    class CreateGenotypeCallBack extends ZfinAsyncCallback<GenotypeDTO> {

        public CreateGenotypeCallBack(String errorMessage, ErrorHandler errorLabel, Image loadingImg) {
            super(errorMessage, errorLabel, loadingImg);
        }

        @Override
        public void onSuccess(GenotypeDTO genotypeDTO) {
            resetNewGenotypeUI();
            loadingImageCreateGenotype.setVisible(false);
            errorHandler.setError("Successfully created new Genotype: " + genotypeDTO.getHandle());
            // update genotype list on Create fish section
            curationExperimentRPCAsync.getGenotypes(publicationID, genotypeListCallBack);
            diseaseCurationRPCAsync.getGenotypeList(publicationID, new RetrieveGenotypeListCallBack("Genotype List", errorLabel));
            curationExperimentRPCAsync.getGenotypes(publicationID, genotypeListCallBack);
        }
    }

    private void resetNewGenotypeUI() {
        genotypeNickname = new TextBox();
        genotypeDisplayName = new InlineHTML();
        genotypeHandle = new Label();
        setGenotypeInfo();
    }

    class RetrieveExistingGenotypeListCallBack extends ZfinAsyncCallback<List<GenotypeDTO>> {

        public RetrieveExistingGenotypeListCallBack(String errorMessage, ErrorHandler errorLabel) {
            super(errorMessage, errorLabel, loadingImage);
        }

        @Override
        public void onSuccess(List<GenotypeDTO> list) {
            resetUI();
            if (list != null) {
                existingGenotypeList = list;
                updateExistingGenotypeListTableContent(list);
                attributionModule.populateAttributeRemoval();
            } else {
                existingGenotypeList = new ArrayList<>();
            }
            loadingImageGenoSearch.setVisible(false);
        }
    }

    private List<ZygosityDTO> zygosityList = new ArrayList<>();

    class RetrieveZygosityListCallBack extends ZfinAsyncCallback<List<ZygosityDTO>> {

        public RetrieveZygosityListCallBack(String errorMessage, ErrorHandler errorLabel) {
            super(errorMessage, errorLabel, loadingImage);
        }

        @Override
        public void onSuccess(List<ZygosityDTO> list) {
            zygosityList = list;
            for (ZygosityDTO dto : list) {
                if (!dto.getName().startsWith("wild"))
                    zygosityListBox.addItem(dto.getName(), dto.getZdbID());
                zygosityMaternalListBox.addItem(dto.getName(), dto.getZdbID());
                zygosityPaternalListBox.addItem(dto.getName(), dto.getZdbID());
            }
        }
    }

    class RemoveGenotypeFeature implements ClickHandler {

        private GenotypeFeatureDTO dto;

        public RemoveGenotypeFeature(GenotypeFeatureDTO dto) {
            this.dto = dto;
        }

        @Override
        public void onClick(ClickEvent clickEvent) {
            genotypeFeatureDTOList.remove(dto);
            updateGenotypeFeatureList();
        }
    }
}

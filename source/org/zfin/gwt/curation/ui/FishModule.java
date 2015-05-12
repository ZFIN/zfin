package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.root.dto.FishDTO;
import org.zfin.gwt.root.dto.GenotypeDTO;
import org.zfin.gwt.root.dto.RelatedEntityDTO;
import org.zfin.gwt.root.ui.ErrorHandler;
import org.zfin.gwt.root.ui.HandlesError;
import org.zfin.gwt.root.ui.SimpleErrorElement;
import org.zfin.gwt.root.ui.ZfinAsyncCallback;

import java.util.ArrayList;
import java.util.List;

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

    @UiField
    Button createFishButton;

    @UiField
    SimpleErrorElement errorLabel;

    @UiField
    Image loadingImage;

    @UiField
    Label noneDefined;

    @UiField
    FlexTable constructionTable;

    @UiField
    FlexTable fishListTable;

    public FishModule(String publicationID) {
        this.publicationID = publicationID;
        onModuleLoad();
    }

    // only few STR will be added
    private List<RelatedEntityDTO> newStrList = new ArrayList<>(4);
    private GenotypeDTO newGenotype;
    private List<FishDTO> fishList = new ArrayList<>(10);

    @Override
    public void onModuleLoad() {

        FlowPanel outer = uiBinder.createAndBindUi(this);
        RootPanel.get(FISH_TAB).add(outer);
        errorLabel.setStyleName("error");
        createFishButton.setEnabled(false);
        genotypeListCallBack = new RetrieveGenotypeListCallBack<>(genotypeSelectionBox, "Genotypes", null);
        strListCallBack = new RetrieveRelatedEntityListCallBack(strSelectionBox, "STRs", null);
        genotypeSelectionBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent changeEvent) {
                int index = genotypeSelectionBox.getSelectedIndex();
                //Window.alert(""+index);
                List<GenotypeDTO> dtoList = genotypeListCallBack.getDtoList();
                newGenotype = dtoList.get(index);
                createFishButton.setEnabled(true);
                createFishDisplayName();
                unsetErrorMessage();
            }
        });
        addStrButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                int index = strSelectionBox.getSelectedIndex();
                List<RelatedEntityDTO> dtoList = strListCallBack.getDtoList();
                RelatedEntityDTO e = dtoList.get(index);
                newStrList.add(e);
                createFishDisplayName();
                unsetErrorMessage();
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
        attributionModule = new AttributionModule();
        RelatedEntityDTO relatedEntityDTO = new RelatedEntityDTO();
        relatedEntityDTO.setPublicationZdbID(publicationID);
        attributionModule.setDTO(relatedEntityDTO);
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
            }

            @Override
            public void addHandlesErrorListener(HandlesError handlesError) {

            }
        });
        initConstructionTableHeader();
        initConstructionTable();
        initFishListTable();
        retrieveAllValues();
    }

    private void showLoadingImage(boolean showLoadingImage) {
        loadingImage.setVisible(showLoadingImage);
    }

    private boolean validate(FishDTO newFish) {
        // check if the fish already exists
        if (fishList.size() > 0)
            return !fishList.contains(newFish);
        return true;
    }

    private FishDTO getNewFish() {
        FishDTO dto = new FishDTO();
        dto.setGenotypeDTO(newGenotype);
        dto.setStrList(newStrList);
        return dto;
    }

    private void resetUI() {
        newGenotype = null;
        removeConstructionRow();
        unsetErrorMessage();
    }


    private void unsetErrorMessage() {
        errorLabel.setText("");
    }

    private void createFishDisplayName() {
        updateConstructionTable();
    }

    private String getStrName() {
        String name = "";
        if (newStrList.size() > 0) {
            for (RelatedEntityDTO str : newStrList) {
                name += str.getName() + " + ";
            }
            name = name.substring(0, name.length() - 3);
        }
        return name;
    }

    private CurationDiseaseRPCAsync diseaseCurationRPCAsync = CurationDiseaseRPC.App.getInstance();
    private CurationExperimentRPCAsync curationExperimentRPCAsync = CurationExperimentRPC.App.getInstance();
    private RetrieveGenotypeListCallBack<GenotypeDTO> genotypeListCallBack;
    private RetrieveRelatedEntityListCallBack strListCallBack;

    private void retrieveAllValues() {
        // get genotype list

        curationExperimentRPCAsync.getGenotypes(publicationID, genotypeListCallBack);

        // get STR list
        diseaseCurationRPCAsync.getStrList(publicationID, strListCallBack);

        // get Fish List
        diseaseCurationRPCAsync.getFishList(publicationID, new RetrieveFishListCallBack("Fish List", null));

    }

    private void initConstructionTableHeader() {
        constructionTable.setText(0, 0, "Genotype");
        constructionTable.getCellFormatter().setStyleName(0, 0, "bold");
        constructionTable.setText(0, 1, "ST Reagent");
        constructionTable.getCellFormatter().setStyleName(0, 1, "bold");
        constructionTable.getRowFormatter().setStyleName(0, "table-header");
    }

    private void updateConstructionTable() {
        if (newGenotype != null)
            constructionTable.setText(1, 0, newGenotype.getName());
        else {
            constructionTable.setText(1, 0, "");
            constructionTable.setText(1, 1, "");
            return;
        }
        if (newStrList != null) {
            constructionTable.setText(1, 1, getStrName());
        }
    }

    private void removeConstructionRow() {
        updateConstructionTable();
    }


    private void initConstructionTable() {
        constructionTable.setWidget(2, 0, genotypeSelectionBox);
        HorizontalPanel panelStr = new HorizontalPanel();
        panelStr.add(strSelectionBox);
        panelStr.add(addStrButton);
        constructionTable.setWidget(2, 1, panelStr);
    }

    private void initFishListTable() {
        int col = 0;
        fishListTable.getCellFormatter().setStyleName(0, col, "bold");
        fishListTable.setText(0, col++, "Fish Handle");
        fishListTable.getCellFormatter().setStyleName(0, col, "bold");
        fishListTable.setText(0, col++, "Display Name");
        fishListTable.getCellFormatter().setStyleName(0, col, "bold");
        fishListTable.setText(0, col++, "Delete");
        fishListTable.getRowFormatter().setStyleName(0, "table-header");
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

        for (FishDTO fish : fishList) {
            int col = 0;
            fishListTable.setText(index, col++, fish.getHandle());
            Anchor html = new Anchor(fish.getName(), "/" + fish.getZdbID());
            fishListTable.setWidget(index, col++, html);
            Anchor anchor = new Anchor("X", "/action/infrastructure/deleteRecord/" + fish.getZdbID());
            fishListTable.getCellFormatter().setHorizontalAlignment(index, col, HasHorizontalAlignment.ALIGN_CENTER);
            fishListTable.setWidget(index++, col++, anchor);
        }
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
            //Window.alert("Number of Fish: " + relatedEntityDTOs.size());
            fishList = list;
            resetUI();
            updateFishListTableContent(list);
            loadingImage.setVisible(false);
            if (fishList != null && fishList.size() > 0)
                noneDefined.setVisible(false);
        }
    }
}

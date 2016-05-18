package org.zfin.gwt.curation.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import org.zfin.gwt.root.dto.FishDTO;
import org.zfin.gwt.root.dto.GenotypeDTO;
import org.zfin.gwt.root.dto.RelatedEntityDTO;
import org.zfin.gwt.root.ui.ErrorHandler;
import org.zfin.gwt.root.ui.ZfinAsyncCallback;
import org.zfin.gwt.root.util.AppUtils;

import java.util.*;

/**
 * Construction zone for new Fish: genotype plus zero or more STRs
 */
public class FishConstructionPresenter implements Presenter {

    private CurationDiseaseRPCAsync diseaseRpcService = CurationDiseaseRPC.App.getInstance();

    private CurationExperimentRPCAsync curationExperimentRpcService = CurationExperimentRPC.App.getInstance();
    private FishConstruction view;
    private String publicationID;

    private FishPresenter fishPresenter;

    public FishConstructionPresenter(FishConstruction view, String publicationID) {
        this.view = view;
        this.publicationID = publicationID;
        this.view.setFishConstructionPresenter(this);
    }

    public void setFishPresenter(FishPresenter fishPresenter) {
        this.fishPresenter = fishPresenter;
    }

    // only few STR will be added in most cases
    private Set<RelatedEntityDTO> newStrList = new TreeSet<>();
    private GenotypeDTO newGenotype;
    private GenotypeDTO wildtypeGenotype;

    private RetrieveGenotypeListCallBack genotypeListCallBack;
    private RetrieveSTRListCallBack strListCallBack;

    public void bind() {
        genotypeListCallBack = new RetrieveGenotypeListCallBack(view.getGenotypeSelectionBox(), "Genotypes", null);
        strListCallBack = new RetrieveSTRListCallBack(view.getStrSelectionBox(), "STRs", null);
    }

    private void bindStrLinkRemovers() {
        // remove STRs from pile
        final Map<Anchor, RelatedEntityDTO> strAnchorMap = view.getStrAnchorMap();
        for (final Anchor removeStr : strAnchorMap.keySet()) {
            removeStr.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent clickEvent) {
                    newStrList.remove(strAnchorMap.get(removeStr));
                    view.reCreateStrPanel(newStrList);
                    bindStrLinkRemovers();
                }
            });
        }
    }

    private boolean validate(FishDTO newFish) {
        // check if the fish already exists
        List<FishDTO> fishList = fishPresenter.getFishList();
        if (fishList != null && fishList.size() > 0)
            return !fishList.contains(newFish);
        return true;
    }

    private FishDTO getNewFish() {
        FishDTO dto = new FishDTO();
        dto.setGenotypeDTO(newGenotype);
        if (newStrList != null) {
            dto.setStrList(new ArrayList<>(newStrList));
        }
        return dto;
    }

    public void resetGUI() {
        newGenotype = wildtypeGenotype;
        newStrList.clear();
        view.resetGUI();
        view.setGenotypeName(newGenotype);
    }


    @Override
    public void go() {
        bind();
    }

    public void retrieveInitialEntities() {
        // get STR list
        updateSTRListBox();
        updateGenotypeList();
        view.disableAddStrButton();
    }

    public void updateGenotypeList() {
        // get genotype list
        curationExperimentRpcService.getGenotypes(publicationID, genotypeListCallBack);
    }

    public void updateSTRListBox() {
        diseaseRpcService.getStrList(publicationID, strListCallBack);
    }

    public void onCreateFishButtonClick() {
        FishDTO newFish = getNewFish();
        if (validate(newFish)) {
            RetrieveFishListCallBack fishListCallBack = new RetrieveFishListCallBack("error", view.getErrorLabel());
            diseaseRpcService.createFish(publicationID, newFish, fishListCallBack);
            view.getLoadingImage().setVisible(true);
        } else {
            view.getErrorLabel().setText("Fish already exists");
        }
    }

    public void onShowHideClick() {
        view.getConstructionToggle().toggleVisibility();
        retrieveInitialEntities();
    }

    public void onGenotypeSelection(int index) {
        List<GenotypeDTO> dtoList = genotypeListCallBack.getDtoList();
        newGenotype = dtoList.get(index);
        view.getErrorLabel().setText("");
        view.setGenotypeName(newGenotype);
    }

    public void onStrClick(int index) {
        List<RelatedEntityDTO> strList = strListCallBack.getStrList();
        RelatedEntityDTO str = strList.get(index);
        if (!newStrList.contains(str)) {
            newStrList.add(str);
            view.reCreateStrPanel(newStrList);
            view.getErrorLabel().setText("");
        } else {
            view.getErrorLabel().setText("STR already added");
        }
        bindStrLinkRemovers();
    }

    public void onSTRSelection() {
        view.getErrorLabel().setText("");
    }

    class RetrieveFishListCallBack extends ZfinAsyncCallback<List<FishDTO>> {
        public RetrieveFishListCallBack(String errorMessage, ErrorHandler errorLabel) {
            super(errorMessage, errorLabel, (Widget) null);
        }

        @Override
        public void onSuccess(List<FishDTO> list) {
            view.getLoadingImage().setVisible(false);
            resetGUI();
            AppUtils.EVENT_BUS.fireEvent(new AddNewFishEvent());
        }

    }

    public class RetrieveGenotypeListCallBack extends ZfinAsyncCallback<List<GenotypeDTO>> {

        private ListBox entityList;
        private List<GenotypeDTO> dtoList;

        public RetrieveGenotypeListCallBack(ListBox listBox, String errorMessage, ErrorHandler errorLabel) {
            super(errorMessage, errorLabel);
            this.entityList = listBox;
        }

        @Override
        public void onSuccess(List<GenotypeDTO> dtoList) {
            entityList.clear();
            this.dtoList = dtoList;
            int index = 0;
            for (GenotypeDTO dto : dtoList) {
                // set the first genotype -- WT -- into the construction zone
                if (index == 0) {
                    newGenotype = dto;
                    wildtypeGenotype = dto;
                }
                if (dto.getName() == null)
                    continue;
                if (dto.getName().startsWith("---")) {
                    entityList.addItem(dto.getName(), dto.getZdbID());
                    entityList.getElement().getElementsByTagName("option").getItem(index).setAttribute("disabled", "disabled");
                } else
                    entityList.addItem(dto.getName(), dto.getZdbID());
                index++;
            }
            onGenotypeSelection(0);
        }

        public List<GenotypeDTO> getDtoList() {
            return dtoList;
        }
    }


    public class RetrieveSTRListCallBack extends ZfinAsyncCallback<List<RelatedEntityDTO>> {

        private ListBox listBox;
        private List<RelatedEntityDTO> strList = new ArrayList<>();

        public RetrieveSTRListCallBack(ListBox listBox, String errorMessage, ErrorHandler errorLabel) {
            super(errorMessage, errorLabel);
            this.listBox = listBox;
        }

        @Override
        public void onSuccess(List<RelatedEntityDTO> dtoList) {
            listBox.clear();
            strList = new ArrayList<>();
            strList.add(new RelatedEntityDTO());
            strList.addAll(dtoList);

            listBox.addItem("------", "");
            for (RelatedEntityDTO entityDTO : dtoList) {
                listBox.addItem(entityDTO.getName(), entityDTO.getZdbID());
            }
        }

        public List<RelatedEntityDTO> getStrList() {
            return strList;
        }
    }

}

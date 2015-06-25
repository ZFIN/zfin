package org.zfin.gwt.curation.ui;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.root.dto.FishDTO;
import org.zfin.gwt.root.dto.GenotypeDTO;
import org.zfin.gwt.root.dto.RelatedEntityDTO;
import org.zfin.gwt.root.ui.ErrorHandler;
import org.zfin.gwt.root.ui.ZfinAsyncCallback;

import java.util.*;

/**
 * Table of associated genotypes
 */
public class FishConstructionPresenter implements Presenter {

    private CurationDiseaseRPCAsync diseaseRpcService = CurationDiseaseRPC.App.getInstance();
    ;
    private CurationExperimentRPCAsync curationExperimentRpcService = CurationExperimentRPC.App.getInstance();
    private final HandlerManager eventBus;
    private FishConstruction view;
    private String publicationID;

    private FishPresenter fishPresenter;

    public FishConstructionPresenter(HandlerManager eventBus, FishConstruction view, String publicationID) {
        this.eventBus = eventBus;
        this.view = view;
        this.publicationID = publicationID;
        view.setPublicationID(publicationID);
    }

    public void setFishPresenter(FishPresenter fishPresenter) {
        this.fishPresenter = fishPresenter;
    }

    // only few STR will be added in most cases
    private Set<RelatedEntityDTO> newStrList = new TreeSet<>();
    private GenotypeDTO newGenotype;

    private RetrieveGenotypeListCallBack genotypeListCallBack;
    private RetrieveSTRListCallBack strListCallBack;

    public void bind() {
        view.getShowHideConstruction().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                view.getConstructionToggle().toggleVisibility();
                retrieveInitialEntities();
            }
        });

        genotypeListCallBack = new RetrieveGenotypeListCallBack(view.getGenotypeSelectionBox(), "Genotypes", null);
        strListCallBack = new RetrieveSTRListCallBack(view.getStrSelectionBox(), "STRs", null);

        view.getCreateFishButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                FishDTO newFish = getNewFish();
                if (validate(newFish)) {
                    RetrieveFishListCallBack fishListCallBack = new RetrieveFishListCallBack("error", view.getErrorLabel());
                    diseaseRpcService.createFish(publicationID, newFish, fishListCallBack);
                    view.getLoadingImage().setVisible(true);
                } else {
                    view.getErrorLabel().setText("Fish already exists");
                }
            }
        });
        view.getGenotypeSelectionBox().addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent changeEvent) {
                int index = view.getGenotypeSelectionBox().getSelectedIndex();
                List<GenotypeDTO> dtoList = genotypeListCallBack.getDtoList();
                newGenotype = dtoList.get(index);
                view.updateConstructionTable(newGenotype, newStrList);
                view.getErrorLabel().setText("");
            }
        });
        Button addStrButton = view.getAddStrButton();
        addStrButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                int index = view.getStrSelectionBox().getSelectedIndex();
                List<RelatedEntityDTO> strList = strListCallBack.getStrList();
                RelatedEntityDTO str = strList.get(index);
                if (!newStrList.contains(str)) {
                    newStrList.add(str);
                    view.updateConstructionTable(newGenotype, newStrList);
                    view.getErrorLabel().setText("");
                } else {
                    view.getErrorLabel().setText("STR already added");
                }
                bindStrLinkRemovers();
            }
        });

        // change handler for STR change selection
        view.getStrSelectionBox().addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent changeEvent) {
                view.getErrorLabel().setText("");
            }
        });
    }

    private void bindStrLinkRemovers() {
        // remove STRs from pile
        final Map<Anchor, RelatedEntityDTO> strAnchorMap = view.getStrAnchorMap();
        for (final Anchor removeStr : strAnchorMap.keySet()) {
            removeStr.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent clickEvent) {
                    newStrList.remove(strAnchorMap.get(removeStr));
                    view.updateConstructionTable(newGenotype, newStrList);
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
        newGenotype = null;
        newStrList.clear();
        view.updateConstructionTable(newGenotype, newStrList);
    }


    @Override
    public void go() {
        bind();
        view.updateConstructionTable(newGenotype, newStrList);
    }

    public void retrieveInitialEntities() {
        // get STR list
        updateSTRListBox();
        updateGenotypeList();

    }

    public void updateGenotypeList() {
        // get genotype list
        curationExperimentRpcService.getGenotypes(publicationID, genotypeListCallBack);
    }

    public void updateSTRListBox() {
        diseaseRpcService.getStrList(publicationID, strListCallBack);
    }

    class RetrieveFishListCallBack extends ZfinAsyncCallback<List<FishDTO>> {
        public RetrieveFishListCallBack(String errorMessage, ErrorHandler errorLabel) {
            super(errorMessage, errorLabel, (Widget) null);
        }

        @Override
        public void onSuccess(List<FishDTO> list) {
            view.getLoadingImage().setVisible(false);
            resetGUI();
            eventBus.fireEvent(new AddNewFishEvent());
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
                if (index == 0)
                    newGenotype = dto;
                if (dto.getName() == null)
                    continue;
                if (dto.getName().startsWith("---")) {
                    entityList.addItem(dto.getName(), dto.getZdbID());
                    entityList.getElement().getElementsByTagName("option").getItem(index).setAttribute("disabled", "disabled");
                } else
                    entityList.addItem(dto.getName(), dto.getZdbID());
                index++;
            }
            view.updateConstructionTable(newGenotype, newStrList);
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
            strList = dtoList;

            for (RelatedEntityDTO entityDTO : dtoList) {
                listBox.addItem(entityDTO.getName(), entityDTO.getZdbID());
            }
        }

        public List<RelatedEntityDTO> getStrList() {
            return strList;
        }
    }

}

package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.root.dto.GenotypeDTO;
import org.zfin.gwt.root.dto.RelatedEntityDTO;
import org.zfin.gwt.root.ui.SimpleErrorElement;
import org.zfin.gwt.root.util.ShowHideWidget;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Table of associated genotypes
 */
public class FishConstruction extends Composite {

    private static MyUiBinder binder = GWT.create(MyUiBinder.class);

    @UiTemplate("FishConstruction.ui.xml")
    interface MyUiBinder extends UiBinder<FlowPanel, FishConstruction> {
    }

    public FishConstruction() {
        initWidget(binder.createAndBindUi(this));
        constructionToggle = new ShowHideWidget(showHideConstruction, constructionPanel);
    }

    @UiField
    VerticalPanel constructionPanel;
    @UiField
    Hyperlink showHideConstruction;
    @UiField
    Button createFishButton;
    @UiField
    SimpleErrorElement errorLabel;
    @UiField
    Image loadingImage;
    @UiField
    InlineHTML genotypeName;
    @UiField
    HorizontalPanel sTRPanel;
    @UiField
    ListBox genotypeSelectionBox;
    @UiField
    ListBox strSelectionBox;
    @UiField
    Button addStrButton;

    private ShowHideWidget constructionToggle;

    private FishConstructionPresenter fishConstructionPresenter;

    public void setFishConstructionPresenter(FishConstructionPresenter fishConstructionPresenter) {
        this.fishConstructionPresenter = fishConstructionPresenter;
    }

    public void resetGUI() {
        genotypeName.setHTML("");
        sTRPanel.clear();
        genotypeSelectionBox.setSelectedIndex(0);
        strSelectionBox.setSelectedIndex(0);
    }


    @UiHandler("genotypeSelectionBox")
    void onGenotypeChange(@SuppressWarnings("unused") ChangeEvent event) {
        fishConstructionPresenter.onGenotypeSelection(genotypeSelectionBox.getSelectedIndex());
    }

    @UiHandler("strSelectionBox")
    void onSTRChange(@SuppressWarnings("unused") ChangeEvent event) {
        fishConstructionPresenter.onSTRSelection();
    }

    @UiHandler("createFishButton")
    void onCreateFishButtonClick(@SuppressWarnings("unused") ClickEvent event) {
        fishConstructionPresenter.onCreateFishButtonClick();
    }

    @UiHandler("showHideConstruction")
    void onShowHideClick(@SuppressWarnings("unused") ClickEvent event) {
        fishConstructionPresenter.onShowHideClick();
    }

    @UiHandler("addStrButton")
    void onStrClick(@SuppressWarnings("unused") ClickEvent event) {
        if (strSelectionBox.getSelectedIndex() != 0)
            fishConstructionPresenter.onStrClick(strSelectionBox.getSelectedIndex());
    }

    public void reCreateStrPanel(final Set<RelatedEntityDTO> newStrList) {
        sTRPanel.clear();
        sTRPanel.setSpacing(5);
        if (newStrList == null || newStrList.size() == 0)
            return;
        if (newStrList.size() > 0) {
            int index = 0;
            for (RelatedEntityDTO str : newStrList) {
                sTRPanel.add(new InlineHTML(str.getName()));
                Anchor removeLink = new Anchor(" (X)");
                strAnchorMap.put(removeLink, str);
                sTRPanel.add(removeLink);
                if (index < newStrList.size() - 1)
                    sTRPanel.add(new InlineHTML(" + "));
                index++;
            }
        }
    }

    private Map<Anchor, RelatedEntityDTO> strAnchorMap = new HashMap<>(4);

    public Map<Anchor, RelatedEntityDTO> getStrAnchorMap() {
        return strAnchorMap;
    }

    public ShowHideWidget getConstructionToggle() {
        return constructionToggle;
    }

    public SimpleErrorElement getErrorLabel() {
        return errorLabel;
    }

    public Image getLoadingImage() {
        return loadingImage;
    }

    public ListBox getGenotypeSelectionBox() {
        return genotypeSelectionBox;
    }

    public ListBox getStrSelectionBox() {
        return strSelectionBox;
    }

    public void setGenotypeName(GenotypeDTO genotype) {
        genotypeName.setText(genotype.getName());
    }

}

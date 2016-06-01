package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.root.dto.GenotypeDTO;
import org.zfin.gwt.root.dto.GenotypeFeatureDTO;
import org.zfin.gwt.root.dto.RelatedEntityDTO;
import org.zfin.gwt.root.ui.ShowHideToggle;
import org.zfin.gwt.root.ui.SimpleErrorElement;
import org.zfin.gwt.root.ui.StringListBox;
import org.zfin.gwt.root.ui.ZfinFlexTable;
import org.zfin.gwt.root.util.DeleteLink;
import org.zfin.gwt.root.util.ShowHideWidget;

import java.util.List;
import java.util.Set;

/**
 * Table of associated genotypes
 */
public class FishGenotypeConstruction extends Composite {

    private static MyUiBinder binder = GWT.create(MyUiBinder.class);

    public void setMessage(String reportMessage) {
        messageLabel.setText(reportMessage);
        messageLabel.setStyleName("phenotype-normal");
    }


    @UiTemplate("FishGenotypeConstruction.ui.xml")
    interface MyUiBinder extends UiBinder<FlowPanel, FishGenotypeConstruction> {
    }

    public static final String UNRECOVERED = "unrecovered";
    public static final String UNSPECIFIED = "unspecified";

    @UiField
    ShowHideToggle showHideToggle;
    @UiField
    ZfinFlexTable genotypeConstructionTable;
    @UiField
    VerticalPanel genotypeConstructionPanel;
    @UiField
    Button createFishGenotypeButton;
    @UiField
    Button resetButton;
    @UiField
    SimpleErrorElement errorLabel;
    @UiField
    Image loadingImage;
    @UiField
    InlineHTML genotypeDisplayName;
    @UiField
    Label genotypeHandle;
    @UiField
    StringListBox backgroundListBox;
    @UiField
    Button buttonUUU;
    @UiField
    Button button211;
    @UiField
    Button button2UU;
    @UiField
    HorizontalPanel backgroundGenotypePanel;
    @UiField
    Button button1UU;
    @UiField
    Button button22U;
    @UiField
    Label messageLabel;
    @UiField
    StringListBox strSelectionBox;
    @UiField
    FlowPanel sTRPanel;
    @UiField
    StringListBox featureForGenotypeListBox;
    @UiField
    StringListBox zygosityListBox;
    @UiField
    StringListBox zygosityMaternalListBox;
    @UiField
    StringListBox zygosityPaternalListBox;
    @UiField
    Button addGenotypeFeature;

    public FishGenotypeConstruction() {
        initWidget(binder.createAndBindUi(this));
        genotypeConstructionToggle = new ShowHideWidget(showHideToggle, genotypeConstructionPanel, true);
    }

    FishGenotypeConstructionPresenter presenter;

    public void setPresenter(FishGenotypeConstructionPresenter presenter) {
        this.presenter = presenter;
    }

    private String publicationID;
    private ShowHideWidget genotypeConstructionToggle;


    @UiHandler("showHideToggle")
    void onShowHideClick(@SuppressWarnings("unused") ClickEvent event) {
        presenter.onShowHideClick();
    }

    @UiHandler("createFishGenotypeButton")
    void onCreateFishButtonClick(@SuppressWarnings("unused") ClickEvent event) {
        presenter.onCreateGenotypeButtonClick();
    }

    @UiHandler("resetButton")
    void onResetButtonClick(@SuppressWarnings("unused") ClickEvent event) {
        presenter.resetGUI();
        presenter.disableWTGenotypeBackground(false);
        resetError();
    }

    @UiHandler("buttonUUU")
    void onUUUButtonClick(@SuppressWarnings("unused") ClickEvent event) {
        zygosityListBox.setSelectedIndex(3);
        zygosityMaternalListBox.setSelectedIndex(3);
        zygosityPaternalListBox.setSelectedIndex(3);
        resetError();
    }

    @UiHandler("button2UU")
    void on2UUButtonClick(@SuppressWarnings("unused") ClickEvent event) {
        zygosityListBox.setSelectedIndex(0);
        zygosityMaternalListBox.setSelectedIndex(3);
        zygosityPaternalListBox.setSelectedIndex(3);
        resetError();
    }

    private void resetError() {
        getErrorLabel().setError("");
        setMessage("");
    }

    @UiHandler("button211")
    void on211ButtonClick(@SuppressWarnings("unused") ClickEvent event) {
        zygosityListBox.setSelectedIndex(0);
        zygosityMaternalListBox.setSelectedIndex(1);
        zygosityPaternalListBox.setSelectedIndex(1);
        resetError();
    }

    @UiHandler("button1UU")
    void on1UUButtonClick(@SuppressWarnings("unused") ClickEvent event) {
        zygosityListBox.setSelectedIndex(1);
        zygosityMaternalListBox.setSelectedIndex(3);
        zygosityPaternalListBox.setSelectedIndex(3);
        resetError();
    }

    @UiHandler("button22U")
    void on22UButtonClick(@SuppressWarnings("unused") ClickEvent event) {
        zygosityListBox.setSelectedIndex(0);
        zygosityMaternalListBox.setSelectedIndex(0);
        zygosityPaternalListBox.setSelectedIndex(3);
        resetError();
    }

    @UiHandler("addGenotypeFeature")
    void onAddGenoFeature(@SuppressWarnings("unused") ClickEvent event) {
        resetError();
        presenter.onAddGenoFeatureClick();
    }

    @UiHandler("strSelectionBox")
    void onStrClick(@SuppressWarnings("unused") ChangeEvent event) {
        if (strSelectionBox.getSelectedIndex() != 0)
            presenter.onStrClick(strSelectionBox.getSelectedIndex());
    }

    @UiHandler("backgroundListBox")
    void onAddGenotypeBackgroundClick(@SuppressWarnings("unused") ChangeEvent event) {
        presenter.onBackgroundClick();
        resetError();
    }

    @UiHandler("featureForGenotypeListBox")
    void onFeatureSelectChangeBackgroundClick(@SuppressWarnings("unused") ChangeEvent event) {
        resetError();
    }

    public void resetGUI() {
        errorLabel.clearAllErrors();
    }

    public void reCreateStrPanel(final Set<RelatedEntityDTO> newStrList) {
        sTRPanel.clear();
        if (newStrList == null || newStrList.size() == 0)
            return;
        if (newStrList.size() > 0) {
            int index = 0;
            for (RelatedEntityDTO str : newStrList) {
                sTRPanel.add(new InlineHTML(str.getName()));
                Anchor removeLink = new Anchor(" (X)");
                presenter.addDeleteStrClickHandler(removeLink, str);
                sTRPanel.add(removeLink);
                if (index < newStrList.size() - 1)
                    sTRPanel.add(new InlineHTML(" + "));
                index++;
            }
        }
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
        genotypeConstructionTable.getCellFormatter().setStyleName(0, column, "bold");
        genotypeConstructionTable.getRowFormatter().setStyleName(0, "table-header");
    }


    private void initGenotypeConstructionRow(int row) {
        int col = 0;
        genotypeConstructionTable.setWidget(row, col++, featureForGenotypeListBox);
        genotypeConstructionTable.setWidget(row, col++, zygosityListBox);
        genotypeConstructionTable.setWidget(row, col++, zygosityMaternalListBox);
        genotypeConstructionTable.setWidget(row, col++, zygosityPaternalListBox);
        genotypeConstructionTable.setWidget(row, col, addGenotypeFeature);
        row++;
        col = 0;
        genotypeConstructionTable.setText(row, col++, "Set Zygosities");
        FlowPanel zygosityButtons = new FlowPanel();
        zygosityButtons.add(buttonUUU);
        zygosityButtons.add(button2UU);
        zygosityButtons.add(button211);
        zygosityButtons.add(button1UU);
        zygosityButtons.add(button22U);
        genotypeConstructionTable.setWidget(row, col, zygosityButtons);
        genotypeConstructionTable.getFlexCellFormatter().setColSpan(row, col, 4);
    }

    public void updateGenotypeFeatureList(List<GenotypeFeatureDTO> genotypeFeatureDTOList, List<GenotypeDTO> backgroundGenotype, Set<RelatedEntityDTO> strSet) {
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
            Anchor delete = new Anchor(" (X)");
            presenter.addRemoveGenotypeFeatureClickHandler(delete, genotypeFeature);
            genotypeConstructionTable.setWidget(rowIndex, col, delete);
            groupIndex = genotypeConstructionTable.setRowStyle(rowIndex++, null, genotypeFeature.getZdbID(), groupIndex);
        }
        setFishGenotypeInfo(genotypeFeatureDTOList, backgroundGenotype, strSet);
        initGenotypeConstructionRow(rowIndex);
    }

    public void setFishGenotypeInfo(List<GenotypeFeatureDTO> genotypeFeatureDTOList, List<GenotypeDTO> backgroundGenotypeList, Set<RelatedEntityDTO> strSet) {
        String fishGenotypeDisplayNameString = getFeatureDisplayName(genotypeFeatureDTOList);
        boolean isWildtypeFish = fishGenotypeDisplayNameString.length() == 0;
        if (isWildtypeFish)
            fishGenotypeDisplayNameString += getGenotypeHandleName(backgroundGenotypeList);
        else
            fishGenotypeDisplayNameString += getGenotypeDisplayName(backgroundGenotypeList);
        if (fishGenotypeDisplayNameString.length() > 0)
            fishGenotypeDisplayNameString += "+";
        fishGenotypeDisplayNameString += getStrDisplayName(strSet);
        if (fishGenotypeDisplayNameString.endsWith("+"))
            fishGenotypeDisplayNameString = fishGenotypeDisplayNameString.substring(0, fishGenotypeDisplayNameString.length() - 1);

        genotypeDisplayName.setHTML(SafeHtmlUtils.fromTrustedString(fishGenotypeDisplayNameString));

        String fishGenotypeHandleName = getFeatureHandleName(genotypeFeatureDTOList);
        fishGenotypeHandleName += getGenotypeHandleName(backgroundGenotypeList);
        if (fishGenotypeHandleName.length() > 0)
            fishGenotypeHandleName += "+";
        fishGenotypeHandleName += getStrDisplayName(strSet);
        if (fishGenotypeHandleName.endsWith("+"))
            fishGenotypeHandleName = fishGenotypeHandleName.substring(0, fishGenotypeHandleName.length() - 1);
        genotypeHandle.setText(fishGenotypeHandleName);
        setGenoBackgroundPanel(backgroundGenotypeList);
        reCreateStrPanel(strSet);
    }

    private String getGenotypeHandleName(List<GenotypeDTO> backgroundGenotypeList) {
        String displayName = "";
        if (backgroundGenotypeList == null || backgroundGenotypeList.size() == 0)
            return displayName;
        for (GenotypeDTO backgroundGenotype : backgroundGenotypeList) {
            displayName += backgroundGenotype.getName();
            displayName += ", ";
        }
        displayName = displayName.substring(0, displayName.length() - 2);
        return displayName;
    }

    private String getGenotypeDisplayName(List<GenotypeDTO> backgroundGenotypeList) {
        String displayName = "";
        if (backgroundGenotypeList == null || backgroundGenotypeList.size() == 0)
            return displayName;
        for (GenotypeDTO backgroundGenotype : backgroundGenotypeList) {
            displayName += "(";
            displayName += backgroundGenotype.getName();
            displayName += "), ";
        }
        displayName = displayName.substring(0, displayName.length() - 2);
        return displayName;
    }

    private String getStrDisplayName(Set<RelatedEntityDTO> strSet) {
        String displayName = "";
        if (strSet == null || strSet.size() == 0)
            return displayName;
        for (RelatedEntityDTO str : strSet) {
            displayName += str.getName();
            displayName += "+";
        }
        if (displayName.endsWith("+"))
            displayName = displayName.substring(0, displayName.length() - 1);
        return displayName;
    }

    private String getFeatureHandleName(List<GenotypeFeatureDTO> genotypeFeatureDTOList) {
        String handleName = "";
        if (genotypeFeatureDTOList == null || genotypeFeatureDTOList.size() == 0)
            return handleName;
        for (GenotypeFeatureDTO genotypeFeature : genotypeFeatureDTOList) {
            handleName += genotypeFeature.getFeatureDTO().getName();
            handleName += genotypeFeature.getZygosityInfo();
        }
        return handleName;
    }

    private String getFeatureDisplayName(List<GenotypeFeatureDTO> genotypeFeatureDTOList) {
        String displayName = "";
        if (genotypeFeatureDTOList == null || genotypeFeatureDTOList.size() == 0)
            return displayName;
        for (GenotypeFeatureDTO genotypeFeature : genotypeFeatureDTOList) {
            displayName += "<i>";
            if (genotypeFeature.getFeatureDTO().getDisplayNameForGenotypeBase() != null) {
                displayName += genotypeFeature.getFeatureDTO().getDisplayNameForGenotypeBase();
                displayName += "<sup>";
                displayName += genotypeFeature.getZygosity().getMutantZygosityDisplay(getDisplayFeatureName(genotypeFeature.getFeatureDTO().getName()));
                displayName += "</sup>";
            } else {
                displayName += genotypeFeature.getZygosity().getMutantZygosityDisplay(getDisplayFeatureName(genotypeFeature.getFeatureDTO().getName()));
            }
            displayName += "</i>";
            displayName += " ; ";
        }
        displayName = displayName.substring(0, displayName.length() - 3);
        return displayName;
    }

    private void setGenoBackgroundPanel(List<GenotypeDTO> backgroundGenotypeList) {
        backgroundGenotypePanel.clear();
        if (backgroundGenotypeList == null)
            return;
        for (GenotypeDTO genotype : backgroundGenotypeList) {
            Label name = new Label(genotype.getName());
            backgroundGenotypePanel.add(name);
            DeleteLink deleteLink = new DeleteLink("Delete Background Genotype");
            presenter.addDeleteGenotypeBackgroundClickHandler(deleteLink, genotype);
            backgroundGenotypePanel.add(deleteLink);
        }
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

    public void setPublicationID(String publicationID) {
        this.publicationID = publicationID;
    }

    public SimpleErrorElement getErrorLabel() {
        return errorLabel;
    }

    public Image getLoadingImage() {
        return loadingImage;
    }

    public String getPublicationID() {
        return publicationID;
    }

    public ShowHideWidget getGenotypeConstructionToggle() {
        return genotypeConstructionToggle;
    }

    public ListBox getFeatureForGenotypeListBox() {
        return featureForGenotypeListBox;
    }

    public void resetNewGentoypeUI() {
        genotypeDisplayName.setHTML("");
        genotypeHandle.setText("");

    }
}

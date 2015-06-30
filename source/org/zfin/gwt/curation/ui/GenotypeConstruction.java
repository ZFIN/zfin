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
import org.zfin.gwt.root.ui.SimpleErrorElement;
import org.zfin.gwt.root.ui.ZfinFlexTable;
import org.zfin.gwt.root.util.DeleteImage;
import org.zfin.gwt.root.util.ShowHideWidget;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Table of associated genotypes
 */
public class GenotypeConstruction extends Composite {

    private static MyUiBinder binder = GWT.create(MyUiBinder.class);


    @UiTemplate("GenotypeConstruction.ui.xml")
    interface MyUiBinder extends UiBinder<FlowPanel, GenotypeConstruction> {
    }

    public static final String UNRECOVERED = "unrecovered";
    public static final String UNSPECIFIED = "unspecified";

    @UiField
    Hyperlink showHideSection;
    @UiField
    ZfinFlexTable genotypeConstructionTable;
    @UiField
    VerticalPanel genotypeConstructionPanel;
    @UiField
    Button createGenotypeButton;
    @UiField
    Button resetButton;
    @UiField
    SimpleErrorElement errorLabel;
    @UiField
    Image loadingImage;
    @UiField
    InlineHTML genotypeDisplayName;
    @UiField
    TextBox genotypeNickname;
    @UiField
    Label genotypeHandle;
    @UiField
    ListBox backgroundListBox;
    @UiField
    Button buttonUUU;
    @UiField
    Button button211;
    @UiField
    Button button2UU;

    public GenotypeConstruction() {
        initWidget(binder.createAndBindUi(this));
        genotypeConstructionToggle = new ShowHideWidget(showHideSection, genotypeConstructionPanel);
    }

    GenotypeConstructionPresenter presenter;

    public void setPresenter(GenotypeConstructionPresenter presenter) {
        this.presenter = presenter;
    }

    private String publicationID;
    private ShowHideWidget genotypeConstructionToggle;
    private Button addGenotypeFeature = new Button("Add");
    private ListBox featureForGenotypeListBox = new ListBox();
    private ListBox zygosityListBox = new ListBox();
    private ListBox zygosityMaternalListBox = new ListBox();
    private ListBox zygosityPaternalListBox = new ListBox();


    @UiHandler("showHideSection")
    void onShowHideClick(@SuppressWarnings("unused") ClickEvent event) {
        presenter.onShowHideClick();
    }

    @UiHandler("createGenotypeButton")
    void onCreateFishButtonClick(@SuppressWarnings("unused") ClickEvent event) {
        presenter.onCreateGenotypeButtonClick();
    }

    @UiHandler("resetButton")
    void onResetButtonClick(@SuppressWarnings("unused") ClickEvent event) {
        presenter.onResetClick();
    }

    @UiHandler("backgroundListBox")
    void onBackgroundButtonClick(@SuppressWarnings("unused") ChangeEvent event) {
        presenter.onBackgroundClick();
    }

    @UiHandler("buttonUUU")
    void onUUUButtonClick(@SuppressWarnings("unused") ClickEvent event) {
        presenter.onUUUClick();
    }

    @UiHandler("button2UU")
    void on2UUButtonClick(@SuppressWarnings("unused") ClickEvent event) {
        presenter.on2UUClick();
    }

    @UiHandler("button211")
    void on211ButtonClick(@SuppressWarnings("unused") ClickEvent event) {
        presenter.on211Click();
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
        genotypeConstructionTable.setWidget(row, col, featureForGenotypeListBox);
        genotypeConstructionTable.getCellFormatter().setStyleName(row, col++, "bold");
        genotypeConstructionTable.setWidget(row, col, zygosityListBox);
        genotypeConstructionTable.getCellFormatter().setStyleName(row, col++, "bold");
        genotypeConstructionTable.setWidget(row, col, zygosityMaternalListBox);
        genotypeConstructionTable.getCellFormatter().setStyleName(row, col++, "bold");
        genotypeConstructionTable.setWidget(row, col, zygosityPaternalListBox);
        genotypeConstructionTable.getCellFormatter().setStyleName(row, col++, "bold");
        genotypeConstructionTable.setWidget(row, col, addGenotypeFeature);
        genotypeConstructionTable.getCellFormatter().setStyleName(row, col, "bold");
        genotypeConstructionTable.getRowFormatter().setStyleName(row, "table-header");
    }

    private Map<DeleteImage, GenotypeFeatureDTO> deleteGenoFeatureMap = new HashMap<>();

    public void updateGenotypeFeatureList(List<GenotypeFeatureDTO> genotypeFeatureDTOList, GenotypeDTO backgroundGenotype) {
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
            deleteGenoFeatureMap.put(delete, genotypeFeature);
            genotypeConstructionTable.setWidget(rowIndex, col, delete);
            groupIndex = genotypeConstructionTable.setRowStyle(rowIndex++, null, genotypeFeature.getZdbID(), groupIndex);
        }
        setGenotypeInfo(genotypeFeatureDTOList, backgroundGenotype);
        initGenotypeConstructionRow(rowIndex);
    }

    public void setGenotypeInfo(List<GenotypeFeatureDTO> genotypeFeatureDTOList, GenotypeDTO backgroundGenotype) {
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
        if (backgroundGenotype != null) {
            genotypeHandleName += backgroundGenotype.getName();
        }
        if (genotypeDisplayNameString.length() > 3)
            genotypeDisplayNameString = genotypeDisplayNameString.substring(0, genotypeDisplayNameString.length() - 3);
        genotypeHandle.setText(genotypeHandleName);
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

    public void setPublicationID(String publicationID) {
        this.publicationID = publicationID;
    }


    public Map<DeleteImage, GenotypeFeatureDTO> getDeleteGenoFeatureMap() {
        return deleteGenoFeatureMap;
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

    public ListBox getBackgroundListBox() {
        return backgroundListBox;
    }

    public Button getAddGenotypeFeature() {
        return addGenotypeFeature;
    }

    public ListBox getFeatureForGenotypeListBox() {
        return featureForGenotypeListBox;
    }

    public ListBox getZygosityListBox() {
        return zygosityListBox;
    }

    public ListBox getZygosityMaternalListBox() {
        return zygosityMaternalListBox;
    }

    public ListBox getZygosityPaternalListBox() {
        return zygosityPaternalListBox;
    }

    public Label getGenotypeHandle() {
        return genotypeHandle;
    }

    public TextBox getGenotypeNickname() {
        return genotypeNickname;
    }

    public void resetNewGentoypeUI() {
        genotypeNickname.setText("");
        genotypeDisplayName.setHTML("");
        genotypeHandle.setText("");

    }
}

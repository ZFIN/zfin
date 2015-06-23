package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
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
    ZfinFlexTable newGenotypeInfoTable;
    @UiField
    Button createGenotypeButton;
    @UiField
    SimpleErrorElement errorLabel;
    @UiField
    Image loadingImage;



    public GenotypeConstruction() {
        initWidget(binder.createAndBindUi(this));
        genotypeConstructionToggle = new ShowHideWidget(showHideSection, genotypeConstructionPanel);
        createGenotypeButton.setText("Create Genotype");
    }

    private String publicationID;
    private ShowHideWidget genotypeConstructionToggle;
    ListBox backgroundListBox = new ListBox();
    private Button addGenotypeFeature = new Button("Add");
    private ListBox featureForGenotypeListBox = new ListBox();
    private ListBox zygosityListBox = new ListBox();
    private ListBox zygosityMaternalListBox = new ListBox();
    private ListBox zygosityPaternalListBox = new ListBox();
    private Button buttonUUU = new Button("U,U,U");
    private Button button211 = new Button("2,1,1");
    private Button button2UU = new Button("2,U,U");
    private TextBox genotypeNickname = new TextBox();
    private InlineHTML genotypeDisplayName = new InlineHTML();
    private Label genotypeHandle = new Label();


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
        genotypeConstructionTable.setWidget(row, col, backgroundListBox);
    }

    private Map<DeleteImage, GenotypeFeatureDTO> deleteGenoeFeatureMap = new HashMap<>(); 
    
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
            deleteGenoeFeatureMap.put(delete, genotypeFeature);
            genotypeConstructionTable.setWidget(rowIndex, col++, delete);
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
        genotypeDisplayNameString = genotypeDisplayNameString.substring(0, genotypeDisplayNameString.length() - 3);
        genotypeHandle.setText(genotypeHandleName);
        genotypeDisplayName.setHTML(SafeHtmlUtils.fromTrustedString(genotypeDisplayNameString));
        genotypeDisplayName.setHTML(SafeHtmlUtils.fromTrustedString(genotypeDisplayNameString));
        genotypeNickname.setText(genotypeHandleName);
    }

    public void initNewGenotypeInfo() {
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


    public Map<DeleteImage, GenotypeFeatureDTO> getDeleteGenoeFeatureMap() {
        return deleteGenoeFeatureMap;
    }

    public SimpleErrorElement getErrorLabel() {
        return errorLabel;
    }

    public VerticalPanel getGenotypeConstructionPanel() {
        return genotypeConstructionPanel;
    }

    public Image getLoadingImage() {
        return loadingImage;
    }

    public String getPublicationID() {
        return publicationID;
    }

    public Hyperlink getShowHideSection() {
        return showHideSection;
    }

    public ShowHideWidget getGenotypeConstructionToggle() {
        return genotypeConstructionToggle;
    }

    public Button getCreateGenotypeButton() {
        return createGenotypeButton;
    }

    public ListBox getBackgroundListBox() {
        return backgroundListBox;
    }

    public void setBackgroundListBox(ListBox backgroundListBox) {
        this.backgroundListBox = backgroundListBox;
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

    public Button getButton211() {
        return button211;
    }

    public Button getButton2UU() {
        return button2UU;
    }

    public Button getButtonUUU() {
        return buttonUUU;
    }

    public InlineHTML getGenotypeDisplayName() {
        return genotypeDisplayName;
    }

    public Label getGenotypeHandle() {
        return genotypeHandle;
    }

    public TextBox getGenotypeNickname() {
        return genotypeNickname;
    }

    public void resetNewGentoypeUI() {
        genotypeNickname = new TextBox();
        genotypeDisplayName = new InlineHTML();
        genotypeHandle = new Label();

    }

}

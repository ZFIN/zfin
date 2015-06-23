package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.root.dto.GenotypeDTO;
import org.zfin.gwt.root.dto.RelatedEntityDTO;
import org.zfin.gwt.root.ui.SimpleErrorElement;
import org.zfin.gwt.root.ui.ZfinFlexTable;
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
    ZfinFlexTable constructionTable;
    @UiField
    Button createFishButton;
    @UiField
    Button resetGUI;
    @UiField
    SimpleErrorElement errorLabel;
    @UiField
    Image loadingImage;

    private String publicationID;
    private ShowHideWidget constructionToggle;
    private ListBox genotypeSelectionBox = new ListBox();
    private ListBox strSelectionBox = new ListBox();
    private HorizontalPanel sTRPanel = new HorizontalPanel();
    private Button addStrButton = new Button("Add STR");

    private void initConstructionTableHeader() {
        constructionTable.setText(0, 0, "Genotype");
        constructionTable.getCellFormatter().setStyleName(0, 0, "bold");
        constructionTable.setText(0, 1, "ST Reagent");
        constructionTable.getCellFormatter().setStyleName(0, 1, "bold");
        constructionTable.getRowFormatter().setStyleName(0, "table-header");
    }

    public void updateConstructionTable(GenotypeDTO newGenotype, Set<RelatedEntityDTO> newStrList) {
        constructionTable.clear();
        initConstructionTableHeader();
        HorizontalPanel genoPanel = new HorizontalPanel();
        if (newGenotype != null)
            genoPanel.add(new InlineHTML(newGenotype.getName()));
        //genoPanel.setSpacing(5);
        constructionTable.setWidget(1, 0, genoPanel);
        constructionTable.setWidget(1, 1, getStrPanel(newStrList));
        if (newGenotype != null)
            constructionTable.setRowStyle(1, null, newGenotype.getZdbID(), 0);
        initConstructionRow();
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

    private Widget getStrPanel(final Set<RelatedEntityDTO> newStrList) {
        if (newStrList == null || newStrList.size() == 0)
            return sTRPanel;
        sTRPanel = new HorizontalPanel();
        sTRPanel.setSpacing(5);
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
        return sTRPanel;
    }


    private Map<Anchor, RelatedEntityDTO> strAnchorMap = new HashMap<>(4);

    public Map<Anchor, RelatedEntityDTO> getStrAnchorMap() {
        return strAnchorMap;
    }

    public void setPublicationID(String publicationID) {
        this.publicationID = publicationID;
    }

    public VerticalPanel getConstructionPanel() {
        return constructionPanel;
    }

    public ZfinFlexTable getConstructionTable() {
        return constructionTable;
    }

    public ShowHideWidget getConstructionToggle() {
        return constructionToggle;
    }

    public Button getCreateFishButton() {
        return createFishButton;
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

    public HorizontalPanel getsTRPanel() {
        return sTRPanel;
    }


    public Button getAddStrButton() {
        return addStrButton;
    }

    public Hyperlink getShowHideConstruction() {
        return showHideConstruction;
    }
}

package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.UIObject;
import org.zfin.gwt.root.ui.ListBoxWrapper;
import org.zfin.gwt.root.util.StringUtils;

/**
 * Filter bar aka banana bar.
 * This bar contains three filter elements: figure, gene and fish.
 * Setting a value filters out only experiments and expressions with
 * those characteristics. The filter bar is always visible.
 * <p/>
 * General: Selecting a non-default value will set the background color of the list box to red to make
 * it very visible to the user that he filtered the records. Setting it back to 'ALL' will remove the background
 * color. Any change to one of the filter elements will re-read the appropriate records.
 * <p/>
 * Life cycle: The filter values have a certain life cycle, i.e. they are remembered as follows:
 * A) Figure: saved in the database and stored forever
 * B) Fish and Gene: The values are stored in the user session and thus are lost after session timeout or logging out.
 * Reloading the page or coming back to the FX page will prepopulate the filter with the values that are available
 * at that point in time.
 * <p/>
 * 1) Only Fig: Selecting a Figure applies only to expressions as only they are associated with figures.
 * The list of figure annotations is reread.
 * 2) Only Gene: Selecting a gene applies to both sections and displays only record with experiments that
 * have the selected gene associated.
 * 3) Only Fish: Selecting a applies applies to both sections and displays only record with experiments that
 * have the selected fish associated.
 * 4) Reset: Clicking the button will set all three filter elements to their default (='ALL') and re-read both
 * section.
 */
public class CurationFilterView extends Composite {

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiTemplate("CurationFilterModule.ui.xml")
    interface MyUiBinder extends UiBinder<HorizontalPanel, CurationFilterView> {
    }

    public static final String ALL = "ALL";

    // filter bar (aka banana bar)
    @UiField
    ListBoxWrapper geneList;
    @UiField
    ListBoxWrapper fishList;
    @UiField
    ListBoxWrapper figureList;
    @UiField
    Button resetButton;

    private CurationFilterPresenter curationFilterPresenter;

    public CurationFilterView() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @UiHandler("figureList")
    void onChangeFigureList(@SuppressWarnings("unused") ChangeEvent event) {
        curationFilterPresenter.applyFigureListChange(figureList);
    }

    @UiHandler("geneList")
    void onChangeGeneList(@SuppressWarnings("unused") ChangeEvent event) {
        curationFilterPresenter.applyGeneListChange(geneList);
    }

    @UiHandler("fishList")
    void onChangeFishList(@SuppressWarnings("unused") ChangeEvent event) {
        curationFilterPresenter.applyFishListChange(fishList);
    }

    @UiHandler("resetButton")
    void onResetClick(@SuppressWarnings("unused") ClickEvent event) {
        if (isOneOrMOreFilterSet())
            curationFilterPresenter.reset();
        figureList.setSelectedIndex(0);
        geneList.setSelectedIndex(0);
        fishList.setSelectedIndex(0);
        setBackgroundColorForListBox(null, fishList);
        setBackgroundColorForListBox(null, geneList);
        setBackgroundColorForListBox(null, figureList);
    }

    private boolean isOneOrMOreFilterSet() {
        boolean resetNeeded = false;
        if (figureList.getSelectedIndex() > 0)
            resetNeeded = true;
        if (geneList.getSelectedIndex() > 0)
            resetNeeded = true;
        if (fishList.getSelectedIndex() > 0)
            resetNeeded = true;
        return resetNeeded;
    }


    public void setCurationFilterPresenter(CurationFilterPresenter curationFilterPresenter) {
        this.curationFilterPresenter = curationFilterPresenter;
    }

    public void setBackgroundColorForListBox(String id, UIObject list) {
        if (StringUtils.isEmpty(id))
            list.setStyleName("list-box-unselected");
        else
            list.setStyleName("list-box-selected");
    }


    public ListBoxWrapper getFishList() {
        return fishList;
    }

    public ListBoxWrapper getGeneList() {
        return geneList;
    }

    public ListBoxWrapper getFigureList() {
        return figureList;
    }

}

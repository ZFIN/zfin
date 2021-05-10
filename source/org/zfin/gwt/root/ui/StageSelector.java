package org.zfin.gwt.root.ui;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.root.dto.StageDTO;
import org.zfin.gwt.root.util.WidgetUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Stage Selection GUI:
 * 1) select a start and an end stage
 * 2) Select multiple stages with implied end stage = start stage
 * 3) Contains convenience methods for accessing selected stages and reset the GUI.
 * <p/>
 * Usage:
 * 1) Instantiate this composite
 * 2) set the list of stages for start and end stage list box
 */
public class StageSelector extends VerticalPanel {

    protected static final String SELECT_MULTIPLE_STAGES = "Select Multiple Stages";
    protected static final String SELECT_SINGLE_STAGE = "Select a Stage Range";
    protected static final String STAGE_RANGE = "Stage Range";
    protected static final String MULTIPLE_STAGES = "Multiple Stages";
    protected static final String START = "Start:";
    protected static final String END = "End:";

    protected HorizontalPanel startStage = new HorizontalPanel();
    protected HorizontalPanel endStage = new HorizontalPanel();
    protected ListBox startStageList = new ListBox();
    protected ListBox endStageList = new ListBox();
    protected ListBox multiStartStageList = new ListBox(true);
    protected Button multiStageButton = new Button(SELECT_MULTIPLE_STAGES);
    protected Label panelTitle;
    protected HTML multiStageLabel;
    protected HorizontalPanel toggleRow = new HorizontalPanel();

    protected String publicationID;
    protected SessionSaveServiceAsync sessionRPC = SessionSaveService.App.getInstance();


    public StageSelector() {
        initGui();
    }

    public final void initGui() {

        multiStartStageList.setVisibleItemCount(6);
        panelTitle = new Label(STAGE_RANGE);
        panelTitle.setStyleName(WidgetUtil.BOLD);
        add(panelTitle);
        startStage.add(new Label(START));
        startStage.add(WidgetUtil.getNbsp());
        startStage.add(startStageList);
        endStage.add(new Label(END));
        endStage.add(WidgetUtil.getNbsp());
        endStage.add(endStageList);
        add(startStage);
        add(endStage);
        add(multiStartStageList);
        multiStageLabel = new HTML("&nbsp; or ... &nbsp;");
        toggleRow.add(multiStageLabel);
        toggleRow.add(multiStageButton);
        add(toggleRow);
        addHandlers();
        setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
        setGuiToDefault();
    }

    public void addHandlers() {
        startStageList.addChangeHandler(new StartStageChangeHandler());
        multiStageButton.addClickHandler(new MultiStageButtonClickHandler());
        setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
        setGuiToDefault();
    }

    public void setPublicationID(String publicationID) {
        this.publicationID = publicationID;
    }

    /**
     * Select the checked start stage in the selection box.
     *
     * @param selectedStageID stage ID to be selected
     */
    public void selectStartStage(String selectedStageID) {
        int totalStages = startStageList.getItemCount();
        for (int index = 0; index < totalStages; index++) {
            String value = startStageList.getValue(index);
            if (value.equals(selectedStageID))
                startStageList.setSelectedIndex(index);
        }
    }

    /**
     * Select the checked end stage in the selection box.
     *
     * @param selectedStageID stage ID
     */
    public void selectEndStage(String selectedStageID) {
        int totalStages = endStageList.getItemCount();
        for (int index = 0; index < totalStages; index++) {
            String value = endStageList.getValue(index);
            if (value.equals(selectedStageID))
                endStageList.setSelectedIndex(index);
        }
    }

    public String getSelectedStartStageID() {
        return startStageList.getValue(startStageList.getSelectedIndex());
    }

    public String getSelectedEndStageID() {
        return endStageList.getValue(endStageList.getSelectedIndex());
    }


    /**
     * Reset stage selection to default after adding new expression records.
     */
    public void setDefaultStageSelection() {
        startStage.setVisible(true);
        endStage.setVisible(true);
        multiStartStageList.setVisible(false);
        multiStageButton.setText(SELECT_MULTIPLE_STAGES);
        panelTitle.setText(STAGE_RANGE);
    }

    public boolean isDualStageMode() {
        return startStage.isVisible();
    }

    public boolean isMultiStageMode() {
        return !startStage.isVisible();
    }


    /**
     * Stage IDs selected from the multi stage selection box.
     *
     * @return list of stage ids
     */
    public List<String> getSelectedStageIDs() {
        List<String> ids = new ArrayList<String>(5);
        int stageCount = multiStartStageList.getItemCount();
        for (int index = 0; index < stageCount; index++) {
            if (multiStartStageList.isItemSelected(index)) {
                String startStageID = multiStartStageList.getValue(index);
                ids.add(startStageID);
            }
        }
        return ids;
    }

    public boolean isMultiStageSelected() {
        return !getSelectedStageIDs().isEmpty();
    }

    /**
     * Set the stage GUI to the default values.
     */
    public void setGuiToDefault() {
        if (isDualStageMode())
            setSingleStageMode();
        else
            setMultiStageMode();
        resetGui();
    }

    public String validDualStageSelection() {
        if (startStageList.getSelectedIndex() > endStageList.getSelectedIndex()) {
            return "Selected Start Stage comes after selected End Stage! Please correct your choices.";
        }
        return null;
    }

    public String validMultiStageSelection() {
        if (multiStartStageList.getSelectedIndex() < 0) {
            return "No stage selected. Please select at least one stage.";
        }
        return null;
    }

    /**
     * Sets the items in the start and end stage list.
     * Start and end stages are the same
     *
     * @param stages list of stages
     */
    public void setStageList(List<StageDTO> stages) {
        startStageList.clear();
        endStageList.clear();
        multiStartStageList.clear();
        for (StageDTO stageDTO : stages) {
            startStageList.addItem(stageDTO.getDisplay(), stageDTO.getZdbID());
            endStageList.addItem(stageDTO.getDisplay(), stageDTO.getZdbID());
            multiStartStageList.addItem(stageDTO.getDisplay(), stageDTO.getZdbID());
        }
    }

    public HorizontalPanel getStartStagePanel() {
        return startStage;
    }

    public HorizontalPanel getEndStagePanel() {
        return endStage;
    }

    public HorizontalPanel getTogglePanel() {
        return toggleRow;
    }

    public Label getPanelTitle() {
        return panelTitle;
    }

    public ListBox getMultiStagePanel() {
        return multiStartStageList;
    }

    protected void setMultiStageMode() {
        startStage.setVisible(false);
        endStage.setVisible(false);
        multiStartStageList.setVisible(true);
        multiStageButton.setText(SELECT_SINGLE_STAGE);
        panelTitle.setText(MULTIPLE_STAGES);
    }

    protected void setSingleStageMode() {
        multiStageButton.setText(SELECT_MULTIPLE_STAGES);
        panelTitle.setText(STAGE_RANGE);
        multiStartStageList.setVisible(false);
        startStage.setVisible(true);
        endStage.setVisible(true);
    }

    public void resetGui() {
        startStageList.setSelectedIndex(0);
        endStageList.setSelectedIndex(0);
        multiStartStageList.setSelectedIndex(-1);
    }

    public enum Mode {
        SINGLE_MODE, MULTI_MODE
    }

    public void hideMultiSelect() {
        multiStageButton.setVisible(false);
        multiStageLabel.setVisible(false);
        
    }

// ************  Event Handlers ***********************************************************

    private class StartStageChangeHandler implements ChangeHandler {

        public void onChange(ChangeEvent event) {
            int startStageIndex = startStageList.getSelectedIndex();
            // always set end stage = start stage when changing start stage.
            endStageList.setSelectedIndex(startStageIndex);
        }
    }

    private class MultiStageButtonClickHandler implements ClickHandler {

        public void onClick(ClickEvent event) {
            if (startStage.isVisible()) {
                setMultiStageMode();
                sessionRPC.setStageSelectorSingleMode(false, publicationID, new ZfinAsyncCallback<Void>(null, null));
            } else {
                setSingleStageMode();
                sessionRPC.setStageSelectorSingleMode(true, publicationID, new ZfinAsyncCallback<Void>(null, null));
            }
        }

    }


}

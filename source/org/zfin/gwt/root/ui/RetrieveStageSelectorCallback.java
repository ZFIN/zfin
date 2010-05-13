package org.zfin.gwt.root.ui;

/**
 * Call back class after checking which stage selector should be used.
 * The callback class sets the stage selector to the value set in session.
 */
public class RetrieveStageSelectorCallback extends ZfinAsyncCallback<Boolean> {

    private StageSelector stageSelector;

    public RetrieveStageSelectorCallback(ErrorHandler errorLabel, StageSelector stageSelector) {
        super("Error while reading Stage selector mode ", errorLabel);
        this.stageSelector = stageSelector;
    }

    @Override
    public void onSuccess(Boolean isSingleStageMode) {
        if (isSingleStageMode)
            stageSelector.setSingleStageMode();
        else
            stageSelector.setMultiStageMode();
    }

}

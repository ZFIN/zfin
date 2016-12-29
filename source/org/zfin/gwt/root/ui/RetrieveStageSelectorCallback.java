package org.zfin.gwt.root.ui;

import org.zfin.gwt.root.event.AjaxCallEventType;

/**
 * Call back class after checking which stage selector should be used.
 * The callback class sets the stage selector to the value set in session.
 */
public class RetrieveStageSelectorCallback extends ZfinAsyncCallback<Boolean> {

    private StageSelector stageSelector;

    public RetrieveStageSelectorCallback(ErrorHandler errorLabel, StageSelector stageSelector, ZfinModule module, AjaxCallEventType type) {
        super("Error while reading Stage selector mode ", errorLabel, module, type);
        this.stageSelector = stageSelector;
    }

    @Override
    public void onSuccess(Boolean isSingleStageMode) {
        super.onFinish();
        if (isSingleStageMode)
            stageSelector.setSingleStageMode();
        else
            stageSelector.setMultiStageMode();
    }

}

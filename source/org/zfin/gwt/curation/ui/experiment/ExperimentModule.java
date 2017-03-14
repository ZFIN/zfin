package org.zfin.gwt.curation.ui.experiment;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;
import org.zfin.gwt.curation.event.CurationEvent;
import org.zfin.gwt.curation.event.EventType;
import org.zfin.gwt.curation.ui.CurationTab;
import org.zfin.gwt.curation.ui.ZfinCurationModule;
import org.zfin.gwt.curation.ui.feature.FeatureModule;
import org.zfin.gwt.root.event.SelectAutoCompleteEvent;
import org.zfin.gwt.root.event.SelectAutoCompleteEventHandler;
import org.zfin.gwt.root.ui.ZfinModule;
import org.zfin.gwt.root.util.AppUtils;

/**
 * Entry point for Experiment curation module.
 */
public class ExperimentModule implements ZfinCurationModule {

    public static final String EXPERIMENT_TAB = "experimentTab";
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiTemplate("ExperimentModule.ui.xml")
    interface MyUiBinder extends UiBinder<FlowPanel, ExperimentModule> {
    }

    // data
    private String publicationID;
    private boolean debug;
    @UiField
    ExperimentAddView experimentAddView;
    @UiField
    ConditionAddView conditionAddView;

    private ConditionAddPresenter conditionPresenter;
    private ExperimentAddPresenter addExperimentPresenter;

    public ExperimentModule(String publicationID) {
        this.publicationID = publicationID;
        init();
    }

    @Override
    public void init() {
        FlowPanel outer = uiBinder.createAndBindUi(this);
        RootPanel.get(EXPERIMENT_TAB).add(outer);

        addExperimentPresenter = new ExperimentAddPresenter(experimentAddView, publicationID);
        experimentAddView.setPresenter(addExperimentPresenter);
        addExperimentPresenter.go();

        conditionPresenter = new ConditionAddPresenter(conditionAddView, publicationID);
        conditionAddView.setPresenter(conditionPresenter);
        conditionPresenter.go();

        bindEventBusHandler();

    }


    @Override
    public void refresh() {
        conditionPresenter.go();
        addExperimentPresenter.go();
    }

    @Override
    public void handleCurationEvent(CurationEvent event) {
        if (event.getEventType().is(EventType.CUD_EXPERIMENT_CONDITION))
            addExperimentPresenter.go();
        if (event.getEventType().is(EventType.CREATE_EXPERIMENT) ||
                event.getEventType().is(EventType.UPDATE_EXPERIMENT))
            conditionPresenter.updateExperimentList();
        if (event.getEventType().is(EventType.REMOVE_EXPERIMENT))
            conditionPresenter.go();
    }

    @Override
    public void handleTabToggle() {

    }

    @Override
    public void updateTermInfo(String termName, String ontologyName) {
        conditionPresenter.updateTermInfoBox(termName, ontologyName);
    }

    private void bindEventBusHandler() {
        AppUtils.EVENT_BUS.addHandler(SelectAutoCompleteEvent.TYPE,
                new SelectAutoCompleteEventHandler() {
                    @Override
                    public void onSelect(SelectAutoCompleteEvent event) {
                        conditionPresenter.onTermSelectEvent(event);
                    }
                });
    }



    static ZfinModule getModuleInfo(){
        return new ZfinModule(CurationTab.EXPERIMENT.getName(), FeatureModule.class.getName());
    }
}

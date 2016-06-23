package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;
import org.zfin.gwt.curation.event.AddNewExperimentEvent;
import org.zfin.gwt.curation.event.AddNewExperimentEventHandler;
import org.zfin.gwt.curation.event.UpdateExperimentEvent;
import org.zfin.gwt.curation.event.UpdateExperimentEventHandler;
import org.zfin.gwt.root.dto.RelatedEntityDTO;
import org.zfin.gwt.root.event.SelectAutoCompleteEvent;
import org.zfin.gwt.root.event.SelectAutoCompleteEventHandler;
import org.zfin.gwt.root.util.AppUtils;

/**
 * Entry point for Experiment curation module.
 */
public class ExperimentModule implements EntryPoint {

    public static final String EXPERIMENT_TAB = "experimentTab";
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiTemplate("ExperimentModule.ui.xml")
    interface MyUiBinder extends UiBinder<FlowPanel, ExperimentModule> {
    }

    // data
    private String publicationID;
    private boolean debug;
    @UiField
    AttributionModule attributionModule;
    @UiField
    ExperimentAddView experimentAddView;
    @UiField
    ConditionAddView conditionAddView;

    private ConditionAddPresenter conditionPresenter;
    private ExperimentAddPresenter addExperimentPresenter;

    public ExperimentModule(String publicationID) {
        this.publicationID = publicationID;
        onModuleLoad();
    }

    @Override
    public void onModuleLoad() {
        FlowPanel outer = uiBinder.createAndBindUi(this);
        RootPanel.get(EXPERIMENT_TAB).add(outer);

        RelatedEntityDTO relatedEntityDTO = new RelatedEntityDTO();
        relatedEntityDTO.setPublicationZdbID(publicationID);
        attributionModule.setDTO(relatedEntityDTO);
        ExperimentAddPresenter addExperimentPresenter = new ExperimentAddPresenter(experimentAddView, publicationID);
        experimentAddView.setPresenter(addExperimentPresenter);
        addExperimentPresenter.go();

        conditionPresenter = new ConditionAddPresenter(conditionAddView, publicationID);
        conditionAddView.setPresenter(conditionPresenter);
        conditionPresenter.go();

        bindEventBusHandler();

    }

    private void bindEventBusHandler() {
        AppUtils.EVENT_BUS.addHandler(SelectAutoCompleteEvent.TYPE,
                new SelectAutoCompleteEventHandler() {
                    @Override
                    public void onSelect(SelectAutoCompleteEvent event) {
                        conditionPresenter.onTermSelectEvent(event);
                    }
                });
        AppUtils.EVENT_BUS.addHandler(AddNewExperimentEvent.TYPE,
                new AddNewExperimentEventHandler() {
                    @Override
                    public void onAdd(AddNewExperimentEvent event) {
                        conditionPresenter.go();
                    }

                });
        AppUtils.EVENT_BUS.addHandler(UpdateExperimentEvent.TYPE,
                new UpdateExperimentEventHandler() {
                    @Override
                    public void onUpdate(UpdateExperimentEvent event) {
                        conditionPresenter.go();
                    }

                });
    }


}

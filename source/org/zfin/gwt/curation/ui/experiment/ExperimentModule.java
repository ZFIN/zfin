package org.zfin.gwt.curation.ui.experiment;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import org.zfin.gwt.curation.ui.RemoveAttributeEvent;
import org.zfin.gwt.curation.ui.RemoveAttributeEventHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;
import org.zfin.gwt.curation.event.*;
import org.zfin.gwt.curation.ui.AttributionModule;
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
    private AttributionModule attributionModule = new AttributionModule();
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
        addExperimentPresenter = new ExperimentAddPresenter(experimentAddView, publicationID);
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
        AppUtils.EVENT_BUS.addHandler(ChangeExperimentEvent.TYPE,
                new ChangeExperimentEventHandler() {
                    @Override
                    public void onAdd(ChangeExperimentEvent event) {
                        conditionPresenter.updateExperimentList();
                    }
                    public void onUpdate(ChangeExperimentEvent event) {
                        conditionPresenter.updateExperimentList();
                    }
                    public void onDelete(ChangeExperimentEvent event) {
                        conditionPresenter.go();
                    }

                });

        AppUtils.EVENT_BUS.addHandler(ChangeConditionEvent.TYPE,
                new ChangeConditionEventHandler() {
                    @Override
                    public void onChange(ChangeConditionEvent event) {
                        addExperimentPresenter.go();
                    }

                });

        AppUtils.EVENT_BUS.addHandler(AddAttributeEvent.TYPE,
                new AddAttributeEventHandler() {
                    @Override
                    public void onEvent(AddAttributeEvent event) {
                        attributionModule.populateAttributeRemoval();
                    }
                });
        AppUtils.EVENT_BUS.addHandler(RemoveAttributeEvent.TYPE,
                new RemoveAttributeEventHandler() {
                    @Override
                    public void onRemoveAttribute(RemoveAttributeEvent event) {
                        attributionModule.populateAttributeRemoval();
                    }
                });

    }


}

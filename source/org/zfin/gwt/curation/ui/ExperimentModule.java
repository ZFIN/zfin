package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;
import org.zfin.gwt.curation.event.*;
import org.zfin.gwt.root.dto.RelatedEntityDTO;
import org.zfin.gwt.root.dto.TermDTO;
import org.zfin.gwt.root.ui.HandlesError;
import org.zfin.gwt.root.ui.SimpleErrorElement;
import org.zfin.gwt.root.ui.TermEntry;
import org.zfin.gwt.root.ui.TermInfoComposite;
import org.zfin.gwt.root.util.AppUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Entry point for Experiment curation module.
 */
public class ExperimentModule implements  EntryPoint {

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

        bindEventBusHandler();

    }

    private void bindEventBusHandler() {
      /*  AppUtils.EVENT_BUS.addHandler(AddNewExperimentEvent.TYPE,
                new AddNewExperimentEventHandler() {
                    @Override
                    public void onAdd(AddNewFeatureEvent event) {
                        *//*featureEditPresenter.loadFeaturesForPub(true);
                        featureRelationshipPresenter.onFeatureAddEvent();
                        attributionModule.populateAttributeRemoval();*//*
                    }
                });
*/


    }


}

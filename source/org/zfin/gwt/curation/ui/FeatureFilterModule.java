package org.zfin.gwt.curation.ui;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import org.zfin.gwt.curation.event.FilterChangeEvent;
import org.zfin.gwt.curation.event.FilterChangeListener;
import org.zfin.gwt.root.dto.FeatureDTO;
import org.zfin.gwt.root.dto.FeatureMarkerRelationshipDTO;
import org.zfin.gwt.root.dto.FeatureTypeEnum;
import org.zfin.gwt.root.ui.AbstractComposite;
import org.zfin.gwt.root.ui.FeatureEditCallBack;
import org.zfin.gwt.root.ui.HandlesError;
import org.zfin.gwt.root.ui.StringListBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 */
public class FeatureFilterModule extends AbstractComposite<FeatureDTO>{

    public static final String FILTER_FEATURE_NAME = "FILTER_FEATURE_NAME";
    public static final String FILTER_FEATURE_TYPE = "FILTER_FEATURE_TYPE";
    public static final String ALL = "ALL";

    private Grid grid = new Grid(1,3) ;
    private HTML showHTML = new HTML("SHOW: ") ;
    private HorizontalPanel featureNamePanel = new HorizontalPanel() ;
    private HTML featureNameHTML = new HTML("Feature Name:") ;
    private HorizontalPanel featureTypePanel = new HorizontalPanel() ;
    private HTML featureTypeHTML = new HTML("Feature Type:") ;
    private StringListBox featureNameList = new StringListBox() ;
    private StringListBox featureTypeList = new StringListBox() ;

    private List<FilterChangeListener> filterChangeListeners = new ArrayList<FilterChangeListener>() ;




    public FeatureFilterModule(){
        initWidget(grid);
        initGUI();
        setValues() ;
        addInternalListeners(this) ;
    }

    protected void setValues() {}

    protected void initGUI() {
        grid.setStyleName("curation-filter");
        grid.setWidth("100%");

        grid.setWidget(0,0,showHTML);
        featureNamePanel.add(featureNameHTML);
        featureNamePanel.add(featureNameList);
        grid.setWidget(0,1,featureNamePanel);
        featureTypePanel.add(featureTypeHTML);
        featureTypePanel.add(featureTypeList);
        grid.setWidget(0,2,featureTypePanel);
    }

    @Override
    protected void revertGUI() {
        FeatureRPCService.App.getInstance().getFeaturesMarkerRelationshipsForPub(dto.getPublicationZdbID(),
                new FeatureEditCallBack<List<FeatureMarkerRelationshipDTO>>("Problem finding features for pub: " + dto.getPublicationZdbID()+ " ",this) {
                    @Override
                    public void onSuccess(List<FeatureMarkerRelationshipDTO> featureMarkerRelationshipList) {
                        if(featureMarkerRelationshipList!=null){
                            Map<String, FeatureTypeEnum> featureTypes = new TreeMap<String,FeatureTypeEnum>() ;
                            featureNameList.clear();
                            featureNameList.addItem(ALL,null);
                            for(FeatureMarkerRelationshipDTO featureMarkerRelationshipDTO : featureMarkerRelationshipList){
                                FeatureDTO featureDTO = featureMarkerRelationshipDTO.getFeatureDTO();
                                featureNameList.addItem(featureDTO.getName(),featureDTO.getZdbID());
                                featureTypes.put(featureDTO.getFeatureType().getDisplay(),featureDTO.getFeatureType()) ;
                            }

                            featureTypeList.clear();
                            featureTypeList.addItem(ALL,null);
                            for(String type: featureTypes.keySet()){
                                featureTypeList.addItem(type,type);
                            }
                            refilter();
                        }
                    }
                });
    }

    private FilterChangeEvent getFilterChangeEventFromGui(){
        FilterChangeEvent filterChangeEvent = new FilterChangeEvent();

        if(featureNameList.getItemCount()==0) return filterChangeEvent; 

        if(!featureNameList.getSelectedText().equals(FeatureFilterModule.ALL)){
            filterChangeEvent.put(FILTER_FEATURE_NAME,featureNameList.getSelectedText());
        }
        if(!featureTypeList.getSelectedText().equals(FeatureFilterModule.ALL)){
            filterChangeEvent.put(FILTER_FEATURE_TYPE,featureTypeList.getSelectedText());
        }

        return filterChangeEvent;
    }

    @Override
    protected void addInternalListeners(HandlesError handlesError) {
        featureNameList.addChangeHandler(new ChangeHandler(){
            @Override
            public void onChange(ChangeEvent event) {
                refilter();
            }
        });

        featureTypeList.addChangeHandler(new ChangeHandler(){
            @Override
            public void onChange(ChangeEvent event) {
                refilter();
            }
        });
    }

    public void addFilterChangeListener(FilterChangeListener filterChangeListener){
        filterChangeListeners.add(filterChangeListener) ;
    }

    public void refilter(){
        fireFilterChangedEvent(getFilterChangeEventFromGui()) ;
    }

    private void fireFilterChangedEvent(FilterChangeEvent event) {
        for(FilterChangeListener filterChangeListener : filterChangeListeners){
            filterChangeListener.changed(event) ;
        }
    }

}

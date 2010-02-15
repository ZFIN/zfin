package org.zfin.gwt.marker.ui;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.marker.event.DirectAttributionListener;
import org.zfin.gwt.marker.event.PublicationChangeEvent;
import org.zfin.gwt.marker.event.PublicationChangeListener;
import org.zfin.gwt.root.dto.PublicationDTO;
import org.zfin.gwt.root.ui.StringListBox;
import org.zfin.gwt.root.util.LookupService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 */
public class PublicationLookupBox extends Composite implements DirectAttributionListener{

    private final StringListBox curatorPubList = new StringListBox();
    private final TextBox pubField = new TextBox();
    private final VerticalPanel panel = new VerticalPanel();
    private final VerticalPanel lookupPanel = new VerticalPanel();
    private final PublicationDisplayPanel publicationDisplayPanel = new PublicationDisplayPanel();
    private static final String SEPARATOR = "-------------" ;

    private final List<PublicationChangeListener> publicationChangeListeners = new ArrayList<PublicationChangeListener>();
    private static final int MAX_LENGTH = 30;

    // internal data
    private PublicationDTO publicationAbstractDTO;

    public PublicationLookupBox() {
        initGUI();
        addInternalListeners();
        initWidget(panel);

        RootPanel.get(StandardMarkerDivNames.publicationLookupDiv).add(this);

        // this won't run
        DeferredCommand.addCommand(new Command() {
            public void execute() {
                publicationChanged(new PublicationChangeEvent(curatorPubList.getValue(0)));
            }
        });
    }

    protected void addInternalListeners(){
        // default items
//        curatorPubList.addItem("No default pubs");
        curatorPubList.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                String selectedPub = curatorPubList.getValue(curatorPubList.getSelectedIndex());
                if (false == selectedPub.startsWith(PublicationValidator.ZDB_PUB_PREFIX)) {
                    selectedPub = "";
                }
                pubField.setText(selectedPub);
                publicationChanged(new PublicationChangeEvent(pubField.getText().trim()));
            }
        });

        pubField.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                publicationChanged(new PublicationChangeEvent(pubField.getText().trim()));
            }
        });
        pubField.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                publicationChanged(new PublicationChangeEvent(pubField.getText().trim()));
            }
        });

    }


    void initGUI() {
        panel.setWidth("300px");
        panel.setStyleName("publicationlookup");
        HTML defaultPublicationLabel = new HTML("<b>Default Publication</b>");
        publicationDisplayPanel.setWidth("300px");
        lookupPanel.add(defaultPublicationLabel);
        Label enterPubLabel = new Label("Enter Pub:");
        HorizontalPanel panel1 = new HorizontalPanel();
        panel1.add(enterPubLabel);
        panel1.add(pubField);
        DOM.setElementAttribute(pubField.getElement(), "autocomplete", "off");
        lookupPanel.add(panel1);


        Label orLabel = new Label("- or - ");
        orLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        lookupPanel.add(orLabel);
        Label curatorPubLabel = new Label("Curator Pub:");
        HorizontalPanel panel2 = new HorizontalPanel();
        panel2.add(curatorPubLabel);
        panel2.add(curatorPubList);
        lookupPanel.add(panel2);

        publicationDisplayPanel.setVisible(true);

        panel.add(lookupPanel);
        panel.add(publicationDisplayPanel);
    }

    public void clearPublications() {
        curatorPubList.clear();
    }

    public void addPublication(PublicationDTO publicationAbstractDTO) {
        if (curatorPubList.getItemCount() == 0) {
            curatorPubList.addItem("Choose Pub:");
            curatorPubList.addItem("Scientific Curation", "ZDB-PUB-020723-5");
            curatorPubList.addItem("Nomenclature", "ZDB-PUB-030508-1");
            addRecentPubs() ;
        }
        if (publicationAbstractDTO != null && false==curatorPubList.containsValue(publicationAbstractDTO.getZdbID())) {
            if(publicationAbstractDTO.getTitle().length()< MAX_LENGTH){
                curatorPubList.addItem(publicationAbstractDTO.getTitle(), publicationAbstractDTO.getZdbID());
            }
            else{
                curatorPubList.addItem(publicationAbstractDTO.getTitle().substring(0,MAX_LENGTH)+"...", publicationAbstractDTO.getZdbID());
            }

        }
    }

    void publicationChanged(PublicationChangeEvent event) {

        final String pubChangeZdbID = event.getPublication();
        if (pubChangeZdbID.length() == 0) {
            clearPubBox();
        } else {
            String lookupString = pubField.getText().trim();
            if(false==lookupString.startsWith(PublicationValidator.ZDB_PUB_PREFIX)){
                lookupString = PublicationValidator.ZDB_PUB_PREFIX + lookupString;
            }
            MarkerRPCService.App.getInstance().getPublicationAbstract(lookupString, new AsyncCallback<PublicationDTO>() {
                public void onFailure(Throwable throwable) {
                    setPublicationAbstractDTO(null);
                    clearPubBox();
                }

                public void onSuccess(final PublicationDTO publicationAbstractDTO) {
                    setPublicationAbstractDTO(publicationAbstractDTO);
                    if (publicationAbstractDTO == null) {
                        clearPubBox();
                        return;
                    }
                    setPubBox(publicationAbstractDTO);
                    firePublicationChanged(new PublicationChangeEvent(publicationAbstractDTO.getZdbID()));
                }
            });
        }
    }

    public void addRecentPublicationDTO(String publicationZdbID){
        if(false==curatorPubList.containsValue(publicationZdbID)){
            publicationAbstractDTO.setZdbID(publicationZdbID);
            LookupService.App.getInstance().setRecentPublication(publicationZdbID,
                    new MarkerEditCallBack<PublicationDTO>(publicationZdbID){
                        @Override
                        public void onSuccess(PublicationDTO result) {
                            publicationAbstractDTO = result ; 
                            addPublication(publicationAbstractDTO);
                        }
                    });
        }
    }

    public PublicationDTO getPublicationAbstractDTO() {
        return publicationAbstractDTO;
    }

    void setPublicationAbstractDTO(PublicationDTO publicationAbstractDTO) {
        this.publicationAbstractDTO = publicationAbstractDTO;
    }

    void firePublicationChanged(PublicationChangeEvent publicationChangeEvent) {
        for (PublicationChangeListener publicationChangeListener : publicationChangeListeners) {
            publicationChangeListener.publicationChanged(publicationChangeEvent);
        }
    }

    // nothing to do
    @Override
    public void remove(String pubZdbID) { }

    @Override
    public void add(String pubZdbID) {
        addRecentPublicationDTO(pubZdbID);
    }

    void clearPubBox() {
//        publicationDisplayPanel.setVisible(false);
        publicationDisplayPanel.setHasNoPub();
        firePublicationChanged(new PublicationChangeEvent(""));
    }

    void setPubBox(PublicationDTO publicationAbstractDTO) {
        publicationDisplayPanel.setPublicationAbstractDomain(publicationAbstractDTO);
    }

    public void addPublicationChangeListener(PublicationChangeListener listener) {
        publicationChangeListeners.add(listener);
    }

    public boolean isValidPublication() {
        return publicationDisplayPanel.isValidPub();
    }

    public void addRecentPubs() {

        LookupService.App.getInstance().getRecentPublications(new MarkerEditCallBack<List<PublicationDTO>>("Failed to find recent publications: "){
            @Override
            public void onSuccess(List<PublicationDTO> results) {
                if(results!=null && results.size()>0){
                    addPublication(new PublicationDTO(SEPARATOR,null));
                    Collections.reverse(results);
                    for(PublicationDTO publicationDTO: results){
                        addPublication(publicationDTO);
                    }
                }
            }
        });
    }
}

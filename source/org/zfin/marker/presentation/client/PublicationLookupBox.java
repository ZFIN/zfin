package org.zfin.marker.presentation.client;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import org.zfin.marker.presentation.dto.PublicationAbstractDTO;
import org.zfin.marker.presentation.event.PublicationChangeEvent;
import org.zfin.marker.presentation.event.PublicationChangeListener;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class PublicationLookupBox extends Composite {

    private ListBox curatorPubList = new ListBox();
    private TextBox pubField = new TextBox();
    private VerticalPanel panel = new VerticalPanel();
    private VerticalPanel lookupPanel = new VerticalPanel();
    private PublicationDisplayPanel publicationDisplayPanel = new PublicationDisplayPanel();

    private List<PublicationChangeListener> publicationChangeListeners = new ArrayList<PublicationChangeListener>();

    // internal data
    private PublicationAbstractDTO publicationAbstractDTO;

    public PublicationLookupBox(String div) {
        this();
        RootPanel.get(div).add(this);
    }

    public PublicationLookupBox() {

        initGui();
        initWidget(panel);

        // this won't run
        DeferredCommand.addCommand(new Command() {
            public void execute() {
                publicationChanged(new PublicationChangeEvent(curatorPubList.getValue(0)));
            }
        });
    }

    protected void initGui() {
        lookupPanel.setStyleName("publicationlookup");
        HTML defaultPublicationLabel = new HTML("<b>Default Publication</b>");
        lookupPanel.add(defaultPublicationLabel);
        Label enterPubLabel = new Label("Enter Pub:");
        HorizontalPanel panel1 = new HorizontalPanel();
        panel1.add(enterPubLabel);
        panel1.add(pubField);
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

        // default items
//        curatorPubList.addItem("No default pubs");
        curatorPubList.addChangeListener(new ChangeListener() {
            public void onChange(Widget widget) {
                String selectedPub = curatorPubList.getValue(curatorPubList.getSelectedIndex());
                if (false == selectedPub.startsWith("ZDB-PUB")) {
                    selectedPub = "";
                }
                pubField.setText(selectedPub);
                publicationChanged(new PublicationChangeEvent(pubField.getText().trim()));
            }
        });

        pubField.addChangeListener(new ChangeListener() {
            public void onChange(Widget widget) {
                publicationChanged(new PublicationChangeEvent(pubField.getText().trim()));
            }
        });
        pubField.addKeyboardListener(new KeyboardListenerAdapter() {
            public void onKeyUp(Widget widget, char c, int i) {
                publicationChanged(new PublicationChangeEvent(pubField.getText().trim()));
            }
        });

        panel.add(lookupPanel);
        panel.add(publicationDisplayPanel);
    }

    public void clearPublications() {
        curatorPubList.clear();
    }

    public void addPublication(PublicationAbstractDTO publicationAbstractDTO) {
        if (curatorPubList.getItemCount() == 0) {
            curatorPubList.addItem("Choose Pub:");
            curatorPubList.addItem("Scientific Curation", "ZDB-PUB-020723-5");
            curatorPubList.addItem("Nomenclature", "ZDB-PUB-030508-1");
        }
        if (publicationAbstractDTO != null) {
            curatorPubList.addItem(publicationAbstractDTO.getTitle(), publicationAbstractDTO.getZdbID());
        }
    }

    public void publicationChanged(PublicationChangeEvent event) {

        final String pubChangeZdbID = event.getPublication();
        if (pubChangeZdbID.length() == 0) {
            clearPubBox();
        } else {
            MarkerRPCService.App.getInstance().getPublicationAbstract(pubField.getText().trim(), new AsyncCallback<PublicationAbstractDTO>() {
                public void onFailure(Throwable throwable) {
                    setPublicationAbstractDTO(null);
//                Window.alert("failure!!: "+ throwable);
                    clearPubBox();
                }

                public void onSuccess(PublicationAbstractDTO publicationAbstractDTO) {
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

    public PublicationAbstractDTO getPublicationAbstractDTO() {
        return publicationAbstractDTO;
    }

    public void setPublicationAbstractDTO(PublicationAbstractDTO publicationAbstractDTO) {
        this.publicationAbstractDTO = publicationAbstractDTO;
    }

    protected void firePublicationChanged(PublicationChangeEvent publicationChangeEvent) {
        for (PublicationChangeListener publicationChangeListener : publicationChangeListeners) {
            publicationChangeListener.publicationChanged(publicationChangeEvent);
        }
    }


    public void clearPubBox() {
//        publicationDisplayPanel.setVisible(false);
        publicationDisplayPanel.setError();
        firePublicationChanged(new PublicationChangeEvent(""));
    }

    public void setPubBox(PublicationAbstractDTO publicationAbstractDTO) {
//       Window.alert(publicationAbstractDomain.getTitle());
        publicationDisplayPanel.setPublicationAbstractDomain(publicationAbstractDTO);
    }

    public void clearPublicationChangeListeners() {
        publicationChangeListeners.clear();
    }

    public void addPublicationChangeListener(PublicationChangeListener listener) {
        publicationChangeListeners.add(listener);
    }

    public void removePublicationChangeListener(PublicationChangeListener listener) {
        publicationChangeListeners.remove(listener);
    }

    public boolean isValidPublication() {
        return publicationDisplayPanel.isValidPub();
    }
}

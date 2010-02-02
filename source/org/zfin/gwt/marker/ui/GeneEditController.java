package org.zfin.gwt.marker.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.marker.event.SequenceAddEvent;
import org.zfin.gwt.marker.event.SequenceAddListener;
import org.zfin.gwt.root.dto.DBLinkDTO;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.dto.ReferenceDatabaseDTO;
import org.zfin.gwt.root.ui.AbstractListBox;

import java.util.List;

/**
 * A GWT class for adding proteins to genes on the markerview.apg page.
 */
public final class GeneEditController extends AbstractMarkerEditController<MarkerDTO>{

    private final String newProteinSequenceDiv = "newProteinSequence";
    private final String newStemLoopSequenceDiv = "newStemLoopSequence";

    // lookup
    private static final String LOOKUP_TRANSCRIPT_ZDBID = "zdbID";

    // gui elements
    private final ProteinSequenceArea proteinSequenceArea = new ProteinSequenceArea();
    private final NotificationPanel proteinNotificationPanel = new NotificationPanel();
    private final NotificationPanel stemLoopNotificationPanel = new NotificationPanel();
    private final DockPanel proteinPublicationPanel = new DockPanel();
    private final PublicationLookupBox proteinPublicationLookupBox = new PublicationLookupBox();
    private final NucleotideSequenceArea nucleotideSequenceArea = new NucleotideSequenceArea();
    private final DockPanel proteinAddPanel = new DockPanel();
    private final DockPanel stemLoopAddPanel = new DockPanel();
    private final DockPanel stemLoopPublicationPanel = new DockPanel();
    private final PublicationLookupBox stemLoopPublicationLookupBox = new PublicationLookupBox();


    // internal data
    private String url;
    private final String urlHeader = "?MIval=aa-markerview.apg&UPDATE=1&OID=";


    private class NotificationPanel extends Composite {

        private final VerticalPanel panel = new VerticalPanel();
        private final Label messageLabel = new Label();
        private final Button button = new Button("Okay");

        public NotificationPanel() {
            initGUI();
            initWidget(panel);
        }

        public void initGUI() {
            panel.setVisible(false);
            panel.add(messageLabel);
            panel.add(new HTML("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"));
            panel.add(button);


            button.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event) {
                    setVisible(false);
                    proteinSequenceArea.activate();
                    proteinSequenceArea.resetAndHide();
                    Window.open(url + urlHeader + dto.getZdbID(), "_self", "");
                }
            });
        }

        public void showMessage(String message) {
            messageLabel.setText(message);
            setVisible(true);
        }
    }


    public void initGUI() {
        proteinAddPanel.add(proteinSequenceArea, DockPanel.WEST);
        proteinAddPanel.add(proteinNotificationPanel, DockPanel.SOUTH);
        Label verticalSpaceLabel = new Label();
        verticalSpaceLabel.setHeight("20px");
        proteinPublicationPanel.add(verticalSpaceLabel, DockPanel.NORTH);
        proteinPublicationPanel.add(proteinPublicationLookupBox, DockPanel.CENTER);
        proteinPublicationLookupBox.addPublication(null);
        Label horizontalSpaceLabel = new Label();
        horizontalSpaceLabel.setWidth("30px");
        proteinAddPanel.add(horizontalSpaceLabel, DockPanel.CENTER);
        proteinAddPanel.add(proteinPublicationPanel, DockPanel.EAST);

        proteinPublicationPanel.setVisible(false);

        RootPanel.get(newProteinSequenceDiv).add(proteinAddPanel);

        nucleotideSequenceArea.setRightArrowHTMLString("<a href=#addStemLoop><img align=\"top\" src=\"/images/right.gif\" >Add Stem Loop Sequence</a>");
        nucleotideSequenceArea.setDownArrowHTMLString("<a href=#addStemLoop><img align=\"top\" src=\"/images/down.gif\" >Add Stem Loop Sequence</a>");
//        nucleotideSequenceArea.setHistoryToken("addStemLoop");
        nucleotideSequenceArea.closeBox();


        stemLoopAddPanel.add(nucleotideSequenceArea, DockPanel.WEST);
        stemLoopAddPanel.add(stemLoopNotificationPanel, DockPanel.SOUTH);
        stemLoopPublicationPanel.add(verticalSpaceLabel, DockPanel.NORTH);
        stemLoopPublicationPanel.add(stemLoopPublicationLookupBox, DockPanel.CENTER);
        stemLoopPublicationLookupBox.addPublication(null);
        stemLoopAddPanel.add(horizontalSpaceLabel, DockPanel.CENTER);
        stemLoopAddPanel.add(stemLoopPublicationPanel, DockPanel.EAST);
        stemLoopPublicationPanel.setVisible(false);

        RootPanel.get(newStemLoopSequenceDiv).add(stemLoopAddPanel);

        loadDTO();

        setValues();
        addListeners();


    }

    protected void setValues() {
        // load databases
        TranscriptRPCService.App.getInstance().getGeneEditAddProteinSequenceReferenceDatabases(
                new MarkerEditCallBack<List<ReferenceDatabaseDTO>>("failed to load  sequence databases: ", proteinSequenceArea) {
                    public void onSuccess(List<ReferenceDatabaseDTO> referenceDatabaseDTOs) {
                        AbstractListBox databaseListBoxWrapper = proteinSequenceArea.getDatabaseListBoxWrapper();
                        databaseListBoxWrapper.addItem(AbstractListBox.EMPTY_CHOICE, AbstractListBox.NULL_STRING);
                        for (ReferenceDatabaseDTO referenceDatabaseDTO : referenceDatabaseDTOs) {
                            databaseListBoxWrapper.addItem(referenceDatabaseDTO.getBlastName(), referenceDatabaseDTO.getZdbID());
                        }
                        proteinSequenceArea.activate();
                    }
                });

        TranscriptRPCService.App.getInstance().getGeneEditAddableStemLoopNucleotideSequenceReferenceDatabases(
                new MarkerEditCallBack<List<ReferenceDatabaseDTO>>("failed to load  sequence databases: ", nucleotideSequenceArea) {
                    public void onSuccess(List<ReferenceDatabaseDTO> referenceDatabaseDTOs) {
                        AbstractListBox databaseListBoxWrapper = nucleotideSequenceArea.getDatabaseListBoxWrapper();
                        databaseListBoxWrapper.addItem(AbstractListBox.EMPTY_CHOICE, AbstractListBox.NULL_STRING);
                        for (ReferenceDatabaseDTO referenceDatabaseDTO : referenceDatabaseDTOs) {
                            databaseListBoxWrapper.addItem(referenceDatabaseDTO.getName(), referenceDatabaseDTO.getZdbID());
                        }
                        nucleotideSequenceArea.activate();
                    }
                });


        MarkerRPCService.App.getInstance().getWebDriverPath(
                new MarkerEditCallBack<String>("Failed to retrieve webdriver path, may need to refresh page if adding protein: ", proteinSequenceArea) {
                    public void onSuccess(String s) {
                        url = s;
                    }
                });
    }

    protected void addListeners() {

        proteinSequenceArea.addSequenceAddListener(new SequenceAddListener() {
            public void add(SequenceAddEvent sequenceAddEvent) {
                if (false == proteinPublicationLookupBox.isValidPublication()) {
                    proteinSequenceArea.setError("Must select a valid publication to add sequence.");
                    return;
                }
                proteinSequenceArea.clearError();
                proteinSequenceArea.inactivate();
                String sequenceStatus = proteinSequenceArea.checkSequence();
                if (sequenceStatus != null) {
                    Window.alert(sequenceStatus);
                    proteinSequenceArea.activate();
                    return;
                }

                // accession, defline, sequence
                MarkerRPCService.App.getInstance().addInternalProteinSequence(dto.getZdbID(),
                        sequenceAddEvent.getSequenceDTO().getSequence(),
                        proteinSequenceArea.getPublication(),
                        sequenceAddEvent.getReferenceDatabaseDTO().getZdbID(),
                        new MarkerEditCallBack<DBLinkDTO>("failed to add sequence: ", proteinSequenceArea) {
                            public void onFailure(Throwable throwable) {
                                super.onFailure(throwable);
                                proteinSequenceArea.activate();
                            }

                            public void onSuccess(DBLinkDTO dbLinkDTO) {
                                proteinSequenceArea.resetAndHide();
                                proteinPublicationPanel.setVisible(false);

                                proteinNotificationPanel.showMessage("Added Sequence: " + dbLinkDTO.getName());
                                proteinSequenceArea.activate();
                            }
                        });
            }

            public void cancel(SequenceAddEvent sequenceAddEvent) {
                proteinSequenceArea.resetAndHide();
                proteinSequenceArea.activate();
                proteinPublicationPanel.setVisible(false);
            }

            public void start(SequenceAddEvent sequenceAddEvent) {
                proteinPublicationPanel.setVisible(true);
            }
        });


        nucleotideSequenceArea.addSequenceAddListener(new SequenceAddListener() {
            public void add(SequenceAddEvent sequenceAddEvent) {
                if (false == stemLoopPublicationLookupBox.isValidPublication()) {
                    nucleotideSequenceArea.setError("Must select a valid publication to add sequence.");
                    return;
                }
                nucleotideSequenceArea.clearError();
                nucleotideSequenceArea.inactivate();

                MarkerRPCService.App.getInstance().addInternalNucleotideSequence(dto.getZdbID(),
                        sequenceAddEvent.getSequenceDTO().getSequence(),
                        nucleotideSequenceArea.getPublication(),
                        sequenceAddEvent.getReferenceDatabaseDTO().getZdbID(),
                        new MarkerEditCallBack<DBLinkDTO>("Failed to add sequence: ", nucleotideSequenceArea) {
                            public void onFailure(Throwable caught) {
                                super.onFailure(caught);
                                nucleotideSequenceArea.activate();
                            }

                            public void onSuccess(DBLinkDTO dbLinkDTO) {
                                nucleotideSequenceArea.resetAndHide();
                                nucleotideSequenceArea.activate();
                                stemLoopPublicationLookupBox.setVisible(false);
                                stemLoopNotificationPanel.showMessage("Added Sequence: " + dbLinkDTO.getName());
                            }
                        });
            }

            public void cancel(SequenceAddEvent sequenceAddEvent) {
                nucleotideSequenceArea.resetAndHide();
                stemLoopPublicationPanel.setVisible(false);
            }

            public void start(SequenceAddEvent sequenceAddEvent) {
                // do nothing here
                stemLoopPublicationPanel.setVisible(true);
            }
        });

        proteinPublicationLookupBox.addPublicationChangeListener(proteinSequenceArea);
        stemLoopPublicationLookupBox.addPublicationChangeListener(nucleotideSequenceArea);
    }


    protected void loadDTO() {
// load properties
        Dictionary transcriptDictionary = Dictionary.getDictionary("MarkerProperties");
        final String zdbID = transcriptDictionary.get(LOOKUP_TRANSCRIPT_ZDBID);

        MarkerRPCService.App.getInstance().getGeneForZdbID(zdbID,
                new MarkerEditCallBack<MarkerDTO>("failed to find zdbID: ", nucleotideSequenceArea) {
                    public void onSuccess(MarkerDTO markerDTO) {
                        if (markerDTO == null) {
                            Window.alert("failed to find gene for zdbID: " + zdbID);
                        }
                        setDTO(markerDTO);
                    }
                });
    }

    protected void setDTO(MarkerDTO dto){
        this.dto = dto;
        nucleotideSequenceArea.setMarkerDTO(this.dto);
    }

}
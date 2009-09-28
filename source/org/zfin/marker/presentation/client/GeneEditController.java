package org.zfin.marker.presentation.client;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.i18n.client.Dictionary;


import org.zfin.marker.presentation.event.*;
import org.zfin.marker.presentation.dto.*;

import java.util.List;

/**
 * A GWT class for adding proteins to genes on the markerview.apg page.
 */
public final class GeneEditController {

    private String newProteinSequenceDiv = "newProteinSequence" ;
    private String newStemLoopSequenceDiv = "newStemLoopSequence" ;

    // lookup
    public static final String LOOKUP_TRANSCRIPT_ZDBID= "zdbID" ;
    public static final String LOOKUP_CURATOR_ZDBID= "curatorID" ;

    // gui data
    private final int DEFAULT_LENGTH = 60 ;
    private int lineLength ;

    // gui elements
    private ProteinSequenceArea proteinSequenceArea = new ProteinSequenceArea() ;
    private NotificationPanel proteinNotificationPanel = new NotificationPanel();
    private NotificationPanel stemLoopNotificationPanel = new NotificationPanel();
    private DockPanel proteinPublicationPanel = new DockPanel() ;
    private PublicationLookupBox proteinPublicationLookupBox = new PublicationLookupBox() ;
    private NucleotideSequenceArea nucleotideSequenceArea = new NucleotideSequenceArea() ;
    private DockPanel proteinAddPanel = new DockPanel();
    private DockPanel stemLoopAddPanel = new DockPanel();
    private DockPanel stemLoopPublicationPanel = new DockPanel() ;
    private PublicationLookupBox stemLoopPublicationLookupBox = new PublicationLookupBox() ;



    // internal data
    private String geneZdbID ;
    private MarkerDTO gene ;
    private String url ;
    private String urlHeader = "?MIval=aa-markerview.apg&UPDATE=1&OID=" ;


    private class NotificationPanel extends Composite{

        private VerticalPanel panel = new VerticalPanel() ;
        private Label messageLabel = new Label() ;
        private Button button = new Button("Okay");

        public NotificationPanel(){
            initGUI() ;
            initWidget(panel);
        }

        public void initGUI(){
            panel.setVisible(false);
            panel.add(messageLabel);
            panel.add(new HTML("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"));
            panel.add(button);



            button.addClickListener(new ClickListener(){
                public void onClick(Widget widget) {
                    setVisible(false);
                    proteinSequenceArea.activate() ;
                    proteinSequenceArea.resetAndHide() ;
                    Window.open(url+urlHeader+geneZdbID, "_self", "");
                }
            });
        }

        public void showMessage(String message){
            messageLabel.setText(message);
            setVisible(true);
        }
    }


    public void initGUI() {
        proteinAddPanel.add(proteinSequenceArea,DockPanel.WEST);
        proteinAddPanel.add(proteinNotificationPanel,DockPanel.SOUTH);
        Label verticalSpaceLabel = new Label() ;
        verticalSpaceLabel.setHeight("20px");
        proteinPublicationPanel.add(verticalSpaceLabel,DockPanel.NORTH);
        proteinPublicationPanel.add(proteinPublicationLookupBox,DockPanel.CENTER);
        proteinPublicationLookupBox.addPublication(null);
        Label horizontalSpaceLabel = new Label() ;
        horizontalSpaceLabel.setWidth("30px");
        proteinAddPanel.add(horizontalSpaceLabel,DockPanel.CENTER);
        proteinAddPanel.add(proteinPublicationPanel,DockPanel.EAST);

        proteinPublicationPanel.setVisible(false);

        RootPanel.get(newProteinSequenceDiv).add(proteinAddPanel);

        nucleotideSequenceArea.setRightArrowHTMLString("<a href=#addStemLoop><img align=\"top\" src=\"/images/right.gif\" >Add Stem Loop Sequence</a>");
        nucleotideSequenceArea.setDownArrowHTMLString("<a href=#addStemLoop><img align=\"top\" src=\"/images/down.gif\" >Add Stem Loop Sequence</a>");
        nucleotideSequenceArea.setHistoryToken("addStemLoop");
        nucleotideSequenceArea.closeBox();


        stemLoopAddPanel.add(nucleotideSequenceArea,DockPanel.WEST);
        stemLoopAddPanel.add(stemLoopNotificationPanel,DockPanel.SOUTH);
        stemLoopPublicationPanel.add(verticalSpaceLabel,DockPanel.NORTH);
        stemLoopPublicationPanel.add(stemLoopPublicationLookupBox,DockPanel.CENTER);
        stemLoopPublicationLookupBox.addPublication(null);
        stemLoopAddPanel.add(horizontalSpaceLabel,DockPanel.CENTER);
        stemLoopAddPanel.add(stemLoopPublicationPanel,DockPanel.EAST);
        stemLoopPublicationPanel.setVisible(false);

        RootPanel.get(newStemLoopSequenceDiv).add(stemLoopAddPanel);

        loadGene();

        setValues() ;
        addHandlers() ;


    }

    private void setValues(){
        // load databases
        TranscriptRPCService.App.getInstance().getGeneEditAddProteinSequenceReferenceDatabases(
                new MarkerEditCallBack<List<ReferenceDatabaseDTO>>("failed to load  sequence databases: ",proteinSequenceArea){
                    public void onSuccess(List<ReferenceDatabaseDTO> referenceDatabaseDTOs) {
                        EasyListBox databaseListBoxWrapper = proteinSequenceArea.getDatabaseListBoxWrapper() ;
                        databaseListBoxWrapper.addItem(EasyListBox.EMPTY_CHOICE, EasyListBox.NULL_STRING);
                        for(ReferenceDatabaseDTO referenceDatabaseDTO : referenceDatabaseDTOs){
                            databaseListBoxWrapper.addItem(referenceDatabaseDTO.getBlastName(),referenceDatabaseDTO.getZdbID());
                        }
                        proteinSequenceArea.activate();
                    }
                });

        TranscriptRPCService.App.getInstance().getGeneEditAddableStemLoopNucleotideSequenceReferenceDatabases(
                new MarkerEditCallBack<List<ReferenceDatabaseDTO>>("failed to load  sequence databases: ",nucleotideSequenceArea){
                    public void onSuccess(List<ReferenceDatabaseDTO> referenceDatabaseDTOs) {
                        EasyListBox databaseListBoxWrapper = nucleotideSequenceArea.getDatabaseListBoxWrapper() ;
                        databaseListBoxWrapper.addItem(EasyListBox.EMPTY_CHOICE, EasyListBox.NULL_STRING);
                        for(ReferenceDatabaseDTO referenceDatabaseDTO : referenceDatabaseDTOs){
                            databaseListBoxWrapper.addItem(referenceDatabaseDTO.getName(),referenceDatabaseDTO.getZdbID());
                        }
                        nucleotideSequenceArea.activate();
                    }
                });



        MarkerRPCService.App.getInstance().getWebDriverPath(
                new MarkerEditCallBack<String>("Failed to retrieve webdriver path, may need to refresh page if adding protein: ",proteinSequenceArea){
                    public void onSuccess(String s) {
                        url = s ;
                    }
                });
    }

    private void addHandlers(){

        proteinSequenceArea.addSequenceAddListener(new SequenceAddListener(){
            public void add(SequenceAddEvent sequenceAddEvent) {
                if(false==proteinPublicationLookupBox.isValidPublication()){
                    proteinSequenceArea.setError("Must select a valid publication to add sequence.");
                    return ;
                }
                proteinSequenceArea.clearError();
                proteinSequenceArea.inactivate() ;
                String sequenceStatus = proteinSequenceArea.checkSequence() ;
                if(sequenceStatus!=null){
                    Window.alert(sequenceStatus);
                    proteinSequenceArea.activate();
                    return ;
                }

                // accession, defline, sequence
                MarkerRPCService.App.getInstance().addInternalProteinSequence(geneZdbID,
                        sequenceAddEvent.getSequenceDTO().getSequence(),
                        proteinSequenceArea.getPublication(),
                        sequenceAddEvent.getReferenceDatabaseDTO().getZdbID(),
                        new MarkerEditCallBack<DBLinkDTO>("failed to add sequence: ",proteinSequenceArea){
                            public void onFailure(Throwable throwable) {
                                super.onFailure(throwable);
                                proteinSequenceArea.activate() ;
                            }

                            public void onSuccess(DBLinkDTO dbLinkDTO) {
                                proteinSequenceArea.resetAndHide();
                                proteinPublicationPanel.setVisible(false);

                                proteinNotificationPanel.showMessage("Added Sequence: "+ dbLinkDTO.getName());
                                proteinSequenceArea.activate() ;
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


        nucleotideSequenceArea.addSequenceAddListener(new SequenceAddListener(){
            public void add(SequenceAddEvent sequenceAddEvent) {
                if(false==stemLoopPublicationLookupBox.isValidPublication()){
                    nucleotideSequenceArea.setError("Must select a valid publication to add sequence.");
                    return ;
                }
                nucleotideSequenceArea.clearError();
                nucleotideSequenceArea.inactivate();

                MarkerRPCService.App.getInstance().addInternalNucleotideSequence(geneZdbID,
                        sequenceAddEvent.getSequenceDTO().getSequence(),
                        nucleotideSequenceArea.getPublication(),
                        sequenceAddEvent.getReferenceDatabaseDTO().getZdbID(),
                        new MarkerEditCallBack<DBLinkDTO>("Failed to add sequence: ",nucleotideSequenceArea){
                            public void onFailure(Throwable caught) {
                                super.onFailure(caught);
                                nucleotideSequenceArea.activate();
                            }

                            public void onSuccess(DBLinkDTO dbLinkDTO) {
                                nucleotideSequenceArea.resetAndHide();
                                nucleotideSequenceArea.activate() ;
                                stemLoopPublicationLookupBox.setVisible(false);
                                stemLoopNotificationPanel.showMessage("Added Sequence: "+ dbLinkDTO.getName());
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


    public void loadGene(){
// load properties
        Dictionary transcriptDictionary = Dictionary.getDictionary("MarkerProperties") ;
        geneZdbID = transcriptDictionary.get(LOOKUP_TRANSCRIPT_ZDBID) ;

        MarkerRPCService.App.getInstance().getGeneForZdbID(geneZdbID,
                new MarkerEditCallBack<MarkerDTO>("failed to find zdbID: ",nucleotideSequenceArea){
                    public void onSuccess(MarkerDTO markerDTO) {
                        if(markerDTO==null){
                            Window.alert("failed to find gene for zdbID: "+ geneZdbID) ;
                        }
                        gene = markerDTO ;
                        nucleotideSequenceArea.setMarkerDTO(gene);
                    }
                });
    }


    public String insertLineReturns(String string,int numCharsPerLine){
        this.lineLength = numCharsPerLine ;
        char[] chars = string.toCharArray() ;
        StringBuffer buffer = new StringBuffer() ;
        for(int i = 1 ; i <= chars.length ; i++){
            if(Character.isLetter(chars[i-1])){
                buffer.append(chars[i-1]) ;
            }
            if(i%numCharsPerLine==0 ){
                buffer.append("<br>") ;
            }
        }

        return buffer.toString().toUpperCase() ;
    }


    public int getLineLength() {
        return (lineLength==0 ? lineLength=DEFAULT_LENGTH : lineLength);
    }
}
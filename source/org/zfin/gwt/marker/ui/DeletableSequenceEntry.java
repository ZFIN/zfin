package org.zfin.gwt.marker.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.root.dto.SequenceDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a line of sequence with a button on in to make it deletable.
 */
public class DeletableSequenceEntry extends Composite {

    // gui components
    private VerticalPanel panel = new VerticalPanel();
    private HorizontalPanel deflinePanel = new HorizontalPanel();
    private Label deflineLabel = new Label();
    private HTML sequenceHTML = new HTML();
    private final int DEFAULT_LENGTH = 60;
    private int lineLength;
    private AttributionList attributionList = new AttributionList();
    private String imageURL = "/images/";

    private Image addAttributionButton = new Image(imageURL + "new-attribution.png");
    private Image removeSequenceButton = new Image(imageURL + "delete-button.png");

    // types
    private static final String NUCLEOTIDE_SEQUENCE = "NUCLEOTIDE_SEQUENCE";
    private static final String PROTEIN_SEQUENCE = "PROTEIN_SEQUENCE";

    // internal data
    private SequenceDTO sequenceDTO;
    private String type;
    private HasRelatedEntities parent;


    public DeletableSequenceEntry(String type, SequenceDTO sequenceDTO, HasRelatedEntities parent) {
        if (type.equals(NUCLEOTIDE_SEQUENCE) || type.equals(PROTEIN_SEQUENCE)) {
            this.type = type;
        } else {
            Window.alert("sequence type is unknown: " + type);
            return;
        }
        this.sequenceDTO = sequenceDTO;
        this.parent = parent;
        initGUI();
        initWidget(panel);
    }

    public String getDBLinkZdbID() {
        return sequenceDTO.getDataZdbID();
    }

    protected void initGUI() {
        deflineLabel.setText(sequenceDTO.getDefLine());
        sequenceHTML.setHTML(insertLineReturns(sequenceDTO.getSequence(), getLineLength()));
        sequenceHTML.setStyleName("sequenceDisplay");

        removeSequenceButton.setVisible(sequenceDTO.isEditable());
        deflinePanel.add(removeSequenceButton);

        removeSequenceButton.setStyleName("relatedEntityPubLink");
        deflinePanel.add(deflineLabel);

        addAttributionButton.setStyleName("relatedEntityPubLink");
        addAttributionButton.setTitle("Add attribution.");
        addAttributionButton.setPixelSize(20, 20);

        deflinePanel.add(addAttributionButton);
        deflinePanel.add(attributionList);
        panel.add(deflinePanel);
        panel.add(sequenceHTML);

        if (sequenceDTO.getPublicationZdbID() != null) {
            addAttributionToGUI(sequenceDTO);
        }

        addAttributionButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                parent.addAttribution(sequenceDTO);
            }
        });

        removeSequenceButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                parent.removeRelatedEntity(getDelatableSequence().getSequenceDTO());
            }
        });
    }

    public DeletableSequenceEntry getDelatableSequence() {
        return this;
    }

//    protected void fireSequenceRemoved(SequenceRemovedEvent sequenceRemovedEvent){
//        for(SequenceRemovedListener sequenceRemovedListener: sequenceRemovedListeners){
//            sequenceRemovedListener.remove(sequenceRemovedEvent);
//        }
//    }


    public String getSequenceAsString() {
        char[] chars = sequenceHTML.getText().toCharArray();
        StringBuffer buffer = new StringBuffer();
        for (char aChar : chars) {
            if (Character.isLetter(aChar)) {
                buffer.append(aChar);
            }
        }

        return buffer.toString().toUpperCase();
    }

    public SequenceDTO getSequenceDTO() {
        return this.sequenceDTO;
    }

    public void updateSequence() {
        this.sequenceDTO.setSequence(this.sequenceHTML.getText());
    }

    public String getSequence() {
        return sequenceHTML.getText();
    }

    protected String insertLineReturns(String string, int numCharsPerLine) {
        this.lineLength = numCharsPerLine;
        char[] chars = string.toCharArray();
        StringBuffer buffer = new StringBuffer();
        for (int i = 1; i <= chars.length; i++) {
            if (Character.isLetter(chars[i - 1])) {
                buffer.append(chars[i - 1]);
            }
            if (i % numCharsPerLine == 0) {
                buffer.append("<br>");
            }
        }

        return buffer.toString().toUpperCase();
    }

    public void setSequence(SequenceDTO sequenceDTO) {
        this.sequenceDTO = sequenceDTO;
        if (this.sequenceDTO != null && sequenceDTO.getSequence().length() > 0) {
            sequenceHTML.setHTML(insertLineReturns(this.sequenceDTO.getSequence(), getLineLength()));
        }
    }

    public boolean isDirty() {
        return false == sequenceDTO.getSequence().equals(sequenceHTML.getText());
    }


    /**
     * @return Error string is returned if invalid.
     */
    public String checkSequence() {
        if (type.equals(NUCLEOTIDE_SEQUENCE)) {
            char[] chars = sequenceHTML.getText().toUpperCase().toCharArray();
            for (int i = 0; i < chars.length; i++) {
                if (Character.isLetter(chars[i])) {
                    switch (chars[i]) {
                        case 'A':
                        case 'C':
                        case 'T':
                        case 'G':
                            break;
                        default:
                            return "Not a valid nucleotide sequence at " + i + " [" + chars[i] + "]";
                    }
                }
            }
        } else if (type.equals(PROTEIN_SEQUENCE)) {
            char[] chars = sequenceHTML.getText().toUpperCase().toCharArray();
            for (int i = 0; i < chars.length; i++) {
                if (Character.isLetter(chars[i])) {
                    switch (chars[i]) {
                        case 'G':
                        case 'P':
                        case 'A':
                        case 'V':
                        case 'L':
                        case 'I':
                        case 'M':
                        case 'C':
                        case 'F':
                        case 'Y':
                        case 'W':
                        case 'H':
                        case 'K':
                        case 'R':
                        case 'Q':
                        case 'N':
                        case 'E':
                        case 'D':
                        case 'S':
                        case 'T':
                        case 'X':
                            break;
                        default:
                            return "Not a valid protein sequence at " + i + " [" + chars[i] + "]";
                    }
                }
            }
        }

        return null;
    }

    public int getLineLength() {
        return (lineLength == 0 ? lineLength = DEFAULT_LENGTH : lineLength);
    }

    public void setLineLength(int lineLength) {
        this.lineLength = lineLength;
    }

    public boolean hasAttributions() {
        return attributionList.getWidgetCount() > 0;
    }

    public List<PublicationAttributionLabel<SequenceDTO>> getAttributions() {
        List<PublicationAttributionLabel<SequenceDTO>> attributions = new ArrayList<PublicationAttributionLabel<SequenceDTO>>();
        for (int i = 0; i < attributionList.getWidgetCount(); i++) {
            Widget widget = attributionList.getWidget(i);
            if (widget instanceof PublicationAttributionLabel) {
                attributions.add((PublicationAttributionLabel<SequenceDTO>) widget);
            }
        }
        return attributions;
    }


    public void addAttributionToGUI(SequenceDTO dto) {
        PublicationAttributionLabel<SequenceDTO> publicationAttributionLabel = new PublicationAttributionLabel<SequenceDTO>(this.parent, dto.getPublicationZdbID(), dto.getName(), dto, dto.getAttributionType());
        publicationAttributionLabel.setEditable(true);
        attributionList.addAttribution(publicationAttributionLabel);
    }

    public void removeAttributionFromGUI(SequenceDTO sequenceDTO) {
        for (PublicationAttributionLabel publicationAttributionLabel : attributionList.getAttributions()) {
            if (publicationAttributionLabel.getPublication().equals(sequenceDTO.getPublicationZdbID())) {
                attributionList.remove(publicationAttributionLabel);
            }
        }
    }

    public class AttributionList extends HorizontalPanel {

        public void addAttribution(PublicationAttributionLabel publicationAttributionLabel) {
            add(publicationAttributionLabel);
        }

        public List<PublicationAttributionLabel> getAttributions() {
            List<PublicationAttributionLabel> publicationAttributionLabels = new ArrayList<PublicationAttributionLabel>();
            for (int i = 0; i < getWidgetCount(); i++) {
                publicationAttributionLabels.add((PublicationAttributionLabel) getWidget(i));
            }

            return publicationAttributionLabels;
        }
    }
}


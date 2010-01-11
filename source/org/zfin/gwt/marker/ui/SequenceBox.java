package org.zfin.gwt.marker.ui;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.marker.event.SequenceChangeEvent;
import org.zfin.gwt.marker.event.SequenceChangeListener;
import org.zfin.gwt.root.dto.SequenceDTO;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class SequenceBox extends Composite implements SequenceChangeListener {

    // gui components
    private VerticalPanel panel = new VerticalPanel();
    private TextBox lengthField = new TextBox();
    private Label deflineLabel = new Label();
    private TextArea sequenceArea = new TextArea();
    private final int DEFAULT_LENGTH = 60;
    private int numLines;
    private int lineLength;

    // types
    public static final String NUCLEOTIDE_SEQUENCE = "NUCLEOTIDE_SEQUENCE";
    public static final String PROTEIN_SEQUENCE = "PROTEIN_SEQUENCE";

    // internal data
    private SequenceDTO sequenceDTO;
    private String type;


    // listeners
    private List<SequenceChangeListener> sequenceBoxChangeListeners = new ArrayList<SequenceChangeListener>();

    public SequenceBox(String type) {
        if (type.equals(NUCLEOTIDE_SEQUENCE) || type.equals(PROTEIN_SEQUENCE)) {
            this.type = type;
        } else {
            throw new RuntimeException("Type is unknown: " + type);
        }
        initGui();
        initWidget(panel);
    }

    protected void initGui() {
        HorizontalPanel horizontalPanel = new HorizontalPanel();
        Label lengthLabel = new Label("Length:");
        lengthField.setEnabled(false);
        horizontalPanel.add(lengthLabel);
        horizontalPanel.add(lengthField);

        panel.add(horizontalPanel);
        panel.add(deflineLabel);
        panel.add(sequenceArea);

        sequenceArea.addKeyboardListener(new KeyboardListenerAdapter() {
            public void onKeyUp(Widget widget, char c, int i) {
                fireSequenceBoxChangeEvent(new SequenceChangeEvent());
            }
        });
        sequenceArea.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                fireSequenceBoxChangeEvent(new SequenceChangeEvent());
            }
        });
        sequenceArea.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                fireSequenceBoxChangeEvent(new SequenceChangeEvent());
            }
        });
        sequenceArea.setStyleName("sequenceArea");
        sequenceBoxChangeListeners.add(this); // adds self as a listener
    }


    public String getSequenceAsString() {
        char[] chars = sequenceArea.getText().toCharArray();
        StringBuffer buffer = new StringBuffer();
        for (char aChar : chars) {
            if (Character.isLetter(aChar)) {
                buffer.append(aChar);
            } else if (aChar == '-' || aChar == '*') {
                buffer.append(aChar);
            }
        }

        return buffer.toString().toUpperCase();
    }

    public SequenceDTO getSequenceDTO() {
        return this.sequenceDTO;
    }

    public void updateSequence() {
        this.sequenceDTO.setSequence(this.sequenceArea.getText());
    }

    public void clearSequence() {
        sequenceArea.setText("");
        changed(new SequenceChangeEvent());
    }

    public void revert() {
        sequenceArea.setText(sequenceDTO.getSequence());
//        sequenceChanged(null);
    }

    public String getSequence() {
        return sequenceArea.getText();
    }

    protected String insertLineReturns(String string, int numCharsPerLine) {
        numLines = 0;
        this.lineLength = numCharsPerLine;
        char[] chars = string.toCharArray();
        StringBuffer buffer = new StringBuffer();
        for (int i = 1; i <= chars.length; i++) {
            if (Character.isLetter(chars[i - 1])) {
                buffer.append(chars[i - 1]);
            }
            if (i % numCharsPerLine == 0) {
                buffer.append("\n");
                ++numLines;
            }
        }

        return buffer.toString().toUpperCase();
    }

    public void setSequence(SequenceDTO sequenceDTO) {
        this.sequenceDTO = sequenceDTO;
        if (this.sequenceDTO != null && sequenceDTO.getSequence().length() > 0) {
            sequenceArea.setText(insertLineReturns(this.sequenceDTO.getSequence(), getLineLength()));
            sequenceArea.setCharacterWidth(getLineLength());
            sequenceArea.setVisibleLines(numLines + 2);
            deflineLabel.setText(sequenceDTO.getDefLine());
        }
        changed(new SequenceChangeEvent());
    }

    public boolean isDirty() {
        return false == sequenceDTO.getSequence().equals(sequenceArea.getText());
    }

    protected void fireSequenceBoxChangeEvent(SequenceChangeEvent sequenceChangeEvent) {
        for (SequenceChangeListener sequenceChangeListener : sequenceBoxChangeListeners) {
            sequenceChangeListener.changed(sequenceChangeEvent);
        }
    }

    public void addSequenceBoxChangeListener(SequenceChangeListener sequenceChangeListener) {
        sequenceBoxChangeListeners.add(sequenceChangeListener);
    }

    public void removeSequenceBoxChangeListener(SequenceChangeListener sequenceChangeListener) {
        sequenceBoxChangeListeners.remove(sequenceChangeListener);
    }

    /**
     * @return Error string is returned if invalid.
     */
    public String checkSequence() {
        String sequenceString = getSequenceAsString().toUpperCase();
        if (sequenceString.length() == 0) {
            return "Sequence must not be empty";
        }
        if (type.equals(NUCLEOTIDE_SEQUENCE)) {
            int invalidSequenceCharacter = SequenceValidator.validateNucleotideSequence(sequenceString);
            if (invalidSequenceCharacter != SequenceValidator.NOT_FOUND) {
                return "Letter " + "[" + sequenceString.substring(invalidSequenceCharacter, invalidSequenceCharacter + 1) + "]" + " at position " + (invalidSequenceCharacter + 1) + " is not a valid nucleotide symbol.";
            } else {
                return null;
            }
        } else if (type.equals(PROTEIN_SEQUENCE)) {
            int invalidSequenceCharacter = SequenceValidator.validatePolypeptideSequence(sequenceString);
            if (invalidSequenceCharacter != SequenceValidator.NOT_FOUND) {
                return "Letter " + "[" + sequenceString.substring(invalidSequenceCharacter, invalidSequenceCharacter + 1) + "]" + " at position " + (invalidSequenceCharacter + 1) + " is not a valid protein symbol.";
            } else {
                return null;
            }
        }

        return null;
    }

    public void changed(SequenceChangeEvent sequenceChangeEvent) {
        lengthField.setText(String.valueOf(getSequenceAsString().length()));
    }

    public int getLineLength() {
        return (lineLength == 0 ? lineLength = DEFAULT_LENGTH : lineLength);
    }

    public void setLineLength(int lineLength) {
        this.lineLength = lineLength;
    }

    public void activate() {
        sequenceArea.setEnabled(true);
    }

    public void inactivate() {
        sequenceArea.setEnabled(false);
    }
}

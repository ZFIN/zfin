package org.zfin.marker.presentation.client;

import com.google.gwt.user.client.ui.RootPanel;
import org.zfin.marker.presentation.dto.SequenceDTO;
import org.zfin.marker.presentation.dto.TranscriptDTO;
import org.zfin.marker.presentation.event.SequenceAddEvent;
import org.zfin.marker.presentation.event.SequenceAddListener;

public class NucleotideTranscriptSequenceArea extends NucleotideSequenceArea {

    public NucleotideTranscriptSequenceArea(String div) {
        super();
        RootPanel.get(div).add(this);
    }


    protected void addInternalListeners(final NucleotideSequenceArea nucleotideSequenceArea) {
        super.addInternalListeners(nucleotideSequenceArea);

        addSequenceAddListener(new SequenceAddListener() {
            public void add(SequenceAddEvent sequenceAddEvent) {
                inactivate();
                TranscriptRPCService.App.getInstance().addNucleotideSequenceToTranscript((TranscriptDTO) sequenceAddEvent.getMarkerDTO(),
                        sequenceAddEvent.getSequenceDTO(), sequenceAddEvent.getReferenceDatabaseDTO(),
                        new MarkerEditCallBack<SequenceDTO>("Failed to add sequence: ", nucleotideSequenceArea) {
                            public void onFailure(Throwable caught) {
                                super.onFailure(caught);
                                activate();
                            }

                            public void onSuccess(SequenceDTO sequenceDTO) {
                                // on success
                                addRelatedEntityToGUI(sequenceDTO);
                                resetAndHide();
                                activate();
                            }
                        });
            }

            public void cancel(SequenceAddEvent sequenceAddEvent) {
                resetAndHide();
            }

            public void start(SequenceAddEvent sequenceAddEvent) {
                // do nothing here
            }
        });

    }

    public void handleAddSequenceView() {
        if (sequenceList.getWidgetCount() > 0) {
            hideAddSequence();
        } else {
            showAddSequence();
        }
    }
}
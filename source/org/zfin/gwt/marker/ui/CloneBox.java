package org.zfin.gwt.marker.ui;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.RootPanel;
import org.zfin.gwt.root.dto.CloneDTO;
import org.zfin.gwt.root.dto.CloneTypesDTO;
import org.zfin.gwt.root.ui.*;

/**
 */
public class CloneBox extends AbstractDataBox<CloneDTO>{

    // table
    private Grid table = new Grid(5, 4);
    private StringListBox libraryListBox = new StringListBox();
    private IntegerListBox cloneRatingListBox = new IntegerListBox();
    private StringListBox vectorListBox = new StringListBox();
    private StringListBox digestListBox = new StringListBox();
    private StringListBox polymeraseListBox = new StringListBox();
    private IntegerTextBox insertSizeTextBox = new IntegerTextBox();
    private StringTextBox cloneComments = new StringTextBox();
    private StringTextBox pcrAmplificationTextBox = new StringTextBox();
    private StringListBox cloningSiteListBox = new StringListBox();



    public CloneBox(String div) {
        super();
        initGUI();
        setValues();
        addInternalListeners(this);
        initWidget(panel);
        RootPanel.get(div).add(this);
    }

    protected void initGUI() {
        super.initGUI();
        table.setText(0, 0, "Cloning Site:");
        table.setWidget(0, 1, cloningSiteListBox);
        table.setText(1, 0, "Library:");
        table.setWidget(1, 1, libraryListBox);
        table.setText(2, 0, "Vector:");
        table.setWidget(2, 1, vectorListBox);
        table.setText(4, 0, "Digest:");
        table.setWidget(4, 1, digestListBox);


        cloneRatingListBox.addItem(AbstractListBox.EMPTY_CHOICE, null);
        cloneRatingListBox.addItem("0", "0");
        cloneRatingListBox.addItem("1", "1");
        cloneRatingListBox.addItem("2", "2");
        cloneRatingListBox.addItem("3", "3");
        cloneRatingListBox.addItem("4", "4");

        table.setText(0, 2, "Clone Rating:");
        cloneRatingListBox.setEnabled(false);
        table.setWidget(0, 3, cloneRatingListBox);
        table.setText(1, 2, "Polymerase:");
        table.setWidget(1, 3, polymeraseListBox);
        table.setText(2, 2, "Insert Size:");
        table.setWidget(2, 3, insertSizeTextBox);
        table.setText(3, 2, "Comments:");
        cloneComments.setEnabled(false);
        table.setWidget(3, 3, cloneComments);
        table.setText(4, 2, "PCR Ampl:");
        table.setWidget(4, 3, pcrAmplificationTextBox);

        panel.add(table);
        panel.setStyleName("gwt-editbox");

        panel.add(buttonPanel);
        panel.add(errorLabel);
    }

    @Override
    protected void setValues() {

        CloneRPCService.App.getInstance().getCloneTypes(new AsyncCallback<CloneTypesDTO>() {
            public void onFailure(Throwable throwable) {
                Window.alert("failure to load clone types: " + throwable);
            }

            public void onSuccess(CloneTypesDTO cloneTypesDTO) {
                vectorListBox.addNullAndItems(cloneTypesDTO.getVectorNames());
                libraryListBox.addNullAndItems(cloneTypesDTO.getProbeLibraries());
                cloningSiteListBox.addNullAndItems(cloneTypesDTO.getCloneSites());
                digestListBox.addNullAndItems(cloneTypesDTO.getDigests());
                polymeraseListBox.addNullAndItems(cloneTypesDTO.getPolymeraseNames());

                revertGUI();
            }
        });

    }

    public void addInternalListeners(HandlesError handlesError){
        libraryListBox.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                handleDirty();
            }
        });
        vectorListBox.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                handleDirty();
            }
        });
        digestListBox.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                handleDirty();
            }
        });
        polymeraseListBox.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                handleDirty();
            }
        });
        cloningSiteListBox.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                handleDirty();
            }
        });

        insertSizeTextBox.addKeyPressHandler(new KeyPressHandler(){
            @Override
            public void onKeyPress(KeyPressEvent event) {
                handleDirty();
            }
        });
        pcrAmplificationTextBox.addKeyPressHandler(new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {
                handleDirty();
            }
        });

        saveButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                if (isDirty()) {
                    working();
                    CloneRPCService.App.getInstance().updateCloneData(createDTOFromGUI(), new MarkerEditCallBack<CloneDTO>("Failed to update clone: ") {
                        @Override
                        public void onFailure(Throwable throwable) {
                            super.onFailure(throwable);    //To change body of overridden methods use File | Settings | File Templates.
                            notWorking();
                        }

                        public void onSuccess(CloneDTO updatedDTO) {
                            setDTO(updatedDTO);
                            clearError();
                            notWorking();
                            revertGUI();
                        }
                    });
                } else {
                    setError("No fields changed.  Not sending update.");
                }
            }
        });

        revertButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                revertGUI();
                handleDirty();
            }
        });
    }

    public boolean isDirty() {
//            if (cloneRatingListBox.isDirty(dto.getRating())) return true;
//            if (cloneComments.isDirty(dto.getCloneComments())) return true;
        boolean isDirty = false ;
        isDirty = (vectorListBox.isDirty(dto.getVectorName()) || isDirty)  ;
        isDirty = (digestListBox.isDirty(dto.getDigest())|| isDirty);
        isDirty = (libraryListBox.isDirty(dto.getProbeLibraryName())|| isDirty);
        isDirty = (polymeraseListBox.isDirty(dto.getPolymerase())|| isDirty);
        isDirty = (insertSizeTextBox.isDirty(dto.getInsertSize())|| isDirty);
        isDirty = (pcrAmplificationTextBox.isDirty(dto.getPcrAmplification())|| isDirty);
        isDirty = (cloningSiteListBox.isDirty(dto.getCloningSite())|| isDirty);
        return isDirty ;
    }



    protected CloneDTO createDTOFromGUI() {
        CloneDTO newCloneDTO = new CloneDTO();
        newCloneDTO.setZdbID(dto.getZdbID());
        newCloneDTO.setRating(cloneRatingListBox.getSelected());
        newCloneDTO.setVectorName(vectorListBox.getSelected());
        newCloneDTO.setDigest(digestListBox.getSelected());
        newCloneDTO.setProbeLibraryName(libraryListBox.getSelected());
        newCloneDTO.setPolymerase(polymeraseListBox.getSelected());
        newCloneDTO.setInsertSize(insertSizeTextBox.getBoxValue());
        newCloneDTO.setCloneComments(cloneComments.getBoxValue());
        newCloneDTO.setPcrAmplification(pcrAmplificationTextBox.getBoxValue());
        newCloneDTO.setCloningSite(cloningSiteListBox.getSelected());

        // do not assing
        return newCloneDTO;
    }

    public void revertGUI() {
        if (dto != null) {
            cloneRatingListBox.setIndexForValue(dto.getRating());
            libraryListBox.setIndexForValue(dto.getProbeLibraryName());
            vectorListBox.setIndexForValue(dto.getVectorName());
            digestListBox.setIndexForValue(dto.getDigest());
            polymeraseListBox.setIndexForValue(dto.getPolymerase());
            insertSizeTextBox.setText((dto.getInsertSize() == null ? "" : dto.getInsertSize().toString()));
            cloneComments.setText(dto.getCloneComments());
            pcrAmplificationTextBox.setText(dto.getPcrAmplification());
            cloningSiteListBox.setIndexForValue(dto.getCloningSite());
        }
    }

    public boolean handleDirty() {
        boolean dirty = isDirty();
        saveButton.setEnabled(dirty);
        revertButton.setEnabled(dirty);
        if (false == dirty) {
            fireEventSuccess();
        }
        return dirty ;
    }


}

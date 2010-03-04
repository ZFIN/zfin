package org.zfin.gwt.marker.ui;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.RootPanel;
import org.zfin.gwt.root.dto.AntibodyDTO;
import org.zfin.gwt.root.dto.AntibodyTypesDTO;
import org.zfin.gwt.root.ui.HandlesError;
import org.zfin.gwt.root.ui.StringListBox;

/**
 * Data box for antibody data.
 */
public class AntibodyBox extends AbstractDataBox<AntibodyDTO>{

    // table
    private final Grid table = new Grid(3, 4);
    private final StringListBox hostOrganismListBox= new StringListBox();
    private final StringListBox heavyChainListBox = new StringListBox();
    private final StringListBox lightChainListBox = new StringListBox();
    private final StringListBox typeListBox = new StringListBox();
    private final StringListBox immunogenOrganismListBox = new StringListBox();


    public AntibodyBox() {
        super();
        initGUI();
        setValues();
        addInternalListeners(this);
        initWidget(panel);
        RootPanel.get(StandardDivNames.dataDiv).add(this);
    }

    protected void initGUI() {
        super.initGUI();
        table.setText(0, 0, "Host Organism:");
        table.setWidget(0, 1, hostOrganismListBox);
        table.setHTML(0, 2, "<b>against</b> Immunogen Organism:");
        table.setWidget(0, 3, immunogenOrganismListBox);
        table.setText(1, 0, "Isotype Heavy Chain:");
        table.setWidget(1, 1, heavyChainListBox);
        table.setText(2, 0, "Isotype Light Chain:");
        table.setWidget(2, 1, lightChainListBox);
        table.setText(2, 2, "Type:");
        table.setWidget(2, 3, typeListBox);

        panel.setStyleName("gwt-editbox");

        panel.add(table);
        panel.add(buttonPanel);
        panel.add(errorLabel);
    }

    @Override
    protected void setValues() {

        AntibodyRPCService.App.getInstance().getAntibodyTypes(new AsyncCallback<AntibodyTypesDTO>() {
            public void onFailure(Throwable throwable) {
                Window.alert("failure to load clone types: " + throwable);
            }

            public void onSuccess(AntibodyTypesDTO antibodyTypesDTO) {
                hostOrganismListBox.addNullAndItems(antibodyTypesDTO.getHostOrganisms());
                immunogenOrganismListBox.addNullAndItems(antibodyTypesDTO.getImmunogenOrganisms());
                heavyChainListBox.addNullAndItems(antibodyTypesDTO.getHeavyChains());
                lightChainListBox.addNullAndItems(antibodyTypesDTO.getLightChains());
                typeListBox.addNullAndItems(antibodyTypesDTO.getTypes());

                revertGUI();
            }
        });
    }

    @Override
    protected void addInternalListeners(HandlesError handlesError) {

        hostOrganismListBox.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                handleDirty();
            }
        });
        immunogenOrganismListBox.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                handleDirty();
            }
        });
        heavyChainListBox.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                handleDirty();
            }
        });
        lightChainListBox.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                handleDirty();
            }
        });
        typeListBox.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                handleDirty();
            }
        });


        saveButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                if (isDirty()) {
                    working();
                    AntibodyRPCService.App.getInstance().updateAntibodyData(createDTOFromGUI(), new MarkerEditCallBack<AntibodyDTO>("Failed to update antibody: ") {
                        @Override
                        public void onFailure(Throwable throwable) {
                            super.onFailure(throwable);    //To change body of overridden methods use File | Settings | File Templates.
                            notWorking();
                        }

                        public void onSuccess(AntibodyDTO updatedDTO) {
                            setDTO(updatedDTO);
                            revertGUI();
                            clearError();
                            notWorking();
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
        boolean isDirty = false ;
        isDirty = (hostOrganismListBox.isDirty(dto.getHostOrganism()) || isDirty);
        isDirty = (immunogenOrganismListBox.isDirty(dto.getImmunogenOrganism())|| isDirty) ;
        isDirty = (heavyChainListBox.isDirty(dto.getHeavyChain())|| isDirty) ;
        isDirty = (lightChainListBox.isDirty(dto.getLightChain())|| isDirty) ;
        isDirty = (typeListBox.isDirty(dto.getType())|| isDirty) ;
        return isDirty ;
    }


    protected AntibodyDTO createDTOFromGUI() {
        AntibodyDTO antibodyDTO = new AntibodyDTO();
        antibodyDTO.setZdbID(dto.getZdbID());
        antibodyDTO.setHostOrganism(hostOrganismListBox.getSelected());
        antibodyDTO.setImmunogenOrganism(immunogenOrganismListBox.getSelected());
        antibodyDTO.setHeavyChain(heavyChainListBox.getSelected());
        antibodyDTO.setLightChain(lightChainListBox.getSelected());
        antibodyDTO.setType(typeListBox.getSelected());

        // do not assing
        return antibodyDTO;
    }

    // todo: implement

    protected void revertGUI() {
        if (dto != null) {
            hostOrganismListBox.setIndexForText(dto.getHostOrganism());
            immunogenOrganismListBox.setIndexForText(dto.getImmunogenOrganism());
            heavyChainListBox.setIndexForText(dto.getHeavyChain());
            lightChainListBox.setIndexForText(dto.getLightChain());
            typeListBox.setIndexForText(dto.getType());
        }
    }

    public void revert(){
        revertGUI();
    }


    @Override
    public void working() {
        super.working();
        hostOrganismListBox.setEnabled(false);
        immunogenOrganismListBox.setEnabled(false);
        heavyChainListBox.setEnabled(false);
        lightChainListBox.setEnabled(false);
        typeListBox.setEnabled(false);
    }

    @Override
    public void notWorking() {
        super.notWorking();    //To change body of overridden methods use File | Settings | File Templates.
        hostOrganismListBox.setEnabled(true);
        immunogenOrganismListBox.setEnabled(true);
        heavyChainListBox.setEnabled(true);
        lightChainListBox.setEnabled(true);
        typeListBox.setEnabled(true);
    }
}
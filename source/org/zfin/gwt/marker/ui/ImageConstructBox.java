package org.zfin.gwt.marker.ui;

import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.gwt.root.dto.ImageDTO;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.event.RelatedEntityAdapter;
import org.zfin.gwt.root.event.RelatedEntityEvent;
import org.zfin.gwt.root.ui.*;
import org.zfin.gwt.root.util.StringUtils;



public class ImageConstructBox extends AbstractStackComposite<ImageDTO> { //RelatedEntityBox {

    private LookupComposite constructInput = new LookupComposite(false);
    private VerticalPanel panel = new VerticalPanel();

    public ImageConstructBox(String div) {
        super();
        initGUI();
        addInternalListeners();
        initWidget(panel);
        RootPanel.get(div).add(this);
    }

    public void initGUI() {
        constructInput.setType(LookupStrings.CONSTRUCT_LOOKUP);
        constructInput.setInputName("construct_search");
        constructInput.setShowError(true);
        constructInput.setWildCard(false);
        constructInput.initGui();

        errorLabel.setStyleName("error");
        panel.setStyleName("gwt-stack-composite");

        addPanel.add(constructInput);
        addPanel.add(addButton);

        panel.add(stackTable);
        panel.add(addPanel);
        panel.add(errorLabel);
    }

    public void addInternalListeners() {
        constructInput.setAction(new SubmitAction() {
            public void doSubmit(String value) {
                sendUpdates();
            }
        });
        constructInput.setOnclick(null);
    }

    @Override
    public void setDTO(ImageDTO dto) {
        super.setDTO(dto);
        constructInput.setPubZdb(dto.getPublicationZdbID());
        setValues();
    }

    @Override
    protected void revertGUI() {
        stackTable.clear();
        for (MarkerDTO marker : dto.getConstructs()) {
            addToGUI(marker.getName());
        }
    }

    @Override
    protected void setValues() { }

    @Override
    public void resetInput() {
        constructInput.setText("");
        constructInput.getTextBox().setFocus(true);
    }

    @Override
    public void sendUpdates() {
        final String valueToAdd = constructInput.getText().trim();
        if(containsName(valueToAdd)){
            setError("Supplied already added: "+valueToAdd);
            notWorking();
            return;
        }
        working();
        ImageRPCService.App.getInstance().addConstruct(valueToAdd, dto.getZdbID(),
                new MarkerEditCallBack<MarkerDTO>("failed to add construct to image: ") {
                    @Override
                    public void onSuccess(MarkerDTO result) {
                        addToGUI(result.getName());
                        notWorking();
                        resetInput();
                    }
                });
    }

    @Override
    protected void addToGUI(String name) {
        ImageDTO newDto = createDTOFromGUI();
        newDto.setName(name);
        StackComposite<ImageDTO> stackComposite = new StackComposite<ImageDTO>(newDto);

        //This is the delete listener
        stackComposite.addRelatedEntityListener(new RelatedEntityAdapter<ImageDTO>() {
            @Override
            public void removeRelatedEntity(final RelatedEntityEvent<ImageDTO> event) {

                final String value = event.getDTO().getName();
                ImageRPCService.App.getInstance().removeConstruct(value, event.getDTO().getZdbID(),
                        new MarkerEditCallBack<Void>("failed to remove term from image: ") {
                            public void onSuccess(Void o) {
                                removeFromGUI(value);
                            }
                        });
            }
        });

        stackTable.setWidget(stackTable.getRowCount(), 0, stackComposite);

    }

    @Override
    protected ImageDTO createDTOFromGUI() {
        if (dto==null) return null;
        ImageDTO imageDTO = new ImageDTO();
        if (StringUtils.isNotEmpty(constructInput.getText().trim()))
            imageDTO.setName(constructInput.getText().trim());
        imageDTO.setZdbID(dto.getZdbID());
        return imageDTO;
    }

    public void working() {
        addButton.setEnabled(false);
        constructInput.setEnabled(false);
    }

    public void notWorking() {
        addButton.setEnabled(true);
        constructInput.setEnabled(true);
    }



}

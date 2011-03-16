package org.zfin.gwt.marker.ui;

import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.zfin.gwt.root.dto.ImageDTO;
import org.zfin.gwt.root.dto.OntologyDTO;
import org.zfin.gwt.root.dto.TermDTO;
import org.zfin.gwt.root.event.RelatedEntityAdapter;
import org.zfin.gwt.root.event.RelatedEntityEvent;
import org.zfin.gwt.root.ui.*;
import org.zfin.gwt.root.util.StringUtils;



public class ImageAnatomyBox extends AbstractStackComposite<ImageDTO> { //RelatedEntityBox {

    private LookupComposite termInput = new LookupComposite();

    private VerticalPanel panel = new VerticalPanel();

    public ImageAnatomyBox(String div) {
        super();
        initGUI();
        addInternalListeners();
        initWidget(panel);
        RootPanel.get(div).add(this);
    }

    public void initGUI() {
        termInput.setOntology(OntologyDTO.ANATOMY);
        termInput.setType(LookupComposite.GDAG_TERM_LOOKUP);
        termInput.setShowError(true);
        termInput.setWildCard(false);
        termInput.initGui();

        errorLabel.setStyleName("error");
        panel.setStyleName("gwt-stack-composite");

        addPanel.add(termInput);
        addPanel.add(addButton);

        panel.add(stackTable);
        panel.add(addPanel);
        panel.add(errorLabel);



    }

    public void addInternalListeners() {
        termInput.setAction(new SubmitAction() {
            public void doSubmit(String value) {
                sendUpdates();
            }
        });
        termInput.setOnclick(null);
    }



    //@Override
    public void setDTO(ImageDTO dto) {
        super.setDTO(dto);
        setValues();
    }

    @Override
    protected void revertGUI() {
        stackTable.clear();
        for (TermDTO term : dto.getAnatomyTerms()) {
            addToGUI(term.getName());
        }
    }

    @Override
    protected void setValues() {

    }

    @Override
    public void resetInput() {
        termInput.setText("");
        termInput.getTextBox().setFocus(true);
    }

    @Override
    public void sendUpdates() {
        final String valueToAdd = termInput.getText().trim();
        if(containsName(valueToAdd)){
            setError("Supplied already added: "+valueToAdd);
            notWorking();
            return;
        }
        working();
        ImageRPCService.App.getInstance().addTerm(valueToAdd, dto.getZdbID(),
                new MarkerEditCallBack<TermDTO>("failed to add term to image: ") {
                    @Override
                    public void onSuccess(TermDTO result) {
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
                ImageRPCService.App.getInstance().removeTerm(value, event.getDTO().getZdbID(),
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
        if (StringUtils.isNotEmpty(termInput.getText().trim()))
            imageDTO.setName(termInput.getText().trim());
        imageDTO.setZdbID(dto.getZdbID());
        return imageDTO;
    }

    public void working() {
        addButton.setEnabled(false);
        termInput.setEnabled(false);
    }

    public void notWorking() {
        addButton.setEnabled(true);
        termInput.setEnabled(true);
    }



}



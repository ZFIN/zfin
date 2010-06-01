package org.zfin.gwt.curation.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import org.zfin.gwt.root.dto.GoEvidenceDTO;
import org.zfin.gwt.root.ui.MarkerEditCallBack;
import org.zfin.gwt.root.ui.MarkerGoEvidenceRPCService;
import org.zfin.gwt.root.ui.Revertible;


/**
 */
public class GoActionComposite extends Composite implements Revertible {

    private final String imageURL = "/images/";
    // gui
    private HorizontalPanel panel = new HorizontalPanel();
    private final Image cloneButton = new Image(imageURL + "clone1.png");
    private final Image editButton = new Image(imageURL + "edit.png");
    private final Image deleteButton = new Image(imageURL + "delete-button.png");

    // data
    protected GoEvidenceDTO dto;
    private GoViewTable parent;
    private int rowNumber;

    public GoActionComposite(GoViewTable parent, GoEvidenceDTO goEvidenceDTO, int rowNumber) {
        this.parent = parent;
        this.dto = goEvidenceDTO;
        this.rowNumber = rowNumber;
        initGUI();
        addInternalListeners();
        initWidget(panel);
    }


    private void initGUI() {
        cloneButton.setStyleName("relatedEntityPubLink");
        cloneButton.setTitle("Clone annotation.");
        cloneButton.setSize("20px", "20px");
        panel.add(cloneButton);
        editButton.setStyleName("relatedEntityPubLink");
        editButton.setTitle("Edit annotation.");
        editButton.setSize("20px", "20px");
        panel.add(editButton);
        deleteButton.setStyleName("relatedEntityPubLink");
        deleteButton.setTitle("Remove annotation.");
        panel.add(deleteButton);
    }

    private void addInternalListeners() {
        deleteButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (false == Window.confirm("Delete GO annotation?")) return;
                working();
                MarkerGoEvidenceRPCService.App.getInstance().deleteMarkerGoTermEvidence(dto.getZdbID(),
                        new MarkerEditCallBack<Void>("Failed to remove evidence: " + dto.getZdbID()) {
                            @Override
                            public void onFailure(Throwable throwable) {
                                super.onFailure(throwable);
                                notWorking();
                            }

                            @Override
                            public void onSuccess(Void result) {
                                // this should remove the buttons anyway
                                parent.clearError();
                            }
                        });
            }
        });

        cloneButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                parent.cloneGO(getDto(), rowNumber);
            }
        });

        editButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                parent.editGO(getDto(), rowNumber);
            }
        });

    }

    public GoEvidenceDTO getDto() {
        return dto;
    }

    public void setDto(GoEvidenceDTO dto) {
        this.dto = dto;
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public boolean handleDirty() {
        return false;
    }

    @Override
    public void working() {
        cloneButton.setVisible(false);
        deleteButton.setVisible(false);
        editButton.setVisible(false);
    }

    @Override
    public void notWorking() {
        cloneButton.setVisible(true);
        deleteButton.setVisible(true);
        editButton.setVisible(true);
    }
}

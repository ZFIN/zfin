package org.zfin.gwt.marker.ui;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.ui.Revertible;

/**
 * Base component that is revertible.
 */
public abstract class AbstractRevertibleComposite<T extends MarkerDTO> extends AbstractComposite<T> implements Revertible {


    // GUI name/type elements
    final HorizontalPanel buttonPanel = new HorizontalPanel();
    final Button saveButton = new Button(TEXT_SAVE);
    final Button revertButton = new Button(TEXT_REVERT);


    public void working() {
        saveButton.setEnabled(false);
        revertButton.setEnabled(false);
        saveButton.setText(TEXT_WORKING);
    }

    public void notWorking() {
        saveButton.setText(TEXT_SAVE);
    }

    class CompareCommand implements Command {
        public void execute() {
            if (isDirty()) {
                saveButton.setEnabled(true);
                revertButton.setEnabled(true);
            } else {
                saveButton.setEnabled(false);
                revertButton.setEnabled(false);
                clearError();
            }
        }
    }


    /**
     * If the components are dirty then enable the save/revert buttons.
     * If not, the fire success.
     * @return Component has dirty data.
     */
    public boolean checkDirty() {
        boolean dirty = isDirty();
        saveButton.setEnabled(dirty);
        revertButton.setEnabled(dirty);
        if (false == dirty) {
            fireEventSuccess();
        }
        return dirty ;
    }


    @Override
    public void setDTO(T dto) {
        super.setDTO(dto);
        checkDirty();
    }
}

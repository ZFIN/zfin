package org.zfin.gwt.root.ui;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import org.zfin.gwt.root.dto.RelatedEntityDTO;

/**
 * Base component that is revertible.
 */
public abstract class AbstractRevertibleComposite<T extends RelatedEntityDTO> extends AbstractComposite<T> implements Revertible {


    // GUI name/type elements
    protected final HorizontalPanel buttonPanel = new HorizontalPanel();
    protected final Button saveButton = new Button(TEXT_SAVE);
    protected final Button revertButton = new Button(TEXT_REVERT);


    public void working() {
        saveButton.setEnabled(false);
        revertButton.setEnabled(false);
        saveButton.setText(TEXT_WORKING);
    }

    public void notWorking() {
        saveButton.setText(TEXT_SAVE);
    }

    public class CompareCommand implements Command {
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
     *
     * @return Component has dirty data.
     */
    public boolean handleDirty() {
        boolean dirty = isDirty();
        saveButton.setEnabled(dirty);
        revertButton.setEnabled(dirty);
        if (false == dirty) {
            fireEventSuccess();
        }
        return dirty;
    }


    @Override
    public void setDTO(T dto) {
        super.setDTO(dto);
        handleDirty();
    }
}

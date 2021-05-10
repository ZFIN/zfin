package org.zfin.gwt.root.ui;

import java.util.Set;

/**
 * A list of inferences.
 * In the first box, can be a set of inferences and the "entry" box is open text field
 * In the second box, will be the entry field and only visible if not ZFIN.
 * In the third box, will be a drop-down and only visible IF ZFIN.
 * <p/>
 * Everything available will be related to that pub.
 * However, if free-text can also be entered and it will be associated with whatever is selected in the drop-down.
 */
public class InferenceListBox extends AbstractInferenceListBox {


    public InferenceListBox(String div) {
        super(div);
    }

    public InferenceListBox() {
        this(StandardDivNames.directAttributionDiv);
        stackTable.setStyleName(DIRTY_STYLE);
    }


    @Override
    public boolean isDirty() {

        // set them all to dirty, or not
        Set<String> inferences = dto.getInferredFrom();
        if (inferences == null && stackTable.getRowCount() > 0) {
            return setStackToDirty(true);
        }
        if (stackTable.getRowCount() != inferences.size()) {
            return setStackToDirty(true);
        }

        // now we have to check for an individual match
        for (String inference : inferences) {
            if (false == containsName(inference)) {
                return setStackToDirty(true);
            }
        }
        return setStackToDirty(false);
    }


}
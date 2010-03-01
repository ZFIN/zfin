package org.zfin.gwt.marker.ui;

import org.zfin.gwt.root.ui.HandlesError;

import java.util.Set;

/**
 * A list of inferences.
 * In the first box, can be a set of inferences and the "entry" box is open text field
 * In the second box, will be the entry field and only visible if not ZFIN.
 * In the third box, will be a drop-down and only visible IF ZFIN.
 *
 * Everything available will be related to that pub.
 * However, if free-text can also be entered and it will be associated with whatever is selected in the drop-down.
 *
 */
public class InferenceListBoxComposite extends AbstractInferenceListBox{


    public InferenceListBoxComposite(String div){
        super(div);
    }

    public InferenceListBoxComposite() {
        this(StandardDivNames.directAttributionDiv) ;
        stackTable.setStyleName(DIRTY_STYLE);
    }


    @Override
    public void addInternalListeners(HandlesError handlesError) {
        super.addInternalListeners(this);

        // add additional button for handling button adds?
    }

//    @Override
//    public boolean isDirty(GoEvidenceDTO externalDTO) {
//        if(externalDTO==null) return false ;
//        Set<String> inferences = externalDTO.getInferredFrom();
//        for(String internalInference : dto.getInferredFrom()){
//            if(false == inferences.contains(internalInference)){
//                return true ;
//            }
//        }
//        return false ;
//    }

    @Override
    public boolean isDirty() {

        // set them all to dirty, or not
        Set<String> inferences =  dto.getInferredFrom();
        if(inferences==null && stackTable.getRowCount()>0){
            return setStackToDirty(true) ;
        }
        if(stackTable.getRowCount()!=inferences.size()){
            return setStackToDirty(true) ;
        }

        // now we have to check for an individual match
        for(String inference: inferences){
            if(false==containsName(inference)){
                return setStackToDirty(true);
            }
        }
        return setStackToDirty(false);
    }

    private boolean setStackToDirty(boolean dirty) {
        int rowCount = stackTable.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            ((StackComposite) stackTable.getWidget(i, 0)).setDirty(dirty);
        }
        availableList.setStyleName( (dirty? DIRTY_STYLE : CLEAN_STYLE) );
        lookupBox.setStyleName( (dirty? DIRTY_STYLE : CLEAN_STYLE) );
        addButton.setStyleName( (dirty? DIRTY_STYLE : CLEAN_STYLE) );
        inferenceCategoryList.setStyleName( (dirty? DIRTY_STYLE : CLEAN_STYLE) );
        return dirty ;
    }

}
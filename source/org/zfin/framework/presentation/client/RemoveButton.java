package org.zfin.framework.presentation.client;

import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Command;


/**
 */
public class RemoveButton extends PushButton {


    //    public RemoveButton(String text, LookupTable parentTable,int rowCount){
    public RemoveButton(Image image, HasRemoveTerm parentTable,String term){
        super(image) ;
        addClickListener(new RowClickListener(parentTable,term)) ;
    }

    public class RowClickListener implements ClickListener{

        private HasRemoveTerm parentTable ;
        private String term ;

        public RowClickListener(HasRemoveTerm parentTable,String term){
            this.parentTable = parentTable ;
            this.term = term ;
        }
        public void onClick(Widget sender){
            DeferredCommand.addCommand(new Command(){
                public void execute(){
                    parentTable.removeTermFromTable(term) ;
                }
            });
        }


    }


}

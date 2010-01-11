package org.zfin.gwt.lookup.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PushButton;


/**
 */
public class RemoveButton extends PushButton {


    public RemoveButton(Image image, HasRemoveTerm parentTable, String term) {
        super(image);
        addClickHandler(new RowClickListener(parentTable, term));
    }

    public class RowClickListener implements ClickHandler {

        private HasRemoveTerm parentTable;
        private String term;

        public RowClickListener(HasRemoveTerm parentTable, String term) {
            this.parentTable = parentTable;
            this.term = term;
        }

        public void onClick(ClickEvent event) {
            DeferredCommand.addCommand(new Command() {
                public void execute() {
                    parentTable.removeTermFromTable(term);
                }
            });
        }


    }


}

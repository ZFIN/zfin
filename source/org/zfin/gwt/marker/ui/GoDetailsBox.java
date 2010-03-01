package org.zfin.gwt.marker.ui;

import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.RootPanel;
import org.zfin.gwt.root.dto.GoEvidenceDTO;
import org.zfin.gwt.root.ui.HandlesError;


/**
 * Here, we display go details in an html frame.
 */
public class GoDetailsBox extends AbstractComposite<GoEvidenceDTO>{

    private Frame details = new Frame();
    private final String ONTOLOGY = "GO";

    private String prefix = "http://zfin.org/phenote/Phenote/?ontologyName="+ONTOLOGY+"&termId="+ONTOLOGY+":" ;

    public GoDetailsBox(){
        this(StandardDivNames.dataDiv)  ;
    }

    public GoDetailsBox(String div){
        super() ;
        initGUI();
        setValues();
        addInternalListeners(this);
        initWidget(panel);
        if(div!=null){
            RootPanel.get(div).add(this);
        }
    }

    @Override
    protected void initGUI() {
        details.setWidth("600px");
        details.setHeight("600px");
        panel.add(details);
    }

    @Override
    protected void revertGUI() {
        if(dto.getGoTerm()!=null){
            details.setUrl(prefix+dto.getGoTerm().getDataZdbID());
        }
    }

    @Override
    protected void setValues() { }

    @Override
    protected void addInternalListeners(HandlesError handlesError) { }
}

package org.zfin.gwt.marker.ui;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.i18n.client.Dictionary;
import org.zfin.gwt.root.ui.TestComposite;

/**
 */
public class MarkerEditEntryPoint implements EntryPoint {

    // lookup
    private static final String LOOKUP_ZDBID = "zdbID" ;

    // internal data

    public void onModuleLoad() {

        String zdbID ;
        String state = null ;

        Dictionary dictionary = Dictionary.getDictionary("MarkerProperties") ;
        zdbID = dictionary.get(LOOKUP_ZDBID) ;
        try {
            state = dictionary.get(GoMarkerEditController.STATE_STRING) ;
        } catch (Exception e) {
        }

        if(zdbID.indexOf("TSCRIPT")>0){
            (new TranscriptEditController()).initGUI();
        }
        else
        if(zdbID.indexOf("GENE")>0){
            if(zdbID.startsWith("Alternate")){
                (new AlternateGeneEditController()).initGUI();
            }
            else
            if(state!=null ){
                (new GoMarkerEditController()).initGUI();
            }
            else{
                (new GeneEditController()).initGUI();
            }
        }
        else
        if(zdbID.indexOf("ATB")>0){
            (new AntibodyEditController()).initGUI();
        }
        else
        if (zdbID.indexOf("IMAGE")>=0) {
           (new ImageEditController()).initGUI();
        }
        else
        if(zdbID.equals("test")){
            (new TestComposite()).initGUI();
        }
        else{
            (new CloneEditController()).initGUI();
        }

    }
}

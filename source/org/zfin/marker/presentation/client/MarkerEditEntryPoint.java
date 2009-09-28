package org.zfin.marker.presentation.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.i18n.client.Dictionary;

/**
 */
public class MarkerEditEntryPoint implements EntryPoint {

    // lookup
    private static final String LOOKUP_ZDBID = "zdbID" ;

    // internal data

    public void onModuleLoad() {

        String zdbID ;

        Dictionary dictionary = Dictionary.getDictionary("MarkerProperties") ;
        zdbID = dictionary.get(LOOKUP_ZDBID) ;

        if(zdbID.indexOf("TSCRIPT")>0){
            (new TranscriptEditController()).initGUI();
        }
        else
        if(zdbID.indexOf("GENE")>0){
            (new GeneEditController()).initGUI();
        }
        else{
            (new CloneEditController()).initGUI();
        }

    }
}

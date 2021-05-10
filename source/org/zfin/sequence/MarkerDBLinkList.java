package org.zfin.sequence;

import java.util.ArrayList;
import java.util.Collection;

/**
 *  Just a wrapper around an arraylist so that we can pass to a JSP tag.
 */
public class MarkerDBLinkList extends ArrayList<MarkerDBLink> {

    public MarkerDBLinkList(){}

    public MarkerDBLinkList(Collection collection){
        super(collection) ;
    }

}

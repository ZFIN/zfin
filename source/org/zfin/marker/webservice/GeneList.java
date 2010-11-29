package org.zfin.marker.webservice;

import org.zfin.marker.Marker;

import java.util.ArrayList;
import java.util.List;

/**
 *  This is named for XML conversion.
 */
public class GeneList extends ArrayList<Gene>{
    
    // no-arg constructor required for marshalling
    public GeneList(){}

//    private List<Gene> markers = new ArrayList<Gene>();

    public GeneList(List<Marker> markerList){
        for(Marker m: markerList){
            add(new Gene(m)) ;
        }
    }

}

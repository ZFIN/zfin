package org.zfin.marker.webservice;

import org.apache.log4j.Logger;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.marker.Marker;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * This is named for XML-conversion
*/
public class Gene {

    private final static Logger logger = Logger.getLogger(Gene.class) ;

    private String zdbId;
    private String name;
    private String abbreviation;
    private String link ;
    private List<Anatomy> anatomy ;

    // no-arg constructor required for marshalling
    public Gene(){}

    public Gene(Marker marker){
        zdbId = marker.getZdbID() ;
        name = marker.getName() ;
        abbreviation = marker.getAbbreviation() ;
        link = EntityPresentation.getJumpToLink(marker.getZdbID()) ;
        anatomy = new ArrayList<Anatomy>() ;
        List<AnatomyItem> anatomyItems= RepositoryFactory.getExpressionRepository().getAnatomyForMarker(marker.getZdbID()) ;
        for(AnatomyItem anatomyItem: anatomyItems){
            anatomy.add(new Anatomy(anatomyItem)) ;
        }
    }

    public String getZdbId() {
        return zdbId;
    }

    public void setZdbId(String zdbId) {
        this.zdbId = zdbId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public List<Anatomy> getAnatomy() {
        return anatomy;
    }

    public void setAnatomy(List<Anatomy> anatomy) {
        this.anatomy = anatomy;
    }
}

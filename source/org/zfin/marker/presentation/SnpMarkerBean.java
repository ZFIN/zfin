package org.zfin.marker.presentation;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.zfin.sequence.MarkerSequence;

import java.util.List;

/**
 */
@Controller
public class SnpMarkerBean extends MarkerBean{

    private Logger logger = Logger.getLogger(SnpMarkerBean.class);

    private String variant ;
    private List<MarkerSequence> sequences;
    private String ncbiBlastUrl ;
    private String snpBlastUrl ;

    /**
     * Most of the time there will only be a single sequence.
     * @return
     */
    public MarkerSequence getSequence(){
        if(sequences!=null && sequences.size()>0){
            if(sequences.size()>1){
                logger.error("more than 1 sequence for marker: " + marker);
            }
            return sequences.get(0);
        }
        else{
            return null ;
        }
    }


    public String getVariant() {
        return variant;
    }

    public void setVariant(String variant) {
        this.variant = variant;
    }

    public List<MarkerSequence> getSequences() {
        return sequences;
    }

    public void setSequences(List<MarkerSequence> sequences) {
        this.sequences = sequences;
    }

    public String getNcbiBlastUrl() {
        return ncbiBlastUrl;
    }

    public void setNcbiBlastUrl(String ncbiBlastUrl) {
        this.ncbiBlastUrl = ncbiBlastUrl;
    }

    public String getSnpBlastUrl() {
        return snpBlastUrl;
    }

    public void setSnpBlastUrl(String snpBlastUrl) {
        this.snpBlastUrl = snpBlastUrl;
    }
}
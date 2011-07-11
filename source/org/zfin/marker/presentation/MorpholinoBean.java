package org.zfin.marker.presentation;

import org.apache.log4j.Logger;
import org.zfin.marker.Marker;
import org.zfin.sequence.MarkerSequence;
import org.zfin.sequence.blast.Database;

import java.util.List;
import java.util.Set;

/**
 */
public class MorpholinoBean extends MarkerBean{

    private Logger logger = Logger.getLogger(MorpholinoBean.class);

    private Set<Marker> targetGenes ;
    private List<MarkerSequence> sequences;
    private String ncbiBlastUrl;
    private String sequenceAttribution;
    private List<Database> databases;

    public Set<Marker> getTargetGenes() {
        return targetGenes;
    }

    public void setTargetGenes(Set<Marker> targetGenes) {
        this.targetGenes = targetGenes;
    }

    public List<MarkerSequence> getSequences() {
        return sequences;
    }

    public void setSequences(List<MarkerSequence> sequences) {
        this.sequences = sequences;
    }

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

    public String getNcbiBlastUrl() {
        return ncbiBlastUrl;
    }

    public void setNcbiBlastUrl(String ncbiBlastUrl) {
        this.ncbiBlastUrl = ncbiBlastUrl;
    }

    public List<Database> getDatabases() {
        return databases;
    }

    public void setDatabases(List<Database> databases) {
        this.databases = databases;
    }

    public String getSequenceAttribution() {
        return sequenceAttribution;
    }

    public void setSequenceAttribution(String sequenceAttribution) {
        this.sequenceAttribution = sequenceAttribution;
    }
}

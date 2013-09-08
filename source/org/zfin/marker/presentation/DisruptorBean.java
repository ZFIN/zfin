package org.zfin.marker.presentation;

import org.apache.log4j.Logger;
import org.zfin.marker.Marker;
import org.zfin.profile.MarkerSupplier;
import org.zfin.sequence.MarkerSequence;
import org.zfin.sequence.blast.Database;

import java.util.List;
import java.util.Set;

/**
 */
public class DisruptorBean extends MarkerBean{

    private Logger logger = Logger.getLogger(DisruptorBean.class);

    private Set<Marker> targetGenes ;
    private List<MarkerSequence> sequences;
    private List<MarkerSupplier> suppliers;
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

    public boolean isMorpholino() {
        if (this.marker.getType().isMarkerType("MRPHLNO")) {
            return true;
        }

        return false;
    }

    public boolean isTALEN() {
        if (this.marker.getType().isMarkerType("TALEN")) {
            return true;
        }

        return false;
    }

     public boolean isCRISPR() {
        if (this.marker.getType().isMarkerType("CRISPR")) {
            return true;
        }

        return false;
    }

    public List<MarkerSupplier> getSuppliers() {
        return suppliers;
    }

    public void setSuppliers(List<MarkerSupplier> suppliers) {
        this.suppliers = suppliers;
    }
}


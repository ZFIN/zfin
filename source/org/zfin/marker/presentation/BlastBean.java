package org.zfin.marker.presentation;

import org.zfin.sequence.Sequence;
import org.zfin.sequence.blast.Database;
import org.zfin.marker.Transcript;
import org.zfin.marker.Marker;

import java.util.Map;
import java.util.List;

/**
 * This bean contains data necessary for executing a blast query.
 */
public class BlastBean {

    private Map<String,String> hiddenProperties;
    private List<Sequence> sequences ;
    private Database database ; /// database to blast against

    private Marker gene ;
    private Transcript transcript;

    public Map<String, String> getHiddenProperties() {
        return hiddenProperties;
    }

    public void setHiddenProperties(Map<String, String> hiddenProperties) {
        this.hiddenProperties = hiddenProperties;
    }

    public Sequence getSequence() {
        return ( ( sequences==null || sequences.size()==0 ) ? null : sequences.get(0)) ;
    }

    public List<Sequence> getSequences() {
        return sequences;
    }

    public void setSequences(List<Sequence> sequences) {
        this.sequences = sequences;
    }

    public Marker getGene() {
        return gene;
    }

    public void setGene(Marker gene) {
        this.gene = gene;
    }

    public Transcript getTranscript() {
        return transcript;
    }

    public void setTranscript(Transcript transcript) {
        this.transcript = transcript;
    }

    public Database getDatabase() {
        return database;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }
}

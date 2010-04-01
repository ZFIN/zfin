package org.zfin.sequence.blast;

/**
 *
 */
public class BlastRegenerationCache {
    private long id ;
    private String accession ;
    private Database blastDatabase ;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAccession() {
        return accession;
    }

    public void setAccession(String accession) {
        this.accession = accession;
    }

    public Database getBlastDatabase() {
        return blastDatabase;
    }

    public void setBlastDatabase(Database blastDatabase) {
        this.blastDatabase = blastDatabase;
    }
}

/**
 *  Class Query.
 */
package org.zfin.sequence.blast ; 

import org.zfin.sequence.Accession;
import org.zfin.sequence.reno.RunCandidate ;

import java.util.Set ;
import java.util.HashSet ;

public class Query implements Comparable {

    private String zdbID ; 
    private Accession accession  ; 
    private RunCandidate runCandidate; 
    private Set<Hit> blastHits = new HashSet<Hit>() ; 

    public String getName() {
        return accession.getNumber();
    }

    public boolean getValidBlastHit(){
        if(blastHits.size()>0){
           if(false==blastHits.iterator().next().getTargetAccession().getNumber().equals("NONE")){
               return false ;
           }
        }
        return true ; 
    }


    public String getURL() {
        return accession.getURL();
    }
    
    /**
     * Get accession.
     *
     * @return accession as String.
     */
    public Accession getAccession()
    {
        return accession;
    }
    
    /**
     * Set accession.
     *
     * @param accession the value to set.
     */
    public void setAccession(Accession accession)
    {
        this.accession = accession;
    }
    
    /**
     * Get zdbID.
     *
     * @return zdbID as String.
     */
    public String getZdbID()
    {
        return zdbID;
    }
    
    /**
     * Set zdbID.
     *
     * @param zdbID the value to set.
     */
    public void setZdbID(String zdbID)
    {
        this.zdbID = zdbID;
    }

    public Set<Hit> getBlastHits() {
        return blastHits ; 
    }

    public void setBlastHits(Set<Hit> blastHits) {
        this.blastHits = blastHits ; 
    }
    
    
    /**
     * Get runCandidate.
     *
     * @return runCandidate as RunCandidate.
     */
    public RunCandidate getRunCandidate()
    {
        return runCandidate;
    }
    
    /**
     * Set runCandidate.
     *
     * @param runCandidate the value to set.
     */
    public void setRunCandidate(RunCandidate runCandidate)
    {
        this.runCandidate = runCandidate;
    }

    public int compareTo(Object o) {
        //todo: compareTo method is using Accession.getNumber(), so there's no zeropad happening
        return getName().compareToIgnoreCase( ((Query)o).getName());
    }
}



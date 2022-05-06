/**
 *  Class Query.
 */
package org.zfin.sequence.blast ;

import lombok.Getter;
import lombok.Setter;
import org.zfin.sequence.Accession;
import org.zfin.sequence.reno.RunCandidate;

import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
public class Query implements Comparable {

    private String zdbID ; 
    private Accession accession  ; 
    private RunCandidate runCandidate; 
    private Set<Hit> blastHits = new HashSet<>() ;

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
    
    public Set<Hit> getBlastHits() {
        return blastHits ; 
    }

    public void setBlastHits(Set<Hit> blastHits) {
        this.blastHits = blastHits ; 
    }
    
    
    /**
     *
     * Note: compareTo method is using Accession.getNumber(), so there's no zeropad happening
     * @param o
     * @return CompareTo value.  0 if same.
     */
    public int compareTo(Object o) {
        return getName().compareToIgnoreCase( ((Query)o).getName());
    }
}



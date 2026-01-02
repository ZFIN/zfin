/**
 *  Class Query.
 */
package org.zfin.sequence.blast ;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SortNatural;
import org.zfin.sequence.Accession;
import org.zfin.sequence.reno.RunCandidate;

import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@Entity
@Table(name = "blast_query")
public class Query implements Comparable {

    @Id
    @GeneratedValue(generator = "zdbIdGeneratorQuery")
    @org.hibernate.annotations.GenericGenerator(
            name = "zdbIdGeneratorQuery",
            strategy = "org.zfin.database.ZdbIdGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "type", value = "BQRY")
            }
    )
    @Column(name = "bqry_zdb_id", nullable = false)
    private String zdbID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bqry_accbk_pk_id", nullable = false)
    private Accession accession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bqry_runcan_zdb_id", nullable = false)
    private RunCandidate runCandidate;

    @OneToMany(mappedBy = "query", fetch = FetchType.LAZY)
    @OrderBy("score DESC, expectValue ASC")
    private Set<Hit> blastHits = new HashSet<>();

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
     *
     * Note: compareTo method is using Accession.getNumber(), so there's no zeropad happening
     * @param o
     * @return CompareTo value.  0 if same.
     */
    public int compareTo(Object o) {
        return getName().compareToIgnoreCase( ((Query)o).getName());
    }
}


